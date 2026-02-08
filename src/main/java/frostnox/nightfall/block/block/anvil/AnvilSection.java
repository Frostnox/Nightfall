package frostnox.nightfall.block.block.anvil;

public enum AnvilSection {
    HORN(0.25), FLAT(0.5), EDGE(0.75);

    public final double center;

    AnvilSection(double center) {
        this.center = center;
    }
}