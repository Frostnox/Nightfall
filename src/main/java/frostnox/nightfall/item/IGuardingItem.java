package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageTypeSource;

public interface IGuardingItem {
    float getDefense(DamageTypeSource source);

    float getAbsorption(DamageTypeSource source);
}
