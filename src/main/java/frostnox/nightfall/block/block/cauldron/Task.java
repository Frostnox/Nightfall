package frostnox.nightfall.block.block.cauldron;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum Task implements StringRepresentable {
    IDLE, COOK, DONE;

    private final String name;

    Task() {
        this.name = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
