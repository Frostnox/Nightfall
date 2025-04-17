package frostnox.nightfall.block;

import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;

public abstract class BlockEventListener implements GameEventListener {
    protected final PositionSource source;
    protected final int radius;

    public BlockEventListener(PositionSource source, int radius) {
        this.source = source;
        this.radius = radius;
    }

    @Override
    public PositionSource getListenerSource() {
        return source;
    }

    @Override
    public int getListenerRadius() {
        return radius;
    }
}
