package frostnox.nightfall.mixin;

import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Fixes for various vanilla camera bugs, including:
 * MC-19222: You can see through blocks in third person when having view bobbing enabled
 * MC-92250: You can see through blocks using high FOV values and speed
 * MC-206561: Water is still completely transparent at certain height levels
 * MC-206842: Lava is still briefly transparent at a specific height level
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    private static final double SCALE = 0.75D;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow private boolean initialized;
    @Shadow private BlockGetter level;
    @Shadow private Vec3 position;
    @Shadow private Entity entity;

    @Shadow public abstract Camera.NearPlane getNearPlane();
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void setRotation(float pYRot, float xRot);

    /**
     * Apply view bob to camera directly so its position is accurate
     */
    @Inject(method = "setup", at = @At(value = "INVOKE", target = ("Lnet/minecraft/client/Camera;move(DDD)V")))
    private void nightfall$applyViewBob(CallbackInfo callbackInfo) {
        if(Minecraft.getInstance().options.bobView && entity instanceof Player player) {
            float f = player.walkDist - player.walkDistO;
            float partialTick = ClientEngine.get().getPartialTick();
            float f1 = -(player.walkDist + f * partialTick);
            float f2 = Mth.lerp(partialTick, player.oBob, player.bob);
            float xBob = Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F;
            float yaw = MathUtil.toRadians(Mth.wrapDegrees(yRot + 90F));
            setPosition(position.add(xBob * Mth.sin(-yaw) * SCALE, Math.abs(Mth.cos(f1 * (float) Math.PI) * f2) * SCALE, xBob * Mth.cos(-yaw) * SCALE));
            setRotation(yRot, xRot + Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F * (float) SCALE);
        }
    }

    /**
     * Vanilla plane doesn't adjust for fov changes which results in incorrect vectors
     */
    @ModifyVariable(method = "getNearPlane", at = @At("STORE"), ordinal = 1)
    private double nightfall$getFov(double fov) {
        Minecraft mc = Minecraft.getInstance();
        return Math.tan((mc.options.fov * Mth.lerp(ClientEngine.get().getPartialTick(), mc.gameRenderer.oldFov, mc.gameRenderer.fov))
                * (Math.PI / 180D) / 2.0D) * 0.05D;
    }

    /**
     * @author Frostnox
     * @reason Replace approximation of fluid shapes with fully accurate shapes to fix fluid detection bugs:
     * MC-206561: Water is still completely transparent at certain height levels
     * MC-206842: Lava is still briefly transparent at a specific height level
     */
    @Overwrite
    public FogType getFluidInCamera() {
        if(initialized) {
            Camera.NearPlane plane = getNearPlane();
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            for(Vec3 planeVec : Arrays.asList(plane.forward, plane.getTopLeft(), plane.getTopRight(), plane.getBottomLeft(), plane.getBottomRight(),
                    plane.getPointOnPlane(1F, 0F), plane.getPointOnPlane(-1F, 0F))) {
                Vec3 adjustedPos = position.add(planeVec);
                blockPos.set(adjustedPos.x, adjustedPos.y, adjustedPos.z);
                FluidState fluidState = level.getFluidState(blockPos);
                if(fluidState.is(FluidTags.WATER)) {
                    if(adjustedPos.y <= (double) (getFluidHeight((float) adjustedPos.x, (float) adjustedPos.z, blockPos, fluidState) + blockPos.getY())) {
                        return FogType.WATER;
                    }
                }
                else if(fluidState.is(FluidTags.LAVA)) {
                    if(adjustedPos.y <= (double) (getFluidHeight((float) adjustedPos.x, (float) adjustedPos.z, blockPos, fluidState) + blockPos.getY())) {
                        return FogType.LAVA;
                    }
                }
                else {
                    BlockState blockstate = level.getBlockState(blockPos);
                    if(blockstate.is(Blocks.POWDER_SNOW)) return FogType.POWDER_SNOW;
                }
            }
        }
        return FogType.NONE;
    }

    private float getFluidHeight(float x, float z, BlockPos centerPos, FluidState centerFluid) {
        Fluid type = centerFluid.getType();
        float centerHeight = getHeight(type, centerPos);
        if(centerHeight >= 1F) return centerHeight;
        else {
            //Convert global coords to block local
            x = x % 1F;
            if(x < 0F) x += 1F;
            z = z % 1F;
            if(z < 0F) z += 1F;

            float northHeight = getHeight(type, centerPos.north());
            float southHeight = getHeight(type, centerPos.south());
            float eastHeight = getHeight(type, centerPos.east());
            float westHeight = getHeight(type, centerPos.west());

            float avgNW = calculateAverageHeight(type, centerHeight, northHeight, westHeight, centerPos.relative(Direction.NORTH).relative(Direction.WEST));
            float avgSE = calculateAverageHeight(type, centerHeight, southHeight, eastHeight, centerPos.relative(Direction.SOUTH).relative(Direction.EAST));
            //Select closer plane since there may be two divided along SE to NW
            if(x <= z) {
                float avgSW = calculateAverageHeight(type, centerHeight, southHeight, westHeight, centerPos.relative(Direction.SOUTH).relative(Direction.WEST));
                //Simplified from plane equation
                return avgNW - (-(avgSW - avgNW)) * z + ((avgSE - avgNW) - (avgSW - avgNW)) * x;
            }
            else {
                float avgNE = calculateAverageHeight(type, centerHeight, northHeight, eastHeight, centerPos.relative(Direction.NORTH).relative(Direction.EAST));
                //Simplified from plane equation
                return avgNW + ((avgSE - avgNW) - (avgNE - avgNW)) * z - (-(avgNE - avgNW)) * x;
            }
        }
    }

    private float getHeight(Fluid type, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return getHeight(type, pos, state, state.getFluidState());
    }

    private float getHeight(Fluid type, BlockPos pos, BlockState state, FluidState fluidState) {
        if(type.isSame(fluidState.getType())) {
            BlockState aboveState = level.getBlockState(pos.above());
            return type.isSame(aboveState.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
        }
        else return !state.getMaterial().isSolid() ? 0.0F : -1.0F;
    }

    private float calculateAverageHeight(Fluid type, float centerHeight, float height1, float height2, BlockPos pos) {
        if(!(height2 >= 1.0F) && !(height1 >= 1.0F)) {
            float[] vec = new float[2];

            if(height2 > 0.0F || height1 > 0.0F) {
                float f = getHeight(type, pos);
                if(f >= 1.0F) return 1.0F;
                addWeightedHeight(vec, f);
            }

            addWeightedHeight(vec, centerHeight);
            addWeightedHeight(vec, height2);
            addWeightedHeight(vec, height1);
            return vec[0] / vec[1];
        }
        else return 1.0F;
    }

    private static void addWeightedHeight(float[] vec, float height) {
        if(height >= 0.8F) {
            vec[0] += height * 10.0F;
            vec[1] += 10.0F;
        }
        else if(height >= 0.0F) {
            vec[0] += height;
            vec[1] += 1.0F;
        }
    }
}
