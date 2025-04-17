package frostnox.nightfall.item;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum Style implements IStyle {
    SURVIVOR("plate", "scale", "chainmail"),
    EXPLORER("plate", "scale", "chainmail"),
    SLAYER("plate", "scale", "chainmail"),
    UNDEAD();

    private final List<String> materials;

    Style(String... materials) {
        this.materials = ImmutableList.copyOf(materials);
    }

    @Override
    public List<String> getMaterials() {
        return materials;
    }

    @Override
    public boolean hasMaterial(String name) {
        return materials.contains(name);
    }
}
