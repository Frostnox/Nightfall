package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownRockEntity extends ItemProjectileEntity {
    public ThrownRockEntity(EntityType<? extends ThrownRockEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    public ThrownRockEntity(Level level, double x, double pY, double z) {
        super(EntitiesNF.THROWN_ROCK.get(), x, pY, z, level);
    }

    public ThrownRockEntity(Level level, LivingEntity pShooter) {
        super(EntitiesNF.THROWN_ROCK.get(), pShooter, level);
    }

    @Override
    public DamageType[] getDamageTypes() {
        return new DamageType[] {DamageType.STRIKING};
    }

    @Override
    protected Item getDefaultItem() {
        return ItemsNF.ROCKS.get(Stone.SHALE).get();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if(pId == 3) {
            double velocity = getDeltaMovement().length() * 0.15;
            for(int i = 0; i < 6; ++i) {
                Vec3 pos = getPosition(0.3F);
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, getItem()), pos.x, pos.y, pos.z,
                        (random.nextFloat() - 0.5D) * velocity,
                        (random.nextFloat() - 0.5D) * velocity,
                        (random.nextFloat() - 0.5D) * velocity);
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if(!level.isClientSide) {
            ItemStack item = getItem();
            Vec3 velocity = getDeltaMovement();
            Vec3 hitPos = hitResult.getLocation();
            float mag = (float) velocity.length();
            float breakChance = mag < 1F ? 0 : (mag / 3.1F);
            if(item.is(TagsNF.METAMORPHIC)) breakChance /= 3F;
            else if(item.is(TagsNF.IGNEOUS)) breakChance /= 5F;
            if(breakChance >= 1F || (breakChance > 0F && level.random.nextFloat() < breakChance)) {
                level.broadcastEntityEvent(this, (byte) 3);
            }
            else {
                Vec3 pos = position();
                ItemEntity itemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, item);
                Vec3 hitVec = hitPos.subtract(pos).scale(2);
                if(!itemEntity.isInWall()) itemEntity.setDeltaMovement(velocity.add(hitVec).normalize().scale(mag * 0.3F));
                level.addFreshEntity(itemEntity);
            }
            level.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundsNF.PROJECTILE_ROCK_IMPACT.get(), SoundSource.BLOCKS,
                    1F, 1.6F + level.random.nextFloat() * 0.4F);
            discard();
        }
    }
}
