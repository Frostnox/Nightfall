package frostnox.nightfall.util.data;

public record Vec2f(float x, float y) {
    public static Vec2f ZERO = new Vec2f(0F, 0F);

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj instanceof Vec2f vec) return x == vec.x && y == vec.y;
        else return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Vec2f add(Vec2f vec) {
        return add(vec.x, vec.y);
    }

    public Vec2f add(float x, float y) {
        return new Vec2f(this.x + x, this.y + y);
    }

    public Vec2f subtract(Vec2f vec) {
        return subtract(vec.x, vec.y);
    }

    public Vec2f subtract(float x, float y) {
        return add(-x, -y);
    }

    public Vec2f multiply(Vec2f vec) {
        return multiply(vec.x, vec.y);
    }

    public Vec2f multiply(float xMag, float yMag) {
        return new Vec2f(x * xMag, y * yMag);
    }

    public Vec2f scale(float mag) {
        return multiply(mag, mag);
    }

    public float dot(Vec2f vec) {
        return x * vec.x + y * y;
    }

    public float cross(Vec2f vec) {
        return x * vec.y - y * vec.x;
    }

    public float lengthSqr() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(lengthSqr());
    }

    public Vec2f normalize() {
        float length = length();
        return length < 1.0E-4F ? ZERO : new Vec2f(x / length, y / length);
    }

    public Vec2f reverse() {
        return new Vec2f(-x, -y);
    }

    public float distSqr(Vec2f vec) {
        return distSqr(vec.x, vec.y);
    }

    public float distSqr(float x, float y) {
        float xDist = x - this.x, yDist = y - this.y;
        return xDist * xDist + yDist * yDist;
    }

    public float dist(Vec2f vec) {
        return dist(vec.x, vec.y);
    }

    public float dist(float x, float y) {
        return (float) Math.sqrt(distSqr(x, y));
    }
}
