package net.itsfirecat.arcbound.util;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class InfinityState {

    private static final Map<UUID, Integer> active = new HashMap<>();

    public static void activate(PlayerEntity player, int ticks) {
        active.put(player.getUuid(), ticks);
    }

    public static boolean isActive(PlayerEntity player) {
        return active.containsKey(player.getUuid());
    }

    // call this every server tick
    public static void tick() {
        Iterator<Map.Entry<UUID, Integer>> it = active.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();

            int time = entry.getValue() - 1;

            if (time <= 0) {
                it.remove();
            } else {
                entry.setValue(time);
            }
        }
    }
}