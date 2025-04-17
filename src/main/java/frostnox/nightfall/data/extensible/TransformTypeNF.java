package frostnox.nightfall.data.extensible;

import frostnox.nightfall.Nightfall;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;

public abstract class TransformTypeNF {
    public static final ItemTransforms.TransformType RACK = ItemTransforms.TransformType.create(
            Nightfall.MODID + "_rack", ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "rack"));
    public static final ItemTransforms.TransformType THROWN = ItemTransforms.TransformType.create(
            Nightfall.MODID + "_thrown", ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "thrown"));

    public static void init() {
        //Ensure that fields are created during setup
    }
}
