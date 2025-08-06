package frostnox.nightfall.item;

import frostnox.nightfall.action.Poise;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.List;

public enum ArmorType {
    PLATE(FloatList.of(0.8F, 1F, 1F, 1F, 1F, 1F), Poise.MEDIUM),
    SCALE(FloatList.of(0.6F, 0.8F, 0.8F, 0.8F, 0.8F, 0.8F), Poise.LOW),
    CHAINMAIL(FloatList.of(0.8F, 0.8F, 0.6F, 0.8F, 0.8F, 0.8F), Poise.LOW);

    public final List<Float> defenseMul;
    public final Poise poise;

    ArmorType(List<Float> defenseMul, Poise poise) {
        this.defenseMul = defenseMul;
        this.poise = poise;
    }
}
