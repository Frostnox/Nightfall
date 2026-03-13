package frostnox.nightfall.block.block.meltedmetal;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.fluid.MeltedMetalFluid;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.math.AxisDirection;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;

public class MeltedMetalBlockEntity extends BlockEntity {
    public float temperature = TieredHeat.ORANGE.getUpperTemp(), targetTemperature;
    public BlockState originalState = BlocksNF.METAL_BLOCKS.get(Metal.COPPER).get().defaultBlockState();
    public IMetal.Entry metal = RegistriesNF.getMetals().getValue(ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, Metal.COPPER.getName()));
    public int units = 400;
    public boolean hasSlag = false, untouched = true;

    public MeltedMetalBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(BlockEntitiesNF.MELTED_METAL.get(), pPos, pBlockState);
    }

    protected MeltedMetalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
    
    public void drain(int drain) {
        units = Math.max(0, units - drain);
        int totalUnits = units;
        List<MeltedMetalBlockEntity> metals = new ObjectArrayList<>(LevelUtil.MAX_BLAST_FURNACE_AREA);
        metals.add(this);
        ObjectArrayFIFOQueue<BlockPos> positions = new ObjectArrayFIFOQueue<>(32);
        for(AxisDirection dir : AxisDirection.XZ) positions.enqueue(getBlockPos().offset(dir.x, dir.y, dir.z));
        ObjectArraySet<BlockPos> visited = new ObjectArraySet<>();
        visited.add(getBlockPos());
        while(!positions.isEmpty() && metals.size() < 64) {
            BlockPos pos = positions.dequeue();
            if(visited.contains(pos)) continue;
            else visited.add(pos);
            if(level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity metal && metal.metal.value == this.metal.value) {
                metals.add(metal);
                totalUnits += metal.units;
                for(AxisDirection dir : AxisDirection.XZ) positions.enqueue(pos.offset(dir.x, dir.y, dir.z));
            }
        }

        int base = totalUnits / metals.size();
        int remainder = totalUnits % metals.size();
        int[] unitsMap = new int[metals.size()];
        for(int i = 0; i < metals.size(); i++) unitsMap[i] = base + (i < remainder ? 1 : 0);

        metals.sort(Comparator.comparingInt((metal) -> metal.getBlockPos().distManhattan(getBlockPos())));
        for(int i = 0; i < metals.size(); i++) {
            MeltedMetalBlockEntity metal = metals.get(i);
            metal.units = unitsMap[i];
            if(metal.units == 0) {
                metal.level.removeBlock(metal.getBlockPos(), false);
            }
            metal.untouched = false;
            metal.setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeltedMetalBlockEntity entity) {
        float targetTemp = entity.targetTemperature + LevelUtil.getRainTempPenalty(level, pos);
        if(entity.temperature != targetTemp) {
            if(entity.temperature > targetTemp) entity.temperature = Math.max(entity.temperature - 0.05F, targetTemp);
            else entity.temperature = Math.min(entity.temperature + 0.05F, targetTemp);
            if(entity.temperature < entity.metal.value.getMeltTemp()) {
                if(entity.untouched) level.setBlockAndUpdate(pos, entity.originalState);
                else {
                    if(entity.hasSlag) level.setBlockAndUpdate(pos, BlocksNF.SLAG.get().defaultBlockState());
                    else {
                        MeltedMetalFluid fluid = FluidsNF.MELTED_METAL.get(TieredHeat.values()[(state.getValue(MeltedMetalBlock.HEAT))]).get();
                        level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock().setValue(LiquidBlock.LEVEL, 3), 11);
                        fluid.tick(level, pos, fluid.defaultFluidState());
                    }
                }
            }
            else {
                TieredHeat heat = TieredHeat.fromTemp(entity.temperature);
                if(heat.getTier() != state.getValue(MeltedMetalBlock.HEAT)) level.setBlockAndUpdate(pos, state.setValue(MeltedMetalBlock.HEAT, heat.getTier()));
            }
            entity.setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        temperature = tag.getFloat("temperature");
        targetTemperature = tag.getFloat("targetTemperature");
        originalState = MeltedMetalBlock.stateById(tag.getInt("originalState"));
        metal = RegistriesNF.getMetals().getValue(ResourceLocation.parse(tag.getString("metal")));
        units = tag.getInt("units");
        hasSlag = tag.getBoolean("hasSlag");
        untouched = tag.getBoolean("untouched");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("temperature", temperature);
        tag.putFloat("targetTemperature", targetTemperature);
        tag.putInt("originalState", MeltedMetalBlock.getId(originalState));
        tag.putString("metal", metal.getRegistryName().toString());
        tag.putInt("units", units);
        tag.putBoolean("hasSlag", hasSlag);
        tag.putBoolean("untouched", untouched);
    }
}
