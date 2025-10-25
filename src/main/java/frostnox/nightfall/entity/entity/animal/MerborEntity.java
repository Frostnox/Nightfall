package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class MerborEntity extends TamableAnimalEntity {
    public enum Type {
        BOG, BRINE, RIVER
    }
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

    public Type getMerborType() {
        return getEntityData().get(TYPE);
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
    public Diet getDiet() {
        return Diet.OMNIVORE;
    }

    @Override
    public SoundEvent getEatSound() {
        return null;
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
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
        return null;
    }
}
