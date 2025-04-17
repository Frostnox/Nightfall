package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class AttributeAccessoryItem extends AttributeItem {
    public AttributeAccessoryItem(ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(modifiers, properties);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(AccessorySlot slot, ItemStack stack) {
        return slot.acceptsItem(stack) ? modifiers.get() : ImmutableMultimap.of();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        AccessorySlot slot = AccessorySlot.getSlotFor(item);
        if(slot != null) {
            AccessoryInventory inventory = PlayerData.get(player).getAccessoryInventory();
            ItemStack slotItem = inventory.getItem(slot);
            if(slotItem.isEmpty()) {
                ItemStack copy = item.copy();
                copy.setCount(1);
                inventory.setItem(slot, copy);
                if(!player.isCreative()) item.shrink(1);
                return InteractionResultHolder.sidedSuccess(item, level.isClientSide());
            }
        }
        return InteractionResultHolder.fail(item);
    }
}
