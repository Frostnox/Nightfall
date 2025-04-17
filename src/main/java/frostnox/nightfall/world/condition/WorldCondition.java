package frostnox.nightfall.world.condition;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class WorldCondition extends ForgeRegistryEntry<WorldCondition> {
    public abstract boolean test(Player player);
}
