package net.itsfirecat.arcbound.client.render;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ClientFreezeEffects {

    private static final List<ActiveFreezeTracker> ACTIVE_FREEZES = new ArrayList<>();

    private static class ActiveFreezeTracker {
        final ClientWorld world;
        final Entity entity;
        final int maxDuration;
        int currentTick = 0;

        ActiveFreezeTracker(ClientWorld world, Entity entity, int durationTicks) {
            this.world = world;
            this.entity = entity;
            this.maxDuration = durationTicks;
        }
    }

    /**
     * Finds the targeted entity by ID on the client and begins tracking their frozen particle loop.
     */
    public static void startFreezeEffect(ClientWorld world, int entityId, int durationTicks) {
        Entity target = world.getEntityById(entityId);
        if (target != null) {
            synchronized (ACTIVE_FREEZES) {
                ACTIVE_FREEZES.add(new ActiveFreezeTracker(world, target, durationTicks));
            }
        }
    }

    /**
     * Loops every client tick to continuously wrap the frozen entities in heavy snow/frost layers.
     */
    public static void tickActiveVisuals() {
        List<ActiveFreezeTracker> toRemove = new ArrayList<>();

        synchronized (ACTIVE_FREEZES) {
            for (ActiveFreezeTracker freeze : ACTIVE_FREEZES) {
                Entity entity = freeze.entity;

                // If the entity died, left tracking range, or the freeze ended, wrap it up
                if (entity == null || !entity.isAlive() || freeze.currentTick >= freeze.maxDuration) {
                    toRemove.add(freeze);
                    continue;
                }

                ClientWorld world = freeze.world;
                Vec3d pos = entity.getPos();
                float height = entity.getHeight();
                float width = entity.getWidth();

                // Continuous Frost Aura Layer (Runs every frame they are trapped)
                for (int i = 0; i < 4; i++) {
                    double offsetX = (Math.random() - 0.5) * width;
                    double offsetY = Math.random() * height;
                    double offsetZ = (Math.random() - 0.5) * width;

                    // SNOWFLAKE or INSTANT_EFFECT create a clean icy coat
                    world.addParticle(
                            ParticleTypes.SNOWFLAKE,
                            pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                            0, -0.02, 0
                    );
                }

                freeze.currentTick++;
            }

            ACTIVE_FREEZES.removeAll(toRemove);
        }

        // Trigger the wear-off shatter on anything that expired this tick
        for (ActiveFreezeTracker completed : toRemove) {
            if (completed.entity != null && completed.entity.isAlive()) {
                triggerIceShatter(completed.world, completed.entity);
            }
        }
    }

    /**
     * WEAR-OFF EXPLOSION: Blasts a ring of snow fragments away when they break free.
     */
    private static void triggerIceShatter(ClientWorld world, Entity entity) {
        Vec3d pos = entity.getPos();
        float height = entity.getHeight();

        world.playSound(
                pos.x, pos.y, pos.z,
                SoundEvents.BLOCK_POWDER_SNOW_BREAK,
                SoundCategory.PLAYERS,
                1.2f,
                1.4f,
                false
        );

        for (int i = 0; i < 40; i++) {
            double speedX = (Math.random() - 0.5) * 0.5;
            double speedY = Math.random() * 0.3;
            double speedZ = (Math.random() - 0.5) * 0.5;

            world.addParticle(
                    ParticleTypes.SNOWFLAKE,
                    pos.x, pos.y + (Math.random() * height), pos.z,
                    speedX, speedY, speedZ
            );
        }
    }
}