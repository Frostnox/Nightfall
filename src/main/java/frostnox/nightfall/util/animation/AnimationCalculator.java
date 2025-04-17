package frostnox.nightfall.util.animation;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Calculates animations between two vectors with support for easing.
 */
public class AnimationCalculator {
    private boolean looping = false;
    private Easing easing = Easing.none;
    public Vector3f startVec = new Vector3f(0, 0, 0);
    public Vector3f endVec = new Vector3f(0, 0, 0);
    public int length = 1;
    public int frame = 0;
    public int offset = 0;
    public float partialTicks = 1;

    public AnimationCalculator() {

    }

    public AnimationCalculator(int length, int frame, float partialTicks) {
        this.length = length;
        this.frame = frame;
        this.partialTicks = partialTicks;
    }

    public AnimationCalculator(int length, int frame, float partialTicks, Easing easing) {
        this.length = length;
        this.frame = frame;
        this.partialTicks = partialTicks;
        this.easing = easing;
    }

    public AnimationCalculator(int length, int frame, float partialTicks, Vector3f startVec, Vector3f endVec) {
        this.length = length;
        this.frame = frame;
        this.partialTicks = partialTicks;
        this.startVec = startVec.copy();
        this.endVec = endVec.copy();
    }

    public AnimationCalculator(int length, int frame, float partialTicks, Vector3f startVec, Vector3f endVec, Easing easing) {
        this.length = length;
        this.frame = frame;
        this.partialTicks = partialTicks;
        this.startVec = startVec.copy();
        this.endVec = endVec.copy();
        this.easing = easing;
    }

    public AnimationCalculator copy() {
        AnimationCalculator calc = new AnimationCalculator();
        calc.length = length;
        calc.frame = frame;
        calc.partialTicks = partialTicks;
        calc.startVec = startVec.copy();
        calc.endVec = endVec.copy();
        calc.easing = easing;
        calc.offset = offset;
        return calc;
    }

    public void scale(float x, float y, float z) {
        startVec.mul(x, y, z);
        endVec.mul(x, y, z);
    }

    public Vector3f getTransformations() {
        Vector3f vec1 = endVec.copy();
        vec1.sub(startVec);
        vec1.mul(getProgress());
        Vector3f vec2 = startVec.copy();
        vec2.add(vec1);
        return vec2;
    }

