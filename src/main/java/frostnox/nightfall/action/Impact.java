package frostnox.nightfall.action;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum Impact implements StringRepresentable {
    NONE, LOW, MEDIUM, HIGH, MAXIMUM;

    private final String name;

    Impact() {
        this.name = name().toLowerCase(Locale.ROOT);
    }

    public Impact increase() {
        if(ordinal() < values().length - 1) return values()[ordinal() + 1];
        else return MAXIMUM;
    }

    public Impact decrease() {
        if(ordinal() == 0) return NONE;
        else return values()[ordinal() + 1];
    }

    public boolean negatedBy(Poise poise) {
        return poise.negates(this);
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