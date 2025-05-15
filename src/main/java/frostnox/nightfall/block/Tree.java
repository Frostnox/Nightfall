package frostnox.nightfall.block;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.world.generation.TreePool;
import frostnox.nightfall.world.generation.tree.*;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Random;

public enum Tree implements ITree {
    ACACIA(1.6F, 0.7F, 0.2F, 0.5F, 1F, 0F, 0.35F, MaterialColor.TERRACOTTA_LIGHT_GRAY,
            MaterialColor.COLOR_ORANGE, new TreeGenerator(6, 5, 3, 3, 2),
            240, true, "patterned", null),
    BIRCH(1.2F, 0.55F, 0.6F, 0.4F, 0.75F, 0.4F, 0.85F, MaterialColor.QUARTZ,
            MaterialColor.SAND, new BirchTreeGenerator(5, 7, 1, 0, 2),
            200, true, "simple", ParticleTypesNF.LEAF_BIRCH),
    CAEDTAR(1.8F, 0.6F, 0.85F, 0.1F, 1F, 0.7F, 1F, MaterialColor.TERRACOTTA_BLACK,
            MaterialColor.TERRACOTTA_PINK, new TreeGenerator(6, 5, 3, 3, 2),
            500, true, "ornate", ParticleTypesNF.LEAF_CAEDTAR),
    IRONWOOD(2.4F, 0.5F, 0.4F, 0.1F, 1F, 0.1F, 1F, MaterialColor.TERRACOTTA_GRAY,
            MaterialColor.TERRACOTTA_BROWN, new IronwoodTreeGenerator(8, 9, 2, 3, 1),
            600, false, "ornate", ParticleTypesNF.LEAF_IRONWOOD),
    JUNGLE(1F, 0.6F, 0.75F, 0.45F, 0.65F, 0.6F, 1F, MaterialColor.TERRACOTTA_BROWN,
            MaterialColor.DIRT, new JungleTreeGenerator(7, 10, 3, 4, 1, 0.7, false),
            240, true, "rustic", ParticleTypesNF.LEAF_JUNGLE),
    LARCH(0.8F, 0.25F, 0.45F, 0F, 0.4F, 0.1F, 0.8F, MaterialColor.TERRACOTTA_GRAY,
            MaterialColor.PODZOL, new LarchTreeGenerator(7, 6, 2),
            400, true, "medieval", null),
    MAPLE(1.4F, 0.5F, 0.6F, 0.3F, 0.7F, 0.2F, 1F, MaterialColor.TERRACOTTA_LIGHT_GRAY,
            MaterialColor.DIRT, new TreeGenerator(6, 5, 3, 3, 2),
            280, true, "simple", ParticleTypesNF.LEAF_MAPLE),
    OAK(1.6F, 0.5F, 0.55F, 0.35F, 1F, 0.3F, 1F, MaterialColor.TERRACOTTA_BROWN,
            MaterialColor.WOOD, new OakTreeGenerator(6, 5, 3, 3, 2),
            340, true, "simple", ParticleTypesNF.LEAF_OAK),
    PALM(1.8F, 0.65F, 0.8F, 0.45F, 0.75F, 0.7F, 1F, MaterialColor.TERRACOTTA_LIGHT_GRAY,
            MaterialColor.SAND, new PalmTreeGenerator(7, 8, 0.6, true),
            260, false, "patterned", null),
    REDWOOD(0.6F, 0.2F, 0.75F, 0F, 0.35F, 0.5F, 1F, MaterialColor.TERRACOTTA_ORANGE,
            MaterialColor.CRIMSON_HYPHAE, new TreeGenerator(6, 5, 3, 3, 2),
            700, false, "medieval", null),
    SPRUCE(0.8F, 0.35F, 0.5F, 0F, 0.6F, 0.4F, 1F, MaterialColor.COLOR_BROWN,
            MaterialColor.QUARTZ, new SpruceTreeGenerator(6, 6, 2),
            360, false, "medieval", null),
    WILLOW(0.6F, 0.8F, 0.8F, 0.45F, 1F, 0.65F, 1F, MaterialColor.TERRACOTTA_BROWN,
            MaterialColor.TERRACOTTA_LIGHT_GREEN, new TreeGenerator(6, 5, 3, 3, 2),
            240, true, "rustic", ParticleTypesNF.LEAF_WILLOW);

