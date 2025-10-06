package frostnox.nightfall.item.item;

import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.entity.RopeKnotEntity;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class RopeBlockItem extends BlockItemNF {
    public RopeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos.MutableBlockPos pos = context.getHitResult().getBlockPos().mutable();
        if(context.getHitResult().getDirection() == Direction.DOWN) {
            pos.setY(pos.getY() - 1);
        }
        Level level = context.getLevel();
        while(level.getBlockState(pos).is(getBlock())) pos.setY(pos.getY() - 1);
        if(pos.getY() > level.getMinBuildHeight()) {
            BlockPlaceContext newContext = new BlockPlaceContext(context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false));
            if(level.getBlockState(pos).canBeReplaced(newContext)) {
                return newContext;
            }
        }
        return context;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        if(ClientEngine.get().getPlayer() != null && PlayerData.get(ClientEngine.get().getPlayer()).hasCompletedEntry(EntriesNF.TAMING.getId())) {
            tooltips.add(new TranslatableComponent("item.leading").withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack item, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if(item.is(ItemsNF.ROPE.get()) && pInteractionTarget.isAlive() && pInteractionTarget instanceof Mob mob && mob.canBeLeashed(pPlayer)) {
            mob.setLeashedTo(pPlayer, true);
            item.shrink(1);
            return InteractionResult.sidedSuccess(pPlayer.level.isClientSide);
        }
        else return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if(blockstate.is(BlockTags.FENCES)) {
            Player player = pContext.getPlayer();
            if(!level.isClientSide && player != null) bindPlayerMobs(player, level, blockpos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(pContext);
    }

    public static InteractionResult bindPlayerMobs(Player pPlayer, Level pLevel, BlockPos pPos) {
        LeashFenceKnotEntity knot = null;
        boolean flag = false;
        int i = pPos.getX();
        int j = pPos.getY();
        int k = pPos.getZ();
        for(Mob mob : pLevel.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D))) {
            if(mob.getLeashHolder() == pPlayer) {
                if(knot == null) {
                    knot = RopeKnotEntity.getOrCreateKnot(pLevel, pPos);
                    knot.playPlacementSound();
                }
                mob.setLeashedTo(knot, true);
                flag = true;
            }
        }
        return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
