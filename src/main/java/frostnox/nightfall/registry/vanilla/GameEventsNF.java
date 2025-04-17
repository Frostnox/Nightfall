package frostnox.nightfall.registry.vanilla;

import frostnox.nightfall.Nightfall;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventsNF {
    public static GameEvent ACTION_SOUND;

    public static void register() {
        ACTION_SOUND = register("action_sound");
    }

    private static GameEvent register(String pName) {
        return register(pName, 16);
    }

    private static GameEvent register(String pName, int pNotificationRadius) {
        return Registry.register(Registry.GAME_EVENT, Nightfall.MODID + ":" + pName, new GameEvent(pName, pNotificationRadius));
    }

    //All vanilla events have a notification range of 16 blocks so custom hearing ranges are specified here. Events not specified here default to 16.
    public static final Object2FloatMap<GameEvent> EVENT_SOUND_RANGE = Object2FloatMaps.unmodifiable(Util.make(new Object2FloatOpenHashMap<>(), (map) -> {
        map.put(GameEvent.STEP, 10);
        map.put(GameEvent.FLAP, 10);
        map.put(GameEvent.SWIM, 10);
        map.put(GameEvent.ELYTRA_FREE_FALL, 6);
        map.put(GameEvent.HIT_GROUND, 11);
        map.put(GameEvent.MINECART_MOVING, 24);
        map.put(GameEvent.RING_BELL, 32);
        map.put(GameEvent.BLOCK_CHANGE, 10);
        map.put(GameEvent.PROJECTILE_SHOOT, 10);
        map.put(GameEvent.DRINKING_FINISH, 12);
        map.put(GameEvent.PRIME_FUSE, 10);
        map.put(GameEvent.PROJECTILE_LAND, 12);
        map.put(GameEvent.EAT, 10);
        map.put(GameEvent.MOB_INTERACT, 6);
        map.put(GameEvent.ENTITY_DAMAGED, 12);
        map.put(GameEvent.EQUIP, 12);
        map.put(GameEvent.SHEAR, 10);
        map.put(GameEvent.BLOCK_CLOSE, 12);
        map.put(GameEvent.BLOCK_UNSWITCH, 10);
        map.put(GameEvent.BLOCK_UNPRESS, 6);
        map.put(GameEvent.BLOCK_DETACH, 6);
        map.put(GameEvent.DISPENSE_FAIL, 10);
        map.put(GameEvent.BLOCK_OPEN, 12);
        map.put(GameEvent.BLOCK_SWITCH, 10);
        map.put(GameEvent.BLOCK_PRESS, 10);
        map.put(GameEvent.BLOCK_ATTACH, 6);
        map.put(GameEvent.ENTITY_PLACE, 10);
        map.put(GameEvent.BLOCK_PLACE, 10);
        map.put(GameEvent.FLUID_PLACE, 10);
        map.put(GameEvent.ENTITY_KILLED, 0);
        map.put(GameEvent.BLOCK_DESTROY, 12);
        map.put(GameEvent.FLUID_PICKUP, 6);
        map.put(GameEvent.FISHING_ROD_REEL_IN, 10);
        map.put(GameEvent.CONTAINER_CLOSE, 12);
        map.put(GameEvent.FISHING_ROD_CAST, 6);
        map.put(GameEvent.EXPLODE, 24);
        map.put(GameEvent.LIGHTNING_STRIKE, 32);
        map.put(ACTION_SOUND, 12);
    }));

    public static float getEventRange(GameEvent event, Entity eventEntity) {
        if(EVENT_SOUND_RANGE.containsKey(event)) {
            if(event == GameEvent.STEP || event == GameEvent.HIT_GROUND) {
                if(eventEntity instanceof Player player && player.isCrouching()) return 1F;
            }
            return EVENT_SOUND_RANGE.getFloat(event);
        }
        return 16F;
    }
}
