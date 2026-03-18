package frostnox.nightfall.block.block.furnacechannel;

import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.fireable.FireableMetalBlock;
import frostnox.nightfall.block.block.meltedmetal.MeltedMetalBlockEntity;
import frostnox.nightfall.block.block.mold.BlockMoldBlockEntity;
import frostnox.nightfall.block.block.mold.ItemMoldBlock;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FurnaceChannelBlockEntity extends BlockEntity {
    private static final VoxelShape FLUID_PASSAGE = Block.box(7, 0, 7, 9, 16, 9);
    protected boolean wasCasting = false, lastForceChunk = true;
    public int visualDist;
    public float visualTemp;

    public FurnaceChannelBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.FURNACE_CHANNEL.get(), pos, state);
    }

    protected FurnaceChannelBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public boolean wasCasting() {
        return visualDist > 0;
    }

    public void startCasting(int dist, float temp) {
        if(visualDist != dist || visualTemp != temp) {
            visualDist = dist;
            visualTemp = temp;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
        wasCasting = true;
    }

    public void stopCasting() {
        if(wasCasting) {
            visualDist = 0;
            visualTemp = 0;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
        wasCasting = false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FurnaceChannelBlockEntity entity) {
        boolean forceChunk = false, casting = false;
        if(state.getFluidState().isEmpty()) {
            BlockPos.MutableBlockPos mutPos = pos.mutable();
            if(LevelUtil.getBlockHeatResistanceTier(level.getBlockState(mutPos.move(state.getValue(FurnaceChannelBlock.FACING)))) > 0) {
                if(level.getBlockEntity(mutPos.move(state.getValue(FurnaceChannelBlock.FACING))) instanceof MeltedMetalBlockEntity targetMetal) {
                    forceChunk = true;
                    if(!targetMetal.untouched || targetMetal.alloyTimer <= 0) {
                        mutPos.set(pos);
                        LevelChunk chunk = level.getChunkAt(pos);
                        BlockMoldBlockEntity mold = null;
                        int i = 0;
                        for(; i < 16; i++) {
                            BlockState castTarget = chunk.getBlockState(mutPos.setY(mutPos.getY() - 1));
                            if(castTarget.getBlock() instanceof ItemMoldBlock) mold = (BlockMoldBlockEntity) chunk.getBlockEntity(mutPos);
                            else if(!castTarget.isAir() && Shapes.joinIsNotEmpty(castTarget.getShape(level, mutPos), FLUID_PASSAGE, BooleanOp.AND)) break;
                        }
                        if(mold != null) {
                            if(mold.addFluid(new FluidStack(targetMetal.metal.getFluid().get(), 1), targetMetal.temperature)) {
                                entity.startCasting(i, targetMetal.temperature);
                                targetMetal.drain(1);
                                if(TieredHeat.fromTemp(targetMetal.temperature).getTier() > ((FurnaceChannelBlock) state.getBlock()).maxHeat.getTier() && level.random.nextInt(4) == 0) {
                                    level.destroyBlock(pos, false);
                                }
                                casting = true;
                            }
                        }
                    }
                }
                else {
                    BlockState neighbor = level.getBlockState(mutPos);
                    if(neighbor.getBlock() instanceof FireableMetalBlock && neighbor.getValue(FireableMetalBlock.LIT)) forceChunk = true;
                }
            }
        }
        if(!casting) entity.stopCasting();
        if(forceChunk != entity.lastForceChunk) LevelUtil.forceTickingChunk(level, pos, forceChunk);
        entity.lastForceChunk = forceChunk;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("dist", visualDist);
        tag.putFloat("temp", visualTemp);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        int oldDist = visualDist;
        visualDist = tag.getInt("dist");
        if(oldDist == 0 && visualDist > 0) ClientEngine.get().playFurnaceChannelPourSound(this);
        visualTemp = tag.getFloat("temp");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if(pkt.getTag() != null) handleUpdateTag(pkt.getTag());
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = getBlockState().getShape(level, getBlockPos()).bounds().move(getBlockPos());
        if(visualDist > 0) {
            Direction dir = getBlockState().getValue(FurnaceChannelBlock.FACING);
            float x = dir.getStepX() * 2F/16F, z = dir.getStepZ() * 2F/16F;
            return new AABB(box.minX - x, box.minY - 4F/16F - visualDist, box.minZ - z, box.maxX + x, box.maxY, box.maxZ + z);
        }
        return box;
    }
}
