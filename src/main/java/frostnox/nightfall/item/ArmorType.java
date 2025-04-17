package frostnox.nightfall.item;

import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.List;

public enum ArmorType {
    PLATE(FloatList.of(0.8F, 0.9F, 0.9F, 1F, 1F, 1F)),
    SCALE(FloatList.of(0.5F, 0.8F, 0.8F, 0.9F, 0.9F, 0.9F)),
    CHAINMAIL(FloatList.of(0.8F, 0.8F, 0.5F, 0.8F, 0.8F, 0.8F));

    public final List<Float> defenseAbsorptionMul;

    ArmorType(List<Float> defenseAbsorptionMul) {
        this.defenseAbsorptionMul = defenseAbsorptionMul;
    }
}
