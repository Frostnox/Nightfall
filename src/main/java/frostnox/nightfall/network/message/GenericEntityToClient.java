package frostnox.nightfall.network.message;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.EntityLightEngine;
import frostnox.nightfall.entity.IChaser;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.entity.entity.monster.UndeadEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class GenericEntityToClient {
    private NetworkHandler.Type messageType;
    private int entityID;
    private boolean isValid;

    public GenericEntityToClient(NetworkHandler.Type messageType, int entityID) {
        this.messageType = messageType;
        this.entityID = entityID;
        isValid = true;
    }

    private GenericEntityToClient() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeEnum(messageType);
            b.writeInt(entityID);
        }
    }

    public static GenericEntityToClient read(FriendlyByteBuf b) {
        GenericEntityToClient msg = new GenericEntityToClient();
        msg.messageType = b.readEnum(NetworkHandler.Type.class);
        msg.entityID = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(GenericEntityToClient msg, Supplier<NetworkEvent.Context> supplier) {
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
            Nightfall.LOGGER.warn("GenericMessageToClient with " + msg.messageType + " received on server.");
        }
    }

    private static void doClientWork(GenericEntityToClient msg, Level world) {
        Entity baseEntity = world.getEntity(msg.entityID);
        if(msg.messageType == NetworkHandler.Type.MOVING_BLOCK_SET_AIR_CLIENT) {
            if(baseEntity instanceof MovingBlockEntity entity) entity.setBlockState(Blocks.AIR.defaultBlockState());
            else Nightfall.LOGGER.error(GenericEntityToClient.class.getName() + " message " + msg.messageType + " cannot be applied to non MovingBlockEntity");
            return;
        }
        else if(msg.messageType == NetworkHandler.Type.MOVING_BLOCK_STOP_PHYSICS_CLIENT) {
            if(baseEntity instanceof MovingBlockEntity entity) {
                entity.noPhysics = true;
                entity.queueDiscard();
            }
            else Nightfall.LOGGER.error(GenericEntityToClient.class.getName() + " message " + msg.messageType + " cannot be applied to non MovingBlockEntity");
            return;
        }
        else if(msg.messageType == NetworkHandler.Type.CHASER_ACQUIRE_TARGET_CLIENT || msg.messageType == NetworkHandler.Type.CHASER_REMOVE_TARGET_CLIENT) {
            if(baseEntity instanceof IChaser chaser) {
                chaser.setChasing(msg.messageType == NetworkHandler.Type.CHASER_ACQUIRE_TARGET_CLIENT);
            }
            else Nightfall.LOGGER.error(GenericEntityToClient.class.getName() + " message " + msg.messageType + " cannot be applied to non IChaser entity");
            return;
        }
        if(baseEntity == null) {
            Nightfall.LOGGER.warn(GenericEntityToClient.class.getName() + " Entity is null.");
            return;
        }
        if(msg.messageType == NetworkHandler.Type.ADD_LIGHT_SOURCE_CLIENT) {
            EntityLightEngine.get().addLightSource(baseEntity);
            return;
        }
        else if(msg.messageType == NetworkHandler.Type.REMOVE_LIGHT_SOURCE_CLIENT) {
            EntityLightEngine.get().removeLightSource(baseEntity);
            return;
        }
        if(!(baseEntity instanceof LivingEntity entity)) {
            Nightfall.LOGGER.warn(GenericEntityToClient.class.getName() + " Entity is invalid type.");
            return;
        }
        if(msg.messageType == NetworkHandler.Type.RESURRECT_UNDEAD_CLIENT) {
            if(entity instanceof UndeadEntity undead) undead.resurrect();
            return;
        }
        if(!entity.isAlive()) {
            Nightfall.LOGGER.warn(GenericEntityToClient.class.getName() + " Entity is dead.");
            return;
        }
        if(entity instanceof Player player) {
            IPlayerData capP = PlayerData.get(player);
            IActionTracker capA = ActionTracker.get(player);
            switch(msg.messageType) {
                case BLOCK_CLIENT -> capP.setLastBlockTick(player.tickCount);
                case ACTIVATE_MAINHAND -> capP.setMainhandActive();
                case ACTIVATE_OFFHAND -> capP.setOffhandActive();
                case START_CRAWLING -> capP.setCrawling(true);
                case STOP_CRAWLING -> capP.setCrawling(false);
                case START_CLIMBING -> capP.setClimbing(true);
                case STOP_CLIMBING -> capP.setClimbing(false);
                case STOP_HOLDING_CLIENT -> {
                    capA.startAction(ActionsNF.EMPTY.getId());
                    capP.setHeldContents(new CompoundTag());
                }
                case QUEUE_ACTION_TRACKER -> capA.queue();
                case HITSTOP_CLIENT -> {
                    if(capA.isDamaging()) capP.setHitStopFrame(capA.getAction().getBlockHitFrame(capA.getState(), player));
                }
                case HIT_PAUSE_CLIENT -> capA.setHitPause(ClientEngine.get().getPartialTick()); //Can't use a consistent partial time here, so the current partial is better even though it can still be choppy
                case OPEN_ATTRIBUTE_SELECTION_SCREEN_CLIENT -> ClientEngine.get().openAttributeSelectionScreen();
                case PUZZLE_EXPERIMENT_FAIL_CLIENT -> ClientEngine.get().playExperimentSound(false);
                case PUZZLE_EXPERIMENT_SUCCESS_CLIENT -> ClientEngine.get().playExperimentSound(true);
                default -> Nightfall.LOGGER.warn("No player handler in " + GenericEntityToClient.class.getSimpleName() + " for message " + msg.messageType);
            }
        }
        else {
            IActionTracker capA = ActionTracker.get(entity);
            switch(msg.messageType) {
                case QUEUE_ACTION_TRACKER -> capA.queue();
                default -> Nightfall.LOGGER.warn("No entity handler in " + GenericEntityToClient.class.getSimpleName() + " for message " + msg.messageType);
            }
        }
    }
}
