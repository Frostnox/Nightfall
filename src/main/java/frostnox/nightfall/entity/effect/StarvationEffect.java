package frostnox.nightfall.entity.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class StarvationEffect extends MobEffect implements ITickUpEffect {
    public StarvationEffect() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    @Override
    public boolean shouldTickUp(LivingEntity entity, int amplifier, int duration) {
        return duration < 20 * 60 * 60 && entity instanceof Player player && player.getFoodData().getFoodLevel() <= 0;
    }
}
