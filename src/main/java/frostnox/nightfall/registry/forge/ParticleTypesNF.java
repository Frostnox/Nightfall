package frostnox.nightfall.registry.forge;

import com.mojang.serialization.Codec;
import frostnox.nightfall.Nightfall;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ParticleTypesNF {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Nightfall.MODID);
    public static final RegistryObject<ParticleType<BlockParticleOption>> LEAF_BIRCH = PARTICLES.register("leaf_birch", () -> blockParticle());
    public static final RegistryObject<ParticleType<BlockParticleOption>> LEAF_IRONWOOD = PARTICLES.register("leaf_ironwood", () -> blockParticle());
    public static final RegistryObject<ParticleType<BlockParticleOption>> LEAF_JUNGLE = PARTICLES.register("leaf_jungle", () -> blockParticle());
    public static final RegistryObject<ParticleType<BlockParticleOption>> LEAF_OAK = PARTICLES.register("leaf_oak", () -> blockParticle());
    public static final RegistryObject<SimpleParticleType> FLAME_RED = PARTICLES.register("flame_red", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAME_ORANGE = PARTICLES.register("flame_orange", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAME_YELLOW = PARTICLES.register("flame_yellow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAME_WHITE = PARTICLES.register("flame_white", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAME_BLUE = PARTICLES.register("flame_blue", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPARK_RED = PARTICLES.register("spark_red", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPARK_ORANGE = PARTICLES.register("spark_orange", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPARK_YELLOW = PARTICLES.register("spark_yellow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPARK_WHITE = PARTICLES.register("spark_white", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPARK_BLUE = PARTICLES.register("spark_blue", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ESSENCE = PARTICLES.register("essence", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ESSENCE_MOON = PARTICLES.register("essence_moon", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLOOD_RED = PARTICLES.register("blood_red", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLOOD_DARK_RED = PARTICLES.register("blood_dark_red", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLOOD_PALE_BLUE = PARTICLES.register("blood_pale_blue", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FRAGMENT_BONE = PARTICLES.register("fragment_bone", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FRAGMENT_CREEPER = PARTICLES.register("fragment_creeper", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> COCKATRICE_SPIT = PARTICLES.register("cockatrice_spit", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> DRIPPING_WATER = PARTICLES.register("dripping_water", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FALLING_WATER = PARTICLES.register("falling_water", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> DRIPPING_LAVA = PARTICLES.register("dripping_lava", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FALLING_LAVA = PARTICLES.register("falling_lava", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LANDING_LAVA = PARTICLES.register("landing_lava", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> POISON = PARTICLES.register("poison", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> RAIN = PARTICLES.register("rain", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAIN_SPLASH = PARTICLES.register("rain_splash", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SNOW = PARTICLES.register("snow", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FADING_CLOUD = PARTICLES.register("fading_cloud", () -> new SimpleParticleType(true));

    public static void register() {
        PARTICLES.register(Nightfall.MOD_EVENT_BUS);
    }

    public static ParticleType<BlockParticleOption> blockParticle() {
        return particle(BlockParticleOption.DESERIALIZER, BlockParticleOption::codec);
    }

    public static <T extends ParticleOptions> ParticleType<T> particle(ParticleOptions.Deserializer<T> deserializer, final Function<ParticleType<T>, Codec<T>> codecFunc) {
        return new ParticleType<>(false, deserializer) {
            public Codec<T> codec() {
                return codecFunc.apply(this);
            }
        };
    }
}
