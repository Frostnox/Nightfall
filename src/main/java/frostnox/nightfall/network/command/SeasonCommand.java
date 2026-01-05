package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.LevelDataToClient;
import frostnox.nightfall.world.Season;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.text.DecimalFormat;

public class SeasonCommand {
    public static final String QUERY = "commands.time.season.query";
    public static final String SET = "commands.time.season.set";
    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("season").requires((context) -> {
            return context.hasPermission(2) && LevelData.isPresent(context.getLevel());
        }).then(Commands.literal("set").then(Commands.literal("spring").executes((context) -> {
            return setSeason(context.getSource(), Season.SPRING.getMiddleTime());
        })).then(Commands.literal("summer").executes((context) -> {
            return setSeason(context.getSource(), Season.SUMMER.getMiddleTime());
        })).then(Commands.literal("fall").executes((context) -> {
            return setSeason(context.getSource(), Season.FALL.getMiddleTime());
        })).then(Commands.literal("winter").executes((context) -> {
            return setSeason(context.getSource(), Season.WINTER.getMiddleTime());
        })).then(Commands.argument("time", DoubleArgumentType.doubleArg(0, 1)).executes((context) -> {
            return setSeason(context.getSource(), (long) (DoubleArgumentType.getDouble(context, "time") * Season.YEAR_LENGTH));
        }))).then(Commands.literal("query").executes((context) -> {
                    return querySeason(context.getSource(), LevelData.get(context.getSource().getLevel()).getSeasonTime());
        })));
    }

    private static int querySeason(CommandSourceStack pSource, long seasonTime) {
        pSource.sendSuccess(new TranslatableComponent(QUERY, Season.get(seasonTime).toTranslatable(), FORMAT.format(Season.getNormalizedProgress(seasonTime))), false);
        return 1;
    }

    private static int setSeason(CommandSourceStack pSource, long seasonTime) {
        ServerLevel level = pSource.getLevel();
        ILevelData cap = LevelData.get(level);
        cap.setSeasonTime(seasonTime);
        Object message = new LevelDataToClient(cap.writeNBTSync(new CompoundTag()));
        for(ServerPlayer player : level.players()) NetworkHandler.toClient(player, message);
        pSource.sendSuccess(new TranslatableComponent(SET, Season.get(seasonTime).toTranslatable(), FORMAT.format(Season.getNormalizedProgress(seasonTime))), true);
        return 1;
    }
}
