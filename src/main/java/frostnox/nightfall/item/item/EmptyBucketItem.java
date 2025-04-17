package frostnox.nightfall.item.item;

import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Fills buckets using the bucket fluids map in {@link FluidsNF}
 */
public class EmptyBucketItem extends ItemNF {
    public EmptyBucketItem(Properties builder) {
        super(builder);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack emptyBucket = player.getItemInHand(hand);
        BlockHitResult fluidSourceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if(fluidSourceResult.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(emptyBucket);
        else if(fluidSourceResult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(emptyBucket);
        else {
            BlockPos fluidPos = fluidSourceResult.getBlockPos();
            Direction direction = fluidSourceResult.getDirection();
            BlockPos pos = fluidPos.relative(direction);
            if(level.mayInteract(player, fluidPos) && player.mayUseItemAt(pos, direction, emptyBucket)) {
                BlockState fluidBlock = level.getBlockState(fluidPos);
                if(fluidBlock.getBlock() instanceof BucketPickup bucketBlock) {
                    Fluid fluidType = level.getFluidState(fluidPos).getType();
                    ItemStack filledBucket = FluidsNF.getFilledBucket(emptyBucket.getItem(), fluidType);
                    if(!filledBucket.isEmpty()) {
                        bucketBlock.pickupBlock(level, fluidPos, fluidBlock);
                        player.awardStat(Stats.ITEM_USED.get(this));
                        bucketBlock.getPickupSound(fluidBlock).ifPresent((sound) -> player.playSound(sound, 1.0F, 1.0F));

                        level.gameEvent(player, GameEvent.FLUID_PICKUP, fluidPos);
                        ItemStack itemResult = ItemUtils.createFilledResult(emptyBucket, player, filledBucket);
                        if(!level.isClientSide) {
                            if(fluidType.is(FluidTags.WATER)) PlayerData.get(player).addKnowledge(KnowledgeNF.COLLECTED_WATER.getId());
                        }

                        return InteractionResultHolder.sidedSuccess(itemResult, level.isClientSide());
                    }
                }
            }
            return InteractionResultHolder.fail(emptyBucket);
        }
    }
}
