package frostnox.nightfall.event;

import com.google.common.hash.Hashing;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.EntityLightEngine;
import frostnox.nightfall.encyclopedia.knowledge.IItemKnowledge;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import frostnox.nightfall.entity.entity.projectile.ThrownWeaponEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.command.GodModeCommand;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.capability.ActionTrackerToClient;
import frostnox.nightfall.network.message.capability.LevelDataToClient;
import frostnox.nightfall.network.message.capability.PlayerDataToClient;
import frostnox.nightfall.network.message.capability.SetAccessoriesToClient;
import frostnox.nightfall.network.message.entity.*;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.KnowledgeNF;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.world.ContinentalWorldType;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import frostnox.nightfall.world.inventory.PlayerInventoryContainer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static frostnox.nightfall.Nightfall.MODID;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEventHandler {
    private static boolean shouldAttachLevelCapability(Level level) {
        return level.dimensionType().effectsLocation().equals(ContinentalWorldType.LOCATION);
    }

    @SubscribeEvent
    public static void onAttachLevelCapabilityEvent(AttachCapabilitiesEvent<Level> event) {
        if(shouldAttachLevelCapability(event.getObject())) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "level_data"), new LevelData.LevelDataCapability(event.getObject()));
        }
    }

    @SubscribeEvent
    public static void onAttachChunkCapabilityEvent(AttachCapabilitiesEvent<LevelChunk> event) {
        if(LevelData.isPresent(event.getObject().getLevel())) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "chunk_data"), new ChunkData.ChunkDataCapability(event.getObject()));
        }
        event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "global_chunk_data"), new GlobalChunkData.GlobalChunkDataCapability(event.getObject()));
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "player_data"), new PlayerData.PlayerDataCapability(player));
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "player_action"), new ActionTracker.ActionTrackerCapability(player));
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "player_light"), new LightData.LightDataCapability(player));
        }
        else if(event.getObject() instanceof ActionableEntity entity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "entity_action"), new ActionTracker.ActionTrackerCapability(entity));
        }
        else if(event.getObject() instanceof ItemEntity itemEntity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "entity_light"), new LightData.LightDataCapability(itemEntity));
        }
    }

    @SubscribeEvent
    public static void onLoadWorldEvent(WorldEvent.Load event) {
        if(event.getWorld() instanceof ServerLevel level && LevelData.isPresent(level)) {
            ILevelData capL = LevelData.get(level);
            capL.onLoad(Hashing.sha512().hashLong(level.getSeed()).asLong());
            capL.updateWeather();
            capL.updateWind();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        if(!event.getWorld().isClientSide()) {
            Entity entity = event.getEntity();
            LazyOptional<ILightData> capL = entity.getCapability(LightData.CAPABILITY);
            if(capL.isPresent()) {
                capL.orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional.")).setupClientNotifications();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStartingEvent(ServerStartingEvent event) {
        ServerLevel level = event.getServer().overworld();
        ServerLevelData data = (ServerLevelData) level.getLevelData();
        //Remove spawn chunk ticket after generating so spawn chunks aren't loaded forever
        level.getChunkSource().removeRegionTicket(TicketType.START,
                new ChunkPos(new BlockPos(data.getXSpawn(), 0, data.getZSpawn())), 11, Unit.INSTANCE);
        //Lock difficulty unless cheats are enabled
        if(!data.getAllowCommands() && event.getServer().isSingleplayer()) {
            event.getServer().setDifficultyLocked(true);
        }
    }

    @SubscribeEvent
    public static void onStartTrackingEvent(PlayerEvent.StartTracking event) {
        ServerPlayer tracker = (ServerPlayer) event.getPlayer();
        Entity entity = event.getTarget();
        LazyOptional<ILightData> capL = entity.getCapability(LightData.CAPABILITY);
        if(capL.isPresent() && capL.orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional.")).notifyClientOnStopTracking()) {
            NetworkHandler.toClient(tracker, new GenericEntityToClient(NetworkHandler.Type.ADD_LIGHT_SOURCE_CLIENT, entity.getId()));
        }
        if(entity instanceof Player target && target.isAlive()) {
            IActionTracker targetCapA = ActionTracker.get(target);
            IPlayerData targetCapP = PlayerData.get(target);
            targetCapP.setLastDodgeTick(-100);
            targetCapP.setLastBlockTick(-100);
            NetworkHandler.toClient(tracker, new ActionTrackerToClient(targetCapA.writeNBT(), target.getId()));
            NetworkHandler.toClient(tracker, new PlayerDataToClient(targetCapP.writeNBT(), target.getId()));
            AccessoryInventory accessories = targetCapP.getAccessoryInventory();
            List<Pair<AccessorySlot, ItemStack>> accessoriesList = new ObjectArrayList<>(0);
            for(AccessorySlot slot : AccessorySlot.values()) {
                ItemStack item = accessories.getItem(slot);
                if(!item.isEmpty()) accessoriesList.add(Pair.of(slot, item));
            }
            if(!accessoriesList.isEmpty()) NetworkHandler.toClient(tracker, new SetAccessoriesToClient(accessoriesList, target.getId()));
            //Fix vanilla bug where head y rotation isn't synced
            NetworkHandler.toClient(tracker, new HeadYRotToClient(target.getYHeadRot(), target.getId()));
        }
        else if(entity instanceof ActionableEntity target && target.isAlive()) {
            IActionTracker targetCapA = target.getActionTracker();
            NetworkHandler.toClient(tracker, new ActionTrackerToClient(targetCapA.writeNBT(), entity.getId()));
            if(entity instanceof DregEntity dreg && dreg.ally != null) {
                NetworkHandler.toClient(tracker, new SetAllyToClient(dreg.ally.getId(), dreg.getId()));
            }
        }
        else if(entity instanceof ThrownWeaponEntity target) {
            NetworkHandler.toClient(tracker, new ThrownWeaponToClient(target.getPickupItem(), target.getActionID(), target.getId()));
        }
        else if(entity instanceof ArrowEntity target) {
            NetworkHandler.toClient(tracker, new ArrowItemToClient(target.getProjectileItem(), target.getId()));
        }
        else if(entity instanceof MovingBlockEntity target) {
            if(target.slideDir != Direction.DOWN) NetworkHandler.toClient(tracker, new MovingBlockToClient(target.getSlideTime(), target.slideDir, false, target.getId()));
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorldEvent(EntityLeaveWorldEvent event) {
        Entity entity = event.getEntity();
        if(event.getWorld().isClientSide() && LightData.isPresent(entity)) {
            EntityLightEngine.get().removeLightSource(entity);
        }
    }

    @SubscribeEvent
    public static void onStopTrackingEvent(PlayerEvent.StopTracking event) {
        ServerPlayer tracker = (ServerPlayer) event.getPlayer();
        Entity entity = event.getTarget();
        if(!entity.isRemoved()) {
            LazyOptional<ILightData> capL = entity.getCapability(LightData.CAPABILITY);
            if(capL.isPresent() && capL.orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional.")).notifyClientOnStopTracking()) {
                NetworkHandler.toAllTracking(entity, new GenericEntityToClient(NetworkHandler.Type.REMOVE_LIGHT_SOURCE_CLIENT, entity.getId()));
            }
        }
    }

    private static ContainerListener createKnowledgeListener(ServerPlayer player) {
        return new ContainerListener() {
            public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack itemStack) {
                if(!itemStack.isEmpty()) {
                    Slot slot = menu.getSlot(slotIndex);
                    if(!(slot instanceof ResultSlot)) {
                        if(slot.container == player.getInventory()) {
                            for(Knowledge knowledge : RegistriesNF.getActiveServerKnowledge()) {
                                if(knowledge instanceof IItemKnowledge itemKnowledge) itemKnowledge.onPickedUpItem(player, itemStack);
                            }
                        }
                    }
                }
            }

            public void dataChanged(AbstractContainerMenu p_143462_, int p_143463_, int p_143464_) {
            }
        };
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if(LevelData.isPresent(player.level)) {
            ILevelData capL = LevelData.get(player.level);
            NetworkHandler.toClient(player, new LevelDataToClient(capL.writeNBTSync(capL.writeNBTClientInit(new CompoundTag()))));
        }
        player.inventoryMenu = new PlayerInventoryContainer(player.getInventory(), !player.level.isClientSide());
        player.containerMenu = player.inventoryMenu;
        player.initMenu(player.inventoryMenu);
        player.inventoryMenu.addSlotListener(createKnowledgeListener(player));

        player.spawnInvulnerableTime = 2;

        IPlayerData capP = PlayerData.get(player);
        IActionTracker capA = ActionTracker.get(player);
        capP.setLastDodgeTick(-100);
        capP.setLastBlockTick(-100);
        capP.setClimbTicks(0);
        capP.setCrouchTicks(0);
        capP.setClimbing(false);
        capP.setLastMainItem();
        capP.setLastOffItem();

        if(capP.hasGodMode()) player.sendMessage(GodModeCommand.ENABLE, Util.NIL_UUID);

        //First time player has joined the server
        if(capP.hasNoEntries()) {
            capP.unlockEntry(EntriesNF.TOOLS.getId());
            player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(100);
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            player.setHealth(player.getMaxHealth());
        }
        capP.refreshEncyclopedia();

        NetworkHandler.toClient(player, new ActionTrackerToClient(capA.writeNBT(), player.getId()));
        NetworkHandler.toClient(player, new PlayerDataToClient(capP.writeNBT(), player.getId()));
        if(capP.needsAttributeSelection() && !player.canUseGameMasterBlocks() && !player.isSpectator()) {
            NetworkHandler.toClient(player, new GenericEntityToClient(NetworkHandler.Type.OPEN_ATTRIBUTE_SELECTION_SCREEN_CLIENT, player.getId()));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        if(!player.isAlive()) player.reviveCaps();
        IPlayerData capP = PlayerData.get(player);
        IActionTracker capA = ActionTracker.get(player);
        if(capP.dropBlockEntity()) capA.startAction(ActionsNF.EMPTY.getId());
        if(player.isPassenger()) player.stopRiding();
        if(player.isAlive()) {
            for(Mob mob : player.level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(15))) {
                if(mob.isLeashed() && mob.getLeashHolder() == player) mob.dropLeash(true, true);
            }
            LevelUtil.warpServerPlayer(event.getPlayer(), false);
        }
        if(!player.isAlive()) player.invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if(event.isEndConquered()) return;
        Player player = event.getPlayer();
        player.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(100);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        player.setHealth(player.getMaxHealth());
        ServerPlayer p = (ServerPlayer) event.getPlayer();
        p.spawnInvulnerableTime = 2;
        IPlayerData capP = PlayerData.get(p);
        IActionTracker capA = ActionTracker.get(p);
        capP.addRevelatoryKnowledge(KnowledgeNF.ESSENCE.getId());
        capP.setStamina(AttributesNF.getMaxStamina(p));
        capA.startAction(ActionsNF.EMPTY.getId());
        capA.setFrame(-1);
        capA.setStunFrame(-1);
        capP.setLastDodgeTick(-100);
        capP.setLastBlockTick(-100);
        capP.setClimbTicks(0);
        capP.setAirTicks(0);
        capP.setCrouchTicks(0);
        capP.setClimbing(false);
        capP.setLastMainItem();
        capP.setLastOffItem();
        NetworkHandler.toClient((ServerPlayer) p, new ActionTrackerToClient(capA.writeNBT(), p.getId()));
        NetworkHandler.toClient((ServerPlayer) p, new PlayerDataToClient(capP.writeNBT(), p.getId()));
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if(LevelData.isPresent(player.level)) {
            ILevelData capL = LevelData.get(player.level);
            NetworkHandler.toClient(player, new LevelDataToClient(capL.writeNBTSync(capL.writeNBTClientInit(new CompoundTag()))));
        }
        IPlayerData capP = PlayerData.get(player);
        IActionTracker capA = ActionTracker.get(player);
        capP.setLastDodgeTick(-100);
        capP.setLastBlockTick(-100);
        capP.setClimbTicks(0);
        capP.setAirTicks(0);
        capP.setCrouchTicks(0);
        capP.setClimbing(false);
        capP.setLastMainItem();
        capP.setLastOffItem();
        NetworkHandler.toClient(player, new ActionTrackerToClient(capA.writeNBT(), player.getId()));
        NetworkHandler.toClient(player, new PlayerDataToClient(capP.writeNBT(), player.getId()));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        ServerPlayer clone = (ServerPlayer) event.getPlayer();
        original.reviveCaps();
        IActionTracker oCapA = ActionTracker.get(original);
        IActionTracker cCapA = ActionTracker.get(clone);
        IPlayerData oCapP = PlayerData.get(original);
        IPlayerData cCapP = PlayerData.get(clone);
        clone.inventoryMenu = new PlayerInventoryContainer(clone.getInventory(), !clone.level.isClientSide());
        clone.containerMenu = clone.inventoryMenu;
        clone.initMenu(clone.inventoryMenu);
        clone.spawnInvulnerableTime = 2;
        clone.inventoryMenu.addSlotListener(createKnowledgeListener(clone));
        clone.getInventory().replaceWith(original.getInventory());
        cCapP.getAccessoryInventory().replaceWith(oCapP.getAccessoryInventory());
        clone.foodData = original.foodData;
        MobEffectInstance starvation = original.getEffect(EffectsNF.STARVATION_1.get());
        if(starvation == null) starvation = original.getEffect(EffectsNF.STARVATION.get());
        if(starvation != null) {
            clone.addEffect(starvation);
            MobEffectInstance finalStarvation = starvation;
            clone.server.execute(() -> clone.connection.send(new ClientboundUpdateMobEffectPacket(clone.getId(), finalStarvation)));
        }
        if(!clone.isCreative() && !clone.isSpectator()) {
            MobEffectInstance despair = original.getEffect(EffectsNF.DESPAIR.get());
            if(despair == null) {
                if(event.isWasDeath()) {
                    MobEffectInstance newDespair = new MobEffectInstance(EffectsNF.DESPAIR.get(), (int) ContinentalWorldType.DAY_LENGTH);
                    clone.addEffect(newDespair);
                    clone.server.execute(() -> clone.connection.send(new ClientboundUpdateMobEffectPacket(clone.getId(), newDespair)));
                }
            }
            else {
                MobEffectInstance newDespair = !event.isWasDeath() ? despair :
                        new MobEffectInstance(EffectsNF.DESPAIR.get(), (int) ContinentalWorldType.DAY_LENGTH, Math.min(2, despair.getAmplifier() + 1));
                clone.addEffect(newDespair);
                clone.server.execute(() -> clone.connection.send(new ClientboundUpdateMobEffectPacket(clone.getId(), newDespair)));
            }
        }

        cCapP.readEncyclopediaNBT(oCapP.writeEncyclopediaNBT(new CompoundTag()));
        cCapP.setStamina(oCapP.getStamina());
        cCapA.startAction(ActionsNF.EMPTY.getId());
        cCapA.setFrame(-1);
        cCapA.setStunFrame(-1);
        cCapP.setLastDodgeTick(-100);
        cCapP.setLastBlockTick(-100);
        cCapP.setClimbTicks(0);
        cCapP.setAirTicks(0);
        cCapP.setCrouchTicks(0);
        cCapP.setClimbing(false);
        cCapP.setLastMainItem();
        cCapP.setLastOffItem();
        cCapP.setNeedsAttributeSelection(oCapP.needsAttributeSelection());
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            cCapP.setAttributePoints(attribute, oCapP.getAttributePoints(attribute));
        }
        original.invalidateCaps();
    }
}
