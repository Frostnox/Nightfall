package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.world.ContinentalWorldType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

//Overwrites the original TimeCommand functionality to support variable day lengths
public class TimeCommandNF {
    private static final float CONVERSION_RATIO = ContinentalWorldType.DAY_LENGTH / 24000F;

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("time").requires((context) -> {
            return context.hasPermission(2); //Requirements don't seem to apply if command was already registered
        }).then(Commands.literal("set").then(Commands.literal("day").executes((context) -> {
            return setTime(context.getSource(), Math.round(1000 * getConversionRatio(context.getSource().getLevel())));
        })).then(Commands.literal("noon").executes((context) -> {
            return setTime(context.getSource(), Math.round(6000 * getConversionRatio(context.getSource().getLevel())));
        })).then(Commands.literal("night").executes((context) -> {
            return setTime(context.getSource(), Math.round(13000 * getConversionRatio(context.getSource().getLevel())));
        })).then(Commands.literal("midnight").executes((context) -> {
            return setTime(context.getSource(), Math.round(18000 * getConversionRatio(context.getSource().getLevel())));
        }))).then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time()).executes((context) -> {
            int arg = IntegerArgumentType.getInteger(context, "time");
            if(arg % 24000 == 0) arg *= getConversionRatio(context.getSource().getLevel()); //Convert if argument was in days
            return setTime(context.getSource(), context.getSource().getLevel().getDayTime() + arg);
        }))).then(Commands.literal("query").then(Commands.literal("daytime").executes((context) -> {
            return queryTime(context.getSource(), getDayTime(context.getSource().getLevel()));
        })).then(Commands.literal("day").executes((context) -> {
            return queryTime(context.getSource(), (int)(context.getSource().getLevel().getDayTime() / 24000L * getConversionRatio(context.getSource().getLevel()) % 2147483647L));
        }))));
    }

    private static float getConversionRatio(Level level) {
        return LevelData.isPresent(level) ? CONVERSION_RATIO : 1F;
    }

    /**
     * Returns the day time (time wrapped within a day)
     */
    private static int getDayTime(ServerLevel level) {
        return (int) (level.getDayTime() % 24000L * CONVERSION_RATIO);
    }

    private static int queryTime(CommandSourceStack pSource, int pTime) {
        pSource.sendSuccess(new TranslatableComponent("commands.time.query", pTime), false);
        return pTime;
    }

    private static int setTime(CommandSourceStack pSource, long pTime) {
        pSource.getLevel().setDayTime(pTime);
        pSource.sendSuccess(new TranslatableComponent("commands.time.set", pTime), true);
        return 1;
    }
}
