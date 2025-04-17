package frostnox.nightfall.entity.entity.projectile;

import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.action.player.action.thrown.ThrowTechnique;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.world.OrientedEntityHitResult;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ThrownWeaponEntity extends AbstractArrow {
    protected ItemStack item = ItemStack.EMPTY;
    protected ResourceLocation actionID = ActionsNF.EMPTY.getId();
    protected boolean dealtDamage = false;
    public boolean spinning;
    protected float damage, curve;
    protected DamageType[] damageType;
    protected int airTicks;

    public ThrownWeaponEntity(EntityType<? extends ThrownWeaponEntity> entity, Level level) {
        super(entity, level);
    }

    public ThrownWeaponEntity(Level level, ItemStack item, ResourceLocation actionID) {
        super(EntitiesNF.THROWN_WEAPON.get(), level);
        setItem(item);
        setAction(actionID, null);
    }

    public ThrownWeaponEntity(Level level, LivingEntity owner, ItemStack item, ResourceLocation actionID) {
        super(EntitiesNF.THROWN_WEAPON.get(), owner, level);
        setItem(item);
        setAction(actionID, owner);
    }

    public void setItem(ItemStack item) {
        this.item = item.copy();
        if(item.getItem() instanceof IWeaponItem weapon) damage = weapon.getBaseDamage();
        else damage = 20F;
    }

    public void setAction(ResourceLocation actionID, @Nullable LivingEntity owner) {
        this.actionID = actionID;
        if(ActionsNF.get(actionID) instanceof ThrowTechnique throwTechnique) {
            curve = throwTechnique.curve;
            damageType = throwTechnique.getDamageTypes(owner);
            spinning = throwTechnique.spinning;
        }
        else {
            curve = 0;
            damageType = DamageType.PIERCING.asArray();
            spinning = false;
        }
    }

    public void damageItem(int amount) {
        if(level instanceof ServerLevel serverLevel && item.hurt(amount, random, null)) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getPickupItem()), getX(), getY(), getZ(), 10, ((double)random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0D, 0D);
            discard();
        }
    }

    public boolean inGround() {
        return inGround;
    }

    public float getDamage() {
        return damage;
    }

    public float getCurve() {
        return curve;
    }

    public DamageType[] getDamageTypes() {
        return damageType;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public ResourceLocation getActionID() {
        return actionID;
    }

    @Override
    public void tick() {
        super.tick();
        if(inGroundTime > 4) dealtDamage = false;
        if(tickCount == 1) hasImpulse = true; //Entity gets desynced immediately most of the time when thrown, hasImpulse forces a sync
        if(!inGround) airTicks++;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if(!dealtDamage) damageItem(4 + random.nextInt(4));
        setSoundEvent(null); //Stop super from playing sound
        super.onHitBlock(result);
        if(getPickupItem().getDamageValue() >= getPickupItem().getMaxDamage()) {
            setSoundEvent(SoundEvents.ITEM_BREAK);
            playSound(getHitGroundSoundEvent(), 1.0F, 0.9F + 0.2F * random.nextFloat());
        }
        else {
            setSoundEvent(level.getBlockState(result.getBlockPos()).getSoundType().getHitSound());
            playSound(getHitGroundSoundEvent(), 1.0F, 0.5F + 0.2F * random.nextFloat());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity target = pResult.getEntity();
        Entity owner = getOwner();
        Vec3 pos = pResult.getLocation().subtract(target.position());
        Vec3 knockbackVec = pResult.getLocation().subtract(position());
        damageItem(4 + random.nextInt(4));
        DamageTypeSource damageSource = DamageTypeSource.createProjectileAttackSource(this, damageType, (Attack) ActionsNF.get(actionID),
                owner == null ? this : owner, new HitData(target, (float) pos.x, (float) pos.y, (float) pos.z,
                        new Vec3f((float) knockbackVec.x, (float) knockbackVec.y, (float) knockbackVec.z).normalize(),
                        ((OrientedEntityHitResult) pResult).boxIndex));
        if(getPickupItem().getDamageValue() >= getPickupItem().getMaxDamage()) damageSource.setSound(() -> SoundEvents.ITEM_BREAK);
        else damageSource.setImpactSoundType(damageType[0].getImpactSoundType(), target);
        dealtDamage = true;
        if(target.hurt(damageSource, Math.min(damage * (float) getDeltaMovement().length(), damage * 2)) && target instanceof LivingEntity livingTarget) doPostHurtEffects(livingTarget);
        setDeltaMovement(getDeltaMovement().multiply(0.3D, 0.3D, 0.3D));
    }

    @Override
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        return dealtDamage ? null : LevelUtil.getHitEntity(level, this, pStartVec, pEndVec, getBoundingBox().expandTowards(getDeltaMovement()), this::canHitEntity);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || isNoPhysics() && player.getInventory().add(getPickupItem());
    }

    @Override
    public void playerTouch(Player pEntity) {
        super.playerTouch(pEntity);
    }

    @Override
    protected void tickDespawn() {
        life++;
        if(life >= 20 * 60 * 3) discard();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if(pCompound.contains("item", 10)) setItem(ItemStack.of(pCompound.getCompound("item")));
        if(pCompound.contains("actionID", Tag.TAG_STRING)) setAction(ResourceLocation.parse(pCompound.getString("actionID")), (LivingEntity) getOwner());
        dealtDamage = pCompound.getBoolean("dealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("item", item.save(new CompoundTag()));
        if(actionID != null) pCompound.putString("actionID", actionID.toString());
        pCompound.putBoolean("dealtDamage", dealtDamage);
    }

    @Override
    public ItemStack getPickupItem() {
        return item.copy();
    }

    @Override
    public boolean shouldRender(double x, double pY, double z) {
        return true;
    }
}
