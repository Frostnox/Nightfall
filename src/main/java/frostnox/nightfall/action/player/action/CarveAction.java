package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.recipe.HeldToolRecipe;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class CarveAction extends MoveSpeedPlayerAction {
    public CarveAction(int[] duration, Properties properties) {
        super(properties, -0.25F, duration);
    }

    public CarveAction(Properties properties, int... duration) {
        super(properties, -0.25F, duration);
    }

    @Override
    public int getRequiredCharge(LivingEntity user) {
        return 0;
    }

    @Override
    public int getChargeTimeout() {
        return CHARGE_MAX;
    }

    @Override
    public boolean hasChargeZoom() {
        return false;
    }

    @Override
    public void onTick(LivingEntity user) {
        super.onTick(user);
        IActionTracker capA = ActionTracker.get(user);
        if(!user.level.isClientSide && capA.getFrame() % getDuration(getChargeState(), user) == 0) {
            Player player = (Player) user;
            Optional<HeldToolRecipe> recipe = HeldToolRecipe.getRecipe(player);
            if(recipe.isPresent()) {
                ItemStack oppItem = user.getItemInHand(PlayerData.get(player).getOppositeActiveHand());
                if(!player.getAbilities().instabuild) oppItem.shrink(1);
                LevelUtil.giveItemToPlayer(recipe.get().getResultItem(), player, true);
                if(oppItem.isEmpty()) {
                    capA.queue();
                    NetworkHandler.toAllTrackingAndSelf(player, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, player.getId()));
                }
            }
            else {
                capA.queue();
                NetworkHandler.toAllTrackingAndSelf(player, new GenericEntityToClient(NetworkHandler.Type.QUEUE_ACTION_TRACKER, player.getId()));
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return 0;
    }

    @Override
    public void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {
        tCalc.add(-3F/16F * xSide * side, 0, -3F/16F * side);
        if(capA.getState() >= 1) tCalc.freeze();
        if(capA.getState() >= 2) tCalc.extend(Vector3f.ZERO);
    }
}
