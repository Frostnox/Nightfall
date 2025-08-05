package frostnox.nightfall.action;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum Poise implements StringRepresentable {
    NONE, LOW, MEDIUM, HIGH, MAXIMUM;

    private final String name;

    Poise() {
        this.name = name().toLowerCase(Locale.ROOT);
    }

    public boolean negates(Impact impact) {
        return ordinal() >= impact.ordinal();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}