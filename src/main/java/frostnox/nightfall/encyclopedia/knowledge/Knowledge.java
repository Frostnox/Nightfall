package frostnox.nightfall.encyclopedia.knowledge;

import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Knowledge is a requirement for encyclopedia entries and recipes
 */
public class Knowledge extends ForgeRegistryEntry<Knowledge> {
    private String descriptionId;

    public String getDescriptionId() {
        if(descriptionId == null) {
            descriptionId = Util.makeDescriptionId("knowledge", RegistriesNF.getKnowledge().getKey(this));
        }
        return descriptionId;
    }
}
