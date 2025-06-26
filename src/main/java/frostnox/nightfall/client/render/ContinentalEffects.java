package frostnox.nightfall.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.capability.ILevelData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.registry.forge.BiomesNF;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Random;

public class ContinentalEffects extends DimensionSpecialEffects {
    public static final ResourceLocation SUN_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/environment/sun.png");
    public static final ResourceLocation MOON_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/environment/moon_phases.png");
    public static final ResourceLocation STARS_LOCATION = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/environment/stars.png");
    public static final float MOON_SIZE = 16F;
    public static final float SUN_SIZE = 32F;
    protected static final int CLOUD_WIDTH = 16, CLOUD_HEIGHT = 12;
    protected final VertexBuffer skyBuffer, darkBuffer, starBuffer;
    protected @Nullable VertexBuffer cloudBuffer;
    protected @Nullable FractalSimplexNoiseFast opacityNoise, minYNoise, maxYNoise;
    protected int prevCloudX = Integer.MIN_VALUE;
    protected int prevCloudY = Integer.MIN_VALUE;
    protected int prevCloudZ = Integer.MIN_VALUE;
    protected double prevTime = 0D;
    protected Vec3 prevCloudColor = Vec3.ZERO;
    protected @Nullable CloudStatus prevCloudsType;
    public boolean regenClouds = true;
    protected final float[] rainSizeX = new float[1024], rainSizeZ = new float[1024];
    protected int rainSoundTime;
    private boolean initialized = false;

    public void init(long seed) {
        Random cloudRand = new Random(seed);
        opacityNoise = new FractalSimplexNoiseFast(cloudRand.nextLong(), 0.025F, 2, 0.5F, 2.0F);
        minYNoise = new FractalSimplexNoiseFast(cloudRand.nextLong(), 0.02F, 2, 0.5F, 2.0F);
        maxYNoise = new FractalSimplexNoiseFast(cloudRand.nextLong(), 0.02F, 2, 0.5F, 2.0F);
        initialized = true;
    }

