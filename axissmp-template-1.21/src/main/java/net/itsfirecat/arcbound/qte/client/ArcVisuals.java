package net.itsfirecat.arcbound.qte.client;

import net.itsfirecat.arcbound.sound.ArcSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ArcVisuals {

    // -------------------------------------------------------
    // Sound tuning
    // -------------------------------------------------------
    public static float SOUND_VOLUME_BLUE_SPAWN = 1.0f;
    public static float SOUND_PITCH_BLUE_SPAWN  = 1.0f;
    public static float SOUND_VOLUME_RED_SPAWN  = 1.0f;
    public static float SOUND_PITCH_RED_SPAWN   = 1.0f;
    public static float SOUND_VOLUME_MERGE      = 1.0f;
    public static float SOUND_PITCH_MERGE       = 1.0f;
    public static float SOUND_VOLUME_LAUNCH     = 1.0f;
    public static float SOUND_PITCH_LAUNCH      = 1.0f;

    // -------------------------------------------------------
    // Timing constants (ms)
    // -------------------------------------------------------
    private static final long RED_SPAWN_TIME   = 600;
    private static final long MERGE_START_TIME = 1200;
    private static final long MERGE_DURATION   = 800;
    private static final long TOTAL_PHASE1     = MERGE_START_TIME + MERGE_DURATION; // 2000ms

    // -------------------------------------------------------
    // Animation state
    // -------------------------------------------------------
    private static Entity currentCaster    = null;
    private static int    animState        = 0;
    private static long   stateStartTime   = 0;
    private static Vec3d  cachedLook       = Vec3d.ZERO;
    private static Vec3d  cachedOrigin     = Vec3d.ZERO;

    private static boolean blueSoundPlayed  = false;
    private static boolean redSoundPlayed   = false;
    private static boolean mergeSoundPlayed = false;

    // -------------------------------------------------------
    // Flash / shader state
    // -------------------------------------------------------
    private static long flashStartTime      = 0;
    private static long flashDurationMs     = 0;
    private static int  flashColor          = 0xFFFFFF;
    private static long inverseShaderEndTime = 0;

    // -------------------------------------------------------
    // Particle tracking
    // -------------------------------------------------------
    private static final List<Particle> trackedParticles = new ArrayList<>();
    public static volatile boolean isSpawningTracked = false;

    // -------------------------------------------------------
    // Particle definitions
    // Bigger scale values = bigger individual dust particles
    // -------------------------------------------------------
    private static final DustColorTransitionParticleEffect BLUE_DUST =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.05f, 0.38f, 0.50f),
                    new Vector3f(0.10f, 0.55f, 0.65f),
                    2.8f
            );
    private static final DustColorTransitionParticleEffect RED_DUST =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.42f, 0.01f, 0.01f),
                    new Vector3f(0.60f, 0.08f, 0.02f),
                    2.8f
            );
    private static final DustColorTransitionParticleEffect PURPLE_CORE =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.28f, 0.00f, 0.55f),
                    new Vector3f(0.50f, 0.00f, 0.70f),
                    3.5f
            );

    // -------------------------------------------------------
    // Flash
    // -------------------------------------------------------
    public static void triggerFlash(long durationMs, int color) {
        flashStartTime  = System.currentTimeMillis();
        flashDurationMs = durationMs;
        flashColor      = color;
    }

    public static float getFlashAlpha() {
        if (flashDurationMs <= 0) return 0.0f;
        long elapsed = System.currentTimeMillis() - flashStartTime;
        if (elapsed >= flashDurationMs) return 0.0f;
        return 1.0f - (float) elapsed / (float) flashDurationMs;
    }

    public static int getFlashColor() { return flashColor; }

    // -------------------------------------------------------
    // Inverse shader
    // -------------------------------------------------------
    public static void triggerInverseShader(long durationMs) {
        inverseShaderEndTime = System.currentTimeMillis() + durationMs;
    }

    public static boolean isInverseShaderActive() {
        return System.currentTimeMillis() < inverseShaderEndTime;
    }

    // -------------------------------------------------------
    // Particle tracking (called by Mixin)
    // -------------------------------------------------------
    public static void trackParticle(Particle particle) {
        if (isSpawningTracked) {
            trackedParticles.add(particle);
        }
    }

    private static void killAllTrackedParticles() {
        for (Particle p : trackedParticles) {
            p.markDead();
        }
        trackedParticles.clear();
    }

    // -------------------------------------------------------
    // Animation state control
    // -------------------------------------------------------
    public static void setAnimationTarget(Entity entity, int state) {
        killAllTrackedParticles();
        animState = state;

        blueSoundPlayed  = false;
        redSoundPlayed   = false;
        mergeSoundPlayed = false;

        if (state == 0) {
            currentCaster = null;
            return;
        }

        currentCaster  = entity;
        stateStartTime = System.currentTimeMillis();

        if (entity != null) {
            cachedLook   = entity.getRotationVec(1.0f).normalize();
            cachedOrigin = entity.getPos().add(0, entity.getStandingEyeHeight() - 0.4, 0);
        }
    }

    // -------------------------------------------------------
    // Tick
    // -------------------------------------------------------
    public static void tickClientAnimations() {
        if (animState == 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        if (currentCaster == null || !currentCaster.isAlive()) {
            setAnimationTarget(null, 0);
            return;
        }

        Vec3d center = cachedOrigin.add(
                cachedLook.x * 1.2,
                cachedLook.y * 1.2,
                cachedLook.z * 1.2
        );
        long elapsed = System.currentTimeMillis() - stateStartTime;

        isSpawningTracked = true;

        if (animState == 1) {
            spawnPhase1(client, center, elapsed);
        } else if (animState == 2) {
            spawnPhase2(client, center);
        }

        isSpawningTracked = false;
    }

    private static void spawnPhase1(MinecraftClient client, Vec3d center, long elapsed) {

        // --- Sound cues ---
        if (!blueSoundPlayed) {
            playSound(client, center, ArcSoundEvents.BLUE_SPAWN,
                    SOUND_VOLUME_BLUE_SPAWN, SOUND_PITCH_BLUE_SPAWN);
            blueSoundPlayed = true;
        }
        if (elapsed >= RED_SPAWN_TIME && !redSoundPlayed) {
            playSound(client, center, ArcSoundEvents.RED_SPAWN,
                    SOUND_VOLUME_RED_SPAWN, SOUND_PITCH_RED_SPAWN);
            redSoundPlayed = true;
        }
        if (elapsed >= MERGE_START_TIME && !mergeSoundPlayed) {
            playSound(client, center, ArcSoundEvents.MERGE,
                    SOUND_VOLUME_MERGE, SOUND_PITCH_MERGE);
            mergeSoundPlayed = true;
        }

        // --- Right vector for side-by-side placement ---
        Vec3d rightVec;
        if (Math.abs(cachedLook.y) > 0.95) {
            rightVec = new Vec3d(1.0, 0.0, 0.0);
        } else {
            rightVec = new Vec3d(-cachedLook.z, 0, cachedLook.x).normalize();
        }

        // --- Separation logic ---
        // Before merge starts: fixed distance apart
        // After merge starts: lerp distance from 1.8 → 0
        double distanceApart;
        if (elapsed < MERGE_START_TIME) {
            distanceApart = 2.8;
        } else {
            double mergeProgress = Math.min(1.0,
                    (double)(elapsed - MERGE_START_TIME) / MERGE_DURATION);
            distanceApart = 2.8 * (1.0 - mergeProgress);
        }

        Vec3d blueCenter = center.add(rightVec.multiply(-distanceApart));
        Vec3d redCenter  = center.add(rightVec.multiply(distanceApart));

        // Blue always present
        spawnSphere(client, blueCenter, BLUE_DUST, 20);

        // Red only after RED_SPAWN_TIME
        if (elapsed >= RED_SPAWN_TIME) {
            spawnSphere(client, redCenter, RED_DUST, 20);
        }
    }

    private static void spawnPhase2(MinecraftClient client, Vec3d center) {
        spawnSphere(client, center, PURPLE_CORE, 28);
    }

    private static void spawnSphere(MinecraftClient client, Vec3d center,
                                    DustColorTransitionParticleEffect effect, int count) {
        for (int i = 0; i < count; i++) {
            // True spherical distribution
            double theta  = Math.random() * 2 * Math.PI;
            double phi    = Math.acos(2 * Math.random() - 1);
            double radius = 1.4 + Math.random() * 0.3; // was 1.0–1.25, now 1.4–1.7

            double ox = radius * Math.sin(phi) * Math.cos(theta);
            double oy = radius * Math.sin(phi) * Math.sin(theta);
            double oz = radius * Math.cos(phi);

            client.particleManager.addParticle(effect,
                    center.x + ox,
                    center.y + oy,
                    center.z + oz,
                    0.0, 0.0, 0.0);
        }
    }

    private static void playSound(MinecraftClient client, Vec3d pos,
                                  net.minecraft.sound.SoundEvent event,
                                  float volume, float pitch) {
        client.getSoundManager().play(
                new PositionedSoundInstance(
                        event,
                        SoundCategory.PLAYERS,
                        volume, pitch,
                        client.world.getRandom(),
                        pos.x, pos.y, pos.z
                )
        );
    }
}