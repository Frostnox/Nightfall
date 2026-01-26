package frostnox.nightfall.action.player.action;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.item.item.ActionableAmmoItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ReloadAction extends MoveSpeedPlayerAction {
    public ReloadAction(float speedMultiplier, int... duration) {
        super(speedMultiplier, duration);
    }

    public ReloadAction(Properties properties, float speedMultiplier, int... duration) {
        super(properties, speedMultiplier, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(!user.level.isClientSide && capA.getState() == getTotalStates() - 1 && capA.getFrame() == 1 && !capA.isStunned() && user instanceof Player player) {
            IPlayerData capP = PlayerData.get(player);
            InteractionHand hand = capP.getActiveHand();
            ItemStack item = user.getItemInHand(hand);
            ItemStack ammo = user.getItemInHand(capP.getOppositeActiveHand());
            if(item.getItem() instanceof ActionableAmmoItem ammoItem && ammoItem.shouldReload(item, ammo)) {
                ammoItem.setAmmo(item, ammoItem.maxAmmo);
                if(!player.getAbilities().instabuild) ammo.shrink(1);
            }
        }
    }
}