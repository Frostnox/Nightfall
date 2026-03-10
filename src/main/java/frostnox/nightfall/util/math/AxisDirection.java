package frostnox.nightfall.util.math;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

public enum AxisDirection {
    XP(1, 0, 0, 1),
    XN(-1, 0, 0, 0),
    YP(0, 1, 0, 3),
    YN(0, -1, 0, 2),
    ZP(0, 0, 1, 5),
    ZN(0, 0, -1, 4);

    public static final AxisDirection[] XYZ = new AxisDirection[]{XP, XN, YP, YN, ZP, ZN};
    public static final AxisDirection[] XZ = new AxisDirection[]{XP, XN, ZP, ZN};
    public static final AxisDirection[] YPXZ = new AxisDirection[]{YP, XP, XN, ZP, ZN};
    public static final AxisDirection[] XZYN = new AxisDirection[]{XP, XN, ZP, ZN, YN};

    public final int x;
    public final int y;
    public final int z;
    @Nonnull public final Direction normal;
    @Nonnull public final Direction.Axis axis;
    private final int oppositeIndex;

    AxisDirection(int x, int y, int z, int oppositeIndex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.normal = Direction.fromNormal(x, y, z);
        this.axis = normal.getAxis();
        this.oppositeIndex = oppositeIndex;
    }

    public AxisDirection getOpposite() {
        return XYZ[oppositeIndex];
    }
}
