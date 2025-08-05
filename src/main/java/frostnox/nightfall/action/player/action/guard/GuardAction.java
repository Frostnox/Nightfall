package frostnox.nightfall.action.player.action.guard;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.player.PlayerAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.item.IGuardingItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public abstract class GuardAction extends PlayerAction {
    public GuardAction(int... duration) {
        super(duration);
    }

    public GuardAction(Action.Properties properties, int... duration) {
        super(properties, duration);
    }

    public abstract boolean blocksDamageSource(DamageTypeSource source);

    public abstract float getGuardAngle();

    @Override
    public boolean hasChargeZoom() {
        return false;
    }

    @Override
    public int getChargeTimeout() {
        return Action.CHARGE_MAX;
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return !isChargeable() ? 0 : getDuration(getChargeState(), user);
    }

    @Override
    public void onEnd(LivingEntity user) {
        CombatUtil.removeTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.BLOCK_SLOW_ID);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(user instanceof Player player) {
            if(isActive(user)) {
                IPlayerData capP = PlayerData.get(player);
                CombatUtil.addTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), user.tickCount - capP.getLastBlockTick() < 9 ? -0.6F : -0.2F, CombatUtil.BLOCK_SLOW_ID, "block_slow");
                if(user.level.isClientSide() && !user.isOnGround()) {
                    Vec3 velocity = user.getDeltaMovement();
                    float modifier = user.tickCount - capP.getLastBlockTick() < 9 ? 0.7F : 0.9F;
                    //Slow movement in air to better match ground speed; this would be safer to do on the server if possible
                    user.setDeltaMovement(velocity.x * modifier, velocity.y, velocity.z * modifier);
                }
            }
            else CombatUtil.removeTransientMultiplier(user, user.getAttribute(Attributes.MOVEMENT_SPEED), CombatUtil.BLOCK_SLOW_ID);
        }
        CombatUtil.alignBodyRotWithHead(user, ActionTracker.get(user));
    }

    public float onAttackReceived(LivingEntity user, DamageTypeSource source, float damage, boolean playSound) {
        float newDamage = damage;
        if(isActive(user) && blocksDamageSource(source)) {
            Entity attacker = source.getEntity();
            if(user instanceof ServerPlayer player) {
                IPlayerData capP = PlayerData.get(player);
                if(capP.getStamina() <= 0F) return newDamage;
                else {
                    capP.addStamina(-source.getStunDuration() * 2F);
                    //NetworkHandler.toClient(player, new StaminaChangedMessageToClient(capP.getStamina(), player.getId()));
                }
            }
            Vec3 attackVec = source.hasHitCoords() ? new Vec3(source.getHitCoords().x + attacker.getX(), source.getHitCoords().y + attacker.getY(), source.getHitCoords().z + attacker.getZ()) : attacker.getEyePosition();
            float hitAngle = CombatUtil.getRelativeHorizontalAngle(user.getEyePosition(), attackVec, user.getYHeadRot());
            float defense = 0;
            InteractionHand hand = user instanceof Player player ? PlayerData.get(player).getActiveHand() : InteractionHand.MAIN_HAND;
            if(user.getItemInHand(hand).getItem() instanceof IGuardingItem item) defense = item.getDefense(source);
            boolean blocked = false;
            if(hitAngle >= -getGuardAngle() && hitAngle <= getGuardAngle()) blocked = true;
            else if(source.hasHitCoords()) {
                float entityAngle = CombatUtil.getRelativeHorizontalAngle(user.getEyePosition(), attacker.getEyePosition(), user.getYHeadRot());
                //Allow block for case where collider hits just behind eye position but entity is actually in front
                if(Math.abs(entityAngle - hitAngle) >= 180) blocked = true;
            }
            if(blocked) {
                newDamage = CombatUtil.applyDamageReduction(damage, defense);
                if(!user.level.isClientSide && playSound) {
                    user.level.playSound(null, user, this.getSound().get(), user.getSoundSource(), 1, (newDamage != 0 ? 0.8F : 1) + user.level.random.nextFloat(-0.02F, 0.02F));
                }
            }
        }
        if(newDamage == 0) {
            if(user instanceof Player player) updateBlockTick(player);
        }
        return newDamage;
    }

    @Override
    public float onAttackReceived(LivingEntity user, DamageTypeSource source, float damage) {
        return onAttackReceived(user, source, damage, true);
    }

    @Override
    public float onDamageReceived(LivingEntity user, DamageTypeSource source, float damage) {
        float newDamage = onAttackReceived(user, source, damage, false);
        if(newDamage != damage) {
            if(user instanceof Player player) updateBlockTick(player);
            return newDamage;
        }
        return damage;
    }

    @Override
    public boolean canStart(LivingEntity user) {
        return super.canStart(user) && PlayerData.get((Player) user).getStamina() > 0F;
    }

    @Override
    public boolean canContinueCharging(LivingEntity user) {
        IPlayerData capP = PlayerData.get((Player) user);
        return capP.getStamina() > 0F || user.tickCount - capP.getLastBlockTick() <= 8;
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        List<Component> tooltips = new ObjectArrayList<>();
        tooltips.add(new TranslatableComponent("action.guard").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
        DecimalFormat format = new DecimalFormat("0");
        if(ClientEngine.get().isShiftHeld() && stack.getItem() instanceof IGuardingItem item) {
            for(DamageType type : DamageType.STANDARD_TYPES) {
                float defense = item.getDefense(new DamageTypeSource(type.toString(), type));
                tooltips.add(new TextComponent(" ").withStyle(ChatFormatting.BLUE).append(new TranslatableComponent("action.guard.block",
                        format.format(defense * 100) + "%", RenderUtil.getDamageTypeText(type))));
            }
        }
        return tooltips;
    }

    public boolean isActive(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        return capA.getState() == 0 && !capA.isInactive() && !capA.isStunned();
    }

    protected void updateBlockTick(Player player) {
        PlayerData.get(player).setLastBlockTick(player.tickCount);
        NetworkHandler.toAllTrackingAndSelf(player, new GenericEntityToClient(NetworkHandler.Type.BLOCK_CLIENT, player.getId()));
    }
}
