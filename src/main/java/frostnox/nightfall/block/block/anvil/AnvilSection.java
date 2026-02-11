package frostnox.nightfall.block.block.anvil;

public enum AnvilSection {
    HORN(0.25F), FLAT(0.5F), EDGE(0.75F);

    public final float center;

    AnvilSection(float center) {
        this.center = center;
    }
}