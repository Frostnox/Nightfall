package frostnox.nightfall.item;

import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/**
 * Materials for weapons/tools. Uses the vanilla parameters (except for attackDamage) plus
 * weight and some scalars affecting damage and stamina use.
 */
public enum TieredItemMaterial implements ITieredItemMaterial {
    WOOD(0, 44, 30.0F, 1, 0.6F, Weight.LIGHT),
    FLINT(0, 52, 30.0F, 1, 0.6F, Weight.MEDIUM),
    COPPER(Metal.COPPER, 258, 50.0F, 1, 0.7F, Weight.LIGHT),
    BRONZE(Metal.BRONZE, 387, 60.0F, 5, 0.8F, Weight.LIGHT),
    IRON(Metal.IRON, 514, 80.0F, 1, 1F, Weight.MEDIUM),
    STEEL(Metal.STEEL, 770, 90.0F, 1, 1.1F, Weight.MEDIUM),
    METEORITE(Metal.METEORITE, 258, 50F, 20, 0.7F, Weight.LIGHT);

    private final int tier;
    private final int maxUses;
    private final float efficiency;
    private final int enchantability;
    private final float damageMod;
    private final Weight weight;
    private final Metal metal;

    TieredItemMaterial(Metal metal, int tier, int maxUsesIn, float efficiencyIn, int enchantabilityIn, float damageMod, Weight weight) {
        this.tier = tier;
        this.maxUses = maxUsesIn;
        this.efficiency = efficiencyIn;
        this.enchantability = enchantabilityIn;
        this.damageMod = damageMod;
        this.weight = weight;
        this.metal = metal;
    }

    TieredItemMaterial(int tier, int maxUsesIn, float efficiencyIn, int enchantabilityIn, float damageMod, Weight weight) {
        this(null, tier, maxUsesIn, efficiencyIn, enchantabilityIn, damageMod, weight);
    }

    TieredItemMaterial(Metal metal, int maxUsesIn, float efficiencyIn, int enchantabilityIn, float damageMod, Weight weight) {
        this(metal, metal.getTier(), maxUsesIn, efficiencyIn, enchantabilityIn, damageMod, weight);
    }

    @Override
    public int getUses() {
        return this.maxUses;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public int getDurability() {
        return this.maxUses;
    }

    @Override
    public float getSpeed() {
        return this.efficiency;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0;
    }

    @Override
    public int getLevel() {
        return this.tier;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public float getDamageMultiplier() {
        return damageMod;
    }

    @Override
    public Weight getWeight() {
        return weight;
    }

    @Nullable
    @Override
    public IMetal getMetal() {
        return metal;
    }
}
