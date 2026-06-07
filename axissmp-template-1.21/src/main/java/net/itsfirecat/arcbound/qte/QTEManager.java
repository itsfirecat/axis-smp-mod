package net.itsfirecat.arcbound.qte;

import net.itsfirecat.arcbound.network.QTEClearPacket;
import net.itsfirecat.arcbound.network.QTEStartPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QTEManager {
    private static final Map<UUID, ActiveQTE> activeQtes = new ConcurrentHashMap<>();

    public static void startQTE(ServerPlayerEntity player, QTEType type, int durationTicks, long currentTick) {
        ActiveQTE qte = new ActiveQTE(type, currentTick, durationTicks);
        activeQtes.put(player.getUuid(), qte);
        ServerPlayNetworking.send(player, new QTEStartPacket(type, durationTicks));
    }

    public static ActiveQTE getActiveQTE(ServerPlayerEntity player) {
        return activeQtes.get(player.getUuid());
    }

    public static void removeQTE(ServerPlayerEntity player) {
        activeQtes.remove(player.getUuid());
        ServerPlayNetworking.send(player, new QTEClearPacket());
    }

    public static void cleanupExpired(long currentTick, net.minecraft.server.MinecraftServer server) {
        activeQtes.forEach((uuid, qte) -> {
            if (qte.isExpired(currentTick)) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    ServerPlayNetworking.send(player, new QTEClearPacket());
                }
            }
        });
        activeQtes.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTick));
    }
}