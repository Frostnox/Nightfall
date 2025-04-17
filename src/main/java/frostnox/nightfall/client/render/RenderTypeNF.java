package frostnox.nightfall.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.ClientEngine;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL11C;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.OptionalDouble;
import java.util.function.Function;

public class RenderTypeNF extends RenderType {
    private static ShaderInstance positionColorNormalShader;

    public static final RenderType LINES_DEPTH = create(Nightfall.MODID + ":lines_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256,
            false, false, CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).createCompositeState(false));
    //Same as vanilla eyes but write to depth so it doesn't render behind translucent entities
    public static final Function<ResourceLocation, RenderType> EYES = Util.memoize((loc) -> {
        RenderStateShard.TextureStateShard shard = new RenderStateShard.TextureStateShard(loc, false, false);
        return create("eyes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(shard).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));
    });

    public RenderTypeNF(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    /**
     * Unused vanilla shader, updated to work with current version
     */
    public static @Nullable ShaderInstance getPositionColorNormalShader() {
        return positionColorNormalShader;
    }

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), ResourceLocation.parse("position_color_normal"), DefaultVertexFormat.POSITION_COLOR_NORMAL),
                (instance) -> positionColorNormalShader = instance);
    }
}
