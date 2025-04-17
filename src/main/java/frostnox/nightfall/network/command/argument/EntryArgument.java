package frostnox.nightfall.network.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public class EntryArgument implements ArgumentType<ResourceLocation> {
   private static final Collection<String> EXAMPLES = List.of("nightfall:basic_tools");
   public static final String INVALID = "nightfall.command.entry.not_found";
   private static final DynamicCommandExceptionType ERROR = new DynamicCommandExceptionType((name)
           -> new TranslatableComponent(INVALID, name));

   public static EntryArgument id() {
      return new EntryArgument();
   }

   public static ResourceLocation getEntry(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      return verify(pContext.getArgument(pName, ResourceLocation.class));
   }

   private static ResourceLocation verify(ResourceLocation id) throws CommandSyntaxException {
      if(!RegistriesNF.getEntries().containsKey(id)) throw ERROR.create(id);
      else return id;
   }

   @Override
   public ResourceLocation parse(StringReader pReader) throws CommandSyntaxException {
      return ResourceLocation.read(pReader);
   }

   @Override
   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}