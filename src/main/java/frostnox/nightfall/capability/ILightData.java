package frostnox.nightfall.capability;

import frostnox.nightfall.world.ILightSource;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface ILightData extends ILightSource {
    Entity getEntity();

    void setBrightness(int brightness);

    void setLightRadiusSqr(double lightRadiusSqr);

    int getLastProcessedBrightness();

    void setLastProcessedBrightness(int brightness);

    double getLastProcessedLightRadiusSqr();

    void setLastProcessedLightRadiusSqr(double lightRadiusSqr);

    double getLightX();

    double getLightY();

    double getLightZ();

    void setLightX(double x);

    void setLightY(double y);

    void setLightZ(double z);

    void updateLight();

    void inWaterTickServer();

    void init();

    boolean notifyClientOnStopTracking();

    void setupClientNotifications();

    Object2DoubleMap<BlockPos> getLightMap();

    boolean isLightDirty();

    void setLightDirty(boolean dirty);
}
