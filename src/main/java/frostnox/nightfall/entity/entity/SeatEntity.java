package frostnox.nightfall.entity.entity;

import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.registry.forge.EntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {
    public SeatEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noPhysics = true;
    }

    public SeatEntity(Level pLevel, double pX, double pY, double pZ, float yRot) {
        this(EntitiesNF.SEAT.get(), pLevel);
        setPos(pX, pY, pZ);
        xo = pX;
        yo = pY;
        zo = pZ;
        setYRot(yRot);
    }

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

    @Override
    public void tick() {
        super.tick();
        if(!level.isClientSide && getPassengers().isEmpty()) remove(RemovalReason.DISCARDED);
    }

    @Override
    public void positionRider(Entity pPassenger) {
        super.positionRider(pPassenger);
        clampRotation(pPassenger);
    }

    @Override
    public void onPassengerTurned(Entity pEntityToUpdate) {
        clampRotation(pEntityToUpdate);
    }

    @Override
    protected void addPassenger(Entity pPassenger) {
        super.addPassenger(pPassenger);
        pPassenger.setYRot(getYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pPassenger) {
        BlockPos.MutableBlockPos pos = blockPosition().mutable();
        Direction facing = Direction.fromYRot(getYRot());
        for(Direction dir : new Direction[]{facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()}) {
            Vec3 loc = DismountHelper.findSafeDismountLocation(pPassenger.getType(), level, pos.setWithOffset(blockPosition(), dir), false);
            if(loc != null) return loc;
        }
        return new Vec3(getX(), getBlockY() + 0.2, getZ());
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
