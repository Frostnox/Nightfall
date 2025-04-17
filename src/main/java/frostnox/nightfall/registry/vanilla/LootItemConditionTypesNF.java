package frostnox.nightfall.registry.vanilla;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.loot.ActionTagCondition;
import frostnox.nightfall.data.loot.DamageTypeCondition;
import frostnox.nightfall.data.loot.LootItemEntityCondition;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LootItemConditionTypesNF {
    public static LootItemConditionType ENTITY;
    public static LootItemConditionType DAMAGE_TYPE;
    public static LootItemConditionType ACTION_TAG;

    public static void register() {
        ENTITY = register("entity", new LootItemEntityCondition.Serializer());
        DAMAGE_TYPE = register("damage_type", new DamageTypeCondition.Serializer());
        ACTION_TAG = register("action_tag", new ActionTagCondition.Serializer());
    }

    private static LootItemConditionType register(String name, Serializer<? extends LootItemCondition> serializer) {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, Nightfall.MODID + ":" + name, new LootItemConditionType(serializer));
    }
}
