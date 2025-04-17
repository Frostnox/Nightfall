package frostnox.nightfall.data.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import frostnox.nightfall.registry.vanilla.LootItemFunctionTypesNF;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemColorFunction extends LootItemConditionalFunction {
   private final int color;

   SetItemColorFunction(LootItemCondition[] pConditions, int color) {
      super(pConditions);
      this.color = color;
   }

   @Override
   public LootItemFunctionType getType() {
      return LootItemFunctionTypesNF.SET_ITEM_COLOR;
   }

   @Override
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if(pStack.getItem() instanceof DyeableLeatherItem dyeableItem) {
         dyeableItem.setColor(pStack, color);
      }
      return pStack;
   }

   public static Builder<?> color(int color) {
      return simpleBuilder((conditions) -> new SetItemColorFunction(conditions, color));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemColorFunction> {
      @Override
      public void serialize(JsonObject pJson, SetItemColorFunction pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("color", pValue.color);
      }

      @Override
      public SetItemColorFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         return new SetItemColorFunction(pConditions, GsonHelper.getAsInt(pObject, "color"));
      }
   }
}