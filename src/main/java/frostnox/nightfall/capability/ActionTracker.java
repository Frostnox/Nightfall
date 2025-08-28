package frostnox.nightfall.capability;

import com.google.common.collect.Lists;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.effect.DamageEffect;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.capability.StatusToClient;

import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.util.math.BoundingSphere;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.math.Mat4f;
import frostnox.nightfall.util.math.Quat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public class ActionTracker implements IActionTracker {
    public static final Capability<IActionTracker> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    private static final UUID UNDERWATER_SPEED_MODIFIER_UUID = UUID.fromString("48be4186-aa6c-476d-917a-4f07882832f4");
    private static final AttributeModifier UNDERWATER_SPEED_MODIFIER = new AttributeModifier(UNDERWATER_SPEED_MODIFIER_UUID,
            "Underwater speed penalty", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final LivingEntity user;
    private ResourceLocation actionID = ActionsNF.EMPTY.getId();
    private int frame;
    private int duration;
    private int state;
    private int charge;
    private float speedMultiplier; //Affects duration of new actions (value from action speed attribute)
    private boolean queue; //Whether a chain attack is queued
    private int stunFrame;
    private int stunDuration;
    private int blockInvulnerableTime; //Ticks for invulnerability from blocks
    private int movingBlockInvulnerableTime; //Ticks for invulnerability from moving blocks
    private int dotInvulnerableTime; //Fire relies on vanilla invulnerable timer which is no longer used, extra timer is required to stop excessive damage if fire time stalls at damage tick
    private int bleedTime; //Ticks left bleeding, should be in sync with the effect
    private int poisonTime; //Ticks left poisoned, should be in sync with the effect
    private int livingEntitiesHit; //Amount of entities hit of class LivingEntity
    //Cache fields
    private final List<Integer> hitEntities; //This should never be synced to the client, use messages that provide IDs instead
    private float lastChargePartial; //Last partial when charge state was active, client only
    private Vec3 lastPosition; //Last position (vanilla loves resetting these before the end of the tick)
    private BoundingSphere[] lastHurtSpheres; //Fully transformed spheres from last damage attack, null if last tick wasn't an attack
    private float hitPause = -1F;

    public ActionTracker(LivingEntity entity) {
        this.user = entity;
        frame = -1;
        duration = 1;
        stunFrame = -1;
        stunDuration = 1;
        queue = false;
        hitEntities = Lists.newArrayList();
    }

    @Override
    public LivingEntity getEntity() {
        return user;
    }

    @Override
    public ResourceLocation getActionID() {
        return actionID;
    }

    @Override
    public void setActionID(ResourceLocation id) {
        actionID = id;
    }

    @Override
    public int getFrame() {
        return frame;
    }

    @Override
    public void setFrame(int frame) {
        this.frame = Mth.clamp(frame, -1, 9999);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = Mth.clamp(duration, 1, 9999);
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int state) {
        this.state = Math.max(0, state);
    }

    @Override
    public int getCharge() {
        return charge;
    }

    @Override
    public void setCharge(int charge) {
        if(getAction() != null) this.charge = Mth.clamp(charge, 0, getAction().getMaxCharge());
        else this.charge = Math.max(0, charge);
    }

    @Override
    public float getChargePartial() {
        return lastChargePartial;
    }

    @Override
    public void setChargePartial(float partial) {
        lastChargePartial = partial;
    }

    @Override
    public float getChargeAttackMultiplier() {
        if(charge <= 0) return 1F;
        else {
            float minCharge = getAction().getRequiredCharge(user);
            return 1F + Mth.clamp((charge - minCharge) / (getAction().getMaxCharge() - minCharge) * 0.5F, 0F, 0.5F);
        }
    }

    @Override
    public float getChargeDestroyProgressMultiplier() {
        if(charge <= 0) return 1F;
        else return 1F + Math.min(1F, (float) charge / (getAction().getMaxCharge()));
    }

    @Override
    public boolean isFullyCharged() {
        return charge >= getAction().getMaxCharge();
    }

    @Override
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public boolean isQueued() {
        return queue;
    }

    @Override
    public void queue() {
        queue = true;
    }

    @Override
    public void dequeue() {
        queue = false;
    }

    @Override
    public void releaseCharge() {
        if(isCharging()) queue();
    }

    @Override
    public int getStunFrame() {
        return stunFrame;
    }

    @Override
    public int getStunDuration() {
        return stunDuration;
    }

    @Override
    public void setStunFrame(int frame) {
        stunFrame = Mth.clamp(frame, -1, 9999);
    }

    @Override
    public void setStunDuration(int duration) {
        stunDuration = Mth.clamp(duration, 1, 9999);
    }

    @Override
    public int getBlockInvulnerableTime() {
        return blockInvulnerableTime;
    }

    @Override
    public void setBlockInvulnerableTime(int time) {
        blockInvulnerableTime = Math.max(0, time);
    }

    @Override
    public int getMovingBlockInvulnerableTime() {
        return movingBlockInvulnerableTime;
    }

    @Override
    public void setMovingBlockInvulnerableTime(int time) {
        movingBlockInvulnerableTime = Math.max(0, time);
    }

    @Override
    public int getDotInvulnerableTime() {
        return dotInvulnerableTime;
    }

    @Override
    public void setDotInvulnerableTime(int time) {
        dotInvulnerableTime = Math.max(0, time);
    }

    @Override
    public int getBleedDuration() {
        return bleedTime;
    }

    @Override
    public void setBleedDuration(int duration) {
        bleedTime = Math.max(0, duration);
    }

    @Override
    public int getPoisonDuration() {
        return poisonTime;
    }

    @Override
    public void setPoisonDuration(int duration) {
        poisonTime = Math.max(0, duration);
    }

    @Override
    public List<Integer> getHitEntities() {
        return hitEntities;
    }

    @Override
    public int getLivingEntitiesHit() {
        return livingEntitiesHit;
    }

    @Override
    public void setLivingEntitiesHit(int amount) {
        livingEntitiesHit = Math.max(0, amount);
    }

    @Override
    public Vec3 getLastPosition() {
        return lastPosition;
    }

    @Override
    public void setLastPosition(Vec3 pos) {
        lastPosition = pos;
    }

    @Override
    public float getProgress(float partial) {
        if(isCharging()) {
            float max = (float) getAction().getRequiredCharge(user);
            if(max == 0F) return 1F;
            else return Math.min(1F, Mth.lerp(modifyPartialTick(partial), (float) (frame - 1) / max, (float) frame / max));
        }
        else return Mth.lerp(modifyPartialTick(partial), (float) (frame - 1) / (float) duration, (float) frame / (float) duration);
    }

    @Override
    public void tick() {
        AttributeInstance actionSpeed = user.getAttribute(AttributesNF.ACTION_SPEED.get());
        if(actionSpeed.getModifier(UNDERWATER_SPEED_MODIFIER_UUID) != null) {
            actionSpeed.removeModifier(UNDERWATER_SPEED_MODIFIER);
        }
        if(user.isUnderWater() && !user.getType().is(TagsNF.AQUATIC_ENTITY)) {
            user.getAttribute(AttributesNF.ACTION_SPEED.get()).addTransientModifier(UNDERWATER_SPEED_MODIFIER);
        }
        //Stun
        if(stunFrame != -1) {
            stunFrame++;
            if(stunFrame > stunDuration) {
                stunFrame = -1;
                frame = -1;
                state = 0;
                charge = 0;
                hitPause = -1F;
                dequeue();
                startAction(ActionsNF.EMPTY.getId());
            }
        }
        //Action
        else {
            Action action = getAction();
            if(action == null) {
                startAction(ActionsNF.EMPTY.getId());
                action = getAction();
            }
            if(frame != -1) {
                if(hitPause >= 0F) hitPause = -1F;
                else frame++;
            }
            if(queue) {
                if(state == action.getChargeState()) {
                    if(frame >= action.getRequiredCharge(user)) {
                        charge = frame;
                        action.onChargeRelease(user);
                        moveState();
                    }
                }
                else if((action.hasDefaultChain() || action.hasConditionalChain())) {
                    if(action.getChainState() == state) {
                        action.onChainStart(user);
                        startAction(action.getChain(user).getId());
                    }
                }
                else dequeue();
            }
            if(state == action.getChargeState()) {
                if(frame > action.getChargeTimeout() || !action.canContinueCharging(user)) {
                    charge = frame;
                    action.onChargeRelease(user);
                    moveState();
                }
            }
            else if(frame > duration) {
                if(action.isIdle() && state == action.getTotalStates() - 1) frame = 1;
                else moveState();
            }
        }
        //DoT timers
        this.setBlockInvulnerableTime(blockInvulnerableTime - 1);
        this.setMovingBlockInvulnerableTime(movingBlockInvulnerableTime - 1);
        this.setDotInvulnerableTime(dotInvulnerableTime - 1);
        if(bleedTime == 1 && !user.level.isClientSide) NetworkHandler.toAllTrackingAndSelf(user, new StatusToClient(0, user.getId(), StatusToClient.Status.BLEEDING));
        this.setBleedDuration(bleedTime - 1);
        if(poisonTime == 1 && !user.level.isClientSide) NetworkHandler.toAllTrackingAndSelf(user, new StatusToClient(0, user.getId(), StatusToClient.Status.POISON));
        this.setPoisonDuration(poisonTime - 1);
        //DoT damage
        if(!user.level.isClientSide() && dotInvulnerableTime == 0) {
            float damage = 0, highestDamage = 0;
            DamageTypeSource strongestSource = DamageTypeSource.ON_FIRE;
            if(user.isOnFire()) {
                damage += 5;
                highestDamage = 5;
            }
            for(MobEffectInstance effectInstance : user.getActiveEffects()) {
                if(effectInstance.getEffect() instanceof DamageEffect effect) {
                    float effectDamage = effect.getDamage(user, effectInstance.getDuration(), effectInstance.getAmplifier());
                    if(effectDamage > 0) damage += effectDamage;
                    if(effectDamage > highestDamage) {
                        highestDamage = effectDamage;
                        strongestSource = effect.damageSource;
                    }
                }
            }
            if(damage > 0F) user.hurt(strongestSource, damage);
            dotInvulnerableTime = 40;
        }
    }

    @Override
    public void moveState() {
        Action action = getAction();
        //Last state
        if(state >= action.getTotalStates() - 1) {
            if(action.shouldFreeze()) frame = duration + 1;
            else startAction(ActionsNF.EMPTY.getId());
        }
        //Other states
        else {
            state++;
            hitEntities.clear();
            livingEntitiesHit = 0;
            lastHurtSpheres = null;
            frame = 1;
            setDuration(action.getDuration(state, user));
        }
    }

    @Override
    public boolean isInactive() {
        if(stunFrame != -1) return false;
        if(actionID.equals(ActionsNF.EMPTY.getId())) return true;
        if(frame == -1) return true;
        else {
            Action action = getAction();
            return action.isInterruptible() && action.isIdle();
        }
    }

    @Override
    public boolean isDamaging() {
        if(frame > -1 && !isStunnedOrHitPaused()) {
            Action action = getAction();
            return action.isDamaging(this);
        }
        return false;
    }

    @Override
    public boolean isCharging() {
        return state == getAction().getChargeState();
    }

    @Override
    public Action getAction() {
        return ActionsNF.get(actionID);
    }

    @Override
    public void startAction(ResourceLocation actionID) {
        Action action = getAction();
        if(action != null) action.onEnd(user);
        setActionID(actionID);
        action = getAction();
        frame = 1;
        state = 0;
        speedMultiplier = (float) user.getAttributeValue(AttributesNF.ACTION_SPEED.get());
        setDuration(action.getDuration(state, user));
        charge = 0;
        dequeue();
        hitEntities.clear();
        livingEntitiesHit = 0;
        stunFrame = -1;
        if(user instanceof Player player) PlayerData.get(player).setInteracted(false);
        action.onStart(user);
    }

    @Override
    public void stunServer(int duration, boolean force) {
        if(user.level.isClientSide()) return;
        stun(duration, force);
        NetworkHandler.toAllTrackingAndSelf(user, new StatusToClient(stunDuration, user.getId(), StatusToClient.Status.STUN));
    }

    @Override
    public void stun(int duration, boolean force) {
        if(stunFrame == -1 || force) {
            stunFrame = 0;
            setStunDuration(duration);
            dequeue();
        }
    }

    @Override
    public boolean isStunned() {
        return stunFrame > -1;
    }

    @Override
    public boolean isStunnedOrHitPaused() {
        return isStunned() || hasHitPause();
    }

    @Override
    public float modifyPartialTick(float partialTick) {
        if(isStunned()) return 1F;
        else if(hasHitPause()) return hitPause;
        else return partialTick;
    }

    @Override
    public void setHitPause(float hitPause) {
        this.hitPause = hitPause;
    }

    @Override
    public boolean hasHitPause() {
        return hitPause >= 0F;
    }

    @Override
    public BoundingSphere[] getLastHurtSpheres() {
        return lastHurtSpheres;
    }

    private BoundingSphere[] getTransformedSpheres(Attack attack, float partialTicks) {
        BoundingSphere[] spheres = attack.getHurtSpheres(user).getSpheres();
        Mat4f userMatrix = new Mat4f();
        if(user instanceof Player player) {
            userMatrix.multiply(new Quat(Mth.lerp(partialTicks, ClientEngine.get().getLastXRot(), player.getXRot()), Vector3f.XN, true));
            userMatrix.multiply(new Quat(Mth.lerp(partialTicks, ClientEngine.get().getLastYRot(), player.getYRot()), Vector3f.YP, true));
            Vector3f translation = attack.getTranslation(player);
            int frame = getFrame();
            int duration = getDuration();
            int side = PlayerData.get(player).getActiveHand() == InteractionHand.MAIN_HAND ? 1 : -1;

            AnimationData data = attack.getAnimationData(player, this).get(EntityPart.HAND_RIGHT);
            data.update(partialTicks);
            if(attack instanceof IClientAction clientAction) {
                clientAction.transformModelFP(getState(), frame, duration, attack.getChargeProgress(getCharge(), getChargePartial()), player, data);
            }

            Vector3f offset = attack.getOffset(user);
            for(int i = 0; i < spheres.length; i++) {
                spheres[i].transformFP(data, userMatrix, translation, offset, side != 1);
                spheres[i].yPos += player.getEyeHeight();
            }
        }
        else if(user instanceof ActionableEntity actionable) {
            AnimationCalculator mCalc = new AnimationCalculator();
            AnimationData layer = new AnimationData();
            Mat4f localMatrix = new Mat4f();
            userMatrix = new Mat4f(new Quat(actionable.getAttackYRot(1), Vector3f.YP, true));
            int frame = getFrame();
            int duration = getDuration();
            EnumMap<EntityPart, AnimationData> transforms = attack.getAnimationData(user, this);
            for(AnimationData transform : transforms.values()) transform.update(partialTicks);
            mCalc.update(getFrame(), getDuration(), partialTicks, Easing.inOutSine);
            layer.update(getFrame(), getDuration(), partialTicks, Easing.inOutSine);
            Action action = getAction();
            attack.transformModel(getState(), frame, duration, action.getChargeProgress(getCharge(), getChargePartial()), attack.getPitch(user, partialTicks), user, transforms, mCalc);
            attack.transformLayer(getState(), frame, duration, action.getChargeProgress(getCharge(), getChargePartial()), user, layer);

            Vector3f mVec = mCalc.getTransformations();
            //userMatrix.multiply(new Quaternion(mVec.z(), Vector3f.ZP, true));
            userMatrix.multiply(new Quat(mVec.y(), Vector3f.YP, true));
            //userMatrix.multiply(new Quaternion(mVec.x(), Vector3f.XN, true));
            Vector3f lVec = layer.rCalc.getTransformations();
            localMatrix.multiply(new Quat(lVec.z(), Vector3f.ZP, true));
            localMatrix.multiply(new Quat(lVec.y(), Vector3f.YP, true));
            localMatrix.multiply(new Quat(lVec.x(), Vector3f.XP, true));
            Vector3f offset = attack.getOffset(user);
            Vector3f translation = attack.getTranslation(user);
            AnimationData[] transformsArray = transforms.values().toArray(new AnimationData[0]);
            for(int i = 0; i < spheres.length; i++) {
                spheres[i].transform(transformsArray, userMatrix, localMatrix, translation, offset);
            }
        }

        Vec3 pos = user instanceof Player ? ClientEngine.get().getPlayerPosition(partialTicks) : user.getPosition(partialTicks);
        for(int i = 0; i < spheres.length; i++) {
            spheres[i].translate(pos.x(), pos.y(), pos.z());
        }
        return spheres;
    }

    @Override
    public List<HitData> getEntitiesInAttack(Attack attack, float partialTicks) {
        List<HitData> hitTargets = Lists.newArrayList();
        int maxTargets = attack.getMaxTargets();
        maxTargets -= getLivingEntitiesHit();
        if(maxTargets <= 0) return hitTargets;
        if(lastHurtSpheres == null) lastHurtSpheres = getTransformedSpheres(attack, 0F);
        BoundingSphere[] spheres = getTransformedSpheres(attack, partialTicks);

        if(user.level.isClientSide() && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            for(int i = 0; i < spheres.length; i++) {
                user.level.addParticle(ParticleTypes.FLAME, true, spheres[i].xPos, spheres[i].yPos, spheres[i].zPos, 0, 0, 0);
            }
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        for(BoundingSphere s : spheres) {
            double sMinX = s.xPos - s.radius, sMinY = s.yPos - s.radius, sMinZ = s.zPos - s.radius;
            double sMaxX = s.xPos + s.radius, sMaxY = s.yPos + s.radius, sMaxZ = s.zPos + s.radius;
            if(sMinX < minX) minX = sMinX;
            if(sMinY < minY) minY = sMinY;
            if(sMinZ < minZ) minZ = sMinZ;
            if(sMaxX > maxX) maxX = sMaxX;
            if(sMaxY > maxY) maxY = sMaxY;
            if(sMaxZ > maxZ) maxZ = sMaxZ;
        }
        List<Entity> entities = CombatUtil.getAttackableEntitiesNearby(new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(IOrientedHitBoxes.MAX_DIST_FROM_AABB), user, user.level);

        for(int i = 0; i < spheres.length; i++) {
            boolean max = false;
            for(Entity entity : entities) {
                if(!getHitEntities().contains(entity.getId())) {
                    HitData hitData = new HitData(entity);
                    if(spheres[i].intersectsAndSeesEntity(user, entity, hitData)) {
                        if(entity instanceof LivingEntity) setLivingEntitiesHit(getLivingEntitiesHit() + 1);
                        getHitEntities().add(entity.getId());
                        hitData.x -= (float) entity.getX();
                        hitData.y -= (float) entity.getY();
                        hitData.z -= (float) entity.getZ();
                        hitData.force = new Vec3f((float) (spheres[i].xPos - lastHurtSpheres[i].xPos),
                                (float) (spheres[i].yPos - lastHurtSpheres[i].yPos),
                                (float) (spheres[i].zPos - lastHurtSpheres[i].zPos)).normalize();
                        hitTargets.add(hitData);
                    }
                    if(hitTargets.size() >= maxTargets) {
                        max = true;
                        break;
                    }
                }
            }
            if(max) break;
        }

        lastHurtSpheres = spheres;
        return hitTargets;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag NBT = new CompoundTag();
        NBT.putString("id", getActionID().toString());
        NBT.putInt("frame", getFrame());
        NBT.putInt("duration", getDuration());
        NBT.putInt("state", getState());
        NBT.putInt("charge", getCharge());
        NBT.putFloat("speedMod", speedMultiplier);
        NBT.putBoolean("queue", isQueued());
        NBT.putInt("stunFrame", getStunFrame());
        NBT.putInt("stunDuration", getStunDuration());
        NBT.putInt("blockInvulnerableTime", getBlockInvulnerableTime());
        NBT.putInt("movingBlockInvulnerableTime", getMovingBlockInvulnerableTime());
        NBT.putInt("dotInvulnerableTime", getDotInvulnerableTime());
        NBT.putInt("bleedDuration", getBleedDuration());
        NBT.putInt("poisonDuration", getPoisonDuration());
        NBT.putInt("entitiesHit", getLivingEntitiesHit());
        return NBT;
    }

    @Override
    public void readNBT(CompoundTag NBT) {
        setActionID(ResourceLocation.parse(NBT.contains("id") ? NBT.getString("id") : ActionsNF.EMPTY.getId().toString()));
        setFrame(NBT.getInt("frame"));
        duration = NBT.getInt("duration");
        setState(NBT.getInt("state"));
        setCharge(NBT.getInt("charge"));
        speedMultiplier = NBT.getFloat("speedMod");
        if(NBT.getBoolean("queue")) queue();
        else dequeue();
        setStunFrame(NBT.getInt("stunFrame"));
        setStunDuration(NBT.getInt("stunDuration"));
        setBlockInvulnerableTime(NBT.getInt("blockInvulnerableTime"));
        setMovingBlockInvulnerableTime(NBT.getInt("movingBlockInvulnerableTime"));
        setDotInvulnerableTime(NBT.getInt("dotInvulnerableTime"));
        setBleedDuration(NBT.getInt("bleedDuration"));
        setPoisonDuration(NBT.getInt("poisonDuration"));
        setLivingEntitiesHit(NBT.getInt("entitiesHit"));
    }

    public static IActionTracker get(Entity entity) {
        return entity.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static boolean isPresent(Entity entity) {
        return entity.getCapability(CAPABILITY).isPresent();
    }

    public static class ActionTrackerCapability implements ICapabilitySerializable<CompoundTag> {
        private final ActionTracker cap;
        private final LazyOptional<IActionTracker> holder;

        public ActionTrackerCapability(LivingEntity entity) {
            cap = new ActionTracker(entity);
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
