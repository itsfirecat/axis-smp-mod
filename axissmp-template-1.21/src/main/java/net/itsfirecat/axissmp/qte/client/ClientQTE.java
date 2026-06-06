package net.itsfirecat.axissmp.qte.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.itsfirecat.axissmp.network.QTEHitPacket;
import net.itsfirecat.axissmp.qte.QTEType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientQTE {
    private static QTEType activeType = null;
    private static long startTick = 0;
    private static int durationTicks = 0;
    private static boolean active = false;

    // Feedback parameters
    private static boolean isFailed = false;
    private static long failTime = 0;
    private static final long FAIL_DURATION_MS = 500; // Keep red/shaking for 0.5s

    public static void start(QTEType type, int duration) {
        activeType = type;
        durationTicks = duration;
        active = true;
        isFailed = false;
        if (MinecraftClient.getInstance().world != null) {
            startTick = MinecraftClient.getInstance().world.getTime();
        }
    }

    public static void clear() {
        active = false;
        activeType = null;
        isFailed = false;
    }

    public static float getProgress(RenderTickCounter tickCounter) {
        if (!active || MinecraftClient.getInstance().world == null) return 0.0f;

        long currentTick = MinecraftClient.getInstance().world.getTime();
        float tickDelta = tickCounter.getTickDelta(true);

        // Total life progress (0.0 to 2.0 because it goes forward then backward)
        float totalProgress = (float) ((currentTick - startTick) + tickDelta) / (durationTicks / 2.0f);

        if (totalProgress >= 2.0f) {
            // Completely missed both passes
            triggerFailure();
            return 0.0f;
        }

        // Ping-pong math: If > 1.0, mirror it back down
        if (totalProgress > 1.0f) {
            return 2.0f - totalProgress;
        }
        return totalProgress;
    }

    public static void handleClientRightClick() {
        if (!active || isFailed) return;

        // Fetch precise current progress without a tick counter instance for calculations
        if (MinecraftClient.getInstance().world == null) return;
        long currentTick = MinecraftClient.getInstance().world.getTime();
        float totalProgress = (float) (currentTick - startTick) / (durationTicks / 2.0f);

        float currentProgress = totalProgress > 1.0f ? 2.0f - totalProgress : totalProgress;

        // Expanded green zone matching your request (0.40f to 0.60f)
        if (currentProgress >= 0.40f && currentProgress <= 0.60f) {
            // SUCCESS! Notify the server immediately
            ClientPlayNetworking.send(new QTEHitPacket());
            clear(); // Disappear cleanly
        } else {
            // FAILED! Trigger red shake feedback
            triggerFailure();
        }
    }

    private static void triggerFailure() {
        if (isFailed) return;
        isFailed = true;
        failTime = System.currentTimeMillis();
    }

    public static boolean isActive() {
        if (isFailed && System.currentTimeMillis() - failTime > FAIL_DURATION_MS) {
            active = false;
            isFailed = false;
        }
        return active;
    }

    public static boolean isFailed() { return isFailed; }

    private static final Set<UUID> espTargets = new HashSet<>();
    private static long espEndTime = 0;

    public static void activateESP(Set<UUID> targets, long durationMs) {
        espTargets.clear();
        espTargets.addAll(targets);
        espEndTime = System.currentTimeMillis() + durationMs;
    }

    public static boolean shouldOutlineEntity(UUID uuid) {
        if (System.currentTimeMillis() > espEndTime) {
            espTargets.clear();
            return false;
        }
        return espTargets.contains(uuid);
    }
}