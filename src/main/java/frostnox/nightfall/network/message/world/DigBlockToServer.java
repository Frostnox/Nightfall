package frostnox.nightfall.network.message.world;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.registry.forge.AttributesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DigBlockToServer {
    private boolean isValid;
    private int x, y, z;

    public DigBlockToServer(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        isValid = true;
    }

    private DigBlockToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(x);
            b.writeInt(y);
            b.writeInt(z);
        }
    }

    public static DigBlockToServer read(FriendlyByteBuf b) {
        DigBlockToServer msg = new DigBlockToServer();
        msg.x = b.readInt();
        msg.y = b.readInt();
        msg.z = b.readInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(DigBlockToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("DigBlockToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doWork(DigBlockToServer msg, ServerPlayer player) {
        boolean failed = false;
        if(!player.isAlive()) {
            Nightfall.LOGGER.warn("Player {} tried to dig but was not alive.", player.getName().getString());
        }
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        BlockPos center = new BlockPos(msg.x, msg.y, msg.z);
        Action action = capA.getAction();
        if(player.distanceToSqr(msg.x, msg.y, msg.z) > 64) {
            Nightfall.LOGGER.warn("Player {} tried to dig a block at {} but was too far away.", player.getName().getString(), center.toShortString());
            failed = true;
        }
        else if(capP.hasDugBlock()) {
            Nightfall.LOGGER.warn("Player {} tried to dig twice during the same state.", player.getName().getString());
            failed = true;
        }
        else if(!action.canHarvest()) {
            Nightfall.LOGGER.warn("Player {} tried to dig but action '{}' does not allow harvesting.", player.getName().getString(), capA.getAction().toString());
            failed = true;
        }
        else if(!capA.isInactive()) {
            ItemStack stack = player.getItemInHand(capP.getActiveHand());
            int hits = 0;
            boolean canMineAny = !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_SICKLE) && !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_DAGGER) && !action.harvestableBlocks.equals(BlockTags.MINEABLE_WITH_AXE);
            if(canMineAny || action.canHarvest(player.level.getBlockState(center))) {
                boolean facingX = player.getDirection().getAxis().equals(Direction.Axis.X);
                int xRange = (stack.is(TagsNF.SICKLE) && (!action.isChargeable() || !facingX)) ? 1 : 0;
                int zRange = (stack.is(TagsNF.SICKLE) && (!action.isChargeable() || facingX)) ? 1 : 0;
                int yRange = (stack.is(TagsNF.SICKLE) && action.isChargeable()) ? 1 : 0;
                for(BlockPos pos : BlockPos.betweenClosed(center.getX() - xRange, center.getY() - yRange, center.getZ() - zRange, center.getX() + xRange, center.getY() + yRange, center.getZ() + zRange)) {
                    BlockState block = player.level.getBlockState(pos);
                    if(!canMineAny && !action.canHarvest(block)) continue;
                    IGlobalChunkData chunkData = GlobalChunkData.get(player.level.getChunkAt(pos));
                    float progress = block.getDestroyProgress(player, player.level, pos)
                            * AttributesNF.getStrengthMultiplier(player) * capA.getChargeDestroyProgressMultiplier() + chunkData.getBreakProgress(pos);
                    BlockEntity blockEntity = player.level.getBlockEntity(pos);
                    if(progress >= 1.0F) {
                        int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(player.level, player.gameMode.getGameModeForPlayer(), player, pos.immutable());
                        if(exp == -1) {
                            NetworkHandler.toClient(player, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
                            continue;
                        }
                    }
                    hits++;
                    boolean isCenter = pos.equals(center);
                    if(progress >= 1.0F) {
                        boolean canHarvest = block.canHarvestBlock(player.level, pos, player);
                        if(block.onDestroyedByPlayer(player.level, pos, player, canHarvest, block.getFluidState())) {
                            block.getBlock().destroy(player.level, pos, block);
                            if(canHarvest) block.getBlock().playerDestroy(player.level, player, pos, block, blockEntity, stack);
                            chunkData.removeBreakProgress(pos);
                        }
                    }
                    else {
                        if(isCenter && !block.getMaterial().isReplaceable() && !stack.is(TagsNF.NO_HITSTOP)) {
                            capP.setHitStopFrame(capA.getFrame());
                            NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.HITSTOP_CLIENT, player.getId()));
                        }
                        chunkData.setBreakProgress(pos.immutable(), progress);
                    }
                    if(isCenter) {
                        SoundType sound = block.getSoundType(player.level, pos, player);
                        player.level.playSound(player, pos, sound.getHitSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2F, sound.getPitch() * 0.75F);
                    }
                    NetworkHandler.toAllTrackingAndSelf(player, new DigBlockToClient(pos.getX(), pos.getY(), pos.getZ(), progress));
                }
                capP.setDugBlock(true);
                if(!player.isCreative()) stack.hurtAndBreak(hits, player, (p) -> p.broadcastBreakEvent(capP.getActiveHand()));
            }
        }
        else {
            Nightfall.LOGGER.warn("Player {} tried to dig but is idling.", player.getName().getString());
            failed = true;
        }
        if(failed) {
            NetworkHandler.toClient(player, new DigBlockToClient(center.getX(), center.getY(), center.getZ(), GlobalChunkData.get(player.level.getChunkAt(center)).getBreakProgress(center)));
        }
    }
}
