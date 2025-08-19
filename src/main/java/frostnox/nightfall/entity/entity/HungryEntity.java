package frostnox.nightfall.entity.entity;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.IHungerEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.entity.EatItemToClient;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class HungryEntity extends ActionableEntity implements IHungerEntity {
    protected int satiety;

    public HungryEntity(EntityType<? extends ActionableEntity> type, Level level) {
        super(type, level);
    }

    public double getSatietyPercent() {
        return ((double) satiety) / getMaxSatiety();
    }

    public int getSatiety() {
        return satiety;
    }

    protected abstract int getMaxSatiety();

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if(satiety > 0) satiety--;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(satiety > 0) tag.putInt("satiety", satiety);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        satiety = tag.getInt("satiety");
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        satiety = getMaxSatiety() / 2 + random.nextInt(getMaxSatiety() / 2);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void addSatiety(int amount) {
        satiety = Mth.clamp(satiety + amount, 0, getMaxSatiety());
    }

    @Override
    public boolean isHungry() {
        return satiety <= 0;
    }

    @Override
    public void eatBlock(BlockState state, BlockPos pos) {
        if(canEat(state)) {
            playSound(getEatSound(), 1F, 1F);
            if(state.getBlock() instanceof IFoodBlock foodBlock) foodBlock.eat(this, level, pos);
            else LevelUtil.destroyBlockNoSound(level, pos, true, this);
            satiety = getMaxSatiety();
        }
    }

    @Override
    public void eatEntity(Entity entity) {
        if(!level.isClientSide) {
            if(canEat(entity)) {
                if(entity instanceof ItemEntity itemEntity) {
                    ItemStack item = itemEntity.getItem();
                    NetworkHandler.toAllTracking(this, new EatItemToClient(item.copy(), getId()));
                    item.setCount(item.getCount() - 1);
                }
                else {
                    if(entity instanceof ActionableEntity actionable) {
                        actionable.forceDropAllDeathLoot(actionable.getLastDamageSource() == null ? DamageTypeSource.GENERIC : actionable.getLastDamageSource());
                    }
                    if(entity instanceof LivingEntity livingEntity) {
                        level.broadcastEntityEvent(entity, (byte) 60);
                        ItemStack foodDrop = ItemStack.EMPTY;
                        for(Item item : LevelUtil.getAllLootItems(livingEntity.getLootTable(), (ServerLevel) level)) {
                            if(item.builtInRegistryHolder().is(TagsNF.ANIMAL_EDIBLE_MEAT)) {
                                foodDrop = new ItemStack(item);
                                break;
                            }
                        }
                        NetworkHandler.toAllTracking(this, new EatItemToClient(foodDrop, getId()));
                    }
                    entity.remove(RemovalReason.KILLED);
                }
                satiety = getMaxSatiety();
            }
        }
    }
}
