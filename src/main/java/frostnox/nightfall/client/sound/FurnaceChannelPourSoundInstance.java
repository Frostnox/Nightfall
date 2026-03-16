package frostnox.nightfall.client.sound;

import frostnox.nightfall.block.block.furnacechannel.FurnaceChannelBlockEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class FurnaceChannelPourSoundInstance extends FadingSoundInstance {
    private final FurnaceChannelBlockEntity entity;

    public FurnaceChannelPourSoundInstance(FurnaceChannelBlockEntity entity, SoundEvent sound, SoundSource source) {
        super(0.1F, 1, sound, source);
        this.entity = entity;
        looping = true;
        volume = 0.5F;
        pitch = 0.9F + entity.getLevel().random.nextFloat() * 0.2F;
        x = entity.getBlockPos().getX() + 0.5;
        y = entity.getBlockPos().getY() + 0.5;
        z = entity.getBlockPos().getZ() + 0.5;
    }

    @Override
    protected boolean shouldFade() {
        return fadeCount > 0 || entity.isRemoved() || !entity.wasCasting();
    }
}
