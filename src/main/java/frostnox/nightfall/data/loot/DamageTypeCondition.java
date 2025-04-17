package frostnox.nightfall.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.registry.vanilla.LootItemConditionTypesNF;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class DamageTypeCondition implements LootItemCondition {
    private final DamageType type;

    private DamageTypeCondition(DamageType type) {
        this.type = type;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.DAMAGE_SOURCE);
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditionTypesNF.DAMAGE_TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        DamageSource source = context.getParam(LootContextParams.DAMAGE_SOURCE);
        DamageTypeSource typeSource = source instanceof DamageTypeSource ? (DamageTypeSource) source : DamageTypeSource.convertFromVanilla(source);
        for(DamageType sourceType : typeSource.types) {
            if(sourceType == type) return true;
        }
        return false;
    }

    public static DamageTypeCondition.Builder of(DamageType type) {
        return new Builder(type);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final DamageType type;

        public Builder(DamageType type) {
            this.type = type;
        }

        @Override
        public LootItemCondition build() {
            return new DamageTypeCondition(type);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageTypeCondition> {
        @Override
        public void serialize(JsonObject json, DamageTypeCondition condition, JsonSerializationContext context) {
            json.addProperty("type", condition.type.toString());
        }

        @Override
        public DamageTypeCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            String typeName = GsonHelper.getAsString(json, "type");
            DamageType type = null;
            for(DamageType t : DamageType.values()) {
                if(t.toString().equals(typeName)) {
                    type = t;
                    break;
                }
            }
            if(type == null) throw new IllegalStateException("Failed to deserialize unknown type '" + typeName + "' in " + DamageTypeCondition.class.getSimpleName());
            return new DamageTypeCondition(type);
        }
    }
}
