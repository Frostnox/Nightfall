package frostnox.nightfall.item.item;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

public class IgnitableBlockItem extends BlockItemNF {
    public Supplier<? extends Item> ignitedItem;

    public IgnitableBlockItem(Supplier<? extends Item> ignitedItem, Block block, Properties properties) {
        super(block, properties);
        this.ignitedItem = ignitedItem;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player != null && !player.isCrouching()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            ItemStack heldItem = context.getItemInHand();
            if(state.getBlock() instanceof IHeatSource heatSource && heatSource.getHeat(level, pos, state) != TieredHeat.NONE) {
                if(!level.isClientSide()) player.setItemInHand(context.getHand(), new ItemStack(ignitedItem.get(), heldItem.getCount()));
                player.playSound(SoundsNF.FIRE_WHOOSH.get(), 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            else {
                BlockHitResult fluidSourceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
                if(fluidSourceResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos fluidPos = fluidSourceResult.getBlockPos();
                    BlockState fluidBlock = level.getBlockState(fluidPos);
                    if(fluidBlock.getBlock() instanceof IHeatSource heatSource && heatSource.getHeat(level, fluidPos, fluidBlock) != TieredHeat.NONE) {
                        if(!level.isClientSide()) player.setItemInHand(context.getHand(), new ItemStack(ignitedItem.get(), heldItem.getCount()));
                        player.playSound(SoundsNF.FIRE_WHOOSH.get(), 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        BlockHitResult fluidSourceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if(fluidSourceResult.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(heldItem);
        else if(fluidSourceResult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(heldItem);
        else {
            BlockPos fluidPos = fluidSourceResult.getBlockPos();
            BlockState fluidBlock = level.getBlockState(fluidPos);
            if(fluidBlock.getBlock() instanceof IHeatSource heatSource && heatSource.getHeat(level, fluidPos, fluidBlock) != TieredHeat.NONE) {
                player.playSound(SoundsNF.FIRE_WHOOSH.get(), 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
                return InteractionResultHolder.sidedSuccess(new ItemStack(ignitedItem.get(), heldItem.getCount()), level.isClientSide());
            }
            return InteractionResultHolder.fail(heldItem);
        }
    }
}
