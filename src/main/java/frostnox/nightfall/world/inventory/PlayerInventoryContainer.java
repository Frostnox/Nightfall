package frostnox.nightfall.world.inventory;

import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.recipe.CraftingRecipeNF;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

import java.util.Optional;

public class PlayerInventoryContainer extends InventoryMenu {
    public static final ResourceLocation EMPTY_ACCESSORY_SLOT_FACE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "item/empty_accessory_slot_face");
    public static final ResourceLocation EMPTY_ACCESSORY_SLOT_NECK = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "item/empty_accessory_slot_neck");
    public static final ResourceLocation EMPTY_ACCESSORY_SLOT_WAIST = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "item/empty_accessory_slot_waist");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET, EMPTY_ACCESSORY_SLOT_FACE, EMPTY_ACCESSORY_SLOT_NECK, EMPTY_ACCESSORY_SLOT_WAIST};
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player owner;

    public PlayerInventoryContainer(Inventory playerInv, boolean serverSide) {
        super(playerInv, serverSide, playerInv.player);
        slots.clear();
        lastSlots.clear();
        remoteSlots.clear();
        owner = playerInv.player;
        addSlot(new ResultSlot(playerInv.player, craftSlots, resultSlots, 0, 151, 44) {
            public void onTake(Player pPlayer, ItemStack pStack) {
                checkTakeAchievements(pStack);
                net.minecraftforge.common.ForgeHooks.setCraftingPlayer(pPlayer);
                NonNullList<ItemStack> remainders = pPlayer.level.getRecipeManager().getRemainingItemsFor(CraftingRecipeNF.TYPE, craftSlots, pPlayer.level);
                net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
                for(int i = 0; i < remainders.size(); ++i) {
                    ItemStack input = craftSlots.getItem(i);
                    ItemStack remainder = remainders.get(i);
                    if(!input.isEmpty()) {
                        if(input.getTag() != null && input.getTag().contains("Damage")) {
                            input.hurt(1, pPlayer.level.getRandom(), null);
                            if(input.getDamageValue() >= input.getMaxDamage()) craftSlots.removeItem(i, 1);
                        }
                        else craftSlots.removeItem(i, 1);
                        input = craftSlots.getItem(i);
                    }

                    if (!remainder.isEmpty()) {
                        if (input.isEmpty()) {
                            craftSlots.setItem(i, remainder);
                        } else if (ItemStack.isSame(input, remainder) && ItemStack.tagMatches(input, remainder)) {
                            remainder.grow(input.getCount());
                            craftSlots.setItem(i, remainder);
                        } else if (!pPlayer.getInventory().add(remainder)) {
                            pPlayer.drop(remainder, false);
                        }
                    }
                }

            }
        });

        int craftX = 96;
        int craftY = 26;
        //Original crafting slots
        addSlot(new Slot(craftSlots, 0, craftX, craftY));
        addSlot(new Slot(craftSlots, 1, craftX + 18, craftY));
        addSlot(new Slot(craftSlots, 3, craftX, craftY + 18));
        addSlot(new Slot(craftSlots, 4, craftX + 18, craftY + 18));

        //Equipment slots
        for(int k = 0; k < 4; ++k) {
            final EquipmentSlot equipmentslot = SLOT_IDS[k];
            addSlot(new Slot(playerInv, 39 - k, 8, 8 + k * 18) {
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack p_39746_) {
                    return p_39746_.canEquip(equipmentslot, owner);
                }

                public boolean mayPickup(Player p_39744_) {
                    ItemStack item = getItem();
                    return !item.isEmpty() && !p_39744_.isCreative() && EnchantmentHelper.hasBindingCurse(item) ? false : super.mayPickup(p_39744_);
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
                }
            });
        }

        //Inventory slots
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                if(j < 5) addSlot(new Slot(playerInv, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18)); //Base
                else addSlot(new CapacitySlot(i * 4 + j - 5, playerInv, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
            }
        }

        //Hotbar slots
        for(int i1 = 0; i1 < 9; ++i1) {
            addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 142));
        }

        //Offhand slot
        addSlot(new Slot(playerInv, 40, 75, 62) {
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        //Accessory slots
        AccessoryInventory accessoryInventory = PlayerData.get(owner).getAccessoryInventory();
        for(int i = 0; i < 3; i++) {
            AccessorySlot accessorySlot = AccessorySlot.values()[i];
            addSlot(new Slot(accessoryInventory, i, 75, 8 + 18 * i) {
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack p_39746_) {
                    return accessorySlot.acceptsItem(p_39746_);
                }

                public boolean mayPickup(Player p_39744_) {
                    ItemStack item = getItem();
                    return !item.isEmpty() && !p_39744_.isCreative() && EnchantmentHelper.hasBindingCurse(item) ? false : super.mayPickup(p_39744_);
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[4 + accessorySlot.ordinal()]);
                }
            });
        }

        //Remaining extra crafting slots
        addSlot(new Slot(craftSlots, 2, craftX + 2 * 18, craftY + 0 * 18));
        addSlot(new Slot(craftSlots, 5, craftX + 2 * 18, craftY + 1 * 18));
        addSlot(new Slot(craftSlots, 6, craftX + 0 * 18, craftY + 2 * 18));
        addSlot(new Slot(craftSlots, 7, craftX + 1 * 18, craftY + 2 * 18));
        addSlot(new Slot(craftSlots, 8, craftX + 2 * 18, craftY + 2 * 18));
    }

    public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {
        craftSlots.fillStackedContents(pItemHelper);
    }

    public void clearCraftingContent() {
        resultSlots.clearContent();
        craftSlots.clearContent();
    }

    public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
        return pRecipe.matches(craftSlots, owner.level);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void slotsChanged(Container pInventory) {
        ForgeHooks.setCraftingPlayer(owner);
        slotChangedCraftingGrid(this, owner.level, owner, craftSlots, resultSlots);
        ForgeHooks.setCraftingPlayer(null);
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu p_150547_, Level p_150548_, Player p_150549_, CraftingContainer p_150550_, ResultContainer p_150551_) {
        if (!p_150548_.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)p_150549_;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<CraftingRecipeNF> optional = p_150548_.getServer().getRecipeManager().getRecipeFor(CraftingRecipeNF.TYPE, p_150550_, p_150548_);
            if (optional.isPresent()) {
                CraftingRecipe craftingrecipe = optional.get();
                if (p_150551_.setRecipeUsed(p_150548_, serverplayer, craftingrecipe)) {
                    itemstack = craftingrecipe.assemble(p_150550_);
                }
            }

            p_150551_.setItem(0, itemstack);
            p_150547_.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(p_150547_.containerId, p_150547_.incrementStateId(), 0, itemstack));
        }
    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        resultSlots.clearContent();
        if(!pPlayer.level.isClientSide) {
            clearContainer(pPlayer, craftSlots);
        }
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
            if(pIndex == 0) { //Result slot
                if (!moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex >= 1 && pIndex < 9 || (pIndex >= 46 && pIndex <= 50)) { //Crafting/equipment slots
                if (!moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR && !slots.get(8 - equipmentslot.getIndex()).hasItem()) {
                int i = 8 - equipmentslot.getIndex();
                if (!moveItemStackTo(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot == EquipmentSlot.OFFHAND && !slots.get(45).hasItem()) {
                if (!moveItemStackTo(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack.is(TagsNF.ACCESSORY_FACE) && !slots.get(46).hasItem()) {
                if (!moveItemStackTo(itemstack1, 46, 47, false)) {
                    return ItemStack.EMPTY;
                }
            }  else if (itemstack.is(TagsNF.ACCESSORY_NECK) && !slots.get(47).hasItem()) {
                if (!moveItemStackTo(itemstack1, 47, 48, false)) {
                    return ItemStack.EMPTY;
                }
            }  else if (itemstack.is(TagsNF.ACCESSORY_WAIST) && !slots.get(48).hasItem()) {
                if (!moveItemStackTo(itemstack1, 48, 49, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (pIndex >= 9 && pIndex < 36) { //Inventory
                if (!moveItemStackTo(itemstack1, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (pIndex >= 36 && pIndex < 45) { //Hotbar
                if (!moveItemStackTo(itemstack1, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
            if (pIndex == 0) {
                pPlayer.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
     * null for the initial slot that was double-clicked.
     */
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }

    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return craftSlots.getWidth();
    }

    public int getGridHeight() {
        return craftSlots.getHeight();
    }

    public int getSize() {
        return 10;
    }

    public CraftingContainer getCraftSlots() {
        return craftSlots;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int p_150591_) {
        return p_150591_ != getResultSlotIndex();
    }
}
