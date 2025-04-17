package frostnox.nightfall.util.math;

import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;

public class Quat {
    public float r, i, j, k;

    public Quat() {
        set(1, 0, 0, 0);
    }

    public Quat(float r, float i, float j, float k) {
        set(r, i, j, k);
    }

    public Quat(Quat q) {
        set(q.r, q.i, q.j, q.k);
    }

    public Quat(float angle, Vector3f axis, boolean inDegrees) {
        if(inDegrees) angle = (float) (angle / 180F * Math.PI);
        float cos = (float) Math.cos(angle / 2F);
        float sin = (float) Math.sin(angle / 2F);
        this.r = cos;
        this.i = axis.x() * sin;
        this.j = axis.y() * sin;
        this.k = axis.z() * sin;
    }

    float dot(Quat q) {
        return r * q.r + i * q.i + j * q.j + k * q.k;
    }

    public void normalize() {
        float magnitude =  r * r + i * i + j * j + k * k;
        if(magnitude == 1) return;
        magnitude = magnitude > 0 ? Mth.fastInvSqrt(magnitude) : 0;
        r *= magnitude;
        i *= magnitude;
        j *= magnitude;
        k *= magnitude;
    }

    public void set(float r, float i, float j, float k) {
        this.r = r;
        this.i = i;
        this.j = j;
        this.k = k;
    }

    public Quat getProduct(Quat q) {
        float qr, qi, qj, qk;
        qr = r * q.r - i * q.i - j * q.j - k * q.k;
        qi = r * q.i - i * q.r + j * q.k - k * q.j;
        qj = r * q.j - i * q.k + j * q.r + k * q.i;
        qk = r * q.k + i * q.j - j * q.i + k * q.r;
        return new Quat(qr, qi, qj, qk);
    }

    /**
     * This doesn't seem to work, do not use
     */
    @Deprecated
    public void multiply(Quat q) {
        r = -(j * q.j + i * q.i + k * q.k - r * q.r);
        i = i * q.r + j * q.k + r * q.i - k * q.j;
        j = k * q.i + r * q.j + j * q.r - i * q.k;
        k = r * q.k + k * q.r + i * q.j - j * q.i;
    }

    public Quat getConjugate() {
        return new Quat(r, -i, -j, -k);
    }
}
