package frostnox.nightfall.entity.entity;

import frostnox.nightfall.block.Tree;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.entity.IEntityWithItem;
import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

public class BoatEntity extends Boat implements IEntityWithItem {
    private static final EntityDataAccessor<String> MATERIAL = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.STRING);

    public BoatEntity(EntityType<? extends BoatEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public BoatEntity(Level pLevel, double pX, double pY, double pZ) {
        this(EntitiesNF.BOAT.get(), pLevel);
        setPos(pX, pY, pZ);
        xo = pX;
        yo = pY;
        zo = pZ;
    }

    private void setMaterial(String name) {
        if(name.contains(":")) entityData.set(MATERIAL, name);
    }

    /**
     * @param id registry ID of type from item used to create this
     */
    public void setMaterial(ResourceLocation id) {
        entityData.set(MATERIAL, id.toString());
    }

    public String getMaterial() {
        return entityData.get(MATERIAL);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MATERIAL, ItemsNF.PLANKS.get(Tree.OAK).getId().toString());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("Material", getMaterial());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setMaterial(pCompound.getString("Material"));
    }

    @Override
    public Item getDropItem() {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial()));
    }

    @Override
    public Item getItemForm() {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial().replace("plank", "boat")));
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial())));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(isInvulnerableTo(pSource)) return false;
        else if(!level.isClientSide && !isRemoved()) {
            setHurtDir(-getHurtDir());
            setHurtTime(10);
            setDamage(getDamage() + pAmount * 10.0F);
            markHurt();
            gameEvent(GameEvent.ENTITY_DAMAGED, pSource.getEntity());
            boolean canDrop = pSource.getEntity() instanceof Player && ((Player)pSource.getEntity()).getAbilities().instabuild;
            if(canDrop || getDamage() > 40.0F) {
                if(!canDrop && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    spawnAtLocation(new ItemStack(getDropItem(), 16));
                }
                discard();
            }
            return true;
        }
        else return true;
    }

    @Override
    public float getGroundFriction() {
        return Math.min(0.8F, super.getGroundFriction());
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        lastYd = getDeltaMovement().y;
        if(!isPassenger()) {
            if(pOnGround) {
                if(fallDistance > 3.0F) {
                    causeFallDamage(fallDistance, 1.0F, DamageSource.FALL);
                    if(!level.isClientSide && fallDistance >= 10F && !isRemoved()) {
                        kill();
                        if(level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            spawnAtLocation(new ItemStack(getDropItem(), 16));
                        }
                    }
                }
                resetFallDistance();
            }
            else if(!level.getFluidState(blockPosition().below()).is(FluidTags.WATER) && pY < 0.0D) fallDistance -= (float) pY;
        }
    }

    @Override
    protected void controlBoat() {
        if(getControllingPassenger() instanceof Player player && player.isAlive()) {
            if(player.isUsingItem() || !ActionTracker.get(player).isInactive()) {
                setPaddleState(false, false);
                return;
            }
        }
        super.controlBoat();
    }

    @Override
    protected void clampRotation(Entity rider) {
        //Only clamp body rotation when not acting
        if(!(rider.isAlive() && (rider instanceof Player || rider instanceof ActionableEntity) && !ActionTracker.get(rider).isInactive())) {
            rider.setYBodyRot(getYRot());
        }
        float rot = Mth.wrapDegrees(rider.getYRot() - getYRot());
        float maxRot = Mth.clamp(rot, -105.0F, 105.0F);
        rider.yRotO += maxRot - rot;
        rider.setYRot(rider.getYRot() + maxRot - rot);
        rider.setYHeadRot(rider.getYRot());
    }
}
