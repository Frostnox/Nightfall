package frostnox.nightfall.util.math;

import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.world.phys.Vec3;

public class Mat4f {
    public float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

    public Mat4f() {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m03 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m13 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
        this.m23 = 0.0F;
        this.m30 = 0.0F;
        this.m31 = 0.0F;
        this.m32 = 0.0F;
        this.m33 = 1.0F;
    }

    public Mat4f(Quat q) {
        float f = q.i;
        float f1 = q.j;
        float f2 = q.k;
        float f3 = q.r;
        float f4 = 2.0F * f * f;
        float f5 = 2.0F * f1 * f1;
        float f6 = 2.0F * f2 * f2;
        this.m00 = 1.0F - f5 - f6;
        this.m11 = 1.0F - f6 - f4;
        this.m22 = 1.0F - f4 - f5;
        this.m33 = 1.0F;
        float f7 = f * f1;
        float f8 = f1 * f2;
        float f9 = f2 * f;
        float f10 = f * f3;
        float f11 = f1 * f3;
        float f12 = f2 * f3;
        this.m10 = 2.0F * (f7 + f12);
        this.m01 = 2.0F * (f7 - f12);
        this.m20 = 2.0F * (f9 - f11);
        this.m02 = 2.0F * (f9 + f11);
        this.m21 = 2.0F * (f8 + f10);
        this.m12 = 2.0F * (f8 - f10);
    }

    public Mat4f(Mat4f matrix) {
        this.m00 = matrix.m00;
        this.m01 = matrix.m01;
        this.m02 = matrix.m02;
        this.m03 = matrix.m03;
        this.m10 = matrix.m10;
        this.m11 = matrix.m11;
        this.m12 = matrix.m12;
        this.m13 = matrix.m13;
        this.m20 = matrix.m20;
        this.m21 = matrix.m21;
        this.m22 = matrix.m22;
        this.m23 = matrix.m23;
        this.m30 = matrix.m30;
        this.m31 = matrix.m31;
        this.m32 = matrix.m32;
        this.m33 = matrix.m33;
    }

    public com.mojang.math.Matrix4f toVanillaMatrix() {
        return new com.mojang.math.Matrix4f(new float[] {
                m00, m01, m02, m03, m10, m11, m12, m13,
                m20, m21, m22, m23, m30, m31, m32, m33
        });
    }

    public void multiply(Quat quaternion) {
        multiply(new Mat4f(quaternion));
    }

