package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.IBlockChunkLoader;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.meltedmetal.MeltedMetalBlock;
import frostnox.nightfall.block.block.meltedmetal.MeltedMetalBlockEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireableMetalBlock extends FireableBlock implements IBlockChunkLoader {
    public final @Nullable RegistryObject<? extends Block> firedBlock;
    public final IMetal metalType;
    public final int metalUnits;
    public final boolean hasSlag;
    public final float meltTemp;

    public FireableMetalBlock(IMetal metalType, Properties properties) {
        this(metalType, 400, false, null, properties);
    }

    public FireableMetalBlock(IMetal metalType, int metalUnits, boolean hasSlag, @Nullable RegistryObject<? extends Block> firedBlock, Properties properties) {
        super(20 * 60 * 8, TieredHeat.fromTier(Math.max(1, metalType.getWorkTier())), false, properties);
        this.firedBlock = firedBlock;
        this.metalType = metalType;
        this.meltTemp = cookHeat.getTier() >= 5 ? Float.MAX_VALUE : TieredHeat.fromTier(metalType.getTier() + 1).getBaseTemp();
        this.metalUnits = metalUnits;
        this.hasSlag = hasSlag;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        if(firedBlock != null) pTooltip.add(new TranslatableComponent("block.fireable").append(
                new TranslatableComponent("heat.tier." + cookHeat.getTier()).withStyle(Style.EMPTY.withColor(cookHeat.color.getRGB()))));
        if(meltTemp != Float.MAX_VALUE) pTooltip.add(new TranslatableComponent("block.meltable").append(
                new TranslatableComponent("heat.tier." + TieredHeat.fromTemp(meltTemp).getTier()).withStyle(Style.EMPTY.withColor(TieredHeat.fromTemp(meltTemp).color.getRGB()))));
    }

    @Override
    public boolean isStructureValid(Level level, BlockPos pos, BlockState state) {
        return LevelUtil.getNearbySmelterTier(level, pos) >= cookHeat.getTier();
    }

    @Override
    public BlockState getFiredBlock(Level level, BlockPos pos, BlockState state, float temperature) {
        if(temperature >= metalType.getMeltTemp()) return BlocksNF.MELTED_METAL.get().defaultBlockState().setValue(MeltedMetalBlock.HEAT, TieredHeat.fromTemp(temperature).getTier());
        else return firedBlock == null ? state : firedBlock.get().defaultBlockState();
    }

    @Override
    protected void onFire(Level level, BlockPos pos, BlockState originalState, BlockState firedState, float temperature) {
        if(level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity metal) {
            metal.targetTemperature = TieredHeat.fromTemp(temperature).getUpperTemp();
            metal.temperature = metal.targetTemperature;
            metal.originalState = originalState;
            if(metalType == Metal.IRON && (LevelUtil.isBlockBurningCarbon(level.getBlockState(pos.above())) || LevelUtil.isBlockBurningCarbon(level.getBlockState(pos.below())))) metal.metal = Metal.STEEL;
            else metal.metal = metalType;
            metal.units = metalUnits;
            metal.hasSlag = hasSlag;
            metal.setChanged();
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return false;
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && oldState.is(this) && oldState.getValue(LIT) != newState.getValue(LIT)) LevelUtil.forceTickingChunk(level, pos, newState.getValue(LIT));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this)) LevelUtil.forceTickingChunk(level, pos, false);
    }

    @Override
    public boolean keepForceChunk(BlockState state) {
        return state.getValue(LIT);
    }
}
