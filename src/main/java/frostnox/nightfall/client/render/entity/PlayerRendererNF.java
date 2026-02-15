package frostnox.nightfall.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.block.block.anvil.AnvilSection;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.PlayerModelNF;
import frostnox.nightfall.client.render.blockentity.TieredAnvilRenderer;
import frostnox.nightfall.client.render.entity.layer.ArmorLayer;
import frostnox.nightfall.client.render.entity.layer.PlayerEquipmentLayer;
import frostnox.nightfall.item.item.TongsItem;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.RenderUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.world.inventory.AccessorySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Re-renders the vanilla player model with extra animations.
 */
public class PlayerRendererNF extends PlayerRenderer {
    public PlayerModelNF<AbstractClientPlayer> newModel;
    public final boolean slim;

    public PlayerRendererNF(EntityRendererProvider.Context renderer) {
        this(renderer, false);
    }

    public PlayerRendererNF(EntityRendererProvider.Context renderer, boolean slim) {
        this(renderer, slim, ImmutableMap.of());
    }

    public PlayerRendererNF(EntityRendererProvider.Context renderer, boolean slim, ImmutableMap<String, Vec3f> layerScaleMap) {
        super(renderer, slim);
        newModel = new PlayerModelNF<>(renderer.bakeLayer(slim ? ModelRegistryNF.PLAYER_SLIM : ModelRegistryNF.PLAYER), slim);
        this.slim = slim;
        //Replace with custom layers
        for(int i = 0; i < layers.size(); i++) {
            if(layers.get(i) instanceof ItemInHandLayer) {
                layers.remove(i);
                layers.add(i, new CombatHeldItemLayer<>(this));
            }
            else if(layers.get(i) instanceof HumanoidArmorLayer) {
                layers.remove(i);
                layers.add(i, new ArmorLayer<>(this, renderer, layerScaleMap));
            }
            else if(layers.get(i) instanceof CustomHeadLayer) {
                layers.remove(i);
                layers.add(i, new PlayerEquipmentLayer<>(this, renderer));
            }
            else if(layers.get(i) instanceof ArrowLayer) { //Remove arrow layer since texture isn't accurate anymore
                layers.remove(i);
                i--;
            }
        }
        layers.add(new PlayerExtraLayer<>(this));
    }

    @Override
    public void render(AbstractClientPlayer player, float yaw, float partialTicks, PoseStack matrix, MultiBufferSource buffers, int light) {
        setModelProperties(player);
        matrix.pushPose();
        this.newModel.attackTime = this.getAttackAnim(player, partialTicks);

        boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
        this.newModel.riding = shouldSit;
        this.newModel.young = player.isBaby();
        float f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
        float headYaw = f1 - f;
        if (shouldSit && player.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)player.getVehicle();
            f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            headYaw = f1 - f;
            float f3 = Mth.wrapDegrees(headYaw);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            headYaw = f1 - f;
        }

