package frostnox.nightfall.item.item;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.anvil.AnvilAction;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlockEntity;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.item.ModifiableScreen;
import frostnox.nightfall.client.gui.screen.item.TongsVisualRecipeScreen;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.SmithingRecipe;
import frostnox.nightfall.item.IContainerChanger;
import frostnox.nightfall.item.client.IClientSwapBehavior;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.inventory.FluidSlot;
import frostnox.nightfall.world.inventory.ItemStackHandlerNF;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TongsItem extends ItemNF implements IContainerChanger, IModifiable, IClientSwapBehavior {
    public final int maxHeatTier;

    public TongsItem(int maxHeatTier, Properties properties) {
        super(properties);
        this.maxHeatTier = maxHeatTier;
    }

    public int getMaxHeatTier() {
        return maxHeatTier;
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
        item.getTag().remove("slagCenter");
        item.getTag().remove("slagLeft");
        item.getTag().remove("slagRight");
        item.getTag().remove("flip");
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack tongs, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if(player != null && getTemperature(tongs) > 250) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
            Fluid quenchFluid = Fluids.EMPTY;
            if(hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hitResult.getBlockPos();
                BlockState hitBlock = level.getBlockState(hitPos);
                if(!level.isClientSide && level.getBlockEntity(hitPos) instanceof MenuProvider menuProvider) {
                    AbstractContainerMenu menu = menuProvider.createMenu(0, player.getInventory(), player);
                    int fluidIndex = LevelUtil.getFirstFluidSlotIndex(menu);
                    if(fluidIndex >= 0) {
                        Slot slot = menu.getSlot(fluidIndex);
                        quenchFluid = ((FluidSlot) slot).getFluid();
                        slot.getItem().shrink(1);
                        slot.setChanged();
                    }
                }
                if(quenchFluid == Fluids.EMPTY && !hitBlock.getFluidState().isEmpty()) quenchFluid = hitBlock.getFluidState().getType();
            }
            if(quenchFluid != Fluids.EMPTY) {
                if(!level.isClientSide) {
                    Item workpiece = getWorkpiece(tongs);
                    int[] work = getWork(tongs);
                    ItemStack resultItem = null;
                    RecipeWrapper inventory = new RecipeWrapper(new ItemStackHandlerNF(new ItemStack(workpiece)));
                    for(SmithingRecipe recipe : level.getRecipeManager().getRecipesFor(SmithingRecipe.TYPE, inventory, level)) {
                        if(recipe.matchesWorkAndFluid(work, quenchFluid)) {
                            resultItem = recipe.assemble(inventory);
                            break;
                        }
                    }
                    if(resultItem == null) resultItem = new ItemStack(Metal.fromString(workpiece.toString()).getMatchingItem(TagsNF.SCRAP));
                    removeWorkpiece(tongs);
                    player.setItemInHand(InteractionHand.MAIN_HAND, tongs.copy());
                    LevelUtil.giveItemToPlayer(resultItem, player, true);
                    Vec3 loc = hitResult.getLocation();
                    ((ServerLevel) level).sendParticles(ParticleTypesNF.STEAM.get(), loc.x, loc.y, loc.z, 14, 0.15F, 0.05F, 0.15F, 0);
                    level.playSound(null, player, SoundsNF.QUENCH.get(), SoundSource.PLAYERS, 1F, 1F);
                }
                else setLastUsedObject(null);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tongs = player.getItemInHand(hand);
        if(!hasWorkpiece(tongs)) {
            InteractionHand oppHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack item = player.getItemInHand(oppHand);
            if(item.is(TagsNF.METAL_WORKPIECE)) {
                if(!level.isClientSide) {
                    tongs.getTag().putString("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
                    if(item.is(ItemsNF.IRON_BLOOM.get())) {
                        tongs.getTag().putBoolean("slagCenter", true);
                        tongs.getTag().putBoolean("slagLeft", true);
                        tongs.getTag().putBoolean("slagRight", true);
                    }
                    tongs.getTag().putInt("color", Metal.fromString(item.getItem().toString()).getColor().getRGB());
                    tongs.getTag().putFloat("temperature", 0);
                    int[] work = new int[11];
                    if(item.is(TagsNF.PLATES)) {
                        work[0] = 2;
                        work[3] = 2;
                        work[7] = 2;
                    }
                    else if(item.is(TagsNF.METAL_RODS)) {
                        work[1] = 2;
                        work[4] = 2;
                        work[8] = 2;
                    }
                    else if(!item.is(Tags.Items.INGOTS)) {
                        work[1] = -2;
                        work[4] = -2;
                        work[8] = -2;
                    }
                    tongs.getTag().putIntArray("work", work);
                    if(!player.getAbilities().instabuild) item.shrink(1);
                }
                player.swing(oppHand);
                player.playSound(SoundsNF.TONGS_HANDLE.get(), 1F, 0.975F + level.random.nextFloat() * 0.05F);
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
                IMetal workMetal = Metal.fromString(getWorkpiece(tongs).toString());
                if(getMaxHeatTier() != 5) temperature = Math.min(TieredHeat.fromTier(getMaxHeatTier() + 1).getBaseTemp() - 100, temperature);
                temperature = Math.min(temperature, workMetal.getMeltTemp());
                if(temperature > 100 && temperature >= getTemperature(tongs)) {
                    tongs.getTag().putFloat("temperature", temperature);
                    tongs.getTag().putInt("stableTempTicks", 20 * 45);
                    if(player != null) {
                        player.swing(context.getHand(), true);
                        level.playSound(null, player, SoundsNF.FIRE_WHOOSH.get(), SoundSource.PLAYERS, 1F, 1F);
                        if(tongs.getMaxDamage() - tongs.getDamageValue() == 1) LevelUtil.giveItemToPlayer(new ItemStack(workMetal.getMatchingItem(TagsNF.SCRAP)), player, true);
                        tongs.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        else if(hasWorkpiece(context.getItemInHand()) && state.getBlock() instanceof TieredAnvilBlock && context.getClickedFace() == Direction.UP && context.getClickLocation().y >= pos.getY() + 0.75) {
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
                            level.playSound(null, player, SoundsNF.TONGS_HANDLE.get(), SoundSource.PLAYERS, 1F, 0.975F + level.random.nextFloat() * 0.05F);
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
        if(worldIn.isClientSide) {
            if(isSelected) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player) && player.getMainHandItem() == stack) {
                    boolean hasWorkpiece = hasWorkpiece(stack);
                    boolean oldCanUse = ClientEngine.get().canUseModifiableMain;
                    ClientEngine.get().canUseModifiableMain = hasWorkpiece;
                    if(oldCanUse != ClientEngine.get().canUseModifiableMain) ModifiableScreen.updateSelection(this, hasWorkpiece ? getVisualRecipes(player.level, player, getWorkpiece(stack)) : List.of(), true);
                }
            }
            else if(itemSlot == 0) {
                if(entityIn instanceof Player player && PlayerData.isPresent(player) && player.getOffhandItem() == stack) {
                    boolean hasWorkpiece = hasWorkpiece(stack);
                    boolean oldCanUse = ClientEngine.get().canUseModifiableOff;
                    ClientEngine.get().canUseModifiableOff = hasWorkpiece;
                    if(oldCanUse != ClientEngine.get().canUseModifiableOff) ModifiableScreen.updateSelection(this, hasWorkpiece ? getVisualRecipes(player.level, player, getWorkpiece(stack)) : List.of(), false);
                }
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
        if(hasWorkpiece(pStack)) pTooltipComponents.add(new TranslatableComponent("item.nightfall.tongs.info_place").withStyle(ChatFormatting.AQUA));
        else pTooltipComponents.add(new TranslatableComponent("item.nightfall.tongs.info_hold").withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(new TranslatableComponent("action.smithing.context").withStyle(ChatFormatting.GRAY)
                .append(new TranslatableComponent("action.smithing." + AnvilAction.FLIP.name().toLowerCase()).withStyle(ChatFormatting.DARK_AQUA)));
    }

    @Override
    public void containerChanged(ItemStack item) {
        item.getTag().remove("stableTempTicks");
        item.getTag().remove("temperature");
    }

    public List<SmithingRecipe> getVisualRecipes(Level level, Player player, Item workpiece) {
        ForgeHooks.setCraftingPlayer(player);
        List<SmithingRecipe> recipes = level.getRecipeManager().getRecipesFor(SmithingRecipe.TYPE, new RecipeWrapper(new ItemStackHandlerNF(new ItemStack(workpiece))), level).stream()
                .sorted((r1, r2) -> {
                    if(r1.menuOrder < 0 && r2.menuOrder >= 0) return 1;
                    else if(r2.menuOrder < 0 && r1.menuOrder >= 0) return -1;
                    else if(r1.menuOrder == r2.menuOrder || r1.menuOrder < 0) return r1.getResultItem().getDescriptionId().compareTo(r2.getResultItem().getDescriptionId());
                    else return r1.menuOrder > r2.menuOrder ? 1 : -1;
                })
                .collect(Collectors.toList());
        ForgeHooks.setCraftingPlayer(null);
        recipes.add(0, null);
        return recipes;
    }

    @Override
    public void swapClient(Minecraft mc, ItemStack item, Player player, boolean mainHand) {
        if(hasWorkpiece(item)) ModifiableScreen.initSelection(mc, getVisualRecipes(player.level, player, getWorkpiece(item)), this, mainHand);
    }

    @Override
    public Optional<Screen> modifyStartClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand) {
        if(hasWorkpiece(item)) {
            List<SmithingRecipe> recipes = getVisualRecipes(player.level, player, getWorkpiece(item));
            if(recipes.size() <= 1) return Optional.empty();
            else return Optional.of(new TongsVisualRecipeScreen(PlayerData.get(player).isMainhandActive(), this, recipes));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Screen> modifyContinueClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        return Optional.empty();
    }

    @Override
    public void modifyReleaseClient(Minecraft mc, ItemStack item, Player player, InteractionHand hand, int heldTime) {
        if(mc.screen instanceof TongsVisualRecipeScreen) mc.screen.onClose();
    }

    @Override
    public int getBackgroundUOffset() {
        return ModifiableScreen.BUILDING_BACKGROUND;
    }

    @Override
    public @Nullable Object getLastUsedObject() {
        return ClientEngine.get().getLastVisualizedRecipe();
    }

    @Override
    public int getLastUsedPage() {
        return ClientEngine.get().getLastVisualizedRecipePage();
    }

    @Override
    public void setLastUsedObject(Object object) {
        ClientEngine.get().setLastVisualizedRecipe(object);
    }

    @Override
    public void setLastUsedPage(int page) {
        ClientEngine.get().setLastVisualizedRecipePage(page);
    }
}