package frostnox.nightfall.util;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Mat4f;
import frostnox.nightfall.util.math.Quat;
import it.unimi.dsi.fastutil.ints.IntLongImmutablePair;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MathUtil {
    public static final float PI = 3.141592653589793F, PI_SQRT = (float) Math.sqrt(PI);
    public static final float SQRT_2 = (float) Math.sqrt(2);
    public static final double IDEAL_OPENSIMPLEX2_PAIR_VERTICAL_SAMPLING_OFFSET = 0.4330127018922193; // sqrt(3)/4, half the distance between two lattice points vertically

    public static float toRadians(float degrees) {
        return degrees / 180F * PI;
    }

    public static Vector3f toRadians(Vector3f vec) {
        return new Vector3f(toRadians(vec.x()), toRadians(vec.y()), toRadians(vec.z()));
    }

    public static float toDegrees(float radians) {
        return radians / PI * 180F;
    }

    public static Vector3f toDegrees(Vector3f vec) {
        return new Vector3f(toDegrees(vec.x()), toDegrees(vec.y()), toDegrees(vec.z()));
    }

    public static float getAngleDegrees(double z, double x) {
        return (float) ((Mth.atan2(z, x) * 180D / Math.PI) + 270D) % 360F;
    }

    public static float lerp2D(float xAmount, float yAmount, float minXMinY, float maxXMinY, float minXMaxY, float maxXMaxY) {
        return Mth.lerp(yAmount, Mth.lerp(xAmount, minXMinY, maxXMinY), Mth.lerp(xAmount, minXMaxY, maxXMaxY));
    }

    public static int distManhattanXZ(Vec3i pos1, Vec3i pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getZ() - pos2.getZ());
    }

    public static Vector3f rotatePointByYaw(Vector3f point, float yawDegrees, @Nullable Vec2 offset) {
        if(yawDegrees == 0) return point.copy();
        if(offset != null) point.add(offset.x, 0F, offset.y);
        float cos = Mth.cos(toRadians(yawDegrees));
        float sin = Mth.sin(toRadians(yawDegrees));
        Vector3f vec = new Vector3f(point.x() * cos - point.z() * sin, point.y(), point.x() * sin + point.z() * cos);
        if(offset != null) vec.add(-offset.x, 0, -offset.y);
        return vec;
    }

    public static Vector3f rotatePointByYaw(Vector3f point, float yawDegrees) {
        return rotatePointByYaw(point, yawDegrees, null);
    }

    public static Vector3f getShortestPointFromPointToBox(float xPos, float yPos, float zPos, AABB box) {
        //Slight offset improves collision detection on corners
        float x = (float) Mth.clamp(xPos, box.minX + 0.0001D, box.maxX - 0.0001D);
        float y = (float) Mth.clamp(yPos, box.minY + 0.0001D, box.maxY - 0.0001D);
        float z = (float) Mth.clamp(zPos, box.minZ + 0.0001D, box.maxZ - 0.0001D);
        return new Vector3f(x, y, z);
    }

    public static Vector3d getShortestPointFromPointToBox(double xPos, double yPos, double zPos, AABB box) {
        double x = Mth.clamp(xPos, box.minX + 0.0001D, box.maxX - 0.0001D);
        double y = Mth.clamp(yPos, box.minY + 0.0001D, box.maxY - 0.0001D);
        double z = Mth.clamp(zPos, box.minZ + 0.0001D, box.maxZ - 0.0001D);
        return new Vector3d(x, y, z);
    }

    public static float getShortestDistanceSqrPointToBox(float xPos, float yPos, float zPos, AABB box) {
        Vector3f vec = getShortestPointFromPointToBox(xPos, yPos, zPos, box);
        return (vec.x() - xPos) * (vec.x() - xPos) + (vec.y() - yPos) * (vec.y() - yPos) + (vec.z() - zPos) * (vec.z() - zPos);
    }

    public static double getShortestDistanceSqrPointToBox(double xPos, double yPos, double zPos, AABB box) {
        Vector3d vec = getShortestPointFromPointToBox(xPos, yPos, zPos, box);
        return (vec.x - xPos) * (vec.x - xPos) + (vec.y - yPos) * (vec.y - yPos) + (vec.z - zPos) * (vec.z - zPos);
    }

    public static double getShortestDistanceSqrBoxToBox(AABB box1, AABB box2) {
        double x1 = Math.max(0, box1.minX - box2.maxX);
        double y1 = Math.max(0, box1.minY - box2.maxY);
        double z1 = Math.max(0, box1.minZ - box2.maxZ);
        double x2 = Math.max(0, box2.minX - box1.maxX);
        double y2 = Math.max(0, box2.minY - box1.maxY);
        double z2 = Math.max(0, box2.minZ - box1.maxZ);
        return x1 * x1 + y1 * y1 + z1 * z1 + x2 * x2 + y2 * y2 + z2 * z2;
    }

    //Algorithm from https://github.com/KdotJPG/Cave-Tech-Demo/blob/master/src/main/java/jpg/k/cavetechdemo/carver/DerivativeTunnelClosingCaveCarver.java
    public static double getCaveClosingValue(Vector3d dir1, Vector3d dir2) {
        double noiseDeltaMagSq1 = dir1.x*dir1.x + dir1.y*dir1.y + dir1.z*dir1.z;
        double noiseDeltaMagSq2 = dir2.x*dir2.x + dir2.y*dir2.y + dir2.z*dir2.z;
        double noiseDeltaDot = dir1.x*dir2.x + dir1.y*dir2.y + dir1.z*dir2.z;
        double squaredNormalizedDotProduct = (noiseDeltaDot * noiseDeltaDot) / (noiseDeltaMagSq1 * noiseDeltaMagSq2);
        return (squaredNormalizedDotProduct * squaredNormalizedDotProduct);
    }

    public static float lerp3D(float xm_ym_zm, float xp_ym_zm, float xm_yp_zm, float xp_yp_zm,
                                float xm_ym_zp, float xp_ym_zp, float xm_yp_zp, float xp_yp_zp,
                                float x, float y, float z) {
        return (xm_ym_zm * (1 - x) * (1 - y) * (1 - z)) + (xp_ym_zm * x * (1 - y) * (1 - z)) + (xm_yp_zm * (1 - x) * y * (1 - z)) + (xp_yp_zm * x * y * (1 - z)) +
                (xm_ym_zp * (1 - x) * (1 - y) * z) + (xp_ym_zp * x * (1 - y) * z) + (xm_yp_zp * (1 - x) * y * z) + (xp_yp_zp * x * y * z);
    }

    /**
     * Ensure that either k is small or n - k is small to prevent overflow
     */
    public static long comb(long n, long k) {
        long c = 1L;
        for(int i = 0; i < Math.min(n - k, k); i++) {
            c = c * (n - i) / (i + 1);
        }
        return c;
    }

    /**
     * @param shape >= 1 (an algorithm exists for 0 < shape < 1 but is not implemented here)
     */
    public static double gammaSample(double shape, Random random) {
        //Same algorithm as in Apache Commons Math library, found at https://dl.acm.org/doi/pdf/10.1145/358407.358414
        if(shape < 1D) return 0D;
        double d = shape - 1D / 3D;
        double c = 1D / (3D * Math.sqrt(d));
        while(true) {
            double x = random.nextGaussian();
            double v = Math.pow(1D + c * x, 3);
            if(v > 0D) {
                double u = random.nextDouble();
                double xSqr = x * x;
                if(u < 1D - 0.0331D * xSqr * xSqr ||
                        Math.log(u) < 0.5D * xSqr + d * (1D - v + Math.log(v))) return d * v;
            }
        }
    }

    private static final double[] LOG_FACTORIALS = new double[] {
            0D,
            0D,
            Math.log(2D),
            Math.log(3D * 2D),
            Math.log(4D * 3D * 2D),
            Math.log(5D * 4D * 3D * 2D),
            Math.log(6D * 5D * 4D * 3D * 2D),
            Math.log(7D * 6D * 5D * 4D * 3D * 2D),
            Math.log(8D * 7D * 6D * 5D * 4D * 3D * 2D),
            Math.log(9D * 8D * 7D * 6D * 5D * 4D * 3D * 2D)
    };
    private static final double LOG_SQRT_2_PI = Math.log(Math.sqrt(2D * Math.PI));

    /**
     * @param mean > 0
     */
    public static long poissonSample(double mean, Random random) {
        if(mean < 30D) { //Untested: various sources give different max values for this algorithm, 30 is the lowest
            //Algorithm from https://web.archive.org/web/20170715084112/http://mathaa.epfl.ch/cours/PMMI2001/interactive/rng7.htm
            double a = Math.exp(-mean);
            double r = 1D;
            long x = -1L;
            while(r > a) {
                double u = random.nextDouble();
                r *= u;
                x++;
            }
            return x;
        }
        else {
            //Algorithm from https://research.wu.ac.at/ws/portalfiles/portal/18953249/document.pdf
            double c = mean + 0.445D;
            double smu = Math.sqrt(mean);
            double b = 0.931D + 2.53D * smu;
            double a = -0.059D + 0.02483 * b;
            double a2 = a * 2D;
            double invA = 1.1239D + 1.1328D / (b - 3.4D);
            double ur = 0.43D;
            double vr = 0.9277D - 3.6224D / (b - 2D);
            double vr086 = vr * 0.86D;
            double logMean = Math.log(mean);
            while(true) {
                double v = random.nextDouble();
                double u;
                if(v <= vr086) {
                    u = v / vr - ur;
                    return (long) Math.floor((2D * a / (0.5D - Math.abs(u)) + b) * u + c);
                }
                else if(v >= vr) u = random.nextDouble() - 0.5D;
                else {
                    u = v / vr - 0.93D;
                    u = Math.signum(u) * 0.5D - u;
                    v = random.nextDouble() * vr;
                }
                double us = 0.5D - Math.abs(u);
                if(us < 0.013D && v > us) continue;
                int k = (int) Math.floor((a2 / us + b) * u + c);
                v = v * invA / (a / (us * us) + b);
                if(k >= 10 && Math.log(v * smu) <= (k + 0.5D) * Math.log(mean / k) - mean - LOG_SQRT_2_PI + k - (1D / 12D - 1D / (360D * k * k)) / k) {
                    return k;
                }
                if(k >= 0 && k <= 9 && Math.log(v) <= k * logMean - mean - LOG_FACTORIALS[k]) return k;
            }
        }
    }

    /**
     * Effective as a negative binomial distribution
     */
    public static long gammaPoissonSample(int shape, double gammaScale, Random random) {
        return poissonSample(gammaSample(shape, random) * gammaScale, random);
    }

    /**
     * Best suited for low values of p and low values of either maxSuccesses or trials
     * @param p chance for success
     * @param maxSuccesses max significant value
     * @return number of successes determined by cumulative distribution function
     */
    public static int getRandomSuccesses(double p, long trials, int maxSuccesses, Random random) {
        int limit = (int) Math.min(trials, maxSuccesses);
        if(limit <= 0 || p <= 0D) return 0;
        if(p >= 1D) return limit;
        double d = random.nextDouble();
        double q = 1D - p;
        double c = 1; //trials choose s term, keep multiplying iterative term
        double pPow = 1; //p^s term, can keep multiplying since s increases linearly
        double qPow = Math.pow(q, trials); //q^(trials - s) term, can keep dividing in similar fashion
        //Cumulative distribution checking each success count
        //s = 0
        if(d < qPow) return 0;
        double pSum = qPow; //1 * 1 * qPow
        //s >= 1
        for(int s = 1; s < limit; s++) {
            c *= (trials - (s - 1)) / (double) s;
            pPow *= p;
            qPow /= q;
            pSum += c * pPow * qPow;
            if(d < pSum) return s;
        }
        return limit;
    }

    /**
     * Best suited for low values of p and low values of either maxSuccesses or trials
     * @param p chance for success
     * @param maxSuccesses max significant value
     * @param spacing minimum number of trials between successes
     * @return number of successes determined by cumulative distribution function
     */
    public static int getRandomSuccesses(double p, long trials, int maxSuccesses, int spacing, Random random) {
        int limit = (int) Math.min(trials, maxSuccesses);
        if(limit <= 0 || p <= 0D) return 0;
        if(p >= 1D) return limit;
        double q = 1D - p;
        double[] rawPs = new double[limit + 1];
        double totalP = 0;
        double pPow = 1; //p^s term, can keep multiplying since s increases linearly
        double qPow = Math.max(Double.MIN_VALUE, Math.pow(q, trials)); //q^(trials - s) term, can keep dividing in similar fashion
        //Raw cdf probabilities
        for(int s = 0; s <= limit; s++) {
            long spacedTrials = s == 0 ? 0 : ((s - 1L) * spacing); //Minimum possible spacing that must occur
            if(trials < s + spacedTrials) break;
            long availableTrials = trials - spacedTrials;
            double c = 1;
            for(int i = 0; i < s; i++) {
                c *= (availableTrials - i) / (double) (i + 1);
            }
            pPow *= p;
            qPow /= q;
            double rawP = c * pPow * qPow;
            rawPs[s] = rawP;
            totalP += rawP;
        }
        //Normalize cdf to remove combinations that are impossible due to spacing
        double[] normalPs = new double[limit + 1];
        double pSum = 0;
        for(int s = 0; s <= limit; s++) {
            if(rawPs[s] > 0) pSum += rawPs[s] / totalP;
            normalPs[s] = pSum;
        }
        double d = random.nextDouble();
        for(int s = 0; s <= limit; s++) {
            if(d < normalPs[s]) return s;
        }
        return 0;
    }

    /**
     * @param p chance for success
     * @param maxSuccesses max significant value
     * @return number of successes determined by {@code getRandomSuccesses} and remaining trials
     */
    public static IntLongPair getRandomSuccessesAndRemainingTrials(double p, long trials, int maxSuccesses, Random random) {
        int successes = getRandomSuccesses(p, trials, maxSuccesses, random);
        if(successes == maxSuccesses && successes < trials) {
            //From https://en.wikipedia.org/wiki/Negative_binomial_distribution#Gamma%E2%80%93Poisson_mixture
            //"we can view the negative binomial as a Poisson(λ) distribution, where λ is itself a random variable,
            //distributed as a gamma distribution with shape r and scale θ = (1 − p)/p"
            double scale = (1D - p) / p;
            long maxRemaining = trials - successes;
            long failures;
            //Sample is likely in range, but it isn't guaranteed so resample until it is
            do failures = gammaPoissonSample(successes, scale, random);
            while(failures > maxRemaining);
            return new IntLongImmutablePair(successes, maxRemaining - failures);
        }
        else return new IntLongImmutablePair(successes, 0L);
    }

    /**
     * @param point       point to transform
     * @param transforms  main rotations (in degrees) from first to last; translation is applied after each rotation
     * @param userMatrix  any rotations applied to PoseStack, done last
     * @param localMatrix any rotations around the origin, done first
     * @param translation extra translation applied after transforms (still affected by userMatrix)
     * @param offset      extra translation applied before any transforms
     * @return transformed point
     */
    public static Vector3f transformPoint(Vector3f point, AnimationData[] transforms, Mat4f userMatrix, Mat4f localMatrix, Vector3f translation, Vector3f offset) {
        point.add(offset);
        localMatrix.transformVector3f(point);

        //Else solution will work as well, this just avoids unnecessary operations
        if(transforms.length == 1) {
            //This is the right way to perform rotations to mimic vanilla
            //x is always inverted and rotations should be done in the opposite order (so XYZ instead of ZYX)
            Mat4f mat = new Mat4f();
            Vector3f rVec = transforms[0].rCalc.getTransformations();
            if(rVec.x() != 0) mat.multiply(new Quat(rVec.x(), Vector3f.XN, true));
            if(rVec.y() != 0) mat.multiply(new Quat(rVec.y(), Vector3f.YP, true));
            if(rVec.z() != 0) mat.multiply(new Quat(rVec.z(), Vector3f.ZP, true));
            mat.transformVector3f(point);
            Vector3f tVec = transforms[0].tCalc.getTransformations(); //This assumes data follows ModelPart format
            tVec.mul(1/16F);
            tVec.mul(1, -1, -1);
            point.add(tVec);
        }
        else if(transforms.length > 1) {
            Stack<Vector3f> offsets = new Stack<>();
            Stack<Vector3f> translations = new Stack<>();
            for(int i = transforms.length - 1; i > -1; i--) {
                offsets.push(transforms[i].offset.copy());
                Vector3f tVec = transforms[i].tCalc.getTransformations();
                tVec.mul(1/16F);
                tVec.mul(1, -1, -1);
                translations.push(tVec);
            }
            //Transform stack translations/offsets
            for(int i = 0; i < transforms.length - 1; i++) {
                Mat4f locMat = new Mat4f();
                Vector3f rVec = transforms[i].rCalc.getTransformations();
                if(rVec.x() != 0) locMat.multiply(new Quat(rVec.x(), Vector3f.XN, true));
                if(rVec.y() != 0) locMat.multiply(new Quat(rVec.y(), Vector3f.YP, true));
                if(rVec.z() != 0) locMat.multiply(new Quat(rVec.z(), Vector3f.ZP, true));
                Vector3f tVec = offsets.pop();
                for(int j = 0; j < offsets.size(); j++) {
                    Vector3f tVec2 = offsets.get(j);
                    tVec2.sub(tVec);
                    Vector3f temp = new Vector3f(tVec2.x(), tVec2.y(), tVec2.z());
                    locMat.transformVector3f(temp);
                    tVec2.set(temp.x(), temp.y(), temp.z());
                    tVec2.add(tVec);
                }
                for(int j = translations.size() - 2 - i; j > -1; j--) {
                    Vector3f tVec2 = translations.get(j);
                    Vector3f temp = new Vector3f(tVec2.x(), tVec2.y(), tVec2.z());
                    locMat.transformVector3f(temp);
                    tVec2.set(temp.x(), temp.y(), temp.z());
                }
            }
            //Build cumulative rotation matrix
            Mat4f rotMat = new Mat4f();
            for(int i = transforms.length - 1; i > -1; i--) {
                Vector3f rVec = transforms[i].rCalc.getTransformations();
                if(rVec.x() != 0) rotMat.multiply(new Quat(rVec.x(), Vector3f.XN, true));
                if(rVec.y() != 0) rotMat.multiply(new Quat(rVec.y(), Vector3f.YP, true));
                if(rVec.z() != 0) rotMat.multiply(new Quat(rVec.z(), Vector3f.ZP, true));
            }
            //Transform around final point
            Vector3f tVec = offsets.pop();
            Vector3f tVecO = transforms[transforms.length - 1].offset.copy();
            Vector3f temp = point.copy(); //Untransformed difference between center and translation
            temp.sub(tVecO);
            point = tVec.copy();
            point.add(temp);
            Vector3f temp2 = point.copy();
            temp2.sub(temp); //Distance from temp to center
            rotMat.transformVector3f(temp);
            point.set(temp.x(), temp.y(), temp.z());
            point.add(temp2); //Add translation back after rotating
            //Add local translations
            for(Vector3f vec : translations) point.add(vec);
        }
        point.add(translation);
        userMatrix.transformVector3f(point);
        return point;
    }

    public static Vector3f transformPoint(Vector3f point, AnimationData[] transforms, Mat4f userMatrix, Mat4f localMatrix) {
        return transformPoint(point, transforms, userMatrix, localMatrix, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public static Vector3f transformPoint(Vector3f point, AnimationData[] transforms, Mat4f userMatrix, Vector3f translation, Vector3f offset) {
        return transformPoint(point, transforms, userMatrix, new Mat4f(), translation, offset);
    }

    public static Vector3f transformPoint(Vector3f point, AnimationData[] transforms, Mat4f userMatrix, Vector3f offset) {
        return transformPoint(point, transforms, userMatrix, new Mat4f(), new Vector3f(0, 0, 0), offset);
    }

    public static Vector3f transformPoint(Vector3f point, AnimationData[] transforms, Mat4f userMatrix) {
        return transformPoint(point, transforms, userMatrix, new Mat4f(), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public static AABB rotateAabb(AABB box, Rotation rot) {
        return switch(rot) {
            case NONE -> box;
            case CLOCKWISE_90 -> new AABB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX);
            case CLOCKWISE_180 -> new AABB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case COUNTERCLOCKWISE_90 -> new AABB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
        };
    }

    public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
        List<AABB> boxes = shape.toAabbs();
        List<VoxelShape> shapes = new ObjectArrayList<>();
        for(AABB box : boxes) {
            shapes.add(Shapes.create(rotateAabb(box.move(-0.5, -0.5, -0.5), rotation).move(0.5, 0.5, 0.5)));
        }
        //Just crash if an empty shape got here somehow
        VoxelShape finalShape = shapes.get(0);
        for(int i = 1; i < shapes.size(); i++) finalShape = Shapes.joinUnoptimized(finalShape, shapes.get(i), BooleanOp.OR);
        return finalShape.optimize();
    }

    public static int getPassedIntervals(int start, int end, int spacing) {
        int firstInterval = ((start + spacing - 1) / spacing) * spacing;
        if(firstInterval > end) return 0;
        return (end - firstInterval) / spacing + 1;
    }
}
