package frostnox.nightfall.action;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Vector3f;
import frostnox.nightfall.util.math.BoundingSphere;
import frostnox.nightfall.util.math.Mat4f;
import frostnox.nightfall.util.math.Quat;

public class HurtSphere {
    public static final HurtSphere NONE = new HurtSphere(0, 0, 0, 0, 0);
    //Weapons (oriented as model appears in first person with no transformations applied)
    public static final HurtSphere HAMMER = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 3D/16D),
            new BoundingSphere(0, 2D/16D + 3D/16D, 0, 3D/16D),
            new BoundingSphere(0, 2D/16D + 3D/16D * 2, 0, 3D/16D),
            new BoundingSphere(0, 2D/16D + 8.5D/16D, 0, 4.5D/16D));
    public static final HurtSphere SWORD = new HurtSphere(0.25D, 4, 0, 0.3D, 0, 0, 2D/16D, 0);
    public static final HurtSphere SABRE = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.25D),
            new BoundingSphere(-1D/16D, 2D/16D + 0.3D, 0, 0.25D),
            new BoundingSphere(-1D/16D, 2D/16D + 0.3D * 2, 0, 0.25D),
            new BoundingSphere(0, 2D/16D + 0.3D * 3, 0, 0.25D));
    public static final HurtSphere FLINT_DAGGER = new HurtSphere(0.25D, 3, 0, 0.18D, 0, 0, 2D/16D, 0);
    public static final HurtSphere CHISEL = new HurtSphere(0.24D, 3, 0, 0.24D, 0, 0, 2D/16D, 0);
    public static final HurtSphere DAGGER = new HurtSphere(0.25D, 3, 0, 0.25D, 0, 0, 2D/16D, 0);
    public static final HurtSphere SPEAR = new HurtSphere(0.22D, 5, 0, 0.27D, 0, 0, 2D/16D, 0);
    public static final HurtSphere CLUB = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.2D),
            new BoundingSphere(0, 2D/16D + 4D/16D, 0, 0.25D),
            new BoundingSphere(0, 2D/16D + 8.5D/16D, 0, 0.33D));
    public static final HurtSphere MACE = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 0.22, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 0.44, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 11D/16D, 0, 0.27D));
    public static final HurtSphere AXE = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 4D/16D, 0, 0.22D),
            new BoundingSphere(-1D/16D, 2D/16D + 9D/16D, 0, 0.27D));
    public static final HurtSphere SHOVEL = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 3.5D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 3.5D/16D * 2, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 11.5D/16D, 0, 0.27D));
    public static final HurtSphere PICKAXE = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 0.22, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 9D/16D, 0, 0.25D),
            new BoundingSphere(3.5D/16D, 2D/16D + 8D/16D, 0, 0.21D),
            new BoundingSphere(-3.5D/16D, 2D/16D + 8D/16D, 0, 0.21D));
    public static final HurtSphere SICKLE = new HurtSphere(new BoundingSphere(-0.25D/16D, 2D/16D, 0, 0.25D),
            new BoundingSphere(-1.25D/16D, 2D/16D + 0.3D, 0, 0.25D),
            new BoundingSphere(-1.75D/16D, 2D/16D + 0.3D * 2, 0, 0.225D),
            new BoundingSphere(-2.75D/16D, 1D/16D + 0.3D * 3, 0, 0.2D));
    public static final HurtSphere ADZE = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 0.22, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 9D/16D, 0, 0.24D),
            new BoundingSphere(-2D/16D, 2D/16D + 7.5D/16D, 0, 0.2D));
    public static final HurtSphere MAUL = new HurtSphere(new BoundingSphere(0, 2D/16D, 0, 0.22D),
            new BoundingSphere(0, 2D/16D + 4D/16D, 0, 0.22D),
            new BoundingSphere(-2D/16D, 2D/16D + 9D/16D, 0, 0.285D));
    //Items
    public static final HurtSphere SHIELD = new HurtSphere(0.3D, 1, 0, 0, 0);
    //Enemies
    public static final HurtSphere HUSK_ARM = new HurtSphere(0.3D, 1, 0, 0, 0);
    public static final HurtSphere SKELETON_ARM = new HurtSphere(0.2D, 1, 0, 0, 0);
    public static final HurtSphere COCKATRICE_BITE = new HurtSphere(0.25D, 1, 0, 0, 0);
    public static final HurtSphere COCKATRICE_CLAW = new HurtSphere(0.35D, 1, 0, 0, 0);
    public static final HurtSphere SPIDER_BITE = new HurtSphere(0.25D, 1, 0, 0, 0);
    public static final HurtSphere ROCKWORM_BITE = new HurtSphere(0.4D, 1, 0, 0, 0);
    public static final HurtSphere PIT_DEVIL_BITE = new HurtSphere(0.3D, 1, 0, 0, 0);
    public static final HurtSphere ECTOPLASM_CLUB = new HurtSphere(5D/16D, 1, 0, 0, 0);

    public static final ImmutableMap<HurtSphere, HurtSphere> WEAPONS_TP;
    protected static void addTP(ImmutableMap.Builder<HurtSphere, HurtSphere> builder, HurtSphere hurtSphere) {
        builder.put(hurtSphere, transformTP(hurtSphere));
    }
    private static HurtSphere transformTP(HurtSphere hurtSphere) {
        BoundingSphere[] spheres = hurtSphere.getSpheres();
        for(BoundingSphere sphere : spheres) sphere.translate(0, -2D/16D, 0);
        Mat4f matrix = new Mat4f(new Quat(-90, Vector3f.YP, true));
        matrix.multiply(new Quat(-90, Vector3f.XP, true));
        for(BoundingSphere sphere : spheres) sphere.transform(matrix);
        return new HurtSphere(spheres);
    }
    static {
        ImmutableMap.Builder<HurtSphere, HurtSphere> builder = new ImmutableMap.Builder<>();
        addTP(builder, ADZE);
        addTP(builder, AXE);
        addTP(builder, CLUB);
        addTP(builder, CHISEL);
        addTP(builder, DAGGER);
        addTP(builder, FLINT_DAGGER);
        addTP(builder, HAMMER);
        addTP(builder, MACE);
        addTP(builder, PICKAXE);
        addTP(builder, SABRE);
        addTP(builder, SICKLE);
        addTP(builder, SHOVEL);
        addTP(builder, SPEAR);
        addTP(builder, SWORD);
        addTP(builder, MAUL);
        WEAPONS_TP = builder.build();
    }

    private final BoundingSphere[] spheres;

    public HurtSphere(BoundingSphere... spheres) {
        this.spheres = spheres;
    }

    public HurtSphere(double radius, int amount, double xSpacing, double ySpacing, double zSpacing) {
        this(radius, amount, xSpacing, ySpacing, zSpacing, 0, 0, 0);
    }

    public HurtSphere(double radius, int amount, double xSpacing, double ySpacing, double zSpacing, double xShift, double yShift, double zShift) {
        spheres = new BoundingSphere[amount];
        for(int i = 0; i < amount; i++) {
            spheres[i] = new BoundingSphere(xSpacing * i, ySpacing * i, zSpacing * i, radius);
            spheres[i].translate(xShift, yShift, zShift);
        }
    }

    public HurtSphere(double radius, int amount, double xSpacing, double ySpacing, double zSpacing, BoundingSphere... extraSpheres) {
        this(radius, amount, xSpacing, ySpacing, zSpacing, 0, 0, 0, extraSpheres);
    }

    public HurtSphere(double radius, int amount, double xSpacing, double ySpacing, double zSpacing, double xShift, double yShift, double zShift, BoundingSphere... extraSpheres) {
        spheres = new BoundingSphere[amount + extraSpheres.length];
        for(int i = 0; i < amount; i++) {
            spheres[i] = new BoundingSphere(xSpacing * i, ySpacing * i, zSpacing * i, radius);
            spheres[i].translate(xShift, yShift, zShift);
        }
        for(int i = amount; i < amount + extraSpheres.length; i++) {
            spheres[i] = extraSpheres[i - amount];
        }
    }

    /**
     * @return copy of spheres as an array
     */
    public BoundingSphere[] getSpheres() {
        BoundingSphere[] spheres = new BoundingSphere[this.spheres.length];
        for(int i = 0; i < this.spheres.length; i++) {
            spheres[i] = new BoundingSphere(this.spheres[i]);
        }
        return spheres;
    }
}
