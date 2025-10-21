package frostnox.nightfall.capability;

import com.mojang.math.Vector3d;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.encyclopedia.Entry;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.capability.*;
import frostnox.nightfall.network.message.world.ClimbPositionToServer;
import frostnox.nightfall.network.message.world.TakeHoldableToClient;
import frostnox.nightfall.registry.EntriesNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

/**
 * Collection of assorted data for use with Player only
 */
public class PlayerData implements IPlayerData {
    public static final Capability<IPlayerData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    private static final UUID VITALITY_UUID = UUID.fromString("cc078d25-5332-4d91-bb71-ab98790bc63b");
    private static final UUID ENDURANCE_UUID = UUID.fromString("7ed3bc0a-87ed-4c49-b444-4804a7456765");
    private static final UUID WILLPOWER_UUID = UUID.fromString("295f9d46-540a-418e-877d-1d4bf78dec76");
    private static final UUID STRENGTH_UUID = UUID.fromString("7aa9386a-67b1-41a6-b0bd-e05b9c509549");
    private static final UUID AGILITY_UUID = UUID.fromString("d4501abd-e9b2-47d0-a303-f2a24db10e9e");
    private static final UUID PERCEPTION_UUID = UUID.fromString("dc94f46e-266d-410f-94f3-a626974c557e");
    private final Player player;
    private int lastDodgeTick = -100; //Tick of last dodge input
    private int lastBlockTick = -100; //Tick of last successful block
    private int holdTicks = 0; //Cumulative ticks spent holding
    private int climbTicks = 0; //Cumulative ticks spent climbing
    private double climbYAmount = 0; //Climb progress relative to y position, client-use only
    private final Vector3d climbPosition = new Vector3d(0, -1, 0); //Last climb position
    private int airTicks = 0; //Cumulative ticks spent airborne
    private int crouchTicks = 0; //Cumulative ticks spent crouching
    private int punchTicks = 0; //Cooldown for unarmed punches
    private int dodgeDir; //Ordinal value of DodgeDirection enum
    private boolean shouldSwing = false; //Whether the vanilla arm swing animation should play
    private boolean mainhandActive = true; //Which hand is being used by the ActionTracker
    private boolean dugBlock = false; //Whether player damaged a block in current action state
    private int hitStop = -1; //Which frame current action should apply hit stop, -1 to not apply
    private float hitStopPartial = 0F; //Partial tick for active hitstop, client-use only
    private boolean interacted = false; //Whether player has performed an interaction in current action (ex. smithing)
    private String mainUUID = "NULL", offUUID = "NULL"; //Most recently used item UUID for each hand
    private boolean isCrawling = false; //Whether player is manually crawling
    private boolean isClimbing = false; //Whether player is climbing the ledge of a block
    private int ticksSinceHit; //Ticks since player last damaged an entity with a weapon, server-use only
    private CompoundTag heldBlock = new CompoundTag(); //Held entity
    private final AccessoryInventory accessoryInventory = new AccessoryInventory();
    private final ItemStack[] lastAccessories = new ItemStack[AccessoryInventory.SIZE];
    private int lastInventoryCapacity;
    private int cachedIndex; //Index cache for modifiable items, server-use only, not saved
    private ItemStack lastMainItem = ItemStack.EMPTY, lastOffItem = ItemStack.EMPTY; //Items held last tick, not saved
    private ItemStack heldItemRecipeCache = ItemStack.EMPTY; //Item cache for use with recipe matching, not saved
    private int lastMainSwapTime = 0, lastOffSwapTime = 0; //Last player tick that items were swapped
    private boolean godmode = false; //Makes player invulnerable to damage
    private boolean needsAttributeSelection = true; //Permits access to attribute selection screen
    private final EnumMap<PlayerAttribute, Integer> attributePoints = new EnumMap<>(PlayerAttribute.class); //+/- points for each attribute
    private int undeadKilled = 0, undeadPursuers = 0;
    //Stamina
    private double stamina;
    private double lastStamina; //Last seen stamina value
    private int lastStaminaTick = -100; //Last player tick that stamina was drained, do not sync this to the client; ticks on server/client are different
    //Essence
    //Encyclopedia
    private final Map<ResourceLocation, EntryStage> entryStages = new Object2ObjectOpenHashMap<>(); //Entry ID and associated stage, if ID is not present it's locked
    private final Object2IntMap<ResourceLocation> revelatoryKnowledge = new Object2IntOpenHashMap<>(); //Entries with time left to unlock
    private final Set<ResourceLocation> knowledge = new ObjectOpenHashSet<>(); //All learned knowledge IDs, if ID is not present it's not yet discovered
    private final Set<ResourceLocation> notifications = new ObjectArraySet<>(); //Entry IDs that have notifications

