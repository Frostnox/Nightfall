package frostnox.nightfall.block.block.meltedmetal;

import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.fluid.MeltedMetalFluid;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.Vec2f;
import frostnox.nightfall.util.math.AxisDirection;
import it.unimi.dsi.fastutil.objects.*;
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
import java.util.Map;

public class MeltedMetalBlockEntity extends BlockEntity {
    public float temperature = TieredHeat.ORANGE.getUpperTemp(), targetTemperature;
    public BlockState originalState = BlocksNF.METAL_BLOCKS.get(Metal.COPPER).get().defaultBlockState();
    public IMetal metal = Metal.COPPER;
    public int units = 400, alloyTimer = 20 * 5;
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
            if(level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity metal && metal.metal == this.metal) {
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
                if(metal.hasSlag) metal.level.setBlockAndUpdate(metal.getBlockPos(), BlocksNF.SLAG.get().defaultBlockState());
                else metal.level.removeBlock(metal.getBlockPos(), false);
            }
            metal.untouched = false;
            metal.alloyTimer = 20 * 5;
            metal.setChanged();
        }
    }

    public void alloy() {
        List<MeltedMetalBlockEntity> entities = new ObjectArrayList<>(LevelUtil.MAX_BLAST_FURNACE_AREA);
        Map<IMetal, Integer> unitsMap = new Object2IntArrayMap<>(4);
        ObjectArrayFIFOQueue<BlockPos> positions = new ObjectArrayFIFOQueue<>(32);
        positions.enqueue(getBlockPos());
        ObjectArraySet<BlockPos> visited = new ObjectArraySet<>();
        while(!positions.isEmpty() && entities.size() < 64) {
            BlockPos pos = positions.dequeue();
            if(visited.contains(pos)) continue;
            else visited.add(pos);
            if(level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity entity) {
                entities.add(entity);
                if(!unitsMap.containsKey(entity.metal)) unitsMap.put(entity.metal, entity.units);
                else unitsMap.put(entity.metal, entity.units + unitsMap.get(entity.metal));
                for(AxisDirection dir : AxisDirection.XZ) positions.enqueue(pos.offset(dir.x, dir.y, dir.z));
            }
        }
        int totalUnits = 0;
        for(int units : unitsMap.values()) totalUnits += units;

        IMetal.Entry alloyMetal = null;
        for(IMetal.Entry metalType : RegistriesNF.getMetals()) {
            Map<IMetal, Vec2f> bases = metalType.value.getBaseMetals();
            if(unitsMap.size() != bases.size()) continue;
            boolean alloyed = true;
            for(IMetal key : unitsMap.keySet()) {
                if(!bases.containsKey(key)) {
                    alloyed = false;
                    break;
                }
                float part = ((float) unitsMap.get(key)) / totalUnits;
                Vec2f range = bases.get(key);
                if(part < range.x() || part > range.y()) {
                    alloyed = false;
                    break;
                }
            }
            if(alloyed) {
                alloyMetal = metalType;
                break;
            }
        }

        if(alloyMetal != null) for(MeltedMetalBlockEntity entity : entities) {
            entity.metal = alloyMetal.value;
            entity.untouched = false;
            entity.alloyTimer = 0;
            entity.setChanged();
        }
        else for(MeltedMetalBlockEntity entity : entities) { //Stop other metals in this pool from trying to alloy again without any changes
            entity.alloyTimer = 0;
            entity.setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeltedMetalBlockEntity entity) {
        float targetTemp = entity.targetTemperature + LevelUtil.getRainTempPenalty(level, pos);
        if(entity.temperature != targetTemp) {
            if(entity.temperature > targetTemp) entity.temperature = Math.max(entity.temperature - 0.05F, targetTemp);
            else entity.temperature = Math.min(entity.temperature + 0.05F, targetTemp);
            if(entity.temperature < entity.metal.getMeltTemp()) {
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
                entity.setChanged();
            }
        }
        if(entity.alloyTimer > 0) {
            if(entity.alloyTimer == 1) entity.alloy();
            else {
                entity.alloyTimer--;
                entity.setChanged();
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        temperature = tag.getFloat("temperature");
        targetTemperature = tag.getFloat("targetTemperature");
        originalState = MeltedMetalBlock.stateById(tag.getInt("originalState"));
        IMetal.Entry metalEntry = RegistriesNF.getMetals().getValue(ResourceLocation.parse(tag.getString("metal")));
        if(metalEntry != null) this.metal = metalEntry.value;
        units = tag.getInt("units");
        alloyTimer = tag.getInt("alloyTimer");
        hasSlag = tag.getBoolean("hasSlag");
        untouched = tag.getBoolean("untouched");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("temperature", temperature);
        tag.putFloat("targetTemperature", targetTemperature);
        tag.putInt("originalState", MeltedMetalBlock.getId(originalState));
        tag.putString("metal", metal.getId().toString());
        tag.putInt("units", units);
        tag.putInt("alloyTimer", alloyTimer);
        tag.putBoolean("hasSlag", hasSlag);
        tag.putBoolean("untouched", untouched);
    }
}
