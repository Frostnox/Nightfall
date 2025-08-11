package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;
import java.util.stream.Stream;

public class BelowBlockPlacement extends PlacementModifier {
   private final BlockPredicate targetCondition;
   private final boolean avoid;
   public static final Codec<BelowBlockPlacement> CODEC = RecordCodecBuilder.create((builder) ->
           builder.group(BlockPredicate.CODEC.fieldOf("target_condition").forGetter((placement) -> placement.targetCondition),
                           Codec.BOOL.fieldOf("avoid").forGetter((placement -> placement.avoid)))
                   .apply(builder, BelowBlockPlacement::new));

   private BelowBlockPlacement(BlockPredicate targetCondition, boolean avoid) {
      this.targetCondition = targetCondition;
      this.avoid = avoid;
   }

   public static BelowBlockPlacement of(BlockPredicate pTargetCondition, boolean avoid) {
      return new BelowBlockPlacement(pTargetCondition, avoid);
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext pContext, Random pRandom, BlockPos pos) {
      if(targetCondition.test(pContext.getLevel(), pos.below())) return avoid ? Stream.of() : Stream.of(pos);
      else return avoid ? Stream.of(pos) : Stream.of();
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierTypesNF.BELOW_BLOCK_PLACEMENT;
   }
}