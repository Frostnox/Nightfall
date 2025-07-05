package frostnox.nightfall.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum OctalDirection implements StringRepresentable {
    NORTH(0F, 0F, -1F, 1),
    SOUTH(0F, 0F, 1F, 0),
    WEST(-1F, 0F, 0F, 3),
    EAST(1F, 0F, 0F, 2),
    NORTHWEST(-0.7071F, 0F, -0.7071F, 7),
    NORTHEAST(0.7071F, 0F, -0.7071F, 6),
    SOUTHWEST(-0.7071F, 0F, 0.7071F, 5),
    SOUTHEAST(0.7071F, 0F, 0.7071F, 4),
    UP(0F, 1F, 0F, 9),
    DOWN(0F, -1F, 0F, 8),
    CENTER(0F, 0F, 0F, 10);

    //For convenience
    public static final OctalDirection[] CARDINALS = new OctalDirection[] {NORTH, WEST, SOUTH, EAST};
    public static final OctalDirection[] CARDINALS_UP = new OctalDirection[] {NORTH, WEST, SOUTH, EAST, UP};
    public static final OctalDirection[] STRAIGHTS = new OctalDirection[] {NORTH, WEST, SOUTH, EAST, UP, DOWN};
    public static final OctalDirection[] DIAGONALS = new OctalDirection[] {NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST};
    public static final OctalDirection[] OCTALS = new OctalDirection[] {NORTH, WEST, SOUTH, EAST, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST};
    public static final OctalDirection[] OCTALS_UP = new OctalDirection[] {NORTH, WEST, SOUTH, EAST, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST, UP};
    public static final OctalDirection[] NORTH_SINGLE = new OctalDirection[] {NORTH};
    public static final OctalDirection[] SOUTH_SINGLE = new OctalDirection[] {SOUTH};
    public static final OctalDirection[] WEST_SINGLE = new OctalDirection[] {WEST};
    public static final OctalDirection[] EAST_SINGLE = new OctalDirection[] {EAST};
    public static final OctalDirection[] NORTHWEST_SINGLE = new OctalDirection[] {NORTHWEST};
    public static final OctalDirection[] NORTHEAST_SINGLE = new OctalDirection[] {NORTHEAST};
    public static final OctalDirection[] SOUTHWEST_SINGLE = new OctalDirection[] {SOUTHWEST};
    public static final OctalDirection[] SOUTHEAST_SINGLE = new OctalDirection[] {SOUTHEAST};
    public static final OctalDirection[] CENTER_SINGLE = new OctalDirection[] {CENTER};
    public static final OctalDirection[] NORTH_AND_WEST = new OctalDirection[] {NORTH, WEST};
    public static final OctalDirection[] NORTH_AND_EAST = new OctalDirection[] {NORTH, EAST};
    public static final OctalDirection[] NORTH_AND_SOUTH = new OctalDirection[] {NORTH, SOUTH};
    public static final OctalDirection[] SOUTH_AND_WEST = new OctalDirection[] {SOUTH, WEST};
    public static final OctalDirection[] SOUTH_AND_EAST = new OctalDirection[] {SOUTH, EAST};
    public static final OctalDirection[] WEST_AND_EAST = new OctalDirection[] {WEST, EAST};
    public static final OctalDirection[] NORTH_DIAGONALS = new OctalDirection[] {NORTHWEST, NORTHEAST};
    public static final OctalDirection[] SOUTH_DIAGONALS = new OctalDirection[] {SOUTHWEST, SOUTHEAST};
    public static final OctalDirection[] WEST_DIAGONALS = new OctalDirection[] {SOUTHWEST, NORTHWEST};
    public static final OctalDirection[] EAST_DIAGONALS = new OctalDirection[] {SOUTHEAST, NORTHEAST};

    public final float xStep, yStep, zStep, xStepHalf, yStepHalf, zStepHalf;
    public final int xStepInt, yStepInt, zStepInt;
    private final int oppositeIndex;
    private final String name;

    OctalDirection(float xStep, float yStep, float zStep, int oppositeIndex) {
        this.xStep = xStep;
        this.yStep = yStep;
        this.zStep = zStep;
        this.xStepHalf = xStep / 2F;
        this.yStepHalf = yStep / 2F;
        this.zStepHalf = zStep / 2F;
        this.xStepInt = Math.round(xStep);
        this.yStepInt = Math.round(yStep);
        this.zStepInt = Math.round(zStep);
        this.oppositeIndex = oppositeIndex;
        this.name = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static OctalDirection fromDirection(Direction direction) {
        return switch(direction) {
            case NORTH -> NORTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public Direction toDirection() {
        return switch(this) {
            case NORTH, NORTHWEST, NORTHEAST -> Direction.NORTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            case SOUTH, SOUTHWEST, SOUTHEAST -> Direction.SOUTH;
            case UP -> Direction.UP;
            case DOWN, CENTER -> Direction.DOWN;
        };
    }

    public boolean equalsDirection(Direction dir) {
        return dir.toString().equals(name);
    }

    public BlockPos move(BlockPos pos) {
        return pos.offset(xStepInt, yStepInt, zStepInt);
    }

    public BlockPos.MutableBlockPos move(BlockPos.MutableBlockPos pos) {
        return pos.move(xStepInt, yStepInt, zStepInt);
    }

    public BlockPos.MutableBlockPos moveFrom(BlockPos.MutableBlockPos pos, Vec3i from) {
        return pos.setWithOffset(from, xStepInt, yStepInt, zStepInt);
    }

    public OctalDirection straighten() {
        if(isDiagonal()) return ordinal() <= 5 ? NORTH : SOUTH;
        return this;
    }

    public OctalDirection getOpposite() {
        return values()[oppositeIndex];
    }

    public boolean isCardinal() {
        return ordinal() < 4;
    }

    public boolean isDiagonal() {
        return ordinal() >= 4 && ordinal() <= 7;
    }

    public boolean isVertical() {
        return yStepInt != 0;
    }

    public boolean isNearSame(OctalDirection direction) {
        if(yStep != 0F) return Math.abs(yStep + direction.yStep) > 1.5F;
        return Math.abs(xStep + direction.xStep) > 1.5F || Math.abs(zStep + direction.zStep) > 1.5F;
    }

    public boolean isNearOpposite(OctalDirection direction) {
        if(yStep != 0F) return Math.abs(yStep + direction.yStep) < 0.5F;
        if(direction == CENTER) return false;
        return Math.abs(xStep + direction.xStep) < 0.8F && Math.abs(zStep + direction.zStep) < 0.8F;
    }

    public boolean isOpposite(OctalDirection direction) {
        return xStep == -direction.xStep && yStep == -direction.yStep && zStep == -direction.zStep;
    }
}
