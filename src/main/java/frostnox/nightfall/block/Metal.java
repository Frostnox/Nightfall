package frostnox.nightfall.block;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.data.Vec2f;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.Map;

public enum Metal implements IMetal {
    TIN(0, 0, Category.NOBLE, 5F, 5F, MaterialColor.COLOR_LIGHT_GRAY, new Color(0xffadb6bc), SoundType.COPPER, Map.of()),
    COPPER(1, 1, Category.HARD, 10F, 10F, MaterialColor.COLOR_ORANGE, new Color(0xffcb623a), SoundType.COPPER, Map.of(),
            FloatList.of(0.05F, 0.05F, 0.05F, 0F, 0F, -1F)),
    BRONZE(2, 1, Category.HARD, 12F, 12F, MaterialColor.TERRACOTTA_ORANGE, new Color(0xffc07d3d), SoundsNF.BRONZE_TYPE,
            Map.of(COPPER, new Vec2f(0.85F, 1F), TIN, new Vec2f(0.05F, 0.15F)),
            FloatList.of(0.08F, 0.08F, 0.08F, 0.04F, 0.04F, 0.04F)),
    IRON(3, 3, Category.HARD, 14F, 14F, MaterialColor.COLOR_GRAY, new Color(0xff575554), SoundType.NETHERITE_BLOCK, Map.of(),
            FloatList.of(0.1F, 0.1F, 0.1F, 0.06F, 0.06F, 0.06F)),
    STEEL(4, 3, Category.HARD, 18F, 18F, MaterialColor.METAL, new Color(0xff777c85), SoundType.NETHERITE_BLOCK, Map.of(),
            FloatList.of(0.12F, 0.12F, 0.12F, 0.08F, 0.08F, 0.08F)),
    METEORITE(1, 2, Category.MYSTIC, 10F, 10F, MaterialColor.COLOR_PURPLE, new Color(0xff78519d), SoundsNF.METEORITE_TYPE, Map.of(),
            FloatList.of(0.04F, 0.04F, 0.04F, 0.06F, 0.06F, 0.06F));

    private final int tier, workTier;
    private final Category category;
    private final float strength, explosionResistance;
    private final MaterialColor materialColor;
    private final Color color;
    private final SoundType sound;
    private final Map<IMetal, Vec2f> bases;
    private final List<Float> defenses;
    private final TagKey<Item> itemTag;

    Metal(int tier, int workTier, Category category, float strength, float explosionResistance, MaterialColor materialColor, Color color, SoundType sound, Map<IMetal, Vec2f> bases) {
        this(tier, workTier, category, strength, explosionResistance, materialColor, color, sound, bases, FloatList.of(0, 0, 0, 0, 0, 0));
    }

    Metal(int tier, int workTier, Category category, float strength, float explosionResistance, MaterialColor materialColor, Color color, SoundType sound, Map<IMetal, Vec2f> bases, List<Float> defenses) {
        this.tier = tier;
        this.workTier = workTier;
        this.category = category;
        this.strength = strength;
        this.explosionResistance = explosionResistance;
        this.materialColor = materialColor;
        this.color = color;
        this.sound = sound;
        this.bases = bases;
        this.defenses = defenses;
        itemTag = TagKey.create(Registry.ITEM_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "metal/" + getName()));
    }

    /**
     * @param searchString string to search for a valid metal name within
     * @return first result found, null if none
     */
    public static @Nullable IMetal fromString(String searchString) {
        for(var metal : RegistriesNF.getMetals()) {
            if(searchString.contains(metal.value.getName())) return metal.value;
        }
        return null;
    }

    @Override
    public MaterialColor getBaseColor() {
        return materialColor;
    }

    @Override
    public SoundType getSound() {
        return sound;
    }

    @Override
    public float getStrength() {
        return strength;
    }

    @Override
    public float getExplosionResistance() {
        return explosionResistance;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public int getWorkTier() {
        return workTier;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public TagKey<Item> getTag() {
        return itemTag;
    }

    @Override
    public Map<IMetal, Vec2f> getBaseMetals() {
        return bases;
    }

    @Override
    public List<Float> getBaseDefenses() {
        return defenses;
    }
}
