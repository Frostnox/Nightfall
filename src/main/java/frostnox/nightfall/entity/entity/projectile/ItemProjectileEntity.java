package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.world.OrientedEntityHitResult;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ItemProjectileEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    protected float baseDamage, knockback;

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> pEntityType, double x, double pY, double z, Level level) {
        this(pEntityType, level);
        setPos(x, pY, z);
    }

    protected ItemProjectileEntity(EntityType<? extends ItemProjectileEntity> pEntityType, LivingEntity pShooter, Level level) {
        this(pEntityType, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ(), level);
        setOwner(pShooter);
    }

    public abstract DamageType[] getDamageTypes();

    protected abstract Item getDefaultItem();

    protected double getGravity() {
        return 0.04;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(float baseDamage) {
        this.baseDamage = Math.max(0F, baseDamage);
    }

    public void setItem(ItemStack item) {
        getEntityData().set(ITEM, Util.make(item.copy(), (stack) -> stack.setCount(1)));
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = getEntityData().get(ITEM);
        return item.isEmpty() ? new ItemStack(getDefaultItem()) : item;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(!level.isClientSide) {
            Entity target = pResult.getEntity(), owner = getOwner();
            Vec3 pos = pResult.getLocation().subtract(target.position());
            Vec3 knockbackVec = pResult.getLocation().subtract(position());
            DamageType[] damageTypes = getDamageTypes();
            DamageTypeSource damageSource = DamageTypeSource.createProjectileSource(this, damageTypes,
                    owner == null ? this : owner, new HitData(target, (float) pos.x, (float) pos.y, (float) pos.z,
                            new Vec3f((float) knockbackVec.x, (float) knockbackVec.y, (float) knockbackVec.z).normalize().scale(knockback),
                            ((OrientedEntityHitResult) pResult).boxIndex));
            damageSource.setImpactSoundType(damageTypes[0].getImpactSoundType(), target);
            damageSource.setStun(Math.round(Math.min(CombatUtil.STUN_MEDIUM * (float) getDeltaMovement().length() / 1.5F, CombatUtil.STUN_MEDIUM * 2)));
            target.hurt(damageSource, Math.min(baseDamage * (float) getDeltaMovement().length() / 1.5F, baseDamage * 2));
        }
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hitResult = LevelUtil.getHitResult(this, this::canHitEntity);
        if(hitResult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitResult)) {
            onHit(hitResult);
        }
        checkInsideBlocks();
        Vec3 delta = getDeltaMovement();
        double x = getX() + delta.x;
        double y = getY() + delta.y;
        double z = getZ() + delta.z;
        updateRotation();
        float speed;
        if(isInWater()) {
            for(int i = 0; i < 4; i++) {
                level.addParticle(ParticleTypes.BUBBLE, x - delta.x * 0.25D, y - delta.y * 0.25D, z - delta.z * 0.25D, delta.x, delta.y, delta.z);
            }
            speed = 0.8F;
        }
        else speed = 0.99F;
        setDeltaMovement(delta.scale(speed));
        if(!isNoGravity()) {
            delta = getDeltaMovement();
            setDeltaMovement(delta.x, delta.y - getGravity(), delta.z);
        }
        setPos(x, y, z);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double size = getBoundingBox().getSize() * 4.0D;
        if(Double.isNaN(size)) size = 4.0D;
        size *= 64.0D;
        return pDistance < size * size;
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        ItemStack item = getItem();
        if(!item.is(getDefaultItem()) || item.hasTag()) pCompound.put("item", item.save(new CompoundTag()));
        if(baseDamage > 0F) pCompound.putFloat("baseDamage", baseDamage);
        if(knockback > 0F) pCompound.putFloat("knockback", knockback);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if(pCompound.contains("item")) setItem(ItemStack.of(pCompound.getCompound("item")));
        baseDamage = pCompound.getFloat("baseDamage");
        knockback = pCompound.getFloat("knockback");
    }
}
