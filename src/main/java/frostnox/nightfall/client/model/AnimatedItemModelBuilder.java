package frostnox.nightfall.client.model;

import com.google.gson.JsonObject;
import frostnox.nightfall.Nightfall;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AnimatedItemModelBuilder extends ModelBuilder<AnimatedItemModelBuilder> {
    protected final ItemModelBuilder itemModelBuilder;
    protected JsonObject base = null, inventory = null;
    protected float swapSpeed = 1F, swapYOffset = 0F;

    public AnimatedItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
        this.itemModelBuilder = new ItemModelBuilder(location, existingFileHelper);
    }

    public AnimatedItemModelBuilder base(JsonObject json) {
        base = json;
        return this;
    }

    public AnimatedItemModelBuilder inventory(JsonObject json) {
        inventory = json;
        return this;
    }

    public AnimatedItemModelBuilder swapSpeed(float swapSpeed) {
        this.swapSpeed = swapSpeed;
        return this;
    }

    public AnimatedItemModelBuilder swapYOffset(float swapYOffset) {
        this.swapYOffset = swapYOffset;
        return this;
    }

    public AnimatedItemModelBuilder animatedLoader() {
        return customLoader(((itemModelBuilder, existingFileHelper1) -> new CustomLoaderBuilder<AnimatedItemModelBuilder>(
                ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "animated-item"), itemModelBuilder, existingFileHelper1){})).end();
    }

    public ItemModelBuilder itemModelBuilder() {
        return itemModelBuilder;
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if(swapSpeed != 1F) root.addProperty("swapSpeed", swapSpeed);
        if(swapYOffset != 0F) root.addProperty("swapYOffset", swapYOffset);
        if(base != null) root.add("base", base);
        if(inventory != null) root.add("inventory", inventory);
        JsonObject proxyJson = itemModelBuilder.toJson();
        if(proxyJson.has("overrides")) root.add("overrides", proxyJson.getAsJsonArray("overrides"));
        return root;
    }
}
