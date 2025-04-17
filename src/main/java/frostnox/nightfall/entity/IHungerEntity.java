package frostnox.nightfall.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IHungerEntity extends IActionableEntity {
    void addSatiety(int amount);

    boolean isHungry();

    boolean canEat(BlockState state);

    void eatBlock(BlockState state, BlockPos pos);

    boolean canEat(ItemStack item);

    void eatItem(ItemEntity itemEntity);

    void doEatParticlesClient(ItemStack item);

    SoundEvent getEatSound();
}
