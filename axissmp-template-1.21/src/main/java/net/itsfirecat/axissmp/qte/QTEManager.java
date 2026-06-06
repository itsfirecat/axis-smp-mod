package net.itsfirecat.axissmp.qte;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QTEManager {

    private static final Map<UUID, ActiveQTE> QTES = new HashMap<>();

    public static void start(UUID uuid, QTEType type, long startTick, int durationTicks) {
        QTES.put(uuid, new ActiveQTE(type, startTick, durationTicks));
    }

    public static ActiveQTE get(UUID uuid) {
        return QTES.get(uuid);
    }

    public static boolean has(UUID uuid) {
        return QTES.containsKey(uuid);
    }

    public static void clear(UUID uuid) {
        QTES.remove(uuid);
    }

    public static boolean tryConsume(UUID uuid, long currentTick, QTEType type, float min, float max) {
        ActiveQTE qte = QTES.get(uuid);

        if (qte == null) return false;
        if (qte.type != type) return false;
        if (qte.consumed) return false;

        float p = qte.progress(currentTick);

        if (p >= min && p <= max) {
            qte.consumed = true;
            QTES.remove(uuid);
            return true;
        }

        return false;
    }

    public static void tickCleanup(long currentTick) {
        QTES.entrySet().removeIf(e ->
                e.getValue().progress(currentTick) > 1f
        );
    }
}