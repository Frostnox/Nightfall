package frostnox.nightfall.item;

import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerActionSet;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IWeaponItem extends IActionableItem, IGuardingItem {
    PlayerActionSet getBaseActionSet();

    PlayerActionSet getActionSet(Player player);

    /**
     * @return base damage dealt
     */
    float getBaseDamage();

    float getDamageMultiplier();

    Weight getWeight();
    
    HurtSphere getHurtSpheres();

    HurtSphere getNPCHurtSpheres();

    DamageType[] getDefaultDamageTypes();

    @Nullable AttackEffect getBaseAttackEffect();

    /**
     * Tries to start a basic attack
     * @param user of attack
     * @return true if attack starts or is queued
     */
    boolean tryBasicAttack(Player user);

    /**
     * Tries to start an alternate attack
     * @param user of attack
     * @return true if attack starts or is queued
     */
    boolean tryAlternateAttack(Player user);

    /**
     * Tries to start technique
     * @param user of technique
     * @return true if technique starts
     */
    boolean tryTechnique(Player user);

    /**
     * Tries to start a crawling attack
     * @param user of attack
     * @return true if attack starts
     */
    boolean tryCrawlingAttack(Player user);
}
