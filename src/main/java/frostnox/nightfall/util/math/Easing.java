package frostnox.nightfall.util.math;

import frostnox.nightfall.util.MathUtil;
import net.minecraft.util.Mth;

/**
 * Easing functions from easings.net
 */
public enum Easing {
    none {
        @Override
        public float apply(float p) {
            return p;
        }
    },
    inSine {
        @Override
        public float apply(float p) {
            return 1 - Mth.cos(p * MathUtil.PI / 2F);
        }
    },
    outSine {
        @Override
        public float apply(float p) {
            return Mth.sin(p * MathUtil.PI / 2F);
        }
    },
    inOutSine {
        @Override
        public float apply(float p) {
            return -(Mth.cos(MathUtil.PI * p) - 1F) / 2F;
        }
    },
    inQuart {
        @Override
        public float apply(float p) {
            return p * p * p * p;
        }
    },
    outQuart {
        @Override
        public float apply(float p) {
            return 1 - (float) Math.pow(1 - p, 4);
        }
    },
    inOutQuart {
        @Override
        public float apply(float p) {
            return p < 0.5F ? 8 * p * p * p * p : 1F - (float) Math.pow(-2 * p + 2, 4) / 2F;
        }
    },
    inQuad {
        @Override
        public float apply(float p) {
            return p * p;
        }
    },
    outQuad {
        @Override
        public float apply(float p) {
            return 1 - (1 - p) * (1 - p);
        }
    },
    inOutQuad {
        @Override
        public float apply(float p) {
            return p < 0.5F ? 2 * p * p : 1 - (float) Math.pow(-2 * p + 2, 2) / 2F;
        }
    },
    inCubic {
        @Override
        public float apply(float p) {
            return p * p * p;
        }
    },
    outCubic {
        @Override
        public float apply(float p) {
            return 1 - (float) Math.pow(1 - p, 3);
        }
    },
    inOutCubic {
        @Override
        public float apply(float p) {
            return p < 0.5F ? 4 * p * p * p : 1 - (float) Math.pow(-2 * p + 2, 3) / 2;
        }
    },
    inBack {
        @Override
        public float apply(float p) {
            return b * p * p * p - a * p * p;
        }
    },
    outBack {
        @Override
        public float apply(float p) {
            return 1 + b * (float) Math.pow(p - 1, 3) + a * (float) Math.pow(p - 1, 2);
        }
    },
    inOutBack {
        @Override
        public float apply(float p) {
            return p < 0.5F ? ((float) Math.pow(2F * p, 2F) * ((c + 1F) * 2F * p - c)) / 2F : ((float) Math.pow(2F * p - 2F, 2F) * ((c + 1F) * (p * 2F - 2F) + c) + 2F) / 2F;
        }
    };

    private static final float a = 1.70158F;
    private static final float b = a + 1F;
    private static final float c = a * 1.525F;

    public double apply(double p) {
        return apply((float) p);
    }

    public abstract float apply(float p);
}