    public static final Tree[] PRIMARY_TREES = new Tree[] {BIRCH, JUNGLE, LARCH};
    public static final Tree[] SECONDARY_TREES = new Tree[] {MAPLE, SPRUCE, WILLOW};
    public static final Tree[] TERTIARY_TREES = new Tree[] {ACACIA, CAEDTAR, OAK};
    public static final Tree[] QUATERNARY_TREES = new Tree[] {IRONWOOD, PALM, REDWOOD};

    public final float idealTemp, idealHumidity;
    private final float strength, explosionResistance, minTemp, maxTemp, minHumidity, maxHumidity;
    private final MaterialColor barkColor, woodColor;
    private final TreeGenerator treeGenerator;
    private final int growthInterval;
    private final boolean deciduous;
    private final String style;
    private final TagKey<Block> tag;
    private final @Nullable RegistryObject<ParticleType<BlockParticleOption>> particle;

    Tree(float hardness, float idealTemp, float idealHumidity, float minTemp, float maxTemp, float minHumidity, float maxHumidity, MaterialColor barkColor, MaterialColor woodColor, TreeGenerator treeGenerator, int growthIntervalSeconds, boolean deciduous, String style, @Nullable RegistryObject<ParticleType<BlockParticleOption>> particle) {
        this(BASE_STRENGTH * hardness, 3F * hardness, idealTemp, idealHumidity, minTemp, maxTemp, minHumidity, maxHumidity, barkColor, woodColor, treeGenerator, growthIntervalSeconds, deciduous, style, particle);
    }

    Tree(float strength, float explosionResistance, float idealTemp, float idealHumidity, float minTemp, float maxTemp, float minHumidity, float maxHumidity, MaterialColor barkColor, MaterialColor woodColor, TreeGenerator treeGenerator, int growthIntervalSeconds, boolean deciduous, String style, @Nullable RegistryObject<ParticleType<BlockParticleOption>> particle) {
        this.strength = strength;
        this.explosionResistance = explosionResistance;
        this.idealTemp = idealTemp;
        this.idealHumidity = idealHumidity;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.barkColor = barkColor;
        this.woodColor = woodColor;
        this.treeGenerator = treeGenerator;
        this.growthInterval = growthIntervalSeconds * 20; //Convert to ticks
        this.deciduous = deciduous;
        this.style = style;
        this.tag = TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "tree/" + getName()));
        this.particle = particle;
    }

    public static @Nullable Tree pickRandomTree(TreePool pool, Random random) {
        if(pool.size() == 0) return null;
        int rand = random.nextInt(pool.totalWeight());
        int cumulativeWeight = 0;
        for(int i = 0; i < pool.size(); i++) {
            TreePool.Entry entry = pool.trees()[i];
            cumulativeWeight += entry.weight();
            if(rand < cumulativeWeight) return entry.tree();
        }
        return null;
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
    public MaterialColor getBarkColor() {
        return barkColor;
    }

    @Override
    public MaterialColor getBaseColor() {
        return woodColor;
    }

    @Override
    public SoundType getSound() {
        return SoundType.WOOD;
    }

    @Override
    public TreeGenerator getGenerator() {
        return treeGenerator;
    }

    @Override
    public int getGrowthIntervalTicks() {
        return growthInterval;
    }

    @Override
    public boolean isDeciduous() {
        return deciduous;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public float getHardness() {
        return strength / BASE_STRENGTH;
    }

    @Override
    public TagKey<Block> getTag() {
        return tag;
    }

    @Override
    public @Nullable RegistryObject<ParticleType<BlockParticleOption>> getParticle() {
        return particle;
    }

    @Override
    public boolean canSurvive(float temperature, float humidity) {
        return temperature >= minTemp && temperature <= maxTemp && humidity >= minHumidity && humidity <= maxHumidity;
    }
}
