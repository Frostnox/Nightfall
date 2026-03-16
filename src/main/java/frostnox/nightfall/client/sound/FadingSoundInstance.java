package frostnox.nightfall.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public abstract class FadingSoundInstance extends AbstractTickableSoundInstance {
    public final float fade, maxVolume;
    protected int fadeCount;

    protected FadingSoundInstance(float fade, float maxVolume, SoundEvent sound, SoundSource source) {
        super(sound, source);
        this.fade = fade;
        this.maxVolume = maxVolume;
    }

    protected abstract boolean shouldFade();

    @Override
    public void tick() {
        if(shouldFade()) {
            volume -= fade;
            fadeCount++;
            if(volume <= 0) stop();
        }
        else volume = Math.min(maxVolume, volume + fade);
    }
}
