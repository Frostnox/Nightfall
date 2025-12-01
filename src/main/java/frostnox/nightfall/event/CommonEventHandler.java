package frostnox.nightfall.event;

import com.google.common.collect.Multimap;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.block.*;
import frostnox.nightfall.block.block.crucible.CrucibleContainer;
import frostnox.nightfall.block.block.tree.TreeStemBlock;
import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.CrucibleRecipe;
import frostnox.nightfall.data.recipe.MixtureRecipe;
import frostnox.nightfall.entity.ITamable;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.item.item.AttributeAccessoryItem;
import frostnox.nightfall.item.item.MeleeWeaponItem;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.LevelDataToClient;
import frostnox.nightfall.network.message.capability.SetAccessoriesToClient;
import frostnox.nightfall.network.message.capability.StatusToClient;
import frostnox.nightfall.network.message.entity.HurtDirToClient;
import frostnox.nightfall.network.message.world.ChunkClimateToServer;
import frostnox.nightfall.network.message.world.ChunkDigProgressToServer;
import frostnox.nightfall.network.message.world.DigBlockToClient;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import frostnox.nightfall.util.*;
import frostnox.nightfall.world.ILightSource;
import frostnox.nightfall.world.MoonPhase;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getPlayer();
        if(player == null) return;
        if(!player.isAlive()) return;
        IPlayerData capP = PlayerData.get(player);
        if(!ActionTracker.get(player).isInactive() || player.getItemInHand(capP.getActiveHand()).getItem() instanceof MeleeWeaponItem) {
            event.setCanceled(true);
        }
        else if(event.getHand() == InteractionHand.MAIN_HAND) {
            Level level = player.level;
            BlockPos pos = event.getPos();
            IGlobalChunkData chunkData = GlobalChunkData.get(level.getChunkAt(pos));
            if(player.getAbilities().instabuild) {
                chunkData.removeBreakProgress(pos);
                if(!level.isClientSide) NetworkHandler.toAllTrackingAndSelf(player, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), -1));
                return;
            }
            event.setCanceled(true);
            BlockState block = level.getBlockState(pos);
            ItemStack item = event.getItemStack();
            boolean isTool = item.is(TagsNF.TOOL);
            float progress = level.getBlockState(pos).getDestroyProgress(player, level, pos) * AttributesNF.getStrengthMultiplier(player);
            if(!isTool) progress *= 0.66F;
            progress += chunkData.getBreakProgress(pos);
            block.attack(level, pos, player);
            if(progress >= 1F) {
                BlockEntity blockEntity = player.level.getBlockEntity(pos);
                if(!level.isClientSide) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(player.level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos);
                    if(exp == -1) {
                        NetworkHandler.toClient(serverPlayer, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
                        return;
                    }
                }
                boolean canHarvest = block.canHarvestBlock(player.level, pos, player);
                if(block.onDestroyedByPlayer(level, pos, player, canHarvest, block.getFluidState())) {
                    block.getBlock().destroy(level, pos, block);
                    if(level.isClientSide) ClientEngine.get().visuallyDestroyBlock(pos, -1);
                    else if(canHarvest) {
                        block.getBlock().playerDestroy(player.level, player, pos, block, blockEntity, item);
                        //This reintroduces an exploit where the player can use a high efficiency tool to dig then swap out right before breaking
                        //Won't fix this unless Nightfall introduces tools that don't function through actions for some reason
                        //For Nightfall's purposes, this line will never run and is purely a backup for other mods
                        if(!player.isCreative() && item.is(TagsNF.TOOL)) item.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    }
                    chunkData.removeBreakProgress(pos);
                }
            }
            else {
                if(level.isClientSide) ClientEngine.get().visuallyDestroyBlock(pos, (int) (progress * 10F) - 1);
                chunkData.setBreakProgress(pos, progress);
            }
            if(block.getDestroySpeed(level, pos) > 0F) {
                SoundType sound = block.getSoundType(player.level, pos, player);
                if(player.tickCount % 5 == 0) player.level.playSound(player, pos, sound.getHitSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2F, sound.getPitch() * 0.75F);
            }
            if(!level.isClientSide) {
                NetworkHandler.toAllTrackingAndSelf(player, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecificEvent(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getPlayer();
        if(player == null || event.getTarget() == null) return;
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        if(!capA.isInactive()) {
            if(capA.getActionID().equals(ActionsNF.HOLD_ENTITY.getId())) {
                capP.useBlockEntity(event.getTarget());
                event.setCancellationResult(InteractionResult.CONSUME); //Prevent further interaction events from firing
                event.setCanceled(true);
            }
            else if(!allowRightClick(player, capP.getActiveHand())) {
                event.setCancellationResult(InteractionResult.CONSUME); //Prevent further interaction events from firing
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
        if(!allowRightClick(event.getPlayer(), event.getHand())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if((player.level.isClientSide && ClientEngine.get().microHitResult != null) || (!player.getAbilities().instabuild && player.swingTime < 2 && player.swinging)) {
            event.setCanceled(true);
            return;
        }
        IPlayerData capP = PlayerData.get(player);
        if(event.getHand() == InteractionHand.MAIN_HAND) {
            if(player.isCrouching() && !player.level.isClientSide()) {
                IActionTracker capA = ActionTracker.get(player);
                if(capA.getActionID().equals(ActionsNF.HOLD_ENTITY.getId()) && capP.getHoldTicks() == 4) {
                    BlockPos pos = (event.getFace() == null || player.level.getBlockState(event.getPos()).getMaterial().isReplaceable()) ? event.getPos() : event.getPos().relative(event.getFace());
                    capP.putBlockEntity(pos, event.getHitVec());
                    event.setCanceled(true);
                }
                else if(capP.getHoldTicks() == -1 && capA.isInactive() && player.getItemInHand(event.getHand()).isEmpty() && player.level.getBlockEntity(event.getPos()) instanceof IHoldable holdable) {
                    capP.holdBlockEntity(holdable);
                    event.setCanceled(true);
                }
            }
            else if(ActionTracker.get(player).getActionID().equals(ActionsNF.HOLD_ENTITY.getId())) {
                capP.useBlockEntity(event.getPos());
                event.setCanceled(true);
            }
        }
        if(!allowRightClick(player, event.getHand())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerHarvestEvent(PlayerEvent.HarvestCheck event) {
        Player player = event.getPlayer();
        if(player == null || !player.isAlive()) return;
        IPlayerData capP = PlayerData.get(player);
        if(!capP.isMainhandActive() && player.getItemInHand(capP.getActiveHand()).isCorrectToolForDrops(event.getTargetBlock())) event.setCanHarvest(true);
        IActionTracker capA = ActionTracker.get(player);
        if(!capA.isInactive()) event.setCanHarvest(event.canHarvest() || capA.getAction().canHarvest(event.getTargetBlock()));
    }

    private static int getTooltipIndex(List<Component> tooltip) {
        if(tooltip.isEmpty()) return 0;
        int sub = 0;
        for(Component component : tooltip) {
            if(component.getStyle().getColor() != null && ChatFormatting.DARK_GRAY.getColor().equals(component.getStyle().getColor().getValue())) sub++;
            else if(component instanceof TranslatableComponent translatable) {
                if(translatable.getKey().equals("item.durability")) sub++;
            }
        }
        return Math.max(1, tooltip.size() - sub);
    }

    @SubscribeEvent
    public static void onItemTooltipEvent(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();
        ItemStack item = event.getItemStack();
        int hideFlags = item.getHideFlags();
        tooltip.removeIf(c -> c == TextComponent.EMPTY);
        if(ItemStack.shouldShowInTooltip(hideFlags, ItemStack.TooltipPart.ADDITIONAL)) {
            List<Component> groups = new ObjectArrayList<>();
            if(item.is(TagsNF.MEAT)) groups.add(new TranslatableComponent("nightfall.food_group.meat"));
            if(item.is(TagsNF.VEGETABLE)) groups.add(new TranslatableComponent("nightfall.food_group.vegetable"));
            if(item.is(TagsNF.FRUIT)) groups.add(new TranslatableComponent("nightfall.food_group.fruit"));
            if(item.is(TagsNF.GRAIN)) groups.add(new TranslatableComponent("nightfall.food_group.grain"));
            if(item.is(TagsNF.HERB)) groups.add(new TranslatableComponent("nightfall.food_group.herb"));
            if(!groups.isEmpty()) {
                int units = MixtureRecipe.getUnitsOf(new ItemStack(item.getItem(), 1));
                MutableComponent component;
                if(units == 1) component = new TranslatableComponent("food.small").withStyle(ChatFormatting.BLUE).append(" ");
                else if(units == 2) component = new TranslatableComponent("food.medium").withStyle(ChatFormatting.BLUE).append(" ");
                else if(units == 3) component = new TranslatableComponent("food.large").withStyle(ChatFormatting.BLUE).append(" ");
                else component = new TextComponent(units + " ").withStyle(ChatFormatting.BLUE);
                for(int i = 0; i < groups.size(); i++) {
                    component.append(groups.get(i));
                    if(i != groups.size() - 1) component.append(", ");
                }
                tooltip.add(getTooltipIndex(tooltip), component);
            }
            if(event.getPlayer() != null) {
                for(CrucibleRecipe recipe : event.getPlayer().level.getRecipeManager().getAllRecipesFor(CrucibleRecipe.TYPE)) {
                    if(recipe.getInput().test(item) && recipe.getResultFluid().getFluid() instanceof MetalFluid metalFluid) {
                        tooltip.add(getTooltipIndex(tooltip), new TextComponent(recipe.getResultFluid().getAmount() + " ").withStyle(ChatFormatting.BLUE)
                                .append(new TranslatableComponent("metal." + metalFluid.metal.getName())));
                        break;
                    }
                }
                if(item.is(TagsNF.HEAT_RESISTANT_ITEM_1)) {
                    IPlayerData capP = PlayerData.get(event.getPlayer());
                    if(capP.hasCompletedEntry(EntriesNF.POTTERY.getId())) {
                        int tier;
                        /*if(item.is(TagsNF.HEAT_RESISTANT_ITEM_4) && capP.hasEntry(null)) tier = 4;
                        else if(item.is(TagsNF.HEAT_RESISTANT_ITEM_3) && capP.hasEntry(null)) tier = 3;
                        else*/ if(item.is(TagsNF.HEAT_RESISTANT_ITEM_2) && capP.hasCompletedEntry(EntriesNF.SMELTING.getId())) tier = 2;
                        else tier = 1;
                        tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("block.heat_resistant." + tier)
                                .withStyle(Style.EMPTY.withColor(TieredHeat.fromTier(tier).color.getRGB())));
                    }
                }
            }
            if(item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof EntityBlock entityBlock &&
                    entityBlock.newBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState()) instanceof IHoldable) {
                tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("block.holdable").withStyle(ChatFormatting.BLUE).append(" ").append(
                        new TranslatableComponent("block.holdable.control").withStyle(ChatFormatting.AQUA)));
            }
        }
        if(ItemStack.shouldShowInTooltip(hideFlags, ItemStack.TooltipPart.MODIFIERS)) {
            boolean hasAnySlot = false;
            if(item.getEquipmentSlot() != null) hasAnySlot = true;
            else for(AccessorySlot slot : AccessorySlot.values()) {
                if(slot.acceptsItem(item)) {
                    hasAnySlot = true;
                    tooltip.add(getTooltipIndex(tooltip), (new TranslatableComponent("item.modifiers." + slot)).withStyle(ChatFormatting.GRAY));
                }
            }
            if(item.getItem() instanceof AttributeAccessoryItem accessoryItem) {
                for(AccessorySlot slot : AccessorySlot.values()) {
                    Multimap<Attribute, AttributeModifier> map = accessoryItem.getAttributeModifiers(slot, item);
                    if(map.isEmpty()) continue;
                    for(Map.Entry<Attribute, AttributeModifier> entry : map.entries()) {
                        Attribute attribute = entry.getKey();
                        boolean forcePercentage = attribute.equals(AttributesNF.BLEEDING_RESISTANCE.get()) || attribute.equals(AttributesNF.POISON_RESISTANCE.get())
                                || attribute.equals(AttributesNF.STAMINA_REDUCTION.get());
                        AttributeModifier modifier = entry.getValue();
                        double baseAmount = modifier.getAmount();
                        double finalAmount;
                        if(!forcePercentage && modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            if(attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) finalAmount = baseAmount * 10.0D;
                            else finalAmount = baseAmount;
                        }
                        else finalAmount = baseAmount * 100.0D;
                        if(baseAmount > 0.0D) {
                            tooltip.add(getTooltipIndex(tooltip), (new TranslatableComponent("attribute.modifier.plus." + (forcePercentage ? 2 : modifier.getOperation().toValue()),
                                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(finalAmount),
                                    new TranslatableComponent(attribute.getDescriptionId()))).withStyle(ChatFormatting.BLUE));
                        }
                        else if(baseAmount < 0.0D) {
                            tooltip.add(getTooltipIndex(tooltip), (new TranslatableComponent("attribute.modifier.take." + (forcePercentage ? 2 : modifier.getOperation().toValue()),
                                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-finalAmount),
                                    new TranslatableComponent(attribute.getDescriptionId()))).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
            if(hasAnySlot) {
                if(item.getItem() instanceof TieredArmorItem armor) {
                    if(armor.getAttributeModifiers(armor.slot, item).isEmpty()) tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.modifiers." + armor.slot.getName()).withStyle(ChatFormatting.GRAY));
                    if(armor.material.getPoise() != Poise.NONE) {
                        tooltip.add(new TranslatableComponent("poise." + armor.material.getPoise().getSerializedName()).withStyle(ChatFormatting.DARK_GREEN)
                                .append(" ").append(new TranslatableComponent(AttributesNF.POISE.get().getDescriptionId()).withStyle(ChatFormatting.BLUE)));
                    }
                    //Special replacements for style effects
                    for(Map.Entry<Attribute, AttributeModifier> entry : item.getAttributeModifiers(armor.slot).entries()) {
                        if(!entry.getKey().equals(AttributesNF.BLEEDING_RESISTANCE.get()) && !entry.getKey().equals(AttributesNF.POISON_RESISTANCE.get()) &&
                                !entry.getKey().equals(AttributesNF.STAMINA_REDUCTION.get())) continue;
                        AttributeModifier modifier = entry.getValue();
                        double base = modifier.getAmount();
                        double percentage = base * 100.0D;
                        TranslatableComponent replacement;
                        if(base >= 0.0D) {
                            replacement = (TranslatableComponent) new TranslatableComponent("attribute.modifier.plus.2", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percentage),
                                    new TranslatableComponent(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.GOLD);
                        }
                        else {
                            percentage *= -1.0D;
                            replacement = (TranslatableComponent) (new TranslatableComponent("attribute.modifier.take.2", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percentage),
                                    new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.GOLD);
                        }
                        for(int i = 0; i < tooltip.size(); i++) {
                            if(tooltip.get(i) instanceof TranslatableComponent translatable) {
                                for(Object arg : translatable.getArgs()) {
                                    if(arg instanceof TranslatableComponent tArg) {
                                        if(tArg.getKey().equals(entry.getKey().getDescriptionId())) {
                                            tooltip.set(i, replacement);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    DecimalFormat format = new DecimalFormat("0.0");
                    float durabilityPenalty = CombatUtil.getArmorDefenseDurabilityPenalty(item.getMaxDamage() - item.getDamageValue(), item.getMaxDamage());
                    boolean penalized = durabilityPenalty < 1F;
                    for(int i = 0; i < 6; i++) {
                        float defense = armor.material.getDefense(armor.slot, DamageType.values()[i].asArray(), false);
                        tooltip.add(getTooltipIndex(tooltip), new TextComponent(format.format(defense * 100 * durabilityPenalty) + "% ").withStyle(penalized ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN)
                                .append(RenderUtil.getDamageTypeText(DamageType.values()[i]).append(" ").append(new TranslatableComponent("item.armor.defense"))
                                        .withStyle(ChatFormatting.BLUE)));
                    }
                }
                if(item.getItem() instanceof ILightSource) {
                    tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.emits_light").withStyle(ChatFormatting.BLUE));
                }
                if(item.is(ItemsNF.MASK.get())) {
                    tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.nightfall.mask.info").withStyle(ChatFormatting.BLUE));
                }
                if(item.is(ItemsNF.WARDING_CHARM.get())) {
                    tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.nightfall.warding_charm.info").withStyle(ChatFormatting.BLUE));
                }
                for(Component component : tooltip) {
                    if(component instanceof TranslatableComponent translatable) {
                        boolean plus = translatable.getKey().contains("attribute.modifier.plus");
                        boolean take = translatable.getKey().contains("attribute.modifier.take");
                        if(plus || take) {
                            boolean invert = false;
                            for(Object arg : translatable.getArgs()) {
                                if(arg instanceof TranslatableComponent tArg) {
                                    if(tArg.getKey().contains(AttributesNF.STAMINA_REDUCTION.get().getDescriptionId())) invert = true;
                                    tArg.withStyle(translatable.getStyle());
                                }
                            }
                            if(plus) translatable.withStyle(invert ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN);
                            else translatable.withStyle(invert ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);
                        }
                    }
                }
            }
            if(item.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if(block.builtInRegistryHolder().is(TagsNF.HAS_PHYSICS)) {
                    int integrity = 0;
                    if(block.builtInRegistryHolder().is(TagsNF.SUPPORT_1)) integrity += 1;
                    if(block.builtInRegistryHolder().is(TagsNF.SUPPORT_2)) integrity += 2;
                    if(block.builtInRegistryHolder().is(TagsNF.SUPPORT_4)) integrity += 4;
                    if(block.builtInRegistryHolder().is(TagsNF.SUPPORT_8)) integrity += 8;
                    if(integrity > 0) tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.integrity", integrity).withStyle(ChatFormatting.DARK_AQUA));
                }
            }
            FoodProperties food = item.getFoodProperties(null);
            if(food != null) {
                float saturation = food.getSaturationModifier();
                tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.nutrition", food.getNutrition()).withStyle(ChatFormatting.DARK_GREEN));
                if(saturation < 0.2F) tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.low_saturation").withStyle(ChatFormatting.DARK_GREEN));
                else if(saturation < 0.4F) tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.medium_saturation").withStyle(ChatFormatting.DARK_GREEN));
                else tooltip.add(getTooltipIndex(tooltip), new TranslatableComponent("item.high_saturation").withStyle(ChatFormatting.DARK_GREEN));

                for(var pair : food.getEffects()) {
                    if(pair.getSecond() < 1F) continue;
                    MobEffectInstance effect = pair.getFirst();
                    MutableComponent tip = new TranslatableComponent(effect.getDescriptionId());
                    MobEffect mobeffect = effect.getEffect();
                    if(effect.getAmplifier() > 0) {
                        tip = new TranslatableComponent("potion.withAmplifier", tip, new TranslatableComponent("potion.potency." + effect.getAmplifier()));
                    }
                    if(effect.getDuration() > 20) {
                        tip = new TranslatableComponent("potion.withDuration", tip, MobEffectUtil.formatDuration(effect, 1));
                    }
                    tooltip.add(getTooltipIndex(tooltip), tip.withStyle(mobeffect.getCategory().getTooltipFormatting()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPotionAddedEvent(PotionEvent.PotionAddedEvent event) {
        MobEffectInstance effect = event.getPotionEffect();
        boolean bleeding = effect.getEffect() == EffectsNF.BLEEDING.get();
        boolean poison = effect.getEffect() == EffectsNF.POISON.get();
        if(bleeding || poison) {
            LivingEntity entity = event.getEntityLiving();
            if(entity.isAlive() && (entity instanceof Player || entity instanceof ActionableEntity)) {
                int duration = effect.getDuration();
                if(bleeding) duration = (int) Math.round(duration * (1 - entity.getAttributeValue(AttributesNF.BLEEDING_RESISTANCE.get())));
                else duration = (int) Math.round(duration * (1 - entity.getAttributeValue(AttributesNF.POISON_RESISTANCE.get())));
                if(event.getOldPotionEffect() != null) {
                    duration += event.getOldPotionEffect().getDuration();
                    effect.update(new MobEffectInstance(effect.getEffect(), duration,
                            effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon(), null));
                }
                else effect.update(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(),
                        effect.isVisible(), effect.showIcon(), null));
                IActionTracker capA = ActionTracker.get(entity);
                if(bleeding) {
                    capA.setBleedDuration(duration);
                    if(!entity.level.isClientSide) {
                        NetworkHandler.toAllTrackingAndSelf(entity, new StatusToClient(capA.getBleedDuration(), entity.getId(), StatusToClient.Status.BLEEDING));
                    }
                }
                else {
                    capA.setPoisonDuration(duration);
                    if(!entity.level.isClientSide) {
                        NetworkHandler.toAllTrackingAndSelf(entity, new StatusToClient(capA.getPoisonDuration(), entity.getId(), StatusToClient.Status.POISON));
                    }
                }
            }
        }
        else if(effect.getEffect() == EffectsNF.PARALYSIS.get()) {
            MobEffectInstance oldEffect = event.getOldPotionEffect();
            if(oldEffect != null) {
                effect.update(new MobEffectInstance(effect.getEffect(), Math.max(oldEffect.getDuration(), effect.getDuration()),
                        Math.min(2, Math.max(oldEffect.getAmplifier() + 1, effect.getAmplifier())), effect.isAmbient(), effect.isVisible(), effect.showIcon(), null));
            }
        }
    }

    @SubscribeEvent
    public static void onPotionRemoveEvent(PotionEvent.PotionRemoveEvent event) {
        MobEffectInstance instance = event.getPotionEffect();
        MobEffect effect = instance.getEffect();
        if(effect == EffectsNF.BLEEDING.get() || effect == EffectsNF.POISON.get()) {
            LivingEntity entity = event.getEntityLiving();
            if(entity.isAlive() && (entity instanceof Player || entity instanceof ActionableEntity)) {
                IActionTracker capA = ActionTracker.get(entity);
                if(effect == EffectsNF.BLEEDING.get()) {
                    capA.setBleedDuration(0);
                    if(!entity.level.isClientSide) {
                        NetworkHandler.toAllTrackingAndSelf(entity, new StatusToClient(0, entity.getId(), StatusToClient.Status.BLEEDING));
                    }
                }
                else {
                    capA.setPoisonDuration(0);
                    if(!entity.level.isClientSide) {
                        NetworkHandler.toAllTrackingAndSelf(entity, new StatusToClient(0, entity.getId(), StatusToClient.Status.POISON));
                    }
                }
            }
        }
        else if(effect == EffectsNF.INFESTED.get()) {
            LivingEntity entity = event.getEntityLiving();
            entity.level.playSound(null, entity, SoundsNF.SKARA_SWARM_HURT.get(), SoundSource.HOSTILE, 1F, 0.9F + entity.level.random.nextFloat() * 0.2F);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if(entity.isAlive()) {
            if(ActionTracker.isPresent(entity)) {
                IActionTracker capA = ActionTracker.get(entity);
                if(entity.level.isClientSide()) {
                    //Effect particles
                    /*if(entity.hasEffect(EffectsNF.MOON_BLESSING.get())) {
                        entity.level.addParticle(ParticleTypesNF.ESSENCE_MOON.get(), entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), (entity.getRandom().nextDouble() - 0.5D) * 2.0D, -entity.getRandom().nextDouble(), (entity.getRandom().nextDouble() - 0.5D) * 2.0D);
                    }*/
                    if(capA.getBleedDuration() > 0) {
                        ParticleOptions particle;
                        if(entity instanceof ActionableEntity actEntity) particle = actEntity.getHurtParticle();
                        else particle = ParticleTypesNF.BLOOD_RED.get();
                        if(particle != null) {
                            int mod = 15;
                            int duration = capA.getBleedDuration();
                            if(duration > 90 * 20) mod = 4;
                            else if(duration > 60 * 20) mod = 8;
                            else if(duration > 30 * 20) mod = 12;
                            if(entity.tickCount % mod == 0) {
                                entity.level.addParticle(particle, entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), 0, -8, 0);
                            }
                        }
                    }
                    if(capA.getPoisonDuration() > 0) {
                        if(entity.tickCount % 18 == 0) {
                            entity.level.addParticle(ParticleTypesNF.POISON.get(), entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), (entity.getRandom().nextDouble() - 0.5D) * 2.0D, -entity.getRandom().nextDouble(), (entity.getRandom().nextDouble() - 0.5D) * 2.0D);
                        }
                    }
                    MobEffectInstance infested = entity.getEffect(EffectsNF.INFESTED.get());
                    if(infested != null && infested.getDuration() > 0) { //Client won't remove effect after it expires so duration check is necessary
                        entity.level.addParticle(ParticleTypesNF.SKARA.get(), entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D),
                                entity.getRandom().nextFloat() * MathUtil.PI * 2, 0, 0);
                    }
                }
            }
        }
        if(!entity.level.isClientSide) {
            if(entity.hasEffect(EffectsNF.INFESTED.get()) && (entity.isOnFire() || entity.isEyeInFluid(FluidTags.WATER))) {
                entity.removeEffect(EffectsNF.INFESTED.get());
            }
        }
        if(entity.isOnGround() && entity.isSteppingCarefully()) { //Call stepOn function whenever an entity is on a block instead of only when not sneaking
            BlockState state = entity.level.getBlockState(entity.getOnPos());
            state.getBlock().stepOn(entity.level, entity.getOnPos(), state, entity);
        }
        if(!(entity instanceof ActionableEntity)) return;
        if(entity.animationSpeed == 1.5F) entity.animationSpeed = entity.animationSpeedOld;
    }

    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        if(!event.getWorld().isClientSide()) {
            if(event.getEntity() instanceof ItemEntity itemEntity) {
                if(itemEntity.getItem().is(TagsNF.FLUID_ITEM)) event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if(event.getEntityLiving() instanceof Player player) {
            IPlayerData capP = PlayerData.get(player);
            capP.dropBlockEntity();
            if(!player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) capP.getAccessoryInventory().dropAll();
        }
    }

    @SubscribeEvent
    public static void onLoadWorldEvent(WorldEvent.Load event) {
        if(event.getWorld() instanceof ServerLevel level) {
            RegistriesNF.buildActiveKnowledge(level);
            RegistriesNF.buildNaturalVegetation();
        }
    }

    private static boolean isSpawnAreaFlat(ContinentalChunkGenerator generator, BlockPos.MutableBlockPos pos) {
        int centerX = pos.getX(), centerZ = pos.getZ();
        for(int x = centerX - 1; x <= centerX + 1; x++) {
            pos.setX(x);
            for(int z = centerZ - 1; z <= centerZ + 1; z++) {
                pos.setZ(z);
                if(Math.abs(generator.getHeight(x, z) - pos.getY()) > 1) return false;
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onCreateSpawnPositionEvent(WorldEvent.CreateSpawnPosition event) {
        if(event.getWorld() instanceof ServerLevel level) {
            if(!(level.getChunkSource().getGenerator() instanceof ContinentalChunkGenerator generator)) return;
            int dist = 32, x = dist, z = dist, height;
            float temp, humidity;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int seaLevel = generator.getSeaLevel();
            //Search for first valid land block, very likely to be a beach
            while(true) {
                height = generator.getHeight(x, 0);
                if(height > seaLevel && height < seaLevel + 9) {
                    temp = generator.getTemperature(x, 0);
                    if(temp > ContinentalChunkGenerator.LOW_CLIMATE && temp < ContinentalChunkGenerator.HIGH_CLIMATE) {
                        humidity = generator.getHumidity(x, 0);
                        if(humidity > ContinentalChunkGenerator.LOW_CLIMATE && humidity < ContinentalChunkGenerator.HIGH_CLIMATE) {
                            pos.set(x, height, 0);
                            if(isSpawnAreaFlat(generator, pos)) break;
                        }
                    }
                }
                height = generator.getHeight(-x, 0);
                if(height > seaLevel && height < seaLevel + 9) {
                    temp = generator.getTemperature(-x, 0);
                    if(temp > ContinentalChunkGenerator.LOW_CLIMATE && temp < ContinentalChunkGenerator.HIGH_CLIMATE) {
                        humidity = generator.getHumidity(-x, 0);
                        if(humidity > ContinentalChunkGenerator.LOW_CLIMATE && humidity < ContinentalChunkGenerator.HIGH_CLIMATE) {
                            pos.set(-x, height, 0);
                            if(isSpawnAreaFlat(generator, pos)) break;
                        }
                    }
                }
                x += dist;
                height = generator.getHeight(0, z);
                if(height > seaLevel && height < seaLevel + 9) {
                    temp = generator.getTemperature(0, z);
                    if(temp > ContinentalChunkGenerator.LOW_CLIMATE && temp < ContinentalChunkGenerator.HIGH_CLIMATE) {
                        humidity = generator.getHumidity(0, z);
                        if(humidity > ContinentalChunkGenerator.LOW_CLIMATE && humidity < ContinentalChunkGenerator.HIGH_CLIMATE) {
                            pos.set(0, height, z);
                            if(isSpawnAreaFlat(generator, pos)) break;
                        }
                    }
                }
                height = generator.getHeight(0, -z);
                if(height > seaLevel && height < seaLevel + 9) {
                    temp = generator.getTemperature(0, -z);
                    if(temp > ContinentalChunkGenerator.LOW_CLIMATE && temp < ContinentalChunkGenerator.HIGH_CLIMATE) {
                        humidity = generator.getHumidity(0, -z);
                        if(humidity > ContinentalChunkGenerator.LOW_CLIMATE && humidity < ContinentalChunkGenerator.HIGH_CLIMATE) {
                            pos.set(0, height, -z);
                            if(isSpawnAreaFlat(generator, pos)) break;
                        }
                    }
                }
                z += dist;
            }
            event.getSettings().setSpawn(pos.immutable(), 0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo().getItem() instanceof TieredArmorItem armor
                && event.getSlot() != EquipmentSlot.MAINHAND && event.getSlot() != EquipmentSlot.OFFHAND && player.spawnInvulnerableTime <= 0
                && !event.getFrom().sameItemStackIgnoreDurability(event.getTo()) && armor.material.getSoundEvent() != null) {
            event.getEntityLiving().gameEvent(GameEvent.EQUIP);
            event.getEntityLiving().getLevel().playSound(null, event.getEntity(), armor.material.getSoundEvent(), SoundSource.PLAYERS, 1F, 1F);
        }
    }

    private static void playFallSound(LivingEntity entity) {
        BlockPos pos = entity.getOnPos();
        BlockState blockstate = entity.level.getBlockState(pos);
        if(!blockstate.isAir()) {
            SoundType soundtype = blockstate.getSoundType(entity.level, pos, entity);
            entity.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.25F, soundtype.getPitch());
        }
    }

    @SubscribeEvent
    public static void onLivingFallEvent(LivingFallEvent event) {
        float distance = event.getDistance(), damageMultiplier = event.getDamageMultiplier();
        if(event.getEntityLiving() instanceof Player player && distance > 1F && !player.isSilent() && Mth.ceil((distance * 0.5F - 3.0F) * damageMultiplier) <= 0) {
            playFallSound(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerFlyableFallEvent(PlayerFlyableFallEvent event) {
        //This will run instead of LivingFallEvent if player can fly
        if(event.getDistance() > 1F && !event.getPlayer().isSilent()) playFallSound(event.getPlayer());
    }

    @SubscribeEvent
    public static void onBlockNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
        if(event.getWorld() instanceof ServerLevel level && LevelData.isPresent(level)) {
            BlockState state = event.getState();
            if(state.isAir() || state.getMaterial().isLiquid()) {
                ChunkData.get(level.getChunkAt(event.getPos())).schedulePhysicsTickAround(event.getPos());
            }
            else if(state.is(TagsNF.HAS_PHYSICS)) {
                ChunkData.get(level.getChunkAt(event.getPos())).schedulePhysicsTick(event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeedEvent(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();
        float speed = event.getNewSpeed();
        if(player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) speed *= 2.5F;
        if(!player.isOnGround()) speed *= 5F;
        if(player.isAlive()) {
            IActionTracker capA = ActionTracker.get(player);
            if(!capA.isInactive()) {
                if(!capA.getAction().canHarvest(event.getState())) speed /= 4F;
                if(capA.getAction().is(TagsNF.SLOW_PLAYER_HARVEST_ACTION)) speed /= 2F;
                else if(capA.getAction().is(TagsNF.ADZE_ACTION)) speed *= 0.75F;
            }
        }
        BlockState state = event.getState();
        if(state.is(TagsNF.TREE_WOOD)) {
            int size = 0;
            BlockPos.MutableBlockPos pos = event.getPos().mutable();
            while(state.is(BlockTags.LOGS)) {
                if(state.getBlock() instanceof TreeStemBlock) {
                    switch(state.getValue(TreeStemBlock.TYPE)) {
                        case TOP, ROTATED_TOP ->  {
                            switch(state.getValue(TreeStemBlock.AXIS)) {
                                case X -> pos.setX(pos.getX() + 1);
                                case Y -> pos.setY(pos.getY() + 1);
                                case Z -> pos.setZ(pos.getZ() - 1);
                            }
                        }
                        case BOTTOM, ROTATED_BOTTOM -> {
                            switch(state.getValue(TreeStemBlock.AXIS)) {
                                case X -> pos.setX(pos.getX() - 1);
                                case Y -> pos.setY(pos.getY() - 1);
                                case Z -> pos.setZ(pos.getZ() + 1);
                            }
                        }
                        default -> pos.setY(pos.getY() + 1);
                    }
                }
                else pos.setY(pos.getY() + 1);
                size++;
                state = player.level.getBlockState(pos);
            }
            if(size > 1) speed *= 1F / (size * 0.8F);
        }
        event.setNewSpeed(speed);
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if(player == null) return;
        Level level = player.level;
        if(event.phase == TickEvent.Phase.END) {
            if(!level.isClientSide) {
                if(player.isAlive()) {
                    if(player.tickCount == 1) LevelUtil.warpServerPlayer(player, false);
                    if(player.getFoodData().getFoodLevel() <= 0 && !player.hasEffect(EffectsNF.STARVATION.get()) && !player.hasEffect(EffectsNF.STARVATION_1.get())) {
                        player.addEffect(new MobEffectInstance(EffectsNF.STARVATION_1.get(), 5 * 20, 0));
                    }
                }
            }
            else if(player.deathTime == 20) LevelUtil.warpClientPlayer(player, false);
        }
        if(!player.isAlive()) {
            return;
        }
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        Action action;
        if(event.phase == TickEvent.Phase.START) {
            capP.setTicksSinceHit(capP.getTicksSinceHit() + 1);
            //Reverse hurt animation
            if(player.animationSpeed == 1.5F) player.animationSpeed = player.animationSpeedOld;
            if(!level.isClientSide()) {
                //Record hand items
                if(!capP.getLastMainItem().sameItemStackIgnoreDurability(player.getMainHandItem())) capP.setLastMainItem();
                if(!capP.getLastOffItem().sameItemStackIgnoreDurability(player.getOffhandItem())) capP.setLastOffItem();
                if(!player.getAbilities().invulnerable) player.causeFoodExhaustion(0.00125F); //1.5 exhaustion per min
            }
            //Force out of charge state if player weapon is missing
            if(capA.isCharging() && !(player.getItemInHand(capP.getActiveHand()).getItem() instanceof IActionableItem)) capA.queue();
            action = capA.getAction();
            action.onTick(player);
            //Play attack sound on start of damage state
            if(action.getSound() != null && capA.getFrame() == 1 && !capA.hasHitPause() && capA.isDamaging() && player.getItemInHand(capP.getActiveHand()).getItem() instanceof IActionableItem) {
                player.playSound(action.isChargeable() && capA.getCharge() >=  Math.round(action.getMaxCharge() * 0.75F) ? action.getExtraSound().get() : action.getSound().get(), 1F, 1F + level.random.nextFloat(-0.03F, 0.03F));
                player.gameEvent(GameEventsNF.ACTION_SOUND);
            }
            //Update temperature
            if(!level.isClientSide || ClientEngine.get().getPlayer() == player) {
                float bodyTemp = capP.getTemperature();
                ILevelData levelData = LevelData.isPresent(level) ? LevelData.get(level) : null;
                IChunkData chunkData = levelData != null ? ChunkData.get(level.getChunkAt(player.blockPosition())) : null;
                float temp = levelData != null ? levelData.getSeasonalTemperature(chunkData, player.blockPosition()) : 0.5F;
                BlockPos eyePos = player.eyeBlockPosition();
                if(level.canSeeSky(eyePos)) {
                    if(LevelUtil.isDayTimeWithin(level, LevelUtil.MORNING_TIME, LevelUtil.NIGHT_TIME)) {
                        if(levelData != null) {
                            temp += switch(levelData.getWeather(chunkData, eyePos)) {
                                case CLEAR -> 0.1F;
                                case CLOUDS -> 0.05F;
                                case RAIN -> -0.25F;
                                case SNOW -> -0.1F;
                                case FOG -> 0F;
                            };
                        }
                        else if(level.isRainingAt(eyePos)) temp -= 0.2F;
                    }
                }
                float heatTemp = capP.getCachedHeatTemperature();
                if(player.tickCount % 20 == 0 || heatTemp < 0) {
                    BlockPos feetPos = player.blockPosition();
                    AABB playerBox = player.getBoundingBox();
                    float minDistSqr = 2F * 2F;
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(feetPos.getX() - 2, feetPos.getY() - 1, feetPos.getZ() - 2);
                    int ySize = (feetPos.getY() == eyePos.getY() ? 2 : 3);
                    for(int i = 0; i < 5; i++) {
                        for(int j = 0; j < 5; j++) {
                            LevelChunk chunk = level.getChunkAt(pos);
                            for(int k = 0; k < ySize; k++) {
                                if(chunk.getBlockState(pos).getBlock() instanceof IHeatSource) {
                                    float distSqr = MathUtil.getShortestDistanceSqrPointToBox(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, playerBox);
                                    if(distSqr < minDistSqr) {
                                        minDistSqr = distSqr;
                                        heatTemp = 0.4F * (1F - (distSqr / (2 * 2)));
                                    }
                                }
                                pos.setY(pos.getY() + 1);
                            }
                            pos.setZ(pos.getZ() + 1);
                        }
                        pos.setX(pos.getX() + 1);
                    }
                    capP.setCachedHeatTemperature(Math.max(0, heatTemp));
                }
                if(heatTemp < 0.05F && player.isHolding((stack) -> stack.is(TagsNF.WARMING_ITEM))) heatTemp = 0.05F;
                temp += heatTemp;
                if(player.isOnFire()) temp += 1F;
                if(player.isInWater()) temp -= 0.25F;
                float tempChange = Mth.clamp(Math.abs(temp - bodyTemp) / 1000, 0.0001F, 0.0005F);
                bodyTemp = temp > bodyTemp ? Math.min(bodyTemp + tempChange, temp) : Math.max(bodyTemp - tempChange, temp);
                Nightfall.LOGGER.info(bodyTemp);
                capP.setTemperature(bodyTemp);
            }
            //Update accessories
            if(!level.isClientSide()) {
                AccessoryInventory accessories = capP.getAccessoryInventory();
                List<Pair<AccessorySlot, ItemStack>> dirtyAccessories = new ObjectArrayList<>(0);
                for(AccessorySlot slot : AccessorySlot.values()) {
                    ItemStack oldItem = capP.getLastAccessory(slot);
                    ItemStack newItem = accessories.getItem(slot);
                    if(!ItemStack.matches(oldItem, newItem)) {
                        if(oldItem.getItem() instanceof AttributeAccessoryItem attributeItem) {
                            player.getAttributes().removeAttributeModifiers(attributeItem.getAttributeModifiers(slot, oldItem));
                        }
                        if(newItem.getItem() instanceof AttributeAccessoryItem attributeItem) {
                            player.getAttributes().addTransientAttributeModifiers(attributeItem.getAttributeModifiers(slot, newItem));
                        }
                        ItemStack copiedItem = newItem.copy();
                        capP.setLastAccessory(slot, copiedItem);
                        dirtyAccessories.add(Pair.of(slot, copiedItem));
                    }
                }
                if(!dirtyAccessories.isEmpty()) {
                    NetworkHandler.toAllTracking(player, new SetAccessoriesToClient(dirtyAccessories, player.getId()));
                }
            }
            capA.tick();
            return;
        }
        if(!level.isClientSide() && player.isInWater()) LightData.get(player).inWaterTickServer();
        capP.setPunchTicks(capP.getPunchTicks() - 1);
        //Drop held entity if stunned
        if(capA.isStunned()) capP.dropBlockEntity();
        //Stop DoTs and blocks from syncing player position
        if(player.hurtMarked && player.getLastDamageSource() instanceof DamageTypeSource damageTypeSource
                && (damageTypeSource.isDoT() || damageTypeSource.isFromBlock())) player.hurtMarked = false;
        if(level.isClientSide()) {
            //Minimize walk distance during dodges to smooth view bobbing
            if(player.tickCount - Math.max(ClientEngine.get().lastDashTick, capP.getLastDodgeTick()) < 4) player.walkDist = Math.min(player.walkDist, player.walkDistO + 0.13F);
        }
        else {
            capP.updateExpandableInventory(false);
            //Fix for MC-26678: Damage wobble no longer shows direction of incoming damage
            if(player.hurtMarked) NetworkHandler.toClient((ServerPlayer) player, new HurtDirToClient(player.hurtDir, player.getId()));
        }

        if(player.isFallFlying() && player.isInWater()) player.stopFallFlying();
        if(AnimationUtil.getClimbProgress(player, 1) > 0.0F) {
            player.setSprinting(false);
        }
        //Remove y velocity when climbing on the server since it will build up from gravity as the player isn't on the ground
        if(capP.isClimbing()) {
            if(!level.isClientSide()) player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            player.resetFallDistance();
        }

        //Crawling poses
        if(capP.isCrawling() || (player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem weapon && capA.getActionID().equals(weapon.getActionSet(player).crawl.getId()))) {
            if(player.isPassenger() || player.isSleeping() || player.isSpectator()) {
                capP.setCrawling(false);
                player.setPose(Pose.STANDING);
                player.setForcedPose(null);
            }
            else {
                Pose pose;
                if(player.isFallFlying()) {
                    pose = Pose.FALL_FLYING;
                    //Keep swim amount synced up with fall flying amount in case player exits pose into crawling later
                    player.swimAmount = Math.min(1F, player.getFallFlyingTicks() / 10F);
                    player.swimAmountO = player.swimAmount;
                }
                else pose = Pose.SWIMMING;
                player.setPose(pose);
                player.setForcedPose(pose); //Force to prevent pose flashing from server delay
                if(player.isEyeInFluid(FluidTags.WATER)) {
                    capP.setCrawling(false);
                    player.setSprinting(true);
                }
                else {
                    player.setSprinting(false);
                    if(pose != Pose.FALL_FLYING) capP.setCrawling(true);
                }
            }
        }
        else player.setForcedPose(null); //Clear forced pose after crawl

        //Climbing ticks
        if(capP.isClimbing()) capP.setClimbTicks(Math.min(capP.getClimbTicks() + 1, 4));
        else capP.setClimbTicks(Math.max(-1, capP.getClimbTicks() - 1));
        //Airborne ticks
        if(!player.isOnGround() && !player.isPassenger() && !player.onClimbable() && !capP.isClimbing() && !player.isFallFlying()) capP.setAirTicks(Math.min(capP.getAirTicks() + 1, 9));
        else capP.setAirTicks(Math.max(-1, capP.getAirTicks() - 2));
        //Crouch ticks
        if(player.isCrouching()) capP.setCrouchTicks(Math.min(capP.getCrouchTicks() + 1, 4));
        else capP.setCrouchTicks(Math.max(-1, capP.getCrouchTicks() - 1));
        //Hold ticks
        if(!capP.getHeldContents().isEmpty()) capP.setHoldTicks(Math.min(capP.getHoldTicks() + 1, 4));
        else capP.setHoldTicks(Math.max(-1, capP.getHoldTicks() - 1));

        //Update action
        action = capA.getAction();
        //Reset dug block flag (want to retain for as long as possible so wait until first frame of second state)
        if(capA.getState() == 1 && capA.getFrame() == 1 && !capA.isStunnedOrHitPaused()) {
            capP.setDugBlock(false);
            capP.setHitStopFrame(-1);
        }
        //Predict hitstop for other players on client
        if(level.isClientSide && player != ClientEngine.get().getPlayer()) {
            ItemStack stack = player.getItemInHand(capP.getActiveHand());
            if(capA.isDamaging() && capP.getHitStopFrame() == -1 && capA.getFrame() == action.getBlockHitFrame(capA.getState(), player)) {
                if(stack.is(TagsNF.GRID_INTERACTABLE) && isHittingMicroGrid(player, level)) {
                    capP.setHitStopFrame(capA.getFrame());
                }
                else if(stack.getItem() instanceof IActionableItem) {
                    if(action.canHarvest() && capA.getLivingEntitiesHit() == 0) {
                        Vec3 eyePos = player.getEyePosition();
                        Vec3 viewVec = player.getViewVector(1F);
                        double reach = player.getReachDistance();
                        Vec3 endPos = eyePos.add(viewVec.x * reach, viewVec.y * reach, viewVec.z * reach);
                        BlockHitResult hitResult = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                        if(hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos pos = hitResult.getBlockPos();
                            boolean canMineAny = !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_SICKLE) && !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_DAGGER) && !action.harvestableBlocks.equals(BlockTags.MINEABLE_WITH_AXE);
                            if(canMineAny || action.canHarvest(level.getBlockState(pos))) {
                                IGlobalChunkData chunkData = GlobalChunkData.get(level.getChunkAt(pos));
                                float progress = chunkData.getBreakProgress(pos) + level.getBlockState(pos).getDestroyProgress(player, level, pos) *
                                        AttributesNF.getStrengthMultiplier(player) * capA.getChargeDestroyProgressMultiplier();
                                if(progress < 1F && !level.getBlockState(pos).getMaterial().isReplaceable() && !stack.is(TagsNF.NO_HITSTOP)) {
                                    capP.setHitStopFrame(capA.getFrame());
                                }
                            }
                        }
                    }
                }
            }
        }
        //Nightfall.LOGGER.info(capA.getActionID() + ": " + capA.getState() + " at " + capA.getProgress(1F) + " with hand " + capP.getActiveHand() + " with charge " + capA.getCharge());

        if(!level.isClientSide()) {
            if(LevelUtil.isDay(level)) capP.setUndeadKilledThisNight(0);
            capP.tickStamina();
            capP.tickRevelatoryKnowledge();
            //Make sure action startup is valid, very quick item swapping while trying to attack can trigger this
            if(!capA.isInactive() && !capA.isStunned() && capA.getState() == 0 && capA.getFrame() <= 2 && player.getItemInHand(capP.getActiveHand()).getItem() instanceof IActionableItem item) {
                if(!item.hasAction(capA.getActionID(), player)) {
                    capA.stunServer(CombatUtil.STUN_MEDIUM, false);
                }
            }
        }
        if(!capA.isInactive()) {
            if(!action.allowSprinting()) player.setSprinting(false);
            if(!action.allowCrawling()) {
                if(player.isFallFlying()) player.stopFallFlying();
                if(player.getPose() != Pose.STANDING && player.getPose() != Pose.CROUCHING) capA.stunServer(CombatUtil.STUN_LONG, false);
            }
        }
        if(capA.isStunned()) {
            if(player.isFallFlying()) player.stopFallFlying();
            CombatUtil.addTransientMultiplier(player, player.getAttribute(Attributes.MOVEMENT_SPEED), -0.5F, CombatUtil.STUN_SLOW_ID, "stun_slow");
        }
        else {
            CombatUtil.removeTransientModifier(player, player.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.STUN_SLOW_ID);
        }
        //Stops the body from turning to the side when dodging left/right unless it was already faced that way
        if(player.tickCount - capP.getLastDodgeTick() < 4) {
            player.yBodyRot = player.yBodyRotO;
        }
        if(!capA.isInactive() && !ActionsNF.isEmpty(capA.getActionID()) && !capA.isStunned() && !action.getRegistryName().equals(ActionsNF.HOLD_ENTITY.getId())) {
            CombatUtil.alignBodyRotWithHead(player, capA);
        }
        else if(capP.isClimbing() || player.onClimbable()) CombatUtil.alignBodyRotWithHead(player, capP);
        capA.setLastPosition(player.position());
        //Knowledge triggers
        if(!level.isClientSide) {
            if(LevelUtil.isNight(level)) {
                MoonPhase moonPhase = MoonPhase.get(level);
                if(moonPhase != MoonPhase.NEW) capP.addRevelatoryKnowledge(KnowledgeNF.UNDEAD_PRESENCE.getId());
                if(moonPhase == MoonPhase.FULL) capP.addRevelatoryKnowledge(KnowledgeNF.ESSENCE.getId());
            }
            if(player.containerMenu instanceof CrucibleContainer crucibleContainer) {
                for(FluidStack fluid : crucibleContainer.entity.fluids) {
                    if(fluid.getFluid() instanceof MetalFluid metalFluid) {
                        if(metalFluid.metal.getCategory() == IMetal.Category.HARD) capP.addKnowledge(KnowledgeNF.MELTED_HARD_METAL.getId());
                        if(metalFluid.metal.getCategory() == IMetal.Category.HARD || metalFluid.metal.getCategory() == IMetal.Category.MYSTIC) {
                            capP.addKnowledge(KnowledgeNF.MELTED_CASTABLE_METAL.getId());
                        }
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTickEvent(TickEvent.WorldTickEvent event) {
        if(event.world instanceof ServerLevel level && event.phase == TickEvent.Phase.END && LevelData.isPresent(level)) {
            ILevelData cap = LevelData.get(level);
            cap.tick();
            if(level.getServer().getTickCount() % 1000 == 0) { //Sync time occasionally
                Object message = new LevelDataToClient(cap.writeNBTSync(new CompoundTag()));
                for(ServerPlayer player : level.players()) NetworkHandler.toClient(player, message);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoadEvent(ChunkEvent.Load event) {
        if(event.getChunk() instanceof LevelChunk chunk) {
            Level level = (Level) event.getWorld();
            if(LevelData.isPresent(level)) {
                if(level.isClientSide()) {
                    NetworkHandler.toServer(new ChunkClimateToServer(chunk)); //Request climate data
                }
                else {
                    ServerLevel serverLevel = (ServerLevel) event.getWorld();
                    IChunkData capC = ChunkData.get(chunk);
                    //Fill climate data for new chunks
                    if(capC.isNew()) {
                        if(serverLevel.getChunkSource().getGenerator() instanceof ContinentalChunkGenerator generator) {
                            int i = chunk.getPos().getMinBlockX(), j = chunk.getPos().getMinBlockZ();
                            //Sample points
                            for(int x = 0; x < 16; x += 5) {
                                for(int z = 0; z < 16; z += 5) {
                                    int worldX = x + i;
                                    int worldZ = z + j;
                                    capC.setTemperature(x, z, generator.getTemperature(worldX, worldZ));
                                    capC.setHumidity(x, z, generator.getHumidity(worldX, worldZ));
                                }
                            }
                        }
                        else {
                            for(int x = 0; x < 16; x += 5) {
                                for(int z = 0; z < 16; z += 5) {
                                    capC.setTemperature(x, z, 0.5F);
                                    capC.setHumidity(x, z, 0.5F);
                                }
                            }
                        }
                        capC.setOld();
                    }

                    long loadDay = capC.getLastLoadedDayTime();
                    if(LevelUtil.hasPassedNight(loadDay, serverLevel)) {
                        capC.setSpawnedUndead(false);
                        chunk.setUnsaved(false);
                    }
                }
            }
            if(level.isClientSide) {
                NetworkHandler.toServer(new ChunkDigProgressToServer(chunk)); //Request since client may not have chunk loaded yet when server expects it to be
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnloadEvent(ChunkEvent.Unload event) {
        if(event.getChunk() instanceof LevelChunk chunk && LevelData.isPresent((Level) event.getWorld())) {
            if(event.getWorld() instanceof ServerLevel level) {
                IChunkData cap = ChunkData.get(chunk);
                cap.setLastLoadedDayTime(level.getDayTime());
                chunk.setUnsaved(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityStartRidingEvent(EntityMountEvent event) {
        Entity rider = event.getEntityMounting();
        if(!rider.level.isClientSide) {
            if(event.isMounting()) {
                if(rider instanceof Player player) {
                    Nightfall.LOGGER.info(player.tickCount + ", " + rider.fallDistance);
                    if(!player.isCreative() && !rider.isOnGround() && !rider.isInWater() && !rider.isInLava()) event.setCanceled(true);
                }
                else if(event.getEntityBeingMounted() instanceof Boat) {
                    if(!rider.getType().is(TagsNF.BOAT_PASSENGER) || (rider instanceof ITamable tamable && !tamable.isTamed())) event.setCanceled(true);
                }
            }
            else {
                if(!event.isCanceled()) {
                    Entity vehicle = event.getEntityBeingMounted();
                    if(vehicle.fallDistance > rider.fallDistance) rider.fallDistance = vehicle.fallDistance;
                }
            }
        }
    }

    private static boolean allowRightClick(Player player, InteractionHand hand) {
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        if(hand.equals(InteractionHand.MAIN_HAND) && !capP.isMainhandActive()) {
            ItemStack item = player.getOffhandItem();
            if(!item.equals(ItemStack.EMPTY)) return false;
        }
        else if(capP.isMainhandActive() && hand.equals(InteractionHand.OFF_HAND)) return false;
        return capA.isInactive() || capA.getAction().isInterruptible() || !capP.getHeldContents().isEmpty();
    }

    private static boolean isHittingMicroGrid(Player player, Level level) {
        int searchReach = 3; //Search reach could be higher to find BlockEntities that are further away than their grids
        int playerReach = 2;
        Vector3f look = new Vector3f(player.getViewVector(1F));
        Vec3 startVec = player.getEyePosition();
        look.mul(playerReach);
        Vec3 endVec = startVec.add(look.x(), look.y(), look.z());
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        for(int x = -searchReach; x <= searchReach; x++) {
            for(int y = -searchReach; y <= searchReach; y++) {
                for(int z = -searchReach; z <= searchReach; z++) {
                    blockPos.set(startVec.x + x, startVec.y + y, startVec.z + z);
                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if(blockEntity instanceof IMicroGrid gridEntity) {
                        float rot = -gridEntity.getRotationDegrees();
                        Vec3 gridPos = gridEntity.getWorldPos(blockPos, 0, 0, 0);
                        Vector3f gridSize = MathUtil.rotatePointByYaw(new Vector3f(gridEntity.getGridXSize()/16F, gridEntity.getGridYSize()/16F, gridEntity.getGridZSize()/16F), rot);
                        //Check first that anywhere on the grid is being looked at and LoS is available
                        Optional<Vec3> prelimHitVec = new AABB(gridPos, gridPos.add(gridSize.x(), gridSize.y(), gridSize.z())).clip(startVec, endVec);
                        if(prelimHitVec.isEmpty()) continue;
                        else if(level.clip(new ClipContext(startVec, prelimHitVec.get(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
                        for(int gridX = 0; gridX < gridEntity.getGridXSize(); gridX++) {
                            for(int gridY = 0; gridY < gridEntity.getGridYSize(); gridY++) {
                                for(int gridZ = 0; gridZ < gridEntity.getGridZSize(); gridZ++) {
                                    Vector3f selectPos = MathUtil.rotatePointByYaw(new Vector3f(gridX/16F, gridY/16F, gridZ/16F), rot);
                                    Vector3f selectSize = MathUtil.rotatePointByYaw(new Vector3f(1/16F, 1/16F, 1/16F), rot);
                                    Vec3 finalPos = gridPos.add(selectPos.x(), selectPos.y(), selectPos.z());
                                    Optional<Vec3> hitVec = new AABB(finalPos, finalPos.add(selectSize.x(), selectSize.y(), selectSize.z())).clip(startVec, endVec);
                                    if(gridEntity.getGrid()[gridX][gridY][gridZ] && hitVec.isPresent()) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