    private PlayerData(Player player) {
        this.player = player;
        Arrays.fill(lastAccessories, ItemStack.EMPTY);
        for(PlayerAttribute attribute : PlayerAttribute.values()) {
            attributePoints.put(attribute, 0);
        }
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public double getStamina() {
        return stamina;
    }

    @Override
    public double getLastStamina() {
        return lastStamina;
    }

    @Override
    public int getLastStaminaDrainTick() {
        return lastStaminaTick;
    }

    @Override
    public void setStamina(double value){
        double maxStamina = AttributesNF.getMaxStamina(player);
        if(value < stamina) setLastStaminaDrainTick(player.tickCount);
        if(value < 0) stamina = 0;
        else stamina = Math.min(value, maxStamina);
    }

    @Override
    public void updateLastStamina(){
        lastStamina = stamina;
    }

    @Override
    public void setLastStaminaDrainTick(int tick) {
        lastStaminaTick = tick;
    }

    /**
     * Adds value to current stamina and (if value was negative) updates tick
     */
    @Override
    public void addStamina(double value) {
        if(value >= 0) stamina = Math.min(stamina + value, AttributesNF.getMaxStamina(player));
        else {
            stamina = Math.max(stamina + value * player.getAttributeValue(AttributesNF.STAMINA_REDUCTION.get()), 0);
            setLastStaminaDrainTick(player.tickCount);
        }
    }

    @Override
    public void tickStamina() {
        double maxStamina = AttributesNF.getMaxStamina(player);

        IActionTracker capA = ActionTracker.get(player);
        if(!capA.getAction().allowStaminaRegen(capA.getState())) {
            setLastStaminaDrainTick(player.tickCount);
        }
        //Handle sprinting
        if(player.isSprinting() && !player.isPassenger() && !player.isFallFlying()) {
            addStamina(player.isVisuallySwimming() ? -0.4D : -0.8D);
            if (getStamina() == 0) {
                player.setSprinting(false);
            }
        }
        else if(isClimbing()) {
            addStamina(-0.4D);
            if(getStamina() <= 0F) setClimbing(false);
        }

        //Check for regen
        if(getStamina() < maxStamina) {
        /*if(getStamina() == 0) {
            if(player.tickCount - getLastTick() >= 40) {
                addStamina(2F, player);
            }
        }
        else */
            if(player.tickCount - getLastStaminaDrainTick() >= 24 && (!player.level.isClientSide() || getStamina() > 0F)) {
                double regen = 1.5D;
                if(player.hasEffect(EffectsNF.ENERGIZING.get())) regen += 0.5;
                addStamina(regen * AttributesNF.getStaminaRegenMultiplier(player));
            }
        }

        if(player.isCreative() || player.isSpectator()) {
            setStamina(maxStamina);
        }
        //Sync client stamina
        else if(!player.level.isClientSide) {
            if(getStamina() > maxStamina) setStamina(maxStamina);
            if(getStamina() != getLastStamina()) {
                NetworkHandler.toClient((ServerPlayer) player, new StaminaChangedToClient(getStamina(), player.getId()));
            }
        }
        //Update last stamina for next tick
        updateLastStamina();
    }

    @Override
    public int getLastDodgeTick() {
        return lastDodgeTick;
    }

    @Override
    public void setLastDodgeTick(int value) {
        lastDodgeTick = value;
    }

    @Override
    public int getLastBlockTick() {
        return lastBlockTick;
    }

    @Override
    public void setLastBlockTick(int value) {
        lastBlockTick = value;
    }

    @Override
    public InteractionHand getActiveHand() {
        return isMainhandActive() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public InteractionHand getOppositeActiveHand() {
        return isMainhandActive() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    @Override
    public boolean isMainhandActive() {
        return mainhandActive;
    }

    @Override
    public void setMainhandActive() {
        mainhandActive = true;
    }

    @Override
    public void setOffhandActive() {
        mainhandActive = false;
    }

    @Override
    public int getDodgeDirection() {
        return dodgeDir;
    }

    @Override
    public void setDodgeDirection(int value) {
        dodgeDir = value;
    }

    @Override
    public boolean hasDugBlock() {
        return dugBlock;
    }

    @Override
    public void setDugBlock(boolean value) {
        dugBlock = value;
    }

    @Override
    public int getHitStopFrame() {
        return hitStop;
    }

    @Override
    public void setHitStopFrame(int value) {
        hitStop = value;
    }

    @Override
    public float getHitStopPartial() {
        return hitStopPartial;
    }

    @Override
    public void setHitStopPartial(float value) {
        hitStopPartial = value;
    }

    @Override
    public boolean hasInteracted() {
        return interacted;
    }

    @Override
    public void setInteracted(boolean value) {
        interacted = value;
    }

    @Override
    public boolean isCrawling() {
        return isCrawling;
    }

    @Override
    public void setCrawling(boolean value) {
        isCrawling = value;
    }

    @Override
    public boolean isClimbing() {
        return isClimbing;
    }

    @Override
    public void setClimbing(boolean value) {
        isClimbing = value;
    }

    @Override
    public int getClimbTicks() {
        return climbTicks;
    }

    @Override
    public void setClimbTicks(int amount) {
        climbTicks = amount;
    }

    @Override
    public double getClimbYAmount() {
        return climbYAmount;
    }

    @Override
    public void setClimbYAmount(double value) {
        climbYAmount = value;
    }

    @Override
    public Vector3d getClimbPosition() {
        return climbPosition;
    }

    @Override
    public void setClimbPosition(Vector3d position) {
        climbPosition.set(position);
    }

    @Override
    public void sendClimbPosition(Vector3d position) {
        if(!player.level.isClientSide()) return;
        setClimbPosition(position);
        NetworkHandler.toServer(new ClimbPositionToServer(position));
    }

    @Override
    public int getAirTicks() {
        return airTicks;
    }

    @Override
    public void setAirTicks(int amount) {
        airTicks = amount;
    }

    @Override
    public int getCrouchTicks() {
        return crouchTicks;
    }

    @Override
    public void setCrouchTicks(int amount) {
        crouchTicks = amount;
    }

    @Override
    public int getHoldTicks() {
        return holdTicks;
    }

    @Override
    public void setHoldTicks(int amount) {
        holdTicks = amount;
    }

    @Override
    public int getPunchTicks() {
        return punchTicks;
    }

    @Override
    public void setPunchTicks(int amount) {
        punchTicks = Math.max(-1, amount);
    }

    @Override
    public int getTicksSinceHit() {
        return ticksSinceHit;
    }

    @Override
    public void setTicksSinceHit(int amount) {
        ticksSinceHit = Math.max(-1, amount);
    }

    @Override
    public int getCachedModifiableIndex() {
        return cachedIndex;
    }

    @Override
    public void setCachedModifiableIndex(int index) {
        cachedIndex = index;
    }

    @Override
    public ItemStack getLastMainItem() {
        return lastMainItem;
    }

    @Override
    public void setLastMainItem() {
        lastMainItem = player.getMainHandItem().copy();
        lastMainSwapTime = player.tickCount;
    }

    @Override
    public ItemStack getLastOffItem() {
        return lastOffItem;
    }

    @Override
    public void setLastOffItem() {
        lastOffItem = player.getOffhandItem().copy();
        lastOffSwapTime = player.tickCount;
    }

    @Override
    public int getLastMainSwapTime() {
        return lastMainSwapTime;
    }

    @Override
    public int getLastOffSwapTime() {
        return lastOffSwapTime;
    }

    @Override
    public boolean hasNoSwapDelay() {
        int delay = player.level.isClientSide() ? 5 : 3; //Two tick latency buffer for servers
        return isMainhandActive() ? (player.tickCount - lastMainSwapTime > delay) : (player.tickCount - lastOffSwapTime > delay);
    }

    @Override
    public CompoundTag getHeldContents() {
        return heldBlock;
    }

    @Override
    public void setHeldContents(CompoundTag contents) {
        heldBlock = contents;
    }

    @Override
    public void holdBlockEntity(IHoldable holdable) {
        if(player.level.isClientSide()) return;
        BlockEntity blockEntity = (BlockEntity) holdable;
        heldBlock = holdable.writeDataAndClear();
        if(player.level.removeBlock(blockEntity.getBlockPos(), false)) {
            ActionTracker.get(player).startAction(ActionsNF.HOLD_ENTITY.getId());
            NetworkHandler.toAllTrackingAndSelf(player, new TakeHoldableToClient(heldBlock, player.getId()));
            SoundType soundtype = blockEntity.getBlockState().getSoundType(player.level, blockEntity.getBlockPos(), player);
            player.level.playSound(null, blockEntity.getBlockPos(), soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            blockEntity.setRemoved();
        }
        else {
            blockEntity.load(heldBlock);
            heldBlock = new CompoundTag();
            NetworkHandler.toClient((ServerPlayer) player, new ActionTrackerToClient(ActionTracker.get(player).writeNBT(), player.getId()));
        }
    }

    @Override
    public void putBlockEntity(BlockPos putPos, BlockHitResult result) {
        if(player.level.isClientSide()) return;
        BlockState block = player.level.getBlockState(putPos);
        BlockPlaceContext placeContext = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, result);
        boolean failed = false;
        if(!block.canBeReplaced(placeContext)) failed = true;
        BlockState state = Block.stateById(heldBlock.getInt("state"));
        IHoldable staticHoldable = (IHoldable) BlockEntity.loadStatic(player.blockPosition(), state, heldBlock);
        state = staticHoldable.resolvePutState(state, state.getBlock().getStateForPlacement(placeContext));
        if(!failed && state.getMaterial().blocksMotion() && !player.level.isUnobstructed(state, putPos, CollisionContext.of(player))) failed = true;
        if(!failed && state.is(TagsNF.HAS_PHYSICS) && LevelUtil.canFallThrough(player.level.getBlockState(putPos.below()))) failed = true;
        if(!failed && player.level.setBlockAndUpdate(putPos, state)) {
            ActionTracker.get(player).startAction(ActionsNF.EMPTY.getId());
            NetworkHandler.toAllTrackingAndSelf(player, new GenericEntityToClient(NetworkHandler.Type.STOP_HOLDING_CLIENT, player.getId()));
            BlockEntity blockEntity = player.level.getBlockEntity(putPos);
            blockEntity.load(heldBlock);
            ((IHoldable) blockEntity).onPut(putPos, player);
            blockEntity.setChanged();
            player.level.gameEvent(player, GameEvent.BLOCK_PLACE, putPos);
            heldBlock = new CompoundTag();
            SoundType soundtype = blockEntity.getBlockState().getSoundType(player.level, putPos, player);
            player.level.playSound(null, putPos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        }
        else failed = true;
        if(failed) {
            NetworkHandler.toClient((ServerPlayer) player, new ActionTrackerToClient(ActionTracker.get(player).writeNBT(), player.getId()));
        }
    }

    @Override
    public boolean dropBlockEntity() {
        if(player.level.isClientSide() || heldBlock.isEmpty()) return false;
        IHoldable holdable = (IHoldable) BlockEntity.loadStatic(player.blockPosition(), Block.stateById(heldBlock.getInt("state")), heldBlock);
        holdable.onDrop(player.level, player.blockPosition());
        heldBlock =  new CompoundTag();
        NetworkHandler.toAllTrackingAndSelf(player, new GenericEntityToClient(NetworkHandler.Type.STOP_HOLDING_CLIENT, player.getId()));
        return true;
    }

    @Override
    public boolean useBlockEntity(BlockPos usePos) {
        if(player.level.isClientSide() || heldBlock.isEmpty()) return false;
        IHoldable holdable = (IHoldable) BlockEntity.loadStatic(player.blockPosition(), Block.stateById(heldBlock.getInt("state")), heldBlock);
        boolean result = holdable.heldUse(usePos, player);
        heldBlock = holdable.writeDataAndClear();
        return result;
    }

    @Override
    public boolean useBlockEntity(Entity target) {
        if(player.level.isClientSide() || heldBlock.isEmpty()) return false;
        IHoldable holdable = (IHoldable) BlockEntity.loadStatic(player.blockPosition(), Block.stateById(heldBlock.getInt("state")), heldBlock);
        boolean result = holdable.heldUse(target, player);
        heldBlock = holdable.writeDataAndClear();
        return result;
    }

    @Override
    public AccessoryInventory getAccessoryInventory() {
        if(accessoryInventory.player == null) accessoryInventory.player = player;
        return accessoryInventory;
    }

    @Override
    public ItemStack getLastAccessory(AccessorySlot slot) {
        return lastAccessories[slot.ordinal()];
    }

    @Override
    public void setLastAccessory(AccessorySlot slot, ItemStack item) {
        lastAccessories[slot.ordinal()] = item;
    }

    @Override
    public void updateExpandableInventory(boolean force) {
        int capacity = AttributesNF.getInventoryCapacity(player);
        if(capacity != lastInventoryCapacity || force) {
            for(int i = 9; i < 36; i++) {
                Slot slot = player.inventoryMenu.getSlot(i);
                if(!slot.isActive()) {
                    ItemStack item = slot.getItem();
                    if(!item.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(item.copy());
                        slot.set(ItemStack.EMPTY);
                    }
                }
            }
        }
        lastInventoryCapacity = capacity;
    }

    @Override
    public boolean hasGodMode() {
        return godmode;
    }

    @Override
    public boolean toggleGodMode() {
        godmode = !godmode;
        return godmode;
    }

    @Override
    public boolean needsAttributeSelection() {
        return needsAttributeSelection;
    }

    @Override
    public void setNeedsAttributeSelection(boolean value) {
        needsAttributeSelection = value;
    }

    @Override
    public void setAttributePoints(PlayerAttribute attribute, int points) {
        if(points < -3 || points > 3) Nightfall.LOGGER.error("Cannot set " + attribute.toString() + " points to " + points + ". Points cannot exceed +-3");
        else attributePoints.put(attribute, points);
        if(!player.level.isClientSide) {
            switch(attribute) {
                case VITALITY -> {
                    player.getAttribute(Attributes.MAX_HEALTH).removePermanentModifier(VITALITY_UUID);
                    player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                            new AttributeModifier(VITALITY_UUID, "Innate vitality", points * 10, AttributeModifier.Operation.ADDITION));
                }
                case ENDURANCE -> {
                    player.getAttribute(AttributesNF.ENDURANCE.get()).removePermanentModifier(ENDURANCE_UUID);
                    player.getAttribute(AttributesNF.ENDURANCE.get()).addPermanentModifier(
                            new AttributeModifier(ENDURANCE_UUID, "Innate endurance", points, AttributeModifier.Operation.ADDITION));
                }
                case WILLPOWER -> {
                    player.getAttribute(AttributesNF.WILLPOWER.get()).removePermanentModifier(WILLPOWER_UUID);
                    player.getAttribute(AttributesNF.WILLPOWER.get()).addPermanentModifier(
                            new AttributeModifier(WILLPOWER_UUID, "Innate willpower", points, AttributeModifier.Operation.ADDITION));
                }
                case STRENGTH -> {
                    player.getAttribute(AttributesNF.STRENGTH.get()).removePermanentModifier(STRENGTH_UUID);
                    player.getAttribute(AttributesNF.STRENGTH.get()).addPermanentModifier(
                            new AttributeModifier(STRENGTH_UUID, "Innate strength", points, AttributeModifier.Operation.ADDITION));
                }
                case AGILITY -> {
                    player.getAttribute(Attributes.MOVEMENT_SPEED).removePermanentModifier(AGILITY_UUID);
                    player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                            new AttributeModifier(AGILITY_UUID, "Innate agility", points * 0.03, AttributeModifier.Operation.MULTIPLY_BASE));
                }
                case PERCEPTION -> {
                    player.getAttribute(AttributesNF.PERCEPTION.get()).removePermanentModifier(PERCEPTION_UUID);
                    player.getAttribute(AttributesNF.PERCEPTION.get()).addPermanentModifier(
                            new AttributeModifier(PERCEPTION_UUID, "Innate perception", points, AttributeModifier.Operation.ADDITION));
                }
            }
        }
    }

    @Override
    public int getAttributePoints(PlayerAttribute attribute) {
        return attributePoints.get(attribute);
    }

    @Override
    public EnumMap<PlayerAttribute, Integer> copyAttributePoints() {
        return attributePoints.clone();
    }

    @Override
    public void setHeldItemForRecipe(ItemStack item) {
        heldItemRecipeCache = item;
    }

    @Override
    public ItemStack getHeldItemForRecipe() {
        return heldItemRecipeCache;
    }

    @Override
    public int getUndeadKilledThisNight() {
        return undeadKilled;
    }

    @Override
    public void setUndeadKilledThisNight(int value) {
        undeadKilled = value;
    }

    @Override
    public String getMainUUID() {
        return mainUUID;
    }

    @Override
    public void setMainUUID(String uuid) {
        mainUUID = uuid;
    }

    @Override
    public String getOffUUID() {
        return offUUID;
    }

    @Override
    public void setOffUUID(String uuid) {
        offUUID = uuid;
    }

    @Override
    public boolean getShouldSwing() {
        return shouldSwing;
    }

    @Override
    public void setShouldSwing(boolean value) {
        shouldSwing = value;
    }

    @Override
    public void advanceStage(ResourceLocation id) {
        if(id == null) Nightfall.LOGGER.error("Tried to advance null entry, likely from previous version. Ignoring...");
        else if(!hasEntry(id)) Nightfall.LOGGER.error("Stage for entry " + id + " could not be advanced since entry is not learned.");
        else if(getStage(id) == EntryStage.COMPLETED) Nightfall.LOGGER.error("Entry " + id + " cannot be advanced since it is already completed.");
        else {
            EntryStage nextStage = getStage(id).advance();
            entryStages.put(id, nextStage);
            if(!player.level.isClientSide()) {
                NetworkHandler.toClient((ServerPlayer) player, new EncyclopediaEntryToClient(id, nextStage, player.getId()));
                if(nextStage == EntryStage.COMPLETED) {
                    Knowledge knowledge = RegistriesNF.getKnowledge().getValue(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_entry"));
                    if(knowledge != null) addKnowledge(knowledge.getRegistryName());
                }
            }
            refreshEncyclopedia();
        }
    }

    @Override
    public void unlockEntry(ResourceLocation id) {
        EntryStage stage = getStage(id);
        if(stage != EntryStage.LOCKED && stage != EntryStage.HIDDEN) Nightfall.LOGGER.error("Tried to unlock entry " + id + " but it is already unlocked.");
        else addEntry(id, EntriesNF.get(id).puzzle == null ? EntryStage.COMPLETED : EntryStage.PUZZLE);
    }

    @Override
    public void addEntry(ResourceLocation id, EntryStage stage) {
        if(id == null) Nightfall.LOGGER.error("Tried to add null entry.");
        else {
            entryStages.put(id, stage);
            if(!player.level.isClientSide()) NetworkHandler.toClient((ServerPlayer) player, new EncyclopediaEntryToClient(id, stage, player.getId()));
        }
    }

    @Override
    public void removeEntry(ResourceLocation id) {
        if(id != null) {
            entryStages.remove(id);
            refreshEncyclopedia();
        }
    }

    @Override
    public void addKnowledge(ResourceLocation id) {
        if(id == null) Nightfall.LOGGER.warn("Tried to add null knowledge.");
        else if(knowledge.add(id)) {
            if(!player.level.isClientSide()) NetworkHandler.toClient((ServerPlayer) player, new EncyclopediaKnowledgeToClient(id, false, player.getId()));
            refreshEncyclopedia();
        }
    }

    @Override
    public void addRevelatoryKnowledge(ResourceLocation id) {
        if(id == null) Nightfall.LOGGER.warn("Tried to add null revelatory knowledge.");
        else if(!revelatoryKnowledge.containsKey(id) && !knowledge.contains(id)) {
            revelatoryKnowledge.put(id, 20 * 60 * 4 + player.getRandom().nextInt(20 * 60 * 6));
        }
    }

    @Override
    public void tickRevelatoryKnowledge() {
        for(var entry : revelatoryKnowledge.object2IntEntrySet()) {
            if(entry.getIntValue() == 1) {
                addKnowledge(entry.getKey());
                revelatoryKnowledge.remove(entry.getKey(), entry.getIntValue());
            }
            else revelatoryKnowledge.put(entry.getKey(), entry.getIntValue() - 1);
        }
    }

    @Override
    public void removeKnowledge(ResourceLocation id) {
        if(id != null) {
            if(knowledge.remove(id)) {
                if(!player.level.isClientSide()) NetworkHandler.toClient((ServerPlayer) player, new EncyclopediaKnowledgeToClient(id, true, player.getId()));
            }
            else revelatoryKnowledge.remove(id);
        }
    }

    @Override
    public void refreshEncyclopedia() {
        for(Entry entry : RegistriesNF.getEntries()) {
            EntryStage stage = getStage(entry.getRegistryName());
            if(stage == EntryStage.HIDDEN || stage == EntryStage.LOCKED) {
                if(entry.shouldUnlock(this)) unlockEntry(entry.getRegistryName());
                else if(stage == EntryStage.HIDDEN && entry.shouldReveal(this)) {
                    addEntry(entry.getRegistryName(), EntryStage.LOCKED);
                }
            }
        }
    }

    @Override
    public void resetEncyclopedia() {
        entryStages.clear();
        revelatoryKnowledge.clear();
        notifications.clear();
        NetworkHandler.toClient((ServerPlayer) player, new EncyclopediaToClient(writeEncyclopediaNBT(new CompoundTag()), player.getId()));
        unlockEntry(EntriesNF.TOOLS.getId());
        refreshEncyclopedia();
    }

    @Override
    public boolean hasEntry(ResourceLocation id) {
        return entryStages.getOrDefault(id, EntryStage.HIDDEN) != EntryStage.HIDDEN;
    }

    @Override
    public boolean hasEntryStage(ResourceLocation id, EntryStage stage) {
        return entryStages.containsKey(id) && stage == entryStages.get(id);
    }

    @Override
    public boolean hasCompletedEntry(ResourceLocation id) {
        return hasEntryStage(id, EntryStage.COMPLETED);
    }

    @Override
    public boolean hasNoEntries() {
        return entryStages.isEmpty();
    }

    @Override
    public boolean hasKnowledge(ResourceLocation id) {
        return knowledge.contains(id);
    }

    @Override
    public EntryStage getStage(ResourceLocation id) {
        if(!hasEntry(id)) return EntryStage.HIDDEN;
        else return entryStages.get(id);
    }

    @Override
    public void addEntryNotification(ResourceLocation id) {
        notifications.add(id);
    }

    @Override
    public void removeEntryNotification(ResourceLocation id) {
        notifications.remove(id);
    }

    @Override
    public boolean hasEntryNotification(ResourceLocation id) {
        return notifications.contains(id);
    }

    @Override
    public Set<ResourceLocation> getEntryNotifications() {
        return notifications;
    }

    @Override
    public CompoundTag writeEncyclopediaNBT(CompoundTag tag) {
        CompoundTag entryStages = new CompoundTag();
        this.entryStages.forEach((id, stage) -> entryStages.putInt(id.toString(), stage.ordinal()));
        tag.put("entry_stages", entryStages);

        CompoundTag revelatoryKnowledge = new CompoundTag();
        this.revelatoryKnowledge.forEach((id, ticks) -> revelatoryKnowledge.putInt(id.toString(), ticks));
        tag.put("revelatory_knowledge", revelatoryKnowledge);

        ListTag knowledge = new ListTag();
        this.knowledge.forEach(id -> knowledge.add(StringTag.valueOf(id.toString())));
        tag.put("knowledge", knowledge);

        ListTag notifications = new ListTag();
        this.notifications.forEach(id -> notifications.add(StringTag.valueOf(id.toString())));
        tag.put("notifications", notifications);
        return tag;
    }

    @Override
    public void readEncyclopediaNBT(CompoundTag tag) {
        entryStages.clear();
        CompoundTag entryStages = tag.getCompound("entry_stages");
        for(String id : entryStages.getAllKeys()) this.entryStages.put(ResourceLocation.parse(id), EntryStage.values()[entryStages.getInt(id)]);

        revelatoryKnowledge.clear();
        CompoundTag revelatoryKnowledge = tag.getCompound("revelatory_knowledge");
        for(String id : revelatoryKnowledge.getAllKeys()) this.revelatoryKnowledge.put(ResourceLocation.parse(id), revelatoryKnowledge.getInt(id));

        knowledge.clear();
        ListTag knowledge = tag.getList("knowledge", ListTag.TAG_STRING);
        for(Tag id : knowledge) {
            this.knowledge.add(ResourceLocation.parse(id.getAsString()));
        }

        notifications.clear();
        ListTag notifications = tag.getList("notifications", ListTag.TAG_STRING);
        for(Tag id : notifications) {
            this.notifications.add(ResourceLocation.parse(id.getAsString()));
        }
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("stamina", stamina);
        tag.putDouble("lastStamina", lastStamina);
        tag.putInt("lastDodgeTick", getLastDodgeTick());
        tag.putInt("lastBlockTick", getLastBlockTick());
        tag.putInt("dodgeDirection", getDodgeDirection());
        tag.putBoolean("dugBlock", hasDugBlock());
        tag.putInt("hitStop", getHitStopFrame());
        tag.putBoolean("interacted", hasInteracted());
        tag.putBoolean("swing", getShouldSwing());
        tag.putBoolean("activeMainhand", isMainhandActive());
        tag.putString("mainUUID", getMainUUID());
        tag.putString("offUUID", getOffUUID());
        tag.putBoolean("crawling", isCrawling());
        tag.putBoolean("climbing", isClimbing());
        tag.putInt("climbTicks", getClimbTicks());
        tag.putFloat("climbX", (float) getClimbPosition().x);
        tag.putFloat("climbY", (float) getClimbPosition().y);
        tag.putFloat("climbZ", (float) getClimbPosition().z);
        tag.putInt("airTicks", getAirTicks());
        tag.putInt("crouchTicks", getCrouchTicks());
        tag.putInt("holdTicks", getHoldTicks());
        tag.putInt("punchTicks", getPunchTicks());
        tag.put("heldEntity", heldBlock);
        tag.put("accessoryInventory", accessoryInventory.save());
        tag.putInt("lastInventoryCapacity", lastInventoryCapacity);
        tag.putInt("undeadKilled", undeadKilled);
        tag.putBoolean("godmode", godmode);
        tag.putBoolean("needsAttributeSelection", needsAttributeSelection);
        for(PlayerAttribute key : attributePoints.keySet()) {
            tag.putInt(key.toString() + "Points", attributePoints.get(key));
        }

        writeEncyclopediaNBT(tag);

        return tag;
    }

    @Override
    public void readNBT(CompoundTag tag) {
        stamina = tag.getDouble("stamina");
        lastStamina = tag.getDouble("lastStamina");
        setLastDodgeTick(tag.getInt("lastDodgeTick"));
        setLastBlockTick(tag.getInt("lastBlockTick"));
        setDodgeDirection(tag.getInt("dodgeDirection"));
        setDugBlock(tag.getBoolean("dugBlock"));
        setHitStopFrame(tag.getInt("hitStop"));
        setInteracted(tag.getBoolean("interacted"));
        setShouldSwing(tag.getBoolean("swing"));
        if(tag.getBoolean("activeMainhand")) setMainhandActive();
        else setOffhandActive();
        setMainUUID(tag.getString("mainUUID"));
        setOffUUID(tag.getString("offUUID"));
        setCrawling(tag.getBoolean("crawling"));
        setClimbing(tag.getBoolean("climbing"));
        setClimbTicks(tag.getInt("climbTicks"));
        setClimbPosition(new Vector3d(tag.getFloat("climbX"), tag.getFloat("climbY"), tag.getFloat("climbZ")));
        setAirTicks(tag.getInt("airTicks"));
        setCrouchTicks(tag.getInt("crouchTicks"));
        setHoldTicks(tag.getInt("holdTicks"));
        setPunchTicks(tag.getInt("punchTicks"));
        heldBlock = tag.getCompound("heldEntity");
        accessoryInventory.load(tag.getList("accessoryInventory", 10));
        lastInventoryCapacity = tag.getInt("lastInventoryCapacity");
        undeadKilled = tag.getInt("undeadKilled");
        godmode = tag.getBoolean("godmode");
        if(tag.contains("needsAttributeSelection")) needsAttributeSelection = tag.getBoolean("needsAttributeSelection");
        for(PlayerAttribute key : attributePoints.keySet()) {
            attributePoints.put(key, tag.getInt(key.toString() + "Points"));
        }

        readEncyclopediaNBT(tag);
    }

    public static IPlayerData get(Player p) {
        return p.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static boolean isPresent(Player p) {
        return p.getCapability(CAPABILITY, null).isPresent();
    }

    public static class PlayerDataCapability implements ICapabilitySerializable<CompoundTag> {
        private final PlayerData cap;
        private final LazyOptional<IPlayerData> holder;

        public PlayerDataCapability(Player player) {
            cap = new PlayerData(player);
            holder = LazyOptional.of(() -> cap);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> c, Direction side) {
            return CAPABILITY == c ? (LazyOptional<T>) holder : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return cap.writeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag NBT) {
            cap.readNBT(NBT);
        }
    }
}
