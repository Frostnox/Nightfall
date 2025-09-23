package frostnox.nightfall.entity.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class SpitEntity extends Projectile {
    protected final boolean doParticleSpeed;

    protected SpitEntity(EntityType<? extends SpitEntity> pEntityType, Level level, boolean doParticleSpeed) {
        super(pEntityType, level);
        this.doParticleSpeed = doParticleSpeed;
    }

    protected abstract ParticleOptions getParticle();

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
            if(doParticleSpeed) for(int i = 0; i < 3; i++) {
                level.addParticle(getParticle(), this.getX() + dX * (double)i / 4.0D,
                        this.getY() + dY * (double)i / 4.0D, this.getZ() + dZ * (double)i / 4.0D,
                        -dX, -dY + 0.2D, -dZ);
            }
            else for(int i = 0; i < 3; i++) {
                level.addParticle(getParticle(), this.getX() + dX * (double)i / 4.0D,
                        this.getY() + dY * (double)i / 4.0D, this.getZ() + dZ * (double)i / 4.0D,
                        0, 0, 0);
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
