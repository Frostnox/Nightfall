package frostnox.nightfall.util.math.noise;

/**
 * Fractional brownian motion applied to simplex noise (cached).
 * Generates only 3D noise, primarily intended for use within world generation.
 * Must call init function before use. Make sure to call delete function after use.
 */
public class FractalSimplexNoiseCached {
    public final SimplexNoiseCached noiseSource;
    private final ThreadLocal<SimplexNoiseCached.Generator[]> generators = new ThreadLocal<>();
    public final float frequency, persistence, lacunarity;

    public FractalSimplexNoiseCached(long seed, float frequency, float persistence, float lacunarity) {
        this.noiseSource = new SimplexNoiseCached(seed);
        this.frequency = frequency;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
    }

    public void initGenerators(int octaves) {
        this.generators.set(new SimplexNoiseCached.Generator[octaves]);
        for(int i = 0; i < octaves; i++) generators.get()[i] = noiseSource.generator();
    }

    public void deleteGenerators() {
        generators.remove();
    }

    public void setXZ(double x, double z) {
        float frequency = this.frequency;
        for(SimplexNoiseCached.Generator generator : generators.get()) {
            generator.setXZ(x * frequency, z * frequency);
            frequency *= lacunarity;
        }
    }

    public float getForY(double y) {
        float noise = 0;
        float amplitudes = 0;
        float frequency = this.frequency;
        float amplitude = 1;
        for(SimplexNoiseCached.Generator generator : generators.get()) {
            noise += generator.getForY(y * frequency) * amplitude;
            amplitudes += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return noise / amplitudes;
    }
}
