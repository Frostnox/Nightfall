package frostnox.nightfall.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.vanilla.LootItemFunctionTypesNF;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Increases ItemStack count by specified value depending on perception
 */
public class PerceptionCountFunction extends LootItemConditionalFunction {
    private final int count;
    private final float chance, increment;

    protected PerceptionCountFunction(LootItemCondition[] pConditions, int count, float chance, float increment) {
        super(pConditions);
        this.count = count;
        this.chance = chance;
        this.increment = increment;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Entity entity = context.hasParam(LootContextParams.KILLER_ENTITY) ? context.getParamOrNull(LootContextParams.KILLER_ENTITY)
                : context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if(entity instanceof LivingEntity livingEntity) {
            int perception = AttributesNF.getPerceptionInfluence(livingEntity);
            if(context.getRandom().nextFloat() < chance + perception * increment) {
                stack.grow(count);
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctionTypesNF.PERCEPTION_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    /**
     * @param count count to increase ItemStack by if successful
     * @param chance base chance for success
     * @param increment amount per perception influence point to adjust chance by
     */
    public static LootItemConditionalFunction.Builder<?> with(int count, float chance, float increment) {
        return simpleBuilder((conditions) -> new PerceptionCountFunction(conditions, count, chance, increment));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PerceptionCountFunction> {
        @Override
        public void serialize(JsonObject json, PerceptionCountFunction func, JsonSerializationContext context) {
            super.serialize(json, func, context);
            json.addProperty("count", func.count);
            json.addProperty("chance", func.chance);
            json.addProperty("increment", func.increment);
        }

        @Override
        public PerceptionCountFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            int count = GsonHelper.getAsInt(json, "count");
            float chance = GsonHelper.getAsFloat(json, "chance");
            float increment = GsonHelper.getAsFloat(json, "increment");
            return new PerceptionCountFunction(conditions, count, chance, increment);
        }
    }
}
