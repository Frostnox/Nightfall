package frostnox.nightfall.block.block.bowl;

import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.recipe.BowlCrushingRecipe;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BowlBlock extends WaterloggedEntityBlock {
    protected static final VoxelShape SHAPE = Shapes.or(Block.box(5, 0, 5, 11, 1, 11),
            Shapes.join(Block.box(4, 1, 4, 12, 3, 12), Block.box(5, 1, 5, 11, 3, 11), BooleanOp.NOT_SAME));

    public BowlBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.BOWL.get().create(pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, level, pos, facingPos);
        if(!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if(player.isCrouching() && player.getItemInHand(pHand).isEmpty()) return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof BowlBlockEntity bowl) {
            if(level.isClientSide) return InteractionResult.SUCCESS;
            else {
                ItemStack heldItem = player.getItemInHand(pHand);
                if(bowl.item.isEmpty()) {
                    if(!heldItem.isEmpty()) {
                        bowl.item = player.getAbilities().instabuild ? new ItemStack(heldItem.getItem()) : heldItem.split(1);
                        bowl.itemAngle = Math.round((-player.getViewYRot(1F)) / 45F) * 45F;
                        bowl.setChanged();
                        level.sendBlockUpdated(pos, state, state, 2);
                    }
                    else return InteractionResult.PASS;
                }
                else {
                    IPlayerData capP = PlayerData.get(player);
                    capP.setHeldItemForRecipe(heldItem);
                    RecipeWrapper container = new RecipeWrapper(new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, bowl.item)));
                    ForgeHooks.setCraftingPlayer(player);
                    Optional<BowlCrushingRecipe> recipe = level.getRecipeManager().getRecipeFor(BowlCrushingRecipe.TYPE, container, level);
                    ForgeHooks.setCraftingPlayer(null);
                    capP.setHeldItemForRecipe(ItemStack.EMPTY);
                    if(recipe.isPresent() && recipe.get().isUnlocked(player)) {
                        bowl.crushes++;
                        if(heldItem.isDamageableItem() && bowl.crushes % 2 == 0) {
                            heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pHand));
                        }
                        level.playSound(null, pos, SoundsNF.WOODEN_BOWL_CRUSH.get(), SoundSource.PLAYERS, 1F, 0.97F + 0.06F * level.random.nextFloat());
                        ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, bowl.item), pos.getX() + 0.5,
                                pos.getY() + 0.35, pos.getZ() + 0.5, 3 + level.random.nextInt() % 2,
                                0.025, 0.01, 0.025, 0.025);
                        if(bowl.crushes >= 4) {
                            bowl.item = new ItemStack(recipe.get().getResultItem().getItem(), bowl.item.getCount());
                            bowl.crushes = 0;
                            bowl.setChanged();
                            level.sendBlockUpdated(pos, state, state, 2);
                        }
                    }
                    else {
                        LevelUtil.giveItemToPlayer(bowl.item.copy(), player, true);
                        bowl.item = ItemStack.EMPTY;
                        bowl.setChanged();
                        level.sendBlockUpdated(pos, state, state, 2);
                    }
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof BowlBlockEntity bowl) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bowl.item);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
