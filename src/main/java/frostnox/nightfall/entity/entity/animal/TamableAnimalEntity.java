package frostnox.nightfall.entity.entity.animal;

import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.entity.EatItemToClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class TamableAnimalEntity extends AnimalEntity {
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(TamableAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(TamableAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    public final Sex sex;
    public boolean tamable;
    protected int breedTime, gestationTime;

    public TamableAnimalEntity(EntityType<? extends AnimalEntity> type, Level level, Sex sex) {
        super(type, level);
        this.sex = sex;
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public boolean isTamed() {
        return getEntityData().get(TAMED);
    }

    protected abstract boolean isTameItem(ItemStack item);

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return !isLeashed() && isTamed();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(isAlive()) {
            if(breedTime > 0) breedTime--;
            else if(breedTime < 0) breedTime++;
        }
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if(tamable && !isTamed()) {
            ItemStack item = pPlayer.getItemInHand(pHand);
            if(isTameItem(item)) {
                if(!level.isClientSide) {
                    NetworkHandler.toAllTracking(this, new EatItemToClient(item.copy(), getId()));
                    item.shrink(1);
                    getEntityData().set(TAMED, true);
                    if(getCollapseAction() != null && getActionTracker().getActionID().equals(getCollapseAction()) && getActionTracker().isCharging()) {
                        getActionTracker().queue();
                        NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, getId()));
                    }
                }
                return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void simulateTime(long timePassed) {
        super.simulateTime(timePassed);
        if(isAlive()) {

        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPECIAL, false);
        entityData.define(TAMED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("special", isSpecial());
        tag.putBoolean("tamed", isTamed());
        tag.putBoolean("tamable", tamable);
        tag.putInt("breedTime", breedTime);
        tag.putInt("gestationTime", gestationTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        getEntityData().set(TAMED, tag.getBoolean("tamed"));
        tamable = tag.getBoolean("tamable");
        breedTime = tag.getInt("breedTime");
        gestationTime = tag.getInt("gestationTime");
    }
}
