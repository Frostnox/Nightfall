package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.block.block.nest.NestBlockEntity;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.IHomeEntity;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.ai.goals.*;
import frostnox.nightfall.entity.ai.sensing.AmplifiedAudioSensing;
import frostnox.nightfall.entity.ai.sensing.AudioSensing;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class RabbitEntity extends AnimalEntity implements IOrientedHitBoxes, IHomeEntity {
    public enum Type {
        BRUSH, COTTONTAIL, ARCTIC, STRIPED
    }
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(RabbitEntity.class, DataSerializersNF.RABBIT_TYPE);
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(RabbitEntity.class, EntityDataSerializers.BOOLEAN);
    protected BlockPos homePos = null;

    public RabbitEntity(EntityType<? extends RabbitEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH / 2);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 4)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(AttributesNF.HEARING_RANGE.get(), 30)
                .add(AttributesNF.FROST_ABSORPTION.get(), 0.2);
    }

    public Type getRabbitType() {
        return getEntityData().get(TYPE);
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new StepUpFleeEntityOrHomeGoal<>(this, LivingEntity.class, 1.6D, 1.8D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.RABBIT_PREDATOR);
        }));
        goalSelector.addGoal(3, new MoveToNestGoal(this, 1.3D, 1.1D) {
            @Override
            public boolean canUse() {
                if(LevelUtil.isDayTimeWithin(level, LevelUtil.MORNING_TIME, LevelUtil.SUNSET_TIME) || level.random.nextInt() % 16 != 0) return false;
                else return super.canUse();
            }
        });
        goalSelector.addGoal(4, new StepUpFleeDamageGoal(this, 1.8D));
        goalSelector.addGoal(5, new ForageItemGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(6, new ForageBlockGoal(this, 1.3D, 20, 2));
        goalSelector.addGoal(7, new ReducedWanderLandGoal(this, 1.3D, 16));
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return pTarget.canBeSeenAsEnemy();
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex == 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }

    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        if(dead && homePos != null && !level.isClientSide && level.getBlockEntity(homePos) instanceof NestBlockEntity nest) {
            nest.stopTrackingEntity(getUUID());
        }
    }

    @Override
    protected AudioSensing createAudioSensing() {
        return new AmplifiedAudioSensing(this, 10, 1.5F);
    }

    @Override
    public float getVisionAngle() {
        return 180F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.BRUSH);
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int type = getRabbitType().ordinal();
        if(type != 0) tag.putInt("type", type);
        boolean special = isSpecial();
        if(special) tag.putBoolean("special", special);
        if(homePos != null) tag.put("homePos", NbtUtils.writeBlockPos(homePos));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        if(tag.contains("homePos")) homePos = NbtUtils.readBlockPos(tag.getCompound("homePos"));
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }

        public static GroupData create(float temp, float humidity) {
            Type type;
            if(temp < 0.3F) type = Type.ARCTIC;
            else if(humidity > 0.7F) type = Type.STRIPED;
            else if(temp > 0.7F) type = Type.COTTONTAIL;
            else type = Type.BRUSH;
            return new GroupData(type);
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        Type type;
        if(spawnDataIn instanceof GroupData data) type = data.type;
        else {
            IChunkData chunkData = ChunkData.get(worldIn.getLevel().getChunkAt(blockPosition()));
            float temp = chunkData.getTemperature(blockPosition());
            float humidity = chunkData.getHumidity(blockPosition());
            spawnDataIn = GroupData.create(temp, humidity);
            type = ((GroupData) spawnDataIn).type;
        }
        getEntityData().set(TYPE, type);
        if(random.nextInt() % 8192 == 0) getEntityData().set(SPECIAL, true);
        return spawnDataIn;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height * 0.95F;
    }

    @Override
    protected void pushEntities() {

    }

    @Override
    protected void doPush(Entity pEntity) {

    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 60;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {

    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public OBB[] getOBBs(float partial) {
        if(!isAlive()) return new OBB[0];
        else {
            Quaternion rot = Vector3f.YP.rotationDegrees(-getViewYRot(partial));
            Vector3f head = new Vector3f(0, 4.5F/16F, 3.5F/16F);
            head.transform(rot);
            rot.mul(Vector3f.XP.rotationDegrees(getViewXRot(partial)));
            return new OBB[] {
                    new OBB(3.5F/16F, 4F/16F, 4.5F/16F, 0, 1F/16F, 1.5F/16F, head.x(), head.y(), head.z(), rot)
            };
        }
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.5, bb.minY, bb.minZ - 0.5, bb.maxX + 0.5, bb.maxY + 0.5, bb.maxZ + 0.5);
    }

    @Override
    public @Nullable BlockPos getHomePos() {
        return homePos;
    }

    @Override
    public void setHomePos(@Nullable BlockPos pos) {
        homePos = pos;
    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.RABBIT_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(ItemStack item) {
        return item.is(TagsNF.RABBIT_FOOD_ITEM);
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
