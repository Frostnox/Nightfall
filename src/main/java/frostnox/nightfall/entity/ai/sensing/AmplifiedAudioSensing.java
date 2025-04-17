package frostnox.nightfall.entity.ai.sensing;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;

public class AmplifiedAudioSensing extends AudioSensing {
    public final float amplifier;

    public AmplifiedAudioSensing(ActionableEntity entity, int memoryDuration, float amplifier) {
        super(entity, memoryDuration);
        this.amplifier = amplifier;
    }

    @Override
    public float getSoundRange(GameEvent event, Entity eventEntity) {
        return super.getSoundRange(event, eventEntity) * amplifier;
    }
}
