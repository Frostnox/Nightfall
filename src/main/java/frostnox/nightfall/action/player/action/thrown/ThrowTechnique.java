package frostnox.nightfall.action.player.action.thrown;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerAttack;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.entity.projectile.ThrownWeaponEntity;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public abstract class ThrowTechnique extends PlayerAttack {
    public final float throwVelocity, curve;
    public final boolean spinning;

    public ThrowTechnique(DamageType[] damageType, int stunDuration, int[] duration, Properties properties, float throwVelocity, float curve, boolean spinning, @Nullable AttackEffect... effects) {
        super(0F, damageType, new HurtSphere(), 1, stunDuration, duration, properties, effects);
        this.throwVelocity = throwVelocity;
        this.curve = curve;
        this.spinning = spinning;
    }

    @Override
    public int getChargeTimeout() {
        return Action.CHARGE_MAX;
    }

    @Override
    public void onTick(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(user.level instanceof ServerLevel level && capA.getState() == getChargeState() + 1 && capA.getFrame() == 2 && !capA.isStunned()) {
            ItemStack item = user.getItemInHand(user instanceof Player player ? PlayerData.get(player).getActiveHand() : InteractionHand.MAIN_HAND);
            if(item.isEmpty()) return;
            ThrownWeaponEntity thrownWeapon = new ThrownWeaponEntity(level, user, item, capA.getActionID());
            thrownWeapon.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, Math.min((float) capA.getCharge() / (float) getDuration(getChargeState(), user) * throwVelocity, throwVelocity),
                    user instanceof Player player ? CombatUtil.modifyProjectileAccuracy(player, 1F) : 1.0F);
            if(user instanceof Player player) {
                if(player.getAbilities().instabuild) thrownWeapon.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                else player.getInventory().removeItem(item);
            }
            level.addFreshEntity(thrownWeapon);
            level.playSound(null, thrownWeapon, capA.getCharge() >= getMaxCharge() ? getExtraSound().get() : getSound().get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        return List.of(new TranslatableComponent("action.throw").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)),
                new TextComponent(" ").append(super.getTooltips(stack, level, isAdvanced).get(0)));
    }
}
