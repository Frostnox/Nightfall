package frostnox.nightfall.block.block;

import com.google.common.collect.ImmutableMap;
import frostnox.nightfall.block.IBurnable;
import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FireBlockNF extends BaseFireBlock implements IHeatSource {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((entry) -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private final Map<BlockState, VoxelShape> shapesCache;

    public FireBlockNF(Properties properties) {
        super(properties, 1F);
        registerDefaultState(stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)));
        shapesCache = ImmutableMap.copyOf(stateDefinition.getPossibleStates().stream()
                .filter((state) -> state.getValue(AGE) == 0).collect(Collectors.toMap(Function.identity(), FireBlockNF::calculateShape)));
    }

    private static VoxelShape calculateShape(BlockState p_53491_) {
        VoxelShape voxelshape = Shapes.empty();
        if (p_53491_.getValue(UP)) {
            voxelshape = UP_AABB;
        }

        if (p_53491_.getValue(NORTH)) {
            voxelshape = Shapes.or(voxelshape, NORTH_AABB);
        }

        if (p_53491_.getValue(SOUTH)) {
            voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
        }

        if (p_53491_.getValue(EAST)) {
            voxelshape = Shapes.or(voxelshape, EAST_AABB);
        }

        if (p_53491_.getValue(WEST)) {
            voxelshape = Shapes.or(voxelshape, WEST_AABB);
        }

        return voxelshape.isEmpty() ? DOWN_AABB : voxelshape;
    }

    public static boolean canBePlacedAt(Level pLevel, BlockPos pPos) {
        BlockState state = pLevel.getBlockState(pPos);
        if(!state.isAir()) return false;
        else return BlocksNF.FIRE.get().getPlacementState(pLevel, pPos).canSurvive(pLevel, pPos);
    }

    public BlockState getPlacementState(BlockGetter pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (!canCatchFire(pLevel, pPos, Direction.UP) && !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
            BlockState blockstate1 = defaultBlockState();

            for(Direction direction : Direction.values()) {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
                if (booleanproperty != null) {
                    blockstate1 = blockstate1.setValue(booleanproperty, Boolean.valueOf(canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())));
                }
            }

            return blockstate1;
        } else {
            return defaultBlockState();
        }
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return canSurvive(pState, pLevel, pCurrentPos) ? getStateWithAge(pLevel, pCurrentPos, pState.getValue(AGE)) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shapesCache.get(pState.setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockGetter pLevel = pContext.getLevel();
        BlockPos pPos = pContext.getClickedPos();
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (!canCatchFire(pLevel, pPos, Direction.UP) && !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
            BlockState blockstate1 = defaultBlockState();

            for(Direction direction : Direction.values()) {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
                if (booleanproperty != null) {
                    blockstate1 = blockstate1.setValue(booleanproperty, Boolean.valueOf(canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())));
                }
            }

            return blockstate1;
        } else {
            return defaultBlockState();
        }
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        return pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP) || isValidFireLocation(pLevel, pPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        level.scheduleTick(pos, this, getFireTickDelay(level.random));
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!state.canSurvive(level, pos)) {
                level.removeBlock(pos, false);
            }

            BlockState blockstate = level.getBlockState(pos.below());
            boolean flag = blockstate.isFireSource(level, pos, Direction.UP);
            int i = state.getValue(AGE);
            if (!flag && level.isRaining() && isNearRain(level, pos) && rand.nextFloat() < 0.2F + (float)i * 0.03F) {
                level.removeBlock(pos, false);
            } else {
                int j = Math.min(15, i + rand.nextInt(3) / 2);
                if (i != j) {
                    state = state.setValue(AGE, Integer.valueOf(j));
                    level.setBlock(pos, state, 4);
                }

                if (!flag) {
                    if (!isValidFireLocation(level, pos)) {
                        BlockPos blockpos = pos.below();
                        if (!level.getBlockState(blockpos).isFaceSturdy(level, blockpos, Direction.UP) || i > 3) {
                            level.removeBlock(pos, false);
                        }

                        return;
                    }

                    if (i == 15 && rand.nextInt(4) == 0 && !canCatchFire(level, pos.below(), Direction.UP)) {
                        level.removeBlock(pos, false);
                        return;
                    }
                }
                spreadHeat(level, pos, getHeat(level, pos, state));

                boolean flag1 = level.isHumidAt(pos);
                int k = flag1 ? -50 : 0;
                trySpreadOrBurn(level, pos.east(), 300 + k, rand, i, Direction.WEST);
                trySpreadOrBurn(level, pos.west(), 300 + k, rand, i, Direction.EAST);
                trySpreadOrBurn(level, pos.below(), 250 + k, rand, i, Direction.UP);
                trySpreadOrBurn(level, pos.above(), 250 + k, rand, i, Direction.DOWN);
                trySpreadOrBurn(level, pos.north(), 300 + k, rand, i, Direction.SOUTH);
                trySpreadOrBurn(level, pos.south(), 300 + k, rand, i, Direction.NORTH);
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                for(int l = -1; l <= 1; ++l) {
                    for(int i1 = -1; i1 <= 1; ++i1) {
                        for(int j1 = -1; j1 <= 4; ++j1) {
                            if (l != 0 || j1 != 0 || i1 != 0) {
                                int k1 = 100;
                                if (j1 > 1) {
                                    k1 += (j1 - 1) * 100;
                                }

                                blockpos$mutableblockpos.setWithOffset(pos, l, j1, i1);
                                int l1 = getFireOdds(level, blockpos$mutableblockpos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 60) / (i + 30);
                                    if (flag1) {
                                        i2 /= 2;
                                    }

                                    if (i2 > 0 && rand.nextInt(k1) <= i2 && (!level.isRaining() || !isNearRain(level, blockpos$mutableblockpos))) {
                                        int j2 = Math.min(15, i + rand.nextInt(5) / 4);
                                        level.setBlock(blockpos$mutableblockpos, getStateWithAge(level, blockpos$mutableblockpos, j2), 3);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    protected boolean isNearRain(Level pLevel, BlockPos pPos) {
        return pLevel.isRainingAt(pPos) || pLevel.isRainingAt(pPos.west()) || pLevel.isRainingAt(pPos.east()) || pLevel.isRainingAt(pPos.north()) || pLevel.isRainingAt(pPos.south());
    }

    private void trySpreadOrBurn(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face) {
        int i = pLevel.getBlockState(pPos).getFlammability(pLevel, pPos, face);
        if (pRandom.nextInt(pChance) < i) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            if(!blockstate.getMaterial().blocksMotion() && pRandom.nextInt(pAge + 10) < 5 && !pLevel.isRainingAt(pPos)) {
                int j = Math.min(pAge + pRandom.nextInt(5) / 4, 15);
                pLevel.setBlock(pPos, getStateWithAge(pLevel, pPos, j), 3);
            } else {
                if(blockstate.getBlock() instanceof IBurnable burnable) pLevel.setBlockAndUpdate(pPos, burnable.getBurnedState(blockstate));
                else pLevel.removeBlock(pPos, false);
            }

            blockstate.onCaughtFire(pLevel, pPos, face, null);
        }

    }

    private BlockState getStateWithAge(LevelAccessor pLevel, BlockPos pPos, int pAge) {
        BlockState blockstate;
        BlockPos blockpos = pPos.below();
        BlockState blockstate11 = pLevel.getBlockState(blockpos);
        if (!canCatchFire(pLevel, pPos, Direction.UP) && !blockstate11.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
            BlockState blockstate1 = defaultBlockState();

            for(Direction direction : Direction.values()) {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
                if (booleanproperty != null) {
                    blockstate1 = blockstate1.setValue(booleanproperty, Boolean.valueOf(canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())));
                }
            }

            blockstate = blockstate1;
        } else {
            blockstate = defaultBlockState();
        }
        return blockstate.is(BlocksNF.FIRE.get()) ? blockstate.setValue(AGE, Integer.valueOf(pAge)) : blockstate;
    }

    private boolean isValidFireLocation(BlockGetter pLevel, BlockPos pPos) {
        for(Direction direction : Direction.values()) {
            if (canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    private int getFireOdds(LevelReader pLevel, BlockPos pPos) {
        if (!pLevel.isEmptyBlock(pPos)) {
            return 0;
        } else {
            int i = 0;

            for(Direction direction : Direction.values()) {
                BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
                i = Math.max(blockstate.getFireSpreadSpeed(pLevel, pPos.relative(direction), direction.getOpposite()), i);
            }

            return i;
        }
    }

    @Override
    @Deprecated //Forge: Use canCatchFire with more context
    protected boolean canBurn(BlockState pState) {
        return false;
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        pLevel.scheduleTick(pPos, this, getFireTickDelay(pLevel.random));
    }

    private static int getFireTickDelay(Random pRandom) {
        return 30 + pRandom.nextInt(10);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).isFlammable(world, pos, face);
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public TieredHeat getHeat(Level level, BlockPos pos, BlockState state) {
        return TieredHeat.RED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean pIsMoving) {
        super.onRemove(state, level, pos, newState, pIsMoving);
        if(!newState.is(this)) spreadHeat(level, pos, TieredHeat.NONE);
    }
}
