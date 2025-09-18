package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class DrakefowlEntity extends TamableAnimalEntity {
    public enum Type {
        BRONZE, EMERALD
    }
    protected static final EntityDataAccessor<DrakefowlEntity.Type> TYPE = SynchedEntityData.defineId(DrakefowlEntity.class, DataSerializersNF.DRAKEFOWL_TYPE);

    public DrakefowlEntity(EntityType<? extends TamableAnimalEntity> type, Level level, Sex sex) {
        super(type, level, sex);
    }

    public static DrakefowlEntity createFemale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.FEMALE);
    }

    public static DrakefowlEntity createMale(EntityType<? extends TamableAnimalEntity> type, Level level) {
        return new DrakefowlEntity(type, level, Sex.MALE);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 0)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 15)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.2)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.1)
                .add(AttributesNF.FIRE_DEFENSE.get(), 0.3);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -7.5F/16F, 5F/16F)));
        map.put(EntityPart.NECK, new AnimationData(new Vector3f(0F/16F, -10F/16F, 0F/16F), new Vector3f(35, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(-35, 0, 0)));
        return map;
    }

    public DrakefowlEntity.Type getDrakefowlType() {
        return getEntityData().get(TYPE);
    }

    @Override
    protected boolean isTameItem(ItemStack item) {
        return item.is(TagsNF.DRAKEFOWL_FOOD_ITEM);
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
    public @Nullable ResourceLocation getCollapseAction() {
        return ActionsNF.DRAKEFOWL_COLLAPSE.getId();
    }

    @Override
    public boolean canEat(BlockState state) {
        return false;
    }

    @Override
    public boolean canEat(Entity entity) {
        return false;
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
}