        //Client player is already adjusted in ClientEngine for use with weapon collision
        float headPitch = player == Minecraft.getInstance().player ? player.getXRot() : Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        if (player.getPose() == Pose.SLEEPING) {
            Direction direction = player.getBedOrientation();
            if (direction != null) {
                float f4 = player.getEyeHeight(Pose.STANDING) - 0.1F;
                matrix.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
            }
        }
        float bobProgress = this.getBob(player, partialTicks);
        this.setupRotations(player, matrix, bobProgress, f, partialTicks);
        matrix.scale(-1.0F, -1.0F, 1.0F);
        this.scale(player, matrix, partialTicks);
        matrix.translate(0.0D, (double)-1.501F, 0.0D);
        if(player.isCrouching() && player.isAlive() && PlayerData.get(player).isCrawling()) matrix.translate(0, 0, 0.134D * player.getSwimAmount(partialTicks));
        if(player.isVisuallyCrawling()) matrix.translate(0, 0, -0.18D * player.getSwimAmount(partialTicks));
        if(player.isVisuallyCrawling() && !player.isInWater()) {
            float tilt = AnimationUtil.getAirborneProgress(player, partialTicks);
            if(tilt > 0.0F) {
                tilt = AnimationUtil.applyEasing(tilt, Easing.inOutCubic);
                matrix.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(player.getSwimAmount(partialTicks), 0, 75) * tilt));
            }
        }
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && player.isAlive()) {
            //Reverse hurt animation
            if(player.animationSpeed == 1.5F) {
                player.animationSpeed = player.animationSpeedOld;
            }
            limbSwingAmount = Mth.lerp(partialTicks, player.animationSpeedOld, player.animationSpeed);
            limbSwing = player.animationPosition - player.animationSpeed * (1.0F - partialTicks);
            if (player.isBaby()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        this.newModel.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
        this.newModel.setupAnim(player, limbSwing, limbSwingAmount, bobProgress, headYaw, headPitch);
        if(newModel.swimAmount > 0.0F && player.isVisuallyCrawling()) {
            //matrix.mulPose(Vector3f.YP.rotation(newModel.swimAmount * Mth.cos(limbSwing * 0.33333334F)));
            matrix.mulPose(Vector3f.ZP.rotation(newModel.swimAmount * Mth.cos(limbSwing * 0.75F) * 0.09F));
        }
        //Combat animations
        float yRot = this.newModel.doCombatAnimations(player, matrix);
        if(player.isAlive()) {
            IPlayerData capP = PlayerData.get(player);
            float xAmount = CombatUtil.DodgeDirection.get(capP.getDodgeDirection()).getXAmount();
            float zAmount = CombatUtil.DodgeDirection.get(capP.getDodgeDirection()).getZAmount();
            //Factor in y rotation from Action transform
            float x = xAmount * Mth.cos(yRot) - zAmount * Mth.sin(yRot);
            float z = xAmount * Mth.sin(yRot) + zAmount * Mth.cos(yRot);
            //Dodge tilt
            if(player.tickCount - capP.getLastDodgeTick() >= 0 && player.tickCount - capP.getLastDodgeTick() < 4) {
                AnimationCalculator calc = new AnimationCalculator(3, player.tickCount - capP.getLastDodgeTick(), partialTicks, Easing.outQuart);
                matrix.mulPose(Vector3f.XP.rotationDegrees(x * 5 * calc.getProgress()));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(z * 5 * calc.getProgress()));
            }
            else if(player.tickCount - capP.getLastDodgeTick() < 7) {
                AnimationCalculator calc = new AnimationCalculator(3, player.tickCount - capP.getLastDodgeTick() - 4, partialTicks, Easing.inOutSine);
                matrix.mulPose(Vector3f.XP.rotationDegrees(x * 5 * (1 - calc.getProgress())));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(z * 5 * (1 - calc.getProgress())));
            }
            //Slight walking bob
            /*if(!Minecraft.getInstance().options.bobView || player != Minecraft.getInstance().player) {
                float airProgress = 1F - getAirborneProgress(player, partialTicks);
                matrix.translate(0, 0.015 * airProgress * (Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount), 0);
            }*/
        }
        //Copies
        newModel.hat.copyFrom(newModel.head);
        newModel.leftPants.copyFrom(newModel.leftLeg);
        newModel.rightPants.copyFrom(newModel.rightLeg);
        newModel.leftSleeveN.copyFrom(newModel.leftHand);
        newModel.rightSleeveN.copyFrom(newModel.rightHand);
        newModel.jacket.copyFrom(newModel.body);

        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(player);
        boolean flag1 = !flag && !player.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(player);
        RenderType rendertype = this.getRenderType(player, flag, flag1, flag2);
        //Adjust arms to new offset since vanilla overwrites the default position
        this.newModel.rightArm.x += slim ? -0.5F : -1F;
        this.newModel.leftArm.x += slim ? 0.5F : 1F;
        if (rendertype != null) {
            VertexConsumer ivertexbuilder = buffers.getBuffer(rendertype);
            //This causes the red overlay on damage
            int i = AnimationUtil.getOverlayCoords(player, this.getWhiteOverlayProgress(player, partialTicks));
            this.newModel.renderToBuffer(matrix, ivertexbuilder, light, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }
        if (!player.isSpectator()) {
            for(RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layerrenderer : this.layers) {
                layerrenderer.render(matrix, buffers, light, player, limbSwing, limbSwingAmount, partialTicks, bobProgress, headYaw, headPitch);
            }
        }

        matrix.popPose();
        net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(player, player.getDisplayName(), this, matrix, buffers, light, partialTicks);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(player))) {
            this.renderNameTag(player, renderNameplateEvent.getContent(), matrix, buffers, light);
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<>(player, this, partialTicks, matrix, buffers, light));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Post(player, this, partialTicks, matrix, buffers, light));
    }

    @Override
    protected boolean isShaking(AbstractClientPlayer pEntity) {
        return PlayerData.get(pEntity).isShivering();
    }

    @Override
    protected boolean shouldShowName(AbstractClientPlayer pEntity) {
        if(PlayerData.get(pEntity).getAccessoryInventory().getItem(AccessorySlot.FACE).is(ItemsNF.MASK.get())) return false;
        else return super.shouldShowName(pEntity);
    }

    @Override
    public PlayerModel<AbstractClientPlayer> getModel() {
        return newModel;
    }

    protected void setModelProperties(AbstractClientPlayer player) {
        PlayerModelNF<AbstractClientPlayer> playermodel = newModel;
        if (player.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            playermodel.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
            playermodel.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
            playermodel.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.leftSleeve.visible = false;
            playermodel.rightSleeve.visible = false;
            playermodel.leftSleeveN.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeveN.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.crouching = player.isCrouching() && !(player.isAlive() && PlayerData.get(player).isCrawling());
            HumanoidModel.ArmPose bipedmodel$armpose = getArmPose(player, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose bipedmodel$armpose1 = getArmPose(player, InteractionHand.OFF_HAND);
            if (bipedmodel$armpose.isTwoHanded()) {
                bipedmodel$armpose1 = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (player.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = bipedmodel$armpose;
                playermodel.leftArmPose = bipedmodel$armpose1;
            } else {
                playermodel.rightArmPose = bipedmodel$armpose1;
                playermodel.leftArmPose = bipedmodel$armpose;
            }
        }
    }

    protected static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
            } else if (!player.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    private static class PlayerExtraLayer<T extends Player, M extends EntityModel<T> & HeadedModel & ArmedModel> extends PlayerItemInHandLayer<T, M> {
        public PlayerExtraLayer(RenderLayerParent<T, M> p_i50934_1_) {
            super(p_i50934_1_);
        }

        @Override
        public void render(PoseStack stack, MultiBufferSource pBuffer, int pPackedLight, T player, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            CompoundTag contents = PlayerData.get(player).getHeldContents();
            //Render held block entity
            if(player.isAlive() && !contents.isEmpty()) {
                BlockState state = Block.stateById(contents.getInt("state"));
                BlockEntity blockEntity = BlockEntity.loadStatic(BlockPos.ZERO, state, contents);
                if(blockEntity instanceof IHoldable holdable) {
                    stack.pushPose();
                    stack.scale(0.99F, 0.99F, 0.99F); //Fix z-fighting with player arms
                    stack.mulPose(Vector3f.XP.rotationDegrees(180));
                    float crouch = AnimationUtil.getCrouchProgress(player, pPartialTicks);
                    double y = -0.5 - 2D/16D + holdable.getThirdPersonYOffset();
                    if(crouch > 0.0F) stack.translate(-0.5, y - 0.1825 * crouch, 2D/16D);
                    else stack.translate(-0.5, y, 2D/16D);
                    int light = state.emissiveRendering(player.level, player.blockPosition()) ? LightTexture.FULL_BRIGHT : pPackedLight;
                    if(holdable.useBlockEntityItemRenderer()) {
                        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
                        renderer.render(blockEntity, pPartialTicks, stack, pBuffer, light, OverlayTexture.NO_OVERLAY);
                    }
                    Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, stack, pBuffer, light, OverlayTexture.NO_OVERLAY, blockEntity.getModelData());
                    stack.popPose();
                }
            }
            //Tongs
            for(InteractionHand hand : InteractionHand.values()) {
                ItemStack item = player.getItemInHand(hand);
                if(item.getItem() instanceof TongsItem tongs && tongs.hasWorkpiece(item)) {
                    stack.pushPose();
                    getParentModel().translateToHand(hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT, stack);
                    stack.translate(0, 7.5D/16D, -0.625D);
                    TieredAnvilRenderer.renderWorkpiece(stack, pBuffer, RenderUtil.getHeatedMetalColor(tongs.getTemperature(item), tongs.getColor(item)),
                            pPackedLight, AnvilSection.FLAT, tongs.getTemperature(item), tongs.getWork(item), item.getTag().getBoolean("flip"),
                            item.getTag().getBoolean("slagCenter"), item.getTag().getBoolean("slagLeft"), item.getTag().getBoolean("slagRight"));
                    stack.popPose();
                }
            }
        }
    }

    private static class CombatHeldItemLayer<T extends Player, M extends EntityModel<T> & HeadedModel & ArmedModel> extends PlayerItemInHandLayer<T, M> {
        public CombatHeldItemLayer(RenderLayerParent<T, M> p_i50934_1_) {
            super(p_i50934_1_);
        }

        @Override
        public void render(PoseStack p_225628_1_, MultiBufferSource p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
            boolean flag = p_225628_4_.getMainArm() == HumanoidArm.RIGHT;
            ItemStack itemstack = flag ? p_225628_4_.getOffhandItem() : p_225628_4_.getMainHandItem();
            ItemStack itemstack1 = flag ? p_225628_4_.getMainHandItem() : p_225628_4_.getOffhandItem();
            if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
                p_225628_1_.pushPose();
                this.renderArmWithItem(p_225628_4_, itemstack1, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, p_225628_1_, p_225628_2_, p_225628_3_);
                this.renderArmWithItem(p_225628_4_, itemstack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, p_225628_1_, p_225628_2_, p_225628_3_);
                p_225628_1_.popPose();
            }
        }

        private void renderArmWithSpyglass(LivingEntity p_174518_, ItemStack p_174519_, HumanoidArm p_174520_, PoseStack p_174521_, MultiBufferSource p_174522_, int p_174523_) {
            p_174521_.pushPose();
            ModelPart modelpart = this.getParentModel().getHead();
            float f = modelpart.xRot;
            modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float)Math.PI / 6F), ((float)Math.PI / 2F));
            modelpart.translateAndRotate(p_174521_);
            modelpart.xRot = f;
            CustomHeadLayer.translateToHead(p_174521_, false);
            boolean flag = p_174520_ == HumanoidArm.LEFT;
            p_174521_.translate((double)((flag ? -2.5F : 2.5F) / 16.0F), -0.0625D, 0.0D);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(p_174518_, p_174519_, ItemTransforms.TransformType.HEAD, false, p_174521_, p_174522_, p_174523_);
            p_174521_.popPose();
        }

        @Override
        protected void renderArmWithItem(LivingEntity user, ItemStack itemStack, ItemTransforms.TransformType p_229135_3_, HumanoidArm handSide, PoseStack stack, MultiBufferSource p_229135_6_, int p_229135_7_) {
            if(itemStack.is(Items.SPYGLASS) && user.getUseItem() == itemStack && user.swingTime == 0) {
                renderArmWithSpyglass(user, itemStack, handSide, stack, p_229135_6_, p_229135_7_);
            }
            else if(!itemStack.isEmpty()) {
                stack.pushPose();
                this.getParentModel().translateToHand(handSide, stack);
                stack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                boolean flag = handSide == HumanoidArm.LEFT;
                stack.translate(0, 0.125D, -0.625D);
                if(((PlayerModelNF) (this.getParentModel())).slim) stack.translate(flag ? -0.5F / 16F : 0.5F / 16F, 0, 0);
                Minecraft.getInstance().getItemInHandRenderer().renderItem(user, itemStack, p_229135_3_, flag, stack, p_229135_6_, p_229135_7_);
                stack.popPose();
            }
        }
    }
}