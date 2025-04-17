package frostnox.nightfall.block;

public enum Fertility {
    NONE, GRAVEL, SAND, POOR, FAIR, RICH;

    public boolean richerThan(Fertility fertility) {
        return this.ordinal() > fertility.ordinal();
    }

    public boolean poorerThan(Fertility fertility) {
        return this.ordinal() < fertility.ordinal();
    }

    public boolean richerThanOrEqual(Fertility fertility) {
        return this.ordinal() >= fertility.ordinal();
    }

    public boolean poorerThanOrEqual(Fertility fertility) {
        return this.ordinal() <= fertility.ordinal();
    }
}
