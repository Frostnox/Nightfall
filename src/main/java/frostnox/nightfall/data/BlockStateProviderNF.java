package frostnox.nightfall.data;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.*;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.block.block.barrel.BarrelBlockNF;
import frostnox.nightfall.block.block.campfire.CampfireBlockNF;
import frostnox.nightfall.block.block.cauldron.CauldronBlockNF;
import frostnox.nightfall.block.block.cauldron.Task;
import frostnox.nightfall.block.block.ChairBlock;
import frostnox.nightfall.block.block.eggnest.EggNestBlock;
import frostnox.nightfall.block.block.fireable.FireablePartialBlock;
import frostnox.nightfall.block.block.nest.OverlayBurrowBlock;
import frostnox.nightfall.block.block.pile.PileBlock;
import frostnox.nightfall.block.block.tree.*;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.registry.forge.BlocksNF;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class BlockStateProviderNF extends BlockStateProvider {
    protected static final ExistingFileHelper.ResourceType TEXTURE = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
    protected final String externalPath;
    protected final DataGenerator generator;
    protected final ExistingFileHelper fileHelper;

    public BlockStateProviderNF(DataGenerator generator, String id, ExistingFileHelper helper) {
        super(generator, id, helper);
        this.generator = generator;
        String outputString = generator.getOutputFolder().toString();
        this.externalPath = outputString.substring(0, outputString.lastIndexOf("\\src\\")) + "\\src\\main\\resources\\assets\\" + id + "\\models\\";
        this.fileHelper = helper;
    }

    protected Path getExternalImagePath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(externalPath + loc.getPath() + ".json");
    }

    protected ResourceLocation extendWithFolder(ResourceLocation location) {
        if(location.getPath().contains("/")) return location;
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + location.getPath());
    }

    protected ModelFile file(Block block) {
        return new ModelFile.ExistingModelFile(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + block.getRegistryName().getPath()), models().existingFileHelper);
    }

    protected ModelFile file(Block block, String suffix) {
        return new ModelFile.ExistingModelFile(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + block.getRegistryName().getPath() + suffix), models().existingFileHelper);
    }

    protected String name(Block block) {
        return block.getRegistryName().getPath();
    }

    protected ResourceLocation resource(String name) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + name);
    }

    protected ResourceLocation resource(Block block) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + block.getRegistryName().getPath());
    }

    protected ResourceLocation resource(Block block, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + block.getRegistryName().getPath() + suffix);
    }

    protected ResourceLocation resource(String prefix, Block block, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "block/" + prefix + block.getRegistryName().getPath() + suffix);
    }

    public void fileBlock(Block block) {
        simpleBlock(block, file(block));
    }

    public void emptyBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().getBuilder(name(block))).build());
    }

    public void particleOnlyBlock(Block block, ResourceLocation particleLocation) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().getBuilder(name(block))
                .texture("particle", particleLocation)).build());
    }

    public ModelFile templateModel(Block block, ResourceLocation template, Pair<String, ResourceLocation>... textures) {
        return templateModel(name(block), template, textures);
    }

    public ModelFile templateModel(String name, ResourceLocation template, Pair<String, ResourceLocation>... textures) {
        var model = models().withExistingParent(name, template);
        for(Pair<String, ResourceLocation> texture : textures) model.texture(texture.left(), texture.right());
        return model;
    }

    public void templateBlock(Block block, ResourceLocation template, Pair<String, ResourceLocation>... textures) {
        var model = templateModel(block, template, textures);
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(model).build());
    }

    public void cubeTopBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(cubeTop(block)).build());
    }

    public ModelFile cubeTop(Block block) {
        return models().cubeTop(name(block), resource(block), resource(block, "_top"));
    }

    public ModelFile cubeColumn(Block block) {
        return models().cubeColumn(name(block), resource(block), resource(block, "_top"));
    }

    public ModelFile cubeMirroredAll(Block block) {
        return models().withExistingParent(name(block) + "_mirrored", "cube_mirrored_all").texture("all", resource(block));
    }

    public ModelFile cubeMirroredColumn(Block block) {
        return models().withExistingParent(name(block) + "_mirrored", "cube_column_mirrored").texture("side", resource(block)).texture("end", resource(block, "_top"));
    }

    public void wallTorchBlock(Block block, Block torch) {
        getVariantBuilder(block)
                .forAllStatesExcept(state -> ConfiguredModel.builder()
                                .modelFile(models().withExistingParent(name(block), mcLoc("template_torch_wall")).texture("torch", resource(torch)))
                                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 90) % 360)
                                .build()
                        , BlockStatePropertiesNF.WATER_LEVEL, BlockStatePropertiesNF.WATERLOG_TYPE);
    }

    public void lanternBlock(Block block) {
        getVariantBuilder(block).partialState().with(LanternBlock.HANGING, false).addModels(ConfiguredModel.builder().modelFile(models()
                        .withExistingParent(name(block), mcLoc("template_lantern"))
                        .texture("lantern", resource(block))).build())
                .partialState().with(LanternBlock.HANGING, true).addModels(ConfiguredModel.builder().modelFile(models()
                        .withExistingParent(name(block) + "_hanging", mcLoc("template_hanging_lantern"))
                        .texture("lantern", resource(block))).build());
    }

    public ModelFile cubeTopHorizontalRotated(String name, ResourceLocation side, ResourceLocation top) {
        return models().withExistingParent(name, resource("cube_top_horizontal_rotated")).texture("side", side).texture("top", top);
    }

    public ModelFile cubeTopHorizontal(String name, ResourceLocation side, ResourceLocation top) {
        return models().withExistingParent(name, resource("cube_top_horizontal")).texture("side", side).texture("top", top);
    }

    public void woodBlock(RotatedPillarBlock block, ResourceLocation texture) {
        axisBlock(block, texture, texture);
    }

    public void stemBlock(TreeStemBlock block, RotatedPillarBlock texBlock) {
        for(boolean charred : TreeStemBlock.CHARRED.getPossibleValues()) {
            ResourceLocation sideTex = resource(charred ? BlocksNF.CHARRED_LOG.get() : texBlock), topTex = resource(charred ? BlocksNF.CHARRED_LOG.get() : texBlock, "_top");
            for(TreeStemBlock.Type type : TreeStemBlock.TYPE.getPossibleValues()) {
                int xRot = (type == TreeStemBlock.Type.BOTTOM || type == TreeStemBlock.Type.ROTATED_BOTTOM) ? 180 : 0;
                ModelFile vertical, horizontal;
                String name = charred ? "charred_stem" : name(block);
                if(type == TreeStemBlock.Type.END || type == TreeStemBlock.Type.FAKE_END) {
                    vertical = models().cubeColumn(name + "_end", sideTex, sideTex);
                    horizontal = models().cubeColumnHorizontal(name + "_horizontal_end", sideTex, sideTex);
                }
                else if(type == TreeStemBlock.Type.TOP || type == TreeStemBlock.Type.BOTTOM) {
                    vertical = models().cubeTop(name, sideTex, topTex);
                    horizontal = cubeTopHorizontal(name + "_horizontal", sideTex, topTex);
                }
                else {
                    vertical = cubeTopHorizontalRotated(name + "_horizontal_rotated", sideTex, topTex);
                    horizontal = cubeTopHorizontalRotated(name + "_horizontal_rotated", sideTex, topTex);
                }
                getVariantBuilder(block)
                        .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y).with(TreeStemBlock.TYPE, type).with(TreeStemBlock.CHARRED, charred)
                        .modelForState().modelFile(vertical).rotationX(xRot).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z).with(TreeStemBlock.TYPE, type).with(TreeStemBlock.CHARRED, charred)
                        .modelForState().modelFile(horizontal).rotationX(xRot + 90).addModel()
                        .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.X).with(TreeStemBlock.TYPE, type).with(TreeStemBlock.CHARRED, charred)
                        .modelForState().modelFile(horizontal).rotationX(xRot + 90).rotationY(90).addModel();
            }
        }
    }

    public void leavesBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), "leaves").texture("all", resource(block))).build());
    }

    public void crossLeavesBlock(Block block) {
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, false).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block), resource("leaves_cross")).texture("all",
                        resource(block)).texture("cross", resource(block, "_cross"))).build());
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, true).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block) + "_alt", resource("leaves_cross_alt")).texture("all",
                        resource(block)).texture("cross", resource(block, "_cross"))).build());
    }

    public void fruitLeavesBlock(Block base, Block block) {
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, false).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block), resource("leaves_cross")).texture("all",
                        resource(base)).texture("cross", resource(block, "_cross"))).build());
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, true).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block) + "_alt", resource("leaves_cross_alt")).texture("all",
                        resource(base)).texture("cross", resource(block, "_cross"))).build());
    }

    public void tintedFruitLeavesBlock(Block base, Block block) {
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, false).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block), resource("leaves_double_cross")).texture("all",
                        resource(base)).texture("cross2", resource(block, "_cross")).texture("cross", resource(block, "_foliage_cross"))).build());
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, true).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block) + "_alt", resource("leaves_double_cross_alt")).texture("all",
                        resource(base)).texture("cross2", resource(block, "_cross")).texture("cross", resource(block, "_foliage_cross"))).build());
    }

    public void rotatedFruitLeavesBlock(Block base, Block block) {
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, false).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block), resource("leaves_cross")).texture("all",
                        resource(base)).texture("cross", resource(block, "_cross"))).build());
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, true).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block) + "_alt", resource("leaves_cross_rotated")).texture("all",
                        resource(base)).texture("cross", resource(block, "_cross"))).build());
    }

    public void rotatedLeavesBlock(Block block) {
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, false).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block), "leaves").texture("all", resource(block))).build());
        getVariantBuilder(block).partialState().with(TreeBranchesBlock.ALTERNATE, true).addModels(ConfiguredModel.builder().modelFile(
                models().withExistingParent(name(block) + "_alt", resource("leaves_rotated")).texture("all", resource(block))).build());
    }

    public void trunkBlock(TreeTrunkBlock block, Block textureBlock) {
        getVariantBuilder(block).partialState().with(TreeTrunkBlock.CHARRED, false).modelForState().modelFile(models().cubeColumn(name(block), resource(textureBlock), resource(textureBlock.getRegistryName().getPath() + "_top"))).addModel()
                .partialState().with(TreeTrunkBlock.CHARRED, true).modelForState().modelFile(models().cubeColumn("charred_trunk", resource(BlocksNF.CHARRED_LOG.get()), resource(BlocksNF.CHARRED_LOG.getId().getPath() + "_top"))).addModel();
    }

    public void barrelBlock(BarrelBlockNF block) {
        ModelFile vertical = templateModel(block, resource("barrel_vertical"), Pair.of("side", resource(block, "_side")),
                Pair.of("top", resource(block, "_end")), Pair.of("bottom", resource(block, "_end")));
        ModelFile horizontal = templateModel(name(block) + "_horizontal", resource("barrel_horizontal"), Pair.of("side", resource(block, "_side")),
                Pair.of("top", resource(block, "_end")), Pair.of("bottom", resource(block, "_end")));
        ModelFile verticalOpen = templateModel(name(block) + "_open", resource("barrel_vertical"), Pair.of("side", resource(block, "_side")),
                Pair.of("top", resource(block, "_open")), Pair.of("bottom", resource(block, "_end")));
        ModelFile horizontalOpen = templateModel(name(block) + "_horizontal_open", resource("barrel_horizontal"), Pair.of("side", resource(block, "_side")),
                Pair.of("top", resource(block, "_open")), Pair.of("bottom", resource(block, "_end")));
        for(Direction facing : BarrelBlockNF.FACING.getPossibleValues()) {
            for(boolean open : BarrelBlockNF.OPEN.getPossibleValues()) {
                if(facing.getAxis().isHorizontal()) {
                    getVariantBuilder(block).partialState().with(BarrelBlockNF.OPEN, open).with(BarrelBlockNF.FACING, facing).addModels((ConfiguredModel.builder()
                            .modelFile(open ? horizontalOpen : horizontal).rotationY((int) facing.getOpposite().toYRot()).build()));
                }
                else getVariantBuilder(block).partialState().with(BarrelBlockNF.OPEN, open).with(BarrelBlockNF.FACING, facing).addModels((ConfiguredModel.builder()
                        .modelFile(open ? verticalOpen : vertical).build()));
            }
        }
    }

    public void chestBlock(Block block, ResourceLocation particle) {
        for(Direction facing : ChestBlock.FACING.getPossibleValues()) {
            int yRot = (int) facing.getOpposite().toYRot();
            getVariantBuilder(block).partialState().with(ChestBlock.FACING, facing).with(ChestBlock.TYPE, ChestType.SINGLE).addModels((ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(name(block), resource("chest_single"))
                            .texture("all", ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "entity/chest/" + block.getRegistryName().getPath() + "_single"))
                            .texture("particle", particle)).rotationY(yRot).build()));
            getVariantBuilder(block).partialState().with(ChestBlock.FACING, facing).with(ChestBlock.TYPE, ChestType.LEFT).addModels((ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(name(block) + "_left", resource("chest_left"))
                            .texture("all", ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "entity/chest/" + block.getRegistryName().getPath() + "_left"))
                            .texture("particle", particle)).rotationY(yRot).build()));
            getVariantBuilder(block).partialState().with(ChestBlock.FACING, facing).with(ChestBlock.TYPE, ChestType.RIGHT).addModels((ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(name(block) + "_right", resource("chest_right"))
                            .texture("all", ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "entity/chest/" + block.getRegistryName().getPath() + "_right"))
                            .texture("particle", particle)).rotationY(yRot).build()));
        }
    }

    public void chairBlock(ChairBlock block, ResourceLocation particle) {
        ModelFile singleBottom = templateModel(name(block) + "_single_bottom", resource("chair_single_bottom"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile leftBottom = templateModel(name(block) + "_left_bottom", resource("chair_left_bottom"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile rightBottom = templateModel(name(block) + "_right_bottom", resource("chair_right_bottom"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile middleBottom = templateModel(name(block) + "_middle_bottom", resource("chair_middle_bottom"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile singleTop = templateModel(name(block) + "_single_top", resource("chair_single_top"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile leftTop = templateModel(name(block) + "_left_top", resource("chair_left_top"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile rightTop = templateModel(name(block) + "_right_top", resource("chair_right_top"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        ModelFile middleTop = templateModel(name(block) + "_middle_top", resource("chair_middle_top"), Pair.of("0", resource(block)), Pair.of("particle", particle));
        for(Direction facing : ChairBlock.FACING.getPossibleValues()) {
            for(ChairBlock.Type type : ChairBlock.TYPE.getPossibleValues()) {
                for(DoubleBlockHalf half : ChairBlock.HALF.getPossibleValues()) {
                    getVariantBuilder(block).partialState().with(ChairBlock.TYPE, type).with(ChairBlock.FACING, facing).with(ChairBlock.HALF, half).addModels((ConfiguredModel.builder()
                            .modelFile(switch(type) {
                                case SINGLE -> half == DoubleBlockHalf.LOWER ? singleBottom : singleTop;
                                case LEFT -> half == DoubleBlockHalf.LOWER ? leftBottom : leftTop;
                                case RIGHT -> half == DoubleBlockHalf.LOWER ? rightBottom : rightTop;
                                case MIDDLE -> half == DoubleBlockHalf.LOWER ? middleBottom : middleTop;
                            }).rotationY((int) facing.getOpposite().toYRot()).build()));
                }
            }
        }
    }

    public void troughBlock(TroughBlock block, ResourceLocation particle) {
        for(Direction.Axis facing : TroughBlock.AXIS.getPossibleValues()) {
            for(int amount : TroughBlock.AMOUNT.getPossibleValues()) {
                if(amount == 0) {
                    getVariantBuilder(block).partialState().with(TroughBlock.AXIS, facing).with(TroughBlock.AMOUNT, amount).addModels((ConfiguredModel.builder()
                            .modelFile(templateModel(name(block) + "_" + amount, resource("trough_" + amount),
                                    Pair.of("long", resource(block)), Pair.of("short", resource(block, "_side")), Pair.of("bottom", resource(block, "_bottom")), Pair.of("particle", particle)))
                            .rotationY(facing == Direction.Axis.X ? 90 : 0).build()));
                }
                else for(TroughBlock.FoodType foodType : TroughBlock.FOOD_TYPE.getPossibleValues()) {
                    getVariantBuilder(block).partialState().with(TroughBlock.AXIS, facing).with(TroughBlock.AMOUNT, amount).with(TroughBlock.FOOD_TYPE, foodType).addModels((ConfiguredModel.builder()
                            .modelFile(templateModel(name(block) + "_" + foodType.getSerializedName() + "_" + amount, resource("trough_" + amount),
                                    Pair.of("long", resource(block)), Pair.of("short", resource(block, "_side")), Pair.of("bottom", resource(block, "_bottom")),
                                    Pair.of("food", resource("trough_" + foodType.getSerializedName())), Pair.of("particle", particle)))
                            .rotationY(facing == Direction.Axis.X ? 90 : 0).build()));
                }
            }
        }
    }

    public void crossBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), "cross").texture("cross", resource(block))).build());
    }

    public void crossBlock(Block block, ResourceLocation texture) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), "cross").texture("cross", texture)).build());
    }

    public void tintedCrossBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), "tinted_cross")
                .texture("cross", resource(block))).build());
    }

    public void tallTintedCrossBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), resource("tall_tinted_cross"))
                .texture("cross", resource(block))).build());
    }

    public void tintedCross(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), resource("tinted_cross"))
                .texture("base", resource(block)).texture("overlay", resource(block, "_overlay"))).build());
    }

    public void evergreenSaplingBlock(Block block) {
        getVariantBuilder(block).partialState().with(TreeSeedBlock.STAGE, 0).addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(
                name(block) + "_sprout", "cross").texture("cross", resource(block, "_sprout"))).build());
        getVariantBuilder(block).partialState().with(TreeSeedBlock.STAGE, 1).addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(
                name(block), "cross").texture("cross", resource(block))).build());
    }

    public void deciduousSaplingBlock(Block block) {
        getVariantBuilder(block).partialState().with(TreeSeedBlock.STAGE, 0).addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(
                name(block) + "_sprout", "cross").texture("cross", resource(block, "_sprout"))).build());
        getVariantBuilder(block).partialState().with(TreeSeedBlock.STAGE, 1).addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), resource("tinted_cross"))
                .texture("base", resource(block)).texture("overlay", resource(block, "_overlay"))).build());
    }

    public void vinesBlock(Block block) {
        multifaceBlock(block, models().withExistingParent(name(block), "vine").texture("vine", resource(block)));
    }

    public void multifaceBlock(Block block, ModelFile model) {
        getMultipartBuilder(block).part().modelFile(model).rotationX(270).uvLock(true).addModel().condition(BlockStateProperties.UP, true).end()
                .part().modelFile(model).addModel().condition(BlockStateProperties.NORTH, true).end()
                .part().modelFile(model).rotationY(270).uvLock(true).addModel().condition(BlockStateProperties.WEST, true).end()
                .part().modelFile(model).rotationY(180).uvLock(true).addModel().condition(BlockStateProperties.SOUTH, true).end()
                .part().modelFile(model).rotationY(90).uvLock(true).addModel().condition(BlockStateProperties.EAST, true).end();
        if(block.defaultBlockState().hasProperty(BlockStateProperties.DOWN)) {
            getMultipartBuilder(block).part().modelFile(model).rotationX(90).uvLock(true).addModel().condition(BlockStateProperties.DOWN, true).end();
        }
    }

    public void randomRotatedBlock(Block block, ModelFile model) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(model).nextModel().modelFile(model).rotationY(90).nextModel()
                .modelFile(model).rotationY(180).nextModel().modelFile(model).rotationY(270).build());
    }

    public void randomRotatedBlock(Block block) {
        randomRotatedBlock(block, cubeAll(block));
    }

    public void mirroredBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(cubeAll(block)).nextModel().modelFile(cubeMirroredAll(block)).nextModel()
                .modelFile(cubeAll(block)).rotationY(180).nextModel().modelFile(cubeMirroredAll(block)).rotationY(180).build());
    }

    public void columnBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(cubeColumn(block)).build());
    }

    public void mirroredColumnBlock(Block block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(cubeColumn(block)).nextModel().modelFile(cubeMirroredColumn(block)).nextModel()
                .modelFile(cubeColumn(block)).rotationY(180).nextModel().modelFile(cubeMirroredColumn(block)).rotationY(180).build());
    }

    public void snowyTintedSoilBlock(CoveredSoilBlock block, String soilName, String topName) {
        getVariantBuilder(block).forAllStates(state -> {
            if(state.getValue(BlockStateProperties.SNOWY)) return ConfiguredModel.builder().modelFile(models().withExistingParent(name(block) + "_snow", "cube_bottom_top")
                    .texture("top", resource(topName)).texture("bottom", resource(block.soilBlock.get()))
                    .texture("side", resource("snowy_" + soilName + "_side")).texture("particle", resource(block.soilBlock.get()))).build();
            else {
                ModelFile file = models().withExistingParent(name(block), resource("template_soil")).texture("overlay",  resource(block, "_side_overlay"))
                        .texture("top", resource(topName)).texture("bottom", resource(block.soilBlock.get()))
                        .texture("side", resource(block, "_side")).texture("particle", resource(block.soilBlock.get()));
                return ConfiguredModel.builder().modelFile(file).nextModel().modelFile(file).rotationY(90)
                        .nextModel().modelFile(file).rotationY(180).nextModel().modelFile(file).rotationY(270).build();
            }
        });
    }

    public void snowySoilBlock(CoveredSoilBlock block, String soilName, String topName) {
        getVariantBuilder(block).forAllStates(state -> {
            if(state.getValue(BlockStateProperties.SNOWY)) return ConfiguredModel.builder().modelFile(models().withExistingParent(name(block) + "_snow", "cube_bottom_top")
                    .texture("top", resource(topName)).texture("bottom", resource(block.soilBlock.get()))
                    .texture("side", resource("snowy_" + soilName + "_side")).texture("particle", resource(block.soilBlock.get()))).build();
            else {
                ModelFile file = models().withExistingParent(name(block), "cube_bottom_top")
                        .texture("top", resource(topName)).texture("bottom", resource(block.soilBlock.get()))
                        .texture("side", resource(block, "_side")).texture("particle", resource(block.soilBlock.get()));
                return ConfiguredModel.builder().modelFile(file).nextModel().modelFile(file).rotationY(90)
                        .nextModel().modelFile(file).rotationY(180).nextModel().modelFile(file).rotationY(270).build();
            }
        });
    }

    public void fuelPileBlock(Block block) {
        getVariantBuilder(block).forAllStates(state -> {
                    String suffix = state.getValue(BlockStatePropertiesNF.SMELTED) ? "_smelted" : (state.getValue(BlockStateProperties.LIT) ? "_lit" : "");
                    if(suffix.equals("_lit")) return ConfiguredModel.builder().modelFile(file(block, suffix)).build();
                    else return ConfiguredModel.builder()
                            .modelFile(models().cubeAll(name(block) + suffix, resource(name(block) + suffix)))
                            .build();
                });
    }

    public void stairBlock(StairBlockNF block, ResourceLocation texture) {
        stairBlock(block, texture, texture, texture);
    }

    public void stairBlock(StairBlockNF block, String name, ResourceLocation texture) {
        stairBlock(block, name, texture, texture, texture);
    }

    public void stairBlock(StairBlockNF block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        stairsBlockInternal(block, block.getRegistryName().toString(), side, bottom, top);
    }

    public void stairBlock(StairBlockNF block, String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        stairsBlockInternal(block, name + "_stairs", side, bottom, top);
    }

    private void stairsBlockInternal(StairBlockNF block, String baseName, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        ModelFile stairs = models().stairs(baseName, side, bottom, top);
        ModelFile stairsInner = models().stairsInner(baseName + "_inner", side, bottom, top);
        ModelFile stairsOuter = models().stairsOuter(baseName + "_outer", side, bottom, top);
        stairBlock(block, stairs, stairsInner, stairsOuter);
    }

    public void stairBlock(StairBlockNF block, ModelFile stairs, ModelFile stairsInner, ModelFile stairsOuter) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
                    Direction facing = state.getValue(StairBlockNF.FACING);
                    Half half = state.getValue(StairBlockNF.HALF);
                    StairsShape shape = state.getValue(StairBlockNF.SHAPE);
                    int yRot = (int) facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason
                    if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                        yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
                    }
                    if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                        yRot += 90; // Top stairs are rotated 90 degrees clockwise
                    }
                    yRot %= 360;
                    boolean uvlock = yRot != 0 || half == Half.TOP; // Don't set uvlock for states that have no rotation
                    return ConfiguredModel.builder()
                            .modelFile(shape == StairsShape.STRAIGHT ? stairs : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner : stairsOuter)
                            .rotationX(half == Half.BOTTOM ? 0 : 180)
                            .rotationY(yRot)
                            .uvLock(uvlock)
                            .build();
                }, StairBlockNF.WATER_LEVEL, StairBlockNF.WATERLOG_TYPE, StairBlock.WATERLOGGED);
    }

    public void sidingBlock(SidingBlock block, ResourceLocation texture) {
        sidingBlock(block, texture, texture, texture, texture, texture, texture, texture, texture, texture, texture, true);
    }

    public void sidingBlock(SidingBlock block, ResourceLocation doubled, ResourceLocation side, ResourceLocation shortSide, ResourceLocation bottom, ResourceLocation top, ResourceLocation quartBottom, ResourceLocation quartTop, ResourceLocation innerBottom, ResourceLocation innerTop, ResourceLocation innerSide, boolean lockUV) {
        sidingBlock(block,
                models().withExistingParent(name(block), resource("siding")).texture("side", side).texture("short_side", shortSide)
                        .texture("bottom", bottom).texture("top", top),
                models().withExistingParent(name(block) + "_quartet", resource("quartet")).texture("side", shortSide)
                        .texture("bottom", quartBottom).texture("top", quartTop),
                models().withExistingParent(name(block) + "_inner", resource("siding_inner")).texture("side", side).texture("short_side", shortSide)
                        .texture("inner_side", innerSide).texture("bottom", innerBottom).texture("top", innerTop),
                models().getExistingFile(doubled), lockUV);
    }

    public void sidingBlock(SidingBlock block, ModelFile full, ModelFile quartet, ModelFile inner, ModelFile doubled, boolean lockUV) {
        for(SidingBlock.Type type : SidingBlock.Type.values()) {
            for(SidingBlock.Shape shape : SidingBlock.Shape.values()) {
                if(type == SidingBlock.Type.DOUBLE) {
                    if(shape == SidingBlock.Shape.FULL) {
                        getVariantBuilder(block).partialState().with(SidingBlock.TYPE, type).addModels(new ConfiguredModel(doubled));
                    }
                    continue;
                }
                int yRot = (int) type.getDirection().getClockWise().toYRot();
                if(!shape.positive) yRot -= 90;
                yRot = (yRot + 360) % 360;
                getVariantBuilder(block).partialState().with(SidingBlock.TYPE, type).with(SidingBlock.SHAPE, shape).addModels(ConfiguredModel.builder()
                        .modelFile(shape == SidingBlock.Shape.FULL ? full : (shape.inner ? inner : quartet)).rotationY(yRot).uvLock(yRot != 0 && lockUV).build());
            }
        }
    }

    public void slabBlock(SlabBlockNF block, ResourceLocation texture) {
        slabBlock(block, texture, texture, texture, texture);
    }

    public void slabBlock(SlabBlockNF block, ResourceLocation doubleslab, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        slabBlock(block, models().slab(name(block), side, bottom, top), models().slabTop(name(block) + "_top", side, bottom, top), models().getExistingFile(doubleslab));
    }

    public void slabBlock(SlabBlockNF block, ModelFile bottom, ModelFile top, ModelFile doubleslab) {
        getVariantBuilder(block)
                .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottom))
                .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(top))
                .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleslab));
    }

    public void fourWayBlock(CrossCollisionBlockNF block, ModelFile post, ModelFile side) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block)
                .part().modelFile(post).addModel().end();
        fourWayMultipart(builder, side);
    }

    public void fenceBlock(FenceBlockNF block, ResourceLocation texture) {
        String baseName = block.getRegistryName().toString();
        fourWayBlock(block, models().fencePost(baseName + "_post", texture), models().fenceSide(baseName + "_side", texture));
    }

    public void fenceBlock(FenceBlockNF block, String name, ResourceLocation texture) {
        fourWayBlock(block, models().fencePost(name + "_fence_post", texture), models().fenceSide(name + "_fence_side", texture));
    }

    public void fenceGateBlock(FenceGateBlockNF block, ResourceLocation texture) {
        fenceGateBlockInternal(block, block.getRegistryName().toString(), texture);
    }

    public void fenceGateBlock(FenceGateBlockNF block, String name, ResourceLocation texture) {
        fenceGateBlockInternal(block, name + "_fence_gate", texture);
    }

    private void fenceGateBlockInternal(FenceGateBlockNF block, String baseName, ResourceLocation texture) {
        ModelFile gate = templateModel(baseName, resource("template_fence_gate"), Pair.of("0", texture));
        ModelFile gateOpen = templateModel(baseName + "_open", resource("template_fence_gate_open"), Pair.of("0", texture));
        ModelFile gateWall = templateModel(baseName + "_wall", resource("template_fence_gate_wall"), Pair.of("0", texture));
        ModelFile gateWallOpen = templateModel(baseName + "_wall_open", resource("template_fence_gate_wall_open"), Pair.of("0", texture));
        fenceGateBlock(block, gate, gateOpen, gateWall, gateWallOpen);
    }

    public void fenceGateBlock(FenceGateBlockNF block, ModelFile gate, ModelFile gateOpen, ModelFile gateWall, ModelFile gateWallOpen) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            ModelFile model = gate;
            if (state.getValue(FenceGateBlock.IN_WALL)) {
                model = gateWall;
            }
            if (state.getValue(FenceGateBlock.OPEN)) {
                model = model == gateWall ? gateWallOpen : gateOpen;
            }
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY((int) state.getValue(FenceGateBlock.FACING).toYRot())
                    .uvLock(true)
                    .build();
        }, FenceGateBlock.POWERED, FenceGateBlockNF.WATER_LEVEL, BlockStatePropertiesNF.WATERLOG_TYPE);
    }

    public void barsBlock(Block block) {
        ResourceLocation bars = resource(block), edge = resource(block, "_edge");
        ModelFile cap = templateModel(name(block) + "_cap", resource("bars_cap"), Pair.of("bars", edge), Pair.of("particle", bars));
        ModelFile capAlt = templateModel(name(block) + "_cap_alt", resource("bars_cap_alt"), Pair.of("bars", edge), Pair.of("particle", bars));
        ModelFile post = templateModel(name(block) + "_post", mcLoc("iron_bars_post"), Pair.of("bars", edge), Pair.of("particle", bars));
        ModelFile postEnds = templateModel(name(block) + "_post_ends", mcLoc("iron_bars_post_ends"), Pair.of("edge", edge), Pair.of("particle", bars));
        ModelFile side = templateModel(name(block) + "_side", mcLoc("iron_bars_side"), Pair.of("bars", bars), Pair.of("edge", edge), Pair.of("particle", bars));
        ModelFile sideAlt = templateModel(name(block) + "_side_alt", mcLoc("iron_bars_side_alt"), Pair.of("bars", bars), Pair.of("edge", edge), Pair.of("particle", bars));
        getMultipartBuilder(block)
                .part().modelFile(postEnds).addModel()
                .end()
                .part().modelFile(post).addModel()
                .condition(BlockStateProperties.NORTH, false)
                .condition(BlockStateProperties.WEST, false)
                .condition(BlockStateProperties.SOUTH, false)
                .condition(BlockStateProperties.EAST, false)
                .end()
                .part().modelFile(cap).addModel()
                .condition(BlockStateProperties.NORTH, true)
                .condition(BlockStateProperties.WEST, false)
                .condition(BlockStateProperties.SOUTH, false)
                .condition(BlockStateProperties.EAST, false)
                .end()
                .part().modelFile(cap).rotationY(90).addModel()
                .condition(BlockStateProperties.NORTH, false)
                .condition(BlockStateProperties.WEST, false)
                .condition(BlockStateProperties.SOUTH, false)
                .condition(BlockStateProperties.EAST, true)
                .end()
                .part().modelFile(capAlt).addModel()
                .condition(BlockStateProperties.NORTH, false)
                .condition(BlockStateProperties.WEST, false)
                .condition(BlockStateProperties.SOUTH, true)
                .condition(BlockStateProperties.EAST, false)
                .end()
                .part().modelFile(capAlt).rotationY(90).addModel()
                .condition(BlockStateProperties.NORTH, false)
                .condition(BlockStateProperties.WEST, true)
                .condition(BlockStateProperties.SOUTH, false)
                .condition(BlockStateProperties.EAST, false)
                .end()
                .part().modelFile(side).addModel()
                .condition(BlockStateProperties.NORTH, true)
                .end()
                .part().modelFile(side).rotationY(90).addModel()
                .condition(BlockStateProperties.EAST, true)
                .end()
                .part().modelFile(sideAlt).addModel()
                .condition(BlockStateProperties.SOUTH, true)
                .end()
                .part().modelFile(sideAlt).rotationY(90).addModel()
                .condition(BlockStateProperties.WEST, true);
    }

    public void ingotBlock(HeatablePileBlock block) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int heat = state.getValue(BlockStatePropertiesNF.HEAT);
            String suffix = "_" + state.getValue(block.count);
            if(heat == 0) {
                ResourceLocation texture = resource(name(block));
                return ConfiguredModel.builder().modelFile(models().withExistingParent(name(block) + suffix, resource("template_ingot" + suffix))
                                .texture("all", texture).texture("particle", texture)).build();
            }
            else return ConfiguredModel.builder().modelFile(models().getExistingFile(resource("hot_ingot_" + (heat - 1) + suffix))).build();
        }, HeatablePileBlock.WATER_LEVEL, BlockStatePropertiesNF.WATERLOG_TYPE);
    }

    public void simpleHeatableBlock(Block block) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int heat = state.getValue(BlockStatePropertiesNF.HEAT);
            String suffix = "_" + TieredHeat.fromTier(heat);
            ResourceLocation texture = resource(name(block) + suffix);
            return ConfiguredModel.builder().modelFile(models().cubeAll(texture.getPath(), texture)).build();
        });
    }

    public void horizontalHeatableBlock(Block block) {
        for(int heat : BlockStatePropertiesNF.HEAT.getPossibleValues()) {
            String suffix = "_" + TieredHeat.fromTier(heat);
            ModelFile model = models().withExistingParent(name(block) + suffix, resource("cube_column_horizontal"))
                    .texture("side", resource(name(block) + suffix)).texture("end", resource(name(block) + "_top" + suffix));
            getVariantBuilder(block)
                    .partialState().with(BlockStatePropertiesNF.HEAT, heat).with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z)
                    .modelForState().modelFile(model).addModel()
                    .partialState().with(BlockStatePropertiesNF.HEAT, heat).with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X)
                    .modelForState().modelFile(model).rotationY(90).addModel();
        }
    }

    public void crucibleBlock(Block block) {
        for(int heat : BlockStatePropertiesNF.HEAT_FULL.getPossibleValues()) {
            String suffix = "_" + TieredHeat.fromTier(heat);
            ModelFile model = models().withExistingParent(name(block) + suffix, resource(block))
                    .texture("all", resource(name(block) + suffix)).texture("particle", resource(BlocksNF.TERRACOTTA.get()));
            getVariantBuilder(block)
                    .partialState().with(BlockStatePropertiesNF.HEAT_FULL, heat).with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z)
                    .modelForState().modelFile(model).addModel()
                    .partialState().with(BlockStatePropertiesNF.HEAT_FULL, heat).with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X)
                    .modelForState().modelFile(model).rotationY(90).addModel();
        }
    }

    public void unfiredPotteryBlock(FireablePartialBlock block) {
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(models().withExistingParent(name(block), resource(block.firedBlock.get())).texture("all", resource(block)).texture("particle", resource(BlocksNF.CLAY.get()))).build());
    }

    public void unfiredAxisPotteryBlock(FireablePartialBlock block) {
        horizontalAxisBlock(block, models().withExistingParent(name(block), resource(block.firedBlock.get())).texture("all", resource(block)).texture("particle", resource(BlocksNF.CLAY.get())));
    }

    public void horizontalBlockNF(Block block, ModelFile model, int angleOffset) {
        horizontalBlockNF(block, $ -> model, angleOffset);
    }

    public void horizontalBlockNF(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {
        for(Direction dir : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            getVariantBuilder(block).partialState().with(BlockStateProperties.HORIZONTAL_FACING, dir).modelForState()
                    .modelFile(modelFunc.apply(block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, dir)))
                    .rotationY(((int) dir.toYRot() + angleOffset) % 360)
                    .addModel();
        }
    }

    public void directionalBlockNF(Block block, ModelFile model, int angleOffset) {
        directionalBlockNF(block, $ -> model, angleOffset);
    }

    public void directionalBlockNF(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {
        for(Direction dir : BlockStateProperties.FACING.getPossibleValues()) {
            getVariantBuilder(block).partialState().with(BlockStateProperties.FACING, dir).modelForState()
                    .modelFile(modelFunc.apply(block.defaultBlockState().setValue(BlockStateProperties.FACING, dir)))
                    .rotationX(dir == Direction.DOWN ? 90 : (dir == Direction.UP ? -90 : 0))
                    .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.getOpposite().toYRot() + angleOffset) % 360)
                    .addModel();
        }
    }

    public void unfiredMoldBlock(Block unfiredMold, Block firedMold) {
        ModelFile model = models().withExistingParent(name(unfiredMold), resource(firedMold))
                .texture("0", resource("clay_block")).texture("1", resource("clay_darkened"))
                .texture("particle", resource("clay_block"));
        horizontalBlockNF(unfiredMold, model, 0);
    }

    public void horizontalColumnBlock(Block block) {
        ModelFile horizontal = models().withExistingParent(name(block), resource("cube_column_horizontal"))
                .texture("side", resource(block)).texture("end", resource(block, "_top"));
        horizontalAxisBlock(block, horizontal);
    }

    public void horizontalColumnBottomTopBlock(Block block, ResourceLocation bottom, ResourceLocation top) {
        ModelFile horizontal = models().withExistingParent(name(block), resource("cube_column_horizontal_bottom_top"))
                .texture("side", resource(block)).texture("end", resource(block, "_top"))
                .texture("bottom", bottom).texture("top", top);
        horizontalAxisBlock(block, horizontal);
    }

    public void horizontalAxisBlock(Block block, ModelFile model) {
        getVariantBuilder(block)
                .partialState().with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z)
                .modelForState().modelFile(model).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X)
                .modelForState().modelFile(model).rotationY(90).addModel();
    }

    public void anvilBlock(TieredAnvilBlock block) {
        getVariantBuilder(block)
                .forAllStatesExcept(state -> ConfiguredModel.builder()
                                .modelFile(models().withExistingParent(name(block), resource("anvil")).texture("body", resource(block))
                                        .texture("top", resource(block, "_top")).texture("particle", resource(block)))
                                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) % 360)
                                .build()
                        , BlockStatePropertiesNF.WATER_LEVEL, BlockStatePropertiesNF.WATERLOG_TYPE, BlockStatePropertiesNF.RANDOM_TICKING);
    }

    public void ladderBlock(LadderBlockNF block) {
        getVariantBuilder(block)
                .forAllStatesExcept(state -> ConfiguredModel.builder()
                                .modelFile(models().withExistingParent(name(block), "ladder").texture("texture", resource(block))
                                        .texture("particle", resource(block)))
                                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                                .build()
                        , BlockStatePropertiesNF.WATER_LEVEL, BlockStatePropertiesNF.WATERLOG_TYPE);
    }

    public void hatchBlock(HatchBlock block) {
        String baseName = name(block);
        ModelFile bottomLeft = models().withExistingParent(baseName, resource("hatch")).texture("texture", resource(block));
        ModelFile bottomRight = models().withExistingParent(baseName + "_rh", resource("hatch_rh")).texture("texture", resource(block));
        hatchBlock(block, bottomLeft, bottomRight);
    }

    public void hatchBlock(HatchBlock block, ModelFile bottomLeft, ModelFile bottomRight) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int yRot = ((int) state.getValue(HatchBlock.FACING).toYRot()) + 90;
            boolean rh = state.getValue(HatchBlock.HINGE) == DoorHingeSide.RIGHT;
            boolean open = state.getValue(HatchBlock.OPEN);
            boolean right = rh ^ open;
            if (open) {
                yRot += 90;
            }
            if (rh && open) {
                yRot += 180;
            }
            yRot %= 360;
            return ConfiguredModel.builder().modelFile(right ? bottomRight : bottomLeft)
                    .rotationY(yRot)
                    .build();
        }, HatchBlock.WATER_LEVEL, HatchBlock.WATERLOG_TYPE);
    }

    public void doorBlock(DoorBlockNF block) {
        String baseName = block.getRegistryName().toString();
        ResourceLocation bottom = resource(block, "_bottom"), top = resource(block, "_top");
        ModelFile bottomLeft = models().doorBottomLeft(baseName + "_bottom", bottom, top);
        ModelFile bottomRight = models().doorBottomRight(baseName + "_bottom_hinge", bottom, top);
        ModelFile topLeft = models().doorTopLeft(baseName + "_top", bottom, top);
        ModelFile topRight = models().doorTopRight(baseName + "_top_hinge", bottom, top);
        doorBlock(block, bottomLeft, bottomRight, topLeft, topRight);
    }

    public void doorBlock(DoorBlockNF block, ModelFile bottomLeft, ModelFile bottomRight, ModelFile topLeft, ModelFile topRight) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int yRot = ((int) state.getValue(DoorBlock.FACING).toYRot()) + 90;
            boolean rh = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
            boolean open = state.getValue(DoorBlock.OPEN);
            boolean right = rh ^ open;
            if (open) {
                yRot += 90;
            }
            if (rh && open) {
                yRot += 180;
            }
            yRot %= 360;
            return ConfiguredModel.builder().modelFile(state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? (right ? bottomRight : bottomLeft) : (right ? topRight : topLeft))
                    .rotationY(yRot)
                    .build();
        }, DoorBlock.POWERED, DoorBlockNF.WATER_LEVEL, DoorBlockNF.WATERLOG_TYPE);
    }

    public void trapdoorBlock(TrapdoorBlockNF block) {
        trapdoorBlock(block, resource(block), true);
    }

    @Override
    public void trapdoorBlock(TrapDoorBlock block, ModelFile bottom, ModelFile top, ModelFile open, boolean orientable) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int xRot = 0;
            int yRot = ((int) state.getValue(TrapDoorBlock.FACING).toYRot()) + 180;
            boolean isOpen = state.getValue(TrapDoorBlock.OPEN);
            if(orientable && isOpen && state.getValue(TrapDoorBlock.HALF) == Half.TOP) {
                xRot += 180;
                yRot += 180;
            }
            if(!orientable && !isOpen) {
                yRot = 0;
            }
            yRot %= 360;
            return ConfiguredModel.builder().modelFile(isOpen ? open : state.getValue(TrapDoorBlock.HALF) == Half.TOP ? top : bottom)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        }, TrapDoorBlock.POWERED, TrapDoorBlock.WATERLOGGED, TrapdoorBlockNF.WATERLOG_TYPE, TrapdoorBlockNF.WATER_LEVEL);
    }

    public void logBlock(RotatedPillarBlock block, Block textureBlock) {
        ResourceLocation texture = blockTexture(textureBlock);
        axisBlock(block, texture, ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), texture.getPath() + "_top"));
    }

    public void groundItemBlock(Block block, int textures) {
        var builder = ConfiguredModel.builder();
        for(int i = 0; i < textures; i++) {
            ResourceLocation texture = resource(block, "_" + i);
            ModelFile model = models().withExistingParent(name(block) + "_" + i, resource(block))
                    .texture("0", texture).texture("particle", texture);
            builder = builder.modelFile(model).nextModel().modelFile(model).rotationY(90).nextModel()
                    .modelFile(model).rotationY(180).nextModel().modelFile(model).rotationY(270);
            if(i < textures - 1) builder = builder.nextModel();
        }
        getVariantBuilder(block).partialState().addModels(builder.build());
    }

    public void clusterBlock(ClusterBlock block, ResourceLocation texture, String prefix) {
        for(int i = 1; i <= 4; i++) {
            ModelFile model = models().withExistingParent(name(block) + "_" + i, resource(prefix + "_cluster_" + i))
                    .texture("0", texture).texture("particle", texture);
            getVariantBuilder(block).partialState().with(ClusterBlock.COUNT, i).addModels(ConfiguredModel.builder().modelFile(model).nextModel()
                    .modelFile(model).rotationY(90).nextModel().modelFile(model).rotationY(180).nextModel().modelFile(model).rotationY(270).build());
        }
    }

    public void skaraNestBlock(Block block, ResourceLocation texture, String prefix) {
        ModelFile model = models().withExistingParent(name(block), resource(prefix + "_cluster_4"))
                .texture("0", texture).texture("particle", texture);
        getVariantBuilder(block).partialState().addModels(ConfiguredModel.builder().modelFile(model).nextModel()
                .modelFile(model).rotationY(90).nextModel().modelFile(model).rotationY(180).nextModel().modelFile(model).rotationY(270).build());
    }

    public void eggNestBlock(EggNestBlock block) {
        for(int i = 0; i <= 4; i++) {
            ModelFile model = models().withExistingParent(name(block) + "_" + i, resource("egg_nest_" + i)).texture("egg", resource(block, "_egg"));
            getVariantBuilder(block).partialState().with(EggNestBlock.EGGS, i).addModels(ConfiguredModel.builder().modelFile(model).nextModel()
                    .modelFile(model).rotationY(90).nextModel().modelFile(model).rotationY(180).nextModel().modelFile(model).rotationY(270).build());
        }
    }

    public void pileBlock(PileBlock block, ResourceLocation texture, String baseModelPath) {
        for(int i = 1; i <= 8; i++) {
            ModelFile model = models().withExistingParent(name(block) + "_" + i, resource(baseModelPath + "_" + i))
                    .texture("all", texture).texture("particle", texture);
            getVariantBuilder(block).partialState().with(PileBlock.COUNT, i).with(PileBlock.AXIS, Direction.Axis.Z).modelForState().modelFile(model).addModel()
                    .partialState().with(PileBlock.COUNT, i).with(PileBlock.AXIS, Direction.Axis.X).modelForState().modelFile(model).rotationY(90).addModel();
        }
    }

    public void campfireBlock(CampfireBlockNF campfire) {
        ModelFile campfire4 = models().getExistingFile(resource("campfire_4"));
        ModelFile campfire3 = models().getExistingFile(resource("campfire_3"));
        ModelFile campfire2 = models().getExistingFile(resource("campfire_2"));
        ModelFile campfire1 = models().getExistingFile(resource("campfire_1"));
        ModelFile campfire0 = models().getExistingFile(resource("campfire_0"));
        ModelFile sticks = models().getExistingFile(resource("campfire_sticks"));
        ModelFile fire = models().getExistingFile(resource("campfire_fire"));
        getMultipartBuilder(campfire).part().modelFile(campfire4).rotationY(90).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.X).condition(CampfireBlockNF.FIREWOOD, 4).end()
                .part().modelFile(campfire4).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.Z).condition(CampfireBlockNF.FIREWOOD, 4).end()
                .part().modelFile(campfire3).rotationY(90).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.X).condition(CampfireBlockNF.FIREWOOD, 3).end()
                .part().modelFile(campfire3).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.Z).condition(CampfireBlockNF.FIREWOOD, 3).end()
                .part().modelFile(campfire2).rotationY(90).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.X).condition(CampfireBlockNF.FIREWOOD, 2).end()
                .part().modelFile(campfire2).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.Z).condition(CampfireBlockNF.FIREWOOD, 2).end()
                .part().modelFile(campfire1).rotationY(90).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.X).condition(CampfireBlockNF.FIREWOOD, 1).end()
                .part().modelFile(campfire1).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.Z).condition(CampfireBlockNF.FIREWOOD, 1).end()
                .part().modelFile(campfire0).rotationY(90).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.X).condition(CampfireBlockNF.FIREWOOD, 0).end()
                .part().modelFile(campfire0).addModel()
                .condition(CampfireBlockNF.AXIS, Direction.Axis.Z).condition(CampfireBlockNF.FIREWOOD, 0).end()
                .part().modelFile(sticks).rotationY(90).addModel()
                .condition(CampfireBlockNF.HAS_FOOD, true).condition(CampfireBlockNF.AXIS, Direction.Axis.X).end()
                .part().modelFile(sticks).addModel()
                .condition(CampfireBlockNF.HAS_FOOD, true).condition(CampfireBlockNF.AXIS, Direction.Axis.Z).end()
                .part().modelFile(fire).addModel().condition(CampfireBlockNF.LIT, true).end();
    }

    public void cauldronBlock(CauldronBlockNF cauldron) {
        ModelFile done = templateModel(name(cauldron) + "_done", resource("cauldron_template_done"), Pair.of("all", resource(cauldron)),
                Pair.of("particle", resource(BlocksNF.TERRACOTTA.get())));
        ModelFile cook = templateModel(name(cauldron) + "_cook", resource("cauldron_template_cook"), Pair.of("all", resource(cauldron)),
                Pair.of("particle", resource(BlocksNF.TERRACOTTA.get())));
        ModelFile idle = templateModel(cauldron, resource("cauldron_template"), Pair.of("all", resource(cauldron)),
                Pair.of("particle", resource(BlocksNF.TERRACOTTA.get())));
        ModelFile support = templateModel(name(cauldron) + "_support", resource("cauldron_template_support"),
                Pair.of("all", resource("campfire_sticks")));
        getMultipartBuilder(cauldron).part().modelFile(done).rotationY(90).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.X).condition(CauldronBlockNF.TASK, Task.DONE).end()
                .part().modelFile(done).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.Z).condition(CauldronBlockNF.TASK, Task.DONE).end()
                .part().modelFile(cook).rotationY(90).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.X).condition(CauldronBlockNF.TASK, Task.COOK).end()
                .part().modelFile(cook).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.Z).condition(CauldronBlockNF.TASK, Task.COOK).end()
                .part().modelFile(idle).rotationY(90).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.X).condition(CauldronBlockNF.TASK, Task.IDLE).end()
                .part().modelFile(idle).addModel()
                .condition(CauldronBlockNF.AXIS, Direction.Axis.Z).condition(CauldronBlockNF.TASK, Task.IDLE).end()
                .part().modelFile(support).rotationY(90).addModel()
                .condition(CauldronBlockNF.SUPPORT, true).condition(CauldronBlockNF.AXIS, Direction.Axis.X).end()
                .part().modelFile(support).addModel()
                .condition(CauldronBlockNF.SUPPORT, true).condition(CauldronBlockNF.AXIS, Direction.Axis.Z).end();
    }

    public void overlayBurrowBlock(OverlayBurrowBlock burrow) {
        getVariantBuilder(burrow).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(models().withExistingParent(name(burrow) + (state.getValue(OverlayBurrowBlock.FACING) == Direction.DOWN ? "_down" : ""), resource("single_face"))
                        .texture("0", state.getValue(OverlayBurrowBlock.FACING) == Direction.DOWN ? resource(burrow, "_down") : resource(burrow))
                        .texture("particle", resource("empty")))
                .rotationX(state.getValue(OverlayBurrowBlock.FACING) == Direction.DOWN ? 90 : 0)
                .rotationY(((int) state.getValue(OverlayBurrowBlock.FACING).toYRot() + 180) % 360)
                .build());
    }

    public void singleFaceBlock(Block block, Direction facing) {
        getVariantBuilder(block).partialState().modelForState().modelFile(models().withExistingParent(name(block), resource("single_face"))
                .texture("0", resource(block)).texture("particle", resource("empty")))
                .rotationX(facing == Direction.DOWN ? 90 : 0).rotationY(((int) facing.toYRot() + 180) % 360).addModel();
    }

    public void tilledSoilBlock(Block block, ResourceLocation side) {
        getVariantBuilder(block)
                .partialState().with(BlockStatePropertiesNF.HUMIDITY, Humidity.DRY)
                .modelForState().modelFile(models().cubeTop(name(block) + "_dry", side, resource(block, "_dry"))).addModel()
                .partialState().with(BlockStatePropertiesNF.HUMIDITY, Humidity.MOIST)
                .modelForState().modelFile(models().cubeTop(name(block) + "_moist", side, resource(block, "_moist"))).addModel()
                .partialState().with(BlockStatePropertiesNF.HUMIDITY, Humidity.IRRIGATED)
                .modelForState().modelFile(models().cubeTop(name(block) + "_irrigated", side, resource(block, "_irrigated"))).addModel();
    }

    public void stagedCrossBlock(Block block, IntegerProperty property) {
        for(Integer i : property.getPossibleValues()) {
            getVariantBuilder(block).partialState().with(property, i).modelForState()
                    .modelFile(models().withExistingParent(name(block) + "_" + i, "cross")
                            .texture("cross", resource(block, "_" + i))).addModel();
        }
    }

    public void stagedTintedCrossBlock(Block block, IntegerProperty property) {
        for(Integer i : property.getPossibleValues()) {
            getVariantBuilder(block).partialState().with(property, i).modelForState()
                    .modelFile(models().withExistingParent(name(block) + "_" + i, resource("tinted_cross"))
                            .texture("base", resource(block, "_" + i)).texture("overlay", resource(block, "_" + i + "_overlay"))).addModel();
        }
    }

    public enum Tint {
        NONE, FULL, PART
    }

    public void cropBlock(Block block, Tint[] stageTints, int... texStages) {
        for(int i = 0; i < 8; i++) {
            switch(stageTints[texStages[i] - 1]) {
                case NONE -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], resource("crop"))
                                .texture("all", resource(block, "_" + texStages[i]))).addModel();
                case FULL -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], resource("full_tinted_crop"))
                                .texture("all", resource(block, "_" + texStages[i]))).addModel();
                case PART -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], resource("tinted_crop"))
                                .texture("base", resource(block, "_" + texStages[i])).texture("overlay", resource(block, "_" + texStages[i] + "_overlay"))).addModel();
            }
        }
    }

    public void crossCropBlock(Block block, Tint[] stageTints, int... texStages) {
        for(int i = 0; i < 8; i++) {
            switch(stageTints[texStages[i] - 1]) {
                case NONE -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], "cross")
                                .texture("cross", resource(block, "_" + texStages[i]))).addModel();
                case FULL -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], "tinted_cross")
                                .texture("cross", resource(block, "_" + texStages[i]))).addModel();
                case PART -> getVariantBuilder(block).partialState().with(BlockStatePropertiesNF.STAGE_8, i + 1).modelForState()
                        .modelFile(models().withExistingParent(name(block) + "_" + texStages[i], resource("tinted_cross"))
                                .texture("base", resource(block, "_" + texStages[i])).texture("overlay", resource(block, "_" + texStages[i] + "_overlay"))).addModel();
            }
        }
    }

    private void fireBlock(Block fire) {
        ModelFile floor0 = templateModel(name(fire) + "_floor_0", mcLoc("template_fire_floor"), Pair.of("fire", resource(fire, "_0")));
        ModelFile floor1 = templateModel(name(fire) + "_floor_1", mcLoc("template_fire_floor"), Pair.of("fire", resource(fire, "_1")));
        ModelFile side0 = templateModel(name(fire) + "_side_0", mcLoc("template_fire_side"), Pair.of("fire", resource(fire, "_0")));
        ModelFile side1 = templateModel(name(fire) + "_side_1", mcLoc("template_fire_side"), Pair.of("fire", resource(fire, "_1")));
        ModelFile side_alt0 = templateModel(name(fire) + "_side_alt_0", mcLoc("template_fire_side_alt"), Pair.of("fire", resource(fire, "_0")));
        ModelFile side_alt1 = templateModel(name(fire) + "_side_alt_1", mcLoc("template_fire_side_alt"), Pair.of("fire", resource(fire, "_1")));
        ModelFile up0 = templateModel(name(fire) + "_up_0", mcLoc("template_fire_up"), Pair.of("fire", resource(fire, "_0")));
        ModelFile up1 = templateModel(name(fire) + "_up_1", mcLoc("template_fire_up"), Pair.of("fire", resource(fire, "_1")));
        ModelFile up_alt0 = templateModel(name(fire) + "_up_alt_0", mcLoc("template_fire_up_alt"), Pair.of("fire", resource(fire, "_0")));
        ModelFile up_alt1 = templateModel(name(fire) + "_up_alt_1", mcLoc("template_fire_up_alt"), Pair.of("fire", resource(fire, "_1")));
        getMultipartBuilder(fire)
                .part().modelFile(floor0).nextModel().modelFile(floor1).addModel()
                .condition(FireBlock.NORTH, false).condition(FireBlock.EAST, false).condition(FireBlock.SOUTH, false)
                .condition(FireBlock.WEST, false).condition(FireBlock.UP, false).end()
                .part().modelFile(side0).nextModel().modelFile(side1).nextModel().modelFile(side_alt0).nextModel().modelFile(side_alt1).addModel()
                .useOr().nestedGroup().condition(FireBlock.NORTH, true).end()
                .nestedGroup().condition(FireBlock.NORTH, false).condition(FireBlock.EAST, false).condition(FireBlock.SOUTH, false)
                .condition(FireBlock.WEST, false).condition(FireBlock.UP, false).end().end()
                .part().modelFile(side0).rotationY(90).nextModel().modelFile(side1).rotationY(90).nextModel()
                .modelFile(side_alt0).rotationY(90).nextModel().modelFile(side_alt1).rotationY(90).addModel()
                .useOr().nestedGroup().condition(FireBlock.EAST, true).end()
                .nestedGroup().condition(FireBlock.NORTH, false).condition(FireBlock.EAST, false).condition(FireBlock.SOUTH, false)
                .condition(FireBlock.WEST, false).condition(FireBlock.UP, false).end().end()
                .part().modelFile(side0).rotationY(180).nextModel().modelFile(side1).rotationY(180).nextModel()
                .modelFile(side_alt0).rotationY(180).nextModel().modelFile(side_alt1).rotationY(180).addModel()
                .useOr().nestedGroup().condition(FireBlock.SOUTH, true).end()
                .nestedGroup().condition(FireBlock.NORTH, false).condition(FireBlock.EAST, false).condition(FireBlock.SOUTH, false)
                .condition(FireBlock.WEST, false).condition(FireBlock.UP, false).end().end()
                .part().modelFile(side0).rotationY(270).nextModel().modelFile(side1).rotationY(270).nextModel()
                .modelFile(side_alt0).rotationY(270).nextModel().modelFile(side_alt1).rotationY(270).addModel()
                .useOr().nestedGroup().condition(FireBlock.WEST, true).end()
                .nestedGroup().condition(FireBlock.NORTH, false).condition(FireBlock.EAST, false).condition(FireBlock.SOUTH, false)
                .condition(FireBlock.WEST, false).condition(FireBlock.UP, false).end().end()
                .part().modelFile(up0).nextModel().modelFile(up1).nextModel().modelFile(up_alt0).nextModel().modelFile(up_alt1).addModel()
                .condition(FireBlock.UP, true).end();
    }

    @Override
    protected void registerStatesAndModels() {
        randomRotatedBlock(BlocksNF.SILT.get());
        randomRotatedBlock(BlocksNF.DIRT.get());
        randomRotatedBlock(BlocksNF.LOAM.get());
        randomRotatedBlock(BlocksNF.ASH.get());
        randomRotatedBlock(BlocksNF.GRAVEL.get());
        randomRotatedBlock(BlocksNF.BLUE_GRAVEL.get());
        randomRotatedBlock(BlocksNF.BLACK_GRAVEL.get());
        for(var block : BlocksNF.getCoveredSoils()) {
            SoilCover cover = block.get().soilCover;
            snowyTintedSoilBlock(block.get(), block.getId().getPath().toString().replace(cover.prefix + "_", ""), cover.prefix + "_top");
        }
        tilledSoilBlock(BlocksNF.TILLED_SILT.get(), resource(BlocksNF.SILT.get()));
        tilledSoilBlock(BlocksNF.TILLED_DIRT.get(), resource(BlocksNF.DIRT.get()));
        tilledSoilBlock(BlocksNF.TILLED_LOAM.get(), resource(BlocksNF.LOAM.get()));
        randomRotatedBlock(BlocksNF.SNOW.get(), templateModel(BlocksNF.SNOW.get(), mcLoc("snow_height2"),
                Pair.of("texture", resource(BlocksNF.SNOW.get())), Pair.of("particle", resource(BlocksNF.SNOW.get()))));
        randomRotatedBlock(BlocksNF.PACKED_SNOW.get(), models().cubeAll(name(BlocksNF.PACKED_SNOW.get()), resource(BlocksNF.SNOW.get())));
        randomRotatedBlock(BlocksNF.MUD.get());
        randomRotatedBlock(BlocksNF.CLAY.get());
        randomRotatedBlock(BlocksNF.FIRE_CLAY.get());
        fireBlock(BlocksNF.FIRE.get());
        particleOnlyBlock(BlocksNF.WATER.get(), resource("water_still"));
        particleOnlyBlock(BlocksNF.SEAWATER.get(), resource("seawater_still"));
        particleOnlyBlock(BlocksNF.LAVA.get(), resource("lava_still"));
        randomRotatedBlock(BlocksNF.FRAZIL.get(), templateModel(BlocksNF.FRAZIL.get(), resource("sunk_face"),
                Pair.of("0", resource(BlocksNF.FRAZIL.get())), Pair.of("particle", resource(BlocksNF.FRAZIL.get()))));
        randomRotatedBlock(BlocksNF.SEA_FRAZIL.get(), templateModel(BlocksNF.SEA_FRAZIL.get(), resource("sunk_face"),
                Pair.of("0", resource(BlocksNF.SEA_FRAZIL.get())), Pair.of("particle", resource(BlocksNF.SEA_FRAZIL.get()))));
        groundItemBlock(BlocksNF.SEASHELL.get(), 4);
        tallTintedCrossBlock(BlocksNF.TALL_GRASS.get());
        vinesBlock(BlocksNF.VINES.get());
        crossBlock(BlocksNF.DEAD_BUSH.get());
        crossBlock(BlocksNF.DEAD_PLANT.get());
        cropBlock(BlocksNF.DEAD_CROP.get(), new Tint[]{Tint.NONE, Tint.NONE, Tint.NONE, Tint.NONE}, 1, 1, 2, 2, 3, 3, 3, 4);
        cropBlock(BlocksNF.POTATOES.get(), new Tint[]{Tint.FULL, Tint.FULL, Tint.FULL, Tint.PART}, 1, 1, 2, 2, 3, 3, 3, 4);
        cropBlock(BlocksNF.CARROTS.get(), new Tint[]{Tint.FULL, Tint.FULL, Tint.PART, Tint.PART}, 1, 1, 2, 2, 3, 3, 3, 4);
        cropBlock(BlocksNF.FLAX.get(), new Tint[]{Tint.FULL, Tint.FULL, Tint.PART, Tint.PART, Tint.NONE, Tint.NONE}, 1, 2, 3, 4, 4, 5, 5, 6);
        crossCropBlock(BlocksNF.YARROW.get(), new Tint[]{Tint.FULL, Tint.PART, Tint.PART}, 1, 1, 1, 2, 2, 2, 2, 3);
        stagedTintedCrossBlock(BlocksNF.BERRY_BUSH.get(), BlockStatePropertiesNF.STAGE_4);
        for(Stone type : Stone.values()) {
            if(type == Stone.PUMICE) {
                mirroredBlock(BlocksNF.STONE_BLOCKS.get(type).get());
            }
            else {
                List<RegistryObject<Block>> ores;
                if(type == Stone.MOONSTONE) ores = List.of(BlocksNF.METEORITE_ORE);
                else {
                    ores = new ObjectArrayList<>();
                    if(BlocksNF.TIN_ORES.containsKey(type)) ores.add(BlocksNF.TIN_ORES.get(type));
                    if(BlocksNF.COPPER_ORES.containsKey(type)) ores.add(BlocksNF.COPPER_ORES.get(type));
                    if(BlocksNF.AZURITE_ORES.containsKey(type)) ores.add(BlocksNF.AZURITE_ORES.get(type));
                    if(BlocksNF.HEMATITE_ORES.containsKey(type)) ores.add(BlocksNF.HEMATITE_ORES.get(type));
                    if(BlocksNF.COAL_ORES.containsKey(type)) ores.add(BlocksNF.COAL_ORES.get(type));
                    if(BlocksNF.HALITE_ORES.containsKey(type)) ores.add(BlocksNF.HALITE_ORES.get(type));
                    if(BlocksNF.SULFUR_ORES.containsKey(type)) ores.add(BlocksNF.SULFUR_ORES.get(type));
                }
                if(type == Stone.SLATE || type == Stone.DEEPSLATE || type == Stone.SANDSTONE || type == Stone.SUNSCHIST || type == Stone.AURGROT) {
                    mirroredColumnBlock(BlocksNF.STONE_BLOCKS.get(type).get());
                    for(var ore : ores) mirroredColumnBlock(ore.get());
                }
                else {
                    mirroredBlock(BlocksNF.STONE_BLOCKS.get(type).get());
                    for(var ore : ores) mirroredBlock(ore.get());
                }
            }
            clusterBlock(BlocksNF.ROCK_CLUSTERS.get(type).get(), blockTexture(BlocksNF.STONE_BLOCKS.get(type).get()), "rock");
        }
        clusterBlock(BlocksNF.FLINT_CLUSTER.get(), resource("flint"), "rock");
        logBlock(BlocksNF.CHARRED_LOG.get());
        for(Tree type : Tree.values()) {
            logBlock(BlocksNF.LOGS.get(type).get());
            stemBlock(BlocksNF.STEMS.get(type).get(), BlocksNF.LOGS.get(type).get());
            logBlock(BlocksNF.STRIPPED_LOGS.get(type).get());
            if(type == Tree.PALM) rotatedLeavesBlock(BlocksNF.LEAVES.get(type).get());
            else crossLeavesBlock(BlocksNF.LEAVES.get(type).get());
            trunkBlock(BlocksNF.TRUNKS.get(type).get(), BlocksNF.LOGS.get(type).get());
            if(type.isDeciduous()) deciduousSaplingBlock(BlocksNF.TREE_SEEDS.get(type).get());
            else evergreenSaplingBlock(BlocksNF.TREE_SEEDS.get(type).get());
            fourWayBlock(BlocksNF.PLANK_FENCES.get(type).get(),
                    templateModel(BlocksNF.PLANK_FENCES.get(type).getId().getPath() + "_post", resource("fence_post"), Pair.of("0", resource(BlocksNF.PLANK_FENCES.get(type).get()))),
                    templateModel(BlocksNF.PLANK_FENCES.get(type).getId().getPath() + "_side", resource("fence_side"), Pair.of("0", resource(BlocksNF.PLANK_FENCES.get(type).get()))));
            fenceGateBlock(BlocksNF.PLANK_FENCE_GATES.get(type).get(), resource(BlocksNF.PLANK_FENCES.get(type).get()));
            doorBlock(BlocksNF.PLANK_DOORS.get(type).get());
            hatchBlock(BlocksNF.PLANK_HATCHES.get(type).get());
            trapdoorBlock(BlocksNF.PLANK_TRAPDOORS.get(type).get());
            ladderBlock(BlocksNF.PLANK_LADDERS.get(type).get());
            particleOnlyBlock(BlocksNF.PLANK_STANDING_SIGNS.get(type).get(), resource(BlocksNF.PLANK_BLOCKS.get(type).get()));
            particleOnlyBlock(BlocksNF.PLANK_WALL_SIGNS.get(type).get(), resource(BlocksNF.PLANK_BLOCKS.get(type).get()));
            directionalBlockNF(BlocksNF.WOODEN_ITEM_FRAMES.get(type).get(), templateModel(BlocksNF.WOODEN_ITEM_FRAMES.get(type).get(), resource("item_frame"),
                    Pair.of("0", resource(BlocksNF.WOODEN_ITEM_FRAMES.get(type).get()))), 0);
            barrelBlock(BlocksNF.BARRELS.get(type).get());
            chestBlock(BlocksNF.CHESTS.get(type).get(), resource(BlocksNF.PLANK_BLOCKS.get(type).get()));
            horizontalBlockNF(BlocksNF.RACKS.get(type).get(),
                    templateModel(BlocksNF.RACKS.get(type).get(), resource("rack"), Pair.of("all", resource(BlocksNF.PLANK_BLOCKS.get(type).get()))),
                    0);
            horizontalBlockNF(BlocksNF.SHELVES.get(type).get(),
                    templateModel(BlocksNF.SHELVES.get(type).get(), resource("shelf_simple"), Pair.of("all", resource(BlocksNF.SHELVES.get(type).get()))),
                    0);
            chairBlock(BlocksNF.CHAIRS.get(type).get(), resource(BlocksNF.PLANK_BLOCKS.get(type).get()));
            troughBlock(BlocksNF.TROUGHS.get(type).get(), resource(BlocksNF.PLANK_BLOCKS.get(type).get()));
        }
        for(Tree type : BlocksNF.FRUIT_LEAVES.keySet()) {
            if(type.isDeciduous()) tintedFruitLeavesBlock(BlocksNF.LEAVES.get(type).get(), BlocksNF.FRUIT_LEAVES.get(type).get());
            else if(type == Tree.PALM) rotatedFruitLeavesBlock(BlocksNF.LEAVES.get(type).get(), BlocksNF.FRUIT_LEAVES.get(type).get());
            else fruitLeavesBlock(BlocksNF.LEAVES.get(type).get(), BlocksNF.FRUIT_LEAVES.get(type).get());
        }
        for(Tree type : BlocksNF.BRANCHES.keySet()) crossLeavesBlock(BlocksNF.BRANCHES.get(type).get());
        simpleBlock(BlocksNF.GLASS_BLOCK.get());
        ResourceLocation glass = resource(BlocksNF.GLASS_BLOCK.get());
        slabBlock(BlocksNF.GLASS_SLAB.get(), glass, resource(BlocksNF.GLASS_SLAB.get()), glass, glass);
        ResourceLocation glassSiding = resource(BlocksNF.GLASS_SIDING.get());
        ResourceLocation glassQuartet = resource("glass_quartet");
        ResourceLocation glassInnerTop = resource("glass_siding_inner_top");
        sidingBlock(BlocksNF.GLASS_SIDING.get(), glass, glass, glassSiding, glassSiding, glassSiding, glassQuartet, glassQuartet,
                glassInnerTop, glassInnerTop, resource("glass_siding_inner_side"), false);

        templateBlock(BlocksNF.TORCH.get(), mcLoc("template_torch"), Pair.of("torch", resource(BlocksNF.TORCH.get())));
        templateBlock(BlocksNF.TORCH_UNLIT.get(), mcLoc("template_torch"), Pair.of("torch", resource(BlocksNF.TORCH_UNLIT.get())));
        wallTorchBlock(BlocksNF.WALL_TORCH.get(), BlocksNF.TORCH.get());
        wallTorchBlock(BlocksNF.WALL_TORCH_UNLIT.get(), BlocksNF.TORCH_UNLIT.get());
        templateBlock(BlocksNF.WOODEN_BOWL.get(), resource("bowl"), Pair.of("all", resource(BlocksNF.WOODEN_BOWL.get())),
                Pair.of("particle", resource(BlocksNF.WOODEN_BOWL.get())));
        campfireBlock(BlocksNF.CAMPFIRE.get());
        cauldronBlock(BlocksNF.CAULDRON.get());
        unfiredAxisPotteryBlock(BlocksNF.UNFIRED_CAULDRON.get());
        templateBlock(BlocksNF.POT.get(), resource("pot_template"), Pair.of("all", resource(BlocksNF.POT.get())), Pair.of("particle", resource(BlocksNF.TERRACOTTA.get())));
        unfiredPotteryBlock(BlocksNF.UNFIRED_POT.get());
        horizontalAxisBlock(BlocksNF.WARDING_EFFIGY.get(), file(BlocksNF.WARDING_EFFIGY.get()));

        for(var block : BlocksNF.METAL_BARS.values()) barsBlock(block.get());
        for(var block : BlocksNF.INGOT_PILES.values()) horizontalColumnBlock(block.get());
        horizontalColumnBottomTopBlock(BlocksNF.STEEL_INGOT_PILE_POOR.get(), resource(BlocksNF.INGOT_PILES.get(Metal.IRON).get()),
                resource(BlocksNF.INGOT_PILES.get(Metal.STEEL).get()));
        horizontalColumnBottomTopBlock(BlocksNF.STEEL_INGOT_PILE_FAIR.get(), resource(BlocksNF.INGOT_PILES.get(Metal.IRON).get()),
                resource(BlocksNF.INGOT_PILES.get(Metal.STEEL).get()));
        for(var block : BlocksNF.LANTERNS.values()) lanternBlock(block.get());
        for(var block : BlocksNF.LANTERNS_UNLIT.values()) lanternBlock(block.get());
        for(Stone type : BlocksNF.ANVILS_STONE.keySet()) {
            horizontalBlockNF(BlocksNF.ANVILS_STONE.get(type).get(), templateModel(BlocksNF.ANVILS_STONE.get(type).get(), resource("anvil_simple"), Pair.of("side", resource(BlocksNF.STONE_BLOCKS.get(type).get())),
                    Pair.of("top", resource(BlocksNF.POLISHED_STONE.get(type).get())), Pair.of("bottom", resource(BlocksNF.STONE_BLOCKS.get(type).get()))), 0);
        }
        for(var block : BlocksNF.ANVILS_METAL.values()) anvilBlock(block.get());
        for(var block : BlocksNF.ARMAMENT_MOLDS.values()) horizontalBlockNF(block.get(), file(block.get()), 0);
        horizontalBlockNF(BlocksNF.INGOT_MOLD.get(), file(BlocksNF.INGOT_MOLD.get()), 0);
        horizontalBlockNF(BlocksNF.ARROWHEAD_MOLD.get(), file(BlocksNF.ARROWHEAD_MOLD.get()), 0);
        horizontalBlockNF(BlocksNF.ROD_MOLD.get(), file(BlocksNF.ROD_MOLD.get()), 0);
        for(Armament type : BlocksNF.ARMAMENT_MOLDS.keySet()) unfiredMoldBlock(BlocksNF.UNFIRED_ARMAMENT_MOLDS.get(type).get(), BlocksNF.ARMAMENT_MOLDS.get(type).get());
        unfiredMoldBlock(BlocksNF.UNFIRED_INGOT_MOLD.get(), BlocksNF.INGOT_MOLD.get());
        unfiredMoldBlock(BlocksNF.UNFIRED_ARROWHEAD_MOLD.get(), BlocksNF.ARROWHEAD_MOLD.get());
        unfiredMoldBlock(BlocksNF.UNFIRED_ROD_MOLD.get(), BlocksNF.ROD_MOLD.get());
        simpleHeatableBlock(BlocksNF.COKE_BURNING.get());
        simpleHeatableBlock(BlocksNF.COAL_BURNING.get());
        horizontalColumnBlock(BlocksNF.CHARCOAL.get());
        horizontalHeatableBlock(BlocksNF.CHARCOAL_BURNING.get());
        horizontalColumnBlock(BlocksNF.FIREWOOD.get());
        horizontalHeatableBlock(BlocksNF.FIREWOOD_BURNING.get());
        crucibleBlock(BlocksNF.CRUCIBLE.get());
        unfiredAxisPotteryBlock(BlocksNF.UNFIRED_CRUCIBLE.get());
        overlayBurrowBlock(BlocksNF.RABBIT_BURROW.get());
        multifaceBlock(BlocksNF.SPIDER_WEB.get(), templateModel(BlocksNF.SPIDER_WEB.get(), resource("cover"),
                Pair.of("0", resource(BlocksNF.SPIDER_WEB.get())), Pair.of("particle", resource(BlocksNF.SPIDER_WEB.get()))));
        columnBlock(BlocksNF.SPIDER_NEST.get());
        cubeTopBlock(BlocksNF.ANCHORING_RESIN.get());
        for(var type : BlocksNF.STONE_TUNNELS.keySet()) axisBlock(BlocksNF.STONE_TUNNELS.get(type).get(),
                resource(BlocksNF.STONE_BLOCKS.get(type).get()), resource(BlocksNF.STONE_TUNNELS.get(type).get()));
        for(var type : BlocksNF.SKARA_ROCK_CLUSTERS.keySet()) skaraNestBlock(BlocksNF.SKARA_ROCK_CLUSTERS.get(type).get(), blockTexture(BlocksNF.STONE_BLOCKS.get(type).get()), "rock");
        eggNestBlock(BlocksNF.DRAKEFOWL_NEST.get());

        //Note that generated models used in generation of others will still need to be manually created before
        for(RegistryObject<? extends Block> block : BlocksNF.BLOCKS.getEntries()) {
            if(!registeredBlocks.containsKey(block.get())) {
                if(Files.exists(getExternalImagePath(extendWithFolder(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, block.getId().getPath()))))) {
                    fileBlock(block.get());
                }
                else if(block.get() instanceof StairBlockNF stair) stairBlock(stair, resource(stair.base));
                else if(block.get() instanceof SlabBlockNF slab) slabBlock(slab, resource(slab.base));
                else if(block.get() instanceof SidingBlock siding) sidingBlock(siding, resource(siding.base));
                else simpleBlock(block.get());
            }
        }
    }
}
