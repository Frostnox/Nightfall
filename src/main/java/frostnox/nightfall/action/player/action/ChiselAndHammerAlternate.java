package frostnox.nightfall.action.player.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.anvil.AnvilAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.blockentity.AnvilActionToServer;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public class ChiselAndHammerAlternate extends MoveSpeedPlayerAction {
    public ChiselAndHammerAlternate(int[] duration, Properties properties) {
        super(properties, -0.25F, duration);
    }

    public ChiselAndHammerAlternate(Properties properties, int... duration) {
        super(properties, -0.25F, duration);
    }

    @Override
    public void transformModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        switch(state) {
            case 0 -> {
                tCalc.extend(-11F/16F, 8.5F/16F, -7F/16F);
                rCalc.extend(0, 83, -90);
            }
            case 1 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 2 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 3 -> {
                boolean hitstop = PlayerData.get((Player) user).getHitStopFrame() != -1;
                tCalc.add(hitstop ? 0.25F/16F : 1F/16F, 0F, hitstop ? -2F/16F : -8F/16F, Easing.outQuart);
                rCalc.addWithHitStop(user, getDuration(2, user), 0, -1, 0, Easing.outQuart);
            }
            case 4 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 5 -> {
                tCalc.extend(dTranslation);
                rCalc.extend(dRotation);
            }
        }
    }

    @Override
    public void transformOppositeModelFP(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        super.transformOppositeModelFP(state, frame, duration, charge, user, data);
        AnimationCalculator tCalc = data.tCalc;
        AnimationCalculator rCalc = data.rCalc;
        Vector3f dTranslation = data.dTranslation;
        Vector3f dRotation = data.dRotation;
        switch(state) {
            case 0 -> {
                tCalc.extend(-4F/16F, 0F/16F, 3F/16F);
                rCalc.extend(dRotation.x() + 30, dRotation.y(), dRotation.z());
            }
            case 1 -> {
                tCalc.addWithCharge(0, 0, 3F/16F, charge);
                rCalc.addWithCharge(3, 0, 0, charge);
            }
            case 2 -> {
                tCalc.extend(-7F/16F, -1F/16F, 0F/16F);
                rCalc.add(-35, 13, 0);
            }
            case 3 -> {
                tCalc.add(0F/16F, 0F/16F, 1F/16F, Easing.outQuart);
                rCalc.add(3, 0, 0, Easing.outQuart);
            }
            case 4 -> {
                tCalc.freeze();
                rCalc.freeze();
            }
            case 5 -> {
                tCalc.extend(dTranslation);
                rCalc.extend(dRotation);
                tCalc.length -= 2;
                rCalc.length -= 2;
            }
        }
    }

    @Override
    public void transformOppositeHandFP(AnimationCalculator tCalc, int xSide, int side, IActionTracker capA) {

    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(data.size() == 6) {
            int side = AnimationUtil.getActiveSideModifier((Player) user);
            AnimationData rightHand = data.get(EntityPart.getSidedHand(side));
            AnimationData rightArm = data.get(EntityPart.getSidedArm(side));
            AnimationData leftArm = data.get(EntityPart.getSidedArm(-side));
            AnimationData leftHand = data.get(EntityPart.getSidedHand(-side));
            AnimationData rightLeg = data.get(EntityPart.LEG_RIGHT);
            AnimationData leftLeg = data.get(EntityPart.LEG_LEFT);
            switch(state) {
                case 0 -> {
                    rightArm.rCalc.extend(0, 0, 0);
                    rightHand.rCalc.extend(-110 + pitch, -25, 0 - pitch/4);
                    rightHand.toDefaultTranslation();
                    leftArm.rCalc.extend(pitch, 0, pitch/4);
                    leftHand.rCalc.extend(-120, 20, 0);
                    leftHand.tCalc.addFrom(leftHand.dTranslation, 0, 0, 1);
                }
                case 1 -> {
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightHand.tCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.addWithCharge(-15, 0, 0, charge);
                    leftHand.tCalc.freeze();
                }
                case 2 -> {
                    rightArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightHand.tCalc.freeze();
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.extend(-75, 20, 0, Easing.outCubic);
                    leftHand.tCalc.freeze();
                }
                case 3 -> {
                    rightArm.rCalc.freeze();
                    boolean hitstop = PlayerData.get((Player) user).getHitStopFrame() != -1;
                    rightHand.rCalc.add(hitstop ? 4: 10, 0, 0, Easing.outQuart);
                    rightHand.tCalc.addFrom(leftHand.dTranslation, 0, 0, -0.5F);
                    rightHand.tCalc.setEasing(Easing.outQuart);
                    leftArm.rCalc.freeze();
                    leftHand.rCalc.add(-3, 0, 0, Easing.outQuart);
                    leftHand.tCalc.freeze();
                }
                case 4 -> {
                    rightArm.rCalc.freeze();
                    leftHand.rCalc.freeze();
                    leftArm.rCalc.freeze();
                    rightHand.rCalc.freeze();
                    rightHand.tCalc.freeze();
                    leftHand.tCalc.freeze();
                }
                case 5 -> {
                    rightArm.toDefaultRotation();
                    leftArm.toDefaultRotation();
                    rightHand.toDefault();
                    leftHand.toDefault();
                }
            }
        }
    }

    @Override
    public void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.rCalc.extend(-15, 65, -90);
                data.tCalc.addFrom(data.dTranslation, -1.5F/16F, 0.5F/16F, -2.5F/16F);
            }
            case 1 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 2 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 3 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 4 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 5 -> {
                data.toDefault();
            }
        }
    }

    @Override
    public void transformOppositeLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        switch(state) {
            case 0 -> {
                data.rCalc.addFrom(data.dRotation, 10, 5, 0);
                data.tCalc.addFrom(data.dTranslation, 0, -4/16F, 0);
                data.tCalc.setEasing(Easing.outSine);
            }
            case 1 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 2 -> {
                data.rCalc.add(-10, 0, 0, Easing.outCubic);
                data.tCalc.freeze();
            }
            case 3 -> {
                data.rCalc.add(5, 0, 0, Easing.outQuart);
                data.tCalc.freeze();
            }
            case 4 -> {
                data.rCalc.freeze();
                data.tCalc.freeze();
            }
            case 5 -> {
                data.toDefault();
            }
        }
    }

    @Override
    public float getPitch(LivingEntity user, float partial) {
        return Mth.clamp(user.getViewXRot(partial), -65, 60);
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 3;
    }

    @Override
    public int getBlockHitFrame(int state, LivingEntity user) {
        return 1;
    }

    @Override
    public void onClientTick(Player player) {
        if(!player.level.isClientSide()) return;
        Vec3i lookingAt = ClientEngine.get().microHitResult;
        BlockPos pos = ClientEngine.get().microBlockEntityPos;
        IActionTracker capA = ActionTracker.get(player);
        if(isStateDamaging(capA.getState()) && !capA.isStunned()) {
            if(capA.getFrame() == getBlockHitFrame(capA.getState(), player) && lookingAt != null && pos != null) {
                NetworkHandler.toServer(new AnvilActionToServer(AnvilAction.CUT, lookingAt.getX(), pos));
                PlayerData.get(player).setHitStopFrame(capA.getFrame());
            }
        }
    }

    @Override
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        List<Component> tooltips = super.getTooltips(stack, level, isAdvanced);
        if(ClientEngine.get().isShiftHeld() && ClientEngine.get().getPlayer() != null && PlayerData.get(ClientEngine.get().getPlayer()).hasCompletedEntry(EntriesNF.SMITHING.getId())) {
            tooltips.add(new TextComponent(""));
            tooltips.add(new TextComponent(" ").append(new TranslatableComponent("anvil.action.context").withStyle(ChatFormatting.GRAY))
                    .append(new TranslatableComponent("anvil.action." + AnvilAction.CUT.name().toLowerCase() + ".info").withStyle(ChatFormatting.DARK_AQUA)));
        }
        return tooltips;
    }
}
