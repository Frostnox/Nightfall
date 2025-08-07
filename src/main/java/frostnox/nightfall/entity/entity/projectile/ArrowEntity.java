package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.action.Impact;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.world.OrientedEntityHitResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ArrowEntity extends AbstractArrow {
    protected IProjectileItem projectileItem;

    public ArrowEntity(EntityType<? extends ArrowEntity> entity, Level level) {
        super(entity, level);
    }

    public ArrowEntity(Level level, IProjectileItem projectileItem) {
        super(EntitiesNF.ARROW.get(), level);
        this.projectileItem = projectileItem;
    }

    public ArrowEntity(Level level, LivingEntity owner, IProjectileItem projectileItem) {
        super(EntitiesNF.ARROW.get(), owner, level);
        this.projectileItem = projectileItem;
    }

    public IProjectileItem getProjectileItem() {
        return projectileItem;
    }

    public Item getItem() {
        if(projectileItem == null) return ItemsNF.FLINT_ARROW.get();
        else return projectileItem.getItem();
    }

    public void setItem(IProjectileItem item) {
        this.projectileItem = item;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(projectileItem.getItem());
    }

    @Override
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        return LevelUtil.getHitEntity(level, this, pStartVec, pEndVec, getBoundingBox().expandTowards(getDeltaMovement()), this::canHitEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if(!level.isClientSide) {
            float damage = (float) getBaseDamage();
            DamageType[] damageType = projectileItem.getProjectileDamageType();
            Entity target = pResult.getEntity();
            Entity owner = getOwner();
            Vec3 pos = pResult.getLocation().subtract(target.position());
            Vec3 knockbackVec = pResult.getLocation().subtract(position());
            DamageTypeSource damageSource = DamageTypeSource.createProjectileSource(this, damageType,
                    owner == null ? this : owner, new HitData(target, (float) pos.x, (float) pos.y, (float) pos.z,
                            new Vec3f((float) knockbackVec.x, (float) knockbackVec.y, (float) knockbackVec.z).normalize(),
                            ((OrientedEntityHitResult) pResult).boxIndex));
            damageSource.setImpactSoundType(damageType[0].getImpactSoundType(), target);
            double speed = getDeltaMovement().length();
            damageSource.setImpact(speed > 2 ? Impact.HIGH : (speed > 1 ? Impact.MEDIUM : Impact.LOW));
            damageSource.setStun(Math.round(Math.min(CombatUtil.STUN_SHORT * (float) speed / 1.5F, CombatUtil.STUN_SHORT * 2)));
            if(target.hurt(damageSource, Math.min(damage * (float) speed / 1.5F, damage * 2))) {
                if(target instanceof LivingEntity livingTarget) {
                    doPostHurtEffects(livingTarget);
                    livingTarget.setArrowCount(livingTarget.getArrowCount() + 1);
                }
                setDeltaMovement(getDeltaMovement().multiply(0.3D, 0.3D, 0.3D));
            }
            discard();
        }
    }

    @Override
    protected void tickDespawn() {
        life++;
        if(life >= 20 * 60 * 2) discard();
    }

    @Override
    public void tick() {
        super.tick();
        if(tickCount == 1) hasImpulse = true; //Entity gets desynced immediately most of the time when thrown, hasImpulse forces a sync
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(pCompound.getString("item")));
        if(item instanceof IProjectileItem) projectileItem = (IProjectileItem) item;
        else projectileItem = ItemsNF.FLINT_ARROW.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("item", projectileItem.getItem().getRegistryName().toString());
    }
}
