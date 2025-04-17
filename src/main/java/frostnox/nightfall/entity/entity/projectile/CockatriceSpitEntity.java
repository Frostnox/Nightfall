package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CockatriceSpitEntity extends Projectile {
    public CockatriceSpitEntity(EntityType<? extends CockatriceSpitEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    public CockatriceSpitEntity(Level level, LivingEntity pSpitter) {
        this(EntitiesNF.COCKATRICE_SPIT.get(), level);
        setOwner(pSpitter);
        setPos(pSpitter.getX() - (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double) Mth.sin(pSpitter.yBodyRot * (MathUtil.PI / 180F)),
                pSpitter.getEyeY(),
                pSpitter.getZ() + (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(pSpitter.yBodyRot * (MathUtil.PI / 180F)));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 velocity = getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if(hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            onHit(hitresult);
            discard();
        }
        double x = getX() + velocity.x;
        double y = getY() + velocity.y;
        double z = getZ() + velocity.z;
        updateRotation();
        if(level.getBlockStates(getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir) || isInWaterOrBubble()) {
            discard();
        }
        else {
            setDeltaMovement(velocity.scale(0.99F));
            if(!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0.0D, -0.06F, 0.0D));
            setPos(x, y, z);
            velocity = getDeltaMovement();
            double dX = velocity.x, dY = velocity.y, dZ = velocity.z;
            for(int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypesNF.COCKATRICE_SPIT.get(), this.getX() + dX * (double)i / 4.0D,
                        this.getY() + dY * (double)i / 4.0D, this.getZ() + dZ * (double)i / 4.0D,
                        -dX, -dY + 0.2D, -dZ);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if(getOwner() instanceof LivingEntity owner) {
            Entity target = pResult.getEntity();
            target.hurt(new DamageTypeSource("projectile.spit", this, owner, DamageType.ABSOLUTE).setSound(SoundsNF.PROJECTILE_POISON_IMPACT)
                    .setProjectile(), 5.0F);
            if(target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(EffectsNF.POISON.get(), 60 * 20, 0));
            }
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        double x = pPacket.getXa();
        double y = pPacket.getYa();
        double z = pPacket.getZa();
        setDeltaMovement(x, y, z);
    }
}