    public ContinentalEffects(float pCloudLevel, boolean pHasGround, DimensionSpecialEffects.SkyType pSkyType, boolean pForceBrightLightmap, boolean pConstantAmbientLight) {
        super(pCloudLevel, pHasGround, pSkyType, pForceBrightLightmap, pConstantAmbientLight);

        for(int x = 0; x < 32; ++x) {
            for(int z = 0; z < 32; ++z) {
                float centerZ = (float)(z - 16);
                float centerX = (float)(x - 16);
                float dist = Mth.sqrt(centerZ * centerZ + centerX * centerX);
                rainSizeX[x << 5 | z] = -centerX / dist * 0.5F;
                rainSizeZ[x << 5 | z] = centerZ / dist * 0.5F;
            }
        }

        //Sky
        Tesselator skyTesselator = Tesselator.getInstance();
        BufferBuilder builder = skyTesselator.getBuilder();
        skyBuffer = new VertexBuffer();
        buildSkyDisc(builder, 16.0F);
        skyBuffer.upload(builder);
        //Dark
        skyTesselator = Tesselator.getInstance();
        builder = skyTesselator.getBuilder();
        darkBuffer = new VertexBuffer();
        buildSkyDisc(builder, -2.0F);
        darkBuffer.upload(builder);
        //Stars
        skyTesselator = Tesselator.getInstance();
        builder = skyTesselator.getBuilder();
        starBuffer = new VertexBuffer();
        buildStars(builder);
        starBuffer.upload(builder);

        setSkyRenderHandler(((ticks, partialTick, poseStack, level, minecraft) -> {
            Camera camera = minecraft.gameRenderer.getMainCamera();
            boolean doFog = isFoggyAt(Mth.floor(camera.getPosition().x), Mth.floor(camera.getPosition().z)) || minecraft.gui.getBossOverlay().shouldCreateWorldFog();
            if(!doFog) {
                FogType fogtype = camera.getFluidInCamera();
                if(fogtype == FogType.NONE) {
                    Entity cameraEntity = camera.getEntity();
                    if(cameraEntity instanceof LivingEntity entity && (entity.hasEffect(MobEffects.BLINDNESS))) return;
                    Matrix4f matrix = poseStack.last().pose();
                    RenderSystem.disableTexture();
                    Vec3 skyColor = level.getSkyColor(minecraft.gameRenderer.getMainCamera().getPosition(), partialTick);
                    float skyR = (float) skyColor.x;
                    float skyG = Math.max(0.01F, (float) skyColor.y);
                    float skyB = Math.max(0.03F, (float) skyColor.z);
                    FogRenderer.levelFogColor();
                    BufferBuilder skyBuilder = Tesselator.getInstance().getBuilder();
                    RenderSystem.depthMask(false);
                    RenderSystem.setShaderColor(skyR, skyG, skyB, 1.0F);
                    ShaderInstance discShader = RenderSystem.getShader();
                    skyBuffer.drawWithShader(matrix, RenderSystem.getProjectionMatrix(), discShader);
                    RenderSystem.enableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    poseStack.pushPose();
                    float skyVisibility = 1.0F - level.getRainLevel(partialTick);
                    ILevelData capL = LevelData.get(level);
                    if(capL.getGlobalWeather() == Weather.FOG) {
                        float fogVisibility = Math.min(1F, capL.getFogIntensity());
                        skyVisibility = Math.min(skyVisibility, 1.2F - fogVisibility);
                    }
                    float starAlpha = level.getStarBrightness(partialTick) * skyVisibility; //Maxes out at 0.5
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, skyVisibility);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0F));
                    Matrix4f celestialMatrix = poseStack.last().pose();
                    poseStack.popPose();
                    //Stars
                    if(starAlpha > 0.0F) {
                        RenderSystem.setShaderColor(starAlpha * 2F, starAlpha * 2F, starAlpha * 2F, starAlpha * 1.2F);
                        RenderSystem.setShaderTexture(0, STARS_LOCATION);
                        starBuffer.drawWithShader(celestialMatrix, RenderSystem.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                    }

                    //Cover stars past the horizon and makes skybox black in large caves
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    RenderSystem.disableBlend();
                    RenderSystem.disableTexture();

                    double eyeY = minecraft.player.getEyePosition(partialTick).y - ContinentalChunkGenerator.SEA_LEVEL;
                    //Black in caves
                    if(eyeY + 16 < 0.0D && minecraft.level.getBrightness(LightLayer.SKY, minecraft.player.eyeBlockPosition()) <= 0) {
                        RenderSystem.setShaderColor(0F, 0F, 0F, 1.0F);
                    }
                    //Match fog color elsewhere
                    else {
                        RenderSystem.setShaderColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                    }
                    darkBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), discShader);

                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableBlend();
                    //Sunset
                    float[] sunriseColors = super.getSunriseColor(level.getTimeOfDay(partialTick), partialTick);
                    if(sunriseColors != null) {
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        poseStack.pushPose();
                        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                        float f2 = Mth.sin(level.getSunAngle(partialTick)) < 0.0F ? 180.0F : 0.0F;
                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(f2));
                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                        float f3 = sunriseColors[0];
                        float f4 = sunriseColors[1];
                        float f5 = sunriseColors[2];
                        Matrix4f sunsetMatrix = poseStack.last().pose();
                        skyBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                        skyBuilder.vertex(sunsetMatrix, 0.0F, 100.0F, 0.0F).color(f3, f4, f5, sunriseColors[3]).endVertex();
                        for(int j = 0; j <= 16; ++j) {
                            float f6 = (float)j * ((float)Math.PI * 2F) / 16.0F;
                            float f7 = Mth.sin(f6);
                            float f8 = Mth.cos(f6);
                            skyBuilder.vertex(sunsetMatrix, f7 * 120.0F, f8 * 120.0F, -f8 * 40.0F * sunriseColors[3]).color(sunriseColors[0], sunriseColors[1], sunriseColors[2], 0.0F).endVertex();
                        }
                        skyBuilder.end();
                        BufferUploader.end(skyBuilder);
                        poseStack.popPose();
                    }
                    RenderSystem.enableTexture();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    //Sun
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, SUN_LOCATION);
                    RenderSystem.setShaderColor(1, 1, 1, (1 - starAlpha * 2) * skyVisibility);
                    skyBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    skyBuilder.vertex(celestialMatrix, -SUN_SIZE, 100.0F, -SUN_SIZE).uv(0.0F, 0.0F).endVertex();
                    skyBuilder.vertex(celestialMatrix, SUN_SIZE, 100.0F, -SUN_SIZE).uv(1.0F, 0.0F).endVertex();
                    skyBuilder.vertex(celestialMatrix, SUN_SIZE, 100.0F, SUN_SIZE).uv(1.0F, 1.0F).endVertex();
                    skyBuilder.vertex(celestialMatrix, -SUN_SIZE, 100.0F, SUN_SIZE).uv(0.0F, 1.0F).endVertex();
                    skyBuilder.end();
                    BufferUploader.end(skyBuilder);

