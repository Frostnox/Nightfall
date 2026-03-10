package frostnox.nightfall.block.block.meltedmetal;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.AxisDirection;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MeltedMetalBlockEntity extends BlockEntity {
    public float temperature = TieredHeat.YELLOW.getBaseTemp() - 1;
    public BlockState originalState = BlocksNF.METAL_BLOCKS.get(Metal.COPPER).get().defaultBlockState();
    public IMetal.Entry metal = RegistriesNF.getMetals().getValue(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, Metal.COPPER.getName()));
    public int units = 400;

    public MeltedMetalBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(BlockEntitiesNF.MELTED_METAL.get(), pPos, pBlockState);
    }

    protected MeltedMetalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void drain() {
        units--;
        ObjectArraySet<BlockPos> visited = new ObjectArraySet<>(LevelUtil.MAX_BLAST_FURNACE_AREA * 2);
        visited.add(getBlockPos());
        BlockPos.MutableBlockPos nPos = new BlockPos.MutableBlockPos();
        MeltedMetalBlockEntity metal = this;
        int highestDist = 0;
        while(true) {
            BlockPos pos = metal.getBlockPos();
            int highestUnits = 0;
            MeltedMetalBlockEntity highestNeighbor = null;
            for(AxisDirection dir : AxisDirection.YPXZ) {
                if(visited.contains(nPos.set(pos.getX() + dir.x, pos.getY() + dir.y, pos.getZ() + dir.z))) continue;
                if(level.getBlockEntity(nPos) instanceof MeltedMetalBlockEntity neighbor && neighbor.units > metal.units) {
                    visited.add(nPos.immutable());
                    if(neighbor.units > highestUnits) {
                        int dist = neighbor.getBlockPos().distManhattan(getBlockPos());
                        if(dist > highestDist) {
                            highestUnits = neighbor.units;
                            highestNeighbor = neighbor;
                            highestDist = dist;
                        }
                    }
                }
            }
            if(highestUnits > 0) {
                highestNeighbor.units--;
                metal.units++;
                highestNeighbor.setChanged();
                metal.setChanged();
                metal = highestNeighbor;
            }
            else break;
        }
        setChanged();
        if(metal.units == 0) metal.level.removeBlock(metal.getBlockPos(), false);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeltedMetalBlockEntity entity) {

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        temperature = tag.getFloat("temperature");
        originalState = MeltedMetalBlock.stateById(tag.getInt("originalState"));
        metal = RegistriesNF.getMetals().getValue(ResourceLocation.parse(tag.getString("metal")));
        units = tag.getInt("units");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("temperature", temperature);
        tag.putInt("originalState", MeltedMetalBlock.getId(originalState));
        tag.putString("metal", metal.getRegistryName().toString());
        tag.putInt("units", units);
    }
}
