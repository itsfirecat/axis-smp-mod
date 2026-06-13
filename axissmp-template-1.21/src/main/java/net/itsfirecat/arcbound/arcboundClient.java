package net.itsfirecat.arcbound;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.itsfirecat.arcbound.network.*;
import net.itsfirecat.arcbound.qte.client.ClientQTE;
import net.itsfirecat.arcbound.qte.client.QTEHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.entity.Entity;

public class arcboundClient implements ClientModInitializer {

    public static int IMPACT_FRAME_DURATION_MS = 50;
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

        // 4. Receive Packet: Clientbound ESP target UUID tracker link
        ClientPlayNetworking.registerGlobalReceiver(QTEESPPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                java.util.Set<java.util.UUID> targets = new java.util.HashSet<>(payload.targets());
                ClientQTE.activateESP(targets, 5000);
            });
        });

        // 5. Receive Packet: Pulse AoE flashbang overlay visual hook
        ClientPlayNetworking.registerGlobalReceiver(ArcFlashPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                net.itsfirecat.arcbound.qte.client.ArcVisuals.triggerFlash(payload.durationMs(), payload.color());
            });
        });

        // 6. Receive Packet: JJK Hollow Purple state machine updates
        ClientPlayNetworking.registerGlobalReceiver(ArcVisualPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                net.minecraft.client.MinecraftClient client = context.client();
                if (client.world == null) return;

                if (payload.stateId() == 0) {
                    net.itsfirecat.arcbound.qte.client.ArcVisuals.setAnimationTarget(null, 0);
                    return;
                }

                Entity caster = null;
                for (Entity entity : client.world.getEntities()) {
                    if (entity.getUuid().equals(payload.casterUuid())) {
                        caster = entity;
                        break;
                    }
                }

                if (caster == null) return;

                switch (payload.stateId()) {
                    case 2 -> net.itsfirecat.arcbound.qte.client.ArcVisuals.setAnimationTarget(caster, 1);
                    case 3 -> net.itsfirecat.arcbound.qte.client.ArcVisuals.setAnimationTarget(caster, 2);
                    case 4 -> {
                        net.itsfirecat.arcbound.client.ArcImpactHandler.start(
                                net.itsfirecat.arcbound.client.ArcImpactHandler.HEAVEN_DAP_SEQUENCE,
                                net.itsfirecat.arcbound.qte.client.ArcVisuals.IMPACT_FRAME_DURATION_MS,
                                false);
                    }
                }
            });
        });

        // 7. Client tick — animations + impact frames
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            net.itsfirecat.arcbound.qte.client.ArcVisuals.tickClientAnimations();
            net.itsfirecat.arcbound.client.ArcImpactHandler.tick();
        });

        // 8. Entity renderer
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.itsfirecat.arcbound.arcbound.HOLLOW_PURPLE_ENTITY,
                net.minecraft.client.render.entity.EmptyEntityRenderer::new
        );

        // 9. Shader reload listener
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(
                net.minecraft.resource.ResourceType.CLIENT_RESOURCES
        ).registerReloadListener(
                net.itsfirecat.arcbound.client.ArcImpactRenderType.createReloadListener()
        );

// Force initial load in case the reload listener registered too late
        net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
            net.itsfirecat.arcbound.client.ArcImpactRenderType.reload(
                    net.minecraft.client.MinecraftClient.getInstance().getResourceManager()
            );
        });
    }
}