                    RenderSystem.defaultBlendFunc();
                    //Moon
                    RenderSystem.setShaderTexture(0, MOON_LOCATION);
                    RenderSystem.setShaderColor(1, 1, 1, starAlpha * 2F);
                    int phase = level.getMoonPhase();
                    int l = phase % 4;
                    int i1 = phase / 4 % 2;
                    float f13 = (float)(l) / 4.0F;
                    float f14 = (float)(i1) / 2.0F;
                    float f15 = (float)(l + 1) / 4.0F;
                    float f16 = (float)(i1 + 1) / 2.0F;
                    skyBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    skyBuilder.vertex(celestialMatrix, -MOON_SIZE, -100.0F, MOON_SIZE).uv(f15, f16).endVertex();
                    skyBuilder.vertex(celestialMatrix, MOON_SIZE, -100.0F, MOON_SIZE).uv(f13, f16).endVertex();
                    skyBuilder.vertex(celestialMatrix, MOON_SIZE, -100.0F, -MOON_SIZE).uv(f13, f14).endVertex();
                    skyBuilder.vertex(celestialMatrix, -MOON_SIZE, -100.0F, -MOON_SIZE).uv(f15, f14).endVertex();
                    skyBuilder.end();
                    BufferUploader.end(skyBuilder);