    public float getProgress() {
        if(!looping && frame > length) return 1F;
        float progress = AnimationUtil.interpolate((float) Math.max(0, frame - 1 - offset) / (length - offset), (float) (frame - offset) / (length - offset), partialTicks);
        if(looping && progress % 1F != 0F) progress %= 1F;
        return AnimationUtil.applyEasing(progress, easing);
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public void extend(float x, float y, float z) {
        this.startVec = this.endVec.copy();
        this.endVec = new Vector3f(x, y, z);
    }

    public void extend(float x, float y, float z, Easing easing) {
        setEasing(easing);
        extend(x, y, z);
    }

    public void extend(Vector3f endVec) {
        this.startVec = this.endVec.copy();
        this.endVec = endVec.copy();
    }

    public void extend(Vector3f endVec, Easing easing) {
        setEasing(easing);
        extend(endVec);
    }
    public void extendWithCharge(Vector3f vec, float multiplier) {
        extendWithCharge(vec.x(), vec.y(), vec.z(), multiplier);
    }

    public void extendWithCharge(Vector3f vec, float multiplier, Easing easing) {
        extendWithCharge(vec.x(), vec.y(), vec.z(), multiplier, easing);
    }

    public void extendWithCharge(float x, float y, float z, float multiplier) {
        float charge = AnimationUtil.applyEasing(multiplier, this.easing);
        if(charge > 0F) {
            startVec = endVec.copy();
            Vector3f vec = new Vector3f(x, y, z);
            vec.sub(startVec);
            vec.mul(charge);
            endVec.add(vec);
        }
        else extend(x, y, z);
    }

    public void extendWithCharge(float x, float y, float z, float multiplier, Easing easing) {
        setEasing(easing);
        extendWithCharge(x, y, z, multiplier);
    }

    public void addWithCharge(float x, float y, float z, float multiplier) {
        float charge = AnimationUtil.applyEasing(multiplier, this.easing);
        if(charge > 0F) {
            startVec = endVec.copy();
            endVec.add(x * charge, y * charge, z * charge);
        }
        else add(x, y, z);
    }

    public void addWithCharge(float x, float y, float z, float multiplier, Easing easing) {
        setEasing(easing);
        addWithCharge(x, y, z, multiplier);
    }

    public void extendWithHitStop(LivingEntity user, int stopStateDuration, float x, float y, float z) {
        extendWithHitStop(user, stopStateDuration, x, y, z, easing);
    }

    public void extendWithHitStop(LivingEntity user, int stopStateDuration, float x, float y, float z, Easing easing) {
        if(!(user instanceof Player player)) extend(x, y, z, easing);
        else {
            IPlayerData capP = PlayerData.get(player);
            int frame = capP.getHitStopFrame();
            if(frame == -1) extend(x, y, z, easing);
            else {
                AnimationCalculator calc = copy();
                calc.length = stopStateDuration;
                calc.frame = frame;
                calc.partialTicks = capP.getHitStopPartial();
                calc.extend(x, y, z, easing);
                setStaticVector(calc.getTransformations());
            }
        }
    }

    public void addWithHitStop(LivingEntity user, int stopStateDuration, float x, float y, float z) {
        addWithHitStop(user, stopStateDuration, x, y, z, easing);
    }

    public void addWithHitStop(LivingEntity user, int stopStateDuration, float x, float y, float z, Easing easing) {
        if(!(user instanceof Player player)) add(x, y, z, easing);
        else {
            IPlayerData capP = PlayerData.get(player);
            int frame = capP.getHitStopFrame();
            if(frame == -1) add(x, y, z, easing);
            else {
                AnimationCalculator calc = copy();
                calc.length = stopStateDuration;
                calc.frame = frame;
                calc.partialTicks = capP.getHitStopPartial();
                calc.add(x, y, z, easing);
                setStaticVector(calc.getTransformations());
            }
        }
    }

    public void add(float x, float y, float z) {
        startVec = endVec.copy();
        endVec.add(x, y, z);
    }

    public void add(float x, float y, float z, Easing easing) {
        add(x, y, z);
        setEasing(easing);
    }

    public void add(Vector3f vec) {
        this.startVec = this.endVec.copy();
        this.endVec.add(vec);
    }

    public void mulEnd(float xMul, float yMul, float zMul) {
        this.endVec.mul(xMul, yMul, zMul);
    }

    public void setVectors(Vector3f startVec, Vector3f endVec) {
        this.startVec = startVec.copy();
        this.endVec = endVec.copy();
    }

    public void setVectors(Vector3f startVec, Vector3f endVec, Easing easing) {
        setVectors(startVec, endVec);
        this.easing = easing;
    }

    public void setVectors(Vector3f startVec, float x, float y, float z) {
        this.startVec = startVec.copy();
        this.endVec = new Vector3f(x, y, z);
    }

    public void setVectors(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.startVec = new Vector3f(x1, y1, z1);
        this.endVec = new Vector3f(x2, y2, z2);
    }

    public void setVectors(float x1, float y1, float z1, float x2, float y2, float z2, Easing easing) {
        setVectors(x1, y1, z1, x2, y2, z2);
        this.easing = easing;
    }

    public void setVectorsWithHitStop(LivingEntity user, int stopStateDuration, float x1, float y1, float z1, float x2, float y2, float z2) {
        setVectorsWithHitStop(user, stopStateDuration, x1, y1, z1, x2, y2, z2, easing);
    }

    public void setVectorsWithHitStop(LivingEntity user, int stopStateDuration, float x1, float y1, float z1, float x2, float y2, float z2, Easing easing) {
        if(!(user instanceof Player player)) setVectors(x1, y1, z1, x2, y2, z2, easing);
        else {
            IPlayerData capP = PlayerData.get(player);
            int frame = capP.getHitStopFrame();
            if(frame == -1) setVectors(x1, y1, z1, x2, y2, z2, easing);
            else {
                AnimationCalculator calc = copy();
                calc.length = stopStateDuration;
                calc.frame = frame;
                calc.partialTicks = capP.getHitStopPartial();
                calc.setVectors(x1, y1, z1, x2, y2, z2, easing);
                setStaticVector(calc.getTransformations());
            }
        }
    }

    public void addFrom(Vector3f startVec, float x, float y, float z) {
        this.startVec = endVec.copy();
        this.endVec = startVec.copy();
        this.endVec.add(x, y, z);
    }

    public void addFrom(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.startVec = new Vector3f(x1, y1, z1);
        this.endVec = startVec.copy();
        this.endVec.add(x2, y2, z2);
    }

    public void setStaticVector(Vector3f vector) {
        startVec = vector.copy();
        endVec = vector.copy();
    }

    public void setStaticVector(float x, float y, float z) {
        Vector3f vec = new Vector3f(x, y, z);
        startVec = vec.copy();
        endVec = vec.copy();
    }

    public void freeze()  {
        startVec = endVec.copy();
    }

    public void update(int frame, int length) {
        this.frame = frame;
        this.length = length;
        this.offset = 0;
        looping = false;
    }

    public void update(int frame, int length, Easing easing) {
        this.frame = frame;
        this.length = length;
        this.offset = 0;
        this.easing = easing;
        looping = false;
    }

    public void update(int frame, int length, float partialTicks) {
        this.frame = frame;
        this.length = length;
        this.offset = 0;
        this.partialTicks = partialTicks;
        looping = false;
    }

    public void update(int frame, int length, float partialTicks, Easing easing) {
        this.frame = frame;
        this.length = length;
        this.offset = 0;
        this.partialTicks = partialTicks;
        this.easing = easing;
        looping = false;
    }

    public void resetLength(int length) {
        resetLength(length, Easing.none);
    }

    public void resetLength(int length, Easing easing) {
        this.length = length;
        this.offset = 0;
        setEasing(easing);
        looping = false;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void wrapRotation() {
        startVec.set(startVec.x() % 360, startVec.y() % 360, startVec.z() % 360);
        endVec.set(endVec.x() % 360, endVec.y() % 360, endVec.z() % 360);
    }
}
