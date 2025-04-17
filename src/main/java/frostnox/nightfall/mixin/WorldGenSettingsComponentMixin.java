package frostnox.nightfall.mixin;

import frostnox.nightfall.Nightfall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(WorldGenSettingsComponent.class)
public abstract class WorldGenSettingsComponentMixin implements Widget {
    @Shadow private CycleButton<Boolean> bonusItemsButton;
    @Shadow private CycleButton<WorldPreset> typeButton;
    @Shadow private Optional<WorldPreset> preset;
    @Shadow private RegistryAccess.Frozen registryHolder;
    @Shadow private WorldGenSettings settings;

    /**
     * Remove bonus chest option since Nightfall's world generators don't use it
     */
    @Inject(method = "setVisibility", at = @At("TAIL"))
    private void nightfall$setVisibility(boolean pVisible, CallbackInfo callbackInfo) {
        bonusItemsButton.visible = false;

    }

    /**
     * Adjust world preset if it was left at vanilla value
     */
    @Inject(method = "init", at = @At(("TAIL")))
    private void nightfall$adjustDefaultWorldPreset(CreateWorldScreen pCreateWorldScreen, Minecraft pMinecraft, Font pFont, CallbackInfo callbackInfo) {
        Optional<WorldPreset> defaultPreset = ForgeHooksClient.getDefaultWorldPreset();
        if(defaultPreset.isPresent() && defaultPreset.get().equals(WorldPreset.NORMAL)) {
            for(WorldPreset preset : WorldPreset.PRESETS) {
                if(((TranslatableComponent) preset.description()).getKey().equals("generator." + Nightfall.MODID + ".continental")) {
                    typeButton.setValue(preset);
                    this.preset = Optional.of(preset);
                    this.settings = preset.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
                    break;
                }
            }
        }
    }
}
