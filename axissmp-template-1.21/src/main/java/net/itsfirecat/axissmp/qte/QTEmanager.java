package net.itsfirecat.axissmp.qte;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QTEManager {

    private static final Map<UUID, ActiveQTE> ACTIVE_QTES = new HashMap<>();

    public static void startQTE(UUID player, QTEType type, long expireTick) {
        ACTIVE_QTES.put(player, new ActiveQTE(type, expireTick));
    }

    public static boolean hasQTE(UUID player) {
        return ACTIVE_QTES.containsKey(player);
    }

    public static ActiveQTE getQTE(UUID player) {
        return ACTIVE_QTES.get(player);
    }

    public static void completeQTE(UUID player) {
        ACTIVE_QTES.remove(player);
    }

    public static void tick(long currentTick) {
        ACTIVE_QTES.entrySet().removeIf(
                entry -> currentTick > entry.getValue().expireTick()
        );
    }
}