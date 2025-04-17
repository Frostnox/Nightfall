package frostnox.nightfall.util.math.noise;

/**
 * Fractional brownian motion applied to simplex noise (fast).
 * Best suited for 2D noise since visual artifacts worsen with increasing dimensions.
 */
public class FractalSimplexNoiseFast {
    public final SimplexNoiseFast noiseSource;
    private final float frequency, persistence, lacunarity;
    private final int octaves;

    public FractalSimplexNoiseFast(long seed, float frequency, int octaves, float persistence, float lacunarity) {
        this.noiseSource = new SimplexNoiseFast(seed);
        this.frequency = frequency;
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
    }

    public float noise2D(double x, double z) {
        float noise = 0;
        float amplitudes = 0;
        float frequency = this.frequency;
        float amplitude = 1;
        for(int i = 0; i < octaves; i++) {
            noise += noiseSource.noise2D(x * frequency, z * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }

    public float noise3D(double x, double y, double z) {
        float noise = 0;
        float amplitudes = 0;
        float frequency = this.frequency;
        float amplitude = 1;
        for(int i = 0; i < octaves; i++) {
            noise += noiseSource.noise3D(x * frequency, y * frequency, z * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }

    public float noise4D(double x, double y, double z, double w) {
        float noise = 0;
        float amplitudes = 0;
        float frequency = this.frequency;
        float amplitude = 1;
        for(int i = 0; i < octaves; i++) {
            noise += noiseSource.noise4D(x * frequency, y * frequency, z * frequency, w * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }
}
