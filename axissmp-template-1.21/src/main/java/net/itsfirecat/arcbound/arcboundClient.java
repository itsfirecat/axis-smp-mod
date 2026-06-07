package net.itsfirecat.arcbound;

import net.itsfirecat.arcbound.network.QTEClearPacket;
import net.itsfirecat.arcbound.network.QTEESPPacket;
import net.itsfirecat.arcbound.network.QTEStartPacket;
import net.itsfirecat.arcbound.qte.client.ClientQTE;
import net.itsfirecat.arcbound.qte.client.QTEHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class arcboundClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 1. Register the HUD Render Overlay
        HudRenderCallback.EVENT.register(new QTEHud());

        // 2. Receive Packet: Server tells the client to start a QTE
        ClientPlayNetworking.registerGlobalReceiver(QTEStartPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientQTE.start(payload.type(), payload.durationTicks());
            });
        });

        // 3. Receive Packet: Server tells the client to forcefully hide/clear the active QTE
        ClientPlayNetworking.registerGlobalReceiver(QTEClearPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientQTE.clear();
            });
        });

        // 4. Receive Packet: Now successfully links because it is registered on playS2C channel!
        ClientPlayNetworking.registerGlobalReceiver(QTEESPPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Activate true client ESP for 5 seconds (5000ms)
                java.util.Set<java.util.UUID> targets = new java.util.HashSet<>(payload.targets());
                ClientQTE.activateESP(targets, 5000);
            });
        });
    }
}