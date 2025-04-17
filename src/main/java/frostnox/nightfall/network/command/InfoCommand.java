package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import frostnox.nightfall.capability.PlayerData;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class InfoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("nightfall").requires((sourceStack) -> {
            return sourceStack.hasPermission(0);
        }).then(Commands.literal("controls").executes((context) -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            player.sendMessage(new TextComponent("commands.info.server_controls"), Util.NIL_UUID);
            return 1;
        }));

        pDispatcher.register(builder);
    }
}
