package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;

public class DrakefowlBabyEntity extends BabyAnimalEntity implements IOrientedHitBoxes {
    private static final EntityPart[] OBB_PARTS = new EntityPart[]{EntityPart.BODY, EntityPart.NECK, EntityPart.HEAD};
    protected static final EntityDataAccessor<DrakefowlEntity.Type> TYPE = SynchedEntityData.defineId(DrakefowlEntity.class, DataSerializersNF.DRAKEFOWL_TYPE);

    public DrakefowlBabyEntity(EntityType<? extends AnimalEntity> type, Level level) {
        super(type, level, (int) ContinentalWorldType.DAY_LENGTH * 4);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.3);
    }

    public DrakefowlEntity.Type getDrakefowlType() {
        return getEntityData().get(TYPE);
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 30;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height - 0.01F;
    }

    @Override
    public float getVisionAngle() {
        return 160F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, DrakefowlEntity.Type.EMERALD);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", getDrakefowlType().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        getEntityData().set(TYPE, DrakefowlEntity.Type.values()[tag.getInt("type")]);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(getActionTracker().getActionID().equals(getCollapseAction())) return null;
        else return SoundsNF.DRAKEFOWL_BABY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.DRAKEFOWL_BABY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.DRAKEFOWL_BABY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {

    }

    @Override
    public boolean canEat(BlockState state) {
        if(state.is(TagsNF.DRAKEFOWL_FOOD_BLOCK)) {
            if(state.getBlock() instanceof IFoodBlock foodBlock) return foodBlock.isEatable(state);
            else return true;
        }
        else return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) return itemEntity.getItem().is(TagsNF.DRAKEFOWL_FOOD_ITEM);
        else return false;
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
    public boolean includeAABB() {
        return true;
    }

    @Override
    public Vector3f getOBBTranslation() {
        return new Vector3f(0, 3.5F/16F, 0);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDefaultAnimMap() {
        EnumMap<EntityPart, AnimationData> map = getGenericAnimMap();
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -1F/16F, -1.5F/16F)));
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
                new OBB(2.25F/16F, 2.25F/16F, 2.25F/16F, 0, 0.5F/16F, 0.5F/16F)
        };
    }

    @Override
    public AABB getEnclosingAABB() {
        AABB bb = getBoundingBox();
        return new AABB(bb.minX - 0.3, bb.minY, bb.minZ - 0.3, bb.maxX + 0.3, bb.maxY + 0.3, bb.maxZ + 0.3);
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return boxIndex >= 0 ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
    }
}
