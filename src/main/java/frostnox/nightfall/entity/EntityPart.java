package frostnox.nightfall.entity;

public enum EntityPart {
    BODY,
    BODY_2,
    LEG_RIGHT,
    LEG_2_RIGHT,
    LEG_LEFT,
    LEG_2_LEFT,
    NECK,
    HEAD,
    TAIL,
    ARM_RIGHT,
    ARM_LEFT,
    HAND_RIGHT,
    HAND_LEFT,
    EAR_RIGHT,
    EAR_LEFT,
    WING_RIGHT,
    WING_LEFT;

    public static EntityPart getSidedArm(int side) {
        if(side == 1) return ARM_RIGHT;
        else return ARM_LEFT;
    }

    public static EntityPart getSidedHand(int side) {
        if(side == 1) return HAND_RIGHT;
        else return HAND_LEFT;
    }

    public static EntityPart getSidedLeg(int side) {
        if(side == 1) return LEG_RIGHT;
        else return LEG_LEFT;
    }
}
