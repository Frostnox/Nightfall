package frostnox.nightfall.util.data;

public record Line2d(double x1, double y1, double x2, double y2) {
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj instanceof Line2d line) return x1 == line.x1 && y1 == line.y1 && x2 == line.x2 && y2 == line.y2;
        else return false;
    }

    @Override
    public String toString() {
        return "(" + x1 + ", " + y1 + ") -> (" + x1 + ", " + y1 + ")";
    }

    public double lengthSqr() {
        double dX = x2 - x1, dY = y2 - y1;
        return dX * dX + dY * dY;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }
}
