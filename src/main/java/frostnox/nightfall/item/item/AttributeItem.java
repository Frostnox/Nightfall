package frostnox.nightfall.item.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.util.Lazy;

import java.util.function.Supplier;

public class AttributeItem extends ItemNF {
    protected final Lazy<Multimap<Attribute, AttributeModifier>> modifiers; //Lazy since mod attributes aren't accessible yet

    public AttributeItem(ImmutableMultimap<Supplier<Attribute>, AttributeModifier> modifiers, Properties properties) {
        super(properties);
        this.modifiers = Lazy.of(() -> {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            for(var entry : modifiers.entries()) builder.put(entry.getKey().get(), entry.getValue());
            return builder.build();
        });
    }
}
