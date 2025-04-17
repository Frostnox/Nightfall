package frostnox.nightfall.item.item;

import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStandDummyItem extends ItemNF {
    public final ResourceLocation sourceMaterial;

    public ArmorStandDummyItem(ResourceLocation sourceMaterial, Properties properties) {
        super(properties);
        this.sourceMaterial = sourceMaterial;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level level = context.getLevel();
            BlockPlaceContext blockplacecontext = new BlockPlaceContext(context);
            BlockPos blockpos = blockplacecontext.getClickedPos();
            ItemStack itemstack = context.getItemInHand();
            Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
            AABB aabb = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
            if (level.noCollision((Entity)null, aabb) && level.getEntities((Entity)null, aabb).isEmpty()) {
                if (level instanceof ServerLevel serverLevel) {
                    ArmorStandDummyEntity armorStand = EntitiesNF.ARMOR_STAND.get().create(serverLevel, itemstack.getTag(), (Component)null, context.getPlayer(), blockpos, MobSpawnType.SPAWN_EGG, true, true);
                    if(armorStand == null) return InteractionResult.FAIL;
                    armorStand.setMaterial(sourceMaterial);

                    float yaw = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    armorStand.setYBodyRot(yaw); //Fix vanilla rendering bug where yaw is not synced until after the first tick
                    armorStand.moveTo(armorStand.getX(), armorStand.getY(), armorStand.getZ(), yaw, 0.0F);
                    serverLevel.addFreshEntityWithPassengers(armorStand);
                    level.playSound((Player)null, armorStand.getX(), armorStand.getY(), armorStand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, armorStand);
                }

                itemstack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.FAIL;
            }
        }
    }
}
