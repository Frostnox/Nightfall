package frostnox.nightfall.registry.vanilla;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.data.loot.PerceptionCheckFunction;
import frostnox.nightfall.data.loot.PerceptionCountFunction;
import frostnox.nightfall.data.loot.SetItemColorFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class LootItemFunctionTypesNF {
    public static LootItemFunctionType PERCEPTION_COUNT;
    public static LootItemFunctionType PERCEPTION_CHECK;
    public static LootItemFunctionType SET_ITEM_COLOR;

    public static void register() {
        PERCEPTION_COUNT = register("perception_count", new PerceptionCountFunction.Serializer());
        PERCEPTION_CHECK = register("perception_check", new PerceptionCheckFunction.Serializer());
        SET_ITEM_COLOR = register("set_item_color", new SetItemColorFunction.Serializer());
    }

    private static LootItemFunctionType register(String name, Serializer<? extends LootItemFunction> serializer) {
        return Registry.register(Registry.LOOT_FUNCTION_TYPE, Nightfall.MODID + ":" + name, new LootItemFunctionType(serializer));
    }
}
