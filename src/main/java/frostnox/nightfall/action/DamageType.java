package frostnox.nightfall.action;

import frostnox.nightfall.item.ImpactSoundType;

/**
 * Defined damage types to condense vanilla damage sources
 */
public enum DamageType {
    STRIKING("striking", ImpactSoundType.STRIKE),
    SLASHING("slashing", ImpactSoundType.SLASH),
    PIERCING("piercing", ImpactSoundType.PIERCE),
    FIRE("fire", ImpactSoundType.SILENT),
    FROST("frost", ImpactSoundType.SILENT),
    ELECTRIC("electric", ImpactSoundType.SILENT),
    WITHER("wither", ImpactSoundType.SILENT), //Automatically bypasses defenses & buffs
    ABSOLUTE("absolute", ImpactSoundType.SILENT); //Automatically bypasses defenses & buffs

    public static final DamageType[] STANDARD_TYPES = new DamageType[] {
            STRIKING, SLASHING, PIERCING, FIRE, FROST, ELECTRIC
    };
    private final String name;
    private final ImpactSoundType soundType;
    private final DamageType[] array;

    DamageType(String name, ImpactSoundType soundType) {
        this.name = name;
        this.soundType = soundType;
        this.array = new DamageType[] {this};
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isDefensible() {
        return this != WITHER && this != ABSOLUTE;
    }

    public boolean isPhysical() {
        return this == STRIKING || this == SLASHING || this == PIERCING;
    }

    public boolean isElemental() {
        return this == FIRE || this == FROST || this == ELECTRIC;
    }

    public DamageType[] asArray() {
        return array;
    }

    public ImpactSoundType getImpactSoundType() {
        return soundType;
    }
}
