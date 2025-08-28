package frostnox.nightfall.action.npc.ectoplasm;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

public class EctoplasmExplode extends Action {
    protected final double radius;
    protected final float damage, knockback;
    protected final int stun;

    public EctoplasmExplode(Properties properties, double radius, float damage, float knockback, int stun, int... duration) {
        super(properties, duration);
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.stun = stun;
    }

    @Override
    public boolean isStateDamaging(int state) {
        return state == 1;
    }

    @Override
    public double getMaxDistToStart(LivingEntity user) {
        return 2.25;
    }

    @Override
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        switch(state) {
            case 0 -> {
                mCalc.add(0, Mth.cos((frame - 1 + mCalc.partialTicks) * 3F) * 2, 0);
            }
            case 1 -> {
                mCalc.extend(0, 0, 0, Easing.outQuart);
            }
        }
    }

    @Override
    public void onTick(LivingEntity user) {
        if(!user.level.isClientSide) {
            IActionTracker capA = ActionTracker.get(user);
            if(capA.isDamaging() && capA.getFrame() == 1) {
                CombatUtil.damageAllInRadius(user, user.getEyePosition(), radius, damage, knockback, 0.5F,
                        DamageTypeSource.createExplosionSource(user).setImpact(getImpact(capA)).setStun(stun));
                ((ServerLevel) user.level).sendParticles(ParticleTypes.EXPLOSION.getType(), user.getX(), user.getEyeY(), user.getZ(), 1, 0, 0, 0, 0);
                EctoplasmEntity ectoplasm = (EctoplasmEntity) user;
                ectoplasm.onKillRemoval();
                user.remove(Entity.RemovalReason.KILLED);
            }
        }
    }
}
