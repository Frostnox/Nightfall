package frostnox.nightfall.block.block;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IDropsItems;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class MenuContainerBlockEntity extends RandomizableContainerBlockEntity implements Container, IDropsItems {
    public boolean orderedLoot;

    public MenuContainerBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState) {
        super(pType, pos, pBlockState);
    }

    public @Nullable ContainerData getData() {
        return null;
    }
    
    public abstract ItemStackHandlerNF getInventory();

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(!tryLoadLootTable(tag)) getInventory().deserializeNBT(tag.getCompound("inventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(!trySaveLootTable(tag)) tag.put("inventory", getInventory().serializeNBT());
    }

    @Override
    protected boolean tryLoadLootTable(CompoundTag pTag) {
        if(super.tryLoadLootTable(pTag)) {
            orderedLoot = pTag.getBoolean("orderedLoot");
            return true;
        }
        else return false;
    }

    @Override
    protected boolean trySaveLootTable(CompoundTag pTag) {
        if(super.trySaveLootTable(pTag)) {
            pTag.putBoolean("orderedLoot", orderedLoot);
            return true;
        }
        else return false;
    }

    @Override
    public void unpackLootTable(@Nullable Player player) {
        if(lootTable != null && level.getServer() != null) {
            LootTable table = level.getServer().getLootTables().get(lootTable);
            if(player instanceof ServerPlayer) CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, lootTable);
            lootTable = null;
            LootContext.Builder builder = (new LootContext.Builder((ServerLevel)level)).withParameter(LootContextParams.ORIGIN,
                    Vec3.atCenterOf(worldPosition)).withOptionalRandomSeed(lootTableSeed);
            if(player != null) builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            LootContext context = builder.create(LootContextParamSets.CHEST);
            if(orderedLoot) {
                List<ItemStack> randomItems = table.getRandomItems(context);
                List<ItemStack> items = new ArrayList<>();
                for(ItemStack randomItem : randomItems) {
                    boolean combined = false;
                    for(ItemStack item : items) {
                        if(item.getCount() < item.getMaxStackSize() && ItemStack.isSameItemSameTags(randomItem, item)) {
                            item.grow(randomItem.split(Math.min(randomItem.getCount(), item.getMaxStackSize() - item.getCount())).getCount());
                            combined = randomItem.isEmpty();
                            break;
                        }
                    }
                    if(!combined) items.add(randomItem);
                }
                int i = 0;
                int size = getContainerSize();
                for(ItemStack item : items) {
                    if(item.isEmpty()) continue;
                    if(i >= size) {
                        Nightfall.LOGGER.warn("Tried to over-fill a container");
                        return;
                    }
                    while(!getItem(i).isEmpty()) i++;
                    setItem(i, item);
                    i++;
                }
            }
            else table.fill(this, context);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return getInventory().copyItems();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> pItemStacks) {
        for(int i = 0; i < pItemStacks.size(); i++) getInventory().setStackInSlot(i, pItemStacks.get(i));
    }

    @Override
    public int getContainerSize() {
        return getInventory().getSlots();
    }

    @Override
    public boolean isEmpty() {
        unpackLootTable(null);
        return getInventory().isEmpty();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        unpackLootTable(null);
        return getInventory().getStackInSlot(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        unpackLootTable(null);
        return getInventory().extractItem(pSlot, pAmount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        unpackLootTable(null);
        return getInventory().extractItemNoUpdate(pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        unpackLootTable(null);
        getInventory().setStackInSlot(pSlot, pStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockEntity(getBlockPos()) == this && getBlockPos().distToCenterSqr(player.getEyePosition()) <= 36D;
    }

    @Override
    public NonNullList<ItemStack> getContainerDrops() {
        return getItems();
    }

    @Override
    public void clearContent() {
        getInventory().clear();
    }
}
