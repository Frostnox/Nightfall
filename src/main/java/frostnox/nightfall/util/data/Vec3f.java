package frostnox.nightfall.util.data;

public record Vec3f(float x, float y, float z) {
    public static Vec3f ZERO = new Vec3f(0F, 0F, 0F);

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj instanceof Vec3f vec) return x == vec.x && y == vec.y && z == vec.z;
        else return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public Vec3f add(Vec3f vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public Vec3f add(float x, float y, float z) {
        return new Vec3f(this.x + x, this.y + y, this.z + z);
    }

    public Vec3f subtract(Vec3f vec) {
        return subtract(vec.x, vec.y, vec.z);
    }

    public Vec3f subtract(float x, float y, float z) {
        return add(-x, -y, -z);
    }

    public Vec3f multiply(Vec3f vec) {
        return multiply(vec.x, vec.y, vec.z);
    }

    public Vec3f multiply(float xMag, float yMag, float zMag) {
        return new Vec3f(x * xMag, y * yMag, z * zMag);
    }

    public Vec3f cross(Vec3f vec) {
        float vX = vec.x(), vY = vec.y(), vZ = vec.z();
        return new Vec3f(y * vZ - z * vY, z * vX - x * vZ, x * vY - y * vX);
    }

    public Vec3f scale(float mag) {
        return multiply(mag, mag, mag);
    }

    public float lengthSqr() {
        return x * x + y * y + z * z;
    }
    
    public float length() {
        return (float) Math.sqrt(lengthSqr());
    }

    public Vec3f normalize() {
        float length = length();
        return length < 1.0E-4F ? ZERO : new Vec3f(x / length, y / length, z / length);
    }

    public float distSqr(Vec3f vec) {
        return distSqr(vec.x, vec.y, vec.z);
    }

    public float distSqr(float x, float y, float z) {
        float xDist = x - this.x, yDist = y - this.y, zDist = z - this.z;
        return xDist * xDist + yDist * yDist + zDist * zDist;
    }
    
    public float dist(Vec3f vec) {
        return dist(vec.x, vec.y, vec.z);
    }
    
    public float dist(float x, float y, float z) {
        return (float) Math.sqrt(distSqr(x, y, z));
    }
}
