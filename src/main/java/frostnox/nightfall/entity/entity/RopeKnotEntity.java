package frostnox.nightfall.entity.entity;

import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class RopeKnotEntity extends LeashFenceKnotEntity {
    public RopeKnotEntity(EntityType<? extends RopeKnotEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static LeashFenceKnotEntity getOrCreateKnot(Level pLevel, BlockPos pPos) {
        int x = pPos.getX();
        int y = pPos.getY();
        int z = pPos.getZ();
        for(LeashFenceKnotEntity knot : pLevel.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB(x - 1.0D, y - 1.0D, z - 1.0D, x + 1.0D, y + 1.0D, z + 1.0D))) {
            if(knot.getPos().equals(pPos)) return knot;
        }
        RopeKnotEntity knot = EntitiesNF.ROPE_KNOT.get().create(pLevel);
        knot.setPos(x, y, z);
        pLevel.addFreshEntity(knot);
        return knot;
    }

    @Override
    public void tick() {
        if(!level.isClientSide) checkOutOfWorld();
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {
        playSound(SoundsNF.ROPE_KNOT_BREAK.get(), 1.0F, 1.0F);
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundsNF.ROPE_KNOT_PLACE.get(), 1.0F, 1.0F);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ItemsNF.ROPE.get());
    }
}
