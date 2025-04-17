package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.LevelDataToClient;
import frostnox.nightfall.world.Weather;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.text.DecimalFormat;

public class WeatherCommandNF {
    public static final String QUERY = "commands.time.weather.query";
    public static final String SET = "commands.time.weather.set";
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("weather").requires((context) -> {
                    return context.hasPermission(2) && LevelData.isPresent(context.getLevel());
                }).then(Commands.argument("threshold", FloatArgumentType.floatArg(-1F, 1F)).executes((context) -> {
                    return setWeather(context.getSource(), FloatArgumentType.getFloat(context, "threshold"));
                })).then(Commands.literal("clear").executes((context) -> {
                    return setWeather(context.getSource(), -0.2F);
                })).then(Commands.literal("clouds").executes((context) -> {
                    return setWeather(context.getSource(), 0.2F);
                })).then(Commands.literal("precipitation").executes((context) -> {
                    return setWeather(context.getSource(), 0.6F);
                })).then(Commands.literal("fog").executes((context) -> {
                    return setWeather(context.getSource(), -0.6F);
                })).then(Commands.literal("query").executes((context) -> {
                    return queryWeather(context.getSource());
                }))
                .then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((context) -> {
                    return addWeather(context.getSource(), IntegerArgumentType.getInteger(context, "amount"));
                })))
        );
    }

    private static int queryWeather(CommandSourceStack source) throws CommandSyntaxException {
        Level level = source.getLevel();
        ILevelData capL = LevelData.get(level);
        Weather weather;
        if(source.getEntity() != null) {
            BlockPos pos = source.getEntityOrException().eyeBlockPosition();
            weather = capL.getWeather(ChunkData.get(level.getChunkAt(pos)), pos);
        }
        else weather = capL.getGlobalWeather();
        source.sendSuccess(new TranslatableComponent(QUERY, weather.toTranslatable(),
                FORMAT.format(capL.getGlobalWeatherIntensity()), FORMAT.format(capL.getLastWeatherIntensity()),
                FORMAT.format(capL.getTargetWeatherIntensity())), false);
        return 1;
    }

    private static int addWeather(CommandSourceStack source, int amount) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        ILevelData capL = LevelData.get(level);
        capL.setGlobalWeatherIntensity(Mth.clamp(capL.getGlobalWeatherIntensity() + amount, -1F, 1F));

        Object message = new LevelDataToClient(capL.writeNBTSync(new CompoundTag()));
        for(ServerPlayer player : level.players()) NetworkHandler.toClient(player, message);
        Weather weather;
        if(source.getEntity() != null) {
            BlockPos pos = source.getEntityOrException().eyeBlockPosition();
            weather = capL.getWeather(ChunkData.get(level.getChunkAt(pos)), pos);
        }
        else weather = capL.getGlobalWeather();
        source.sendSuccess(new TranslatableComponent(SET, weather.toTranslatable(), capL.getGlobalWeatherIntensity()), true);
        return 1;
    }

    private static int setWeather(CommandSourceStack source, float threshold) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        ILevelData capL = LevelData.get(level);
        capL.setGlobalWeatherIntensity(threshold);

        Object message = new LevelDataToClient(capL.writeNBTSync(new CompoundTag()));
        for(ServerPlayer player : level.players()) NetworkHandler.toClient(player, message);
        Weather weather;
        if(source.getEntity() != null) {
            BlockPos pos = source.getEntityOrException().eyeBlockPosition();
            weather = capL.getWeather(ChunkData.get(level.getChunkAt(pos)), pos);
        }
        else weather = capL.getGlobalWeather();
        source.sendSuccess(new TranslatableComponent(SET, weather.toTranslatable(), threshold), true);
        return 1;
    }
}