    public void multiply(Mat4f matrix) {
        float m00 = this.m00 * matrix.m00 + this.m01 * matrix.m10 + this.m02 * matrix.m20 + this.m03 * matrix.m30;
        float m01 = this.m00 * matrix.m01 + this.m01 * matrix.m11 + this.m02 * matrix.m21 + this.m03 * matrix.m31;
        float m02 = this.m00 * matrix.m02 + this.m01 * matrix.m12 + this.m02 * matrix.m22 + this.m03 * matrix.m32;
        float m03 = this.m00 * matrix.m03 + this.m01 * matrix.m13 + this.m02 * matrix.m23 + this.m03 * matrix.m33;
        float m10 = this.m10 * matrix.m00 + this.m11 * matrix.m10 + this.m12 * matrix.m20 + this.m13 * matrix.m30;
        float m11 = this.m10 * matrix.m01 + this.m11 * matrix.m11 + this.m12 * matrix.m21 + this.m13 * matrix.m31;
        float m12 = this.m10 * matrix.m02 + this.m11 * matrix.m12 + this.m12 * matrix.m22 + this.m13 * matrix.m32;
        float m13 = this.m10 * matrix.m03 + this.m11 * matrix.m13 + this.m12 * matrix.m23 + this.m13 * matrix.m33;
        float m20 = this.m20 * matrix.m00 + this.m21 * matrix.m10 + this.m22 * matrix.m20 + this.m23 * matrix.m30;
        float m21 = this.m20 * matrix.m01 + this.m21 * matrix.m11 + this.m22 * matrix.m21 + this.m23 * matrix.m31;
        float m22 = this.m20 * matrix.m02 + this.m21 * matrix.m12 + this.m22 * matrix.m22 + this.m23 * matrix.m32;
        float m23 = this.m20 * matrix.m03 + this.m21 * matrix.m13 + this.m22 * matrix.m23 + this.m23 * matrix.m33;
        float m30 = this.m30 * matrix.m00 + this.m31 * matrix.m10 + this.m32 * matrix.m20 + this.m33 * matrix.m30;
        float m31 = this.m30 * matrix.m01 + this.m31 * matrix.m11 + this.m32 * matrix.m21 + this.m33 * matrix.m31;
        float m32 = this.m30 * matrix.m02 + this.m31 * matrix.m12 + this.m32 * matrix.m22 + this.m33 * matrix.m32;
        float m33 = this.m30 * matrix.m03 + this.m31 * matrix.m13 + this.m32 * matrix.m23 + this.m33 * matrix.m33;

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Mat4f getProduct(Mat4f matrix) {
        float m00 = this.m00 * matrix.m00 + this.m01 * matrix.m10 + this.m02 * matrix.m20 + this.m03 * matrix.m30;
        float m01 = this.m00 * matrix.m01 + this.m01 * matrix.m11 + this.m02 * matrix.m21 + this.m03 * matrix.m31;
        float m02 = this.m00 * matrix.m02 + this.m01 * matrix.m12 + this.m02 * matrix.m22 + this.m03 * matrix.m32;
        float m03 = this.m00 * matrix.m03 + this.m01 * matrix.m13 + this.m02 * matrix.m23 + this.m03 * matrix.m33;
        float m10 = this.m10 * matrix.m00 + this.m11 * matrix.m10 + this.m12 * matrix.m20 + this.m13 * matrix.m30;
        float m11 = this.m10 * matrix.m01 + this.m11 * matrix.m11 + this.m12 * matrix.m21 + this.m13 * matrix.m31;
        float m12 = this.m10 * matrix.m02 + this.m11 * matrix.m12 + this.m12 * matrix.m22 + this.m13 * matrix.m32;
        float m13 = this.m10 * matrix.m03 + this.m11 * matrix.m13 + this.m12 * matrix.m23 + this.m13 * matrix.m33;
        float m20 = this.m20 * matrix.m00 + this.m21 * matrix.m10 + this.m22 * matrix.m20 + this.m23 * matrix.m30;
        float m21 = this.m20 * matrix.m01 + this.m21 * matrix.m11 + this.m22 * matrix.m21 + this.m23 * matrix.m31;
        float m22 = this.m20 * matrix.m02 + this.m21 * matrix.m12 + this.m22 * matrix.m22 + this.m23 * matrix.m32;
        float m23 = this.m20 * matrix.m03 + this.m21 * matrix.m13 + this.m22 * matrix.m23 + this.m23 * matrix.m33;
        float m30 = this.m30 * matrix.m00 + this.m31 * matrix.m10 + this.m32 * matrix.m20 + this.m33 * matrix.m30;
        float m31 = this.m30 * matrix.m01 + this.m31 * matrix.m11 + this.m32 * matrix.m21 + this.m33 * matrix.m31;
        float m32 = this.m30 * matrix.m02 + this.m31 * matrix.m12 + this.m32 * matrix.m22 + this.m33 * matrix.m32;
        float m33 = this.m30 * matrix.m03 + this.m31 * matrix.m13 + this.m32 * matrix.m23 + this.m33 * matrix.m33;

        Mat4f product = new Mat4f();
        product.m00 = m00;
        product.m01 = m01;
        product.m02 = m02;
        product.m03 = m03;
        product.m10 = m10;
        product.m11 = m11;
        product.m12 = m12;
        product.m13 = m13;
        product.m20 = m20;
        product.m21 = m21;
        product.m22 = m22;
        product.m23 = m23;
        product.m30 = m30;
        product.m31 = m31;
        product.m32 = m32;
        product.m33 = m33;
        return product;
    }

    public void transpose() {
        float f = this.m10;
        this.m10 = this.m01;
        this.m01 = f;
        f = this.m20;
        this.m20 = this.m02;
        this.m02 = f;
        f = this.m21;
        this.m21 = this.m12;
        this.m12 = f;
        f = this.m30;
        this.m30 = this.m03;
        this.m03 = f;
        f = this.m31;
        this.m31 = this.m13;
        this.m13 = f;
        f = this.m32;
        this.m32 = this.m23;
        this.m23 = f;
    }

    public void multiplyWithTranslation(float x, float y, float z) {
        this.m03 += this.m00 * x + this.m01 * y + this.m02 * z;
        this.m13 += this.m10 * x + this.m11 * y + this.m12 * z;
        this.m23 += this.m20 * x + this.m21 * y + this.m22 * z;
        this.m33 += this.m30 * x + this.m31 * y + this.m32 * z;
    }

    public void setTranslation(float x, float y, float z) {
        this.m00 = 1.0F;
        this.m11 = 1.0F;
        this.m22 = 1.0F;
        this.m33 = 1.0F;
        this.m03 = x;
        this.m13 = y;
        this.m23 = z;
    }

    public Vector3f getTranslation() {
        return new Vector3f(this.m03, this.m13, this.m23);
    }

    public void transformVector3f(Vector3f vec) {
        float x = this.m00 * vec.x() + this.m10 * vec.y() + this.m20 * vec.z();
        float y = this.m01 * vec.x() + this.m11 * vec.y() + this.m21 * vec.z();
        float z = this.m02 * vec.x() + this.m12 * vec.y() + this.m22 * vec.z();
        vec.set(x, y, z);
    }

    public void transformVector4f(Vector4f vec) {
        float x = this.m00 * vec.x() + this.m10 * vec.y() + this.m20 * vec.z();
        float y = this.m01 * vec.x() + this.m11 * vec.y() + this.m21 * vec.z();
        float z = this.m02 * vec.x() + this.m12 * vec.y() + this.m22 * vec.z();
        vec.set(x, y, z, vec.w());
    }

    public Vec3 transformVector3d(Vec3 vec) {
        double x = this.m00 * vec.x + this.m10 * vec.y + this.m20 * vec.z;
        double y = this.m01 * vec.x + this.m11 * vec.y + this.m21 * vec.z;
        double z = this.m02 * vec.x + this.m12 * vec.y + this.m22 * vec.z;
        return new Vec3(x, y, z);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Matrix4f:\n");
        stringbuilder.append(this.m00);
        stringbuilder.append(" ");
        stringbuilder.append(this.m01);
        stringbuilder.append(" ");
        stringbuilder.append(this.m02);
        stringbuilder.append(" ");
        stringbuilder.append(this.m03);
        stringbuilder.append("\n");
        stringbuilder.append(this.m10);
        stringbuilder.append(" ");
        stringbuilder.append(this.m11);
        stringbuilder.append(" ");
        stringbuilder.append(this.m12);
        stringbuilder.append(" ");
        stringbuilder.append(this.m13);
        stringbuilder.append("\n");
        stringbuilder.append(this.m20);
        stringbuilder.append(" ");
        stringbuilder.append(this.m21);
        stringbuilder.append(" ");
        stringbuilder.append(this.m22);
        stringbuilder.append(" ");
        stringbuilder.append(this.m23);
        stringbuilder.append("\n");
        stringbuilder.append(this.m30);
        stringbuilder.append(" ");
        stringbuilder.append(this.m31);
        stringbuilder.append(" ");
        stringbuilder.append(this.m32);
        stringbuilder.append(" ");
        stringbuilder.append(this.m33);
        stringbuilder.append("\n");
        return stringbuilder.toString();
    }

    /**
     * Expects rotation matrix to be in order ZYX
     * @return pitch, yaw, roll
     */
    public Vector3f toEulerAngles() {
        float x, y, z;
        if(this.m20 < 1) {
            if(this.m20 > -1) {
                y = (float) Math.asin(-this.m20);
                z = (float) Math.atan2(this.m10, this.m00);
                x = (float) Math.atan2(this.m21, this.m22);
            }
            else {
                y = MathUtil.PI / 2F;
                z = (float) -Math.atan2(-this.m12, this.m11);
                x = 0;
            }
        }
        else {
            y = -MathUtil.PI / 2F;
            z = (float) Math.atan2(-this.m12, this.m11);
            x = 0;
        }
        return new Vector3f(-x, y, z);
    }
}
