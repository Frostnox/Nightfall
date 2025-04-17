package frostnox.nightfall.client.gui.screen.encyclopedia;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public record EncyclopediaCategory(String name, ResourceLocation icon, ResourceLocation background, ResourceLocation unlockEntryId,
                                   RegistryObject<SoundEvent> experimentFailSound, RegistryObject<SoundEvent> experimentSuccessSound) {
}
