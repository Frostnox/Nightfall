package frostnox.nightfall.world.generation.feature;

import com.mojang.serialization.Codec;
import frostnox.nightfall.world.generation.structure.RuinsSurfacePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class RuinsSurfaceFeature extends StructureFeature<NoneFeatureConfiguration> {
    public RuinsSurfaceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, PieceGeneratorSupplier.simple(RuinsSurfaceFeature::checkLocation, RuinsSurfaceFeature::addPieces));
    }
    
    private static <C extends FeatureConfiguration> boolean checkLocation(PieceGeneratorSupplier.Context<C> context) {
        if(!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) return false;
        else return context.getLowestY(12, 15) >= context.chunkGenerator().getSeaLevel();
    }

    private static void addPieces(StructurePiecesBuilder builder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        BlockPos pos = new BlockPos(x, context.chunkGenerator().getFirstFreeHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor()), z);
        Rotation rotation = Rotation.getRandom(context.random());
        builder.addPiece(new RuinsSurfacePiece(context.structureManager(), RuinsSurfacePiece.RUINS_0, pos, rotation));
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
