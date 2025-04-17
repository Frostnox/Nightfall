package frostnox.nightfall.entity.ai.pathfinding;

public enum NodeType {
    OPEN_OR_WALKABLE(0, false, false, false), //This should always be converted into an open or walkable
    OPEN(0, false, false, false),
    CLOSED(-1, true, false, false),
    WALKABLE(0, false, true, false),
    PASSABLE_FLUID(1, false, true, false),
    NEAR_DANGER(1, false, true, false),
    PASSABLE_DANGER_MINOR(2.5F, false, true, true),
    PASSABLE_DANGER_MAJOR(5, false, true, true),
    IMPASSABLE_DANGER_MINOR(2.5F, true, false, true),
    IMPASSABLE_DANGER_MAJOR(5, true, false, true),
    IMPASSABLE_DANGER(-1, true, false, true),
    BUILDABLE_WALKABLE(3, false, true, false);

    public final float cost;
    public final boolean blocksMovement;
    public final boolean walkable;
    public final boolean inDanger;

    NodeType(float cost, boolean blocksMovement, boolean walkable, boolean inDanger) {
        this.cost = cost;
        this.blocksMovement = blocksMovement;
        this.walkable = walkable;
        this.inDanger = inDanger;
    }
}