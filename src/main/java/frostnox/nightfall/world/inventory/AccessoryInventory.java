package frostnox.nightfall.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class AccessoryInventory implements Container {
    public static final int SIZE = 3;
    public final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    public Player player;

    public AccessoryInventory() {

    }

    public void tick() {
        for(int i = 0; i < SIZE; i++) {
            ItemStack item = items.get(i);
            if(!item.isEmpty()) item.onArmorTick(player.level, player); //Use armor tick to distinguish between equipped and unequipped state
        }
    }

    public ListTag save() {
        ListTag listTag = new ListTag();
        for(int i = 0; i < SIZE; ++i) {
            if(!items.get(i).isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("slot", (byte)i);
                items.get(i).save(tag);
                listTag.add(tag);
            }
        }
        return listTag;
    }

    public void load(ListTag listTag) {
        items.clear();
        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag tag = listTag.getCompound(i);
            int index = tag.getByte("slot") & 255;
            ItemStack item = ItemStack.of(tag);
            if(!item.isEmpty() && index < SIZE) items.set(index, item);
        }
    }

    public void dropAll() {
        for(int i = 0; i < SIZE; ++i) {
            ItemStack item = items.get(i);
            if(!item.isEmpty()) {
                this.player.drop(item, true, false);
                items.set(i, ItemStack.EMPTY);
            }
        }
    }

    public void replaceWith(AccessoryInventory inventory) {
        for(int i = 0; i < SIZE; ++i) {
            setItem(i, inventory.getItem(i));
        }
    }

    public ItemStack getItem(AccessorySlot slot) {
        return items.get(slot.ordinal());
    }

    public void setItem(AccessorySlot slot, ItemStack item) {
        setItem(slot.ordinal(), item);
    }

    public boolean contains(ItemStack searchItem) {
        for(ItemStack item : items) {
            if(!item.isEmpty() && item.sameItem(searchItem)) return true;
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack item : this.items) {
            if(!item.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        if(index < 0 || index >= SIZE) return null;
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if(index < 0 || index >= SIZE) return ItemStack.EMPTY;
        return !items.get(index).isEmpty() ? ContainerHelper.removeItem(items, index, count) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if(index < 0 || index >= SIZE) return ItemStack.EMPTY;
        if(!items.get(index).isEmpty()) {
            ItemStack item = items.get(index);
            items.set(index, ItemStack.EMPTY);
            return item;
        }
        else return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if(index < 0 || index >= SIZE) return;
        if(item.getTag() != null) item.getItem().verifyTagAfterLoad(item.getTag());
        if(!item.isEmpty() && !player.isSpectator()) {
            SoundEvent sound = item.getEquipSound();
            if(sound != null) {
                player.gameEvent(GameEvent.EQUIP);
                player.playSound(sound, 1.0F, 1.0F);
            }
        }
        items.set(index, item);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        if(this.player == null || this.player.isRemoved()) return false;
        else return !(player.distanceToSqr(this.player) > 64.0D);
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
