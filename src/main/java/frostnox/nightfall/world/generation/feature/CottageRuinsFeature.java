package frostnox.nightfall.world.generation.feature;

import com.mojang.serialization.Codec;
import frostnox.nightfall.registry.forge.StructuresNF;
import frostnox.nightfall.world.generation.structure.CottageRuinsPiece;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class CottageRuinsFeature extends StructureFeature<NoneFeatureConfiguration> {
    public CottageRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(
                (context) -> StructuresNF.checkSurfaceLocation(context, CottageRuinsPiece.X_SIZE, CottageRuinsPiece.Z_SIZE, 4),
                (builder, context) -> builder.addPiece(new CottageRuinsPiece(context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()))));
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
