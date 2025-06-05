package frostnox.nightfall.encyclopedia;

import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.forge.ContainersNF;
import frostnox.nightfall.world.inventory.PartialInventoryContainer;
import frostnox.nightfall.world.inventory.SingleFluidSlot;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;

public class PuzzleContainer extends PartialInventoryContainer {
    public static final int DEFAULT_ICON = 0, FAILURE_ICON = 1, SUCCESS_ICON = 2;
    public static final ResourceLocation UNKNOWN_ITEM = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/unknown_item");
    public static final ResourceLocation SUCCESS = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/puzzle_success");
    public static final ResourceLocation FAILURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "gui/icon/puzzle_failure");
    public final Entry entry;
    public final ItemStackHandlerNF inventory;
    public final IntList itemIcons;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return itemIcons.getInt(pIndex);
        }

        @Override
        public void set(int pIndex, int pValue) {
            itemIcons.set(pIndex, pValue);
        }

        @Override
        public int getCount() {
            return itemIcons.size();
        }
    };

    public PuzzleContainer(Inventory playerInv, int pContainerId, ResourceLocation entryId) {
        super(ContainersNF.ENCYCLOPEDIA_PUZZLE.get(), playerInv, pContainerId, true);
        entry = EntriesNF.get(entryId);
        Puzzle puzzle = entry.puzzle;
        inventory = new ItemStackHandlerNF(puzzle.ingredients().size());
        itemIcons = new IntArrayList(puzzle.ingredients().size());
        for(int i = 0; i < puzzle.ingredients().size(); i++) {
            itemIcons.add(DEFAULT_ICON);
            addSlot(new SingleFluidSlot(inventory, this, playerInv.player, i, 38 + i * 21, 51, false) {
                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    int icon = itemIcons.getInt(index);
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, icon == DEFAULT_ICON ? UNKNOWN_ITEM : (icon == FAILURE_ICON ? FAILURE : SUCCESS));
                }
            });
        }
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        inventory.transferToPlayer(pPlayer);
    }

    public static PuzzleContainer createClientContainer(int windowID, Inventory playerInv, FriendlyByteBuf extraData) {
        return new PuzzleContainer(playerInv, windowID, extraData.readResourceLocation());
    }
}
