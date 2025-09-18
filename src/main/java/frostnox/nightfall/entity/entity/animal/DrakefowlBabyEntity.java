package frostnox.nightfall.entity.entity.animal;

import com.mojang.math.Vector3d;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.DataSerializersNF;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DrakefowlBabyEntity extends BabyAnimalEntity {
    protected static final EntityDataAccessor<DrakefowlEntity.Type> TYPE = SynchedEntityData.defineId(DrakefowlEntity.class, DataSerializersNF.DRAKEFOWL_TYPE);

    public DrakefowlBabyEntity(EntityType<? extends AnimalEntity> type, Level level) {
        super(type, level, (int) ContinentalWorldType.DAY_LENGTH * 4);
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

    public DrakefowlEntity.Type getDrakefowlType() {
        return getEntityData().get(TYPE);
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
