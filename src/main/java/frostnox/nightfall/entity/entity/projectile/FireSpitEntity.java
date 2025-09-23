package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class FireSpitEntity extends SpitEntity {
    public FireSpitEntity(EntityType<? extends SpitEntity> pEntityType, Level level) {
        super(pEntityType, level, false);
    }

    public FireSpitEntity(Level level, LivingEntity pSpitter) {
        this(EntitiesNF.FIRE_SPIT.get(), level);
        setOwner(pSpitter);
        setPos(pSpitter.getX() - (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double) Mth.sin(pSpitter.yBodyRot * (MathUtil.PI / 180F)),
                pSpitter.getEyeY(),
                pSpitter.getZ() + (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(pSpitter.yBodyRot * (MathUtil.PI / 180F)));
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.SMOKE;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if(getOwner() instanceof LivingEntity owner) {
            Entity target = pResult.getEntity();
            target.hurt(DamageTypeSource.createIndirectSource(this, DamageType.FIRE.asArray(), owner, null).setSound(SoundsNF.PROJECTILE_FIRE_IMPACT)
                    .setProjectile(), 10.0F);
            if(target instanceof LivingEntity livingTarget) livingTarget.setSecondsOnFire(6);
        }
    }
}
