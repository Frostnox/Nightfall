package frostnox.nightfall.item.item;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.item.IContainerChanger;
import frostnox.nightfall.item.ITieredItemMaterial;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class TongsItem extends ItemNF implements IContainerChanger {
    public final ITieredItemMaterial material;

    public TongsItem(ITieredItemMaterial material, Properties properties) {
        super(properties.defaultDurability(material.getUses()));
        this.material = material;
    }

    public int getMaxHeatTier() {
        return material.getMetal().getTier() + 1;
    }

    public boolean hasWorkpiece(ItemStack item) {
        return item.getTag().contains("color");
    }

    public float getTemperature(ItemStack item) {
        return item.getTag().getFloat("temperature");
    }

    public int getStableTempTicks(ItemStack item) {
        return item.getTag().getInt("stableTempTicks");
    }

    public @Nullable Item getWorkpiece(ItemStack item) {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(item.getTag().getString("item")));
    }

    public int getColor(ItemStack item) {
        return item.getTag().getInt("color");
    }

    public int[] getWork(ItemStack item) {
        return item.getTag().getIntArray("work");
    }

    public void removeWorkpiece(ItemStack item) {
        item.getTag().remove("item");
        item.getTag().remove("color");
        item.getTag().remove("work");
        item.getTag().remove("stableTempTicks");
        item.getTag().remove("temperature");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tongs = player.getItemInHand(hand);
        if(!hasWorkpiece(tongs)) {
            InteractionHand oppHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack item = player.getItemInHand(oppHand);
            if(item.is(Tags.Items.INGOTS) || item.is(TagsNF.PLATES) || item.is(TagsNF.METAL_WORKPIECE)) {
                if(!level.isClientSide) {
                    tongs.getTag().putString("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
                    if(item.is(ItemsNF.IRON_BLOOM.get())) tongs.getTag().putInt("color", 0xFF302B32);
                    else tongs.getTag().putInt("color", Metal.fromString(item.getItem().toString()).getColor().getRGB());
                    tongs.getTag().putFloat("temperature", 0);
                    int[] work = new int[8];
                    if(item.is(TagsNF.PLATES)) {
                        work[0] = 2;
                        work[2] = 2;
                        work[5] = 2;
                    }
                    else if(!item.is(Tags.Items.INGOTS)) {
                        work[1] = -2;
                        work[3] = -2;
                        work[6] = -2;
                    }
                    tongs.getTag().putIntArray("work", work);
                    if(!player.getAbilities().instabuild) item.shrink(1);
                }
                player.swing(oppHand);
                return InteractionResultHolder.consume(tongs);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof IHeatSource heatSource) {
            ItemStack tongs = context.getItemInHand();
            if(!level.isClientSide && hasWorkpiece(tongs)) {
                float temperature = heatSource.getTemperature(level, pos, state);
                if(getMaxHeatTier() != 5) temperature = Math.min(TieredHeat.fromTier(getMaxHeatTier() + 1).getBaseTemp() - 100, temperature);
                temperature = Math.min(temperature, Metal.fromString(getWorkpiece(tongs).toString()).getMeltTemp());
                if(temperature > 100 && temperature >= getTemperature(tongs)) {
                    tongs.getTag().putFloat("temperature", temperature);
                    tongs.getTag().putInt("stableTempTicks", 20 * 45);
                    if(player != null) {
                        player.swing(context.getHand(), true);
                        level.playSound(null, player, SoundsNF.FIRE_WHOOSH.get(), SoundSource.PLAYERS, 1F, 1F);
                        tongs.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        else if(state.getBlock() instanceof TieredAnvilBlock && context.getClickedFace() == Direction.UP && context.getClickLocation().y % 1D == 0) {
            if(level.isClientSide()) {
                if(!state.getValue(TieredAnvilBlock.HAS_METAL)) {
                    return InteractionResult.SUCCESS;
                }
            }
            else {
                if(level.getBlockEntity(pos) instanceof TieredAnvilBlockEntity anvil) {
                    if(!anvil.hasWorkpiece()) {
                        if(anvil.putWorkpiece(context.getItemInHand(), context.getClickLocation())) {
                            player.swing(context.getHand());
                            return InteractionResult.CONSUME;
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!worldIn.isClientSide && entityIn instanceof Player player) {
            if((isSelected && player.getMainHandItem() == stack) || (itemSlot == 0 && player.getOffhandItem() == stack)) {
                if(player.isInWater()) {
                    double handY = player.getY() + (0.85F * player.getBbHeight() / 1.8F);
                    BlockPos handPos = new BlockPos(player.getX(), handY, player.getZ());
                    FluidState fluid = worldIn.getFluidState(handPos);
                    if(fluid.is(FluidTags.WATER) && handPos.getY() + fluid.getHeight(worldIn, handPos) > handY) {
                        stack.getTag().remove("stableTempTicks");
                        stack.getTag().remove("temperature");
                        return;
                    }
                }
                int stableTicks = stack.getTag().getInt("stableTempTicks");
                boolean inRain = worldIn.isRainingAt(new BlockPos(entityIn.getX(), entityIn.getBoundingBox().maxY, entityIn.getZ()));
                if(stableTicks > 0) stack.getTag().putInt("stableTempTicks", Math.max(0, stableTicks - (inRain ? 2 : 1)));
                else {
                    float temperature = getTemperature(stack);
                    if(temperature > 0) temperature = Math.max(0, temperature - (inRain ? 1F : 0.5F));
                    stack.getTag().putFloat("temperature", temperature);
                }
            }
            else {
                stack.getTag().remove("stableTempTicks");
                stack.getTag().remove("temperature");
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if(!newStack.is(this)) return true;
        else return hasWorkpiece(newStack) != hasWorkpiece(oldStack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        TieredHeat heat = TieredHeat.fromTier(getMaxHeatTier());
        pTooltipComponents.add(new TranslatableComponent("block.heat_resistant." + heat.getTier()).withStyle(Style.EMPTY.withColor(heat.color.getRGB())));
        pTooltipComponents.add(new TranslatableComponent("item.nightfall.tongs.info").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void containerChanged(ItemStack item) {
        item.getTag().remove("stableTempTicks");
        item.getTag().remove("temperature");
    }
}