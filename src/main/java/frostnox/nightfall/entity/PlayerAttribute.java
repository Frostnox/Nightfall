package frostnox.nightfall.entity;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum PlayerAttribute implements StringRepresentable {
    VITALITY, ENDURANCE, WILLPOWER, STRENGTH, AGILITY, PERCEPTION;

    private final String name;

    PlayerAttribute() {
        this.name = name().toLowerCase(Locale.ROOT);
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
