package frostnox.nightfall.data;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;

import javax.annotation.Nullable;

public class ActionTagsProvider extends ForgeRegistryTagsProvider<Action> {
    public ActionTagsProvider(DataGenerator generator, String id, @Nullable ExistingFileHelper helper) {
        super(generator, RegistriesNF.getActions(), id, helper);
    }

    @Override
    protected void addTags() {
        tag(TagsNF.SMITHING_ACTION).add(ActionsNF.HAMMER_BASIC_1.get(), ActionsNF.HAMMER_BASIC_2.get(), ActionsNF.HAMMER_BASIC_3.get(), ActionsNF.HAMMER_ALTERNATE_1.get(),
                ActionsNF.HAMMER_UPSET.get(), ActionsNF.CHISEL_AND_HAMMER_BASIC_1.get(), ActionsNF.CHISEL_AND_HAMMER_BASIC_2.get(), ActionsNF.CHISEL_AND_HAMMER_BASIC_3.get(),
                ActionsNF.CHISEL_AND_HAMMER_ALTERNATE.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_1.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_2.get(),
                ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_3.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_ALTERNATE.get());
        tag(TagsNF.BOW_ACTION).add(ActionsNF.BOW_SHOOT.get(), ActionsNF.TWISTED_BOW_SHOOT.get(), ActionsNF.SKELETON_SHOOT.get());
        tag(TagsNF.SLING_ACTION).add(ActionsNF.SLING_THROW.get());
        tag(TagsNF.ADZE_ACTION).add(ActionsNF.ADZE_BASIC_1.get(), ActionsNF.ADZE_BASIC_2.get(), ActionsNF.ADZE_BASIC_3.get(),
                ActionsNF.ADZE_ALTERNATE_1.get(), ActionsNF.ADZE_CRAWLING.get(), ActionsNF.ADZE_CARVE.get());
        tag(TagsNF.CHOPPING_ACTION).add(ActionsNF.AXE_ALTERNATE_1.get());
        tag(TagsNF.SLOW_PLAYER_HARVEST_ACTION).add(ActionsNF.MAUL_BASIC_1.get(), ActionsNF.MAUL_BASIC_2.get(), ActionsNF.MAUL_BASIC_3.get(),
                ActionsNF.MAUL_ALTERNATE_1.get(), ActionsNF.MAUL_CRAWLING.get(), ActionsNF.CHISEL_AND_HAMMER_BASIC_1.get(),
                ActionsNF.CHISEL_AND_HAMMER_BASIC_2.get(), ActionsNF.CHISEL_AND_HAMMER_BASIC_3.get(), ActionsNF.CHISEL_AND_HAMMER_ALTERNATE.get(),
                ActionsNF.CHISEL_AND_HAMMER_CRAWLING.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_1.get(),
                ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_2.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_BASIC_3.get(),
                ActionsNF.FLINT_CHISEL_AND_HAMMER_ALTERNATE.get(), ActionsNF.FLINT_CHISEL_AND_HAMMER_CRAWLING.get());
    }

    @Override
    public String getName() {
        return "Action Tags";
    }
}
