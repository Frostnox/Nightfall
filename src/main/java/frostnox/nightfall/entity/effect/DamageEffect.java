package frostnox.nightfall.entity.effect;

import frostnox.nightfall.action.DamageTypeSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DamageEffect extends MobEffect {
    public final int customColor;
    protected final float damage;
    public final DamageTypeSource damageSource;

    public DamageEffect(MobEffectCategory category, int vanillaColor, int color, float damage, DamageTypeSource source) {
        super(category, vanillaColor);
        this.customColor = color;
        this.damage = damage;
        damageSource = source;
    }

    public DamageEffect(MobEffectCategory category, int vanillaColor, int color) {
        this(category, vanillaColor, color, 0F, DamageTypeSource.GENERIC);
    }

    public DamageEffect(MobEffectCategory category, int color) {
        this(category, 0, color); //0 will make the default particles invisible
    }

    public DamageEffect(MobEffectCategory category) {
        this(category, 0, 0);
    }

    public DamageEffect(MobEffectCategory category, float damage, DamageTypeSource source) {
        this(category, 0, 0, damage, source);
    }

    public float getDamage(LivingEntity entity, int duration, int amplifier) {
        return damage;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
