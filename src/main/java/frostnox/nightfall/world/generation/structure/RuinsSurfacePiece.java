package frostnox.nightfall.world.generation.structure;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.registry.forge.StructuresNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Random;

public class RuinsSurfacePiece extends TemplateStructurePiece {
    public static final ResourceLocation RUINS_0 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "ruins_surface/ruins_surface_0");
    private static final BlockPos PIVOT = new BlockPos(3, 5, 5);

    public RuinsSurfacePiece(StructureManager manager, ResourceLocation location, BlockPos pos, Rotation rotation) {
        super(null, 0, manager, location, location.toString(), makeSettings(rotation), pos);
    }

    public RuinsSurfacePiece(StructureManager manager, CompoundTag tag) {
        super(null, tag, manager, (location) -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
    }

    private static StructurePlaceSettings makeSettings(Rotation pRotation) {
        return (new StructurePlaceSettings()).setRotation(pRotation).setMirror(Mirror.NONE).setRotationPivot(PIVOT).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag pTag) {
        super.addAdditionalSaveData(context, pTag);
        pTag.putString("Rot", this.placeSettings.getRotation().name());
    }

    @Override
    protected void handleDataMarker(String pMarker, BlockPos pos, ServerLevelAccessor level, Random random, BoundingBox pBox) {

    }
}
