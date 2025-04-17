package frostnox.nightfall.action.npc.dreg;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.pathfinding.Node;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluids;

import java.util.EnumMap;

public class DregBuild extends Action {
    public DregBuild(int[] duration) {
        super(duration);
    }

    public DregBuild(Properties properties, int... duration) {
        super(properties, duration);
    }

    @Override
    public void onTick(LivingEntity user) {
        if(user.getLevel() instanceof ServerLevel level) {
            DregEntity dreg = (DregEntity) user;
            IActionTracker capA = dreg.getActionTracker();
            if(capA.getState() == 1 && capA.getFrame() == capA.getDuration() / 2) {
                ReversePath path = dreg.getBuildPath();
                if(path != null) {
                    int i = path.getSize() - 1;
                    boolean built = false;
                    while(i >= 0) {
                        Node node = path.getNode(i);
                        if(node.type == NodeType.BUILDABLE_WALKABLE) {
                            if(!built) {
                                BlockPos pos = node.getBlockPos().below();
                                if(level.getBlockState(pos).canBeReplaced(Fluids.EMPTY)) {
                                    SoundType soundType = BlocksNF.MOON_ESSENCE.get().defaultBlockState().getSoundType();
                                    level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                                            (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                                    level.setBlock(pos, BlocksNF.MOON_ESSENCE.get().defaultBlockState(), 3);
                                    dreg.addEssence(-10F);
                                    built = true;
                                }
                            }
                            else break; //Another buildable node exists
                        }
                        //If out of buildable nodes on path, done
                        if(i == 0) dreg.buildDone = true;
                        i--;
                    }
                }
                else dreg.buildDone = true;
            }
        }
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 5;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        AnimationData leftHand = data.get(EntityPart.HAND_LEFT);
        AnimationData leftArm = data.get(EntityPart.ARM_LEFT);
        AnimationData rightHand = data.get(EntityPart.HAND_RIGHT);
        AnimationData rightArm = data.get(EntityPart.ARM_RIGHT);
        AnimationData head = data.get(EntityPart.HEAD);
        switch(state) {
            case 0 -> {
                rightArm.rCalc.extend(0, 0, 0);
                leftArm.rCalc.extend(0, 0, 0);
                rightHand.rCalc.extend(-135, 20, 0, Easing.outSine);
                leftHand.rCalc.extend(-135, -20, 0, Easing.outSine);
                head.rCalc.extend(-55, 0, 0, Easing.outSine);
            }
            case 1 -> {
                rightArm.toDefaultRotation();
                leftArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftHand.toDefaultRotation();
                head.toDefaultRotation();
            }
            case 2 -> {
                rightArm.toDefaultRotation();
                leftArm.toDefaultRotation();
                rightHand.toDefaultRotation();
                leftHand.toDefaultRotation();
                head.toDefaultRotation();
            }
        }
    }
}
