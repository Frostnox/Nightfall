package frostnox.nightfall.util.data;

public class WrappedInt {
    public int val;

    public WrappedInt() {

    }

    public WrappedInt(int val) {
        this.val = val;
    }

    public int setAndGet(int val) {
        this.val = val;
        return val;
    }
}
