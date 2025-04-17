package frostnox.nightfall.world.generation.feature;

import com.mojang.serialization.Codec;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.world.generation.structure.DesertedCampPiece;
import frostnox.nightfall.world.generation.structure.SlayerRuinsPiece;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class SlayerRuinsFeature extends StructureFeature<NoneFeatureConfiguration> {
    public SlayerRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(
                (context) -> StructuresNF.checkSurfaceLocation(context, SlayerRuinsPiece.X_SIZE, SlayerRuinsPiece.Z_SIZE, 3),
                (builder, context) -> builder.addPiece(new SlayerRuinsPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()))));
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
