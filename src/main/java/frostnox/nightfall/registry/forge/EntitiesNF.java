package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.*;
import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.entity.entity.animal.*;
import frostnox.nightfall.entity.entity.monster.*;
import frostnox.nightfall.entity.entity.projectile.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntitiesNF {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Nightfall.MODID);

    public static final RegistryObject<EntityType<RabbitEntity>> RABBIT = ENTITIES.register("rabbit", () -> EntityType.Builder.of(RabbitEntity::new, MobCategory.CREATURE)
            .sized(8F/16F, 6F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("rabbit"));
    public static final RegistryObject<EntityType<DeerEntity>> DEER = ENTITIES.register("deer", () -> EntityType.Builder.of(DeerEntity::new, MobCategory.CREATURE)
            .sized(13F/16F, 15.5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("deer"));

    public static final RegistryObject<EntityType<DrakefowlEntity>> DRAKEFOWL_ROOSTER = ENTITIES.register("drakefowl_rooster", () -> EntityType.Builder.of(DrakefowlEntity::createMale, MobCategory.CREATURE)
            .sized(6F/16F, 9F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("drakefowl_rooster"));
    public static final RegistryObject<EntityType<DrakefowlEntity>> DRAKEFOWL_HEN = ENTITIES.register("drakefowl_hen", () -> EntityType.Builder.of(DrakefowlEntity::createFemale, MobCategory.CREATURE)
            .sized(6F/16F, 9F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("drakefowl_hen"));
    public static final RegistryObject<EntityType<DrakefowlBabyEntity>> DRAKEFOWL_CHICK = ENTITIES.register("drakefowl_chick", () -> EntityType.Builder.of(DrakefowlBabyEntity::new, MobCategory.CREATURE)
            .sized(4F/16F, 5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("drakefowl_chick"));
    public static final RegistryObject<EntityType<MerborEntity>> MERBOR_TUSKER = ENTITIES.register("merbor_tusker", () -> EntityType.Builder.of(MerborEntity::createMale, MobCategory.CREATURE)
            .sized(15.5F/16F, 15.5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("merbor_tusker"));
    public static final RegistryObject<EntityType<MerborEntity>> MERBOR_SOW = ENTITIES.register("merbor_sow", () -> EntityType.Builder.of(MerborEntity::createFemale, MobCategory.CREATURE)
            .sized(15F/16F, 15F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("merbor_sow"));
    public static final RegistryObject<EntityType<MerborBabyEntity>> MERBOR_PIGLET = ENTITIES.register("merbor_piglet", () -> EntityType.Builder.of(MerborBabyEntity::new, MobCategory.CREATURE)
            .sized(7F/16F, 6F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("merbor_piglet"));

    public static final RegistryObject<EntityType<HuskEntity>> HUSK = ENTITIES.register("husk", () -> EntityType.Builder.of(HuskEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.9F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("husk"));
    public static final RegistryObject<EntityType<SkeletonEntity>> SKELETON = ENTITIES.register("skeleton", () -> EntityType.Builder.of(SkeletonEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.9F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("skeleton"));
    public static final RegistryObject<EntityType<DregEntity>> DREG = ENTITIES.register("dreg", () -> EntityType.Builder.of(DregEntity::new, MobCategory.MONSTER)
            .sized(0.57F, 1.85F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("dreg"));

    public static final RegistryObject<EntityType<CreeperEntity>> CREEPER = ENTITIES.register("creeper", () -> EntityType.Builder.of(CreeperEntity::new, MobCategory.MONSTER)
            .sized(0.57F, 1.65F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("creeper"));
    public static final RegistryObject<EntityType<CockatriceEntity>> COCKATRICE = ENTITIES.register("cockatrice", () -> EntityType.Builder.of(CockatriceEntity::new, MobCategory.MONSTER)
            .sized(11F/16F, 15.95F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("cockatrice"));

    public static final RegistryObject<EntityType<SpiderEntity>> SPIDER = ENTITIES.register("spider", () -> EntityType.Builder.of(SpiderEntity::new, MobCategory.MONSTER)
            .sized(10F/16F, 6.5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("spider"));
    public static final RegistryObject<EntityType<RockwormEntity>> ROCKWORM = ENTITIES.register("rockworm", () -> EntityType.Builder.of(RockwormEntity::new, MobCategory.MONSTER)
            .sized(7F/16F, 29F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("rockworm"));
    public static final RegistryObject<EntityType<PitDevilEntity>> PIT_DEVIL = ENTITIES.register("pit_devil", () -> EntityType.Builder.of(PitDevilEntity::new, MobCategory.MONSTER)
            .sized(12F/16F, 13F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("pit_devil"));
    public static final RegistryObject<EntityType<EctoplasmEntity>> ECTOPLASM_LARGE = ENTITIES.register("large_ectoplasm", () -> EntityType.Builder.of(EctoplasmEntity::createLarge, MobCategory.MONSTER)
            .sized(30F/16F, 30F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("large_ectoplasm"));
    public static final RegistryObject<EntityType<EctoplasmEntity>> ECTOPLASM_MEDIUM = ENTITIES.register("ectoplasm", () -> EntityType.Builder.of(EctoplasmEntity::createMedium, MobCategory.MONSTER)
            .sized(20F/16F, 20F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("ectoplasm"));
    public static final RegistryObject<EntityType<EctoplasmEntity>> ECTOPLASM_SMALL = ENTITIES.register("small_ectoplasm", () -> EntityType.Builder.of(EctoplasmEntity::createSmall, MobCategory.MONSTER)
            .sized(10F/16F, 10F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("small_ectoplasm"));
    public static final RegistryObject<EntityType<ScorpionEntity>> SCORPION = ENTITIES.register("scorpion", () -> EntityType.Builder.of(ScorpionEntity::new, MobCategory.MONSTER)
            .sized(6F/16F, 10F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("scorpion"));
    public static final RegistryObject<EntityType<SkaraSwarmEntity>> SKARA_SWARM = ENTITIES.register("skara_swarm", () -> EntityType.Builder.of(SkaraSwarmEntity::new, MobCategory.MONSTER)
            .sized(15F/16F, 1F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("skara_swarm"));
    public static final RegistryObject<EntityType<TrollEntity>> TROLL = ENTITIES.register("troll", () -> EntityType.Builder.of(TrollEntity::new, MobCategory.MONSTER)
            .sized(6F/16F, 10F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("troll"));
    public static final RegistryObject<EntityType<OlmurEntity>> OLMUR = ENTITIES.register("olmur", () -> EntityType.Builder.of(OlmurEntity::new, MobCategory.MONSTER)
            .sized(6F/16F, 10F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("olmur"));

    public static final RegistryObject<EntityType<JellyfishEntity>> JELLYFISH = ENTITIES.register("jellyfish", () -> EntityType.Builder.of(JellyfishEntity::new, MobCategory.WATER_AMBIENT)
            .sized(6.1F/16F, 14.1F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("jellyfish"));

    public static final RegistryObject<EntityType<ArmorStandDummyEntity>> ARMOR_STAND = ENTITIES.register("armor_stand", () -> EntityType.Builder.of(ArmorStandDummyEntity::new, MobCategory.MISC)
            .sized(0.5F, 1.975F).clientTrackingRange(10).updateInterval(4).build("armor_stand"));
    public static final RegistryObject<EntityType<BoatEntity>> BOAT = ENTITIES.register("boat", () -> EntityType.Builder.<BoatEntity>of(BoatEntity::new, MobCategory.MISC)
            .sized(1.375F, 0.5625F).clientTrackingRange(10).build("boat"));
    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITIES.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
            .sized(0, 0).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("seat"));
    public static final RegistryObject<EntityType<RopeKnotEntity>> ROPE_KNOT = ENTITIES.register("rope_knot", () -> EntityType.Builder.<RopeKnotEntity>of(RopeKnotEntity::new, MobCategory.MISC)
            .noSave().sized(0.375F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("rope_knot"));

    public static final RegistryObject<EntityType<MovingBlockEntity>> MOVING_BLOCK = ENTITIES.register("moving_block", () -> EntityType.Builder.of(MovingBlockEntity::new, MobCategory.MISC)
            .sized(0.99F, 0.99F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("moving_block"));
    public static final RegistryObject<EntityType<ThrownRockEntity>> THROWN_ROCK = ENTITIES.register("thrown_rock", () -> EntityType.Builder.<ThrownRockEntity>of(ThrownRockEntity::new, MobCategory.MISC)
            .sized(0.275F, 0.275F).clientTrackingRange(4).updateInterval(20).build("thrown_rock"));
    public static final RegistryObject<EntityType<ArrowEntity>> ARROW = ENTITIES.register("arrow", () -> EntityType.Builder.<ArrowEntity>of(ArrowEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build("arrow"));
    public static final RegistryObject<EntityType<ThrownWeaponEntity>> THROWN_WEAPON = ENTITIES.register("thrown_weapon", () -> EntityType.Builder.<ThrownWeaponEntity>of(ThrownWeaponEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("thrown_weapon"));
    public static final RegistryObject<EntityType<PoisonSpitEntity>> POISON_SPIT = ENTITIES.register("poison_spit", () -> EntityType.Builder.<PoisonSpitEntity>of(PoisonSpitEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build("poison_spit"));
    public static final RegistryObject<EntityType<FireSpitEntity>> FIRE_SPIT = ENTITIES.register("fire_spit", () -> EntityType.Builder.<FireSpitEntity>of(FireSpitEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build("fire_spit"));

    public static void register() {
        ENTITIES.register(Nightfall.MOD_EVENT_BUS);
    }
}
