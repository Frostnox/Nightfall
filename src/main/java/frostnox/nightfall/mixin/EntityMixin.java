package frostnox.nightfall.mixin;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Fix for MC-1133: Whether or not a player experiences some effect is calculated based on the block under the center of the player.
 * Adjusts water fall damage.
 */
@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements Nameable, EntityAccess, CommandSource, IForgeEntity {
    @Shadow public Level level;
    @Shadow private Vec3 position;
    @Shadow private BlockPos blockPosition;
    @Shadow private ChunkPos chunkPosition;
    @Shadow @Nullable private BlockState feetBlockState;
    @Shadow private EntityInLevelCallback levelCallback;

    @Shadow @Nullable public abstract GameEventListenerRegistrar getGameEventListenerRegistrar();
    @Shadow public abstract boolean isRemoved();

    private EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Redirect(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    public void nightfall$cancelInWaterFallReset(Entity entity) {
        //Reduce fall distance over time instead of immediately
        entity.fallDistance *= 0.85F;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    public void nightfall$cancelDiveWaterFallReset(Entity entity) {
        //No immediate reset when hitting water at high speed
    }

    /**
     * @author Frostnox
     * @reason No need to guess at block 0.5 below now that block position is more accurate
     */
    @Overwrite
    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
        if(position.y % 1 == 0) return this.blockPosition().below();
        else return this.blockPosition();
    }

    @Inject(method = "getOnPos", at = @At(value = "TAIL"), cancellable = true)
    public void nightfall$getOnPos(CallbackInfoReturnable<BlockPos> callbackInfo) {
        int i = Mth.floor(this.position.x);
        int j = Mth.floor(this.position.y - (double)0.2F);
        int k = Mth.floor(this.position.z);
        BlockPos pos = new BlockPos(i, j, k);
        if(callbackInfo.getReturnValue().equals(pos)) {  //Use block position unless block was a special case (walls/fences)
            if(position.y % 1 == 0) callbackInfo.setReturnValue(this.blockPosition().below());
            else callbackInfo.setReturnValue(this.blockPosition());
        }
    }

    private boolean isNotOnBlock(BlockState state, BlockPos pos, CollisionContext context) {
        if(state.isAir()) return true;
        VoxelShape shape = state.getCollisionShape(this.level, pos, context);
        return shape.isEmpty() || shape.bounds().maxY + pos.getY() != this.position.y;
    }

    /**
     * Calculate stood-on block using entity dimensions instead of center position to fix bugs:
     * MC-1133: Whether or not a player experiences some effect is calculated based on the block under the center of the player
     */
    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityInLevelCallback;onMove()V"))
    private void nightfall$adjustStandingBlockPosition(CallbackInfo callbackInfo) {
        //Target players only, could apply to all entities but would require checking chunk loading and improved searching for larger entities
        if((Object) this instanceof Player) {
            double x = position.x, y = position.y, z = position.z;
            boolean onEvenHeight = y % 1 == 0;
            int xCenter = Mth.floor(x), zCenter = Mth.floor(z), yOn = onEvenHeight ? Mth.floor(y) - 1 : Mth.floor(y);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(xCenter, yOn, zCenter);
            BlockState state = this.level.getBlockState(pos);
            CollisionContext collisionContext = CollisionContext.of((Entity) (Object) this);
            if(isNotOnBlock(state, pos, collisionContext)) { //Only search for other blocks when center block is not stood on
                //Select block position based on entity dimensions, not just position
                AABB bb = getBoundingBox();
                double xWidth = bb.getXsize() / 2, zWidth = bb.getZsize() / 2;
                int xMin = Mth.floor(x - xWidth), xMax = Mth.floor(x + xWidth);
                int zMin = Mth.floor(z - zWidth), zMax = Mth.floor(z + zWidth);
                Set<BlockPos> blocks = new ObjectOpenHashSet<>(4);
                blocks.add(new BlockPos(xMin, yOn, zMin));
                if(zMax != zMin) blocks.add(new BlockPos(xMin, yOn, zMax));
                if(xMax != xMin) blocks.add(new BlockPos(xMax, yOn, zMin));
                if(blocks.size() == 3) blocks.add(new BlockPos(xMax, yOn, zMax));
                double minDistSqr = Double.MAX_VALUE;
                for(BlockPos blockPos : blocks) {
                    BlockState blockState = this.level.getBlockState(blockPos);
                    if(isNotOnBlock(blockState, blockPos, collisionContext)) continue;
                    double xDist = blockPos.getX() - x;
                    double zDist = blockPos.getZ() - z;
                    double distSqr = xDist * xDist + zDist * zDist;
                    if(distSqr < minDistSqr) { //Select closest block to center
                        minDistSqr = distSqr;
                        pos.set(blockPos);
                    }
                }
            }
            if(onEvenHeight) pos.move(Direction.UP);
            if(!pos.equals(this.blockPosition)) {
                this.blockPosition = pos.immutable();
                this.feetBlockState = null;
                if(SectionPos.blockToSectionCoord(pos.getX()) != this.chunkPosition.x || SectionPos.blockToSectionCoord(pos.getZ()) != this.chunkPosition.z) {
                    this.chunkPosition = new ChunkPos(this.blockPosition);
                }
            }
        }
    }
}
