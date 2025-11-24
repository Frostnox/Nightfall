package frostnox.nightfall.action;

import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.ImpactSoundType;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Set;

/**
 * Contains data for single attacks.
 */
public abstract class Attack extends Action {
    protected static final EnumMap<EntityPart, AnimationData> EMPTY_MAP = new EnumMap<>(EntityPart.class);
    protected final float damage;
    protected final DamageType[] damageType;
    protected final HurtSphere hurtSpheres;
    protected final AttackEffect[] effects;
    protected final int maxTargets;
    protected final int stunDuration;
    protected final Set<EntityPart> modelKeys;

    public Attack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, @Nullable AttackEffect... effects) {
        super(duration);
        this.damageType = damageType;
        this.damage = damage;
        this.hurtSpheres = hurtSpheres;
        this.maxTargets = maxTargets;
        this.effects = effects;
        this.stunDuration = stunDuration;
        this.modelKeys = getDefaultAnimationData().keySet();
    }

    public Attack(float damage, DamageType[] damageType, HurtSphere hurtSpheres, int maxTargets, int stunDuration, int[] duration, Properties properties, @Nullable AttackEffect... effects) {
        super(properties, duration);
        this.damageType = damageType;
        this.damage = damage;
        this.hurtSpheres = hurtSpheres;
        this.maxTargets = maxTargets;
        this.effects = effects;
        this.stunDuration = stunDuration;
        this.modelKeys = getDefaultAnimationData().keySet();
    }

    @Override
    public boolean isDamaging(IActionTracker capA) {
        return super.isDamaging(capA) && capA.getFrame() >= getDamageStartFrame(capA.getState()) && capA.getFrame() <= getDamageEndFrame(capA.getState(), capA.getEntity());
    }

    public Vector3f getTranslation(LivingEntity user) {
        return Vector3f.ZERO;
    }

    /**
     * For NPCs, this should be the distance from the first offset to the desired point of rotation
     */
    public Vector3f getOffset(LivingEntity user) {
        return Vector3f.ZERO;
    }

    public ImpactSoundType getImpactSoundType(LivingEntity user) {
        return getDamageTypes(user)[0].getImpactSoundType();
    }

    public int getDamageStartFrame(int state) {
        return 2;
    }

    public int getDamageEndFrame(int state, LivingEntity user) {
        return getDuration(state, user) - 1;
    }

    public String getName(LivingEntity user) {
        return getDamageTypes(user)[0].toString();
    }

    /**
     * @return base damage for this attack
     */
    public float getDamage(LivingEntity user) {
        return damage;
    }

    public DamageType[] getDamageTypes(LivingEntity user) {
        return damageType;
    }

    public HurtSphere getHurtSpheres(LivingEntity user) {
        return hurtSpheres;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public int getAttacksPerTick() {
        return 1;
    }

    public AttackEffect[] getEffects(LivingEntity user) {
        return effects == null ? null : effects.clone();
    }

    public int getStunDuration() {
        return stunDuration;
    }

    /**
     * Called first in LivingHurtEvent before other calculations.
     * @param user - user executing attack
     * @param target - target of attack
     * @param damage - raw damage (no reductive calculations applied)
     * @return modified damage
     */
    public float onDamageDealtPre(LivingEntity user, LivingEntity target, float damage) {
        return damage;
    }

    /**
     * Called right before damage is applied to target.
     * @param user - user executing attack
     * @param target - target of attack
     * @param damage - final damage (all other calculations done)
     * @return modified damage
     */
    public float onDamageDealtPost(LivingEntity user, LivingEntity target, float damage) {
        return damage;
    }

    /**
     * For NPCs, this should contain the raw distances (divide by 16) from the final pivot to the current pivot in degrees
     * Make sure to return an entirely new copy (do not use the clone function)
     * @return default data for server transformations (should be ordered such that child transformations are first and parent transformations are last)
     */
    protected abstract EnumMap<EntityPart, AnimationData> getDefaultAnimationData();

    /**
     * @return a new copy of animation data that is synced with entity's ActionTracker capability
     */
    public EnumMap<EntityPart, AnimationData> getAnimationData(LivingEntity user, IActionTracker capA) {
        EnumMap<EntityPart, AnimationData> map = getDefaultAnimationData();
        for(AnimationData data : map.values()) {
            data.update(capA.getFrame(), capA.getDuration(), user.level.isClientSide() ? ClientEngine.get().getPartialTick() : 1);
        }
        return map;
    }
}
