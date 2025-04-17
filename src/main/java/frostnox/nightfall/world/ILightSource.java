package frostnox.nightfall.world;

public interface ILightSource {
    /**
     * @return how bright the source is at its center (0 to 15)
     */
    int getBrightness();

    /**
     * @return squared radius in blocks that light should spread across (8 to 14 * 14)
     */
    double getLightRadiusSqr();
}
