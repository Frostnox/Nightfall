package frostnox.nightfall.item;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ImpactSoundType {
    public static final ImpactSoundType SILENT = new ImpactSoundType(() -> null, () -> null, () -> null, () -> null, () -> null);
    public static final ImpactSoundType SLASH = new ImpactSoundType(SoundsNF.SLASH_FLESH, SoundsNF.SLASH_ARMOR, SoundsNF.SLASH_BONE, SoundsNF.SLASH_STONE, () -> null);
    public static final ImpactSoundType PIERCE = new ImpactSoundType(SoundsNF.PIERCE_FLESH, SoundsNF.PIERCE_ARMOR, SoundsNF.PIERCE_BONE, SoundsNF.PIERCE_STONE, () -> null);
    public static final ImpactSoundType STRIKE = new ImpactSoundType(SoundsNF.STRIKE_FLESH, SoundsNF.STRIKE_ARMOR, SoundsNF.STRIKE_BONE, SoundsNF.STRIKE_STONE, () -> null);

    private final Supplier<SoundEvent> flesh, armor, bone, stone, gaseous;

    public ImpactSoundType(Supplier<SoundEvent> flesh, Supplier<SoundEvent> armor, Supplier<SoundEvent> bone, Supplier<SoundEvent> stone, Supplier<SoundEvent> gaseous) {
        this.flesh = flesh;
        this.armor = armor;
        this.bone = bone;
        this.stone = stone;
        this.gaseous = gaseous;
    }

    public @Nullable Supplier<SoundEvent> getImpactSound(Entity entity) {
        if(entity.getType().is(TagsNF.IMPACT_TYPE_BONE)) return bone;
        else if(entity.getType().is(TagsNF.IMPACT_TYPE_STONE)) return stone;
        else if(entity.getType().is(TagsNF.IMPACT_TYPE_GASEOUS)) return gaseous;
        else return flesh;
    }

    public Supplier<SoundEvent> getFleshSound() {
        return flesh;
    }

    public Supplier<SoundEvent> getArmorSound() {
        return armor;
    }

    public Supplier<SoundEvent> getBoneSound() {
        return bone;
    }

    public Supplier<SoundEvent> getStoneSound() {
        return stone;
    }

    public Supplier<SoundEvent> getGaseousSound() {
        return gaseous;
    }
}
