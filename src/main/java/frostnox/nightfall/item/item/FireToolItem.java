package frostnox.nightfall.item.item;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.block.IIgnitable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.FireBlockNF;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public class FireToolItem extends SimpleToolItem {
    public final Map<TagKey<Item>, Integer> pairedItems;
    public final Supplier<SoundEvent> sound;
    public final float breakChance;

    public FireToolItem(Map<TagKey<Item>, Integer> pairedItems, Supplier<SoundEvent> sound, float breakChance, @Nullable RegistryObject<? extends Action> recipeAction, Properties properties) {
        super(recipeAction, properties);
        this.pairedItems = pairedItems;
        this.sound = sound;
        this.breakChance = breakChance;
    }

    protected int getIgnitionBound(ItemStack otherStack) {
        for(TagKey<Item> tag : pairedItems.keySet()) {
            if(otherStack.is(tag)) return pairedItems.get(tag);
        }
        return -1;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack heldStack = context.getItemInHand();
        ItemStack otherStack = player.getItemInHand(context.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        int randBound = getIgnitionBound(otherStack);
        if(randBound >= 0) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof IIgnitable ignitable && !ignitable.isIgnited(state)) {
                if(!level.isClientSide() && (randBound < 1 || level.getRandom().nextInt(randBound) == 0)) {
                    if(ignitable.tryToIgnite(level, pos, state, heldStack, TieredHeat.RED)) {
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                    }
                }
                if(level.random.nextFloat() < breakChance) LevelUtil.breakItem(heldStack, player, context.getHand());
                level.playSound(player, pos, sound.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            else {
                BlockPos adjPos = pos.relative(context.getClickedFace());
                if(FireBlockNF.canBePlacedAt(level, adjPos)) {
                    if(!level.isClientSide() && (randBound < 1 || level.getRandom().nextInt(randBound) == 0)) {
                        level.setBlock(adjPos, BlocksNF.FIRE.get().getStateForPlacement(new BlockPlaceContext(context)), 11);
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, adjPos);
                        PlayerData.get(player).addKnowledge(KnowledgeNF.STARTED_FIRE.getId());
                    }
                    if(level.random.nextFloat() < breakChance) LevelUtil.breakItem(heldStack, player, context.getHand());
                    level.playSound(player, pos, sound.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
                else return InteractionResult.FAIL;
            }
        }
        else return InteractionResult.PASS;
    }
}
