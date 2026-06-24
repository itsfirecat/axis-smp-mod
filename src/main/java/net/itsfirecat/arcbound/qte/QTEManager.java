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

    public static void handlePulseSuccess(ServerPlayerEntity user) {
        // 1. Force a 30-second cooldown (600 ticks) on all Arc items for other players within 10 blocks
        double radius = 10.0;
        var closePlayers = user.getServerWorld().getPlayers(p -> p != user && p.squaredDistanceTo(user) <= radius * radius);

        for (ServerPlayerEntity target : closePlayers) {
            target.getItemCooldownManager().set(net.itsfirecat.arcbound.item.ModItems.DASH_ARC, 600);
            target.getItemCooldownManager().set(net.itsfirecat.arcbound.item.ModItems.RESONANCE_ARC, 600);
            target.getItemCooldownManager().set(net.itsfirecat.arcbound.item.ModItems.FREEZE_ARC, 600);
            target.getItemCooldownManager().set(net.itsfirecat.arcbound.item.ModItems.PULSE_ARC, 600);
            target.getItemCooldownManager().set(net.itsfirecat.arcbound.item.ModItems.INFINITY_ARC, 600);

            // Darken screen/flashbang nearby players down inside their client networks
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(target,
                    new net.itsfirecat.arcbound.network.ArcVisualPayload(user.getUuid(), 6));
        }
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