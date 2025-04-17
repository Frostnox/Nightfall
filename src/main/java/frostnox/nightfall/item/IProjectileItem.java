package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageType;
import net.minecraft.world.item.Item;

public interface IProjectileItem {
    Item getItem();

    DamageType[] getProjectileDamageType();

    float getProjectileDamage();

    float getProjectileVelocityScalar();

    float getProjectileInaccuracy();

    int getAmmoId();
}
