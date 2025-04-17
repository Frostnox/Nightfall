package frostnox.nightfall.world.generation.feature;

import com.mojang.serialization.Codec;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.world.generation.structure.ExplorerRuinsPiece;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class ExplorerRuinsFeature extends StructureFeature<NoneFeatureConfiguration> {
    public ExplorerRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(
                (context) -> StructuresNF.checkSurfaceLocation(context, ExplorerRuinsPiece.SIZE, ExplorerRuinsPiece.SIZE, 3),
                (builder, context) -> builder.addPiece(new ExplorerRuinsPiece(context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ(), Direction.NORTH))));
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
