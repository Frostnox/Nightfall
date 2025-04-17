package frostnox.nightfall.client.gui.screen.encyclopedia;

import frostnox.nightfall.encyclopedia.Entry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class EntryClient {
    public record Image(int width, int height, ResourceLocation texture) {}
    public final EncyclopediaCategory category;
    public final RegistryObject<? extends Entry> entry;
    public final ItemStack itemIcon;
    public final @Nullable ResourceLocation icon; //Icon to use if item icon is empty
    public final int x, y;
    public final @Nullable Image puzzleImage, completedImage;
    public final List<RegistryObject<? extends Entry>> addenda;
    public final boolean separated;

    public EntryClient(EncyclopediaCategory category, RegistryObject<? extends Entry> entry, int x, int y, ItemStack itemIcon, ResourceLocation icon, Image puzzleImage, Image completedImage, List<RegistryObject<? extends Entry>> addenda, boolean separated) {
        this.category = category;
        this.entry = entry;
        this.x = x;
        this.y = y;
        this.itemIcon = itemIcon;
        this.icon = icon;
        this.puzzleImage = puzzleImage;
        this.completedImage = completedImage;
        this.addenda = List.copyOf(addenda);
        this.separated = separated;
    }

    public boolean containsEntry(ResourceLocation entryId) {
        return entry.getId().equals(entryId) || addenda.stream().anyMatch((a) -> a.getId().equals(entryId));
    }
}
