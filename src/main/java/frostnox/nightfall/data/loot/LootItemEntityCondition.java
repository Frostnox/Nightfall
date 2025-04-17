package frostnox.nightfall.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import frostnox.nightfall.entity.IHungerEntity;
import frostnox.nightfall.entity.entity.animal.DeerEntity;
import frostnox.nightfall.entity.entity.animal.RabbitEntity;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
import frostnox.nightfall.registry.vanilla.LootItemConditionTypesNF;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Locale;
import java.util.Set;

public class LootItemEntityCondition implements LootItemCondition {
    public enum Test {
        LIVING_PRESENT, HUNGRY, RABBIT_BRUSH, RABBIT_COTTONTAIL, RABBIT_ARCTIC, RABBIT_STRIPED, DEER_BRIAR, DEER_RED, DEER_SPOTTED, COCKATRICE_BRONZE, COCKATRICE_EMERALD;

        private final String name;

        Test() {
            name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return name;
        }
    }
    private final LootContext.EntityTarget entityTarget;
    private final Test test;

    private LootItemEntityCondition(LootContext.EntityTarget entityTarget, Test test) {
        this.entityTarget = entityTarget;
        this.test = test;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(entityTarget.getParam());
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditionTypesNF.ENTITY;
    }

    @Override
    public boolean test(LootContext context) {
        if(!context.hasParam(entityTarget.getParam())) return false;
        return switch(test) {
            case LIVING_PRESENT -> context.getParam(entityTarget.getParam()) instanceof LivingEntity;
            case HUNGRY -> context.getParam(entityTarget.getParam()) instanceof IHungerEntity hungerEntity && hungerEntity.isHungry();
            case RABBIT_BRUSH -> context.getParam(entityTarget.getParam()) instanceof RabbitEntity rabbit && rabbit.getRabbitType() == RabbitEntity.Type.BRUSH;
            case RABBIT_ARCTIC -> context.getParam(entityTarget.getParam()) instanceof RabbitEntity rabbit && rabbit.getRabbitType() == RabbitEntity.Type.ARCTIC;
            case RABBIT_COTTONTAIL -> context.getParam(entityTarget.getParam()) instanceof RabbitEntity rabbit && rabbit.getRabbitType() == RabbitEntity.Type.COTTONTAIL;
            case RABBIT_STRIPED -> context.getParam(entityTarget.getParam()) instanceof RabbitEntity rabbit && rabbit.getRabbitType() == RabbitEntity.Type.STRIPED;
            case DEER_BRIAR -> context.getParam(entityTarget.getParam()) instanceof DeerEntity deer && deer.getDeerType() == DeerEntity.Type.BRIAR;
            case DEER_RED -> context.getParam(entityTarget.getParam()) instanceof DeerEntity deer && deer.getDeerType() == DeerEntity.Type.RED;
            case DEER_SPOTTED -> context.getParam(entityTarget.getParam()) instanceof DeerEntity deer && deer.getDeerType() == DeerEntity.Type.SPOTTED;
            case COCKATRICE_BRONZE -> context.getParam(entityTarget.getParam()) instanceof CockatriceEntity cockatrice && cockatrice.getCockatriceType() == CockatriceEntity.Type.BRONZE;
            case COCKATRICE_EMERALD -> context.getParam(entityTarget.getParam()) instanceof CockatriceEntity cockatrice && cockatrice.getCockatriceType() == CockatriceEntity.Type.EMERALD;
        };
    }

    public static LootItemEntityCondition.Builder of(Test test) {
        return new Builder(test);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final Test test;

        public Builder(Test test) {
            this.test = test;
        }

        @Override
        public LootItemCondition build() {
            return new LootItemEntityCondition(LootContext.EntityTarget.THIS, test);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemEntityCondition> {
        @Override
        public void serialize(JsonObject json, LootItemEntityCondition condition, JsonSerializationContext context) {
            json.add("entity", context.serialize(condition.entityTarget));
            json.addProperty("test", condition.test.toString());
        }

        @Override
        public LootItemEntityCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            LootContext.EntityTarget entityTarget = GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class);
            String testName = GsonHelper.getAsString(json, "test");
            Test test = null;
            for(Test t : Test.values()) {
                if(t.toString().equals(testName)) {
                    test = t;
                    break;
                }
            }
            if(test == null) throw new IllegalStateException("Failed to deserialize unknown test '" + testName + "' in " + LootItemEntityCondition.class.getSimpleName());
            return new LootItemEntityCondition(entityTarget, test);
        }
    }
}
