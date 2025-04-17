package frostnox.nightfall.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin implements AutoCloseable {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private DynamicTexture lightTexture;
    @Shadow @Final private NativeImage lightPixels;
    @Shadow private boolean updateLightTexture;
    @Shadow private float blockLightRedFlicker;
    @Shadow @Final private GameRenderer renderer;

    @Shadow public abstract float notGamma(float p_109893_);
    @Shadow public abstract float getBrightness(Level level, int pLightLevel);

    /**
     * @author Frostnox
     * @reason Adjust color and brightness of sky light based on moon phase, lessen impact of gamma,
     * and darken low block light values such that 0 is pitch black
     */
    @Overwrite
    public void updateLightTexture(float pPartialTicks) {
        if(this.updateLightTexture) {
            this.updateLightTexture = false;
            this.minecraft.getProfiler().push("lightTex");
            ClientLevel clientlevel = this.minecraft.level;
            if(clientlevel != null) {
                float skyDarken = clientlevel.getSkyDarken(1.0F);
                float skyFlash;
                if(clientlevel.getSkyFlashTime() > 0) skyFlash = 1.0F;
                else skyFlash = skyDarken * 0.95F + 0.05F;

                float waterVision = this.minecraft.player.getWaterVision();
                float nightVision;
                if(this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) nightVision = GameRenderer.getNightVisionScale(this.minecraft.player, pPartialTicks);
                else if(waterVision > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) nightVision = waterVision;
                else if(minecraft.player.isSpectator() && minecraft.player.level.collidesWithSuffocatingBlock(minecraft.player,
                        new AABB(minecraft.player.getEyePosition().subtract(0.001, 0.001, 0.001),
                                minecraft.player.getEyePosition().add(0.001, 0.001, 0.001)))) nightVision = 1F;
                else nightVision = 0.0F;
                Vector3f skyDarkened = new Vector3f(skyDarken, skyDarken, 1F);
                skyDarkened.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                //Adjust color for moon phases
                if(skyDarken < 1F && LevelData.isPresent(clientlevel)) {
                    MoonPhase phase = MoonPhase.get(clientlevel);
                    float moonAmount = (float) Math.max(0, clientlevel.getStarBrightness(1F) - 0.2) * 10F / 3F * 2F; //This matches the opacity value of the moon
                    if(phase == MoonPhase.FULL) skyDarkened.lerp(new Vector3f(0.32F, 0.615F, 1F), moonAmount); //Full
                    else if(phase == MoonPhase.WANING_GIBBOUS || phase == MoonPhase.WAXING_GIBBOUS) {
                        skyDarkened.lerp(new Vector3f(0.32F, 0.515F, 1F), moonAmount); //3/4
                    }
                    else if(phase == MoonPhase.FIRST_QUARTER || phase == MoonPhase.LAST_QUARTER) {
                        skyDarkened.lerp(new Vector3f(0.32F, 0.44F, 1F), moonAmount); //Half
                    }
                    else if(phase == MoonPhase.WANING_CRESCENT || phase == MoonPhase.WAXING_CRESCENT) {
                        skyDarkened.lerp(new Vector3f(0.32F, 0.38F, 1F), moonAmount); //1/4
                    }
                    else if(phase == MoonPhase.NEW) skyDarkened.lerp(new Vector3f(0.17F, 0.17F, 0.42F), moonAmount); //New
                }
                float blockFlicker = this.blockLightRedFlicker + 1.5F;
                Vector3f lightColor = new Vector3f();

                for(int i = 0; i < 16; ++i) {
                    for(int j = 0; j < 16; ++j) {
                        float skyLight = getBrightness(clientlevel, i) * skyFlash;
                        float blockR = getBrightness(clientlevel, j) * blockFlicker;
                        float blockG = blockR * ((blockR * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float blockB = blockR * (blockR * blockR * 0.6F + 0.4F);
                        lightColor.set(blockR, blockG, blockB);
                        if(clientlevel.effects().forceBrightLightmap()) lightColor.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                        else {
                            Vector3f skyDarkenedFlash = skyDarkened.copy();
                            skyDarkenedFlash.mul(skyLight);
                            lightColor.add(skyDarkenedFlash);
                            lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            if(this.renderer.getDarkenWorldAmount(pPartialTicks) > 0.0F) {
                                float darkenAmount = this.renderer.getDarkenWorldAmount(pPartialTicks);
                                Vector3f darkenedLight = lightColor.copy();
                                darkenedLight.mul(0.7F, 0.6F, 0.6F);
                                lightColor.lerp(darkenedLight, darkenAmount);
                            }
                        }

                        lightColor.clamp(0.0F, 1.0F);
                        if(nightVision > 0.0F) {
                            float brightestColor = Math.max(lightColor.x(), Math.max(lightColor.y(), lightColor.z()));
                            if(brightestColor < 1.0F) {
                                float f12 = 1.0F / brightestColor;
                                Vector3f vector3f5 = lightColor.copy();
                                vector3f5.mul(f12);
                                lightColor.lerp(vector3f5, nightVision);
                            }
                        }

                        float gamma = (float)this.minecraft.options.gamma / 4F; //Reduce gamma
                        Vector3f gammaAdjustment = lightColor.copy();
                        gammaAdjustment.map(this::notGamma);
                        lightColor.lerp(gammaAdjustment, gamma);
                        lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        lightColor.clamp(0.0F, 1.0F);
                        if(nightVision == 0F) lightColor.mul((Math.min(13, Math.max(i, j)) / 13F)); //Adjust towards zero light
                        lightColor.mul(255.0F);
                        int r = (int)lightColor.x();
                        int g = (int)lightColor.y();
                        int b = (int)lightColor.z();
                        this.lightPixels.setPixelRGBA(j, i, 0xFF000000 | b << 16 | g << 8 | r);
                    }
                }

                this.lightTexture.upload();
            }
            this.minecraft.getProfiler().pop();
        }
    }
}
