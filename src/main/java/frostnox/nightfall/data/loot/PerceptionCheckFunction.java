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
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Empties ItemStack if perception check fails
 */
public class PerceptionCheckFunction extends LootItemConditionalFunction {
    private final float chance, increment;

    protected PerceptionCheckFunction(LootItemCondition[] pConditions, float chance, float increment) {
        super(pConditions);
        this.chance = chance;
        this.increment = increment;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Entity entity = context.hasParam(LootContextParams.KILLER_ENTITY) ? context.getParamOrNull(LootContextParams.KILLER_ENTITY)
                : context.getParamOrNull(LootContextParams.THIS_ENTITY);
        int perception = entity instanceof LivingEntity livingEntity ? AttributesNF.getPerceptionInfluence(livingEntity) : 0;
        if(context.getRandom().nextFloat() > chance + perception * increment) stack.setCount(0);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctionTypesNF.PERCEPTION_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    /**
     * @param chance base chance for success
     * @param increment amount per perception influence point to adjust chance by
     */
    public static Builder<?> with(float chance, float increment) {
        return simpleBuilder((conditions) -> new PerceptionCheckFunction(conditions, chance, increment));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PerceptionCheckFunction> {
        @Override
        public void serialize(JsonObject json, PerceptionCheckFunction func, JsonSerializationContext context) {
            super.serialize(json, func, context);
            json.addProperty("chance", func.chance);
            json.addProperty("increment", func.increment);
        }

        @Override
        public PerceptionCheckFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            float chance = GsonHelper.getAsFloat(json, "chance");
            float increment = GsonHelper.getAsFloat(json, "increment");
            return new PerceptionCheckFunction(conditions, chance, increment);
        }
    }
}
