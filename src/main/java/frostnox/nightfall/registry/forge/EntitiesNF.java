package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.entity.entity.BoatEntity;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.entity.entity.SeatEntity;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.*;
import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import frostnox.nightfall.entity.entity.projectile.CockatriceSpitEntity;
import frostnox.nightfall.entity.entity.projectile.ThrownRockEntity;
import frostnox.nightfall.entity.entity.projectile.ThrownWeaponEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntitiesNF {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Nightfall.MODID);
    public static final RegistryObject<EntityType<RabbitEntity>> RABBIT = ENTITIES.register("rabbit", () -> EntityType.Builder.of(RabbitEntity::new, MobCategory.CREATURE)
            .sized(8F/16F, 5.5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("rabbit"));
    public static final RegistryObject<EntityType<DeerEntity>> DEER = ENTITIES.register("deer", () -> EntityType.Builder.of(DeerEntity::new, MobCategory.CREATURE)
            .sized(13F/16F, 15.5F/16F).clientTrackingRange(10).setShouldReceiveVelocityUpdates(false).build("deer"));
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
    public static final RegistryObject<EntityType<ArmorStandDummyEntity>> ARMOR_STAND = ENTITIES.register("armor_stand", () -> EntityType.Builder.of(ArmorStandDummyEntity::new, MobCategory.MISC)
            .sized(0.5F, 1.975F).clientTrackingRange(10).updateInterval(4).build("armor_stand"));
    public static final RegistryObject<EntityType<BoatEntity>> BOAT = ENTITIES.register("boat", () -> EntityType.Builder.<BoatEntity>of(BoatEntity::new, MobCategory.MISC)
            .sized(1.375F, 0.5625F).clientTrackingRange(10).build("boat"));
    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITIES.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
            .sized(0, 0).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("seat"));
    public static final RegistryObject<EntityType<MovingBlockEntity>> MOVING_BLOCK = ENTITIES.register("moving_block", () -> EntityType.Builder.of(MovingBlockEntity::new, MobCategory.MISC)
            .sized(0.99F, 0.99F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("moving_block"));
    public static final RegistryObject<EntityType<ThrownRockEntity>> THROWN_ROCK = ENTITIES.register("thrown_rock", () -> EntityType.Builder.<ThrownRockEntity>of(ThrownRockEntity::new, MobCategory.MISC)
            .sized(0.275F, 0.275F).clientTrackingRange(4).updateInterval(20).build("thrown_rock"));
    public static final RegistryObject<EntityType<ArrowEntity>> ARROW = ENTITIES.register("arrow", () -> EntityType.Builder.<ArrowEntity>of(ArrowEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build("arrow"));
    public static final RegistryObject<EntityType<ThrownWeaponEntity>> THROWN_WEAPON = ENTITIES.register("thrown_weapon", () -> EntityType.Builder.<ThrownWeaponEntity>of(ThrownWeaponEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("thrown_weapon"));
    public static final RegistryObject<EntityType<CockatriceSpitEntity>> COCKATRICE_SPIT = ENTITIES.register("cockatrice_spit", () -> EntityType.Builder.<CockatriceSpitEntity>of(CockatriceSpitEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build("cockatrice_spit"));

    public static void register() {
        ENTITIES.register(Nightfall.MOD_EVENT_BUS);
    }
}
