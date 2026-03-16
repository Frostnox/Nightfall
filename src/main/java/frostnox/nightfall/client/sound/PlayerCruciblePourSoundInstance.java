package frostnox.nightfall.client.sound;

import frostnox.nightfall.capability.PlayerData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PlayerCruciblePourSoundInstance extends FadingSoundInstance {
    private final Player entity;

    public PlayerCruciblePourSoundInstance(Player entity, SoundEvent sound, SoundSource source) {
        super(0.1F, 1, sound, source);
        this.entity = entity;
        looping = true;
        volume = 0.5F;
        pitch = 0.9F + entity.getLevel().random.nextFloat() * 0.2F;
        x = entity.getX();
        y = entity.getY() + entity.getBbHeight() / 2;
        z = entity.getZ();
    }

    @Override
    public void tick() {
        x = entity.getX();
        y = entity.getY() + entity.getBbHeight() / 2;
        z = entity.getZ();
        super.tick();
    }

    @Override
    protected boolean shouldFade() {
        return fadeCount > 0 || entity.isRemoved() || !entity.isAlive() || !PlayerData.get(entity).isPouringCrucible();
    }
}
