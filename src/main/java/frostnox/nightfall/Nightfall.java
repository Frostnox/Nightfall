package frostnox.nightfall;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.block.*;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.OverlayNF;
import frostnox.nightfall.client.gui.screen.container.*;
import frostnox.nightfall.client.gui.screen.encyclopedia.EntryPuzzleScreen;
import frostnox.nightfall.client.model.AnimatedItemModel;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.particle.*;
import frostnox.nightfall.client.render.ContinentalEffects;
import frostnox.nightfall.client.render.RenderTypeNF;
import frostnox.nightfall.client.render.blockentity.*;
import frostnox.nightfall.client.render.entity.*;
import frostnox.nightfall.client.render.entity.SpiderRenderer;
import frostnox.nightfall.data.*;
import frostnox.nightfall.data.extensible.TransformTypeNF;
import frostnox.nightfall.data.recipe.*;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.PuzzleContainer;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.entity.entity.animal.*;
import frostnox.nightfall.entity.entity.monster.*;
import frostnox.nightfall.entity.entity.projectile.FireSpitEntity;
import frostnox.nightfall.item.IArmament;
import frostnox.nightfall.item.IDye;
import frostnox.nightfall.item.IStyle;
import frostnox.nightfall.item.ITieredArmorMaterial;
import frostnox.nightfall.item.item.ProjectileLauncherItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.command.*;
import frostnox.nightfall.network.command.argument.EntryArgument;
import frostnox.nightfall.network.command.argument.KnowledgeArgument;
import frostnox.nightfall.registry.*;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import frostnox.nightfall.registry.vanilla.LootItemConditionTypesNF;
import frostnox.nightfall.registry.vanilla.LootItemFunctionTypesNF;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.FlatWorldType;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.biome.ContinentalBiomeSource;
import frostnox.nightfall.world.condition.WorldCondition;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.generation.FlatChunkGenerator;
import frostnox.nightfall.world.inventory.PlayerInventoryContainer;
import frostnox.nightfall.world.inventory.StorageContainer;
import frostnox.nightfall.world.spawngroup.SpawnGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
// The value here should match an entry in the META-INF/mods.toml file
@Mod("nightfall")
public class Nightfall {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "nightfall";
    public static boolean isRubidiumLoaded;
    public static IEventBus MOD_EVENT_BUS;

    public Nightfall() {
        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        isRubidiumLoaded = ModList.get().isLoaded("rubidium");
        ActionsNF.register();
        AttributesNF.register();
        BiomesNF.register();
        BlockEntitiesNF.register();
        BlocksNF.register();
        ContainersNF.register();
        DataSerializersNF.register();
        EffectsNF.register();
        EntriesNF.register();
        KnowledgeNF.register();
        EntitiesNF.register();
        FluidsNF.register();
        ItemsNF.register();
        ParticleTypesNF.register();
        SoundsNF.register();
        SpawnGroupsNF.register();
        StructuresNF.register();
        WorldConditionsNF.register();
    }

