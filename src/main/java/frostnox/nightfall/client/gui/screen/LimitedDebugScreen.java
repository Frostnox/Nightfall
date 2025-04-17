package frostnox.nightfall.client.gui.screen;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;

import java.util.List;

public class LimitedDebugScreen extends DebugScreenOverlay {
    protected final Minecraft minecraft;

    public LimitedDebugScreen(Minecraft minecraft) {
        super(minecraft);
        this.minecraft = minecraft;
    }

    @Override
    protected void drawGameInformation(PoseStack pPoseStack) {
        List<String> list = this.getGameInformation();
        for(int i = 0; i < list.size(); ++i) {
            String s = list.get(i);
            if(!Strings.isNullOrEmpty(s)) {
                int j = 9;
                int k = minecraft.font.width(s);
                int i1 = 2 + j * i;
                fill(pPoseStack, 1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                minecraft.font.draw(pPoseStack, s, 2.0F, (float)i1, 14737632);
            }
        }
    }

    @Override
    protected List<String> getGameInformation() {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        Connection connection = this.minecraft.getConnection().getConnection();
        float f = connection.getAverageSentPackets();
        float f1 = connection.getAverageReceivedPackets();
        String s;
        if(integratedserver != null) s = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedserver.getAverageTickTime(), f, f1);
        else s = String.format("\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, f1);
        return Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion()
                + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, s);
    }

    @Override
    protected List<String> getSystemInformation() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        return Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format("CPU: %s", GlUtil.getCpuInfo()), "", String.format("Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()), GlUtil.getRenderer(), GlUtil.getOpenGLVersion());
    }

    protected static long bytesToMegabytes(long pBytes) {
        return pBytes / 1024L / 1024L;
    }
}
