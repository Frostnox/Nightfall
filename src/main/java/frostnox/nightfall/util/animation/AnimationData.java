package frostnox.nightfall.util.animation;

import com.mojang.math.Vector3f;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.geom.ModelPart;

public class AnimationData {
    public AnimationCalculator tCalc, rCalc, sCalc;
    public final Vector3f dTranslation, dRotation, dScale, offset;

    public AnimationData() {
        this(new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f());
    }

    public AnimationData(Vector3f offset) {
        this(new Vector3f(), new Vector3f(), new Vector3f(), offset);
    }

    public AnimationData(Vector3f offset, Vector3f dRotation) {
        this(new Vector3f(), dRotation, new Vector3f(), offset);
    }

    public AnimationData(Vector3f dTranslation, Vector3f dRotation, Vector3f dScale) {
        this(dTranslation, dRotation, dScale, new Vector3f());
    }

    public AnimationData(Vector3f dTranslation, Vector3f dRotation, Vector3f dScale, Vector3f offset) {
        this.dTranslation = dTranslation;
        this.dRotation = dRotation;
        this.dScale = dScale;
        setDefaults(dTranslation, dRotation, dScale);
        tCalc = new AnimationCalculator();
        tCalc.setStaticVector(dTranslation);
        rCalc = new AnimationCalculator();
        rCalc.setStaticVector(dRotation);
        sCalc = new AnimationCalculator();
        sCalc.setStaticVector(dScale);
        this.offset = offset;
    }

    public AnimationData(AnimationData data) {
        this.tCalc = data.tCalc.copy();
        this.rCalc = data.rCalc.copy();
        this.sCalc = data.sCalc.copy();
        this.dRotation = data.dRotation.copy();
        this.dTranslation = data.dTranslation.copy();
        this.dScale = data.dScale.copy();
        this.offset = data.offset.copy();
    }

    public void setDefaults(Vector3f translation, Vector3f rotation, Vector3f scale) {
        setDefaults(translation.x(), translation.y(), translation.z(), rotation.x(), rotation.y(), rotation.z(), scale.x(), scale.y(), scale.z());
    }

    /**
     * Set default vectors to safe values that won't conflict with animation mirroring (default value needs to be preserved in this case,
     * so exact default value can't be used in animation)
     */
    public void setDefaults(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
        dTranslation.set(offsetValue(x), offsetValue(y), offsetValue(z));
        dRotation.set(offsetValue(xRot), offsetValue(yRot), offsetValue(zRot));
        dScale.set(offsetScale(xScale), offsetScale(yScale), offsetScale(zScale));
    }

    private static float offsetScale(float scale) {
        return scale % 1F != 0F ? scale + 0.00001F : scale;
    }

    private static float offsetValue(float value) {
        return value == 0F ? value : value + 0.00001F;
    }

    public AnimationData copy() {
        return new AnimationData(this);
    }

    public void update(float partialTicks) {
        tCalc.partialTicks = partialTicks;
        rCalc.partialTicks = partialTicks;
        sCalc.partialTicks = partialTicks;
    }

    public void update(int frame, int length, float partialTicks) {
        tCalc.update(frame, length, partialTicks);
        rCalc.update(frame, length, partialTicks);
        sCalc.update(frame, length, partialTicks);
    }

    public void update(int frame, int length, Easing easing) {
        tCalc.update(frame, length, easing);
        rCalc.update(frame, length, easing);
        sCalc.update(frame, length, easing);
    }

    public void update(int frame, int length, float partialTicks, Easing easing) {
        tCalc.update(frame, length, partialTicks, easing);
        rCalc.update(frame, length, partialTicks, easing);
        sCalc.update(frame, length, partialTicks, easing);
    }

    public void resetLengths(int length) {
        resetLengths(length, Easing.none);
    }

    public void resetLengths(int length, Easing easing) {
        tCalc.resetLength(length, easing);
        rCalc.resetLength(length, easing);
        sCalc.resetLength(length, easing);
    }

    public void reset() {
        resetRotation();
        resetTranslation();
        resetScale();
    }

    public void resetRotation() {
        rCalc.setStaticVector(dRotation);
    }

    public void resetTranslation() {
        tCalc.setStaticVector(dTranslation);
    }

    public void resetScale() {
        sCalc.setStaticVector(dScale);
    }

    public void toDefault() {
        toDefaultRotation();
        toDefaultTranslation();
        toDefaultScale();
    }

    public void toDefaultRotation() {
        rCalc.extend(dRotation);
    }

    public void toDefaultTranslation() {
        tCalc.extend(dTranslation);
    }

    public void toDefaultScale() {
        sCalc.extend(dScale);
    }

    /**
     * Overwrites model part data with corresponding animation data
     * Expects rotation to be in degrees
     */
    public void writeToModelPart(ModelPart part) {
        Vector3f rVec = MathUtil.toRadians(rCalc.getTransformations());
        Vector3f tVec = tCalc.getTransformations();
        part.xRot = rVec.x();
        part.yRot = rVec.y();
        part.zRot = rVec.z();
        part.x = tVec.x();
        part.y = tVec.y();
        part.z = tVec.z();
    }

    /**
     * Updates all of the animation data from corresponding model parts
     * Rotation is converted from radians to degrees
     */
    public void readFromModelPart(ModelPart part) {
        setDefaults(part.x, part.y, part.z, MathUtil.toDegrees(part.xRot), MathUtil.toDegrees(part.yRot), MathUtil.toDegrees(part.zRot), 1, 1, 1);
        tCalc.setStaticVector(dTranslation);
        rCalc.setStaticVector(dRotation);
        sCalc.setStaticVector(dScale);
    }

    public void mirrorAcrossY() {
        if(rCalc.startVec.y() != dRotation.y()) rCalc.startVec.setY(-rCalc.startVec.y());
        if(rCalc.startVec.z() != dRotation.z()) rCalc.startVec.setZ(-rCalc.startVec.z());
        if(rCalc.endVec.y() != dRotation.y()) rCalc.endVec.setY(-rCalc.endVec.y());
        if(rCalc.endVec.z() != dRotation.z()) rCalc.endVec.setZ(-rCalc.endVec.z());
    }
}
