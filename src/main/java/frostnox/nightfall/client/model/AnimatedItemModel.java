package frostnox.nightfall.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.player.IClientAction;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * IMPORTANT: Item must implement {@link IActionableItem}. Default rotation values are forcibly symmetric based on right side values.
 * This is similar to SeparatePerspectiveModel, but it only has 2 models.
 * The base model (ActionableItemModel) is used only when the item is in-hand and supports custom transformations based on action state.
 * The inventory model is used elsewhere and is a generic item model.
 */
public class AnimatedItemModel implements IModelGeometry<AnimatedItemModel> {
    private final BlockModel baseModel;
    private final BlockModel inventoryModel;
    private final double swapSpeed, swapYOffset;

    public AnimatedItemModel(BlockModel baseModel, BlockModel inventoryModel, double swapSpeed, double swapYOffset) {
        this.baseModel = baseModel;
        this.inventoryModel = inventoryModel;
        this.swapSpeed = swapSpeed;
        this.swapYOffset = swapYOffset;
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new UnfinishedModel(
                owner.useSmoothLighting(), owner.isShadedInGui(), owner.isSideLit(), swapSpeed, swapYOffset,
                spriteGetter.apply(owner.resolveTexture("particle")),
                baseModel.bake(bakery, baseModel, spriteGetter, modelTransform, modelLocation, owner.isSideLit()),
                inventoryModel.bake(bakery, inventoryModel, spriteGetter, modelTransform, modelLocation, owner.isSideLit())
        );
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> textures = Sets.newHashSet();
        textures.addAll(baseModel.getMaterials(modelGetter, missingTextureErrors));
        textures.addAll(inventoryModel.getMaterials(modelGetter, missingTextureErrors));
        return textures;
    }

    public static class AnimatedOverrideList extends ItemOverrides {
        private final UnfinishedModel model;
        public AnimatedOverrideList(UnfinishedModel model) {
            super();
            this.model = model;
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            return new AnimatedItemModel.Model(model, model.baseModel.getOverrides().resolve(model.baseModel, stack, world, entity, seed),
                    model.inventoryModel.getOverrides().resolve(model.inventoryModel, stack, world, entity, seed), entity);
        }
    }

    private static class UnfinishedModel implements BakedModel {
        private final boolean isAmbientOcclusion;
        private final boolean isGui3d;
        private final boolean isSideLit;
        private final double swapSpeed, swapYOffset;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;
        private final BakedModel baseModel;
        private final BakedModel inventoryModel;

        private UnfinishedModel(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit, double swapSpeed, double swapYOffset, TextureAtlasSprite particle, BakedModel baseModel, BakedModel inventoryModel) {
            this.isAmbientOcclusion = isAmbientOcclusion;
            this.isGui3d = isGui3d;
            this.isSideLit = isSideLit;
            this.swapSpeed = swapSpeed;
            this.swapYOffset = swapYOffset;
            this.particle = particle;
            this.baseModel = baseModel;
            this.inventoryModel = inventoryModel;
            this.overrides = new AnimatedOverrideList(this);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return isAmbientOcclusion;
        }

        @Override
        public boolean isGui3d() {
            return isGui3d;
        }

        @Override
        public boolean usesBlockLight() {
            return isSideLit;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }

        @Override
        public boolean doesHandlePerspectives() {
            return true;
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public BakedModel handlePerspective(ItemTransforms.TransformType type, PoseStack poseStack) {
            if(type == ItemTransforms.TransformType.FIXED || type == ItemTransforms.TransformType.GROUND || type == ItemTransforms.TransformType.GUI) {
                return inventoryModel.handlePerspective(type, poseStack);
            }
            return baseModel.handlePerspective(type, poseStack);
        }
    }

    public static class Model implements BakedModel {
        private final boolean isAmbientOcclusion;
        private final boolean isGui3d;
        private final boolean isSideLit;
        private final ItemOverrides overrides;
        public final double swapSpeed, swapYOffset;
        private final TextureAtlasSprite particle;
        public final BakedModel baseModel;
        public final BakedModel inventoryModel;

        private Model(UnfinishedModel model, BakedModel baseModel, BakedModel inventoryModel, LivingEntity user) {
            this.isAmbientOcclusion = model.isAmbientOcclusion;
            this.isGui3d = model.isGui3d;
            this.isSideLit = model.isSideLit;
            this.overrides = model.overrides;
            this.swapSpeed = model.swapSpeed;
            this.swapYOffset = model.swapYOffset;
            this.particle = model.particle;
            this.baseModel = new ActionableModel(baseModel, user);
            this.inventoryModel = inventoryModel;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return isAmbientOcclusion;
        }

