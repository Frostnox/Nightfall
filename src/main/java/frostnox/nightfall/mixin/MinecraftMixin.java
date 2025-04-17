package frostnox.nightfall.mixin;

import com.mojang.blaze3d.platform.WindowEventHandler;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, net.minecraftforge.client.extensions.IForgeMinecraft {
    @Shadow @Nullable private Screen screen;
    @Shadow @Nullable private LocalPlayer player;
    @Shadow @Final private Gui gui;

    private MinecraftMixin(String pName) {
        super(pName);
    }

    @Unique
    private Music getRandomSeasonMusic() {
        return switch(player.getRandom().nextInt(4)) {
            case 0 -> ClientEngine.get().SPRING_MUSIC;
            case 1 -> ClientEngine.get().SUMMER_MUSIC;
            case 2 -> ClientEngine.get().FALL_MUSIC;
            default -> ClientEngine.get().WINTER_MUSIC;
        };
    }

    /**
     * @author Frostnox
     * @reason Custom music selection logic & arrangement
     */
    @Overwrite
    public Music getSituationalMusic() {
        if(screen instanceof WinScreen) return Musics.CREDITS;
        else if(player == null) return ClientEngine.get().MENU_MUSIC;
        else {
            if(player.level.dimension() == Level.END) return gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
            else {
                Holder<Biome> biome = player.level.getBiome(player.blockPosition());
                Biome.BiomeCategory category = Biome.getBiomeCategory(biome);
                if(category == Biome.BiomeCategory.UNDERGROUND) return biome.value().getBackgroundMusic().orElse(getRandomSeasonMusic());
                else if(LevelUtil.isDay(player.level)) {
                    if(player.level.getRawBrightness(player.eyeBlockPosition(), 0) > 0) {
                        if(category == Biome.BiomeCategory.OCEAN || !LevelData.isPresent(player.level)) {
                            return biome.value().getBackgroundMusic().orElse(getRandomSeasonMusic());
                        }
                        else return switch(Season.get(player.level)) {
                            case SPRING -> ClientEngine.get().SPRING_MUSIC;
                            case SUMMER -> ClientEngine.get().SUMMER_MUSIC;
                            case FALL -> ClientEngine.get().FALL_MUSIC;
                            case WINTER -> ClientEngine.get().WINTER_MUSIC;
                        };
                    }
                }
            }
        }
        return ClientEngine.get().NO_MUSIC;
    }
}