    @SubscribeEvent
    public static void onNewRegistryEvent(NewRegistryEvent event) {
        event.create(new RegistryBuilder<Action>().setType(Action.class).setName(RegistriesNF.ACTIONS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving().hasTags());
        event.create(new RegistryBuilder<Entry>().setType(Entry.class).setName(RegistriesNF.ENTRIES_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<Knowledge>().setType(Knowledge.class).setName(RegistriesNF.KNOWLEDGE_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<WorldCondition>().setType(WorldCondition.class).setName(RegistriesNF.WORLD_CONDITIONS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<SpawnGroup>().setType(SpawnGroup.class).setName(RegistriesNF.SPAWN_GROUPS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving().hasTags());
        event.create(new RegistryBuilder<IArmament.Entry>().setType(IArmament.Entry.class).setName(RegistriesNF.ARMAMENTS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<IDye.Entry>().setType(IDye.Entry.class).setName(RegistriesNF.DYES_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<IMetal.Entry>().setType(IMetal.Entry.class).setName(RegistriesNF.METALS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<ISoil.Entry>().setType(ISoil.Entry.class).setName(RegistriesNF.SOILS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<IStone.Entry>().setType(IStone.Entry.class).setName(RegistriesNF.STONES_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<IStyle.Entry>().setType(IStyle.Entry.class).setName(RegistriesNF.STYLES_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<ITieredArmorMaterial.Entry>().setType(ITieredArmorMaterial.Entry.class).setName(RegistriesNF.TIERED_ARMOR_MATERIALS_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
        event.create(new RegistryBuilder<ITree.Entry>().setType(ITree.Entry.class).setName(RegistriesNF.TREES_KEY.location()).setMaxID(Integer.MAX_VALUE - 1).disableSync().disableSaving());
    }

    @SubscribeEvent
    public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
        NetworkHandler.register();
        event.enqueueWork(() -> {
            ArgumentTypes.register(MODID + ":entry", EntryArgument.class, new EmptyArgumentSerializer<>(EntryArgument::id));
            ArgumentTypes.register(MODID + ":knowledge", KnowledgeArgument.class, new EmptyArgumentSerializer<>(KnowledgeArgument::id));
            Registry.register(Registry.BIOME_SOURCE, ResourceLocation.fromNamespaceAndPath(MODID, "continental"), ContinentalBiomeSource.CODEC);
            Registry.register(Registry.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath(MODID, "continental"), ContinentalChunkGenerator.CODEC);
            Registry.register(Registry.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath(MODID, "flat"), FlatChunkGenerator.CODEC);
            ActionsNF.init();
            FluidsNF.init();
            PlayerActionSet.init();
        });
    }

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientEngine.get().dataExcludedInit();
            //These have to be in here since they access fields of ClientEngine
            ClientRegistry.registerKeyBinding(ClientEngine.get().keyDash);
            ClientRegistry.registerKeyBinding(ClientEngine.get().keyOffhand);
            ClientRegistry.registerKeyBinding(ClientEngine.get().keyEncyclopedia);
            ClientRegistry.registerKeyBinding(ClientEngine.get().keyModify);

            WorldPreset.PRESETS.clear(); //Remove vanilla world types from client selection
            DimensionSpecialEffects.EFFECTS.put(ContinentalWorldType.LOCATION, new ContinentalEffects(ContinentalChunkGenerator.SEA_LEVEL + 256, true, DimensionSpecialEffects.SkyType.NORMAL, false, false));

            MenuScreens.register(ContainersNF.BARREL.get(), BarrelScreen::new);
            MenuScreens.register(ContainersNF.CAULDRON.get(), CauldronScreen::new);
            final ResourceLocation pot = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/screen/pot.png");
            MenuScreens.register(ContainersNF.POT.get(), (StorageContainer menu, Inventory inv, Component title) -> new SimpleContainerScreen<StorageContainer>(menu, inv, title) {
                @Override
                public ResourceLocation getTexture() {
                    return pot;
                }

                @Override
                protected ResourceLocation getInventoryTexture() {
                    return INVENTORY_TERRACOTTA_TEXTURE;
                }
            });
            MenuScreens.register(ContainersNF.CHEST_9x3.get(), ChestScreen::new);
            MenuScreens.register(ContainersNF.CHEST_9x6.get(), ChestScreen::new);
            MenuScreens.register(ContainersNF.CRUCIBLE.get(), CrucibleScreen::new);
            MenuScreens.register(ContainersNF.TIERED_ANVIL.get(), TieredAnvilScreen::new);
            MenuScreens.register(ContainersNF.ENCYCLOPEDIA_PUZZLE.get(), EntryPuzzleScreen::new);

            ItemPropertyFunction slingSwinging = (item, level, user, seed) -> {
                if(user != null && ActionTracker.isPresent(user)) {
                    IActionTracker capA = ActionTracker.get(user);
                    InteractionHand hand = user instanceof Player player ? PlayerData.get(player).getActiveHand() : InteractionHand.MAIN_HAND;
                    if(user.getItemInHand(hand) == item && capA.getAction().is(TagsNF.SLING_ACTION)) return 1F;
                }
                return 0F;
            };
            ItemProperties.register(ItemsNF.SLING.get(), ResourceLocation.fromNamespaceAndPath(MODID, "swinging"), slingSwinging);
            ItemProperties.register(ItemsNF.SLING_REINFORCED.get(), ResourceLocation.fromNamespaceAndPath(MODID, "swinging"), slingSwinging);
            ItemPropertyFunction bowPull = (item, level, user, seed) -> {
                if(user != null && ActionTracker.isPresent(user)) {
                    IActionTracker capA = ActionTracker.get(user);
                    if(user instanceof Player player) {
                        InteractionHand hand = PlayerData.get(player).getActiveHand();
                        if(user.getItemInHand(hand) == item && capA.getAction().is(TagsNF.BOW_ACTION) && capA.isCharging()) return capA.getProgress(1F);
                    }
                    else {
                        Action action = capA.getAction();
                        if(user.getMainHandItem() == item && action.is(TagsNF.BOW_ACTION) && capA.getState() == action.getChargeState() - 1) {
                            return capA.getProgress(1F);
                        }
                    }
                }
                return 0F;
            };
            ItemPropertyFunction bowAmmo = (item, level, user, seed) -> ProjectileLauncherItem.getAmmoByte(item);
            for(var bow : Stream.concat(ItemsNF.BOWS.values().stream(), Stream.of(ItemsNF.TWISTED_BOW)).toList()) {
                ItemProperties.register(bow.get(), ResourceLocation.fromNamespaceAndPath(MODID, "pull"), bowPull);
                ItemProperties.register(bow.get(), ResourceLocation.fromNamespaceAndPath(MODID, "ammo"), bowAmmo);
            }
        });

        List<RegistryObject<? extends Block>> cutout = new ArrayList<>(), cutoutMipped = new ArrayList<>(), translucent = new ArrayList<>();
        cutoutMipped.addAll(BlocksNF.getCoveredSoils());
        for(Tree type : Tree.values()) {
            cutoutMipped.addAll(List.of(BlocksNF.LEAVES.get(type)));
            cutout.addAll(List.of(BlocksNF.TREE_SEEDS.get(type), BlocksNF.PLANK_TRAPDOORS.get(type), BlocksNF.PLANK_HATCHES.get(type),
                    BlocksNF.PLANK_DOORS.get(type), BlocksNF.PLANK_LADDERS.get(type), BlocksNF.CHAIRS.get(type)));
        }
        cutout.addAll(BlocksNF.FRUIT_LEAVES.values());
        cutout.addAll(BlocksNF.BRANCHES.values());
        cutoutMipped.addAll(List.of(BlocksNF.FRAZIL, BlocksNF.SEA_FRAZIL)); //Frazil is cutout and not translucent to avoid rendering issues with water, also needs mipmapping badly
        cutout.addAll(List.of(BlocksNF.SHORT_GRASS, BlocksNF.GRASS, BlocksNF.TALL_GRASS, BlocksNF.SMALL_FERN, BlocksNF.FERN,
                BlocksNF.LARGE_FERN, BlocksNF.VINES, BlocksNF.GLASS_BLOCK, BlocksNF.GLASS_SLAB, BlocksNF.GLASS_SIDING, BlocksNF.CRUCIBLE,
                BlocksNF.TORCH, BlocksNF.TORCH_UNLIT, BlocksNF.WALL_TORCH, BlocksNF.WALL_TORCH_UNLIT, BlocksNF.CAMPFIRE, BlocksNF.DEAD_CROP,
                BlocksNF.DEAD_PLANT, BlocksNF.POTATOES, BlocksNF.CARROTS, BlocksNF.FLAX, BlocksNF.YARROW, BlocksNF.BERRY_BUSH, BlocksNF.WARDING_EFFIGY,
                BlocksNF.DEAD_BUSH, BlocksNF.FIRE, BlocksNF.SPIDER_WEB));
        cutout.addAll(BlocksNF.LANTERNS.values());
        cutout.addAll(BlocksNF.LANTERNS_UNLIT.values());
        translucent.addAll(List.of(BlocksNF.MOON_ESSENCE, BlocksNF.ICE, BlocksNF.RABBIT_BURROW));
        for(var block : cutout) ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
        for(var block : cutoutMipped) ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped());
        for(var block : translucent) ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidsNF.WATER.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidsNF.WATER_FLOWING.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidsNF.SEAWATER.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(FluidsNF.SEAWATER_FLOWING.get(), RenderType.translucent());

        OverlayNF.register();
        TransformTypeNF.init();
    }

    @SubscribeEvent
    public static void onGatherDataEvent(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        if(event.includeServer()) {
            gen.addProvider(new ActionTagsProvider(gen, MODID, helper));
            gen.addProvider(new BiomeTagsProviderNF(gen, MODID, helper));
            BlockTagsProviderNF blockTagsProvider = new BlockTagsProviderNF(gen, MODID, helper);
            gen.addProvider(blockTagsProvider);
            gen.addProvider(new EntityTypeTagsProviderNF(gen, MODID, helper));
            gen.addProvider(new ItemTagsProviderNF(gen, blockTagsProvider, MODID, helper));
            gen.addProvider(new FluidTagsProviderNF(gen, MODID, helper));
            gen.addProvider(new LootTableProviderNF(gen));
            gen.addProvider(new RecipeProviderNF(gen));
            gen.addProvider(new SpawnGroupTagsProvider(gen, MODID, helper));
        }
        if(event.includeClient()) {
            gen.addProvider(new TextureProviderNF(gen, MODID, helper));
            gen.addProvider(new BlockStateProviderNF(gen, MODID, helper));
            gen.addProvider(new ItemModelProviderNF(gen, MODID, event.getExistingFileHelper()));
            gen.addProvider(new LanguageProviderNF(gen, MODID, "en_us"));
        }
    }

    @SubscribeEvent
    public static void onRegisterBlockEvent(RegistryEvent.Register<Block> event) {
        //Register objects that don't have ForgeRegistries here (any register event will do, Block is just first)
        GameEventsNF.register();
        LootItemConditionTypesNF.register();
        LootItemFunctionTypesNF.register();
        PlacementModifierTypesNF.register();
    }

    @SubscribeEvent
    public static void onRegisterFeatureEvent(RegistryEvent.Register<Feature<?>> event) {
        FeaturesNF.registerEvent(event);
        StructuresNF.registerEvent();
    }

    @SubscribeEvent
    public static void onRegisterRecipeSerializerEvent(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().register(BowlCrushingRecipe.SERIALIZER);
        event.getRegistry().register(HeldToolRecipe.SERIALIZER);
        event.getRegistry().register(CraftingRecipeNF.SERIALIZER);
        event.getRegistry().register(CauldronRecipe.SERIALIZER);
        event.getRegistry().register(CrucibleRecipe.SERIALIZER);
        event.getRegistry().register(FurnaceRecipe.SERIALIZER);
        event.getRegistry().register(TieredAnvilRecipe.SERIALIZER);
        event.getRegistry().register(BuildingRecipe.SERIALIZER);
        event.getRegistry().register(CampfireRecipe.SERIALIZER);
        event.getRegistry().register(BarrelRecipe.SERIALIZER);
    }

    @SubscribeEvent
    public static void onRegisterWorldTypeEvent(RegistryEvent.Register<ForgeWorldPreset> event) {
        event.getRegistry().register(new ContinentalWorldType());
        event.getRegistry().register(new FlatWorldType());
    }

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.register(IPlayerData.class);
        event.register(IActionTracker.class);
        event.register(IChunkData.class);
        event.register(ILevelData.class);
    }

    @SubscribeEvent
    public static void onEntityAttributeCreationEvent(EntityAttributeCreationEvent event) {
        event.put(EntitiesNF.RABBIT.get(), RabbitEntity.getAttributeMap().build());
        event.put(EntitiesNF.DEER.get(), DeerEntity.getAttributeMap().build());
        event.put(EntitiesNF.DRAKEFOWL_ROOSTER.get(), DrakefowlEntity.getAttributeMap().build());
        event.put(EntitiesNF.DRAKEFOWL_HEN.get(), DrakefowlEntity.getAttributeMap().build());
        event.put(EntitiesNF.DRAKEFOWL_CHICK.get(), DrakefowlBabyEntity.getAttributeMap().build());
        event.put(EntitiesNF.MERBOR_TUSKER.get(), MerborEntity.getAttributeMap().build());
        event.put(EntitiesNF.MERBOR_SOW.get(), MerborEntity.getAttributeMap().build());
        event.put(EntitiesNF.MERBOR_PIGLET.get(), MerborBabyEntity.getAttributeMap().build());
        event.put(EntitiesNF.HUSK.get(), HuskEntity.getAttributeMap().build());
        event.put(EntitiesNF.SKELETON.get(), SkeletonEntity.getAttributeMap().build());
        event.put(EntitiesNF.DREG.get(), DregEntity.getAttributeMap().build());
        event.put(EntitiesNF.CREEPER.get(), CreeperEntity.getAttributeMap().build());
        event.put(EntitiesNF.COCKATRICE.get(), CockatriceEntity.getAttributeMap().build());
        event.put(EntitiesNF.SPIDER.get(), SpiderEntity.getAttributeMap().build());
        event.put(EntitiesNF.ROCKWORM.get(), RockwormEntity.getAttributeMap().build());
        event.put(EntitiesNF.PIT_DEVIL.get(), PitDevilEntity.getAttributeMap().build());
        event.put(EntitiesNF.ECTOPLASM_LARGE.get(), EctoplasmEntity.getLargeAttributeMap().build());
        event.put(EntitiesNF.ECTOPLASM_MEDIUM.get(), EctoplasmEntity.getMediumAttributeMap().build());
        event.put(EntitiesNF.ECTOPLASM_SMALL.get(), EctoplasmEntity.getSmallAttributeMap().build());
        event.put(EntitiesNF.SCORPION.get(), ScorpionEntity.getAttributeMap().build());
        event.put(EntitiesNF.SKARA_SWARM.get(), SkaraSwarmEntity.getAttributeMap().build());
        event.put(EntitiesNF.TROLL.get(), TrollEntity.getAttributeMap().build());
        event.put(EntitiesNF.OLMUR.get(), OlmurEntity.getAttributeMap().build());
        event.put(EntitiesNF.JELLYFISH.get(), JellyfishEntity.getAttributeMap().build());
        event.put(EntitiesNF.ARMOR_STAND.get(), LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 100D).build());
    }

    @SubscribeEvent
    public static void onEntityAttributeModificationEvent(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributesNF.INVENTORY_CAPACITY.get());
        event.add(EntityType.PLAYER, AttributesNF.ACTION_SPEED.get());
        event.add(EntityType.PLAYER, AttributesNF.STAMINA_REDUCTION.get());
        event.add(EntityType.PLAYER, AttributesNF.ENDURANCE.get());
        event.add(EntityType.PLAYER, AttributesNF.WILLPOWER.get());
        event.add(EntityType.PLAYER, AttributesNF.STRENGTH.get());
        event.add(EntityType.PLAYER, AttributesNF.PERCEPTION.get());
        event.add(EntityType.PLAYER, AttributesNF.STRIKING_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.SLASHING_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.PIERCING_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.FIRE_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.FROST_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.ELECTRIC_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.WITHER_DEFENSE.get());
        event.add(EntityType.PLAYER, AttributesNF.POISE.get());
        event.add(EntityType.PLAYER, AttributesNF.BLEEDING_RESISTANCE.get());
        event.add(EntityType.PLAYER, AttributesNF.POISON_RESISTANCE.get());
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientRegistryEvents {
        @SubscribeEvent
        public static void onRegisterClientReloadListenersEvent(RegisterClientReloadListenersEvent event) {
            ClientEngine.registerClientReloadListeners(event);
        }

        @SubscribeEvent
        public static void onBlockColorHandlerEvent(ColorHandlerEvent.Block event) {
            event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                if(tintIndex == 1) {
                    if(level != null && pos != null && LevelData.isPresent(Minecraft.getInstance().level)) {
                        LevelChunk chunk = Minecraft.getInstance().level.getChunkAt(pos);
                        IChunkData capC = ChunkData.get(chunk);
                        return ClientEngine.get().getGrassColor(capC.getTemperature(pos), capC.getHumidity(pos));
                    }
                    return ClientEngine.get().getGrassColor(0.8F, 0.4F);
                }
                return Color.WHITE.getRGB();
            }), BlocksNF.COVERED_SILT.get(SoilCover.GRASS).get(), BlocksNF.COVERED_DIRT.get(SoilCover.GRASS).get(), BlocksNF.COVERED_LOAM.get(SoilCover.GRASS).get());

            event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                if(tintIndex == 0) {
                    if(level != null && pos != null && LevelData.isPresent(Minecraft.getInstance().level)) {
                        LevelChunk chunk = Minecraft.getInstance().level.getChunkAt(pos);
                        IChunkData capC = ChunkData.get(chunk);
                        return ClientEngine.get().getGrassColor(capC.getTemperature(pos), capC.getHumidity(pos));
                    }
                    return ClientEngine.get().getGrassColor(0.8F, 0.4F);
                }
                return Color.WHITE.getRGB();
            }), BlocksNF.SHORT_GRASS.get(), BlocksNF.GRASS.get(), BlocksNF.TALL_GRASS.get(), BlocksNF.SMALL_FERN.get(),
                    BlocksNF.FERN.get(), BlocksNF.LARGE_FERN.get(), BlocksNF.VINES.get());

            event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                if(tintIndex == 1) {
                    if(level != null && pos != null && LevelData.isPresent(Minecraft.getInstance().level)) {
                        LevelChunk chunk = Minecraft.getInstance().level.getChunkAt(pos);
                        IChunkData capC = ChunkData.get(chunk);
                        return ClientEngine.get().getForestColor(capC.getTemperature(pos));
                    }
                    return ClientEngine.get().getForestColor(0.5F);
                }
                return Color.WHITE.getRGB();
            }), BlocksNF.COVERED_SILT.get(SoilCover.FOREST).get(), BlocksNF.COVERED_DIRT.get(SoilCover.FOREST).get(), BlocksNF.COVERED_LOAM.get(SoilCover.FOREST).get());

            event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                if(tintIndex == 1) {
                    if(level != null && pos != null && LevelData.isPresent(Minecraft.getInstance().level)) {
                        LevelChunk chunk = Minecraft.getInstance().level.getChunkAt(pos);
                        IChunkData capC = ChunkData.get(chunk);
                        return ClientEngine.get().getLichenColor(capC.getHumidity(pos) * 3F); //Lichen naturally appears only in tundras, so max humidity caps out around 1/3
                    }
                    return ClientEngine.get().getLichenColor(0.5F);
                }
                return Color.WHITE.getRGB();
            }), BlocksNF.COVERED_SILT.get(SoilCover.LICHEN).get(), BlocksNF.COVERED_DIRT.get(SoilCover.LICHEN).get(), BlocksNF.COVERED_LOAM.get(SoilCover.LICHEN).get());

            for(Tree type : Tree.values()) {
                if(!type.isDeciduous()) continue;
                event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                    return ClientEngine.get().getLeavesColor(type, Season.getNormalizedProgress(Minecraft.getInstance().level));
                }), BlocksNF.LEAVES.get(type).get(), BlocksNF.TREE_SEEDS.get(type).get());
            }
            for(Tree type : BlocksNF.FRUIT_LEAVES.keySet()) {
                if(!type.isDeciduous()) continue;
                event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                    return tintIndex == 0 ? ClientEngine.get().getLeavesColor(type, Season.getNormalizedProgress(Minecraft.getInstance().level)) : Color.WHITE.getRGB();
                }), BlocksNF.FRUIT_LEAVES.get(type).get());
            }

            event.getBlockColors().register(((state, level, pos, tintIndex) -> {
                    return level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1;
            }), BlocksNF.WATER.get());
        }

        @SubscribeEvent
        public static void onItemColorHandlerEvent(ColorHandlerEvent.Item event) {
            List<Block> coloredBlockItems = new ArrayList<>();
            coloredBlockItems.addAll(BlocksNF.COVERED_SILT.values().stream().filter(block -> block.get().soilCover != SoilCover.MOSS).map(RegistryObject::get).toList());
            coloredBlockItems.addAll(BlocksNF.COVERED_DIRT.values().stream().filter(block -> block.get().soilCover != SoilCover.MOSS).map(RegistryObject::get).toList());
            coloredBlockItems.addAll(BlocksNF.COVERED_LOAM.values().stream().filter(block -> block.get().soilCover != SoilCover.MOSS).map(RegistryObject::get).toList());
            coloredBlockItems.addAll(BlocksNF.LEAVES.values().stream().map(RegistryObject::get).toList());
            coloredBlockItems.addAll(BlocksNF.FRUIT_LEAVES.values().stream().map(RegistryObject::get).toList());
            coloredBlockItems.addAll(List.of(BlocksNF.SHORT_GRASS.get(), BlocksNF.GRASS.get(), BlocksNF.TALL_GRASS.get(),
                    BlocksNF.SMALL_FERN.get(), BlocksNF.FERN.get(), BlocksNF.LARGE_FERN.get(), BlocksNF.VINES.get()));

            event.getItemColors().register((stack, layer) -> {
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return event.getBlockColors().getColor(blockstate, null, null, layer);
            }, coloredBlockItems.toArray(ItemLike[]::new));

            List<Item> dyeableItems = Stream.of(ItemsNF.ITEMS.getEntries()).flatMap(Collection::stream).map(RegistryObject::get)
                    .filter((item) -> item instanceof DyeableLeatherItem).toList();
            event.getItemColors().register((stack, layer) -> layer > 0 ? -1 : ((DyeableLeatherItem) stack.getItem()).getColor(stack),
                    dyeableItems.toArray(ItemLike[]::new));

            /*event.getItemColors().register((stack, layer) -> {
                if(layer <= 0) return -1;
                BlockState blockstate = ((BlockItem)stack.getItem()).getBlock().defaultBlockState();
                return event.getBlockColors().getColor(blockstate, null, null, layer);
            }, );*/
        }

        @SubscribeEvent
        public static void onModelRegistryEvent(ModelRegistryEvent event) {
            ModelLoaderRegistry.registerLoader(ResourceLocation.fromNamespaceAndPath(MODID, "animated-item"), AnimatedItemModel.Loader.INSTANCE);
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitionsEvent(EntityRenderersEvent.RegisterLayerDefinitions event) {
            ModelRegistryNF.init(event);
        }

        @SubscribeEvent
        public static void onEntityRenderersEvent(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntitiesNF.RABBIT.get(), RabbitRenderer::new);
            event.registerEntityRenderer(EntitiesNF.DEER.get(), DeerRenderer::new);
            event.registerEntityRenderer(EntitiesNF.DRAKEFOWL_ROOSTER.get(), DrakefowlRenderer::new);
            event.registerEntityRenderer(EntitiesNF.DRAKEFOWL_HEN.get(), DrakefowlRenderer::new);
            event.registerEntityRenderer(EntitiesNF.DRAKEFOWL_CHICK.get(), DrakefowlBabyRenderer::new);
            event.registerEntityRenderer(EntitiesNF.MERBOR_TUSKER.get(), MerborRenderer::new);
            event.registerEntityRenderer(EntitiesNF.MERBOR_SOW.get(), MerborRenderer::new);
            event.registerEntityRenderer(EntitiesNF.MERBOR_PIGLET.get(), MerborBabyRenderer::new);
            event.registerEntityRenderer(EntitiesNF.HUSK.get(), HuskRenderer::new);
            event.registerEntityRenderer(EntitiesNF.SKELETON.get(), SkeletonRenderer::new);
            event.registerEntityRenderer(EntitiesNF.DREG.get(), DregRenderer::new);
            event.registerEntityRenderer(EntitiesNF.CREEPER.get(), CreeperRenderer::new);
            event.registerEntityRenderer(EntitiesNF.COCKATRICE.get(), CockatriceRenderer::new);
            event.registerEntityRenderer(EntitiesNF.SPIDER.get(), SpiderRenderer::new);
            event.registerEntityRenderer(EntitiesNF.ROCKWORM.get(), RockwormRenderer::new);
            event.registerEntityRenderer(EntitiesNF.PIT_DEVIL.get(), PitDevilRenderer::new);
            event.registerEntityRenderer(EntitiesNF.ECTOPLASM_LARGE.get(), (context) -> new EctoplasmRenderer(context, 0.75F,
                    ModelRegistryNF.ECTOPLASM_LARGE_INNER, ModelRegistryNF.ECTOPLASM_LARGE_OUTER_INNER, ModelRegistryNF.ECTOPLASM_LARGE_OUTER));
            event.registerEntityRenderer(EntitiesNF.ECTOPLASM_MEDIUM.get(), (context) -> new EctoplasmRenderer(context, 0.5F,
                    ModelRegistryNF.ECTOPLASM_MEDIUM_INNER, ModelRegistryNF.ECTOPLASM_MEDIUM_OUTER_INNER, ModelRegistryNF.ECTOPLASM_MEDIUM_OUTER));
            event.registerEntityRenderer(EntitiesNF.ECTOPLASM_SMALL.get(), (context) -> new EctoplasmRenderer(context, 0.2F,
                    ModelRegistryNF.ECTOPLASM_SMALL_INNER, ModelRegistryNF.ECTOPLASM_SMALL_OUTER_INNER, ModelRegistryNF.ECTOPLASM_SMALL_OUTER));
            event.registerEntityRenderer(EntitiesNF.SCORPION.get(), ScorpionRenderer::new);
            event.registerEntityRenderer(EntitiesNF.SKARA_SWARM.get(), NameTagRenderer::new);
            event.registerEntityRenderer(EntitiesNF.TROLL.get(), TrollRenderer::new);
            event.registerEntityRenderer(EntitiesNF.OLMUR.get(), OlmurRenderer::new);
            event.registerEntityRenderer(EntitiesNF.JELLYFISH.get(), JellyfishRenderer::new);
            event.registerEntityRenderer(EntitiesNF.ARMOR_STAND.get(), ArmorStandDummyRenderer::new);
            event.registerEntityRenderer(EntitiesNF.BOAT.get(), BoatRendererNF::new);
            event.registerEntityRenderer(EntitiesNF.SEAT.get(), NoopRenderer::new);
            event.registerEntityRenderer(EntitiesNF.ROPE_KNOT.get(), RopeKnotRenderer::new);
            event.registerEntityRenderer(EntitiesNF.MOVING_BLOCK.get(), MovingBlockRenderer::new);
            event.registerEntityRenderer(EntitiesNF.THROWN_ROCK.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(EntitiesNF.ARROW.get(), ArrowRendererNF::new);
            event.registerEntityRenderer(EntitiesNF.THROWN_WEAPON.get(), ThrownWeaponRenderer::new);
            event.registerEntityRenderer(EntitiesNF.POISON_SPIT.get(), NoopRenderer::new);
            final ResourceLocation FIRE_SPIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/projectile/fire_spit.png");
            event.registerEntityRenderer(EntitiesNF.FIRE_SPIT.get(), (context) -> new EntitySpriteRenderer<>(context, 4F/16F, 0, 0) {
                @Override
                protected int getBlockLightLevel(FireSpitEntity pEntity, BlockPos pPos) {
                    return 15;
                }

                @Override
                public ResourceLocation getTextureLocation(FireSpitEntity pEntity) {
                    return FIRE_SPIT_TEXTURE;
                }
            });
            event.registerBlockEntityRenderer(BlockEntitiesNF.ANVIL.get(), TieredAnvilRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.BOWL.get(), BowlRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.CAULDRON.get(), CauldronRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.CAMPFIRE.get(), CampfireRendererNF::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.CHEST.get(), ChestRendererNF::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.SIGN.get(), SignRendererNF::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.ITEM_FRAME.get(), ItemFrameRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.RACK.get(), RackRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.SHELF.get(), ShelfRenderer::new);
            event.registerBlockEntityRenderer(BlockEntitiesNF.ITEM_MOLD.get(), ItemMoldRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterShadersEvent(RegisterShadersEvent event) throws IOException {
            RenderTypeNF.registerShaders(event);
        }

        @SubscribeEvent
        public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
            ChestRendererNF.stitchChestTextures(event);
            SignRendererNF.stitchSignTextures(event);
            if(event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
                event.addSprite(KnowledgeNF.KNOWLEDGE_TEXTURE);
                event.addSprite(KnowledgeNF.UNKNOWN_KNOWLEDGE_TEXTURE);
                event.addSprite(WorldConditionsNF.SATISFIED_CONDITION_TEXTURE);
                event.addSprite(WorldConditionsNF.UNSATISFIED_CONDITION_TEXTURE);
                event.addSprite(FluidsNF.METAL_STILL);
                event.addSprite(FluidsNF.METAL_FLOW);
                event.addSprite(FluidsNF.METAL_SOLID);
                event.addSprite(PlayerInventoryContainer.EMPTY_ACCESSORY_SLOT_FACE);
                event.addSprite(PlayerInventoryContainer.EMPTY_ACCESSORY_SLOT_NECK);
                event.addSprite(PlayerInventoryContainer.EMPTY_ACCESSORY_SLOT_WAIST);
                event.addSprite(PlayerInventoryContainer.EMPTY_RECIPE_SEARCH_SLOT);
                event.addSprite(PuzzleContainer.UNKNOWN_ITEM);
                event.addSprite(PuzzleContainer.FAILURE);
                event.addSprite(PuzzleContainer.SUCCESS);
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.MEAT_STEW.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.VEGETABLE_STEW.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.HEARTY_STEW.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.HAM_ROAST.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.SUSPICIOUS_STEW.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.BOILED_EGG.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.SOUFFLE.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.SOUFFLE.getId().getPath() + "_side"));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.FRUIT_SOUFFLE.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.FRUIT_SOUFFLE.getId().getPath() + "_side"));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.SAVORY_SOUFFLE.getId().getPath()));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + ItemsNF.SAVORY_SOUFFLE.getId().getPath() + "_side"));
                event.addSprite(ResourceLocation.fromNamespaceAndPath(MODID, "block/terracotta_darkened"));
            }
        }

        @SubscribeEvent
        public static void onParticleFactoryRegisterEvent(ParticleFactoryRegisterEvent event) {
            ParticleEngine engine = Minecraft.getInstance().particleEngine;
            engine.register(ParticleTypesNF.LEAF_BIRCH.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_CAEDTAR.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_IRONWOOD.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_JUNGLE.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_MAPLE.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_OAK.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.LEAF_WILLOW.get(), LeafParticle.Provider::new);
            engine.register(ParticleTypesNF.FLAME_RED.get(), FlameParticleNF.Provider::new);
            engine.register(ParticleTypesNF.FLAME_ORANGE.get(), FlameParticleNF.Provider::new);
            engine.register(ParticleTypesNF.FLAME_YELLOW.get(), FlameParticleNF.Provider::new);
            engine.register(ParticleTypesNF.FLAME_WHITE.get(), FlameParticleNF.Provider::new);
            engine.register(ParticleTypesNF.FLAME_BLUE.get(), FlameParticleNF.Provider::new);
            engine.register(ParticleTypesNF.SPARK_RED.get(), SparkParticle.RedProvider::new);
            engine.register(ParticleTypesNF.SPARK_ORANGE.get(), SparkParticle.OrangeProvider::new);
            engine.register(ParticleTypesNF.SPARK_YELLOW.get(), SparkParticle.YellowProvider::new);
            engine.register(ParticleTypesNF.SPARK_WHITE.get(), SparkParticle.WhiteProvider::new);
            engine.register(ParticleTypesNF.SPARK_BLUE.get(), SparkParticle.BlueProvider::new);
            engine.register(ParticleTypesNF.ESSENCE.get(), EssenceParticle.Provider::new);
            engine.register(ParticleTypesNF.ESSENCE_MOON.get(), EssenceParticle.MoonProvider::new);
            engine.register(ParticleTypesNF.BLOOD_RED.get(), FadingParticle.RedProvider::new);
            engine.register(ParticleTypesNF.BLOOD_DARK_RED.get(), FadingParticle.DarkRedProvider::new);
            engine.register(ParticleTypesNF.BLOOD_PALE_BLUE.get(), FadingParticle.PaleBlueProvider::new);
            engine.register(ParticleTypesNF.BLOOD_GREEN.get(), FadingParticle.GreenProvider::new);
            engine.register(ParticleTypesNF.ECTOPLASM.get(), FadingGlowingParticle.EctoplasmProvider::new);
            engine.register(ParticleTypesNF.FRAGMENT_BONE.get(), FragmentParticle.BoneProvider::new);
            engine.register(ParticleTypesNF.FRAGMENT_CREEPER.get(), FragmentParticle.CreeperProvider::new);
            engine.register(ParticleTypesNF.POISON_SPIT.get(), FadingParticle.PoisonSpitProvider::new);
            engine.register(ParticleTypesNF.DRIPPING_WATER.get(), WaterHangProvider::new);
            engine.register(ParticleTypesNF.FALLING_WATER.get(), WaterFallProvider::new);
            engine.register(ParticleTypesNF.DRIPPING_LAVA.get(), LavaHangProvider::new);
            engine.register(ParticleTypesNF.FALLING_LAVA.get(), LavaFallProvider::new);
            engine.register(ParticleTypesNF.LANDING_LAVA.get(), LavaLandProvider::new);
            engine.register(ParticleTypesNF.POISON.get(), FloatingParticle.Provider::new);
            engine.register(ParticleTypesNF.RAIN.get(), RainParticle.Provider::new);
            engine.register(ParticleTypesNF.RAIN_SPLASH.get(), ColoredWaterDropParticle.Provider::new);
            engine.register(ParticleTypesNF.SNOW.get(), SnowParticle.Provider::new);
            engine.register(ParticleTypesNF.FADING_CLOUD.get(), FadingCloudParticle.Provider::new);
            engine.register(ParticleTypesNF.SKARA.get(), SkaraParticle.Provider::new);
        }

        public static class WaterFallProvider implements ParticleProvider<SimpleParticleType> {
            protected final SpriteSet sprite;

            public WaterFallProvider(SpriteSet pSprites) {
                this.sprite = pSprites;
            }

            public Particle createParticle(SimpleParticleType pType, ClientLevel level, double x, double pY, double z, double xSpeed, double pYSpeed, double zSpeed) {
                DripParticle dripparticle = new DripParticle.FallAndLandParticle(level, x, pY, z, FluidsNF.WATER.get(), ParticleTypes.SPLASH);
                dripparticle.setColor(0.333F, 0.486F, 0.561F);
                dripparticle.pickSprite(this.sprite);
                return dripparticle;
            }
        }

        public static class WaterHangProvider implements ParticleProvider<SimpleParticleType> {
            protected final SpriteSet sprite;

            public WaterHangProvider(SpriteSet pSprites) {
                this.sprite = pSprites;
            }

            public Particle createParticle(SimpleParticleType pType, ClientLevel level, double x, double pY, double z, double xSpeed, double pYSpeed, double zSpeed) {
                DripParticle dripparticle = new DripParticle.DripHangParticle(level, x, pY, z, FluidsNF.WATER.get(), ParticleTypesNF.FALLING_WATER.get());
                dripparticle.setColor(0.333F, 0.486F, 0.561F);
                dripparticle.pickSprite(this.sprite);
                return dripparticle;
            }
        }

        public static class LavaLandProvider implements ParticleProvider<SimpleParticleType> {
            protected final SpriteSet sprite;

            public LavaLandProvider(SpriteSet pSprites) {
                this.sprite = pSprites;
            }

            public Particle createParticle(SimpleParticleType pType, ClientLevel level, double x, double pY, double z, double xSpeed, double pYSpeed, double zSpeed) {
                DripParticle dripparticle = new DripParticle.DripLandParticle(level, x, pY, z, FluidsNF.LAVA.get());
                dripparticle.setColor(0.839F, 0.153F, 0.047F);
                dripparticle.pickSprite(this.sprite);
                return dripparticle;
            }
        }

        public static class LavaFallProvider implements ParticleProvider<SimpleParticleType> {
            protected final SpriteSet sprite;

            public LavaFallProvider(SpriteSet pSprites) {
                this.sprite = pSprites;
            }

            public Particle createParticle(SimpleParticleType pType, ClientLevel level, double x, double pY, double z, double xSpeed, double pYSpeed, double zSpeed) {
                DripParticle dripparticle = new DripParticle.FallAndLandParticle(level, x, pY, z, FluidsNF.LAVA.get(), ParticleTypesNF.LANDING_LAVA.get());
                dripparticle.setColor(0.839F, 0.153F, 0.047F);
                dripparticle.pickSprite(this.sprite);
                return dripparticle;
            }
        }

        public static class LavaHangProvider implements ParticleProvider<SimpleParticleType> {
            protected final SpriteSet sprite;

            public LavaHangProvider(SpriteSet pSprites) {
                this.sprite = pSprites;
            }

            public Particle createParticle(SimpleParticleType pType, ClientLevel level, double x, double pY, double z, double xSpeed, double pYSpeed, double zSpeed) {
                DripParticle dripparticle = new DripParticle.CoolingDripHangParticle(level, x, pY, z, FluidsNF.LAVA.get(), ParticleTypesNF.FALLING_LAVA.get());
                dripparticle.pickSprite(this.sprite);
                return dripparticle;
            }
        }
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeRegistryEvents {
        @SubscribeEvent
        public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
            EntryCommand.register(event.getDispatcher());
            GodModeCommand.register(event.getDispatcher());
            InfoCommand.register(event.getDispatcher());
            KnowledgeCommand.register(event.getDispatcher());
            ReselectAttributesCommand.register(event.getDispatcher());
            SeasonCommand.register(event.getDispatcher());
            TimeCommandNF.register(event.getDispatcher());
            WeatherCommandNF.register(event.getDispatcher());
        }
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Config {
        public static ForgeConfigSpec COMMON_CONFIG, CLIENT_CONFIG;
        public static final ForgeConfigSpec.BooleanValue DISPLAY_CONTROLS_MESSAGE;

        static {
            ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
            clientBuilder.comment("Client Settings").push("client");

            DISPLAY_CONTROLS_MESSAGE = clientBuilder.comment("If true, displays the command to show controls upon joining a world")
                    .define("displayControlsMessage", true);

            clientBuilder.pop();
            CLIENT_CONFIG = clientBuilder.build();
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        }
    }
}