        @Override
        public boolean isGui3d() {
            return isGui3d;
        }

        @Override
        public boolean usesBlockLight() {
            return false;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return baseModel.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }

        @Override
        public boolean doesHandlePerspectives() {
            return true;
        }

        @Override
        public ItemTransforms getTransforms() {
            return baseModel.getTransforms();
        }

        @Override
        public BakedModel handlePerspective(ItemTransforms.TransformType type, PoseStack poseStack) {
            if(type == ItemTransforms.TransformType.FIXED || type == ItemTransforms.TransformType.GROUND || type == ItemTransforms.TransformType.GUI) {
                return inventoryModel.handlePerspective(type, poseStack);
            }
            return baseModel.handlePerspective(type, poseStack);
        }
    }

    /**
     * Model class for weapons created from WeaponModel.
     * Necessary transformations are called automatically from this class based on the current action.
     * If you really need to do something though, use the animation calculators from here.
     */
    public static class ActionableModel implements IDynamicBakedModel {
        protected BakedModel baseModel;
        protected LivingEntity user;

        private final ItemTransforms defaultTransforms; //Stores the original transforms
        private final AnimationData animData;

        private ActionableModel(BakedModel model, @Nullable LivingEntity user) {
            baseModel = model;
            this.user = user;
            ItemTransforms t = model.getTransforms();
            defaultTransforms = new ItemTransforms(t.thirdPersonRightHand, t.thirdPersonRightHand, t.firstPersonRightHand, t.firstPersonRightHand,
                    t.head, t.gui, t.ground, t.fixed, t.moddedTransforms);
            this.animData = new AnimationData();
        }

        public LivingEntity getUser() {
            return user;
        }

        public void animateFirstPersonStun(int frame, int duration) {
            int offset = duration / 2;
            animData.tCalc.add(0, 0F, 0);
            animData.tCalc.setEasing(Easing.outQuart);
            animData.tCalc.length = offset;
            if(frame > offset) {
                animData.resetLengths(duration, Easing.inOutSine);
                animData.toDefault();
            }
        }

        public void animateThirdPersonStun(int frame, int duration) {
            animData.resetLengths(duration / 2, Easing.outQuart);
            animData.toDefault();
        }

        public ItemTransforms getDefaultTransforms() {
            return new ItemTransforms(defaultTransforms);
        }

