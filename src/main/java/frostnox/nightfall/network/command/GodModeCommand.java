package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.Collection;
import java.util.Collections;

public class GodModeCommand {
    public static final TranslatableComponent ENABLE = new TranslatableComponent("commands.godmode.enable");
    public static final TranslatableComponent DISABLE = new TranslatableComponent("commands.godmode.disable");
    public static final String ENABLE_OTHER = "commands.godmode.enable.other";
    public static final String DISABLE_OTHER = "commands.godmode.disable.other";

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("godmode").requires((sourceStack) -> {
            return sourceStack.hasPermission(2);
        }).executes((context) -> {
            return setGodMode(context, Collections.singleton(context.getSource().getPlayerOrException()));
        }).then(Commands.argument("target", EntityArgument.players()).executes((context) -> {
            return setGodMode(context, EntityArgument.getPlayers(context, "target"));
        }));

        pDispatcher.register(builder);
    }

    private static int setGodMode(CommandContext<CommandSourceStack> pSource, Collection<ServerPlayer> players) {
        int count = 0;
        for(ServerPlayer player : players) {
            if(!player.isAlive()) continue;
            logChange(pSource.getSource(), player, PlayerData.get(player).toggleGodMode());
            count++;
        }
        return count;
    }

    private static void logChange(CommandSourceStack pSource, ServerPlayer player, boolean enabled) {
        if(pSource.getEntity() == player) {
            pSource.sendSuccess(enabled ? ENABLE : DISABLE, true);
        }
        else {
            if(pSource.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.sendMessage(enabled ? ENABLE : DISABLE, Util.NIL_UUID);
            }
            pSource.sendSuccess(new TranslatableComponent(enabled ? ENABLE_OTHER : DISABLE_OTHER, player.getDisplayName()), true);
        }
    }
}
