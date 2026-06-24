package net.itsfirecat.arcbound.util;

import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfinityState {

    // Store the exact absolute world time when the invincibility should END
    private static final Map<UUID, Long> activeExpiry = new HashMap<>();

    public static void activate(PlayerEntity player, int ticks) {
        if (player.getWorld() == null) return;

        // Current world time + 60 ticks = the future expiration timestamp
        long expiryTime = player.getWorld().getTime() + ticks;
        activeExpiry.put(player.getUuid(), expiryTime);
    }

    public static boolean isActive(PlayerEntity player) {
        if (player.getWorld() == null) return false;

        Long expiry = activeExpiry.get(player.getUuid());
        if (expiry == null) return false;

        // If the current world time has caught up or passed the expiry timestamp, clear it
        if (player.getWorld().getTime() >= expiry) {
            activeExpiry.remove(player.getUuid());
            return false; // Time's up!
        }

        return true; // Still invincible
    }
}