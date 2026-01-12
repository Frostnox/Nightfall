package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.ToolIngredientRecipe;
import frostnox.nightfall.item.item.ToolItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class CarveAction extends MoveSpeedPlayerAction {
    public CarveAction(int[] duration, Properties properties) {
        super(properties, -0.25F, duration);
    }

    public CarveAction(Properties properties, int... duration) {
        super(properties, -0.25F, duration);
    }

    protected SoundEvent getCarveSound(ItemStack oppItem) {
        return oppItem.is(TagsNF.STONE) ? SoundsNF.CARVE_STONE.get() : SoundsNF.CARVE_WOOD.get();
    }

    protected boolean isSoundFrame(int frame) {
        return frame % 10 == 1;
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
            IPlayerData capP = PlayerData.get(player);
            ItemStack oppItem = user.getItemInHand(capP.getOppositeActiveHand());
            List<ToolIngredientRecipe> recipes = player.getItemInHand(capP.getActiveHand()).getItem() instanceof ToolItem tool ? tool.getRecipes(user.level, player, oppItem) : List.of();
            int index = capP.getCachedModifiableIndex();
            if(index >= 0 && index < recipes.size()) {
                if(!player.getAbilities().instabuild) {
                    oppItem.shrink(1);
                    ItemStack item = player.getItemInHand(capP.getActiveHand());
                    if(item.isDamageableItem()) item.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(capP.getActiveHand()));
                }
                LevelUtil.giveItemToPlayer(recipes.get(index).getResultItem(), player, true);
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
        if(capA.getState() == getChargeState() && isSoundFrame(capA.getFrame())) {
            Player player = (Player) user;
            ItemStack oppItem = user.getItemInHand(PlayerData.get(player).getOppositeActiveHand());
            player.playSound(getCarveSound(oppItem), 0.6F, 0.97F + player.getRandom().nextFloat() * 0.06F);
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
