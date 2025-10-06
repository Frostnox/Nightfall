package frostnox.nightfall.entity;

import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public interface IHungerEntity extends IActionableEntity {
    void addSatiety(int amount);

    boolean isHungry();

    Diet getDiet();

    void eatBlock(BlockState state, BlockPos pos);

    void eatEntity(Entity entity);

    default boolean canEat(BlockState state) {
        if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state, getDiet());
        else return false;
    }

    default boolean canEat(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) return switch(getDiet()) {
            case HERBIVORE -> itemEntity.getItem().is(TagsNF.HERBIVORE_FOOD);
            case CARNIVORE -> itemEntity.getItem().is(TagsNF.CARNIVORE_FOOD);
            case OMNIVORE -> itemEntity.getItem().is(TagsNF.OMNIVORE_FOOD);
        };
        else return false;
    }

    default void doEatClient(ItemStack item) {
        ActionableEntity entity = getEntity();
        Random random = entity.getRandom();
        if(getEatSound() != null) {
            entity.level.playLocalSound(entity.getX(), entity.getEyeY(), entity.getZ(), getEatSound(), entity.getSoundSource(), 1F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        }
        if(!item.isEmpty()) {
            Vec3 eyePos = entity.getEyePosition();
            for(int i = 0; i < 4; i++) {
                Vec3 speed = new Vec3((random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, (random.nextFloat() - 0.5D) * 0.1D);
                speed = speed.xRot(-entity.getXRot() * (MathUtil.PI / 180F));
                speed = speed.yRot(-entity.getYRot() * (MathUtil.PI / 180F));
                Vec3 pos = new Vec3((random.nextFloat() - 0.5D) * 0.6D, (random.nextFloat() - 0.5D) * 0.6D,
                        entity.getBbWidth() + (random.nextFloat() - 0.5D) * 0.4D);
                pos = pos.yRot(-entity.yBodyRot * (MathUtil.PI / 180F));
                pos = pos.add(eyePos);
                entity.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, item), pos.x, pos.y, pos.z, speed.x, speed.y + 0.05D, speed.z);
            }
        }
    }

    SoundEvent getEatSound();
}
