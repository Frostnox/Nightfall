package frostnox.nightfall.world.generation.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frostnox.nightfall.registry.vanilla.PlacementModifierTypesNF;
import frostnox.nightfall.util.math.OctalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;
import java.util.stream.Stream;

public class AdjacentScanPlacement extends PlacementModifier {
   private final BlockPredicate targetCondition;
   private final boolean avoid;
   public static final Codec<AdjacentScanPlacement> CODEC = RecordCodecBuilder.create((builder) ->
           builder.group(BlockPredicate.CODEC.fieldOf("target_condition").forGetter((placement) -> placement.targetCondition),
                           Codec.BOOL.fieldOf("avoid").forGetter((placement -> placement.avoid)))
                   .apply(builder, AdjacentScanPlacement::new));

   private AdjacentScanPlacement(BlockPredicate targetCondition, boolean avoid) {
      this.targetCondition = targetCondition;
      this.avoid = avoid;
   }

   public static AdjacentScanPlacement of(BlockPredicate pTargetCondition, boolean avoid) {
      return new AdjacentScanPlacement(pTargetCondition, avoid);
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext pContext, Random pRandom, BlockPos pos) {
      BlockPos.MutableBlockPos adjPos = pos.mutable();
      WorldGenLevel level = pContext.getLevel();
       for(OctalDirection dir : OctalDirection.OCTALS) {
          adjPos.setX(pos.getX() + dir.xStepInt);
          adjPos.setZ(pos.getZ() + dir.zStepInt);
          if(targetCondition.test(level, adjPos.setY(pos.getY())) || targetCondition.test(level, adjPos.setY(pos.getY() + 1))) return avoid ? Stream.of() : Stream.of(pos);
       }
       return avoid ? Stream.of(pos) : Stream.of();
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierTypesNF.ADJACENT_SCAN_PLACEMENT;
   }
}