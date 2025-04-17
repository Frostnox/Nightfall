package frostnox.nightfall.network.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.network.command.argument.KnowledgeArgument;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class KnowledgeCommand {
    private enum Action {
        FORGET, FORGET_ALL, LEARN, LEARN_ALL
    }
    public static final String FORGET_SELF = "nightfall.command.knowledge.forget.self";
    public static final String FORGET = "nightfall.command.knowledge.forget";
    public static final String FORGET_OTHER = "nightfall.command.knowledge.forget.other";
    public static final TranslatableComponent FORGET_ALL_SELF = new TranslatableComponent("nightfall.command.knowledge.forget_all.self");
    public static final TranslatableComponent FORGET_ALL = new TranslatableComponent("nightfall.command.knowledge.forget_all");
    public static final String FORGET_ALL_OTHER = "nightfall.command.knowledge.forget_all.other";
    public static final String LEARN_SELF = "nightfall.command.knowledge.learn.self";
    public static final String LEARN = "nightfall.command.knowledge.learn";
    public static final String LEARN_OTHER = "nightfall.command.knowledge.learn.other";
    public static final TranslatableComponent LEARN_ALL_SELF = new TranslatableComponent("nightfall.command.knowledge.learn_all.self");
    public static final TranslatableComponent LEARN_ALL = new TranslatableComponent("nightfall.command.knowledge.learn_all");
    public static final String LEARN_ALL_OTHER = "nightfall.command.knowledge.learn_all.other";

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("knowledge").requires((sourceStack) -> {
            return sourceStack.hasPermission(2);
        }).then(Commands.literal("forget").then(executeAll(Commands.literal("all"), Action.FORGET_ALL))
                .then(execute(Commands.argument("knowledge", KnowledgeArgument.id()).suggests(KnowledgeCommand::suggest), Action.FORGET)))
          .then(Commands.literal("learn").then(executeAll(Commands.literal("all"), Action.LEARN_ALL))
                .then(execute(Commands.argument("knowledge", KnowledgeArgument.id()).suggests(KnowledgeCommand::suggest), Action.LEARN)));
        pDispatcher.register(builder);
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(RegistriesNF.getActiveServerKnowledge().stream().map((knowledge -> knowledge.getRegistryName())), builder);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> execute(ArgumentBuilder<CommandSourceStack, ?> builder, Action action) {
        return builder.executes((context) -> {
            return handle(context, KnowledgeArgument.getKnowledge(context, "knowledge"), action, Collections.singleton(context.getSource().getPlayerOrException()));
        }).then(Commands.argument("target", EntityArgument.players()).executes((context) -> {
            return handle(context, KnowledgeArgument.getKnowledge(context, "knowledge"), action, EntityArgument.getPlayers(context, "target"));
        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> executeAll(ArgumentBuilder<CommandSourceStack, ?> builder, Action action) {
        return builder.executes((context) -> {
            return handle(context, null, action, Collections.singleton(context.getSource().getPlayerOrException()));
        }).then(Commands.argument("target", EntityArgument.players()).executes((context) -> {
            return handle(context, null, action, EntityArgument.getPlayers(context, "target"));
        }));
    }

    private static int handle(CommandContext<CommandSourceStack> context, @Nullable ResourceLocation knowledgeId, Action action, Collection<ServerPlayer> players) {
        for(ServerPlayer player : players) {
            IPlayerData capP = PlayerData.get(player);
            Knowledge knowledge = KnowledgeNF.get(knowledgeId);
            switch(action) {
                case FORGET -> capP.removeKnowledge(knowledgeId);
                case FORGET_ALL -> {
                    for(Knowledge k : RegistriesNF.getActiveServerKnowledge()) {
                        capP.removeKnowledge(k.getRegistryName());
                    }
                }
                case LEARN -> capP.addKnowledge(knowledgeId);
                case LEARN_ALL -> {
                    for(Knowledge k : RegistriesNF.getActiveServerKnowledge()) {
                        if(!capP.hasKnowledge(k.getRegistryName())) capP.addKnowledge(k.getRegistryName());
                    }
                }
            }
            CommandSourceStack pSource = context.getSource();
            if(pSource.getEntity() == player) {
                TranslatableComponent component = switch(action) {
                    case FORGET -> new TranslatableComponent(FORGET_SELF, new TranslatableComponent(knowledge.getDescriptionId()));
                    case FORGET_ALL -> FORGET_ALL_SELF;
                    case LEARN -> new TranslatableComponent(LEARN_SELF, new TranslatableComponent(knowledge.getDescriptionId()));
                    case LEARN_ALL -> LEARN_ALL_SELF;
                };
                pSource.sendSuccess(component, true);
            }
            else {
                if(pSource.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                    TranslatableComponent component = switch(action) {
                        case FORGET -> new TranslatableComponent(FORGET, new TranslatableComponent(knowledge.getDescriptionId()));
                        case FORGET_ALL -> FORGET_ALL;
                        case LEARN -> new TranslatableComponent(LEARN, new TranslatableComponent(knowledge.getDescriptionId()));
                        case LEARN_ALL -> LEARN_ALL;
                    };
                    player.sendMessage(component, Util.NIL_UUID);
                }
                String text = switch(action) {
                    case FORGET -> FORGET_OTHER;
                    case FORGET_ALL -> FORGET_ALL_OTHER;
                    case LEARN -> LEARN_OTHER;
                    case LEARN_ALL -> LEARN_ALL_OTHER;
                };
                if(knowledgeId == null) pSource.sendSuccess(new TranslatableComponent(text, player.getDisplayName()), true);
                else pSource.sendSuccess(new TranslatableComponent(text, player.getDisplayName(), new TranslatableComponent(knowledge.getDescriptionId())), true);
            }
        }
        return players.size();
    }
}
