package frostnox.nightfall.item.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Employs similar logic to vanilla buckets.
 * Empty bucket is calculated from the container item instead of using a hardcoded value.
 * Allows placing non-source blocks.
 */
public class FilledBucketItem extends ItemNF {
    public final boolean placeSources;
    private final Supplier<? extends FlowingFluid> heldFluid;

    public FilledBucketItem(boolean placeSources, Supplier<? extends FlowingFluid> heldFluid, Properties builder) {
        super(builder);
        this.placeSources = placeSources;
        this.heldFluid = heldFluid;
    }

    public Fluid getFluid() {
        return heldFluid.get();
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack bucket, UseOnContext context) {
        Player player = context.getPlayer();
        if(player != null) {
            Level level = context.getLevel();
            if(level.getBlockEntity(context.getClickedPos()) instanceof MenuProvider menuProvider) {
                menuProvider.createMenu(0, player.getInventory(), player).quickMoveStack(player, 27 + player.getInventory().selected);
                if(bucket.isEmpty()) {
                    playEmptySound(null, level, context.getClickedPos(), true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack filledBucket = player.getItemInHand(hand);
        BlockHitResult blockResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if(blockResult.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(filledBucket);
        else if(blockResult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(filledBucket);
        else {
            BlockPos pos = blockResult.getBlockPos();
            Direction direction = blockResult.getDirection();
            BlockPos blockPos = pos.relative(direction);
            if(level.mayInteract(player, pos) && player.mayUseItemAt(blockPos, direction, filledBucket)) {
                BlockState blockState = level.getBlockState(pos);
                BlockPos targetPos = (blockState.getBlock() instanceof LiquidBlockContainer liquidBlock
                        && liquidBlock.canPlaceLiquid(level, pos, blockState, heldFluid.get())) ? pos : blockPos;
                if(this.emptyFluid(player, level, targetPos, blockResult)) {
                    if(player instanceof ServerPlayer) CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, targetPos, filledBucket);
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(!player.getAbilities().instabuild ? filledBucket.getContainerItem() : filledBucket, level.isClientSide());
                }
                else return InteractionResultHolder.fail(filledBucket);
            }
            else return InteractionResultHolder.fail(filledBucket);
        }
    }

    protected boolean emptyFluid(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result) {
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        boolean replaceable = blockState.canBeReplaced(heldFluid.get());
        boolean canPlace = replaceable || blockState.isAir() || block instanceof LiquidBlockContainer liquidBlock && liquidBlock.canPlaceLiquid(level, pos, blockState, heldFluid.get());
        if(!canPlace) {
            return result != null && this.emptyFluid(player, level, result.getBlockPos().relative(result.getDirection()), null);
        }
        else if(level.dimensionType().ultraWarm() && heldFluid.get().is(FluidTags.WATER)) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for(int l = 0; l < 8; ++l) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
            }
            return true;
        }
        else if(block instanceof LiquidBlockContainer liquidBlock) {
            if(placeSources) {
                if(liquidBlock.canPlaceLiquid(level, pos, blockState, heldFluid.get())) {
                    liquidBlock.placeLiquid(level, pos, blockState, heldFluid.get().getSource(false));
                    this.playEmptySound(player, level, pos, false);
                    return true;
                }
            }
            else if(liquidBlock.canPlaceLiquid(level, pos, blockState, heldFluid.get().getFlowing())) {
                liquidBlock.placeLiquid(level, pos, blockState, heldFluid.get().getFlowing().defaultFluidState());
                this.playEmptySound(player, level, pos, false);
                return true;
            }
        }
        if(!level.isClientSide && replaceable && !material.isLiquid()) level.destroyBlock(pos, true);

        BlockState newState = heldFluid.get().defaultFluidState().createLegacyBlock();
        if(placeSources) {
            if(!level.setBlock(pos, newState, 11) && !blockState.getFluidState().isSource()) return false;
        }
        else {
            FluidState fluidState = blockState.getFluidState();
            int amount = heldFluid.get().getAmount(heldFluid.get().getFlowing().defaultFluidState());
            if((heldFluid.get() == fluidState.getType() || heldFluid.get().getFlowing() == fluidState.getType()) && amount <= fluidState.getAmount()) return false;
            if(!level.setBlock(pos, newState.setValue(LiquidBlock.LEVEL, amount), 11)) {
                return false;
            }
        }
        this.playEmptySound(player, level, pos, false);
        return true;
    }

    public void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos, boolean forceSound) {
        SoundEvent soundevent = heldFluid.get().getAttributes().getEmptySound();
        if(soundevent == null) soundevent = SoundEvents.BUCKET_EMPTY;
        level.playSound(forceSound ? null : player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
