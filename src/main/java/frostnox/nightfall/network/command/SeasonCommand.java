package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
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

public class SeasonCommand {
    public static final String QUERY = "commands.time.season.query";
    public static final String SET = "commands.time.season.set";

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("season").requires((context) -> {
            return context.hasPermission(2) && LevelData.isPresent(context.getLevel());
        }).then(Commands.literal("set").then(Commands.literal("spring").executes((context) -> {
            return setSeason(context.getSource(), Season.SPRING);
        })).then(Commands.literal("summer").executes((context) -> {
            return setSeason(context.getSource(), Season.SUMMER);
        })).then(Commands.literal("fall").executes((context) -> {
            return setSeason(context.getSource(), Season.FALL);
        })).then(Commands.literal("winter").executes((context) -> {
            return setSeason(context.getSource(), Season.WINTER);
        }))).then(Commands.literal("query").executes((context) -> {
                    return querySeason(context.getSource(), LevelData.get(context.getSource().getLevel()).getSeason());
        })));
    }

    private static int querySeason(CommandSourceStack pSource, Season season) {
        pSource.sendSuccess(new TranslatableComponent(QUERY, season.toTranslatable()), false);
        return 1;
    }

    private static int setSeason(CommandSourceStack pSource, Season season) {
        ServerLevel level = pSource.getLevel();
        ILevelData cap = LevelData.get(level);
        cap.setSeasonTime(season.getMiddleTime());
        Object message = new LevelDataToClient(cap.writeNBTSync(new CompoundTag()));
        for(ServerPlayer player : level.players()) NetworkHandler.toClient(player, message);

        pSource.sendSuccess(new TranslatableComponent(SET, season.toTranslatable()), true);
        return 1;
    }
}
