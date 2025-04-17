package frostnox.nightfall.action.npc;

import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.AttackEffect;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.item.IWeaponItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class NPCAttack extends Attack {
    public NPCAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, effects);
    }

    public NPCAttack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(damage, damageType, hurtSpheres, maxTargets, stunDuration, duration, properties, effects);
    }

    public InteractionHand getHand() {
        return InteractionHand.MAIN_HAND;
    }

    protected boolean hasWeapon(LivingEntity user) {
        return user.getItemInHand(getHand()).getItem() instanceof IWeaponItem;
    }

    @Override
    public float getDamage(LivingEntity user) {
        if(user.getItemInHand(getHand()).getItem() instanceof IWeaponItem weapon) return weapon.getBaseDamage() + super.getDamage(user);
        return super.getDamage(user);
    }

    @Override
    public AttackEffect[] getEffects(LivingEntity user) {
        AttackEffect[] effects = super.getEffects(user);
        AttackEffect weaponEffect = null;
        if(user != null && user.getItemInHand(getHand()).getItem() instanceof IWeaponItem weapon) weaponEffect = weapon.getBaseAttackEffect();
        if(effects == null) return new AttackEffect[]{weaponEffect};
        else {
            if(weaponEffect == null) return effects;
            else {
                AttackEffect[] newEffects = new AttackEffect[effects.length + 1];
                for(int i = 0; i < effects.length; i++) newEffects[i] = effects[i];
                newEffects[effects.length] = weaponEffect;
                return newEffects;
            }
        }
    }

    @Override
    public DamageType[] getDamageTypes(LivingEntity user) {
        if(user.getItemInHand(getHand()).getItem() instanceof IWeaponItem weapon) return weapon.getDefaultDamageTypes();
        return super.getDamageTypes(user);
    }

    @Override
    public HurtSphere getHurtSpheres(LivingEntity user) {
        if(user.getItemInHand(getHand()).getItem() instanceof IWeaponItem weapon) return weapon.getNPCHurtSpheres();
        return super.getHurtSpheres(user);
    }
}
