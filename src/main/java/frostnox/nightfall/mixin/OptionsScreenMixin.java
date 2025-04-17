package frostnox.nightfall.mixin;

import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    @Unique private static final Object[] DIFFICULTIES = new Object[] {Difficulty.PEACEFUL, Difficulty.NORMAL};

    private OptionsScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @ModifyArg(method = "createDifficultyButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;withValues([Ljava/lang/Object;)Lnet/minecraft/client/gui/components/CycleButton$Builder;"))
    private static Object[] nightfall$adjustDifficultyButton(Object[] values) {
        return DIFFICULTIES;
    }
}