                    if(level.effects().hasGround()) {
                        RenderSystem.setShaderColor(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F, 1.0F);
                    }
                    else RenderSystem.setShaderColor(skyR, skyG, skyB, 1.0F);
                    RenderSystem.enableTexture();
                    RenderSystem.depthMask(true);
                }
            }
        }));
        setCloudRenderHandler((ticks, partialTick, stack, level, mc, camX, camY, camZ) -> {
            if(!initialized || mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE ||
                    (mc.gameRenderer.getMainCamera().getEntity() instanceof LivingEntity entity && entity.hasEffect(MobEffects.BLINDNESS))) return;

            float levelHeight = level.effects().getCloudHeight();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);
            double renderX, renderY, renderZ, offX, offY, offZ;
            float drift = (ticks + partialTick) * 0.03F;
            if(mc.player != null) {
                Vec3 pos = mc.player.getEyePosition(partialTick);
                renderX = pos.x + drift;
                renderY = levelHeight - pos.y;
                renderZ = pos.z;
                offX = renderX - camX - (renderX % CLOUD_WIDTH);
                offY = pos.y - camY - (pos.y % CLOUD_HEIGHT);
                offZ = pos.z - camZ - (pos.z % CLOUD_WIDTH);
            }
            else {
                renderX = camX + drift;
                renderY = levelHeight - camY;
                renderZ = camZ;
                offX = -renderX % CLOUD_WIDTH;
                offY = renderY % CLOUD_HEIGHT;
                offZ = -renderZ % CLOUD_WIDTH;
            }
            Vec3 color = level.getCloudColor(partialTick);
            int cloudX = Mth.floor(renderX / CLOUD_WIDTH) * CLOUD_WIDTH;
            int cloudY = Mth.floor(renderY / CLOUD_HEIGHT) * CLOUD_HEIGHT;
            int cloudZ = Mth.floor(renderZ / CLOUD_WIDTH) * CLOUD_WIDTH;
            if(renderX > 0) offX += CLOUD_WIDTH;
            if(renderZ > 0) offZ += CLOUD_WIDTH;
            double time = level.getGameTime() / 200L;
            if(time != prevTime || cloudX != prevCloudX || cloudY != prevCloudY || cloudZ != prevCloudZ || mc.options.getCloudsType() != prevCloudsType || prevCloudColor.distanceToSqr(color) > 2.0E-4D) {
                prevCloudX = cloudX;
                prevCloudY = cloudY;
                prevCloudZ = cloudZ;
                prevTime = time;
                prevCloudColor = color;
                prevCloudsType = mc.options.getCloudsType();
                regenClouds = true;
            }

            if(regenClouds) {
                regenClouds = false;
                BufferBuilder cloudBuilder = Tesselator.getInstance().getBuilder();
                if(cloudBuffer != null) cloudBuffer.close();
                cloudBuffer = new VertexBuffer();
                buildClouds(cloudBuilder, level, cloudX, cloudY, cloudZ, cloudX - drift, cloudZ, color, time);
            }

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            FogRenderer.levelFogColor();
            stack.pushPose();
            stack.translate(offX - drift + 0.0015F, offY + 0.0015F, offZ + 0.0015F);
            stack.scale(0.999F, 0.999F, 0.999F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if(cloudBuffer != null) {
                ShaderInstance shader = RenderSystem.getShader();
                RenderSystem.colorMask(false, false, false, false);
                cloudBuffer.drawWithShader(stack.last().pose(), RenderSystem.getProjectionMatrix(), shader);
                RenderSystem.colorMask(true, true, true, true);
                cloudBuffer.drawWithShader(stack.last().pose(), RenderSystem.getProjectionMatrix(), shader);
            }

            stack.popPose();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        });
        setWeatherRenderHandler((ticks, partialTick, level, minecraft, lightTexture, camX, camY, camZ) -> {

        });
        setWeatherParticleRenderHandler(((ticks, level, mc, camera) -> {
            ILevelData capL = LevelData.get(level);
            if(capL.getGlobalWeatherIntensity() > 0.15F) {
                BlockPos camPos = camera.getBlockPosition();
                BlockPos soundPos = null;
                for(int i = 0; i < 60; ++i) {
                    int x = level.random.nextInt(21) - 10;
                    int z = level.random.nextInt(21) - 10;
                    BlockPos rainPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, camPos.offset(x, 0, z));
                    if(rainPos.getY() > level.getMinBuildHeight() && rainPos.getY() <= camPos.getY() + 10 && rainPos.getY() >= camPos.getY() - 10
                            && capL.getWeather(ChunkData.get(level.getChunkAt(rainPos)), rainPos) == Weather.RAIN) {
                        soundPos = rainPos.below();
                        break;
                    }
                }
                if(soundPos != null) {
                    float intensity = Mth.clamp(capL.getWeatherIntensity(ChunkData.get(level.getChunkAt(soundPos)), soundPos), 0.25F, 1F);
                    if(level.random.nextInt(3 + Math.round(3 * (1F - intensity))) < rainSoundTime++) {
                        float volume = 0.25F * intensity;
                        float pitch = 0.9F - (0.2F * intensity);
                        rainSoundTime = 0;
                        if(soundPos.getY() > camPos.getY() + 1 && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, camPos).getY() > camPos.getY()) {
                            mc.level.playLocalSound(soundPos, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, volume * 0.5F, pitch * 0.5F, false);
                        }
                        else mc.level.playLocalSound(soundPos, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, volume, pitch, false);
                    }
                }
            }
        }));
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 pFogColor, float pBrightness) {
        return pFogColor.multiply(pBrightness * 0.94F + 0.06F, pBrightness * 0.94F + 0.06F, pBrightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int pY) {
        return false;
    }

    @Override
    public @Nullable float[] getSunriseColor(float pTimeOfDay, float pPartialTicks) {
        return null; //Stop FogRenderer from using sunrise
    }

    private void buildClouds(BufferBuilder builder, Level level, int centerX, int startY, int centerZ, double worldX, int worldZ, Vec3 color, double time) {
        ILevelData capL = LevelData.get(level);
        float density = Math.min(capL.getGlobalWeatherIntensity() + 0.425F, 1F);
        float minOpacity = 1F - density;
        if(minOpacity < 0F) return;
        density = 1F + density * 2F;
        time *= 0.05D;
        float presence = Math.max(0, capL.getGlobalWeatherIntensity() + 0.2F);
        presence *= presence;
        float rainLevel = level.getRainLevel(1F);
        float offset = 9.765625E-4F;
        float r = (float) color.x, g = (float) color.y, b = (float) color.z;
        float r9 = r * 0.9F, g9 = g * 0.9F, b9 = b * 0.9F;
        float r7 = r * 0.7F, g7 = g * 0.7F, b7 = b * 0.7F;
        float r8 = r * 0.8F, g8 = g * 0.8F, b8 = b * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int maxSize = prevCloudsType == CloudStatus.FANCY ? 48 : 32;
        int size = Math.min(maxSize, Minecraft.getInstance().options.renderDistance * 2) * CLOUD_WIDTH;
        int sampleSize = prevCloudsType == CloudStatus.FANCY ? 3 : 15;

        for(int x = -size; x <= size; x += CLOUD_WIDTH) {
            for(int z = -size; z <= size; z += CLOUD_WIDTH) {
                float frequency = 1F / CLOUD_WIDTH;
                int noiseX = centerX + x, noiseZ = centerZ + z;
                float a = Math.abs(opacityNoise.noise3D(noiseX * frequency, time, noiseZ * frequency));
                if(a < minOpacity) continue;
                a = Math.min(1F, (a + rainLevel * 0.2F) * density);

                //Fade clouds that are close to ground
                int maxGroundOffset = 0;
                for(int xOff = 0; xOff < CLOUD_WIDTH; xOff += sampleSize) {
                    for(int zOff = 0; zOff < CLOUD_WIDTH; zOff += sampleSize) {
                        int groundOffset = Math.max(0, level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(worldX + x + xOff), worldZ + z + zOff)
                                - ContinentalChunkGenerator.SEA_LEVEL);
                        if(groundOffset > maxGroundOffset) maxGroundOffset = groundOffset;
                    }
                }
                float fade = Math.min(1F, maxGroundOffset / ((getCloudHeight() - ContinentalChunkGenerator.SEA_LEVEL) * 0.5F));
                a *= 1F - fade * fade;
                if(a < 0.3F) continue;

                //Lower & fade far away clouds
                float dist = Math.max(Math.abs(x), Math.abs(z));
                int yFar = Mth.floor((dist * dist) * 0.00025F) / CLOUD_HEIGHT * CLOUD_HEIGHT;
                if(yFar > 48) a *= 1F - Mth.clamp((yFar - 48F) / (48F * 4F), 0.0F, 0.5F);
                yFar = Mth.floor(yFar * presence) / CLOUD_HEIGHT * CLOUD_HEIGHT;

                int y = (startY - yFar)
                        + (int) (minYNoise.noise3D(noiseX * frequency * 5F, time, noiseZ * frequency * 5F) * 30F) / CLOUD_HEIGHT * CLOUD_HEIGHT;
                int ySize = yFar / 8 + Math.max(CLOUD_HEIGHT, Math.abs((int) (maxYNoise.noise3D(noiseX * frequency, time, noiseZ * frequency) * 100F)) / CLOUD_HEIGHT * CLOUD_HEIGHT);
                //Bottom
                if(y >= -ySize) {
                    builder.vertex((x), (y), (z + CLOUD_WIDTH)).color(r7, g7, b7, a).normal(0.0F, -1.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y), (z + CLOUD_WIDTH)).color(r7, g7, b7, a).normal(0.0F, -1.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y), (z)).color(r7, g7, b7, a).normal(0.0F, -1.0F, 0.0F).endVertex();
                    builder.vertex((x), (y), (z)).color(r7, g7, b7, a).normal(0.0F, -1.0F, 0.0F).endVertex();
                }
                //Top
                if(y <= ySize) {
                    builder.vertex((x), (y + ySize - offset), (z + CLOUD_WIDTH)).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y + ySize - offset), (z + CLOUD_WIDTH)).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y + ySize - offset), (z)).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.vertex((x), (y + ySize - offset), (z)).color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
                }
                //Sides
                if(x >= -CLOUD_WIDTH) {
                    builder.vertex((x), (y), (z + CLOUD_WIDTH)).color(r9, g9, b9, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x), (y + ySize), (z + CLOUD_WIDTH)).color(r9, g9, b9, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x), (y + ySize), (z)).color(r9, g9, b9, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x), (y), (z)).color(r9, g9, b9, a).normal(-1.0F, 0.0F, 0.0F).endVertex();
                }
                if(x <= CLOUD_WIDTH) {
                    builder.vertex((x + CLOUD_WIDTH - offset), (y), (z + CLOUD_WIDTH)).color(r9, g9, b9, a).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH - offset), (y + ySize), (z + CLOUD_WIDTH)).color(r9, g9, b9, a).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH - offset), (y + ySize), (z)).color(r9, g9, b9, a).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH - offset), (y), (z)).color(r9, g9, b9, a).normal(1.0F, 0.0F, 0.0F).endVertex();
                }
                if(z >= -CLOUD_WIDTH) {
                    builder.vertex((x), (y + ySize), (z)).color(r8, g8, b8, a).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y + ySize), (z)).color(r8, g8, b8, a).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y), (z)).color(r8, g8, b8, a).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.vertex((x), (y), (z)).color(r8, g8, b8, a).normal(0.0F, 0.0F, -1.0F).endVertex();
                }
                if(z <= CLOUD_WIDTH) {
                    builder.vertex((x), (y + ySize), (z + CLOUD_WIDTH - offset)).color(r8, g8, b8, a).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y + ySize), (z + CLOUD_WIDTH - offset)).color(r8, g8, b8, a).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.vertex((x + CLOUD_WIDTH), (y), (z + CLOUD_WIDTH - offset)).color(r8, g8, b8, a).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.vertex((x), (y), (z + CLOUD_WIDTH - offset)).color(r8, g8, b8, a).normal(0.0F, 0.0F, 1.0F).endVertex();
                }
            }
        }

        builder.end();
        cloudBuffer.upload(builder);
    }

    public static void buildSkyDisc(BufferBuilder pBuilder, float pY) {
        float f = Math.signum(pY) * 512.0F;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        pBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        pBuilder.vertex(0.0D, (double)pY, 0.0D).endVertex();

        for(int i = -180; i <= 180; i += 45) {
            pBuilder.vertex(f * Mth.cos((float)i * ((float)Math.PI / 180F)), pY, 512.0F * Mth.sin((float)i * ((float)Math.PI / 180F))).endVertex();
        }

        pBuilder.end();
    }

    public static void buildStars(BufferBuilder builder) {
        Random random = new Random(10842L);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for(int i = 0; i < 3000; ++i) {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;
                double xPos = d0 * 100.0D;
                double yPos = d1 * 100.0D;
                double zPos = d2 * 100.0D;
                if(yPos < 0F && xPos >= -MOON_SIZE && xPos <= MOON_SIZE && zPos >= -MOON_SIZE && zPos <= MOON_SIZE) continue;
                if(yPos > 0F && xPos >= -SUN_SIZE && xPos <= SUN_SIZE && zPos >= -SUN_SIZE && zPos <= SUN_SIZE) continue;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);
                float uv;
                float colorType = random.nextFloat();
                if(colorType < 0.4F) uv = 0.2F;
                else if(colorType < 0.75F) uv = 0.4F;
                else if(colorType < 0.9F) uv = 0.6F;
                else if(colorType < 0.97F) uv = 0.8F;
                else uv = 0.99F;
                for(int j = 0; j < 4; ++j) {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    builder.vertex((float) (xPos + d25), (float) (yPos + d23), (float) (zPos + d26)).uv(0, uv).endVertex();
                }
            }
        }
        builder.end();
    }
}
