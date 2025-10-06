package frostnox.nightfall.entity.entity.animal;

import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.ITamable;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.entity.EatItemToClient;
import frostnox.nightfall.registry.KnowledgeNF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class TamableAnimalEntity extends AnimalEntity implements ITamable {
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(TamableAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(TamableAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    public final Sex sex;
    public boolean tamable;
    protected int breedTime, gestationTime;
    protected boolean comfortable;

    public TamableAnimalEntity(EntityType<? extends AnimalEntity> type, Level level, Sex sex) {
        super(type, level);
        this.sex = sex;
    }

    public int getBreedTime() {
        return breedTime;
    }

    public int getGestationTime() {
        return gestationTime;
    }

    public boolean isComfortable() {
        return comfortable;
    }

    public void endGestation() {
        gestationTime = 0;
        onGestationEnd();
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    @Override
    public boolean isTamed() {
        return getEntityData().get(TAMED);
    }

    public boolean canBreed() {
        return breedTime > 0 && gestationTime == 0 && comfortable;
    }

    public final void breedPair(TamableAnimalEntity other) {
        breedWith(other);
        other.breedWith(this);
        breedTime = 0;
        other.breedTime = 0;
    }

    protected void updateGoals() {

    }

    protected void onGestationEnd() {

    }

    protected void onFeed() {
        breedTime = 20 * 20;
    }

    public abstract boolean canBreedWith(TamableAnimalEntity other);

    protected abstract void breedWith(TamableAnimalEntity other);

    protected abstract boolean checkComfort();

    public abstract boolean isFeedItem(ItemStack item);

    public abstract ResourceLocation getBreedAction();

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return !isLeashed() && isTamed();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(isAlive()) {
            if(breedTime > 0) breedTime--;
            if(gestationTime > 0) {
                gestationTime--;
                if(gestationTime == 0) onGestationEnd();
            }
            if(!level.isClientSide && randTickCount % 30 == 0) comfortable = checkComfort();
        }
    }

    @Override
    public void addSatiety(int amount) {
        if(satiety == 0 && gestationTime == 0 && amount >= getMaxSatiety() && isTamed()) onFeed();
        super.addSatiety(amount);
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if(!isTamed()) {
            if(tamable) {
                ItemStack item = pPlayer.getItemInHand(pHand);
                if(isFeedItem(item)) {
                    if(!level.isClientSide) {
                        PlayerData.get(pPlayer).addKnowledge(KnowledgeNF.TAMED_ANIMAL.getId());
                        NetworkHandler.toAllTracking(this, new EatItemToClient(item.copy(), getId()));
                        item.shrink(1);
                        getEntityData().set(TAMED, true);
                        setTarget(null);
                        lastTargetPos = null;
                        setAggressive(false);
                        noDespawnTicks = -1;
                        satiety = getMaxSatiety();
                        updateGoals();
                        if(getCollapseAction() != null && getActionTracker().getActionID().equals(getCollapseAction()) && getActionTracker().isCharging()) {
                            getActionTracker().queue();
                            NetworkHandler.toAllTracking(this, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, getId()));
                        }
                    }
                    return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
                }
            }
        }
        else if(breedTime == 0 && satiety == 0 && gestationTime == 0) {
            ItemStack item = pPlayer.getItemInHand(pHand);
            if(isFeedItem(item)) {
                if(!level.isClientSide) {
                    NetworkHandler.toAllTracking(this, new EatItemToClient(item.copy(), getId()));
                    item.shrink(1);
                    addSatiety(getMaxSatiety());
                }
                return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void simulateTime(int timePassed) {
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
        tag.putBoolean("comfortable", comfortable);
        tag.putInt("breedTime", breedTime);
        tag.putInt("gestationTime", gestationTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        getEntityData().set(TAMED, tag.getBoolean("tamed"));
        tamable = tag.getBoolean("tamable");
        comfortable = tag.getBoolean("comfortable");
        breedTime = tag.getInt("breedTime");
        gestationTime = tag.getInt("gestationTime");
    }
}
