package net.itsfirecat.arcbound.client.render;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

public class ClientSonarEffects {

    /**
     * ORIGINAL METHOD
     * This method runs on the first click (base ability use)
     */
    public static void spawnPulseWave(ClientWorld world, Vec3d center, boolean isQteSuccess) {
        System.out.println("[Arcbound-Debug] Client Render: spawnPulseWave called at " + center);

        for (int step = 0; step <= 20; step++) {
            final double radius = (step / 20.0) * 10.0;
            final int points = (int) (radius * 6) + 8;
            final int delayTicks = step;

            runDelayed(delayTicks, () -> {
                System.out.println("[Arcbound-Debug] Client Render: Executing task loop for radius step " + delayTicks);
                for (int i = 0; i < points; i++) {
                    double angle = (i * 2 * Math.PI) / points;
                    double x = center.x + (radius * Math.cos(angle));
                    double z = center.z + (radius * Math.sin(angle));

                    BlockPos searchPos = BlockPos.ofFloored(x, center.y, z);
                    BlockPos surfacePos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, searchPos);

                    double y = (Math.abs(surfacePos.getY() - center.y) > 6) ? center.y : surfacePos.getY();
                    y += 0.15;

                    var particleType = isQteSuccess ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.GLOW;
                    world.addParticle(particleType, x, y, z, 0, 0, 0);
                }
            });
        }
    }

    /**
     * NEW QTE BRANCH METHOD
     * This method renders a distinct, sharp sequential ripple sequence using dark particles
     */
    public static void spawnQtePulseWave(ClientWorld world, Vec3d center) {
        System.out.println("[Arcbound-Debug] Client Render: spawnQtePulseWave sequence triggered at " + center);

        // Runs a staggered 15-step expanding ring sequence (1 block per tick increment)
        for (int step = 1; step <= 15; step++) {
            final double radius = step * 1.0;
            final int points = (int) (radius * 8) + 12; // High density to keep the circle line sharp
            final int delayTicks = step;

            // Spawns a dedicated asynchronous scheduling thread specifically for this QTE wave
            new Thread(() -> {
                try {
                    // Staggers execution by 50 milliseconds per tick step
                    Thread.sleep(delayTicks * 50L);

                    // Hands the particle rendering back to the main client context safely
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        System.out.println("[Arcbound-Debug] Client Render: Executing QTE task loop for radius step " + delayTicks);
                        for (int i = 0; i < points; i++) {
                            double angle = (i * 2 * Math.PI) / points;
                            double x = center.x + (radius * Math.cos(angle));
                            double z = center.z + (radius * Math.sin(angle));

                            BlockPos searchPos = BlockPos.ofFloored(x, center.y, z);
                            BlockPos surfacePos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, searchPos);

                            double y = (Math.abs(surfacePos.getY() - center.y) > 6) ? center.y : surfacePos.getY();

                            // Bumped Y offset slightly higher to prevent tiny dark particles from sinking under block geometry textures
                            y += 0.45;

                            // Spawning using SMOKE instead of ASH if you want a thicker, more visible dark aesthetic outline
                            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void runDelayed(int ticks, Runnable action) {
        // Placeholder helper: Execute this utilizing your custom client tick task scheduler
        // to render staggered ring expansivity across active gameplay frame steps!
        action.run();
    }
}