package frostnox.nightfall.util.data;

public record Vec2d(double x, double y) {
    public static Vec2d ZERO = new Vec2d(0D, 0D);

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj instanceof Vec2d vec) return x == vec.x && y == vec.y;
        else return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Vec2d add(Vec2d vec) {
        return add(vec.x, vec.y);
    }

    public Vec2d add(double x, double y) {
        return new Vec2d(this.x + x, this.y + y);
    }

    public Vec2d subtract(Vec2d vec) {
        return subtract(vec.x, vec.y);
    }

    public Vec2d subtract(double x, double y) {
        return add(-x, -y);
    }

    public Vec2d multiply(Vec2d vec) {
        return multiply(vec.x, vec.y);
    }

    public Vec2d multiply(double xMag, double yMag) {
        return new Vec2d(x * xMag, y * yMag);
    }

    public Vec2d scale(double mag) {
        return multiply(mag, mag);
    }

    public double dot(Vec2d vec) {
        return x * vec.x + y * y;
    }

    public double cross(Vec2d vec) {
        return x * vec.y - y * vec.x;
    }

    public double lengthSqr() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    public Vec2d normalize() {
        double length = length();
        return length < 1.0E-4D ? ZERO : new Vec2d(x / length, y / length);
    }

    public Vec2d reverse() {
        return new Vec2d(-x, -y);
    }

    public double distSqr(Vec2d vec) {
        return distSqr(vec.x, vec.y);
    }

    public double distSqr(double x, double y) {
        double xDist = x - this.x, yDist = y - this.y;
        return xDist * xDist + yDist * yDist;
    }

    public double dist(Vec2d vec) {
        return dist(vec.x, vec.y);
    }

    public double dist(double x, double y) {
        return Math.sqrt(distSqr(x, y));
    }
}
