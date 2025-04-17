package frostnox.nightfall.block.block.nest;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEventListener;

import javax.annotation.Nullable;

public abstract class GuardedNestBlock extends NestBlock {
    public GuardedNestBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(Level pLevel, T pBlockEntity) {
        return pBlockEntity instanceof GuardedNestBlockEntity nest ? nest.getEventListener() : null;
    }
}
