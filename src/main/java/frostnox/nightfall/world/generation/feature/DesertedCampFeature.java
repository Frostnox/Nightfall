package frostnox.nightfall.world.generation.feature;

import com.mojang.serialization.Codec;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.world.generation.structure.DesertedCampPiece;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class DesertedCampFeature extends StructureFeature<NoneFeatureConfiguration> {
    public DesertedCampFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(
                (context) -> StructuresNF.checkSurfaceLocation(context, DesertedCampPiece.SIZE, DesertedCampPiece.SIZE, 2),
                (builder, context) -> builder.addPiece(new DesertedCampPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()))));
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
