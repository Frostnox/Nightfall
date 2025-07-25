package frostnox.nightfall.entity.ai.sensing;

import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.*;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Simple audio sense that records entities and a single position based solely on distance from the sound source.
 * Sounds are held in memory for specified ticks then discarded.
 */
public class AudioSensing  {
    protected final ActionableEntity entity;
    protected final Int2IntMap heardEntities = new Int2IntOpenHashMap();
    protected @Nullable BlockPos closestHeardPos = null;
    protected double closestHeardDistSqr = Double.MAX_VALUE;
    protected final int memoryDuration;
    private final GameEventListener eventListener = new GameEventListener() {
        @Override
        public @NotNull PositionSource getListenerSource() {
            return new EntityPositionSource(entity.getId());
        }

        @Override
        public int getListenerRadius() {
            return (int) entity.getAttribute(AttributesNF.HEARING_RANGE.get()).getValue();
        }

        @Override
        public boolean handleGameEvent(@NotNull Level level, @NotNull GameEvent event, @Nullable Entity eventEntity, @NotNull BlockPos pos) {
            if(eventEntity == entity || (eventEntity instanceof Player player && player.isCreative())) return false;
            float soundRange = getSoundRange(event, eventEntity);
            double blockDistSqr = pos.distToCenterSqr(entity.position());
            if(blockDistSqr <= soundRange * soundRange) {
                if(eventEntity != null) {
                    int id = eventEntity.getId();
                    if(!eventEntity.isAlive() || (eventEntity instanceof LivingEntity livingEntity && !entity.canTargetFromSound(livingEntity))) return false;
                    else heardEntities.put(id, memoryDuration);
                    return true;
                }
                else if(blockDistSqr < closestHeardDistSqr) {
                    closestHeardPos = pos;
                    closestHeardDistSqr = blockDistSqr;
                    return true;
                }
            }
            return false;
        }
    };
    private final GameEventListenerRegistrar eventListenerRegistrar = new GameEventListenerRegistrar(eventListener);

    public AudioSensing(ActionableEntity entity, int memoryDuration) {
        this.entity = entity;
        this.memoryDuration = memoryDuration;
    }

    public void tick() {
        if(!heardEntities.isEmpty()) {
            IntSet removals = new IntArraySet(4);
            for(var entry : heardEntities.int2IntEntrySet()) {
                int ticks = entry.getIntValue();
                if(ticks == 1) removals.add(entry.getIntKey());
                else entry.setValue(ticks - 1);
            }
            for(int key : removals) heardEntities.remove(key);
        }
        closestHeardPos = null;
        closestHeardDistSqr = Double.MAX_VALUE;
    }

    public List<Entity> getHeardEntities() {
        List<Entity> list = Lists.newArrayList();
        for(int id : heardEntities.keySet()) {
            Entity heardEntity = entity.level.getEntity(id);
            if(heardEntity != null) list.add(heardEntity);
        }
        return list;
    }

    public boolean hasHeard(Entity target) {
        return heardEntities.keySet().contains(target.getId());
    }

    public @Nullable BlockPos getClosestHeardPos() {
        return closestHeardPos;
    }

    public float getSoundRange(GameEvent event, Entity eventEntity) {
        return GameEventsNF.getEventRange(event, eventEntity);
    }

    public GameEventListener getEventListener() {
        return eventListener;
    }

    public GameEventListenerRegistrar getEventListenerRegistrar() {
        return eventListenerRegistrar;
    }
}
