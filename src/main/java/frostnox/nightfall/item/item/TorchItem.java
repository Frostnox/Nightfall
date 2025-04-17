package frostnox.nightfall.item.item;

import frostnox.nightfall.block.IIgnitable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.item.IItemLightSource;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class TorchItem extends StandingAndWallBlockItem implements IItemLightSource {
    public final Supplier<? extends Item> extinguishedItem;

    public TorchItem(Block pStandingBlock, Block pWallBlock, Supplier<? extends Item> extinguishedItem, Properties pProperties) {
        super(pStandingBlock, pWallBlock, pProperties);
        this.extinguishedItem = extinguishedItem;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player != null && !player.isCrouching()) {
            ItemStack heldStack = context.getItemInHand();
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof IIgnitable burnable && !burnable.isIgnited(state)) {
                if(!level.isClientSide()) {
                    if(burnable.tryToIgnite(level, pos, state, heldStack, TieredHeat.RED)) {
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                    }
                }
                level.playSound(player, pos, SoundsNF.FIRE_WHOOSH.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.2F + 0.9F);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useOn(context);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isInWater()) LevelUtil.extinguishItemEntity(entity, extinguishedItem.get(), true);
        return false;
    }

    @Override
    public int getBrightness() {
        return 15;
    }

    @Override
    public double getLightRadiusSqr() {
        return 15D * 15D;
    }

    @Override
    public Item getExtinguishedItem() {
        return extinguishedItem.get();
    }

    @Override
    public double getEquippedHeight(Pose pose) {
        return 1.15F;
    }
}
