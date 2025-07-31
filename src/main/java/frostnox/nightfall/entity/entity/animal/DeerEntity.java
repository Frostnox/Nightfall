package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.goals.*;
import frostnox.nightfall.entity.ai.sensing.AmplifiedAudioSensing;
import frostnox.nightfall.entity.ai.sensing.AudioSensing;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DeerEntity extends AnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        BRIAR, RED, SPOTTED
    }
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(DeerEntity.class, DataSerializersNF.DEER_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Sex> SEX = SynchedEntityData.defineId(DeerEntity.class, DataSerializersNF.SEX);
    public int sprintTime, sprintTimeLast;

    public DeerEntity(EntityType<? extends DeerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 25D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 24)
                .add(AttributesNF.FROST_ABSORPTION.get(), 0.2);
    }

    public Type getDeerType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    public Sex getSex() {
        return getEntityData().get(SEX);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new StepUpFleeEntityGoal<>(this, LivingEntity.class, 1.6D, 1.8D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.DEER_PREDATOR);
        }));
        goalSelector.addGoal(3, new StepUpFleeDamageGoal(this, 1.6D));
        goalSelector.addGoal(4, new ForageItemGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(5, new ForageBlockGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(6, new ReducedWanderLandGoal(this, 0.8D, 8));
    }

    @Override
    public void tick() {
        super.tick();
        sprintTimeLast = sprintTime;
        if(isAlive()) {
            IActionTracker capA = getActionTracker();
            if(capA.isCharging() && capA.getFrame() > 100 && capA.getActionID().equals(ActionsNF.DEER_GRAZE.getId())) {
                if(random.nextFloat() < 0.005F) queueAction();
            }
            if(getStepHeight() >= 1) {
                setSprinting(true);
                if(sprintTime < 9) sprintTime++;
            }
            else {
                setSprinting(false);
                if(sprintTime > 0) sprintTime--;
            }
            if(!level.isClientSide && sprintTime == 0 && capA.isInactive() && getBlockStateOn().is(TagsNF.TILLABLE_SOIL)) {
                if(random.nextFloat() < 0.0015F * (1.9 - getSatietyPercent())) startAction(ActionsNF.DEER_GRAZE.getId());
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return pTarget.canBeSeenAsEnemy();
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }

    @Override
    protected AudioSensing createAudioSensing() {
        return new AmplifiedAudioSensing(this, 10, 1.25F);
    }

    @Override
    public float getVisionAngle() {
        return 180F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.SPOTTED);
        entityData.define(SPECIAL, false);
        entityData.define(SEX, Sex.FEMALE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getDeerType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        tag.putBoolean("female", getSex() == Sex.FEMALE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        if(tag.contains("female")) getEntityData().set(SEX, tag.getBoolean("female") ? Sex.FEMALE : Sex.MALE);
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        Type type;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            float temperature = ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition())).getTemperature(blockPosition());
            if(temperature < 0.35F) type = Type.RED;
            else if(temperature > 0.65F) type = Type.BRIAR;
            else type = Type.SPOTTED;
            spawnDataIn = new GroupData(type);
        }
        getEntityData().set(TYPE, type);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        if(random.nextBoolean()) getEntityData().set(SEX, Sex.MALE);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVoicePitch() {
        if(getSex() == Sex.MALE) return super.getVoicePitch() * 0.8F;
        else return super.getVoicePitch();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.DEER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.DEER_STEP.get(), 0.15F, 1.3F);
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public OBB[] getOBBs(float partial) {
        if(!isAlive()) return new OBB[0];
        else {
            float yaw = getViewYRot(partial);
            Quaternion headRot = Vector3f.YP.rotationDegrees(-yaw);
            Quaternion neckRot = Vector3f.YP.rotationDegrees(-Mth.clamp(yaw, yaw - 60, yaw + 60));
            Vector3f neck = new Vector3f(0, 12F/16F, 5.5F/16F);
            Vector3f head = new Vector3f(0, 7F/16F, 0F/16F);
            float scale = 1F;
            if(getSex() == Sex.MALE) {
                scale = 17F/16F;
                neck.mul(scale);
                head.mul(scale);
                head.add(0, 1F/16F, 0);
            }
            neck.transform(neckRot);
            boolean grazing = getActionTracker().getActionID().equals(ActionsNF.DEER_GRAZE.getId());
            neckRot.mul(Vector3f.XP.rotationDegrees(grazing ? 140 : 15));
            head.transform(Vector3f.XP.rotationDegrees(grazing ? 140 : 15));
            head.add(0, 12F/16F, 5.5F/16F);
            head.transform(headRot);
            if(grazing) headRot.mul(Vector3f.XP.rotationDegrees(90));
            else headRot.mul(Vector3f.XP.rotationDegrees(getViewXRot(partial)));
            return new OBB[] {
                    new OBB(3.5F/16F * scale, 8.5F/16F * scale, 3.5F/16F * scale, 0, 4F/16F, 0, neck.x(), neck.y(), neck.z(), neckRot),
                    new OBB(4.5F/16F * scale, 4.5F/16F * scale, 4.5F/16F * scale, 0, 2F/16F, 0, head.x(), head.y(), head.z(), headRot)
            };
        }
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.6, bb.minY, bb.minZ - 0.6, bb.maxX + 0.6, bb.maxY + 0.6, bb.maxZ + 0.6);
    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.DEER_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(ItemStack item) {
        return item.is(TagsNF.DEER_FOOD_ITEM);
    }

    @Override
    public void doEatParticlesClient(ItemStack item) {
        if(getEatSound() != null) {
            level.playLocalSound(getX(), getEyeY(), getZ(), getEatSound(), SoundSource.NEUTRAL, 0.5F + 0.5F * (float)random.nextInt(2),
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
        }
        for(int i = 0; i < 4; i++) {
            Vec3 speed = new Vec3(((double)random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)random.nextFloat() - 0.5D) * 0.1D);
            speed = speed.xRot(-getXRot() * ((float)Math.PI / 180F));
            speed = speed.yRot(-getYRot() * ((float)Math.PI / 180F));
            Vec3 pos = new Vec3(((double)random.nextFloat() - 0.5D) * 0.8D, (double)(-random.nextFloat()) * 0.6D - 0.3D,
                    0.25D + ((double)random.nextFloat() - 0.5D) * 0.4D);
            pos = pos.yRot(-yBodyRot * ((float)Math.PI / 180F));
            pos = pos.add(getX(), getEyeY(), getZ());
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, item), pos.x, pos.y, pos.z, speed.x, speed.y + 0.05D, speed.z);
        }
    }

    @Override
    public SoundEvent getEatSound() {
        return null; //TODO:
    }
}
