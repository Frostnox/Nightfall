package frostnox.nightfall.entity.entity.animal;

import frostnox.nightfall.entity.ai.goal.FloatAtHeightGoal;
import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class BabyAnimalEntity extends AnimalEntity {
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(BabyAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    protected final int matureTime;
    protected int age;

    public BabyAnimalEntity(EntityType<? extends AnimalEntity> type, Level level, int matureTime) {
        super(type, level);
        this.matureTime = matureTime;
    }

    protected abstract ActionableEntity createMatureEntity();

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public int getAge() {
        if(level.isClientSide) return 0;
        else return age;
    }

    public void setAge(int age) {
        this.age = age;
        if(age >= matureTime) {
            ActionableEntity adult = createMatureEntity();
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            discard();
            level.addFreshEntity(adult);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(isAlive() && !level.isClientSide) {
            setAge(age + 1);
        }
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return !isLeashed();
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        return InteractionResult.PASS;
    }

    @Override
    protected void simulateTime(long timePassed) {
        super.simulateTime(timePassed);
        if(isAlive()) {
            if(timePassed > matureTime) setAge(age + matureTime);
            else setAge(age + (int) timePassed);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("age", getAge());
        tag.putBoolean("special", isSpecial());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAge(tag.getInt("age"));
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
    }

    @Override
    public float getPushResistance() {
        return PUSH_ZERO;
    }

    @Override
    public float getPushForce() {
        return PUSH_ZERO;
    }
}
