package frostnox.nightfall.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.IMicroGrid;
import frostnox.nightfall.capability.*;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.EntityLightEngine;
import frostnox.nightfall.client.gui.screen.AttributeSelectionScreen;
import frostnox.nightfall.client.model.entity.PlayerModelNF;
import frostnox.nightfall.client.model.AnimatedItemModel;
import frostnox.nightfall.client.render.entity.PlayerRendererNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.*;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.*;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderEventHandler {
    private static float lastLight = -1F, goalLight;
    private static float lastLightTick;

    @SubscribeEvent
    public static void onRenderTooltipColorEvent(RenderTooltipEvent.Color event) {
        if(Minecraft.getInstance().screen == null || Minecraft.getInstance().screen instanceof AttributeSelectionScreen) {
            event.setBackground(0xf0212227);
            event.setBorderStart(0x509ba0a7);
            event.setBorderEnd(0x50858c97);
        }
        else {
            event.setBackground(0xf0241b0e);
            event.setBorderStart(0x50c4a886);
            event.setBorderEnd(0x509b8060);
        }
    }

    @SubscribeEvent
    public static void onFogColorsEvent(EntityViewRenderEvent.FogColors event) {
        FogType type = event.getCamera().getFluidInCamera();
        if(type == FogType.WATER || type == FogType.NONE) {
            Entity entity = event.getCamera().getEntity();
            float light = entity.level.getBrightness(LightLayer.SKY, entity.eyeBlockPosition());
            if(lastLight == -1F) {
                lastLight = light;
                lastLightTick = entity.tickCount;
            }
            if(goalLight != light) {
                lastLight = Mth.lerp(Easing.inOutSine.apply(Math.min(1F, (entity.tickCount - lastLightTick + ClientEngine.get().getPartialTick()) / 20F)), lastLight, goalLight);
                lastLightTick = entity.tickCount;
                goalLight = light;
            }
            float darkness = Mth.lerp(Easing.inOutSine.apply(Math.min(1F, (entity.tickCount - lastLightTick + ClientEngine.get().getPartialTick()) / 20F)), lastLight, light) / 15F;
            if(type == FogType.WATER || (LevelData.isPresent(entity.getLevel()) &&
                    entity.getEyePosition((float) event.getPartialTicks()).y - ContinentalChunkGenerator.SEA_LEVEL + 16 < 0F)) {
                event.setRed(event.getRed() * darkness);
                event.setGreen(event.getGreen() * darkness);
                event.setBlue(event.getBlue() * darkness);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFogEvent(EntityViewRenderEvent.RenderFogEvent event) {
        FogType fluidFog = event.getCamera().getFluidInCamera();
        if(fluidFog == FogType.NONE) {
            Level level = Minecraft.getInstance().level;
            if(LevelData.isPresent(level)) {
                ILevelData capL = LevelData.get(level);
                if(capL.getGlobalWeather() == Weather.FOG) {
                    float intensity = 1F - Math.min(0.95F, capL.getFogIntensity());
                    float intensitySqr = intensity * intensity;
                    event.setCanceled(true);
                    event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), 460.8F * intensitySqr));
                    event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 512F * (1.5F * intensitySqr + 0.8F * intensity)));
                }
            }
        }
        else if(fluidFog == FogType.WATER) {
            event.setCanceled(true);
            event.setFarPlaneDistance(event.getFarPlaneDistance() * 0.6F);
        }
    }
    
    @SubscribeEvent
    public static void onRenderBlockOverlayEvent(RenderBlockOverlayEvent event) {
        if(event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.BLOCK) {
            Player player = event.getPlayer();
            float size = player.getDimensions(player.getPose()).width * 0.8F;
            AABB box = AABB.ofSize(player.getEyePosition(), size, 1.0E-6D, size);
            BlockPos pos = event.getBlockPos();
            //Vanilla has no collision check for view blocking, so do one for blocks that aren't full shapes
            if(!event.getBlockState().isCollisionShapeFullBlock(player.level, pos)) {
                if(!Shapes.joinIsNotEmpty(event.getBlockState().getCollisionShape(player.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(box), BooleanOp.AND)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.DEBUG) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if(level == null) return;
        if(mc.player == null || (!mc.player.isCreative() && !mc.player.isSpectator() && !ClientEngine.get().isDevVersion())) {
            event.setCanceled(true);
            ClientEngine.get().getLimitedDebugScreen().render(event.getMatrixStack()); //Render limited debug for survival/adventure mode
        }
    }

    @SubscribeEvent
    public static void onRenderEvent(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ClientEngine.get().applyWaterShader(event.getProjectionMatrix(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onRenderLevelLastEvent(RenderLevelLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if(level == null || mc.cameraEntity == null) return;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3i hit = ClientEngine.get().microHitResult;
        BlockPos pos = ClientEngine.get().microBlockEntityPos;
        if(hit != null && pos != null && level.getBlockEntity(pos) instanceof IMicroGrid gridEntity) {
            mc.gameRenderer.resetProjectionMatrix(event.getProjectionMatrix());
            float rot = -gridEntity.getRotationDegrees();
            Vector3f offset = MathUtil.rotatePointByYaw(gridEntity.getWorldGridOffset(), rot, new Vec2(-gridEntity.getGridXSize()/32F - gridEntity.getWorldGridOffset().x(), -gridEntity.getGridZSize()/32F - gridEntity.getWorldGridOffset().z()));
            Vector3f hitPos = MathUtil.rotatePointByYaw(new Vector3f(hit.getX()/16F, hit.getY()/16F, hit.getZ()/16F), rot);
            double cubeX = pos.getX() + offset.x() + hitPos.x();
            double cubeY = pos.getY() + offset.y() + hitPos.y();
            double cubeZ = pos.getZ() + offset.z() + hitPos.z();
            VoxelShape cube = Shapes.create(cubeX, cubeY, cubeZ, cubeX + 1/16F, cubeY + 1/16F, cubeZ + 1/16F);
            poseStack.pushPose();
            poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            if(rot != 0) {
                if(rot == -270) poseStack.translate(-1/16F, 0, 0);
                else if(rot == -180) poseStack.translate(-1/16F, 0, -1/16F);
                else if(rot == -90) poseStack.translate(0, 0, -1/16F);
            }
            RenderUtil.drawCubeOutlineWorld(poseStack, buffer, new Color(10, 10, 10, 255), cube);
            poseStack.popPose();
        }
        //Force block overlay to render if in wall
        if(!mc.options.getCameraType().isFirstPerson() && mc.cameraEntity.isInWall()) mc.options.setCameraType(CameraType.FIRST_PERSON);
    }

    /**
     * Note: the matrix stacks for each hand are not independent. The main hand stack is transformed and then the off hand stack
     * is pushed, so it inherits the main hand transformations.
     */
    @SubscribeEvent
    public static void onRenderHandEvent(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack stack = event.getPoseStack();
        LocalPlayer player = mc.player;
        if(player != null) {
            InteractionHand hand = event.getHand();
            if(!player.isAlive()) return;
            IActionTracker capA = ActionTracker.get(player);
            IPlayerData capP = PlayerData.get(player);
            Action action = capA.getAction();
            if(player.handsBusy && (player.isUsingItem() || !capA.isInactive())) player.handsBusy = false;
            if(!capP.getHeldContents().isEmpty()) {
                event.setCanceled(true);
                if(hand == InteractionHand.MAIN_HAND) {
                    CompoundTag contents = capP.getHeldContents();
                    BlockState state = Block.stateById(contents.getInt("state"));
                    BlockEntity blockEntity = BlockEntity.loadStatic(BlockPos.ZERO, state, contents);
                    if(blockEntity instanceof IHoldable holdable) {
                        stack.pushPose();
                        stack.mulPose(Vector3f.YP.rotationDegrees(180));
                        stack.translate(-0.5, -1 - (1D - AnimationUtil.getHoldProgress(player, event.getPartialTicks())) + holdable.getFirstPersonYOffset(), 0.5);
                        int light = state.emissiveRendering(player.level, player.blockPosition()) ? LightTexture.FULL_BRIGHT : event.getPackedLight();
                        if(holdable.useBlockEntityItemRenderer()) {
                            BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
                            renderer.render(blockEntity, event.getPartialTicks(), stack, event.getMultiBufferSource(), light, OverlayTexture.NO_OVERLAY);
                        }
                        mc.getBlockRenderer().renderSingleBlock(state, stack, event.getMultiBufferSource(), light, OverlayTexture.NO_OVERLAY, blockEntity.getModelData());
                        stack.popPose();
                    }
                }
                return;
            }
            //Lessen bobbing during attacks
            if(mc.options.bobView && !capA.isStunned() && hand == InteractionHand.MAIN_HAND) {
                softenBobbingTransformations(stack, player, 0.15F, event.getPartialTicks());
                if(!capA.isInactive() && action.getTotalStates() > 1 && hand == capP.getActiveHand()) {
                    float progress = 1;
                    float ticks = ClientEngine.get().getPartialTick();
                    AnimationCalculator calc = new AnimationCalculator(capA.getDuration(), capA.getFrame(), ticks, Easing.inOutSine);
                    if(capA.getState() == 0 && ActionsNF.isEmpty(action.chainsFrom().getId())) {
                        calc.setEasing(Easing.inSine);
                        progress = calc.getProgress();
                    }
                    else if(capA.getState() == action.getTotalStates() - 1) {
                        calc.setEasing(Easing.outSine);
                        progress = 1 - calc.getProgress();
                    }
                    softenBobbingTransformations(stack, player, progress * 0.85F, event.getPartialTicks());
                }
            }
            //Idle bob
            if(mc.options.bobView) {
                float bob0 = Mth.lerp(event.getPartialTicks(), Mth.sin((player.tickCount - 1) * 0.03F), Mth.sin(player.tickCount * 0.03F));
                float bob1 = Mth.lerp(event.getPartialTicks(), Mth.sin((player.tickCount + 19) * 0.025F), Mth.sin((player.tickCount + 20) * 0.025F));
                if(hand == InteractionHand.MAIN_HAND) {
                    stack.translate(0.01F * bob1, 0.008F * bob0, 0F);
                }
                else {
                    float bob2 = Mth.lerp(event.getPartialTicks(), Mth.cos((player.tickCount - 1) * 0.03F), Mth.cos(player.tickCount * 0.03F));
                    float bob3 = Mth.lerp(event.getPartialTicks(), Mth.cos((player.tickCount + 19) * 0.025F), Mth.cos((player.tickCount + 20) * 0.025F));
                    stack.translate(-0.01F * bob1 + 0.01F * bob3, -0.008F * bob0 + 0.008F * bob2, 0);
                }
            }
            float modifiedPartial = capA.modifyPartialTick(ClientEngine.get().getPartialTick());
            //Replacement of vanilla item swap animation
            ItemInHandRenderer renderer = Minecraft.getInstance().getItemInHandRenderer();
            float swapYOffsetMain = Minecraft.getInstance().getItemRenderer().getModel(ClientEngine.get().mainHandItem, player.level, player, 0) instanceof AnimatedItemModel.Model model ? (float) model.swapYOffset : 0;
            float mainSwap = (1F - Mth.lerp(event.getPartialTicks(), ClientEngine.get().oMainHandHeight, ClientEngine.get().mainHandHeight)) * (-0.8F + swapYOffsetMain) + (1F - Mth.lerp(event.getPartialTicks(), renderer.oMainHandHeight, renderer.mainHandHeight)) * 0.6F;
            mainSwap += -0.14F * Easing.inOutSine.apply(Mth.lerp(modifiedPartial, ClientEngine.get().lastMainHandLowerTime, ClientEngine.get().mainHandLowerTime) / 4F);
            if(hand == InteractionHand.MAIN_HAND) {
                renderer.mainHandItem = !capA.isInactive() && player.getMainHandItem().isEmpty() ? ItemStack.EMPTY : ClientEngine.get().mainHandItem;
                stack.translate(0, mainSwap, 0);
            }
            else {
                float swapYOffsetOff = Minecraft.getInstance().getItemRenderer().getModel(ClientEngine.get().offHandItem, player.level, player, 0) instanceof AnimatedItemModel.Model model ? (float) model.swapYOffset : 0;
                float offSwap = (1F - Mth.lerp(event.getPartialTicks(), ClientEngine.get().oOffHandHeight, ClientEngine.get().offHandHeight)) * (-0.8F + swapYOffsetOff) + event.getEquipProgress() * 0.6F;
                offSwap += -0.14F * Easing.inOutSine.apply(Mth.lerp(modifiedPartial, ClientEngine.get().lastOffHandLowerTime, ClientEngine.get().offHandLowerTime) / 4F);
                renderer.offHandItem = !capA.isInactive() && player.getOffhandItem().isEmpty() ? ItemStack.EMPTY : ClientEngine.get().offHandItem;
                stack.translate(0, offSwap - mainSwap, 0);
            }
            //Block entity holding
            if(hand == InteractionHand.MAIN_HAND && capP.getHoldTicks() != -1) {
                stack.translate(0, -0.35 * AnimationUtil.getHoldProgress(player, modifiedPartial), 0);
            }
            //Climbing transformations
            float climbProgress = AnimationUtil.getClimbProgress(player, modifiedPartial);
            if(hand == InteractionHand.MAIN_HAND && climbProgress > 0) {
                climbProgress = AnimationUtil.applyEasing(climbProgress, Easing.outSine);
                if(!capA.isInactive()) {
                    if(capA.getState() == 0) climbProgress *= 1F - capA.getProgress(ClientEngine.get().getPartialTick());
                    else if(capA.getState() == action.getTotalStates() - 1) climbProgress *= capA.getProgress(ClientEngine.get().getPartialTick());
                    else climbProgress = 0;
                }
                stack.translate(0, -0.14F * climbProgress, 0);
            }
            //Stun transformations
            if(hand == InteractionHand.MAIN_HAND && capA.isStunned()) {
                AnimationCalculator stunCalc = new AnimationCalculator(capA.getStunDuration(), capA.getStunFrame(), ClientEngine.get().getPartialTick(), Easing.inOutSine);
                stunCalc.setStaticVector(0, 0, 0);
                stunCalc.length /= 2;
                stunCalc.setEasing(Easing.outQuart);
                float mag = Math.max(0.4F, capA.getStunDuration() / (float) CombatUtil.STUN_MEDIUM);
                stunCalc.add(0F, -0.6F * mag, 0.2F * mag);
                if(capA.getStunFrame() > capA.getStunDuration() / 2) {
                    stunCalc.length = capA.getStunDuration();
                    stunCalc.setEasing(Easing.inOutSine);
                    stunCalc.offset = capA.getStunDuration() / 2;
                    stunCalc.extend(0, 0, 0);
                }
                Vector3f vec = stunCalc.getTransformations();
                stack.translate(vec.x(), vec.y(), vec.z());
            }
            //Action transformations
            if(!capA.isInactive() && action instanceof IClientAction clientAction) {
                int xSide = hand == InteractionHand.MAIN_HAND ? 1 : -1;
                int side = 1;
                float ticks = ClientEngine.get().getPartialTick();
                AnimationCalculator calc = new AnimationCalculator(Math.min(6, capA.getDuration()), capA.getFrame(), capA.modifyPartialTick(ticks), Easing.inOutSine);
                calc.setStaticVector(0, 0, 0);
                boolean emptyHand = player.getItemInHand(hand).isEmpty();
                if(emptyHand) xSide = 0;
                if((capP.getActiveHand() == InteractionHand.OFF_HAND) && hand == InteractionHand.OFF_HAND) {
                    if(player.getMainHandItem().isEmpty()) xSide = 0;
                    else xSide = -xSide;
                    side = -1;
                }
                if(capP.getActiveHand() != hand || emptyHand || (capP.getActiveHand() == InteractionHand.OFF_HAND && hand == InteractionHand.OFF_HAND)) {
                    clientAction.transformOppositeHandFP(calc, xSide, side, capA);
                }
                //Stun transformations
                Vector3f vec;
                if(capA.isStunned()) {
                    AnimationCalculator stunCalc = new AnimationCalculator(capA.getStunDuration(), capA.getStunFrame(), ticks, Easing.inOutSine);
                    stunCalc.setStaticVector(calc.getTransformations());
                    stunCalc.length /= 2;
                    stunCalc.setEasing(Easing.outQuart);
                    if(capA.getStunFrame() > capA.getStunDuration() / 2) {
                        stunCalc.length = capA.getStunDuration();
                        stunCalc.setEasing(Easing.inOutSine);
                        stunCalc.offset = capA.getStunDuration() / 2;
                        stunCalc.extend(0, 0, 0);
                    }
                    vec = stunCalc.getTransformations();
                }
                else vec = calc.getTransformations();
                stack.translate(vec.x(), vec.y(), vec.z());
            }
        }
    }

    private static void softenBobbingTransformations(PoseStack matrixStack, Player player, float progress, float partialTicks) {
        float deltaDistanceWalked = player.walkDist - player.walkDistO;
        float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
        float cameraYaw = Mth.lerp(partialTicks, player.oBob, player.bob);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * cameraYaw) * 5.0F) * 0.7F * progress));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 3.0F) * 0.7F * progress));
        matrixStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 0.5F) * 0.7F * progress, -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * cameraYaw)) * 0.7F * progress, 0.0D);
    }

    @SubscribeEvent
    public static void onPlayerRenderEventPre(RenderPlayerEvent.Pre event) {
        event.setCanceled(true);
        AbstractClientPlayer player = (AbstractClientPlayer) event.getPlayer();
        PlayerRendererNF renderer = ClientEngine.get().getPlayerCombatRenderer(event.getRenderer());
        PoseStack matrix = event.getPoseStack();
        float partial = event.getPartialTick();
        if(player.isAlive()) {
            IActionTracker capA = ActionTracker.get(player);
            if(capA.getState() == capA.getAction().getChargeState() && !capA.isStunnedOrHitPaused()) capA.setChargePartial(ClientEngine.get().getPartialTick());
        }
        renderer.render(player, 1, partial, matrix, event.getMultiBufferSource(), event.getPackedLight());
    }

    @SubscribeEvent
    public static void onRenderLivingEventPre(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {

    }

    @SubscribeEvent
    public static void onCameraSetupEvent(EntityViewRenderEvent.CameraSetup event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        //Stop camera from clipping inside of blocks
        if(camera.isDetached() && !entity.isSpectator()) {
            Camera.NearPlane plane = camera.getNearPlane();
            Vec3 toEntity = camera.getPosition().subtract(entity.getEyePosition());
            if(toEntity.lengthSqr() > 0.04) {
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
                for(Vec3 planeVec : Arrays.asList(plane.getPointOnPlane(0F, 1F), plane.getPointOnPlane(1F, 0F), plane.getPointOnPlane(-1F, 0F), plane.getTopLeft(), plane.getTopRight(), plane.getBottomLeft(), plane.getBottomRight())) {
                    Vec3 camPos = camera.getPosition();
                    Vec3 adjustedPos = camPos.add(planeVec);
                    blockPos.set(adjustedPos.x, adjustedPos.y, adjustedPos.z);
                    if(LevelUtil.isPointVisuallyInBlock(entity.level, blockPos, adjustedPos)) {
                        double x, y, z, scale;
                        Vec3 scaledPos = camPos;
                        for(scale = 0.01D; scale < 1D; scale += 0.01D) {
                            x = toEntity.x * scale;
                            y = toEntity.y * scale;
                            z = toEntity.z * scale;
                            if(Mth.floor(adjustedPos.x - x) != blockPos.getX() || Mth.floor(adjustedPos.y - y) != blockPos.getY() || Mth.floor(adjustedPos.z - z) != blockPos.getZ()) {
                                scaledPos = camPos.subtract(toEntity.scale(scale));
                                Vec3 adjustedScaledPos = scaledPos.add(planeVec);
                                blockPos.set(adjustedScaledPos.x, adjustedScaledPos.y, adjustedScaledPos.z);
                                if(!LevelUtil.isPointVisuallyInBlock(entity.level, blockPos, adjustedScaledPos)) break;
                            }
                        }
                        camera.setPosition(scaledPos);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.isAlive()) return;
        if(event.phase == TickEvent.Phase.START) {
            ClientEngine.get().tickRenderStart();
            EntityLightEngine.get().tickRenderStart();
        }
        else {
            ClientEngine.get().tickRenderEnd();
        }
    }

    @SubscribeEvent
    public static void onRenderLivingEventPost(RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        if(!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;
        //Render attack hitboxes
        if(event.getEntity() instanceof Player player) {
            if(!player.isAlive()) return;
            if(!(event.getRenderer().getModel() instanceof PlayerModelNF)) return;
            IActionTracker capA = ActionTracker.get(player);

            //For debugging weapon spheres
//            if(capA.isInactive() && player.getMainHandItem().getItem() instanceof IWeaponItem weapon) {
//                float SCALE = 0.9375F;
//                BoundingSphere[] spheres = weapon.getHurtSpheres().getSpheres();
//                Vector3f offset1 = new Vector3f(0, 0, 0);
//                Vector3f translation = new Vector3f(0, 0, 0);
//                Mat4f userMatrix = new Mat4f();
//                Mat4f localMatrix = new Mat4f();
//                localMatrix.multiply(new Quat(-90, Vector3f.YP, true));
//                localMatrix.multiply(new Quat(-90, Vector3f.XP, true));
//                userMatrix.multiply(new Mat4f(new Quat(player.yBodyRot, Vector3f.YP, true)));
//                AnimationData[] testData = new AnimationData[]{new AnimationData(),
//                        new AnimationData(new Vector3f(-5.5F, -13, -2),
//                                new Vector3f(MathUtil.toDegrees(-0.314F), 0, 0), new Vector3f(1, 1, 1))};
//                for(int i = 0; i < spheres.length; i++) {
//                    spheres[i].scale(SCALE);
//                    spheres[i].transform(testData, userMatrix, localMatrix, translation, offset1);
//                    renderWireSphere(event.getPoseStack().last().pose(), event.getMultiBufferSource(), spheres[i]);
//                }
//                return;
//            }
            if(capA.isStunnedOrHitPaused() || !capA.isDamaging()) return;
            IPlayerData capP = PlayerData.get(player);
            if(capP.getHitStopFrame() != -1) return;
            Action action = capA.getAction();
            if(!(action instanceof Attack attack)) return;
            if(!(action instanceof IClientAction clientAction)) return;
            float partialTicks = ClientEngine.get().getPartialTick();
            PoseStack stack = event.getPoseStack();

            stack.pushPose();
            AnimationData data = attack.getAnimationData(player, capA).get(EntityPart.HAND_RIGHT);
            int frame = capA.getFrame();
            int duration = capA.getDuration();
            int side = PlayerData.get(player).getActiveHand() == InteractionHand.MAIN_HAND ? 1 : -1;

            clientAction.transformModelFP(capA.getState(), frame, duration, action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), player, data);

            Mat4f userMatrix = new Mat4f(new Quat(player.getViewXRot(partialTicks), Vector3f.XN, true));
            userMatrix.multiply(new Quat(player.getViewYRot(partialTicks), Vector3f.YP, true));
            Vector3f translation = attack.getTranslation(player);
            BoundingSphere[] spheres = attack.getHurtSpheres(player).getSpheres();
            Vector3f offset = attack.getOffset(player);

            for(int i = 0; i < spheres.length; i++) {
                spheres[i].transformFP(data, userMatrix, translation, offset, side != 1);
                spheres[i].yPos += player.getEyeHeight();
                renderWireSphere(stack.last().pose(), event.getMultiBufferSource(), spheres[i]);
            }
            stack.popPose();
        }
        else if(event.getEntity() instanceof ActionableEntity entity) {
            LivingEntity user = event.getEntity();
            IActionTracker capA = entity.getActionTracker();
            if(entity.isAlive() && capA.isDamaging()) {
                float partialTicks = ClientEngine.get().getPartialTick();
                PoseStack stack = event.getPoseStack();
                Attack attack = (Attack) capA.getAction();
                com.mojang.math.Matrix4f matrix = event.getPoseStack().last().pose();
                stack.pushPose();
                Mat4f userMatrix = new Mat4f(new Quat(Mth.lerp(partialTicks, user.yBodyRotO, user.yBodyRot), Vector3f.YP, true));
                EnumMap<EntityPart, AnimationData> transforms = attack.getAnimationData(user, capA);
                AnimationCalculator mCalc = new AnimationCalculator(capA.getDuration(), capA.getFrame(), partialTicks, Easing.inOutSine);
                attack.transformModel(capA.getState(), capA.getFrame(), capA.getDuration(), capA.getAction().getChargeProgress(capA.getCharge(), capA.getChargePartial()), attack.getPitch(user, partialTicks), user, transforms, mCalc);
                AnimationData layer = new AnimationData();
                layer.update(capA.getFrame(), capA.getDuration(), partialTicks);
                attack.transformLayer(capA.getState(), capA.getFrame(), capA.getDuration(), capA.getAction().getChargeProgress(capA.getCharge(), capA.getChargePartial()), user, layer);
                BoundingSphere[] spheres = attack.getHurtSpheres(user).getSpheres();
                Vector3f offset = attack.getOffset(user);
                Vector3f translation = attack.getTranslation(user);
                Vector3f mVec = mCalc.getTransformations();
                //userMatrix.multiply(new Quaternion(mVec.z(), Vector3f.ZP, true));
                userMatrix.multiply(new Quat(mVec.y(), Vector3f.YP, true));
                //userMatrix.multiply(new Quaternion(mVec.x(), Vector3f.XN, true));
                Mat4f localMatrix = new Mat4f();
                Vector3f lVec = layer.rCalc.getTransformations();
                localMatrix.multiply(new Quat(lVec.z(), Vector3f.ZP, true));
                localMatrix.multiply(new Quat(lVec.y(), Vector3f.YP, true));
                localMatrix.multiply(new Quat(lVec.x(), Vector3f.XP, true));
                AnimationData[] transformsArray = transforms.values().toArray(new AnimationData[0]);
                for(int i = 0; i < spheres.length; i++) {
                    spheres[i].transform(transformsArray, userMatrix, localMatrix, translation, offset);
                    renderWireSphere(matrix, event.getMultiBufferSource(), spheres[i]);
                }
                stack.popPose();
            }
        }
        if(event.getEntity() instanceof IOrientedHitBoxes hitBoxesEntity) {
            PoseStack stack = event.getPoseStack();
            for(OBB box : hitBoxesEntity.getOBBs(ClientEngine.get().getPartialTick())) {
                stack.pushPose();
                stack.translate(box.translation.x, box.translation.y, box.translation.z);
                stack.mulPose(box.rotation);
                stack.translate(box.center.x(), box.center.y(), box.center.z());
                LevelRenderer.renderLineBox(stack, event.getMultiBufferSource().getBuffer(RenderType.lines()), -box.extents.x, -box.extents.y, -box.extents.z,
                        box.extents.x, box.extents.y, box.extents.z, 0F, 1F, 0F, 1F);
                stack.popPose();
            }
        }
    }

    public static void renderWireSphere(com.mojang.math.Matrix4f matrix, MultiBufferSource bufferSource, BoundingSphere sphere) {
        Level world = Minecraft.getInstance().level;
        if(world == null) return;
        double inc = sphere.radius * 0.1D;
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lineStrip());
        for(double i = -Math.PI; i < Math.PI; i += inc) {
            builder.vertex(matrix, (float) (sphere.xPos + Math.cos(i) * sphere.radius), (float) (sphere.yPos + Math.sin(i) * sphere.radius), (float) sphere.zPos).color(1F, 0F, 0F, 1F).normal(0, 0, 0).endVertex();
        }
        builder = bufferSource.getBuffer(RenderType.lineStrip());
        for(double i = -Math.PI; i < Math.PI; i += inc) {
            builder.vertex(matrix, (float) (sphere.xPos + Math.cos(i) * sphere.radius), (float) sphere.yPos, (float) (sphere.zPos + Math.sin(i) * sphere.radius)).color(1F, 0F, 0F, 1F).normal(0, 0, 0).endVertex();
        }
        builder = bufferSource.getBuffer(RenderType.lineStrip());
        for(double i = -Math.PI; i < Math.PI; i += inc) {
            builder.vertex(matrix, (float) sphere.xPos, (float) (sphere.yPos + Math.cos(i) * sphere.radius), (float) (sphere.zPos + Math.sin(i) * sphere.radius)).color(1F, 0F, 0F, 1F).normal(0, 0, 0).endVertex();
        }
    }
}
