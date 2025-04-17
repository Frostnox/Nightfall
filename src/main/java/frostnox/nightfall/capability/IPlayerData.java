package frostnox.nightfall.capability;

import com.mojang.math.Vector3d;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.encyclopedia.EntryStage;
import frostnox.nightfall.entity.PlayerAttribute;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.EnumMap;
import java.util.Set;

public interface IPlayerData {
    Player getPlayer();

    double getStamina();

    double getLastStamina();

    int getLastStaminaDrainTick();

    void setStamina(double value);

    void updateLastStamina();

    void setLastStaminaDrainTick(int tick);

    void addStamina(double value);

    void tickStamina();

    String getMainUUID();

    void setMainUUID(String uuid);

    String getOffUUID();

    void setOffUUID(String uuid);

    boolean getShouldSwing();

    void setShouldSwing(boolean value);

    int getLastDodgeTick();

    void setLastDodgeTick(int value);

    int getLastBlockTick();

    void setLastBlockTick(int value);

    InteractionHand getActiveHand();

    InteractionHand getOppositeActiveHand();

    boolean isMainhandActive();

    void setMainhandActive();

    void setOffhandActive();

    int getDodgeDirection();

    void setDodgeDirection(int value);

    boolean hasDugBlock();

    void setDugBlock(boolean value);

    int getHitStopFrame();

    void setHitStopFrame(int value);

    float getHitStopPartial();

    void setHitStopPartial(float value);

    boolean hasInteracted();

    void setInteracted(boolean value);

    boolean isCrawling();

    void setCrawling(boolean value);

    boolean isClimbing();

    void setClimbing(boolean value);

    int getClimbTicks();

    void setClimbTicks(int amount);

    double getClimbYAmount();

    void setClimbYAmount(double value);

    Vector3d getClimbPosition();

    void setClimbPosition(Vector3d position);

    void sendClimbPosition(Vector3d position);

    int getAirTicks();

    void setAirTicks(int amount);

    int getCrouchTicks();

    void setCrouchTicks(int amount);

    int getHoldTicks();

    void setHoldTicks(int amount);

    int getPunchTicks();

    void setPunchTicks(int amount);

    int getTicksSinceHit();

    void setTicksSinceHit(int amount);

    int getCachedModifiableIndex();

    void setCachedModifiableIndex(int index);

    ItemStack getLastMainItem();

    void setLastMainItem();

    ItemStack getLastOffItem();

    void setLastOffItem();

    int getLastMainSwapTime();

    int getLastOffSwapTime();

    boolean hasNoSwapDelay();

    public CompoundTag getHeldContents();

    void setHeldContents(CompoundTag contents);

    void holdBlockEntity(IHoldable entity);

    void putBlockEntity(BlockPos putPos, BlockHitResult result);

    boolean dropBlockEntity();

    boolean useBlockEntity(BlockPos usePos);

    boolean useBlockEntity(Entity target);

    AccessoryInventory getAccessoryInventory();

    ItemStack getLastAccessory(AccessorySlot slot);

    void setLastAccessory(AccessorySlot slot, ItemStack item);

    void updateExpandableInventory(boolean force);

    boolean hasGodMode();

    boolean toggleGodMode();

    boolean needsAttributeSelection();

    void setNeedsAttributeSelection(boolean value);

    void setAttributePoints(PlayerAttribute attribute, int points);

    int getAttributePoints(PlayerAttribute attribute);

    EnumMap<PlayerAttribute, Integer> copyAttributePoints();

    void setHeldItemForRecipe(ItemStack item);

    ItemStack getHeldItemForRecipe();

    int getUndeadKilledThisNight();

    void setUndeadKilledThisNight(int value);

    void advanceStage(ResourceLocation id);

    void unlockEntry(ResourceLocation id);

    void addEntry(ResourceLocation id, EntryStage stage);

    void removeEntry(ResourceLocation id);

    void addKnowledge(ResourceLocation id);

    void addRevelatoryKnowledge(ResourceLocation id);

    void tickRevelatoryKnowledge();

    void removeKnowledge(ResourceLocation id);

    void refreshEncyclopedia();

    void resetEncyclopedia();

    boolean hasEntry(ResourceLocation id);

    boolean hasEntryStage(ResourceLocation id, EntryStage stage);

    boolean hasCompletedEntry(ResourceLocation id);

    boolean hasNoEntries();

    boolean hasKnowledge(ResourceLocation id);

    EntryStage getStage(ResourceLocation id);

    void addEntryNotification(ResourceLocation id);

    void removeEntryNotification(ResourceLocation id);

    boolean hasEntryNotification(ResourceLocation id);

    Set<ResourceLocation> getEntryNotifications();

    CompoundTag writeEncyclopediaNBT(CompoundTag tag);

    void readEncyclopediaNBT(CompoundTag tag);

    CompoundTag writeNBT();

    void readNBT(CompoundTag tag);
}
