package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.Collection;
import java.util.Collections;

public class ReselectAttributesCommand {
    public static final TranslatableComponent RESELECT = new TranslatableComponent("commands.reselect_attributes.enable");
    public static final String RESELECT_OTHER = "commands.reselect_attributes.enable.other";

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("reselectattributes").requires((sourceStack) -> {
            return sourceStack.hasPermission(2);
        }).executes((context) -> {
            return reselect(context, Collections.singleton(context.getSource().getPlayerOrException()));
        }).then(Commands.argument("target", EntityArgument.players()).executes((context) -> {
            return reselect(context, EntityArgument.getPlayers(context, "target"));
        }));

        pDispatcher.register(builder);
    }

    private static int reselect(CommandContext<CommandSourceStack> pSource, Collection<ServerPlayer> players) {
        int count = 0;
        for(ServerPlayer player : players) {
            if(!player.isAlive()) continue;
            IPlayerData capP = PlayerData.get(player);
            capP.setNeedsAttributeSelection(true);
            NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.OPEN_ATTRIBUTE_SELECTION_SCREEN_CLIENT, player.getId()));
            logChange(pSource.getSource(), player);
            count++;
        }
        return count;
    }

    private static void logChange(CommandSourceStack pSource, ServerPlayer player) {
        if(pSource.getEntity() == player) {
            pSource.sendSuccess(RESELECT, true);
        }
        else {
            if(pSource.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.sendMessage(RESELECT, Util.NIL_UUID);
            }
            pSource.sendSuccess(new TranslatableComponent(RESELECT_OTHER, player.getDisplayName()), true);
        }
    }
}
