package frostnox.nightfall.item.item;

import frostnox.nightfall.action.*;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.block.IMetal;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.HeldToolRecipe;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.item.*;

import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.ToolActionsNF;
import frostnox.nightfall.item.Weight;
import it.unimi.dsi.fastutil.floats.FloatImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MeleeWeaponItem extends ToolItem implements IWeaponItem {
    public final PlayerActionSet actionSet;
    public final ITieredItemMaterial material;
    public final boolean canDig;
    private final HurtSphere spheres, npcSpheres;
    private final DamageType[] defaultType;
    private final List<ToolAction> toolActions;
    private final List<Float> defense;

    public MeleeWeaponItem(ITieredItemMaterial material, PlayerActionSet actionSet, HurtSphere spheres, HurtSphere npcSpheres, boolean canDig, float durabilityMultiplier, Properties builder, List<ToolAction> toolActions, DamageType... defaultType) {
        super(builder.defaultDurability(Math.round(material.getUses() * durabilityMultiplier)));
        this.actionSet = actionSet;
        this.material = material;
        this.spheres = spheres;
        this.npcSpheres = npcSpheres;
        this.toolActions = toolActions;
        this.defaultType = defaultType;
        this.canDig = canDig;
        IMetal metal = material.getMetal();
        if(metal != null) {
            List<Float> baseDefenses = metal.getBaseDefenses();
            float[] temp = new float[6];
            for(int i = 0; i < temp.length; i++) temp[i] = baseDefenses.get(i) * actionSet.defenseMul;
            this.defense = FloatImmutableList.of(temp);
        }
        else this.defense = FloatImmutableList.of(0, 0, 0, 0, 0, 0);
    }

    public MeleeWeaponItem(ITieredItemMaterial material, PlayerActionSet actionSet, HurtSphere spheres, HurtSphere npcSpheres, boolean canDig, Properties builder, List<ToolAction> toolActions, DamageType... defaultType) {
        this(material, actionSet, spheres, npcSpheres, canDig, 1, builder, toolActions, defaultType);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        if(canDig) {
            float speed = material.getSpeed();
            if(actionSet.basic.get().is(TagsNF.ADZE_ACTION)) speed *= 0.75F;
            if(actionSet.basic.get().is(TagsNF.SLOW_PLAYER_HARVEST_ACTION)) speed *= 0.5F;
            tooltips.add(new TranslatableComponent("item.dig_speed", (int) speed).withStyle(ChatFormatting.DARK_GREEN));
        }
        int size = tooltips.size();
        List<Component> basic = actionSet.basic.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < basic.size(); i++) {
            MutableComponent text = new TranslatableComponent("action.basic");
            if(ClientEngine.get().isShiftHeld()) text.append(new TranslatableComponent("action.control", ClientEngine.get().getAttackKeyName()).withStyle(ChatFormatting.AQUA));
            text.append(": ");
            if(i == 0) tooltips.add(text.append(basic.get(i)));
            else tooltips.add(basic.get(i));
        }
        List<Component> alternate = actionSet.alternate.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < alternate.size(); i++) {
            MutableComponent text = new TranslatableComponent("action.alternate");
            if(ClientEngine.get().isShiftHeld()) text.append(new TranslatableComponent("action.control_held", ClientEngine.get().getAttackKeyName()).withStyle(ChatFormatting.AQUA));
            text.append(": ");
            if(i == 0) tooltips.add(text.append(alternate.get(i)));
            else tooltips.add(alternate.get(i));
        }
        List<Component> technique = actionSet.defaultTech.get().getTooltips(pStack, pLevel, pIsAdvanced);
        for(int i = 0; i < technique.size(); i++) {
            MutableComponent text = new TranslatableComponent("action.technique");
            if(ClientEngine.get().isShiftHeld()) text.append(new TranslatableComponent("action.control", ClientEngine.get().getUseKeyName()).withStyle(ChatFormatting.AQUA));
            text.append(": ");
            if(i == 0) tooltips.add(text.append(technique.get(i)));
            else tooltips.add(technique.get(i));
        }
        boolean doExpandPrompt = tooltips.size() != size;
        if(!toolActions.isEmpty()) {
            TranslatableComponent toolActionsText = new TranslatableComponent("item.toolactions");
            for(int i = 0; i < toolActions.size(); i++) {
                toolActionsText.append(new TranslatableComponent("toolaction." + toolActions.get(i).name()).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
                if(i != toolActions.size() - 1) toolActionsText.append(", ");
            }
            tooltips.add(toolActionsText);
        }
        appendExtraHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        if(!ClientEngine.get().isShiftHeld() && doExpandPrompt) {
            tooltips.add(new TranslatableComponent("tooltip.expand_prompt").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
        }
    }

    protected void appendExtraHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {

    }

    @Override
    public boolean hasAction(ResourceLocation id, Player player) {
        return getActionSet(player).containsAction(id);
    }

    @Override
    public float getDamageMultiplier() {
        return material.getDamageMultiplier();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState block) {
        return material.getSpeed();
    }

    @Override
    public Weight getWeight() {
        return material.getWeight();
    }

    @Override
    public HurtSphere getHurtSpheres() {
        return spheres;
    }

    @Override
    public HurtSphere getNPCHurtSpheres() {
        return npcSpheres;
    }

    @Override
    public DamageType[] getDefaultDamageTypes() {
        return defaultType;
    }

    @Override
    public @Nullable AttackEffect getBaseAttackEffect() {
        return getBaseActionSet().defaultEffect;
    }

    @Override
    public PlayerActionSet getBaseActionSet() {
        return actionSet;
    }

    @Override
    public PlayerActionSet getActionSet(Player player) {
        return getBaseActionSet();
    }

    @Override
    public float getBaseDamage() {
        return getBaseActionSet().attack * material.getDamageMultiplier();
    }

    @Override
    public boolean tryBasicAttack(Player user) {
        if(!canExecuteAttack(user, false)) return false;
        IActionTracker capA = ActionTracker.get(user);
        Action action = capA.getAction();
        PlayerActionSet set = getActionSet(user);
        if(!ActionsNF.get(set.basic.getId()).canStart(user) || (!action.isEmpty() && !action.isActionEqualOrLinked(set.basic.getId()))) return false;
        if(!action.getChain(user).get().isEmpty()) {
            if(capA.getState() == action.getChainState() || (capA.getState() == action.getChainState() - 1 && capA.getFrame() >= capA.getDuration() - 3)) {
                capA.queue();
                return true;
            }
        }
        else if(capA.isInactive()) {
            capA.startAction(set.basic.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean tryAlternateAttack(Player user) {
        if(!canExecuteAttack(user, false)) return false;
        IActionTracker capA = ActionTracker.get(user);
        Action action = capA.getAction();
        PlayerActionSet set = getActionSet(user);
        if(!ActionsNF.get(set.alternate.getId()).canStart(user) || (!action.isEmpty() && !action.isActionEqualOrLinked(set.alternate.getId()))) return false;
        if(!action.getChain(user).get().isEmpty()) {
            if(capA.getState() == action.getChainState() || (capA.getState() == action.getChainState() - 1 && capA.getFrame() >= capA.getDuration() - 3)) {
                capA.queue();
                return true;
            }
        }
        else if(capA.getState() == action.getChargeState()) {
            capA.queue();
            return true;
        }
        else if(capA.isInactive()) {
            capA.startAction(set.alternate.getId());
            return true;
        }
        return false;
    }

    private boolean canDoRecipe(Player user, Action action, PlayerActionSet set) {
        if(set.recipeAction == null) return false;
        else if(ActionsNF.get(set.recipeAction.getId()).canStart(user) && (action.isEmpty() || action.isActionEqualOrLinked(set.recipeAction.getId()))) {
            IPlayerData capP = PlayerData.get(user);
            List<ToolIngredientRecipe> recipes = getRecipes(user.level, user, user.getItemInHand(capP.getOppositeActiveHand()));
            int index = LevelUtil.getModifiableItemIndex(user.level, user, capP.getActiveHand());
            return index >= 0 && index < recipes.size();
        }
        return false;
    }

    private boolean canDoTechnique(Player user, Action action, PlayerActionSet set) {
        return ActionsNF.get(set.defaultTech.getId()).canStart(user) && (action.isEmpty() || action.isActionEqualOrLinked(set.defaultTech.getId()));
    }

    @Override
    public boolean tryTechnique(Player user) {
        if(!canExecuteAttack(user, false)) return false;
        IActionTracker capA = ActionTracker.get(user);
        Action action = capA.getAction();
        PlayerActionSet set = getActionSet(user);
        if(capA.getState() == action.getChargeState()) {
            if(canDoTechnique(user, action, set) || canDoRecipe(user, action, set)) {
                capA.queue();
                return true;
            }
        }
        else if(!action.getChain(user).get().isEmpty()) {
            if(capA.getState() == action.getChainState()) {
                if(canDoTechnique(user, action, set) || canDoRecipe(user, action, set)) {
                    capA.queue();
                    return true;
                }
            }
        }
        else if(capA.isInactive()) {
            if(canDoRecipe(user, action, set)) {
                    capA.startAction(getRecipeAction().getId());
                    return true;
            }
            if(canDoTechnique(user, action, set)) {
                capA.startAction(set.defaultTech.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean tryCrawlingAttack(Player user) {
        if(!canExecuteAttack(user, true)) return false;
        IActionTracker capA = ActionTracker.get(user);
        Action action = capA.getAction();
        PlayerActionSet set = getActionSet(user);
        if(!action.canStart(user) || (!action.isEmpty() && !action.isActionEqualOrLinked(set.crawl.getId()))) return false;
        if(!action.getChain(user).get().isEmpty()) {
            if(capA.getState() == action.getChainState() || (capA.getState() == action.getChainState() - 1 && capA.getFrame() >= capA.getDuration() - 3)) {
                capA.queue();
                return true;
            }
        }
        else if(capA.isInactive()) {
            capA.startAction(set.crawl.getId());
            return true;
        }
        return false;
    }

    public static boolean canExecuteAttack(Player user, boolean crawling) {
        if(user.isUsingItem() || user.isSpectator() || (crawling ? (user.getPose() != Pose.SWIMMING && user.getPose() != Pose.FALL_FLYING) : (user.getPose() != Pose.STANDING && user.getPose() != Pose.CROUCHING)) || user.swinging) return false;
        return !ActionTracker.get(user).isStunned();
    }

    public void initNBT(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if(nbt == null) {
            nbt = new CompoundTag();
            stack.setTag(nbt);
        }
        if(!nbt.contains("UUID")) {
            nbt.putString("UUID", UUID.randomUUID().toString());
            stack.setTag(nbt);
        }
    }

    @Override
    public @Nullable RegistryObject<? extends Action> getRecipeAction() {
        return actionSet.recipeAction;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if(!(entityIn instanceof Player) || !entityIn.isAlive()) return;
        initNBT(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack itemStack, LivingEntity entityLivingBase) {
        return !(entityLivingBase instanceof Player) || !entityLivingBase.isAlive();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if(slotChanged) return true;
        if(newStack.getItem() instanceof MeleeWeaponItem) {
            return !oldStack.getTag().getString("UUID").equals(newStack.getTag().getString("UUID"));
        }
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        return canDig;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        BlockState modifiedState = null;
        for(ToolAction action : toolActions) {
            modifiedState = state.getToolModifiedState(context, action, false);
            if(modifiedState != null) {
                if(action == ToolActionsNF.REFINE) {
                    if(!level.isClientSide && player != null && modifiedState.is(TagsNF.ANVILS)) PlayerData.get(player).addKnowledge(KnowledgeNF.IMPROVISED_ANVIL.getId());
                    if(state.is(Tags.Blocks.STONE)) level.playSound(player, pos, SoundsNF.CARVE_STONE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    else level.playSound(player, pos, SoundsNF.CARVE_WOOD.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                else if(action == ToolActionsNF.STRIP) level.playSound(player, pos, SoundsNF.STRIP_WOOD.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                else if(action == ToolActionsNF.TILL) level.playSound(player, pos, SoundsNF.TILL_SOIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                break;
            }
        }
        if(modifiedState != null) {
            level.setBlock(pos, modifiedState, 11);
            if(player != null) {
                context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        else return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(entity instanceof ActionableEntity actionable) {
            for(ToolAction toolAction : toolActions) {
                boolean interacted = actionable.tryToolAction(stack, player, hand, toolAction);
                if(interacted) {
                    if(toolAction == ToolActionsNF.SKIN) {
                        Vec3 pos = entity.position();
                        player.level.playSound(player, pos.x, pos.y, pos.z, SoundsNF.SKIN_FLESH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolActions.contains(toolAction);
    }

    @Override
    public float getDefense(DamageTypeSource source) {
        float total = 0;
        for(DamageType type : source.types) {
            if(type.isDefensible()) total += defense.get(type.ordinal());
        }
        return total / source.types.length;
    }
}
