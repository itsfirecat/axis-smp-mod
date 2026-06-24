package net.itsfirecat.arcbound.qte.client;

import net.itsfirecat.arcbound.sound.ArcSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
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
    // Impact frame tuning
    // -------------------------------------------------------
    public static int IMPACT_FRAME_DURATION_MS = 50;
    public static long CINEMATIC_WAIT_START_TIME = 1200L;
    public static long CINEMATIC_TOTAL_DURATION = 3000L + CINEMATIC_WAIT_START_TIME;
    // -------------------------------------------------------
    // Timing constants (ms)
    // -------------------------------------------------------
    private static final long RED_SPAWN_TIME   = 600;
    private static final long MERGE_START_TIME = 1200;
    private static final long MERGE_DURATION   = 800;
    private static final long TOTAL_PHASE1     = MERGE_START_TIME + MERGE_DURATION; // 2000ms

    private static final long PULSE_DURATION_MS = 350;

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
    private static long    lastLogTime      = 0; // Guard variable to prevent console flooding

    // -------------------------------------------------------
    // Flashbang state (separate system, used by ArcFlashPacket)
    // -------------------------------------------------------
    private static long flashStartTime  = 0;
    private static long flashDurationMs = 0;
    private static int  flashColor      = 0xFFFFFF;

    // -------------------------------------------------------
    // Item Display tracking (Standalone System)
    // -------------------------------------------------------
    private static final List<net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity> activeItemDisplays = new ArrayList<>();

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

    private static final DustColorTransitionParticleEffect PULSE_DUST =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.00f, 0.75f, 0.85f),
                    new Vector3f(0.50f, 0.00f, 0.85f),
                    1.8f
            );

    private static final DustColorTransitionParticleEffect PULSE_PILLAR_PARTICLES =
            new DustColorTransitionParticleEffect(
                    new Vector3f(1.00f, 1.00f, 1.00f),
                    new Vector3f(0.35f, 0.00f, 0.85f),
                    2.4f
            );

    // -------------------------------------------------------
    // Flashbang (AoE)
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

        // Wipe away vanilla item displays safely if tracking state is interrupted or drops out
        activeItemDisplays.forEach(Entity::discard);
        activeItemDisplays.clear();

        animState = state;

        blueSoundPlayed  = false;
        redSoundPlayed   = false;
        mergeSoundPlayed = false;
        lastLogTime      = 0;

        if (state == 0) {
            currentCaster = null;
            return;
        }

        currentCaster  = entity;
        stateStartTime = System.currentTimeMillis();

        if (entity != null) {
            cachedLook   = entity.getRotationVec(1.0f).normalize();
            cachedOrigin = entity.getPos();
        }
    }

    // -------------------------------------------------------
    // Tick
    // -------------------------------------------------------
    public static void tickClientAnimations() {
        if (animState == 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        if (animState != 3 && animState != 4 && (currentCaster == null || !currentCaster.isAlive())) {
            setAnimationTarget(null, 0);
            return;
        }

        long elapsed = System.currentTimeMillis() - stateStartTime;
        isSpawningTracked = true;

        if (animState == 1) {
            Vec3d HP_Center = cachedOrigin.add(cachedLook.x * 1.2, (currentCaster.getStandingEyeHeight() - 0.6) + cachedLook.y * 1.2, cachedLook.z * 1.2);
            spawnPhase1(client, HP_Center, elapsed);
        } else if (animState == 2) {
            Vec3d HP_Center = cachedOrigin.add(cachedLook.x * 1.2, (currentCaster.getStandingEyeHeight() - 0.6) + cachedLook.y * 1.2, cachedLook.z * 1.2);
            spawnPhase2(client, HP_Center);
        } else if (animState == 3) {
            spawnPulseArcShockwave(client, cachedOrigin.add(0, 0.2, 0), elapsed);
        } else if (animState == 4) {
            renderPillarCinematic(client, elapsed);
            tickStackedItemDisplays(client, elapsed); // Runs in parallel alongside the pillar method
        }

        isSpawningTracked = false;
    }

    private static void spawnPhase1(MinecraftClient client, Vec3d center, long elapsed) {

        // --- Sound cues ---
        if (!blueSoundPlayed) {
            playSound(client, center, ArcSoundEvents.BLUE_SPAWN, SOUND_VOLUME_BLUE_SPAWN, SOUND_PITCH_BLUE_SPAWN);
            blueSoundPlayed = true;
        }
        if (elapsed >= RED_SPAWN_TIME && !redSoundPlayed) {
            playSound(client, center, ArcSoundEvents.RED_SPAWN, SOUND_VOLUME_RED_SPAWN, SOUND_PITCH_RED_SPAWN);
            redSoundPlayed = true;
        }
        if (elapsed >= MERGE_START_TIME && !mergeSoundPlayed) {
            playSound(client, center, ArcSoundEvents.MERGE, SOUND_VOLUME_MERGE, SOUND_PITCH_MERGE);
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

        spawnSphere(client, blueCenter, BLUE_DUST, 20);

        if (elapsed >= RED_SPAWN_TIME) {
            spawnSphere(client, redCenter, RED_DUST, 20);
        }
    }

    private static void spawnPhase2(MinecraftClient client, Vec3d center) {
        spawnSphere(client, center, PURPLE_CORE, 28);
    }

    private static void spawnPulseArcShockwave(MinecraftClient client, Vec3d origin, long elapsed) {
        if (elapsed >= PULSE_DURATION_MS) {
            setAnimationTarget(null, 0);
            return;
        }

        double progress = (double) elapsed / PULSE_DURATION_MS;
        double maxRadius = 8.5;
        double currentRadius = maxRadius * progress;

        int segments = 45;
        for (int i = 0; i < segments; i++) {
            double angle = (i * 2 * Math.PI) / segments;

            double jitterRadius = currentRadius + (Math.random() - 0.5) * 0.2;

            double px = origin.x + (jitterRadius * Math.cos(angle));
            double py = origin.y + (Math.random() - 0.5) * 0.15;
            double pz = origin.z + (jitterRadius * Math.sin(angle));

            client.particleManager.addParticle(PULSE_DUST, px, py, pz, 0.0, 0.0, 0.0);

            if (i % 5 == 0 && Math.random() > 0.4) {
                client.particleManager.addParticle(ParticleTypes.GUST, px, py + 0.2, pz, 0.0, 0.05, 0.0);
            }
        }
    }

    private static void renderPillarCinematic(MinecraftClient client, long elapsed) {
        if (elapsed >= CINEMATIC_TOTAL_DURATION) {
            Vec3d breakPos = cachedOrigin.add(0, 1.5, 0);

            playSound(client, breakPos, SoundEvents.BLOCK_GLASS_BREAK, 2.0f, 0.75f);
            playSound(client, breakPos, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.4f, 0.95f);

            // 1. Sets internal state variables inside ArcVisuals
            triggerFlash(800, 0xFFFFFF);

            // 2. FORCES THE ACTUAL FLASHBANG SCREEN OVERLAY TO RENDER TO THE SCREEN
            // This hooks into your custom full-screen fading flash overlay
            net.itsfirecat.arcbound.client.ArcImpactHandler.startFadingFlash(
                    net.itsfirecat.arcbound.client.ArcImpactHandler.FrameType.WHITE,
                    800L // Matches your 800ms flash duration
            );

            for (int i = 0; i < 50; i++) {
                client.particleManager.addParticle(ParticleTypes.EXPLOSION,
                        breakPos.x + (Math.random() - 0.5) * 2.2,
                        breakPos.y + (Math.random() - 0.5) * 2.2,
                        breakPos.z + (Math.random() - 0.5) * 2.2,
                        0.0, 0.0, 0.0
                );
            }
            setAnimationTarget(null, 0);
            return;
        }

        if (currentCaster != null) {
            if (elapsed < CINEMATIC_WAIT_START_TIME) {
                currentCaster.setVelocity(0.0, 0.22, 0.0);
                cachedOrigin = currentCaster.getPos();
            } else {
                currentCaster.setVelocity(0.0, 0.0, 0.0);
                currentCaster.setPosition(cachedOrigin.x, cachedOrigin.y, cachedOrigin.z);
            }
        }

        double timeInSeconds = elapsed / 1000.0;
        double orbitAngle = timeInSeconds * 2.6;
        double radius = 1.35;
        Vec3d clusterPivot = cachedOrigin.add(0, 0.1, 0);

        for (int arcId = 0; arcId < 5; arcId++) {
            double componentAngle = (arcId * 2 * Math.PI) / 5;
            double finalAngle = componentAngle + orbitAngle;

            double ox = clusterPivot.x + radius * Math.cos(finalAngle);
            double oy = clusterPivot.y + Math.sin(timeInSeconds * 4.5 + arcId) * 0.12;
            double oz = clusterPivot.z + radius * Math.sin(finalAngle);

            for (int pCount = 0; pCount < 3; pCount++) {
                client.particleManager.addParticle(PULSE_PILLAR_PARTICLES,
                        ox + (Math.random() - 0.5) * 0.08,
                        oy + (Math.random() - 0.5) * 0.08,
                        oz + (Math.random() - 0.5) * 0.08,
                        0.0, 0.0, 0.0
                );
            }
        }
    }

    // -------------------------------------------------------
    // Standalone Item Display Mechanism
    // -------------------------------------------------------
    private static void tickStackedItemDisplays(MinecraftClient client, long elapsed) {
        if (currentCaster == null || elapsed >= CINEMATIC_TOTAL_DURATION) {
            activeItemDisplays.forEach(Entity::discard);
            activeItemDisplays.clear();
            return;
        }

        net.minecraft.item.Item[] arcItems = {
                net.itsfirecat.arcbound.item.ModItems.PULSE_ARC,
                net.itsfirecat.arcbound.item.ModItems.INFINITY_ARC,
                net.itsfirecat.arcbound.item.ModItems.FREEZE_ARC,
                net.itsfirecat.arcbound.item.ModItems.DASH_ARC,
                net.itsfirecat.arcbound.item.ModItems.RESONANCE_ARC
        };

        double throwProgress = 0.0;
        if (elapsed > CINEMATIC_WAIT_START_TIME) {
            long throwElapsed = elapsed - CINEMATIC_WAIT_START_TIME;
            long throwDuration = CINEMATIC_TOTAL_DURATION - CINEMATIC_WAIT_START_TIME;
            throwProgress = (double) throwElapsed / throwDuration;
        }

        // The arch expands upward and outward slightly over time during the throw phase
        double archRadius = 1.5 + (throwProgress * 1.5);
        // Center point of the semicircle arch (around the player's chest/eye height)
        Vec3d archCenterPos = currentCaster.getPos().add(0, currentCaster.getStandingEyeHeight() - 0.5, 0);

        if (activeItemDisplays.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                var display = new net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity(
                        net.minecraft.entity.EntityType.ITEM_DISPLAY, client.world);

                display.setItemStack(new net.minecraft.item.ItemStack(arcItems[i]));
                display.setBillboardMode(net.minecraft.entity.decoration.DisplayEntity.BillboardMode.CENTER);

                client.world.addEntity(display);
                activeItemDisplays.add(display);
            }
        }

        // Position the 5 items along the semicircle arch over the player's head
        for (int i = 0; i < activeItemDisplays.size(); i++) {
            var display = activeItemDisplays.get(i);

            // Divide 180 degrees (Math.PI) into 4 equal segments for 5 items
            // i = 0 (0 deg, left), i = 2 (90 deg, top), i = 4 (180 deg, right)
            double angle = (i * Math.PI) / 4.0;

            // Calculate coordinates relative to the player's facing direction
            // Using cachedLook to orient the semicircle flat relative to where the player looked when starting
            Vec3d rightVec = new Vec3d(-cachedLook.z, 0, cachedLook.x).normalize();

            // Offset along the left/right vector and up vector
            double offsetX = rightVec.x * (archRadius * Math.cos(angle));
            double offsetY = archRadius * Math.sin(angle);
            double offsetZ = rightVec.z * (archRadius * Math.cos(angle));

            display.setPosition(
                    archCenterPos.x + offsetX,
                    archCenterPos.y + offsetY,
                    archCenterPos.z + offsetZ
            );

            // Keep them gently facing the viewer or matching client view rotation via the billboard mode
            float spinAngle = (float) ((elapsed / 1000.0) * 1.5);
            display.setYaw(spinAngle * (180f / (float)Math.PI));
        }
    }
    private static void spawnSphere(MinecraftClient client, Vec3d center,
                                    DustColorTransitionParticleEffect effect, int count) {
        for (int i = 0; i < count; i++) {
            double theta  = Math.random() * 2 * Math.PI;
            double phi    = Math.acos(2 * Math.random() - 1);
            double radius = 1.4 + Math.random() * 0.3;

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
                                  net.minecraft.registry.entry.RegistryEntry<SoundEvent> event,
                                  float volume, float pitch) {
        client.getSoundManager().play(
                new PositionedSoundInstance(
                        event.value(),
                        SoundCategory.PLAYERS,
                        volume, pitch,
                        client.world.getRandom(),
                        pos.x, pos.y, pos.z
                )
        );
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