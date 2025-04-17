package frostnox.nightfall.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.ToIntFunction;

public interface IBlock {
    MaterialColor getBaseColor();

    SoundType getSound();

    float getStrength();

    float getExplosionResistance();

    default ToIntFunction<BlockState> getLightEmission() {
        return (state) -> 0;
    }
}
