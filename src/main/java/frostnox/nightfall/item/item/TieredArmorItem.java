package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.item.ITieredArmorMaterial;
import frostnox.nightfall.item.Style;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class TieredArmorItem extends AttributeEquipmentItem {
    public final ITieredArmorMaterial material;

    protected static final UUID[] MODIFIER_UUID_PER_SLOT = new UUID[]{UUID.fromString("d52b28b7-ce27-4dd0-9bc4-f46fa14b97ca"),
            UUID.fromString("d3f35679-86da-4550-9ac1-36ea29c63640"),
            UUID.fromString("8ce067ec-def9-4aa8-b6ee-eb9e96c78e1a"),
            UUID.fromString("4afd627c-c640-4a62-8f55-112ee1442b9f")};

    public TieredArmorItem(ITieredArmorMaterial material, EquipmentSlot slot, Item.Properties builder) {
        super(slot, buildModifiersMap(slot, material), builder.defaultDurability(material.getDurability(slot)));
        this.material = material;
    }

    protected static ImmutableMultimap<Supplier<Attribute>, AttributeModifier> buildModifiersMap(EquipmentSlot slot, ITieredArmorMaterial material) {
        ImmutableMultimap.Builder<Supplier<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = MODIFIER_UUID_PER_SLOT[slot.getIndex()];
        if(material.getStyle() == Style.EXPLORER) builder.put(AttributesNF.STAMINA_REDUCTION, new AttributeModifier(uuid,
                "stamina_reduction", -0.075, AttributeModifier.Operation.ADDITION));
        else if(material.getStyle() == Style.SLAYER) {
            builder.put(AttributesNF.BLEEDING_RESISTANCE, new AttributeModifier(uuid,
                    "bleeding_resistance", 0.125, AttributeModifier.Operation.ADDITION));
            builder.put(AttributesNF.POISON_RESISTANCE, new AttributeModifier(uuid,
                    "poison_resistance", 0.125, AttributeModifier.Operation.ADDITION));
        }
        if(material.getKnockbackResistance(slot) != 0.0F) builder.put(() -> Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid,
                "knockback_resistance", material.getKnockbackResistance(slot), AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        if(material.getStyle() == Style.SURVIVOR) {
            tooltips.add(new TranslatableComponent("item.armor.style." + material.getStyle().getName()).withStyle(ChatFormatting.GOLD));
        }
        float durability = pStack.getMaxDamage() - pStack.getDamageValue();
        float halfDurability = pStack.getMaxDamage() / 2F;
        if(durability < halfDurability) {
            float durabilityPenalty = 0.5F - (durability / halfDurability * 0.5F);
            DecimalFormat format = new DecimalFormat("0.0");
            tooltips.add(new TranslatableComponent("item.durability_penalty", format.format(durabilityPenalty * 100)).withStyle(ChatFormatting.DARK_RED));
        }
    }
}
