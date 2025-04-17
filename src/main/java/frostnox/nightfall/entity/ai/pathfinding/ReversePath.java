package frostnox.nightfall.entity.ai.pathfinding;

import net.minecraft.util.Mth;

public class ReversePath {
    private Node[] reversePath;
    private int size, index;
    private final boolean success;

    ReversePath(Node end, boolean success) {
        reversePath = new Node[32];
        size = 0;
        while(end != null) {
            if(size == reversePath.length) {
                Node[] temp = new Node[size << 1];
                System.arraycopy(reversePath, 0, temp, 0, size);
                reversePath = temp;
            }
            reversePath[size] = end;
            end = end.prev;
            size++;
        }
        this.success = success;
        index = Math.max(0, size - 1 - 1);
    }

    public boolean reachesGoal() {
        return success;
    }

    public boolean isActive() {
        return index >= 0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = Mth.clamp(index, 0, size - 1);
    }

    public int getSize() {
        return size;
    }

    public void advanceIndex() {
        index--;
    }

    public Node getEndNode() {
        return reversePath[0];
    }

    public Node getStartNode() {
        return reversePath[size - 1];
    }

    public Node getCurrentNode() {
        return reversePath[index];
    }

    public Node getNode(int index) {
        return reversePath[index];
    }
}
