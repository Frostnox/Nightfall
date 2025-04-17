package frostnox.nightfall.util.data;

/**
 * Value that is stored until its counter reaches the time limit and resets
 */
public class TimedValue<T> {
    private T value;
    private final T resetValue;
    private int counter;
    public final int timeLimit;

    public TimedValue(T resetValue, int timeLimit) {
        this.resetValue = resetValue;
        this.timeLimit = timeLimit;
        reset();
    }

    public T getValue() {
        return value;
    }

    public int getCounter() {
        return counter;
    }

    public void tick() {
        if(isActive()) counter++;
        if(counter > timeLimit) reset();
    }

    public void set(T value, int counter) {
        this.value = value;
        this.counter = counter;
    }

    public void set(T value) {
        set(value, 0);
    }

    public void reset() {
        value = resetValue;
        counter = timeLimit + 1;
    }

    public boolean isActive() {
        return counter <= timeLimit;
    }
}
