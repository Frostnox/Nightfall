package frostnox.nightfall.action.player.action;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.item.ProjectileLauncherItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LaunchProjectileAction extends MoveSpeedPlayerAction {
    public final float velocity;

    public LaunchProjectileAction(Properties properties, float speedMultiplier, float velocity, int... duration) {
        super(properties, speedMultiplier, duration);
        this.velocity = velocity;
    }

    @Override
    public int getChargeTimeout() {
        return Action.CHARGE_MAX;
    }

    @Override
    public boolean canStart(LivingEntity user) {
        return super.canStart(user) && (user.getPose() == Pose.STANDING || user.getPose() == Pose.CROUCHING);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(user.level instanceof ServerLevel level && capA.getState() == getChargeState() + 1 && capA.getFrame() == 2 && !capA.isStunned()) {
            Player player = (Player) user;
            InteractionHand hand = PlayerData.get(player).getActiveHand();
            ItemStack item = user.getItemInHand(hand);
            if(item.getItem() instanceof ProjectileLauncherItem launcher) {
                Entity projectile = launcher.launchProjectile(item, player, hand,
                        Math.min((float) capA.getCharge() / (float) getDuration(getChargeState(), user) * velocity, velocity));
                if(projectile != null) {
                    level.addFreshEntity(projectile);
                    level.playSound(null, user, capA.getCharge() >= getMaxCharge() ? getExtraSound().get() : getSound().get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }
}
