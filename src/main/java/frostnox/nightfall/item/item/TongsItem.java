package frostnox.nightfall.item.item;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.IServerSwapBehavior;
import frostnox.nightfall.item.ITieredItemMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class TongsItem extends ItemNF implements IServerSwapBehavior {
    public final ITieredItemMaterial material;

    public TongsItem(ITieredItemMaterial material, Properties properties) {
        super(properties.defaultDurability(material.getUses()));
        this.material = material;
    }

    public boolean hasWorkpiece(ItemStack item) {
        return item.getTag().contains("item");
    }

    public float getTemperature(ItemStack item) {
        return item.getTag().getFloat("temperature");
    }

    public @Nullable Item getWorkpiece(ItemStack item) {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(item.getTag().getString("item")));
    }

    public int[] getWork(ItemStack item) {
        return item.getTag().getIntArray("work");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide) {
            ItemStack tongs = player.getItemInHand(hand);
            if(!hasWorkpiece(tongs)) {
                InteractionHand oppHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                ItemStack item = player.getItemInHand(oppHand);
                if(item.is(Tags.Items.INGOTS) || item.is(TagsNF.PLATES) || item.is(TagsNF.METAL_WORKPIECE)) {
                    tongs.getTag().putString("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
                    tongs.getTag().putFloat("temperature", 0);
                    int[] work = new int[8];
                    if(item.is(TagsNF.PLATES)) {
                        work[0] = 3;
                        work[2] = 3;
                        work[5] = 3;
                    }
                    else if(!item.is(Tags.Items.INGOTS)) {

                    }
                    tongs.getTag().putIntArray("work", work);
                    if(!player.getAbilities().instabuild) item.shrink(1);
                    player.swing(oppHand, true);
                    return InteractionResultHolder.consume(tongs);
                }
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if(!level.isClientSide) {
            BlockPos pos = context.getClickedPos();
            Player player = context.getPlayer();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof IHeatSource heatSource) {
                float temperature = heatSource.getTemperature(level, pos, state);
                ItemStack tongs = context.getItemInHand();
                if(temperature > 100 && temperature > getTemperature(tongs)) {
                    tongs.getTag().putFloat("temperature", temperature);
                    if(player != null) {
                        player.swing(context.getHand(), true);
                        tongs.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if(!newStack.is(this)) return true;
        else return newStack != oldStack;
    }

    @Override
    public void swapToServer(ItemStack item, Player player, boolean mainHand) {
        System.out.println("TO");
    }

    @Override
    public void swapFromServer(ItemStack item, Player player, boolean mainHand) {
        System.out.println("FROM");
    }
}