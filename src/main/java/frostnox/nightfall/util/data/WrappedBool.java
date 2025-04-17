package frostnox.nightfall.util.data;

public class WrappedBool {
    public boolean val;

    public WrappedBool() {

    }

    public WrappedBool(boolean val) {
        this.val = val;
    }

    public boolean setAndGet(boolean val) {
        this.val = val;
        return val;
    }
}
