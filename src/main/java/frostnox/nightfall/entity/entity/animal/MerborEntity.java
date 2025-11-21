package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.OBB;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class MerborEntity extends TamableAnimalEntity implements IOrientedHitBoxes {
    public enum Type {
        BOG, BRINE, RIVER
    }
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<Type> TYPE = SynchedEntityData.defineId(MerborEntity.class, DataSerializersNF.MERBOR_TYPE);
    public @Nullable Type fatherType;

    public MerborEntity(EntityType<? extends AnimalEntity> type, Level level, Sex sex) {
        super(type, level, sex);
    }

    public static MerborEntity createFemale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new MerborEntity(type, level, Sex.FEMALE);
    }

    public static MerborEntity createMale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new MerborEntity(type, level, Sex.MALE);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.5)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), -0.5);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, 0F/16F, -7F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F)));
        return map;
    }

    public Type getMerborType() {
        return getEntityData().get(TYPE);
    }

    @Override
    public boolean canTargetFromSound(LivingEntity target) {
        return target.getType().is(TagsNF.MERBOR_PREY) || (target instanceof Player player && !player.isCreative() && !player.isSpectator());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && !(target instanceof MerborEntity || target instanceof MerborBabyEntity);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, Type.RIVER);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", getMerborType().ordinal());
        if(fatherType != null) tag.putInt("fatherType", fatherType.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, Type.values()[tag.getInt("type")]);
        if(tag.contains("fatherType")) fatherType = Type.values()[tag.getInt("fatherType")];
        updateGoals();
    }

    @Override
    public @Nullable ResourceLocation getCollapseAction() {
        return ActionsNF.MERBOR_COLLAPSE.getId();
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    public static class GroupData extends AgeableMob.AgeableMobGroupData {
        public final Type type;

        public GroupData(Type type) {
            super(0F);
            this.type = type;
        }
    }

    @Override
    public float getVoicePitch() {
        if(sex == Sex.MALE) return super.getVoicePitch() * 0.96F;
        else return super.getVoicePitch() * 1.04F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getActionTracker().getActionID().equals(getCollapseAction())) return null;
        else return SoundsNF.MERBOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.MERBOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.MERBOR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        playSound(SoundsNF.MERBOR_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public Diet getDiet() {
        return Diet.OMNIVORE;
    }

    @Override
    public SoundEvent getEatSound() {
        return null;
    }

    @Override
    protected int getMaxSatiety() {
        return (int) ContinentalWorldType.DAY_LENGTH;
    }

    @Override
    public boolean canBreedWith(TamableAnimalEntity other) {
        return false;
    }

    @Override
    protected void breedWith(TamableAnimalEntity other) {

    }

    @Override
    protected boolean checkComfort() {
        return false;
    }

    @Override
    public boolean isFeedItem(ItemStack item) {
        return false;
    }

    @Override
    public ResourceLocation getBreedAction() {
        return ActionsNF.MERBOR_BREED.getId();
    }

    @Override
    public boolean includeAABB() {
        return true;
    }

    @Override
    public float getModelScale() {
        return sex == Sex.MALE ? 17F/16F : 1F;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 10.5F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, 0F/16F, -7F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 13.5F, 0)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0F, -7F)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)));
        return map;
    }

    @Override
    public EntityPart[] getOrderedOBBParts() {
        return OBB_PARTS;
    }

    @Override
    public OBB[][] getDefaultOBBs() {
        return new OBB[][] {
                new OBB[] {
                        new OBB(8.5F/16F, 7.5F/16F, 5F/16F, 0, 0F/16F, 2.5F/16F),
                        new OBB(4.5F/16F, 5.5F/16F, 5.5F/16F, 0, -1F/16F, 5/16F + 2.5F/16F)
                }
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.7, bb.minY - 0.2, bb.minZ - 0.7, bb.maxX + 0.7, bb.maxY + 0.6, bb.maxZ + 0.7);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
