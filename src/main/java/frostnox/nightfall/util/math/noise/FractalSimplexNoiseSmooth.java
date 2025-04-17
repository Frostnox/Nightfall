package frostnox.nightfall.util.math.noise;

/**
 * Fractional brownian motion applied to simplex noise (smooth).
 * Best suited for 3D+ noise that can't be cached and 2D noise where a smoother appearance is preferred.
 */
public class FractalSimplexNoiseSmooth {
    public final SimplexNoiseSmooth noiseSource;
    private final float frequency, persistence, lacunarity;
    private final int octaves;

    public FractalSimplexNoiseSmooth(long seed, float frequency, int octaves, float persistence, float lacunarity) {
        this.noiseSource = new SimplexNoiseSmooth(seed);
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

    public double noise3D(double x, double y, double z) {
        double noise = 0;
        double amplitudes = 0;
        double frequency = this.frequency;
        double amplitude = 1;
        for(int i = 0; i < octaves; i++) {
            noise += noiseSource.noise3D(x * frequency, y * frequency, z * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }

    public double noise4D(double x, double y, double z, double w) {
        double noise = 0;
        double amplitudes = 0;
        double frequency = this.frequency;
        double amplitude = 1;
        for(int i = 0; i < octaves; i++) {
            noise += noiseSource.noise4D(x * frequency, y * frequency, z * frequency, w * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }
}