        public void updateAnimation(IActionTracker capA, HumanoidArm side, float partialTick, boolean firstPerson) {
            if(firstPerson) animData.setDefaults(defaultTransforms.firstPersonRightHand.translation, defaultTransforms.firstPersonRightHand.rotation, defaultTransforms.firstPersonRightHand.scale);
            else animData.setDefaults(defaultTransforms.thirdPersonRightHand.translation, defaultTransforms.thirdPersonRightHand.rotation, defaultTransforms.thirdPersonRightHand.scale);
            animData.reset();
            int state = capA.getState();
            Action action = capA.getAction();
            int frame = capA.getFrame();
            int duration = capA.getDuration();

            InteractionHand hand = user instanceof Player player ? PlayerData.get(player).getActiveHand() : InteractionHand.MAIN_HAND;
            if(AnimationUtil.getSideFromHand(hand) == side) {
                if(firstPerson && action instanceof IClientAction clientAction) {
                    animData.update(frame, duration, capA.modifyPartialTick(partialTick), Easing.inOutSine);
                    clientAction.transformModelFP(state, capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), user, animData);
                }
                else if(!firstPerson) {
                    animData.update(frame, duration, capA.modifyPartialTick(partialTick), Easing.inOutSine);
                    action.transformLayer(state, capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), user, animData);
                }
            }
            else if(action instanceof IClientAction clientAction) {
                if(firstPerson) {
                    animData.update(frame, duration, capA.modifyPartialTick(partialTick), Easing.inOutSine);
                    clientAction.transformOppositeModelFP(state, capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), user, animData);
                }
                else {
                    animData.update(frame, duration, capA.modifyPartialTick(partialTick), Easing.inOutSine);
                    clientAction.transformOppositeLayer(state, capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), user, animData);
                }
            }
            //Stun animation, overlays other animations
            if(capA.isStunned()) {
                animData.update(capA.getStunFrame(), capA.getStunDuration(), partialTick, Easing.inOutSine);
                animData.tCalc.setStaticVector(animData.tCalc.getTransformations());
                animData.rCalc.setStaticVector(animData.rCalc.getTransformations());
                animData.sCalc.setStaticVector(animData.sCalc.getTransformations());
                if(firstPerson) animateFirstPersonStun(capA.getStunFrame(), capA.getStunDuration());
                else animateThirdPersonStun(capA.getStunFrame(), capA.getStunDuration());
            }
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
            return baseModel.getQuads(state, side, rand);
        }

        @NotNull
        @Override
        public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
            return baseModel.getQuads(state, side, rand, extraData);
        }

        @Override
        public ItemOverrides getOverrides() {
            return baseModel.getOverrides();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return baseModel.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return baseModel.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return baseModel.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return baseModel.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            ItemTransforms transforms = new ItemTransforms(getDefaultTransforms());
            ItemTransform rfp = transforms.firstPersonRightHand;
            ItemTransform lfp = transforms.firstPersonLeftHand;
            ItemTransform rtp = transforms.thirdPersonRightHand;
            ItemTransform ltp = transforms.thirdPersonLeftHand;
            ItemTransform rightFP = new ItemTransform(rfp.rotation.copy(), rfp.translation.copy(), rfp.scale.copy());
            ItemTransform leftFP = new ItemTransform(lfp.rotation.copy(), lfp.translation.copy(), lfp.scale.copy());
            ItemTransform rightTP = new ItemTransform(rtp.rotation.copy(), rtp.translation.copy(), rtp.scale.copy());
            ItemTransform leftTP = new ItemTransform(ltp.rotation.copy(), ltp.translation.copy(), ltp.scale.copy());
            if(user != null && user.isAlive() && (user instanceof Player || user instanceof ActionableEntity)) {
                IActionTracker capA = ActionTracker.get(user);
                ItemStack stackMain = ItemStack.EMPTY;
                ItemStack stackOff = ItemStack.EMPTY;
                if(user.getMainHandItem().getItem() instanceof IActionableItem) stackMain = user.getMainHandItem();
                if(user.getOffhandItem().getItem() instanceof IActionableItem) stackOff = user.getOffhandItem();
                float partialTick = ClientEngine.get().getPartialTick();
                if(!stackMain.isEmpty()) {
                    HumanoidArm side = AnimationUtil.getSideFromHand(InteractionHand.MAIN_HAND);
                    //Main item
                    updateAnimation(capA, side, partialTick, true);
                    if(side == HumanoidArm.RIGHT) rightFP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    else leftFP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    updateAnimation(capA, side, partialTick, false);
                    if(side == HumanoidArm.RIGHT) rightTP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    else leftTP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                }
                if(!stackOff.isEmpty()) {
                    HumanoidArm side = AnimationUtil.getSideFromHand(InteractionHand.OFF_HAND);
                    //Off item
                    updateAnimation(capA, side, partialTick, true);
                    if(side == HumanoidArm.RIGHT) rightFP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    else leftFP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    updateAnimation(capA, side, partialTick, false);
                    if(side == HumanoidArm.RIGHT) rightTP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                    else leftTP = new ItemTransform(animData.rCalc.getTransformations(), animData.tCalc.getTransformations(), animData.sCalc.getTransformations());
                }
            }
            leftFP.rotation.setY(leftFP.rotation.y() - 180);
            leftFP.rotation.setZ(-leftFP.rotation.z());
            leftTP.rotation.setY(leftTP.rotation.y() - 180);
            leftTP.rotation.setZ(-leftTP.rotation.z());
            return new ItemTransforms(leftTP, rightTP, leftFP, rightFP, transforms.head, transforms.gui, transforms.ground, transforms.fixed, transforms.moddedTransforms);
        }
    }

    public static class Loader implements IModelLoader<AnimatedItemModel> {
        public static final AnimatedItemModel.Loader INSTANCE = new AnimatedItemModel.Loader();

        private Loader() {

        }

        @Override
        public void onResourceManagerReload(final ResourceManager pResourceManager) {

        }

        @Override
        public AnimatedItemModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            BlockModel baseModel = deserializationContext.deserialize(GsonHelper.getAsJsonObject(modelContents, "base"), BlockModel.class);
            BlockModel inventoryModel = deserializationContext.deserialize(GsonHelper.getAsJsonObject(modelContents, "inventory"), BlockModel.class);
            double swapSpeed = 1;
            if(modelContents.has("swapSpeed")) swapSpeed = modelContents.get("swapSpeed").getAsDouble();
            double swapYOffset = 0;
            if(modelContents.has("swapYOffset")) swapYOffset = modelContents.get("swapYOffset").getAsDouble();
            return new AnimatedItemModel(baseModel, inventoryModel, swapSpeed, swapYOffset);
        }
    }
}
