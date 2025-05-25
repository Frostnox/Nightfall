package frostnox.nightfall.event;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.EntityLightEngine;
import frostnox.nightfall.client.gui.screen.encyclopedia.EncyclopediaScreen;
import frostnox.nightfall.client.gui.screen.encyclopedia.EntryPuzzleScreen;
import frostnox.nightfall.client.gui.screen.item.ModifiableItemScreen;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.data.recipe.BuildingRecipe;
import frostnox.nightfall.entity.IEntityWithItem;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.item.client.IModifiable;
import frostnox.nightfall.item.item.BuildingMaterialItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.GenericToServer;
import frostnox.nightfall.network.message.capability.ActionToServer;
import frostnox.nightfall.network.message.capability.ModifiableIndexToServer;
import frostnox.nightfall.network.message.entity.DodgeToServer;
import frostnox.nightfall.network.message.entity.HitTargetToServer;
import frostnox.nightfall.network.message.world.DigBlockToServer;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.TimedValue;
import frostnox.nightfall.util.math.BoundingSphere;
import frostnox.nightfall.util.math.Mat4f;
import frostnox.nightfall.util.math.Quat;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.inventory.PlayerInventoryContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

import static frostnox.nightfall.util.CombatUtil.DODGE_PENALTY_TICK;
import static frostnox.nightfall.util.CombatUtil.DODGE_STAMINA_COST;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {
    //Click
    private static final int EMPTY = -1, LEFT = 0, RIGHT = 1;
    private static int click = EMPTY, clickCounter = 0;
    private static boolean flagAttack = false, flagUse = false;

    //Buffers & counters
    private enum ActionType {
        EMPTY, BASIC, ALTERNATE, TECHNIQUE, CRAWL
    }
    private static final TimedValue<ActionType> actionBuffer = new TimedValue<>(ActionType.EMPTY, 7);
    private static final TimedValue<Integer> hotbarBuffer = new TimedValue<>(-1, 10);
    private static final TimedValue<Boolean> crawlJumpBuffer = new TimedValue<>(false, 10);
    private static final TimedValue<Boolean> dodgeBuffer = new TimedValue<>(false, 7);
    private static final boolean[] heldKeys = new boolean[4];
    private static int modifyCounter = 0;
    private static long lastSprintTime = 0L;

    //Dodging
    private static final double DODGE_FORCE = 0.8D;
    private static double xDodge;
    private static double zDodge;
    private static boolean triedJump;

    //Misc
    private static boolean skipClickInputEvent = false;
    private static boolean wasAttackKeyDown = false;
    private static boolean retryPickBlock = false;
    private static int tempFood;
    private static boolean disabledSprint;

    private static void updateHand(Player player) {
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        //Set hand to use if starting from idle
        if(capA.isInactive()) {
            if(ClientEngine.get().keyOffhand.isDown() && !player.getOffhandItem().isEmpty()) {
                if(capP.isMainhandActive()) {
                    NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.ACTIVATE_OFFHAND));
                    clickCounter = 0;
                }
                capP.setOffhandActive();
            }
            else {
                if(!capP.isMainhandActive()) {
                    NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.ACTIVATE_MAINHAND));
                    clickCounter = 0;
                }
                capP.setMainhandActive();
            }
        }
    }

    private static void sendServerAction(IActionTracker capA, IPlayerData capP) {
        if(capA.isQueued()) NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.QUEUE_ACTION_TRACKER));
        else NetworkHandler.toServer(new ActionToServer(capP.isMainhandActive(), capA.getActionID()));
    }

    private static void updateClickInput(boolean fromClick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive() || player.isUsingItem() || player.isHandsBusy() || mc.isPaused()) return;
        updateHotbarBuffer(player); //Update hotbar buffer in case item was going to switch
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        if((capA.isStunned() && capA.getStunFrame() == 1)) {
            actionBuffer.reset();
            clickCounter = 0;
        }
        if(modifyCounter <= 0) updateHand(player);
        ItemStack itemStack = player.getItemInHand(capP.getActiveHand());
        if(mc.options.keyAttack.isDown() || flagAttack) click = LEFT;
        else if(mc.options.keyUse.isDown() || flagUse) click = RIGHT;
        else if(clickCounter == 0 && itemStack.getItem() instanceof IWeaponItem) click = EMPTY;
        if(itemStack.getItem() instanceof IWeaponItem weapon) {
            /*
            Avoid randomly skipping animation on techniques with fast animations on first state
            Animation skips happen when click occurs and partialTick is near 1
            */
            if(fromClick && flagUse && weapon.getActionSet(player).defaultTech.get().getChargeState() == 0) return;
        }
        else if(!(itemStack.getItem() instanceof IActionableItem)) actionBuffer.reset();
        if((click == EMPTY && (mc.options.keyAttack.isDown() || mc.options.keyUse.isDown())) ||
                (click == LEFT ? mc.options.keyAttack.isDown() : mc.options.keyUse.isDown()) || fromClick) {
            IWeaponItem weapon;
            if(!(player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem)) {
                if(fromClick && click == LEFT) {
                    if(capA.isCharging()) {
                        capA.queue();
                        NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.QUEUE_ACTION_TRACKER));
                    }
                    clickCounter = 0;
                }
                if(fromClick && click == LEFT && !(player.getOffhandItem().getItem() instanceof IActionableItem)) {
                    capP.setMainhandActive();
                    NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.ACTIVATE_MAINHAND));
                    if(player.getMainHandItem().getItem() instanceof IWeaponItem) weapon = (IWeaponItem) player.getMainHandItem().getItem();
                    else return;
                }
                else {
                    if(itemStack.getItem() instanceof IActionableItem) {
                        if(!fromClick && click == LEFT && itemStack.getItem() instanceof IWeaponItem) clickCounter++;
                        if(click == LEFT && !capA.isInactive() && player.getItemInHand(capP.getOppositeActiveHand()).getItem() instanceof IWeaponItem) {
                            actionBuffer.set(clickCounter > 3 ? ActionType.ALTERNATE : ActionType.BASIC);
                        }
                    }
                    return;
                }
            }
            else weapon = (IWeaponItem) player.getItemInHand(capP.getActiveHand()).getItem();
            if(mc.screen == null && (!fromClick || clickCounter == 0)) {
                if(click == LEFT && wasAttackKeyDown && player.swinging && fromClick) actionBuffer.set(ActionType.BASIC);
                if(click == LEFT || !player.swinging) clickCounter++;
                /*else if(click == LEFT) {
                    buffer = BASIC;
                    bufferCounter = 0;
                }*/
            }
            if(click == LEFT && (player.getPose() == Pose.SWIMMING || player.getPose() == Pose.FALL_FLYING)) {
                if(weapon.tryCrawlingAttack(player)) sendServerAction(capA, capP);
                else if(!crawlJumpBuffer.getValue() && capP.isCrawling()) actionBuffer.set(ActionType.CRAWL);
            }
            else if(clickCounter > 3 && click == LEFT) {
                if(capA.isInactive()) {
                    if(weapon.tryAlternateAttack(player)) sendServerAction(capA, capP);
                    //else buffer = ALTERNATE;
                }
                else if(!mc.options.keyUse.isDown()) {
                    if(capA.getActionID().equals(weapon.getActionSet(player).defaultTech.getId()) && !capA.isInactive()) {
                        if(weapon.tryTechnique(player)) sendServerAction(capA, capP);
                    }
                    else if(weapon.tryBasicAttack(player)) sendServerAction(capA, capP);
                    //else buffer = BASIC;
                }
                //if(!mc.options.keyAttack.isDown() && !mc.options.keyUse.isDown()) clickCounter = 0;
            }
            else if(clickCounter > 1 && click == RIGHT) {
                if(capA.isInactive()) {
                    if(!player.swinging) {
                        skipClickInputEvent = true;
                        Minecraft.getInstance().startUseItem();
                        skipClickInputEvent = false;
                    }
                    if(!player.swinging && weapon.tryTechnique(player)) sendServerAction(capA, capP);
                    else if(!player.swinging) actionBuffer.set(ActionType.TECHNIQUE);
                }
                else {
                    if(weapon.tryAlternateAttack(player)) sendServerAction(capA, capP);
                    else actionBuffer.set(ActionType.TECHNIQUE);
                }
                //if(!mc.options.keyAttack.isDown() && !mc.options.keyUse.isDown()) clickCounter = 0;
            }
        }
        else {
            Action action = capA.getAction();
            IWeaponItem weapon;
            if(!(player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem)) {
                if(click == LEFT && !(player.getOffhandItem().getItem() instanceof IActionableItem) && player.getMainHandItem().getItem() instanceof IWeaponItem) {
                    weapon = (IWeaponItem) player.getMainHandItem().getItem();
                    capP.setMainhandActive();
                    NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.ACTIVATE_MAINHAND));
                }
                else {
                    if(capA.isCharging()) {
                        capA.queue();
                        NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.QUEUE_ACTION_TRACKER));
                    }
                    return;
                }
            }
            else weapon = (IWeaponItem) player.getItemInHand(capP.getActiveHand()).getItem();
            if(capA.getState() == action.getChargeState()) {
                if(capA.getActionID().equals(weapon.getActionSet(player).defaultTech.getId()) || (weapon.getActionSet(player).recipeAction != null && capA.getActionID().equals(weapon.getActionSet(player).recipeAction.getId()))) {
                    //if(click == LEFT) buffer = BASIC;
                    if(weapon.tryTechnique(player)) NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.QUEUE_ACTION_TRACKER));
                    //else buffer = TECHNIQUE;
                }
                else {
                    if(weapon.tryAlternateAttack(player)) NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.QUEUE_ACTION_TRACKER));
                    //else buffer = ALTERNATE;
                }
            }
            else if(clickCounter > 0 && !player.swinging) {
                if(click == LEFT && actionBuffer.getValue() != ActionType.CRAWL) {
                    if(weapon.tryBasicAttack(player)) sendServerAction(capA, capP);
                    else actionBuffer.set(ActionType.BASIC);
                }
                else if(click == RIGHT) {
                    if((capA.isInactive() || clickCounter > 1) && weapon.tryTechnique(player)) sendServerAction(capA, capP);
                    else actionBuffer.set(ActionType.TECHNIQUE);
                }
            }
            clickCounter = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || mc.isPaused()) return;
        if(!player.isAlive()) {
            ClientEngine.get().lastDashTick = -100;
            return;
        }
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        ClientLevel level = player.clientLevel;
        if(event.phase == TickEvent.Phase.START) {
            tempFood = player.foodData.getFoodLevel();
            player.foodData.setFoodLevel(20); //Max food temporarily so sprint doesn't get canceled
            if(LevelUtil.disallowPlayerSprint(player) && !player.hasEffect(MobEffects.BLINDNESS)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS));
                disabledSprint = true;
            }
            ClientEngine.get().tickStart();
            EntityLightEngine.get().tickStart();
            retryPickBlock = true;

            //Input buffers
            crawlJumpBuffer.tick();
            if(crawlJumpBuffer.isActive() && capA.isInactive() && (player.getPose() == Pose.SWIMMING || player.getPose() == Pose.FALL_FLYING)) {
                capP.setCrawling(false);
                NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.STOP_CRAWLING));
                crawlJumpBuffer.reset();
                if(actionBuffer.getValue() == ActionType.CRAWL) actionBuffer.reset();
            }

            dodgeBuffer.tick();
            if(dodgeBuffer.isActive() && !capA.isStunned() && checkDodgeConditions(player)) {
                handleDodge(player);
            }

            hotbarBuffer.tick();
            updateHotbarBuffer(player);

            actionBuffer.tick();
            if(actionBuffer.isActive() && capA.getAction().getChargeState() != capA.getState() && !player.swinging && player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem weapon) {
                switch(actionBuffer.getValue()) {
                    case BASIC -> {
                        if(weapon.tryBasicAttack(player)) {
                            sendServerAction(capA, capP);
                            actionBuffer.reset();
                        }
                    }
                    case ALTERNATE -> {
                        if(weapon.tryAlternateAttack(player)) {
                            sendServerAction(capA, capP);
                            actionBuffer.reset();
                        }
                    }
                    case TECHNIQUE -> {
                        if(!player.swinging && capA.isInactive()) {
                            skipClickInputEvent = true;
                            Minecraft.getInstance().startUseItem();
                            skipClickInputEvent = false;
                        }
                        else if(weapon.getActionSet(player).defaultTech.get().getChargeState() != 0 && weapon.tryTechnique(player)) {
                            sendServerAction(capA, capP);
                            actionBuffer.reset();
                        }
                    }
                    case CRAWL -> {
                        if(weapon.tryCrawlingAttack(player)) {
                            sendServerAction(capA, capP);
                            actionBuffer.reset();
                        }
                    }
                }
            }

            //Reset if weapon was lost
            if(!capA.isInactive() && player.getItemInHand(capP.getActiveHand()).isEmpty()) {
                resetClick();
                actionBuffer.reset();
            }
        }
        else {
            player.foodData.setFoodLevel(tempFood); //Restore food level after vanilla logic is done
            if(disabledSprint) {
                player.removeEffect(MobEffects.BLINDNESS);
                disabledSprint = false;
            }
            ClientEngine.get().tickEnd();
            
            if(mc.player.tickCount - capP.getLastDodgeTick() == 1) {
                doPlayerDodge(mc.player);
            }
            else if(mc.player.tickCount - capP.getLastDodgeTick() == 2) {
                Vec3 motion = mc.player.getDeltaMovement();
                mc.player.lerpMotion(xDodge * DODGE_FORCE * 0.25D, motion.y, zDodge * DODGE_FORCE * 0.25D);
            }
            
            capP.tickStamina();
            if(capA.isStunned()) {
                if(mc.screen instanceof AbstractContainerScreen) {
                    mc.screen.onClose();
                }
                //while(mc.options.keyShift.consumeClick());
                //mc.options.keyShift.setDown(false);
                mc.options.keySprint.setDown(false);
                //mc.options.keyJump.setDown(false);
                if(capA.getStunFrame() == 0) {
                    mc.options.keyAttack.setDown(false);
                    mc.options.keyUse.setDown(false);
                }
                //Keys.KeyDodge.setDown(false);
            }

            if(mc.options.keyJump.isDown() && !player.getAbilities().flying && capA.isInactive() && capP.getStamina() > 0F
                    && !player.onClimbable() && (player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING) && !player.isOnGround()
                    && !player.isInWater() && !player.isInLava() && (capP.isClimbing() || (player.horizontalCollision && player.getDeltaMovement().y() <= 0.0))
                    && player.tickCount - capP.getLastDodgeTick() > 2 && !player.isUsingItem()) {
                BoundingSphere sphere1 = new BoundingSphere(0, (player.getPose() == Pose.CROUCHING ? 0.2F :0.1F), 0.3F, 0.45F);
                BoundingSphere sphere2 = new BoundingSphere(0, -(player.getPose() == Pose.CROUCHING ? 0.1F : 0.3F), 0.3F, 0.45F);
                BoundingSphere sphere3 = new BoundingSphere(0, -(player.getPose() == Pose.CROUCHING ? 0.45F : 0.8F), 0.3F, 0.45F);
                BoundingSphere sphere4 = new BoundingSphere(0, -(player.getPose() == Pose.CROUCHING ? 0.75F : 1.2F), 0.3F, 0.45F);
                Mat4f userMatrix1 = new Mat4f(new Quat(player.getViewYRot(1F), Vector3f.YP, true));
                sphere1.transform(userMatrix1);
                sphere2.transform(userMatrix1);
                sphere3.transform(userMatrix1);
                sphere4.transform(userMatrix1);
                sphere1.xPos += player.getX();
                sphere2.xPos += player.getX();
                sphere3.xPos += player.getX();
                sphere4.xPos += player.getX();
                sphere1.yPos += player.getEyeY();
                sphere2.yPos += player.getEyeY();
                sphere3.yPos += player.getEyeY();
                sphere4.yPos += player.getEyeY();
                sphere1.zPos += player.getZ();
                sphere2.zPos += player.getZ();
                sphere3.zPos += player.getZ();
                sphere4.zPos += player.getZ();
                Vector3d hitCoords = new Vector3d(0, -1, 0);
                double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) * 10;
                if(sphere1.isSpaceClimbable(player, hitCoords)) {
                    Vec3 velocity = player.getDeltaMovement();
                    double y;
                    if(velocity.y < 1 && velocity.y > -1D) {
                        capP.sendClimbPosition(hitCoords);
                        if(!capP.isClimbing()) {
                            capP.setClimbing(true);
                            NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.START_CLIMBING));
                        }
                        if(player.horizontalCollision) {
                            if(LevelUtil.isPositionFullyClimbable(player, hitCoords)) y = 0.1D;
                            else y = 0.07D;
                        }
                        else y = (LevelUtil.isPositionFullyClimbable(player, hitCoords) && !player.isCrouching()) ? -0.1D : 0.0D;
                        player.setDeltaMovement(velocity.x * 0.9, y * speed, velocity.z * 0.9);
                    }
                }
                else if(sphere2.isSpaceClimbable(player, hitCoords)) {
                    Vec3 velocity = player.getDeltaMovement();
                    double y = velocity.y;
                    if(velocity.y < 1 && velocity.y > -1D) {
                        capP.sendClimbPosition(hitCoords);
                        if(!capP.isClimbing()) {
                            capP.setClimbing(true);
                            NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.START_CLIMBING));
                        }
                        if(player.horizontalCollision) {
                            if(LevelUtil.isPositionFullyClimbable(player, hitCoords)) y = 0.1D;
                            else y = sphere1.isSpaceObstructed(player, new BlockPos(hitCoords.x, hitCoords.y + 1, hitCoords.z)) ? 0 : 0.07D;
                        }
                        else y = (LevelUtil.isPositionFullyClimbable(player, hitCoords) && !player.isCrouching()) ? Math.max(y + 0.0784D - 0.1D, -0.1D) : Math.max(y + 0.0784D - 0.025D, -0.025D);
                        player.setDeltaMovement(velocity.x * 0.9, y * speed, velocity.z * 0.9);
                    }
                }
                else if(capP.isClimbing() && (sphere3.isSpaceClimbable(player, hitCoords) || sphere4.isSpaceClimbable(player, hitCoords))) {
                    Vec3 velocity = player.getDeltaMovement();
                    double y = velocity.y;
                    if(velocity.y < 1 && velocity.y > -1D) {
                        capP.sendClimbPosition(hitCoords);
                        if(player.horizontalCollision) {
                            double xSize = player.getBoundingBox().getXsize() / 2;
                            double ySize = player.getBoundingBox().getYsize() - 0.01D;
                            double zSize = player.getBoundingBox().getZsize() / 2;
                            Iterable<VoxelShape> collisions = level.getBlockCollisions(null, new AABB(hitCoords.x - xSize, hitCoords.y, hitCoords.z - zSize, hitCoords.x + xSize, hitCoords.y + ySize, hitCoords.z + zSize));
                            y = collisions.iterator().hasNext() ? 0.07D : 0.14D;
                        }
                        else y = Math.max(y + 0.0784D - 0.05D, -0.05D);
                        player.setDeltaMovement(velocity.x * 0.9, y * speed, velocity.z * 0.9);
                    }
                }
                else {
                    capP.setClimbing(false);
                    NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.STOP_CLIMBING));
                }
            }
            else if(capP.isClimbing()) {
                capP.setClimbing(false);
                NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.STOP_CLIMBING));
            }
            if(!capP.isClimbing() && capP.getClimbTicks() <= 0) capP.setClimbPosition(new Vector3d(0, -1, 0));
            if(player.isVisuallyCrawling() && !mc.isPaused()) player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y * 1.1722446D, player.getDeltaMovement().z);

            updateClickInput(false);
            ItemStack stack = player.getItemInHand(capP.getActiveHand());
            if(modifyCounter > 0) {
                if(stack.getItem() instanceof IModifiable modifiable) {
                    Optional<Screen> screen = modifiable.modifyContinueClient(mc, stack, player, capP.getActiveHand(), modifyCounter);
                    if(screen.isPresent()) mc.setScreen(screen.get());
                }
                modifyCounter++;
            }

            Action action = capA.getAction();
            //Client-side interactions with world
            if(action instanceof IClientAction clientAction) clientAction.onClientTick(player);
            //Attack collision and block breaking
            if(capA.isDamaging() && capP.getHitStopFrame() == -1) {
                if(stack.getItem() instanceof IActionableItem item) {
                    if(action instanceof Attack attack) {
                        if(item.hasAction(capA.getActionID(), player) && capA.getLivingEntitiesHit() < attack.getMaxTargets()) {
                            boolean firstHit = capA.getHitEntities().isEmpty();
                            List<HitData> targets = capA.getEntitiesInAttack(attack, 0.5F);
                            if(capA.getLivingEntitiesHit() < attack.getMaxTargets()) targets.addAll(capA.getEntitiesInAttack(attack, 1F));
                            if(!targets.isEmpty()) {
                                if(firstHit) capA.setHitPause(0F);
                                for(HitData hitData : targets) NetworkHandler.toServer(new HitTargetToServer(hitData));
                            }
                        }
                    }
                    if(action.canHarvest() && ClientEngine.get().microHitResult == null && capA.getLivingEntitiesHit() == 0) {
                        if(!capP.hasDugBlock() && capA.getFrame() == action.getBlockHitFrame(capA.getState(), player) && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos center = ((BlockHitResult)mc.hitResult).getBlockPos();
                            boolean canMineAny = !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_SICKLE) && !action.harvestableBlocks.equals(TagsNF.MINEABLE_WITH_DAGGER) && !action.harvestableBlocks.equals(BlockTags.MINEABLE_WITH_AXE);
                            if(canMineAny || action.canHarvest(level.getBlockState(center))) {
                                capP.setDugBlock(true);
                                boolean facingX = player.getDirection().getAxis().equals(Direction.Axis.X);
                                int xRange = (stack.is(TagsNF.SICKLE) && (!action.isChargeable() || !facingX)) ? 1 : 0;
                                int zRange = (stack.is(TagsNF.SICKLE) && (!action.isChargeable() || facingX)) ? 1 : 0;
                                int yRange = (stack.is(TagsNF.SICKLE) && action.isChargeable()) ? 1 : 0;
                                for(BlockPos pos : BlockPos.betweenClosed(center.getX() - xRange, center.getY() - yRange, center.getZ() - zRange, center.getX() + xRange, center.getY() + yRange, center.getZ() + zRange)) {
                                    BlockState block = level.getBlockState(pos);
                                    if(canMineAny || action.canHarvest(block)) {
                                        IGlobalChunkData chunkData = GlobalChunkData.get(level.getChunkAt(pos));
                                        float progress = chunkData.getBreakProgress(pos) + level.getBlockState(pos).getDestroyProgress(player, level, pos) *
                                                AttributesNF.getStrengthMultiplier(player) * capA.getChargeDestroyProgressMultiplier();
                                        if(progress >= 1F) {
                                            if(block.onDestroyedByPlayer(level, pos, player, false, block.getFluidState())) {
                                                block.getBlock().destroy(level, pos, block);
                                                ClientEngine.get().visuallyDestroyBlock(pos, -1);
                                                chunkData.removeBreakProgress(pos);
                                            }
                                        }
                                        else {
                                            ClientEngine.get().visuallyDestroyBlock(pos, (int) (progress * 10F) - 1);
                                            chunkData.setBreakProgress(pos.immutable(), progress);
                                        }
                                        if(pos.equals(center)) {
                                            if(progress < 1F && !block.getMaterial().isReplaceable() && !stack.is(TagsNF.NO_HITSTOP)) capP.setHitStopFrame(capA.getFrame());
                                            for(int i = 0; i < player.getRandom().nextInt() % 3 + 6; i++) mc.particleEngine.addBlockHitEffects(pos, (BlockHitResult) mc.hitResult);
                                            SoundType sound = level.getBlockState(pos).getSoundType(level, pos, player);
                                            level.playSound(player, pos, sound.getHitSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2F, sound.getPitch() * 0.75F);
                                            NetworkHandler.toServer(new DigBlockToServer(pos.getX(), pos.getY(), pos.getZ()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //Spawn weather particles
            if(LevelData.isPresent(level) && mc.cameraEntity != null) {
                ILevelData capL = LevelData.get(level);
                if(capL.getGlobalWeatherIntensity() > -Weather.GLOBAL_CLEAR_THRESHOLD) {
                    BlockPos camPos = mc.cameraEntity.eyeBlockPosition();
                    Vec3 camVec = mc.cameraEntity.getEyePosition();
                    int lastSectionX = SectionPos.blockToSectionCoord(camPos.getX());
                    int lastSectionZ = SectionPos.blockToSectionCoord(camPos.getZ());
                    LevelChunk chunk = level.getChunk(lastSectionX, lastSectionZ);
                    if(!chunk.isEmpty()) {
                        IChunkData capC = ChunkData.get(chunk);
                        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                        for(int i = 0; i < (int) (Mth.clamp(capL.getWeatherIntensity(capC, camPos), 0F, 1F) * 250); i++) {
                            double radius = Weather.PARTICLE_SPAWN_RADIUS / 2 + (Weather.PARTICLE_SPAWN_RADIUS / 2 * level.random.nextDouble());
                            float theta = 2 * MathUtil.PI * level.random.nextFloat();
                            float phi = (float) Math.acos(2 * level.random.nextDouble() - 1);
                            float phiSin = Mth.sin(phi);
                            double x = camVec.x + radius * phiSin * Mth.cos(theta);
                            double y = camVec.y + radius * phiSin * Mth.sin(theta);
                            double z = camVec.z + radius * Mth.cos(phi);
                            pos.set(Mth.floor(x), Mth.floor(y), Mth.floor(z));
                            int sectionX = SectionPos.blockToSectionCoord(pos.getX());
                            int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());
                            if(sectionX != lastSectionX || sectionZ != lastSectionZ) {
                                chunk = level.getChunk(sectionX, sectionZ);
                                lastSectionX = sectionX;
                                lastSectionZ = sectionZ;
                                if(chunk.isEmpty()) continue;
                                capC = ChunkData.get(chunk);
                            }
                            if(chunk.isEmpty() || chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() & 15, pos.getZ() & 15) >= pos.getY()) continue;
                            Weather localWeather = capL.getWeather(capC, pos);
                            if(localWeather == Weather.RAIN) {
                                level.addParticle(ParticleTypesNF.RAIN.get(), x, y, z, 0, 0, 0);
                            }
                            else if(localWeather == Weather.SNOW) {
                                if(level.random.nextFloat() < 0.19F) level.addParticle(ParticleTypesNF.SNOW.get(), x, y, z, 0, 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSoundSourceEvent(SoundEvent.SoundSourceEvent event) {
        ResourceLocation loc = event.getSound().getLocation();
        //Stop generic hurt sounds from playing
        if(loc.equals(SoundEvents.PLAYER_HURT.getLocation()) || loc.equals(SoundEvents.PLAYER_DEATH.getLocation())) {
            event.getEngine().soundManager.stop(event.getSound());
        }
    }

    @SubscribeEvent
    public static void onDrawSelectionEvent(DrawSelectionEvent event) {
        if(ClientEngine.get().microHitResult != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent event) {
        int key, action = 0;
        if(event instanceof InputEvent.ClickInputEvent clickEvent) {
            key = clickEvent.getKeyMapping().getKey().getValue();
            if(clickEvent.isAttack() || clickEvent.isUseItem()) {
                Minecraft mc = Minecraft.getInstance();
                if(mc.player != null && !mc.player.getAbilities().instabuild && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                    double reach = Math.max(1, mc.player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() - 2);
                    if(mc.hitResult.getLocation().distanceToSqr(mc.player.getEyePosition(ClientEngine.get().getPartialTick())) > reach * reach) {
                        mc.hitResult = BlockHitResult.miss(mc.hitResult.getLocation(), Direction.UP, BlockPos.ZERO);
                        return;
                    }
                }
            }
        }
        else if(event instanceof InputEvent.KeyInputEvent keyEvent) {
            action = keyEvent.getAction();
            key = keyEvent.getKey();
        }
        else if(event instanceof InputEvent.MouseInputEvent mouseEvent) {
            action = mouseEvent.getAction();
            key = mouseEvent.getButton();
        }
        else return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if(key == GLFW.GLFW_KEY_F3) {
            if(p == null || !p.isAlive() || !p.isCreative()) {
                mc.options.renderDebugCharts = false;
                mc.options.renderFpsChart = false;
            }
            return;
        }
        if (p == null || !p.isAlive()) {
            return;
        }
        IActionTracker capA = ActionTracker.get(p);
        IPlayerData capP = PlayerData.get(p);
        if(key == mc.options.keyAttack.getKey().getValue()) {
            if(mc.options.keyAttack.isDown()) {
                wasAttackKeyDown = action == GLFW.GLFW_PRESS;
            }
            else if(action == GLFW.GLFW_RELEASE) wasAttackKeyDown = false;
        }
        else if(key == ClientEngine.get().keyDash.getKey().getValue() && action != GLFW.GLFW_RELEASE) {
            //0 = forward, 1 = back, 2 = right, 3 = left
            for(int i = 0; i < 4; i++) heldKeys[i] = false;
            if(mc.options.keyUp.isDown()) heldKeys[0] = true;
            if(mc.options.keyDown.isDown()) heldKeys[1] = true;
            if(mc.options.keyRight.isDown()) heldKeys[2] = true;
            if(mc.options.keyLeft.isDown()) heldKeys[3] = true;
            if(!checkBooleanArray(heldKeys)) heldKeys[1] = true;

            if(checkDodgeConditions(p)) {
                if(!((mc.options.keyUp.isDown() && mc.options.keyDown.isDown()) || (mc.options.keyRight.isDown() && mc.options.keyLeft.isDown()))) {
                    if(ActionTracker.get(p).isStunned()) {
                        dodgeBuffer.set(true);
                        return;
                    }
                    handleDodge(p);
                }
            }
        }
        else if(key == ClientEngine.get().keyOffhand.getKey().getValue()) {
            if(modifyCounter <= 0) updateHand(p);
        }
        else if(key == ClientEngine.get().keyEncyclopedia.getKey().getValue() && ClientEngine.get().keyEncyclopedia.consumeClick()) {
            if(mc.screen == null) mc.setScreen(new EncyclopediaScreen());
        }
        else if(key == ClientEngine.get().keyModify.getKey().getValue() && action != GLFW.GLFW_REPEAT) {
            InteractionHand hand = PlayerData.get(p).getActiveHand();
            ItemStack item = p.getItemInHand(hand);
            if(action == GLFW.GLFW_RELEASE) {
                if(item.getItem() instanceof IModifiable modifiable) modifiable.modifyReleaseClient(mc, item, p, hand, modifyCounter);
                modifyCounter = 0;
            }
            else {
                modifyCounter = 1;
                if(item.getItem() instanceof IModifiable modifiable) {
                    Optional<Screen> screen = modifiable.modifyStartClient(mc, item, p, hand);
                    if(screen.isPresent()) mc.setScreen(screen.get());
                }
            }
        }
        else if(key == mc.options.keyShift.getKey().getValue() && action != GLFW.GLFW_REPEAT && mc.screen == null) {
            if(action == GLFW.GLFW_PRESS) {
                long sprintTime = System.currentTimeMillis();
                if(sprintTime - lastSprintTime < 250L) { //Double tap crawl
                    if(p.getPose() != Pose.FALL_FLYING && !capP.isCrawling() && !p.isSpectator() && !p.getAbilities().flying && (capA.isInactive() || capA.getAction().isInterruptible()) && !p.isPassenger() && !p.isEyeInFluid(FluidTags.WATER)) {
                        capP.setCrawling(true);
                        NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.START_CRAWLING));
                        p.setPose(Pose.SWIMMING);
                        p.setForcedPose(Pose.SWIMMING);
                    }
                }
                lastSprintTime = sprintTime;
            }
        }
        else if(key == mc.options.keyJump.getKey().getValue() && mc.screen == null) {
            //Check swim amount so crawl doesn't get instantly canceled if jump was pressed at the same time
            if(capP.isCrawling() && p.getSwimAmount(ClientEngine.get().getPartialTick()) > 0.25F && (capA.isInactive() || capA.getAction().isInterruptible())) {
                mc.options.keyJump.setDown(false);
                capP.setCrawling(false);
                NetworkHandler.toServer(new GenericToServer(NetworkHandler.Type.STOP_CRAWLING));
                p.setForcedPose(null);
            }
        }
        else { //Try to buffer hotbar
            if(p.isUsingItem() || mc.isPaused() || mc.screen != null) return;
            if(!capA.isInactive()) {
                int index = -1;
                for(int i = 0; i < mc.options.keyHotbarSlots.length; i++) if(mc.options.keyHotbarSlots[i].getKey().getValue() == key) index = i;
                if(index != -1) {
                    hotbarBuffer.set(index);
                    actionBuffer.reset();
                }
            }
        }
    }

    //Clicks need to be handled here too since keys can be pressed and released before tick events handle them
    @SubscribeEvent
    public static void onClickInputEvent(InputEvent.ClickInputEvent event) {
        if(skipClickInputEvent) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        if(mc.screen instanceof ModifiableItemScreen) {
            event.setSwingHand(false);
            event.setCanceled(true);
            return;
        }
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        //Allow building material to pick matching index from its recipes if already held
        if(event.isPickBlock()) {
            if(retryPickBlock) {
                KeyMapping.click(mc.options.keyPickItem.getKey()); //Pick twice to update recipe in case building material is picked
                retryPickBlock = false;
            }
            ItemStack heldItem = player.getItemInHand(capP.getActiveHand());
            if(heldItem.getItem() instanceof BuildingMaterialItem buildingItem && mc.hitResult != null) {
                if(mc.hitResult instanceof BlockHitResult blockHit) {
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = player.level.getBlockState(pos);
                    ItemStack pickItem = state.getCloneItemStack(blockHit, player.level, pos, player);
                    if(pickItem.getItem() == heldItem.getItem()) {
                        List<BuildingRecipe> recipes = buildingItem.getRecipes(player.level, player);
                        Block block = state.getBlock();
                        for(int i = 0; i < recipes.size(); i++) {
                            BuildingRecipe recipe = recipes.get(i);
                            if(recipe.output instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                                ClientEngine.get().setModifiableIndex(capP.isMainhandActive(), recipe.getResultItem(), i);
                                buildingItem.setLastUsedItem(recipe.getResultItem().getItem());
                                break;
                            }
                        }
                    }
                }
                else if(mc.hitResult instanceof EntityHitResult entityHit) {
                    if(entityHit.getEntity() instanceof IEntityWithItem entity) {
                        ItemStack pickItem = entity.getPickedResult(entityHit);
                        if(pickItem.getItem() == heldItem.getItem()) {
                            List<BuildingRecipe> recipes = buildingItem.getRecipes(player.level, player);
                            Item armorStandItem = entity.getItemForm();
                            for(int i = 0; i < recipes.size(); i++) {
                                BuildingRecipe recipe = recipes.get(i);
                                if(recipe.output == armorStandItem) {
                                    ClientEngine.get().setModifiableIndex(capP.isMainhandActive(), recipe.getResultItem(), i);
                                    buildingItem.setLastUsedItem(recipe.getResultItem().getItem());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return;
        }
        //Set temp flags since the respective keys may not be set at this point
        if(event.isAttack()) flagAttack = true;
        else if(event.isUseItem()) flagUse = true;
        updateClickInput(true);
        Action action = capA.getAction();
        if(flagAttack) action.onAttackInput(player);
        if(flagUse) action.onUseInput(player);
        flagAttack = false;
        flagUse = false;
        if(ClientEngine.get().microHitResult != null) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
        else if(!capA.isInactive()) {
            event.setSwingHand(false);
            if(event.isAttack() && (!capP.getHeldContents().isEmpty() || !(player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem))) event.setCanceled(true);
        }
        else if(event.isAttack() && (player.getItemInHand(capP.getActiveHand()).getItem() instanceof IWeaponItem || player.getMainHandItem().getItem() instanceof IWeaponItem)) event.setSwingHand(false);
        if(event.isUseItem() && mc.hitResult != null) {
            if(mc.hitResult.getType() == HitResult.Type.BLOCK) {
                if(!event.shouldSwingHand() && capP.getHeldContents().isEmpty()) event.setCanceled(true);
            }
            else if(mc.hitResult.getType() == HitResult.Type.ENTITY && !player.getAbilities().instabuild) {
                double reach = Math.max(1, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() - 2);
                if(mc.hitResult.getLocation().distanceToSqr(player.getEyePosition(ClientEngine.get().getPartialTick())) > reach * reach) {
                    mc.hitResult = BlockHitResult.miss(mc.hitResult.getLocation(), Direction.UP, BlockPos.ZERO);
                }
            }
        }
        if(event.isUseItem() && event.getHand() == capP.getActiveHand() && player.getItemInHand(capP.getActiveHand()).getItem() instanceof IModifiable) {
            int index = event.getHand() == InteractionHand.MAIN_HAND ? ClientEngine.get().getModifiableIndexMain() : ClientEngine.get().getModifiableIndexOff();
            if(index >= 0) NetworkHandler.toServer(new ModifiableIndexToServer(index));
        }
    }

    @SubscribeEvent
    public static void onRawMouseEvent(InputEvent.RawMouseEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        IActionTracker capC = ActionTracker.get(player);
        //if(capC.isStunned() && mc.screen == null) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseScrollEvent(InputEvent.MouseScrollEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        //Lock hotbar during attacks
        if(!capA.isInactive()) {
            event.setCanceled(true);
            int selected = player.getInventory().selected;
            int i = (int) Math.signum(event.getScrollDelta());
            for(selected -= i; selected < 0; selected += 9) {}
            while(selected >= 9) selected -= 9;
            hotbarBuffer.set(selected);
            actionBuffer.reset();
        }
    }

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        Action action = capA.getAction();
        /*if(event.getKey() == mc.options.keyJump.getKey().getValue()) {

        }*/
        if((!capA.isInactive() && !action.isInterruptible()) || capA.isStunned() || mc.screen instanceof ModifiableItemScreen) {
            //Prevent offhand swapping during attacks
            if(event.getKey() == mc.options.keySwapOffhand.getKey().getValue()) {
                mc.options.keySwapOffhand.consumeClick();
            }
            else {
                for(KeyMapping key : mc.options.keyHotbarSlots) {
                    if(event.getKey() == key.getKey().getValue()) {
                        key.consumeClick();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdateEvent(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;

        IActionTracker capA = ActionTracker.get(player);
        IPlayerData capP = PlayerData.get(player);
        Action action = capA.getAction();
        //Restore held jumping if it was canceled
        if(triedJump) {
            int dodgeTicks = player.tickCount - PlayerData.get(player).getLastDodgeTick();
            if(dodgeTicks >= 3 && player.isOnGround() && !capA.isStunned()) {
                //player.jumpFromGround();
                event.getInput().jumping = true;
                triedJump = false;
            }
            if(dodgeTicks >= 6) triedJump = false;
        }
        //This stops jumping at the same time as dodging to get a long jump (similar to sprint jump but a bit better)
        if(player.tickCount - capP.getLastDodgeTick() < 3) {
            triedJump = event.getInput().jumping;
            event.getInput().jumping = false;
        }
        //Slow dive ascension in water
        if(player.isVisuallySwimming()) {
            player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y * 0.85F, player.getDeltaMovement().z);
        }
        //Stop elytra from triggering, should look for a cleaner way to do this since it messes with creative flying while attacking
        if(player.getItemBySlot(EquipmentSlot.CHEST).canElytraFly(player)) {
            if(!capA.isInactive() && !action.isInterruptible() && !player.isOnGround() && !player.isInWater() && !player.isInLava() && !player.onClimbable()) {
                event.getInput().jumping = false;
            }
        }
        if(capA.isStunned()) {
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
        else if(player.isVisuallyCrawling() && event.getInput().jumping) {
            event.getInput().jumping = false;
            if(player.getSwimAmount(ClientEngine.get().getPartialTick()) >= 1F) {
                crawlJumpBuffer.set(true);
                if(actionBuffer.getValue() == ActionType.CRAWL) actionBuffer.reset();
            }
        }
    }

    @SubscribeEvent
    public static void onFlyableFallEvent(PlayerFlyableFallEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        IActionTracker capC = ActionTracker.get(player);
    }

    @SubscribeEvent
    public static void onScreenOpenEvent(ScreenOpenEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if(player == null || !player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        if(capA.isStunned()) {
            if(event.getScreen() instanceof AbstractContainerScreen || event.getScreen() instanceof ModifiableItemScreen) event.setCanceled(true);
        }
        else if(event.getScreen() instanceof InventoryScreen) {
            event.setScreen(ClientEngine.get().getInventoryScreen(player));
        }
        else if(event.getScreen() instanceof EntryPuzzleScreen entryPuzzleScreen) {
            if(mc.screen instanceof EncyclopediaScreen encyclopediaScreen) encyclopediaScreen.setEntryScreen(entryPuzzleScreen);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientLoggedInEvent(ClientPlayerNetworkEvent.LoggedInEvent event) {
        Player p = event.getPlayer();
        p.inventoryMenu = new PlayerInventoryContainer(p.getInventory(), false);
        p.containerMenu = p.inventoryMenu;
        ClientEngine.get().dirtyScreen();
        p.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(100);
        p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        //if(!(p.foodData instanceof PlayerFoodData)) p.foodData = new PlayerFoodData();
        //Debug settings are saved between worlds, so reset it if necessary
        if(!p.isCreative() && !p.isSpectator()) {
            DebugRenderer debugRenderer = Minecraft.getInstance().debugRenderer;
            if(debugRenderer.switchRenderChunkborder()) debugRenderer.switchRenderChunkborder();
            Minecraft.getInstance().getEntityRenderDispatcher().setRenderHitBoxes(false);
        }
        //Controls prompt for new players
        if(Nightfall.Config.DISPLAY_CONTROLS_MESSAGE.get()) {
            Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("nightfall.message.controls_command"));
            Nightfall.Config.DISPLAY_CONTROLS_MESSAGE.set(false);
            Nightfall.Config.DISPLAY_CONTROLS_MESSAGE.save();
        }
    }

    @SubscribeEvent
    public static void onClientLoggedOutEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        EntityLightEngine.get().clear();
        EncyclopediaScreen.selectedTab = null;
        //Vanilla never clears these out, so clear them here so client isn't caching outdated data
        Minecraft.getInstance().levelRenderer.destroyingBlocks.clear();
        Minecraft.getInstance().levelRenderer.destructionProgress.clear();
    }

    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        if(event.getEntity() == Minecraft.getInstance().player) {
            EntityLightEngine.get().clear();
        }
    }

    @SubscribeEvent
    public static void onClientRespawnEvent(ClientPlayerNetworkEvent.RespawnEvent event) {
        Player p = event.getPlayer();
        p.inventoryMenu = new PlayerInventoryContainer(p.getInventory(), false);
        p.containerMenu = p.inventoryMenu;
        ClientEngine.get().dirtyScreen();
        triedJump = false;
        actionBuffer.reset();
        hotbarBuffer.reset();
        crawlJumpBuffer.reset();
        dodgeBuffer.reset();
        resetClick();
    }

    @SubscribeEvent
    public static void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
        if(event.getMessage().getContents().equals("commands.info.server_controls")) {
            //Can't access client's settings on server so just replace the message with new ones
            Minecraft mc = Minecraft.getInstance();
            ChatComponent chat = mc.gui.getChat();
            chat.addMessage(new TranslatableComponent("commands.info.encyclopedia", ClientEngine.get().keyEncyclopedia.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
            chat.addMessage(new TranslatableComponent("commands.info.dash", ClientEngine.get().keyDash.getKey().getDisplayName()));
            chat.addMessage(new TranslatableComponent("commands.info.crawl", mc.options.keyShift.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
            chat.addMessage(new TranslatableComponent("commands.info.climb", mc.options.keyJump.getKey().getDisplayName()));
            chat.addMessage(new TranslatableComponent("commands.info.prioritize_offhand", ClientEngine.get().keyOffhand.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
            chat.addMessage(new TranslatableComponent("commands.info.modify_item_behavior", ClientEngine.get().keyModify.getKey().getDisplayName()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFOVModifierEvent(FOVModifierEvent event) {
        Player player = event.getEntity();
        if(player == null || !player.isAlive()) return;
        //Reverse movement speed FoV changes (except for sprinting)
        event.setNewfov(event.getFov() - ((float) player.getAttribute(Attributes.MOVEMENT_SPEED).getValue() - 0.1F) * 5F);
        if(player.isSprinting()) event.setNewfov(event.getNewfov() + 0.15F);
        IActionTracker capA = ActionTracker.get(player);
        //Bow-like zoom for charging actions
        if(capA.isCharging() && capA.getAction().hasChargeZoom()) {
            float max = capA.getAction().getMaxCharge();
            if(max > 0F) {
                float frame = capA.getFrame() + max / 3F;
                float progress = Math.min(1F, Mth.lerp(capA.modifyPartialTick(ClientEngine.get().getPartialTick()), (frame - 1) / max, frame / max));
                if(progress < 1F) progress *= progress;
                else progress = 1F;
                event.setNewfov(event.getNewfov() * Math.max(1F - progress * 0.15F, 0.15F));
            }
        }
    }

    private static void handleDodge(LocalPlayer p) {
        int dir = 0;
        //Determine dodge direction, 0 is north, +1 to move clockwise
        if(heldKeys[0] && heldKeys[3]) dir = 1;
        else if(heldKeys[0] && heldKeys[2]) dir = 7;
        else if(heldKeys[1] && heldKeys[3]) dir = 3;
        else if(heldKeys[1] && heldKeys[2]) dir = 5;
        else if(heldKeys[1]) dir = 4;
        else if(heldKeys[2]) dir = 6;
        else if(heldKeys[3]) dir = 2;

        IPlayerData capP = PlayerData.get(p);
        if(p.tickCount - capP.getLastDodgeTick() < DODGE_PENALTY_TICK) capP.addStamina(DODGE_STAMINA_COST * 2);
        else capP.addStamina(DODGE_STAMINA_COST);
        capP.setLastDodgeTick(p.tickCount);
        capP.setDodgeDirection(dir);
        NetworkHandler.toServer(new DodgeToServer(dir));
        doDodge(p);
        AnimationUtil.createDodgeParticles(p);
    }

    private static boolean checkDodgeConditions(Player p) {
        Minecraft mc = Minecraft.getInstance();
        IPlayerData capP = PlayerData.get(p);
        IActionTracker capA = ActionTracker.get(p);
        return mc.getCameraEntity() != null && (mc.screen == null || (mc.screen instanceof ModifiableItemScreen modifiableItemScreen && modifiableItemScreen.allowMovementInputs()))
                && !mc.options.keyJump.isDown() && capP.getStamina() > 0 && !p.isUsingItem() && p.isOnGround() && p.tickCount - capP.getLastDodgeTick() >= 5
                && !p.isInWater() && !p.isInLava() &&!p.isCrouching() && p.getPose() != Pose.SWIMMING && capP.getHeldContents().isEmpty()
                && capA.getAction().allowDodging(capA.getState());
    }

    private static boolean checkBooleanArray(boolean[] b) {
        for(boolean x : b) {
            if(x) return true;
        }
        return false;
    }

    //0 = forward, 1 = back, 2 = right, 3 = left
    private static void doDodge(LocalPlayer p) {
        double a = p.getYHeadRot();
        if(!checkBooleanArray(heldKeys)) {
            a += -180;
        }
        else if(heldKeys[2] && heldKeys[3]) {
            if(!heldKeys[0]) a += -180;
        }
        else if(heldKeys[0] && heldKeys[1]) {
            if(heldKeys[2]) a += 90;
            if(heldKeys[3]) a += -90;
            if(!heldKeys[2] && !heldKeys[3]) a += -180;
        }
        else {
            int count = 0;
            int offset = 0;
            if(heldKeys[0]) {
                count++;
            }
            if(heldKeys[1]) {
                if(heldKeys[2]) {
                    offset += 180;
                }
                else {
                    offset += -180;
                }
                count++;
            }
            if(heldKeys[2]) {
                offset += 90; count++;
            }
            if(heldKeys[3]) {
                offset += -90; count++;
            }
            a += (offset / count);
        }

        a = (a % 360) * (Math.PI / 180);
        //double d = Math.abs(Math.cos(a)) + Math.abs(Math.sin(a));
        double d = 1;
        double ratioX = -Math.sin(a) / d;
        double ratioZ = Math.cos(a) / d;
        xDodge = ratioX;
        zDodge = ratioZ;
        doPlayerDodge(p);
    }

    private static void doPlayerDodge(LocalPlayer player) {
        if(player != null) {
            if(player.isAlive()) {
                Vec3 motion = player.getDeltaMovement();
                float speedFactor = player.getBlockSpeedFactor();
                double x = xDodge * DODGE_FORCE * speedFactor;
                double y = motion.y;
                double z = zDodge * DODGE_FORCE * speedFactor;
                player.lerpMotion(x, y, z);
            }
        }
    }

    private static void updateHotbarBuffer(Player player) {
        if(hotbarBuffer.isActive() && hotbarBuffer.getValue() != player.getInventory().selected && ActionTracker.get(player).isInactive()) {
            player.getInventory().selected = hotbarBuffer.getValue();
            PlayerData.get(player).setLastMainItem();
            hotbarBuffer.reset();
        }
    }

    private static void resetClick() {
        click = EMPTY;
        clickCounter = 0;
    }
}
