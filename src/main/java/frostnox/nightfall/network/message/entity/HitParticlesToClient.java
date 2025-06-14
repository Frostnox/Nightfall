package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class HitParticlesToClient {
    private int entityID;
    private float damage, x, y, z, xDir, zDir;
    private boolean isValid;

    public HitParticlesToClient(int entityID, float damage, float x, float y, float z, float xDir, float zDir) {
        this.damage = damage;
        this.entityID = entityID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xDir = xDir;
        this.zDir = zDir;
        isValid = true;
    }

    private HitParticlesToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeFloat(damage);
            b.writeFloat(x);
            b.writeFloat(y);
            b.writeFloat(z);
            b.writeFloat(xDir);
            b.writeFloat(zDir);
        }
    }

    public static HitParticlesToClient read(FriendlyByteBuf b) {
        HitParticlesToClient msg = new HitParticlesToClient();
        msg.entityID = b.readInt();
        msg.damage = b.readFloat();
        msg.x = b.readFloat();
        msg.y = b.readFloat();
        msg.z = b.readFloat();
        msg.xDir = b.readFloat();
        msg.zDir = b.readFloat();
        msg.isValid = true;
        return msg;
    }

    public static void handle(HitParticlesToClient msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Optional<Level> world = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
            if(!world.isPresent()) {
                Nightfall.LOGGER.warn("ClientLevel could not be found.");
                return;
            }
            ctx.enqueueWork(() -> doClientWork(msg, world.get()));
        }
        else if(sideReceived.isServer()) {
            Nightfall.LOGGER.warn("HitParticlesToClient received on server.");
        }
    }

    private static void doClientWork(HitParticlesToClient msg, Level world) {
        if(!(world.getEntity(msg.entityID) instanceof LivingEntity entity)) {
            Nightfall.LOGGER.warn("Entity is invalid.");
            return;
        }
        if(!entity.isAlive()) {
            Nightfall.LOGGER.warn("Entity is null or dead.");
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        double x = entity.getX() + motion.x + msg.x, y = entity.getY() + motion.y + msg.y, z = entity.getZ() + motion.z + msg.z;
        boolean noFluid = world.getBlockState(new BlockPos(x, y, z)).getFluidState().isEmpty();
        ParticleOptions particle;
        if(entity instanceof ActionableEntity actEntity) particle = actEntity.getHurtParticle();
        else particle = ParticleTypesNF.BLOOD_RED.get();
        if(particle != null) {
            for(int i = 0; i < Math.min(Math.ceil(msg.damage / 5F), 20); i++) {
                double yVelocity = noFluid ? world.random.nextDouble() + 0.2 : 0D;
                world.addParticle(particle, x, y, z,
                        (msg.xDir + (world.random.nextFloat() - 0.5) * 0.4) * 50D * world.random.nextDouble(),
                        yVelocity * 20D,
                        (msg.zDir + (world.random.nextFloat() - 0.5) * 0.4) * 50D * world.random.nextDouble());
            }
        }
        /*if(msg.stun) {
            for(int i = 0; i < 1; i++) {
                world.addParticle(ParticleTypes.CRIT,
                        entity.getX() + motion.x + 0.5D * (double) entity.getBbWidth(),
                        entity.getY() + motion.y + entity.getEyeHeight(),
                        entity.getZ() + motion.z + 0.5D * (double) entity.getBbWidth(),
                        (world.random.nextFloat() - 0.5F) * 1D,
                        (world.random.nextFloat() - 0.5F) * 1D,
                        (world.random.nextFloat() - 0.5F) * 1D);
            }
        }*/
    }
}
