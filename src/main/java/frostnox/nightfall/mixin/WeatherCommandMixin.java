package frostnox.nightfall.mixin;

import net.minecraft.server.commands.WeatherCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WeatherCommand.class)
public abstract class WeatherCommandMixin {
    /**
     * Stop vanilla weather command from conflicting with Nightfall's weather command
     */
    @ModifyConstant(method = "register", constant = @Constant(stringValue = "weather"))
    private static String nightfall$cancelWeatherCommand(String string) {
        return "weathervanilla";
    }
}
