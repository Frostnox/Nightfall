package frostnox.nightfall.encyclopedia;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.item.ItemStackHandlerNF;
import frostnox.nightfall.world.condition.WorldCondition;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public record Puzzle(List<RegistryObject<? extends Knowledge>> knowledge, List<Ingredient> ingredients, List<RegistryObject<? extends WorldCondition>> conditions) {
    public boolean isSolved(IPlayerData capP, ItemStackHandlerNF input) {
        for(RegistryObject<? extends Knowledge> knowledge : this.knowledge) {
            if(!capP.hasKnowledge(knowledge.getId())) return false;
        }
        for(int i = 0; i < ingredients.size(); i++) {
            if(!ingredients.get(i).test(input.getStackInSlot(i))) return false;
        }
        for(RegistryObject<? extends WorldCondition> condition : conditions) {
            if(!condition.get().test(capP.getPlayer())) return false;
        }
        return true;
    }

    public IntList getItemIcons(ItemStackHandlerNF input) {
        IntList icons = new IntArrayList(ingredients.size());
        for(int i = 0; i < ingredients.size(); i++) {
            icons.add(ingredients.get(i).test(input.getStackInSlot(i)) ? PuzzleContainer.SUCCESS_ICON : PuzzleContainer.FAILURE_ICON);
        }
        return icons;
    }

    public boolean hasAllKnowledge(IPlayerData capP) {
        for(RegistryObject<? extends Knowledge> knowledge : this.knowledge) {
            if(!capP.hasKnowledge(knowledge.getId())) return false;
        }
        return true;
    }
}
