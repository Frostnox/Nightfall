package frostnox.nightfall.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.vanilla.LootItemConditionTypesNF;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class ActionTagCondition implements LootItemCondition {
    private final TagKey<Action> tag;

    private ActionTagCondition(TagKey<Action> tag) {
        this.tag = tag;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditionTypesNF.ACTION_TAG;
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if(entity != null && ActionTracker.isPresent(entity)) return ActionTracker.get(entity).getAction().is(tag);
        else return false;
    }

    public static ActionTagCondition.Builder of(TagKey<Action> tag) {
        return new Builder(tag);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final TagKey<Action> type;

        public Builder(TagKey<Action> type) {
            this.type = type;
        }

        @Override
        public LootItemCondition build() {
            return new ActionTagCondition(type);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ActionTagCondition> {
        @Override
        public void serialize(JsonObject json, ActionTagCondition condition, JsonSerializationContext context) {
            json.addProperty("tag", condition.tag.location().toString());
        }

        @Override
        public ActionTagCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            ResourceLocation tagName = ResourceLocation.parse(GsonHelper.getAsString(json, "tag"));
            return new ActionTagCondition(TagKey.create(RegistriesNF.ACTIONS_KEY, tagName));
        }
    }
}
