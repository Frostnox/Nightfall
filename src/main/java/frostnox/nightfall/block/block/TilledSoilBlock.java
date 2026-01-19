package frostnox.nightfall.block.block;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.Fertility;
import frostnox.nightfall.block.Humidity;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.util.Lazy;

import java.util.Random;
import java.util.function.Supplier;

public class TilledSoilBlock extends UnstableBlock implements ITimeSimulatedBlock {
    public static final EnumProperty<Humidity> HUMIDITY = BlockStatePropertiesNF.HUMIDITY;
    public final Fertility fertility;
    public final Lazy<BlockState> untilledState;
    private final float dryHumidity, moistHumidity, irrigatedHumidity;

    public TilledSoilBlock(Fertility fertility, float dryHumidity, float moistHumidity, float irrigatedHumidity, Supplier<? extends Block> untilledBlock, Supplier<SoundEvent> slideSound, Properties properties) {
        super(slideSound, properties);
        this.fertility = fertility;
        this.untilledState = Lazy.of(() -> untilledBlock.get().defaultBlockState());
        this.dryHumidity = dryHumidity;
        this.moistHumidity = moistHumidity;
        this.irrigatedHumidity = irrigatedHumidity;
        registerDefaultState(getStateDefinition().any().setValue(HUMIDITY, Humidity.DRY));
    }

    public float getHumidity(BlockState state) {
        return switch(state.getValue(HUMIDITY)) {
            case DRY -> dryHumidity;
            case MOIST -> moistHumidity;
            case IRRIGATED -> irrigatedHumidity;
        };
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        Humidity humidity = null;
        if(FarmlandWaterManager.hasBlockWaterTicket(level, pos)) humidity = Humidity.IRRIGATED;
        else for(BlockPos searchPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 0, 4))) {
            BlockState searchState = level.getBlockState(searchPos);
            if(searchState.is(BlocksNF.ICE.get()) || level.getFluidState(searchPos).is(TagsNF.FRESHWATER)) {
                humidity = Humidity.IRRIGATED;
                break;
            }
        }
        if(humidity == null) {
            if(LevelData.isPresent(level)) {
                if(LevelData.get(level).isRainfallCommonAt(ChunkData.get(level.getChunkAt(pos)), pos.getX(), pos.getY() + 1, pos.getZ())) {
                    if(state.getValue(HUMIDITY) == Humidity.MOIST) return;
                    else humidity = level.isRainingAt(pos.above()) ? Humidity.MOIST : Humidity.DRY;
                }
                else humidity = Humidity.DRY;
            }
            else humidity = level.isRainingAt(pos.above()) ? Humidity.MOIST : Humidity.DRY;
        }
        if(humidity != state.getValue(HUMIDITY)) level.setBlock(pos, state.setValue(HUMIDITY, humidity), 2);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction pFacing, BlockState pFacingState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if(pFacing == Direction.UP && !state.canSurvive(level, pCurrentPos)) return untilledState.get();
        else return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return !level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN, SupportType.FULL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return !defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? untilledState.get() : super.getStateForPlacement(pContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HUMIDITY);
    }

    @Override
    public void onMovingBlockEntityCreated(BlockState state, Level level, BlockPos pos, MovingBlockEntity entity) {
        entity.setBlockState(untilledState.get());
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).addSimulatableBlock(TickPriority.HIGH, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this) && LevelData.isPresent(level)) {
            ChunkData.get(level.getChunkAt(pos)).removeSimulatableBlock(TickPriority.HIGH, pos);
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        if(MathUtil.getRandomSuccesses(randomTickChance, elapsedTime, 1, random) >= 1) {
            randomTick(state, level, pos, random);
        }
    }

    @Override
    public TickPriority getTickPriority() {
        return TickPriority.HIGH;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return untilledState.get().getCloneItemStack(target, level, pos, player);
    }
}
