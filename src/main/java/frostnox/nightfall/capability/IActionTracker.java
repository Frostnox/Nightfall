package frostnox.nightfall.capability;

import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.util.math.BoundingSphere;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface IActionTracker {
    LivingEntity getEntity();

    ResourceLocation getActionID();

    void setActionID(ResourceLocation id);

    int getFrame();

    void setFrame(int frame);

    int getDuration();

    void setDuration(int duration);

    int getState();

    void setState(int state);

    int getCharge();

    void setCharge(int charge);

    float getChargePartial();

    void setChargePartial(float partial);

    float getChargeAttackMultiplier();

    float getChargeDestroyProgressMultiplier();

    boolean isFullyCharged();

    float getSpeedMultiplier();

    boolean isQueued();

    void queue();

    void dequeue();

    void releaseCharge();

    int getStunFrame();

    int getStunDuration();

    void setStunFrame(int frame);

    void setStunDuration(int duration);

    int getBlockInvulnerableTime();

    void setBlockInvulnerableTime(int time);

    int getMovingBlockInvulnerableTime();

    void setMovingBlockInvulnerableTime(int time);

    int getDotInvulnerableTime();

    void setDotInvulnerableTime(int time);

    int getBleedDuration();

    void setBleedDuration(int duration);

    int getPoisonDuration();

    void setPoisonDuration(int duration);

    List<Integer> getHitEntities();

    int getLivingEntitiesHit();

    void setLivingEntitiesHit(int amount);

    Vec3 getLastPosition();

    void setLastPosition(Vec3 pos);

    float getProgress(float partial);

    void tick();

    void moveState();

    boolean isInactive();

    boolean isDamaging();

    boolean isCharging();

    Action getAction();

    void startAction(ResourceLocation actionID);

    void stunServer(int duration, boolean force);

    void stun(int duration, boolean force);

    boolean isStunned();

    boolean isStunnedOrHitPaused();

    float modifyPartialTick(float partialTick);

    void setHitPause(float hitPause);

    boolean hasHitPause();

    BoundingSphere[] getLastHurtSpheres();

    List<HitData> getEntitiesInAttack(Attack attack, float partialTicks);

    CompoundTag writeNBT();

    void readNBT(CompoundTag NBT);
}
