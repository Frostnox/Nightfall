package frostnox.nightfall.action;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.entity.PlayerModelNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.ActionsNF;

import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base action class for entities, used for generic keyframe animations.
 */
public abstract class Action extends ForgeRegistryEntry<Action> {
    public static final int DEFAULT_DURATION = 2;
    public static final int CHARGE_MAX = 32768;
    private final int[] duration; //Array of states and duration for each
    private final boolean interruptible;
    private final boolean looping;
    private final boolean freeze;
    private final boolean sprinting;
    private final boolean crawling;
    private final int chainState; //State where action can be chained into another
    private final int chargeState; //State where action can be charged
    public final Set<ResourceLocation> linkedActions = new HashSet<>(); //All actions linked to this one via from, to, and conditionalTo
    private final RegistryObject<? extends Action> from; //Action that preceded this Action
    private final RegistryObject<? extends Action> to; //Action that can be started from this Action's chainState
    private final RegistryObject<? extends Action> conditionalTo; //Conditional chain action that can be started from this Action's chainstate
    private final Function<LivingEntity, Boolean> conditionalFunction; //Returns true when conditional chain should override default chain
    private final Supplier<SoundEvent> sound;
    private final Supplier<SoundEvent> extraSound;
    public final TagKey<Block> harvestableBlocks;
    private final float knockback;

    public Action(int... duration) {
        this.chainState = -1;
        this.from = ActionsNF.EMPTY;
        this.to = ActionsNF.EMPTY;
        this.conditionalTo = ActionsNF.EMPTY;
        this.conditionalFunction = null;
        this.chargeState = -1;
        this.interruptible = false;
        this.looping = false;
        this.freeze = false;
        this.sprinting = false;
        this.crawling = false;
        this.duration = duration;
        this.sound = () -> null;
        this.extraSound = () -> null;
        this.harvestableBlocks = null;
        this.knockback = 0F;
    }

