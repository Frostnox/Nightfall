package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.anvil.TieredAnvilContainer;
import frostnox.nightfall.block.block.crucible.CrucibleContainer;
import frostnox.nightfall.encyclopedia.PuzzleContainer;
import frostnox.nightfall.world.inventory.StorageContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainersNF {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Nightfall.MODID);
    public static final RegistryObject<MenuType<StorageContainer>> BARREL = CONTAINERS.register("barrel", () -> IForgeMenuType.create(StorageContainer::createBarrelContainer));
    public static final RegistryObject<MenuType<StorageContainer>> CAULDRON = CONTAINERS.register("cauldron", () -> IForgeMenuType.create(StorageContainer::createCauldronContainer));
    public static final RegistryObject<MenuType<ChestMenu>> CHEST_9x3 = CONTAINERS.register("chest_9x3", () ->
            IForgeMenuType.create(((windowId, inv, data) -> ChestMenu.threeRows(windowId, inv))));
    public static final RegistryObject<MenuType<ChestMenu>> CHEST_9x6 = CONTAINERS.register("chest_9x6", () ->
            IForgeMenuType.create(((windowId, inv, data) -> ChestMenu.sixRows(windowId, inv))));
    public static final RegistryObject<MenuType<CrucibleContainer>> CRUCIBLE = CONTAINERS.register("crucible", () -> IForgeMenuType.create(CrucibleContainer::createClientContainer));
    public static final RegistryObject<MenuType<StorageContainer>> POT = CONTAINERS.register("pot", () -> IForgeMenuType.create(StorageContainer::createPotContainer));
    public static final RegistryObject<MenuType<TieredAnvilContainer>> TIERED_ANVIL = CONTAINERS.register("anvil", () -> IForgeMenuType.create(TieredAnvilContainer::createClientContainer));
    public static final RegistryObject<MenuType<PuzzleContainer>> ENCYCLOPEDIA_PUZZLE = CONTAINERS.register("encyclopedia_puzzle", () -> IForgeMenuType.create(PuzzleContainer::createClientContainer));

    public static void register() {
        CONTAINERS.register(Nightfall.MOD_EVENT_BUS);
    }
}
