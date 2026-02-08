package frostnox.nightfall.network;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.network.message.blockentity.*;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.GenericToServer;
import frostnox.nightfall.network.message.capability.*;
import frostnox.nightfall.network.message.entity.*;
import frostnox.nightfall.network.message.world.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;
import java.util.Optional;

public class NetworkHandler {
    public static final String VERSION = "1.0";
    private static int ID = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "networkchannel"),
            () -> VERSION,
            NetworkHandler::isAcceptedByClient,
            NetworkHandler::isAcceptedByServer
    );

    public enum Type {
        //Client
        BLOCK_CLIENT,
        HITSTOP_CLIENT,
        HIT_PAUSE_CLIENT,
        STOP_HOLDING_CLIENT,
        RESURRECT_UNDEAD_CLIENT,
        MOVING_BLOCK_SET_AIR_CLIENT,
        MOVING_BLOCK_STOP_PHYSICS_CLIENT,
        ADD_LIGHT_SOURCE_CLIENT,
        REMOVE_LIGHT_SOURCE_CLIENT,
        OPEN_ATTRIBUTE_SELECTION_SCREEN_CLIENT,
        PUZZLE_EXPERIMENT_FAIL_CLIENT,
        PUZZLE_EXPERIMENT_SUCCESS_CLIENT,
        CHASER_ACQUIRE_TARGET_CLIENT,
        CHASER_REMOVE_TARGET_CLIENT,
        //Server
        PUZZLE_EXPERIMENT_SERVER,
        CLOSE_RECIPE_SEARCH_SERVER,
        //Common
        ACTIVATE_MAINHAND,
        ACTIVATE_OFFHAND,
        START_CRAWLING,
        STOP_CRAWLING,
        START_CLIMBING,
        STOP_CLIMBING,
        QUEUE_ACTION_TRACKER
    }

    public static void register() {
        INSTANCE.registerMessage(ID++, ActionTrackerToClient.class, ActionTrackerToClient::write, ActionTrackerToClient::read, ActionTrackerToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, PlayerDataToClient.class, PlayerDataToClient::write, PlayerDataToClient::read, PlayerDataToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, LevelDataToClient.class, LevelDataToClient::write, LevelDataToClient::read, LevelDataToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, GenericEntityToClient.class, GenericEntityToClient::write, GenericEntityToClient::read, GenericEntityToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, GenericToServer.class, GenericToServer::write, GenericToServer::read, GenericToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, StaminaChangedToClient.class, StaminaChangedToClient::write, StaminaChangedToClient::read, StaminaChangedToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ShiveringToClient.class, ShiveringToClient::write, ShiveringToClient::read, ShiveringToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, StatusToClient.class, StatusToClient::write, StatusToClient::read, StatusToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, DamageFailToClient.class, DamageFailToClient::write, DamageFailToClient::read, DamageFailToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, HeadYRotToClient.class, HeadYRotToClient::write, HeadYRotToClient::read, HeadYRotToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, HurtDirToClient.class, HurtDirToClient::write, HurtDirToClient::read, HurtDirToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, HitTargetToServer.class, HitTargetToServer::write, HitTargetToServer::read, HitTargetToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, ActionToServer.class, ActionToServer::write, ActionToServer::read, ActionToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, DodgeToClient.class, DodgeToClient::write, DodgeToClient::read, DodgeToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, DodgeToServer.class, DodgeToServer::write, DodgeToServer::read, DodgeToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, ThrownWeaponToClient.class, ThrownWeaponToClient::write, ThrownWeaponToClient::read, ThrownWeaponToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ArrowItemToClient.class, ArrowItemToClient::write, ArrowItemToClient::read, ArrowItemToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, EatItemToClient.class, EatItemToClient::write, EatItemToClient::read, EatItemToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, HitParticlesToClient.class, HitParticlesToClient::write, HitParticlesToClient::read, HitParticlesToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ActionToClient.class, ActionToClient::write, ActionToClient::read, ActionToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, UpdateBlockToClient.class, UpdateBlockToClient::write, UpdateBlockToClient::read, UpdateBlockToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ClimbPositionToClient.class, ClimbPositionToClient::write, ClimbPositionToClient::read, ClimbPositionToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ClimbPositionToServer.class, ClimbPositionToServer::write, ClimbPositionToServer::read, ClimbPositionToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, DestroyBlockNoSoundToClient.class, DestroyBlockNoSoundToClient::write, DestroyBlockNoSoundToClient::read, DestroyBlockNoSoundToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ChunkDigProgressToClient.class, ChunkDigProgressToClient::write, ChunkDigProgressToClient::read, ChunkDigProgressToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ChunkDigProgressToServer.class, ChunkDigProgressToServer::write, ChunkDigProgressToServer::read, ChunkDigProgressToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, DigBlockToClient.class, DigBlockToClient::write, DigBlockToClient::read, DigBlockToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, DigBlockToServer.class, DigBlockToServer::write, DigBlockToServer::read, DigBlockToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, BlockEntityFluidToClient.class, BlockEntityFluidToClient::write, BlockEntityFluidToClient::read, BlockEntityFluidToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, AnvilActionToServer.class, AnvilActionToServer::write, AnvilActionToServer::read, AnvilActionToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, GridUseToServer.class, GridUseToServer::write, GridUseToServer::read, GridUseToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, SetAllyToClient.class, SetAllyToClient::write, SetAllyToClient::read, SetAllyToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, MovingBlockToClient.class, MovingBlockToClient::write, MovingBlockToClient::read, MovingBlockToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, EncyclopediaEntryToClient.class, EncyclopediaEntryToClient::write, EncyclopediaEntryToClient::read, EncyclopediaEntryToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, EncyclopediaKnowledgeToClient.class, EncyclopediaKnowledgeToClient::write, EncyclopediaKnowledgeToClient::read, EncyclopediaKnowledgeToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, EncyclopediaToClient.class, EncyclopediaToClient::write, EncyclopediaToClient::read, EncyclopediaToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, TakeHoldableToClient.class, TakeHoldableToClient::write, TakeHoldableToClient::read, TakeHoldableToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ChunkClimateToClient.class, ChunkClimateToClient::write, ChunkClimateToClient::read, ChunkClimateToClient::handle, Optional.of(PLAY_TO_CLIENT));
        INSTANCE.registerMessage(ID++, ChunkClimateToServer.class, ChunkClimateToServer::write, ChunkClimateToServer::read, ChunkClimateToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, ModifiableIndexToServer.class, ModifiableIndexToServer::write, ModifiableIndexToServer::read, ModifiableIndexToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, PuzzleContainerRequestToServer.class, PuzzleContainerRequestToServer::write, PuzzleContainerRequestToServer::read, PuzzleContainerRequestToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, EntryNotificationToServer.class, EntryNotificationToServer::write, EntryNotificationToServer::read, EntryNotificationToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, AttributeSelectionToServer.class, AttributeSelectionToServer::write, AttributeSelectionToServer::read, AttributeSelectionToServer::handle, Optional.of(PLAY_TO_SERVER));
        INSTANCE.registerMessage(ID++, SetAccessoriesToClient.class, SetAccessoriesToClient::write, SetAccessoriesToClient::read, SetAccessoriesToClient::handle, Optional.of(PLAY_TO_CLIENT));
    }

    public static void toServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void toClient(ServerPlayer p, Object msg) {
        INSTANCE.sendTo(msg, p.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void toAllTracking(Entity entity, Object msg) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    public static void toAllTrackingAndSelf(Entity entity, Object msg) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    public static void toAllTrackingChunk(LevelChunk chunk, Object msg) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

    public static boolean isAcceptedByClient(String version) {
        return VERSION.equals(version);
    }

    public static boolean isAcceptedByServer(String version) {
        return VERSION.equals(version);
    }
}
