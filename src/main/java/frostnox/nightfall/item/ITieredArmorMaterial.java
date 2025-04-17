package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * Make sure to register a wrapped entry during the associated registry event for full compatibility
 */
public interface ITieredArmorMaterial {
    int getDurability(EquipmentSlot slotIn);

    float getDefense(EquipmentSlot slot, DamageType[] types, boolean asAverage);

    float getAbsorption(EquipmentSlot slot, DamageType[] types, boolean asAverage);

    float getFinalDamage(EquipmentSlot slot, DamageType[] types, int currentDurability, float damage, boolean asAverage);

    int getTier();

    @Nullable ArmorType getArmorType();

    @Nullable IStyle getStyle();

    Weight getWeight();

    int getEnchantability();

    SoundEvent getSoundEvent();

    String getName();

    String getStyledArmorName();

    float getKnockbackResistance(EquipmentSlot slot);

    int getDefaultColor();

    boolean isMetal();

    /**
     * @return percentage weight of slot, sum of all slots should be 1
     */
    static float getSlotModifier(EquipmentSlot slot) {
        switch(slot) {
            case FEET:
                return 0.125F;
            case LEGS:
                return 0.3F;
            case CHEST:
                return 0.4F;
            case HEAD:
                return 0.175F;
        }
        return 0F;
    }

    class Entry extends ForgeRegistryEntry<ITieredArmorMaterial.Entry> {
        public final ITieredArmorMaterial value;

        public Entry(ITieredArmorMaterial value) {
            this.value = value;
        }
    }
}
