package frostnox.nightfall.entity;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.math.OBB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;

public interface IOrientedHitBoxes {
    OBB[] EMPTY = new OBB[0];
    double MAX_DIST_FROM_AABB = 1D; //OBBs exceeding this distance from the entity's AABB will not be detected consistently

    ActionableEntity getEntity();

    boolean includeAABB();

    default float getModelScale() {
        return 1F;
    }

    Vector3f getOBBTranslation();

    EnumMap<EntityPart, AnimationData> getDefaultAnimMap();

    EntityPart[] getOrderedOBBParts();

    OBB[][] getDefaultOBBs();

    AABB getEnclosingAABB();

    default OBB[] getOBBs(float partial) {
        ActionableEntity entity = getEntity();
        if(!entity.isAlive()) return EMPTY;
        else {
            float scale = getModelScale();
            IActionTracker capA = entity.getActionTracker();
            EnumMap<EntityPart, AnimationData> transforms = getDefaultAnimMap();
            if(scale != 1F) for(AnimationData data : transforms.values()) data.offset.mul(scale);
            AnimationCalculator mCalc = new AnimationCalculator();
            if(!capA.isInactive() && !capA.isStunned()) {
                Action action = capA.getAction();
                for(AnimationData transform : transforms.values()) transform.update(capA.getFrame(), capA.getDuration(), partial);
                mCalc.update(capA.getFrame(), capA.getDuration(), partial, Easing.inOutSine);
                action.transformModel(capA.getState(), capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()),
                        action.getPitch(entity, partial), entity, transforms, mCalc);
            }
            EntityPart[] parts = getOrderedOBBParts();
            OBB[][] group = getDefaultOBBs();
            if(scale != 1F) {
                for(OBB[] boxes : group) {
                    for(OBB box : boxes) {
                        box.center.mul(scale);
                        box.extents = box.extents.multiply(scale, scale, scale);
                    }
                }
            }
            float netHeadYaw = Mth.wrapDegrees(Mth.rotLerp(partial, entity.yHeadRotO, entity.yHeadRot) - Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot));
            Quaternion bodyYaw = Vector3f.YP.rotationDegrees(-Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot) - mCalc.getTransformations().y());
            int partOffset = parts.length - group.length;
            for(int i = 0; i < group.length; i++) {
                OBB[] boxes = group[i];
                EntityPart part = parts[i + partOffset];
                for(OBB box : boxes) box.rotation.mul(bodyYaw);
                for(int j = i + partOffset; j >= 0; j--) {
                    AnimationData data = transforms.get(parts[j]);
                    Vector3f t = new Vector3f();
                    Vector3f translations = data.tCalc.getTransformations();
                    translations.sub(data.dTranslation);
                    translations.mul(scale * 1F/16F, scale * -1/16F, scale * -1F/16F);
                    if(j > 0) {
                        Quaternion r = new Quaternion(0, 0, 0, 1);
                        t.add(data.offset);
                        t.sub(transforms.get(parts[j - 1]).offset);
                        for(int k = 0; k <= j - 1; k++) {
                            AnimationData dataRoot = transforms.get(parts[k]);
                            Vector3f rotationsRoot = dataRoot.rCalc.getTransformations();
                            if(parts[k] == EntityPart.NECK || parts[k] == EntityPart.HEAD) rotationsRoot.add(0, netHeadYaw, 0);
                            r.mul(Vector3f.ZP.rotationDegrees(-rotationsRoot.z()));
                            r.mul(Vector3f.YP.rotationDegrees(-rotationsRoot.y()));
                            r.mul(Vector3f.XP.rotationDegrees(rotationsRoot.x()));
                        }
                        t.transform(r);
                        translations.transform(r);
                    }
                    if(j == i + partOffset) {
                        Vector3f translation = getOBBTranslation();
                        translation.mul(scale);
                        translations.add(translation);
                    }
                    t.add(translations);
                    t.transform(bodyYaw);
                    for(OBB box : boxes) box.translation = box.translation.add(t.x(), t.y(), t.z());
                }
                Quaternion r = new Quaternion(0, 0, 0, 1);
                boolean rotatedHead = false;
                for(int j = 0; j <= i + partOffset; j++) {
                    AnimationData data = transforms.get(parts[j]);
                    Vector3f rotations = data.rCalc.getTransformations();
                    if(!rotatedHead && (parts[j] == EntityPart.NECK || parts[j] == EntityPart.HEAD)) {
                        rotations.add(0, netHeadYaw, 0);
                        rotatedHead = true;
                    }
                    r.mul(Vector3f.ZP.rotationDegrees(-rotations.z()));
                    r.mul(Vector3f.YP.rotationDegrees(-rotations.y()));
                    r.mul(Vector3f.XP.rotationDegrees(rotations.x()));
                }
                for(OBB box : boxes) box.rotation.mul(r);
                //Can't really know when to apply pitch transform as actions are free to blend between model's default pose and their own animation
                if(part == EntityPart.HEAD && (capA.isInactive() || capA.isStunned())) {
                    for(OBB box : boxes) box.rotation.mul(Vector3f.XP.rotationDegrees(entity.getViewXRot(partial)));
                }
            }
            int size = 0;
            for(OBB[] boxes : group) size += boxes.length;
            OBB[] result = new OBB[size];
            int i = 0;
            for(OBB[] boxes : group) {
                System.arraycopy(boxes, 0, result, i, boxes.length);
                i += boxes.length;
            }
            return result;
        }
    }
}
