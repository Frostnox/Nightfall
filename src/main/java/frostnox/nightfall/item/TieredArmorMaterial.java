package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Supplier;

public enum TieredArmorMaterial implements ITieredArmorMaterial {
    LEATHER(0, null, null, 8, Weight.LIGHT, false, new float[]{0.02F, 0.02F, 0F, 0F, 0.02F, 0.02F},
            10, SoundsNF.LIGHT_ARMOR_EQUIP, 0.1F, RenderUtil.COLOR_LEATHER),
    PADDED(0, null, null, 8, Weight.LIGHT, false, new float[]{0.05F, 0.0F, 0.0F, 0F, 0F, 0F},
            10, SoundsNF.LIGHT_ARMOR_EQUIP, 0.1F, RenderUtil.COLOR_LINEN),
    RAGGED(0, null, Style.UNDEAD, 8, Weight.LIGHT, false, new float[]{0F, 0F, 0F, 0F, 0F, 0F},
            10, SoundsNF.LIGHT_ARMOR_EQUIP, 0F, RenderUtil.COLOR_UNDEAD_CLOTH),
    RUSTED(Metal.METEORITE, ArmorType.PLATE, Style.UNDEAD, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_UNDEAD_CLOTH),
    COPPER_PLATE_SURVIVOR(Metal.COPPER, ArmorType.PLATE, Style.SURVIVOR, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    COPPER_SCALE_SURVIVOR(Metal.COPPER, ArmorType.SCALE, Style.SURVIVOR, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    COPPER_CHAINMAIL_SURVIVOR(Metal.COPPER, ArmorType.CHAINMAIL, Style.SURVIVOR, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    BRONZE_PLATE_SURVIVOR(Metal.BRONZE, ArmorType.PLATE, Style.SURVIVOR, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    BRONZE_SCALE_SURVIVOR(Metal.BRONZE, ArmorType.SCALE, Style.SURVIVOR, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    BRONZE_CHAINMAIL_SURVIVOR(Metal.BRONZE, ArmorType.CHAINMAIL, Style.SURVIVOR, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    IRON_PLATE_SURVIVOR(Metal.IRON, ArmorType.PLATE, Style.SURVIVOR, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    IRON_SCALE_SURVIVOR(Metal.IRON, ArmorType.SCALE, Style.SURVIVOR, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    IRON_CHAINMAIL_SURVIVOR(Metal.IRON, ArmorType.CHAINMAIL, Style.SURVIVOR, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    STEEL_PLATE_SURVIVOR(Metal.STEEL, ArmorType.PLATE, Style.SURVIVOR, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    STEEL_SCALE_SURVIVOR(Metal.STEEL, ArmorType.SCALE, Style.SURVIVOR, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    STEEL_CHAINMAIL_SURVIVOR(Metal.STEEL, ArmorType.CHAINMAIL, Style.SURVIVOR, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    METEORITE_PLATE_SURVIVOR(Metal.METEORITE, ArmorType.PLATE, Style.SURVIVOR, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    METEORITE_SCALE_SURVIVOR(Metal.METEORITE, ArmorType.SCALE, Style.SURVIVOR, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    METEORITE_CHAINMAIL_SURVIVOR(Metal.METEORITE, ArmorType.CHAINMAIL, Style.SURVIVOR, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    COPPER_PLATE_EXPLORER(Metal.COPPER, ArmorType.PLATE, Style.EXPLORER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    COPPER_SCALE_EXPLORER(Metal.COPPER, ArmorType.SCALE, Style.EXPLORER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    COPPER_CHAINMAIL_EXPLORER(Metal.COPPER, ArmorType.CHAINMAIL, Style.EXPLORER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    BRONZE_PLATE_EXPLORER(Metal.BRONZE, ArmorType.PLATE, Style.EXPLORER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    BRONZE_SCALE_EXPLORER(Metal.BRONZE, ArmorType.SCALE, Style.EXPLORER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    BRONZE_CHAINMAIL_EXPLORER(Metal.BRONZE, ArmorType.CHAINMAIL, Style.EXPLORER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    IRON_PLATE_EXPLORER(Metal.IRON, ArmorType.PLATE, Style.EXPLORER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    IRON_SCALE_EXPLORER(Metal.IRON, ArmorType.SCALE, Style.EXPLORER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    IRON_CHAINMAIL_EXPLORER(Metal.IRON, ArmorType.CHAINMAIL, Style.EXPLORER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    STEEL_PLATE_EXPLORER(Metal.STEEL, ArmorType.PLATE, Style.EXPLORER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    STEEL_SCALE_EXPLORER(Metal.STEEL, ArmorType.SCALE, Style.EXPLORER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    STEEL_CHAINMAIL_EXPLORER(Metal.STEEL, ArmorType.CHAINMAIL, Style.EXPLORER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    METEORITE_PLATE_EXPLORER(Metal.METEORITE, ArmorType.PLATE, Style.EXPLORER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    METEORITE_SCALE_EXPLORER(Metal.METEORITE, ArmorType.SCALE, Style.EXPLORER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    METEORITE_CHAINMAIL_EXPLORER(Metal.METEORITE, ArmorType.CHAINMAIL, Style.EXPLORER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    COPPER_PLATE_SLAYER(Metal.COPPER, ArmorType.PLATE, Style.SLAYER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    COPPER_SCALE_SLAYER(Metal.COPPER, ArmorType.SCALE, Style.SLAYER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    COPPER_CHAINMAIL_SLAYER(Metal.COPPER, ArmorType.CHAINMAIL, Style.SLAYER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    BRONZE_PLATE_SLAYER(Metal.BRONZE, ArmorType.PLATE, Style.SLAYER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    BRONZE_SCALE_SLAYER(Metal.BRONZE, ArmorType.SCALE, Style.SLAYER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    BRONZE_CHAINMAIL_SLAYER(Metal.BRONZE, ArmorType.CHAINMAIL, Style.SLAYER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    IRON_PLATE_SLAYER(Metal.IRON, ArmorType.PLATE, Style.SLAYER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    IRON_SCALE_SLAYER(Metal.IRON, ArmorType.SCALE, Style.SLAYER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    IRON_CHAINMAIL_SLAYER(Metal.IRON, ArmorType.CHAINMAIL, Style.SLAYER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    STEEL_PLATE_SLAYER(Metal.STEEL, ArmorType.PLATE, Style.SLAYER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    STEEL_SCALE_SLAYER(Metal.STEEL, ArmorType.SCALE, Style.SLAYER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    STEEL_CHAINMAIL_SLAYER(Metal.STEEL, ArmorType.CHAINMAIL, Style.SLAYER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN),
    METEORITE_PLATE_SLAYER(Metal.METEORITE, ArmorType.PLATE, Style.SLAYER, 11, Weight.HEAVY,
            SoundsNF.PLATE_ARMOR_EQUIP, 0.5F, RenderUtil.COLOR_LINEN),
    METEORITE_SCALE_SLAYER(Metal.METEORITE, ArmorType.SCALE, Style.SLAYER, 11, Weight.MEDIUM,
            SoundsNF.SCALE_ARMOR_EQUIP, 0.25F, RenderUtil.COLOR_LEATHER),
    METEORITE_CHAINMAIL_SLAYER(Metal.METEORITE, ArmorType.CHAINMAIL, Style.SLAYER, 11, Weight.MEDIUM,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN, 0.25F, RenderUtil.COLOR_LINEN);

    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};

    private final int tier;
    private @Nullable final ArmorType armorType;
    private @Nullable final IStyle style;
    private final Weight weight;
    private final float[] defense; //striking, slashing, piercing, fire, frost, electric
    private final String name, armorName;
    private final int maxDamageFactor;
    private final int enchantability;
    private final Supplier<SoundEvent> soundEvent;
    private final float knockbackResistance;
    private final int defaultColor;
    private final boolean isMetal;

    TieredArmorMaterial(int tier, @Nullable ArmorType armorType, @Nullable IStyle style, int maxDamageFactor, Weight weight, boolean isMetal, float[] defense, int enchantability, Supplier<SoundEvent> soundEvent, float knockbackResistance, int defaultColor) {
        this.tier = tier;
        this.armorType = armorType;
        this.style = style;
        this.weight = weight;
        this.name = toString().toLowerCase(Locale.ROOT);
        this.armorName = name;
        this.maxDamageFactor = maxDamageFactor;
        this.defense = defense;
        this.enchantability = enchantability;
        this.soundEvent = soundEvent;
        this.knockbackResistance = knockbackResistance;
        this.defaultColor = defaultColor;
        this.isMetal = isMetal;
    }

    TieredArmorMaterial(IMetal metal, ArmorType armorType, IStyle style, int maxDamageFactor, Weight weight, Supplier<SoundEvent> soundEvent, float knockbackResistance, int defaultColor) {
        this.tier = metal.getTier();
        this.armorType = armorType;
        this.style = style;
        this.weight = weight;
        this.name = toString().toLowerCase(Locale.ROOT);
        this.armorName = name.replace(metal.getName() + "_", "");
        this.maxDamageFactor = maxDamageFactor;
        this.defense = new float[6];
        for(int i = 0; i < 6; i++) {
            this.defense[i] = metal.getBaseDefenses().get(i) * armorType.defenseMul.get(i);
        }
        this.enchantability = 0; //TODO:?
        this.soundEvent = soundEvent;
        this.knockbackResistance = knockbackResistance;
        this.defaultColor = defaultColor;
        isMetal = true;
    }

    public static ITieredArmorMaterial fromString(String name) {
        for(var material : RegistriesNF.getTieredArmorMaterials()) {
            if(name.contains(material.value.getName())) return material.value;
        }
        return null;
    }

    @Override
    public int getDurability(EquipmentSlot slotIn) {
        int durability = MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.maxDamageFactor;
        if(style == Style.SURVIVOR) durability = Math.round(durability * 1.1F);
        return durability;
    }

    @Override
    public float getDefense(EquipmentSlot slot, DamageType[] types, boolean asAverage) {
        float total = 0;
        for(DamageType type : types) {
            if(type.isDefensible()) total += defense[type.ordinal()] * (asAverage ? ITieredArmorMaterial.getSlotModifier(slot) : 1);
        }
        return total / types.length;
    }

    @Override
    public float getFinalDamage(EquipmentSlot slot, DamageType[] types, int currentDurability, float damage, boolean asAverage) {
        return CombatUtil.applyArmorDamageReduction(damage, currentDurability, getDurability(slot), getDefense(slot, types, asAverage));
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Nullable
    @Override
    public ArmorType getArmorType() {
        return armorType;
    }

    @Nullable
    @Override
    public IStyle getStyle() {
        return style;
    }

    @Override
    public Weight getWeight() {
        return weight;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return soundEvent.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return name independent of material (ex. copper_plate_survivor is plate_survivor)
     */
    @Override
    public String getStyledArmorName() {
        return this.armorName;
    }

    @Override
    public float getKnockbackResistance(EquipmentSlot slot) {
        return this.knockbackResistance * ITieredArmorMaterial.getSlotModifier(slot);
    }

    @Override
    public int getDefaultColor() {
        return defaultColor;
    }

    @Override
    public boolean isMetal() {
        return isMetal;
    }
}
