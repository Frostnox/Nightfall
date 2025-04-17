package frostnox.nightfall.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;

public class AnimatedModelPart extends ModelPart {
    public float defaultXRot, defaultYRot, defaultZRot, defaultX, defaultY, defaultZ;
    public AnimationData animationData;
    public float xScale = 1F, yScale = 1F, zScale = 1F;

    public AnimatedModelPart(ModelPart part) {
        super(part.cubes, part.children);
        x = part.x;
        y = part.y;
        z = part.z;
        xRot = part.xRot;
        yRot = part.yRot;
        zRot = part.zRot;
        visible = part.visible;
        defaultXRot = xRot;
        defaultYRot = yRot;
        defaultZRot = zRot;
        defaultX = x;
        defaultY = y;
        defaultZ = z;
        animationData = new AnimationData(new Vector3f(x, y, z), new Vector3f(xRot, yRot, zRot), new Vector3f(xScale, yScale, zScale));
    }

    @Override
    public void translateAndRotate(PoseStack pPoseStack) {
        super.translateAndRotate(pPoseStack);
        if(xScale != 1F || yScale != 1F || zScale != 1F) pPoseStack.scale(xScale, yScale, zScale);
    }

    /**
     * Overwrites model part data with corresponding animation data
     * Expects rotation to be in degrees
     */
    public void writeAnimation() {
        Vector3f rVec = MathUtil.toRadians(animationData.rCalc.getTransformations());
        Vector3f tVec = animationData.tCalc.getTransformations();
        xRot = rVec.x();
        yRot = rVec.y();
        zRot = rVec.z();
        x = tVec.x();
        y = tVec.y();
        z = tVec.z();
    }

    /**
     * Updates all of the animation data from corresponding model part
     * Rotation is converted from radians to degrees
     */
    public void readAnimation() {
        animationData.setDefaults(x, y, z, MathUtil.toDegrees(xRot), MathUtil.toDegrees(yRot), MathUtil.toDegrees(zRot), 1, 1, 1);
        animationData.tCalc.setStaticVector(animationData.dTranslation);
        animationData.rCalc.setStaticVector(animationData.dRotation);
        animationData.sCalc.setStaticVector(animationData.dScale);
    }

    public void setDefaultPose() {
        defaultXRot = xRot;
        defaultYRot = yRot;
        defaultZRot = zRot;
        defaultX = x;
        defaultY = y;
        defaultZ = z;
    }

    public void resetPose() {
        xRot = defaultXRot;
        yRot = defaultYRot;
        zRot = defaultZRot;
        x = defaultX;
        y = defaultY;
        z = defaultZ;
    }

    public void setScale(@Nullable Vec3f scale) {
        if(scale == null) return;
        xScale = scale.x();
        yScale = scale.y();
        zScale = scale.z();
    }
}
