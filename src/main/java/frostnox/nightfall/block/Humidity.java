package frostnox.nightfall.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Humidity implements StringRepresentable {
    DRY("dry"),
    MOIST("moist"),
    IRRIGATED("irrigated");

    private final String name;

    Humidity(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
