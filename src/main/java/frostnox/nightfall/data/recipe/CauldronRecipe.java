package frostnox.nightfall.data.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import oshi.util.tuples.Pair;

public class CauldronRecipe extends FlatMixtureRecipe {
    public static final int COOK_TIME = 150 * 20;
    public static final RecipeType<CauldronRecipe> TYPE = RecipeType.register(Nightfall.MODID + ":cauldron");
    public static final Serializer<CauldronRecipe> SERIALIZER = new Serializer<>(CauldronRecipe::new, "cauldron");
    public static final TranslatableComponent UNIT = new TranslatableComponent(Nightfall.MODID + ".cauldron.unit");
    public static final TranslatableComponent UNITS = new TranslatableComponent(Nightfall.MODID + ".cauldron.units");
    public static final String MIN_PHRASE = Nightfall.MODID + ".cauldron.min_phrase";
    public static final String MAX_PHRASE = Nightfall.MODID + ".cauldron.max_phrase";
    public static final String RANGE_PHRASE = Nightfall.MODID + ".cauldron.range_phrase";

    public CauldronRecipe(ResourceLocation id, ResourceLocation requirement, NonNullList<Pair<Ingredient, Vec2>> input, ItemStack itemOutput, FluidStack fluidOutput, int unitsPerOutput, int cookTime) {
        super(id, requirement, input, itemOutput, fluidOutput, unitsPerOutput, cookTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public void render(PoseStack poseStack, Screen screen, int mouseX, int mouseY, float partial, int xOffset, int yOffset) {
        int x = 4;
        int y = 15;
        Font font = Minecraft.getInstance().font;
        if(!itemOutput.isEmpty()) {
            Component outputText = itemOutput.getHoverName();
            if(outputText instanceof MutableComponent mutableText) mutableText.setStyle(outputText.getStyle().applyFormat(ChatFormatting.UNDERLINE));
            font.draw(poseStack, outputText, x, y, RenderUtil.COLOR_BLACK);
        }
        y += 15;
        poseStack.scale(6F/7F, 7F/8F, 1F); //Try to keep scale proportional to actual font size for nicer rendering, unsure if this works for other languages
        for(Pair<Ingredient, Vec2> part : input) {
            Ingredient ingredient = part.getA();
            Vec2 range = part.getB();
            if(range.y == 0) continue;
            Component text;
            boolean multiple;
            if(range.x == 0) {
                text = new TranslatableComponent(MAX_PHRASE, (int) range.y);
                multiple = range.y > 1;
            }
            else if(range.y == Float.MAX_VALUE) {
                text = new TranslatableComponent(MIN_PHRASE, (int) range.x);
                multiple = range.x > 1;
            }
            else if(range.x == range.y) {
                text = new TextComponent((int) range.x + " ");
                multiple = range.x > 1;
            }
            else {
                text = new TranslatableComponent(RANGE_PHRASE, (int) range.x, (int) range.y);
                multiple = true;
            }
            font.draw(poseStack, text, x, y, RenderUtil.COLOR_BLACK);
            int xOff = font.width(text.getVisualOrderText());
            if(multiple) {
                font.draw(poseStack, UNITS, x + xOff, y, RenderUtil.COLOR_BLACK);
                xOff += font.width(UNITS);
            }
            else {
                font.draw(poseStack, UNIT, x + xOff, y, RenderUtil.COLOR_BLACK);
                xOff += font.width(UNIT);
            }
            xOff += font.width(" ");
            Component itemText = null;
            for(int i = 0; i < TagsNF.FOOD_GROUPS.size(); i++) {
                boolean match = true;
                ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(TagsNF.FOOD_GROUPS.get(i));
                for(ItemStack item : ingredient.getItems()) {
                    if(!tag.contains(item.getItem())) {
                        match = false;
                        break;
                    }
                }
                if(match) {
                    itemText = RenderUtil.FOOD_GROUPS_TEXT.get(i);
                    break;
                }
            }
            if(itemText == null) itemText = LevelUtil.chooseUnlockedIngredient(ingredient, ClientEngine.get().getPlayer()).getHoverName();
            font.draw(poseStack, itemText, x + xOff, y, RenderUtil.COLOR_BLACK);
            poseStack.translate(0, 10, 0);
        }
    }

    @Override
    public ItemStack clickItem(Screen screen, int mouseX, int mouseY) {
        return ItemStack.EMPTY;
    }

    @Override
    public TranslatableComponent getTitle() {
        return new TranslatableComponent(Nightfall.MODID + ".cauldron");
    }
}
