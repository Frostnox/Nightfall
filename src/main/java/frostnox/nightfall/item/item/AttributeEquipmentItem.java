package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AttributeEquipmentItem extends AttributeItem {
    public final EquipmentSlot slot;

    public AttributeEquipmentItem(EquipmentSlot slot, ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(modifiers, properties);
        this.slot = slot;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == this.slot ? modifiers.get() : super.getAttributeModifiers(slot, stack);
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return slot;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack usedItem = player.getItemInHand(hand);
        ItemStack equippedItem = player.getItemBySlot(slot);
        if(equippedItem.isEmpty()) {
            player.setItemSlot(slot, usedItem.copy());
            usedItem.setCount(0);
            return InteractionResultHolder.sidedSuccess(usedItem, level.isClientSide());
        }
        else return InteractionResultHolder.fail(usedItem);
    }
}