    public Action(Properties properties, int... duration) {
        this.chainState = properties.chainState;
        this.from = properties.from;
        this.to = properties.to;
        this.conditionalTo = properties.conditionalTo;
        this.conditionalFunction = properties.conditionalFunction;
        this.chargeState = properties.chargeState;
        this.interruptible = properties.interruptible;
        this.looping = properties.looping;
        this.freeze = properties.freeze;
        this.sprinting = properties.sprinting;
        this.crawling = properties.crawling;
        this.duration = duration;
        this.sound = properties.sound;
        this.extraSound = properties.extraSound;
        this.harvestableBlocks = properties.harvestableBlocks;
        this.knockback = properties.knockback;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Action action) return action.getRegistryName().equals(this.getRegistryName());
        else return false;
    }

    @Override
    public String toString() {
        if(getRegistryName() == null) return super.toString();
        return getRegistryName().getPath();
    }

    public void init() {
        addToSet(from);
        addToSet(to);
        addToSet(conditionalTo);
    }

    private void addToSet(RegistryObject<? extends Action> object) {
        if(object.getId().equals(ActionsNF.EMPTY.getId()) || linkedActions.contains(object.getId())) return;
        linkedActions.add(object.getId());
        Action action = object.get();
        if(!ActionsNF.isEmpty(action.chainsFrom().getId())) addToSet(action.chainsFrom());
        if(!ActionsNF.isEmpty(action.chainsTo().getId())) addToSet(action.chainsTo());
        if(!ActionsNF.isEmpty(action.conditionalChainsTo().getId())) addToSet(action.conditionalChainsTo());
    }

    public boolean is(TagKey<Action> tag) {
        return ActionsNF.isTagged(this, tag);
    }

    public boolean isEmpty() {
        return ActionsNF.isEmpty(this.getRegistryName());
    }

    public boolean canStart(LivingEntity user) {
        IActionTracker capA = ActionTracker.get(user);
        if(capA.isInactive()) return true;
        if(capA.isStunned()) return false;
        Action action = capA.getAction();
        if(this.isActionEqualOrLinked(capA.getActionID())) {
            //Some lenience is provided to account for inherent desync (real) in singleplayer and for players with unstable connections
            if(capA.getState() == action.getTotalStates() - 1 && capA.getFrame() >= capA.getDuration() - 3) return true; //End state lenience
            if(action.hasAnyChain() && (capA.getState() == action.getChainState() ||
                    (capA.getState() == action.getChainState() - 1 && capA.getFrame() >= capA.getDuration() - 3))) return true; //Before chain state lenience
            return capA.getState() == action.getChargeState() || (capA.getState() == action.getChargeState() - 1 && capA.getFrame() >= capA.getDuration() - 3); //Before charge state lenience
        }
        else return capA.getState() == action.getTotalStates() - 1 && capA.getFrame() >= capA.getDuration() - 3; //End state lenience
    }

    /**
     * @return whether charge should be allowed to continue once it is started
     */
    public boolean canContinueCharging(LivingEntity user) {
        return true;
    }

    /**
     * @return true if the state is a damage state (for actions this is only used for block interactions)
     */
    public boolean isStateDamaging(int state) {
        return false;
    }

    public boolean isDamaging(IActionTracker capA) {
        return isStateDamaging(capA.getState());
    }

    public int getBlockHitFrame(int state, LivingEntity user) {
        return Math.max(1, getDuration(state, user)/2 + (getDuration(state, user) % 2 == 0 ? 0 : 1));
    }

    /**
     * Maximum distance away from target that NPC should start this action from.
     */
    public double getMaxDistToStart(LivingEntity user) {
        return 1000;
    }

    public boolean canHarvest() {
        return harvestableBlocks != null;
    }

    public boolean canHarvest(BlockState state) {
        if(harvestableBlocks == null) return false;
        return state.is(harvestableBlocks);
    }

    public float getKnockback() {
        return knockback;
    }

    public int getDuration(int state, LivingEntity user) {
        if(state < 0 || state >= duration.length) {
            Nightfall.LOGGER.warn("Action " + this.getRegistryName() + " failed to find state " + state + " on getDuration.");
            return 1;
        }
        int finalDuration = (int) (duration[state] * ActionTracker.get(user).getSpeedMultiplier());
        if(duration[state] % 2 != finalDuration % 2) finalDuration++; //Hitstop usually tries for center of animation, so preserve evenness of frames
        return Mth.clamp(finalDuration, 1, 9999);
    }

    public int[] getDurationArray() {
        return duration.clone();
    }

    public int getTotalStates() {
        return duration.length;
    }

    /**
     * @return minimum charge necessary to voluntarily move from the charge state
     */
    public int getRequiredCharge(LivingEntity user) {
        return chargeState == -1 ? 0 : duration[chargeState]/2;
    }

    public int getMaxCharge() {
        return chargeState == -1 ? -1 : duration[chargeState];
    }

    public int getChargeTimeout() {
        return getMaxCharge() * 3;
    }

    public boolean hasChargeZoom() {
        return true;
    }

    /**
     * @return progress of charge to maximum, ranges from 0 to 1
     */
    public float getChargeProgress(int charge, float chargePartial) {
        return charge > getMaxCharge() ? 1 : Math.min(1, AnimationUtil.interpolate(Math.max(0, charge - 1), charge, chargePartial) / getMaxCharge());
    }

    /**
     * @return State where action can be chained into another (this is also the state that animation ends if this action started a chain)
     */
    public int getChargeState() {
        return chargeState;
    }

    public boolean isChargeable() {
        return getMaxCharge() > 0 && chargeState < getTotalStates();
    }

    public int getChainState() {
        return chainState;
    }

    public boolean hasDefaultChain() {
        return chainState > -1 && chainState < getTotalStates() && to != null;
    }

    public boolean hasConditionalChain() {
        return chainState > -1 && chainState < getTotalStates() && conditionalTo != null && conditionalFunction != null;
    }

    public boolean hasAnyChain() {
        return hasDefaultChain() || hasConditionalChain();
    }

    public boolean isConditionalChainSatisfied(LivingEntity user) {
        return hasConditionalChain() && conditionalFunction.apply(user);
    }

    /**
     * @return conditional chain if satisfied, default chain otherwise
     */
    public RegistryObject<? extends Action> getChain(LivingEntity user) {
        if(isConditionalChainSatisfied(user)) return conditionalChainsTo();
        return chainsTo();
    }

    /**
     * @return Action that can be started from this Action's chainState, empty if none
     */
    public RegistryObject<? extends Action> chainsTo() {
        return to;
    }

    public RegistryObject<? extends Action> conditionalChainsTo() {
        return conditionalTo;
    }

    /**
     * @return Action that preceded this Action, empty if none
     */
    public RegistryObject<? extends Action> chainsFrom() {
        return from;
    }

    /**
     * @return whether this action can be cancelled by another action
     */
    public boolean isInterruptible() {
        return interruptible;
    }

    /**
     * @return whether this action is idle and will repeat upon completion
     */
    public boolean isIdle() {
        return looping;
    }

    /**
     * @return whether this action should freeze upon completion
     */
    public boolean shouldFreeze() {
        return freeze;
    }

    public Supplier<SoundEvent> getSound() {
        return sound;
    }

    public Supplier<SoundEvent> getExtraSound() {
        return extraSound;
    }

    /**
     * @return whether sprinting is allowed while active
     */
    public boolean allowSprinting() {
        return sprinting;
    }

    /**
     * @return whether poses other than standing or crouching are allowed while active
     */
    public boolean allowCrawling() {
        return crawling;
    }

    public boolean allowStaminaRegen(int state) {
        return true;
    }

    public boolean allowDodging(int state) {
        return true;
    }

    public boolean isActionEqualOrLinked(ResourceLocation id) {
        if(id.equals(this.getRegistryName())) return true;
        return linkedActions.contains(id);
    }

    public void transformModel(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        transformModelFull(state, frame, duration, charge, pitch, user, data, mCalc);
        if(user instanceof Player player && PlayerData.get(player).getActiveHand() == InteractionHand.OFF_HAND) {
            for(AnimationData d : data.values()) d.mirrorAcrossY();
            mCalc.scale(1, -1, -1);
        }
    }

    private void transformModelFull(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {
        if(state == 0 && !ActionsNF.isEmpty(chainsFrom().getId())) {
            Action last = chainsFrom().get();
            //End at chain state of last action
            if(!(last instanceof Attack lastAttack) || data.keySet().containsAll(lastAttack.modelKeys)) {
                last.transformModelFull(last.getChainState(), DEFAULT_DURATION, DEFAULT_DURATION, charge, last.getPitch(user, mCalc.partialTicks), user, data, mCalc);
            }
        }
        if(state != 0) {
            //Previous states in this action
            transformModelFull(state - 1, DEFAULT_DURATION, DEFAULT_DURATION, charge, pitch, user, data, mCalc);
        }
        //Reset durations/offsets
        for(AnimationData d : data.values()) {
            d.resetLengths(duration, Easing.inOutSine);
        }
        mCalc.resetLength(duration, Easing.inOutSine);
        //Current state for this action
        transformModelSingle(state, frame, duration, charge, pitch, user, data, mCalc);
    }

    /**
     * Common transformations for the model and user matrix, server is typically used for calculating collision
     * @param data map of data to be transformed, expected to be retrieved from {@link Attack#getAnimationData(LivingEntity, IActionTracker)} on the server
     *            and {@link AnimatedModel#getDataFromModel()} or {@link PlayerModelNF#getDataFromModel()} on the client
     * @param mCalc y rotation value for entity matrix, setting x or z will have no effect (this technically works for animation but isn't very useful
     *              since the entity rotates around the top center and would require an additional rotation for collision)
     */
    protected void transformModelSingle(int state, int frame, int duration, float charge, float pitch, LivingEntity user, EnumMap<EntityPart, AnimationData> data, AnimationCalculator mCalc) {

    }

    public void transformLayer(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {
        if(state == 0 && !ActionsNF.isEmpty(chainsFrom().getId())) {
            Action last = chainsFrom().get();
            //End at chain state of last action
            last.transformLayer(last.getChainState(), DEFAULT_DURATION, DEFAULT_DURATION, charge, user, data);
        }
        if(state != 0) {
            //Previous states in this action
            this.transformLayer(state - 1, DEFAULT_DURATION, DEFAULT_DURATION, charge, user, data);
        }
        //Reset duration/offset
        data.resetLengths(duration, Easing.inOutSine);
        transformLayerSingle(state, frame, duration, charge, user, data);
    }

    /**
     * Common transformations for a layer
     * This should only be used for layers separated from the model, ex. held items
     * @param data new AnimationData synced with user's ActionTracker
     */
    protected void transformLayerSingle(int state, int frame, int duration, float charge, LivingEntity user, AnimationData data) {

    }

    public float getPitch(LivingEntity user, float partialTicks) {
        return user.getViewXRot(partialTicks);
    }

    /**
     * @return maximum allowance of pitch change during state
     */
    public float getMaxXRot(int state) {
        return 32768;
    }

    /**
     * @return maximum allowance of yaw change during state
     */
    public float getMaxYRot(int state) {
        return 32768;
    }

    /**
     * Client only!
     *
     * @return tooltip to display on associated items
     */
    public List<Component> getTooltips(ItemStack stack, @Nullable Level level, TooltipFlag isAdvanced) {
        return new ObjectArrayList<>();
    }

    /**
     * Called when the left mouse button is pressed on client.
     * @param player client player executing action
     */
    public void onAttackInput(Player player) {}

    /**
     * Called when the right mouse button is pressed on client.
     * @param player client player executing action
     */
    public void onUseInput(Player player) {}

    /**
     * Called right after this action is started on client & server.
     * @param user user executing action
     */
    public void onStart(LivingEntity user) {

    }

    /**
     * Called right before another action is started (does not mean this action completed) on client & server.
     * @param user user executing action
     */
    public void onEnd(LivingEntity user) {

    }

    /**
     * Called right before a chain is started on client & server.
     * @param user user executing action
     */
    public void onChainStart(LivingEntity user) {

    }

    /**
     * Called right before moving from the charge state on client & server.
     * @param user user executing action
     */
    public void onChargeRelease(LivingEntity user) {

    }


    /**
     * Called every tick while this action is being used on client & server.
     * @param user user executing action
     */
    public void onTick(LivingEntity user) {

    }

    /**
     * Called when an attack is initially received on the server.
     * @param user user executing action
     * @param damage raw damage (armor/effects not yet applied)
     * @return modified damage (if <= 0, attack will be canceled)
     */
    public float onAttackReceived(LivingEntity user, DamageTypeSource source, float damage) {
        return damage;
    }

    /**
     * Called when damage is initially received on the server.
     * @param user user executing action
     * @param damage raw damage (armor/effects not yet applied)
     * @return modified damage
     */
    public float onDamageReceived(LivingEntity user, DamageTypeSource source, float damage) {
        return damage;
    }

    public static class Properties {
        private int chargeState = -1;
        private int chainState = -1;
        private RegistryObject<? extends Action> from = ActionsNF.EMPTY;
        private RegistryObject<? extends Action> to = ActionsNF.EMPTY;
        private RegistryObject<? extends Action> conditionalTo = ActionsNF.EMPTY;
        private Function<LivingEntity, Boolean> conditionalFunction;
        private boolean interruptible = false;
        private boolean looping = false;
        private boolean freeze = false;
        private boolean sprinting = false;
        private boolean crawling = false;
        private Supplier<SoundEvent> sound = () -> null;
        private Supplier<SoundEvent> extraSound = () -> null;
        private TagKey<Block> harvestableBlocks;
        private float knockback;

        public Action.Properties setKnockback(float knockback) {
            this.knockback = knockback;
            return this;
        }

        public Action.Properties setSound(Supplier<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        public Action.Properties setExtraSound(Supplier<SoundEvent> sound) {
            this.extraSound = sound;
            return this;
        }

        public Action.Properties setInterruptible() {
            this.interruptible = true;
            return this;
        }

        public Action.Properties setIdle() {
            this.looping = true;
            return this;
        }

        public Action.Properties setFreeze() {
            this.freeze = true;
            return this;
        }

        public Action.Properties setSprinting() {
            this.sprinting = true;
            return this;
        }

        public Action.Properties setCrawling() {
            this.crawling = true;
            return this;
        }

        public Action.Properties setChainState(int state) {
            if(state < 0) Nightfall.LOGGER.error("Tried to set Action chain state to negative value.");
            this.chainState = state;
            return this;
        }

        public Action.Properties setChargeState(int state) {
            if(state < 0) Nightfall.LOGGER.error("Tried to set Action charge state to negative value.");
            this.chargeState = state;
            return this;
        }

        public Action.Properties setChainFrom(RegistryObject<? extends Action> id) {
            this.from = id;
            return this;
        }

        public Action.Properties setChainTo(RegistryObject<? extends Action> id) {
            this.to = id;
            return this;
        }

        public Action.Properties setConditionalChainTo(RegistryObject<? extends Action> id) {
            this.conditionalTo = id;
            return this;
        }

        public Action.Properties setConditionalChainFunction(Function<LivingEntity, Boolean> conditionalFunction) {
            this.conditionalFunction = conditionalFunction;
            return this;
        }

        public Action.Properties setHarvestable(TagKey<Block> harvestable) {
            this.harvestableBlocks = harvestable;
            return this;
        }
    }
}
