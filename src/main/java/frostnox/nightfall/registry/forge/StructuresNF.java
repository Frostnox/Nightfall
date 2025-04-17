package frostnox.nightfall.registry.forge;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.world.generation.feature.*;
import frostnox.nightfall.world.generation.structure.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StructuresNF {
    public static final DeferredRegister<StructureFeature<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Nightfall.MODID);
    public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> COTTAGE_RUINS = STRUCTURES.register("cottage_ruins", () ->
            new CottageRuinsFeature(NoneFeatureConfiguration.CODEC));
    public static StructurePieceType COTTAGE_RUINS_PIECE;
    public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> SLAYER_RUINS = STRUCTURES.register("slayer_ruins", () ->
            new SlayerRuinsFeature(NoneFeatureConfiguration.CODEC));
    public static StructurePieceType SLAYER_RUINS_PIECE;
    public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> EXPLORER_RUINS = STRUCTURES.register("explorer_ruins", () ->
            new ExplorerRuinsFeature(NoneFeatureConfiguration.CODEC));
    public static StructurePieceType EXPLORER_RUINS_PIECE;
    public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> DESERTED_CAMP = STRUCTURES.register("deserted_camp", () ->
            new DesertedCampFeature(NoneFeatureConfiguration.CODEC));
    public static StructurePieceType DESERTED_CAMP_PIECE;

    public static void register() {
        STRUCTURES.register(Nightfall.MOD_EVENT_BUS);
    }

    public static void registerEvent() {
        COTTAGE_RUINS_PIECE = createPiece(CottageRuinsPiece::new, COTTAGE_RUINS.getId());
        SLAYER_RUINS_PIECE = createPiece(SlayerRuinsPiece::new, SLAYER_RUINS.getId());
        EXPLORER_RUINS_PIECE = createPiece(ExplorerRuinsPiece::new, EXPLORER_RUINS.getId());
        DESERTED_CAMP_PIECE = createPiece(DesertedCampPiece::new, DESERTED_CAMP.getId());
    }

    public static <C extends FeatureConfiguration> boolean checkSurfaceLocation(PieceGeneratorSupplier.Context<C> context, int xSize, int zSize, int maxHeightDiff) {
        if(!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) return false;
        else {
            int[] corners = context.getCornerHeights(context.chunkPos().getMinBlockX(), xSize, context.chunkPos().getMinBlockZ(), zSize);
            int min = Math.min(Math.min(corners[0], corners[1]), Math.min(corners[2], corners[3]));
            if(min <= context.chunkGenerator().getSeaLevel()) return false;
            int max = Math.max(Math.max(corners[0], corners[1]), Math.max(corners[2], corners[3]));
            return max - min < maxHeightDiff;
        }
    }

    private static StructurePieceType createFullContextPiece(StructurePieceType type, ResourceLocation location) {
        return Registry.register(Registry.STRUCTURE_PIECE, location, type);
    }

    private static StructurePieceType createPiece(StructurePieceType.ContextlessType type, ResourceLocation location) {
        return createFullContextPiece(type, location);
    }

    private static StructurePieceType createTemplatePiece(StructurePieceType.StructureTemplateType type, ResourceLocation location) {
        return createFullContextPiece(type, location);
    }
}
