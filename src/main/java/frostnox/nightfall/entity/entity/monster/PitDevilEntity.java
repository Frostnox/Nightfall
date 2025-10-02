package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.ai.goal.*;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class PitDevilEntity extends HungryMonsterEntity implements IOrientedHitBoxes {
    public static final int GROWL_DURATION = 12 * 20;
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<Boolean> SPECIAL = SynchedEntityData.defineId(CockatriceEntity.class, EntityDataSerializers.BOOLEAN);
    public int growlTicks;

    public PitDevilEntity(EntityType<? extends HungryMonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 80D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.STRIKING_DEFENSE.get(), 0.25)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.5)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.5)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.25)
                .add(AttributesNF.FROST_DEFENSE.get(), 0.25)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), 0.25)
                .add(AttributesNF.POISE.get(), Poise.MEDIUM.ordinal());
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2F/16F, -5.5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    public boolean isSpecial() {
        return getEntityData().get(SPECIAL);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LandFleeEntityGoal<>(this, LivingEntity.class, 1.35D, 1.45D, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else return entity.getType().is(TagsNF.PIT_DEVIL_PREDATOR);
        }));
        goalSelector.addGoal(3, new LandFleeTargetGoal(this, 1.35D, 1.45D));
        goalSelector.addGoal(4, new RushAttackGoal(this, 1.35D) {
            @Override
            protected boolean canPursue() {
                return ActionTracker.isPresent(mob) && (getActionTracker().getState() == 2 || !getActionTracker().getActionID().equals(ActionsNF.PIT_DEVIL_GROWL.getId()));
            }

            @Override
            public void stop() {
                super.stop();
                if(mob.isAlive()) mob.getActionTracker().releaseCharge();
            }
        });
        goalSelector.addGoal(5, new FleeDamageGoal(this, 1.35D));
        goalSelector.addGoal(6, new EatEntityGoal(this, 1D, 15, 2));
        goalSelector.addGoal(7, new EatBlockGoal(this, 1D, 15, 2));
        goalSelector.addGoal(8, new WanderLandGoal(this, 0.8D));
        goalSelector.addGoal(9, new RandomLookGoal(this, 0.02F / 2));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, LivingEntity.class, true, (entity) -> {
            if(entity.isDeadOrDying()) return false;
            else if(entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
            else return entity.getType().is(TagsNF.PIT_DEVIL_PREY);
        }));
    }

    @Override
    public boolean shouldFleeFrom(LivingEntity target) {
        return super.shouldFleeFrom(target) && !target.getType().is(TagsNF.PIT_DEVIL_PREY);
    }

    @Override
    protected void simulateTime(int timePassed) {
        growlTicks = Math.max(0, growlTicks - timePassed);
    }

    @Override
    public void tick() {
        super.tick();
        if(!isRemoved() && getActionTracker().isInactive() && getTarget() == null && growlTicks > 0) growlTicks--;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPECIAL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("special", isSpecial());
        tag.putInt("growlTicks", growlTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(SPECIAL, tag.getBoolean("special"));
        growlTicks = tag.getInt("growlTicks");
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        if(random.nextInt() % 4096 == 0) getEntityData().set(SPECIAL, true);
        return spawnDataIn;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if(isDeadOrDying()) return new EntityDimensions(12F/16F, 7F/16F, false).scale(getScale());
        else return getType().getDimensions().scale(getScale());
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        if(distanceSqr < 2 * 2) return ActionsNF.PIT_DEVIL_BITE.getId();
        else return growlTicks <= 0 ? ActionsNF.PIT_DEVIL_GROWL.getId() : ActionsNF.PIT_DEVIL_BITE.getId();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof PitDevilEntity);
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.PIT_DEVIL_PREY) || target instanceof Player;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 20 * 90;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getTarget() == null && tickCount > 100) return SoundsNF.PIT_DEVIL_HOWL.get();
        else return null;
    }

    @Override
    public void playAmbientSound() {
        SoundEvent sound = getAmbientSound();
        if(sound != null) level.playSound(null, this, sound, SoundSource.HOSTILE, 2F, 1F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.PIT_DEVIL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.PIT_DEVIL_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.PIT_DEVIL_STEP.get(), 0.2F, 0.75F);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        if(boxIndex == 0) return hitPos.y > 13F/16F ? EquipmentSlot.LEGS : EquipmentSlot.HEAD;
        else return hitPos.y > 8F/16F ? EquipmentSlot.CHEST : EquipmentSlot.LEGS;
    }

    @Override
    protected float modifyIncomingDamageBySlot(EquipmentSlot slot, float damage) {
        if(slot == EquipmentSlot.HEAD) return damage * 1.5F;
        else if(slot == EquipmentSlot.LEGS) return damage * 1.25F;
        else return damage;
    }

    @Override
    public Impact modifyIncomingImpact(DamageTypeSource source, Impact impact) {
        EquipmentSlot slot = getHitSlot(source.getHitCoords(), source.getHitBoxIndex());
        return (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.HEAD) ? impact.increase() : impact;
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0F/16F, 9F/16F, 0F/16F);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -2F/16F, -5.5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[] getDefaultOBBs() {
        return new OBB[] {
                new OBB(6.5F/16F, 5.5F/16F, 6.5F/16F, 0, 0.5F/16F, 3F/16F)
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.75, bb.minY, bb.minZ - 0.75, bb.maxX + 0.75, bb.maxY + 0.5, bb.maxZ + 0.75);
    }

    @Override
    protected int getMaxSatiety() {
        return (int) (ContinentalWorldType.DAY_LENGTH / 2);
    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.PIT_DEVIL_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) return itemEntity.getItem().is(TagsNF.PIT_DEVIL_FOOD_ITEM);
        else if(entity instanceof LivingEntity livingEntity) return livingEntity.deathTime > 20 && entity.getType().is(TagsNF.EDIBLE_CORPSE);
        else return false;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundsNF.PIT_DEVIL_EAT.get();
    }
}
