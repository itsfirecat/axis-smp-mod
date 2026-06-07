package net.itsfirecat.arcbound.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HollowPurpleEntity extends ProjectileEntity {
    private int ageTicks = 0;
    private static final int MAX_AGE = 100; // Despawns after 5 seconds

    public HollowPurpleEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true; // Pass through walls to look like an absolute erasure wave
    }

    public HollowPurpleEntity(World world, LivingEntity owner) {
        // We temporarily use Snowball's type for registration convenience
        super(EntityType.SNOWBALL, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
        this.noClip = true;
    }

    // FIX: Modern 1.21.1 data tracker initialization method using DataTracker.Builder
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // No custom synced data elements are needed for standard flight tracking
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        if (this.ageTicks > MAX_AGE || this.getWorld().isClient()) {
            if (this.ageTicks > MAX_AGE) this.discard();
            return;
        }

        // Move projectile forward based on velocity
        Vec3d velocity = this.getVelocity();
        double nextX = this.getX() + velocity.x;
        double nextY = this.getY() + velocity.y;
        double nextZ = this.getZ() + velocity.z;
        this.setPosition(nextX, nextY, nextZ);

        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        // Visual effects: Spawn purple/magenta sparks along its flight path
        serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
        serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 3, 0.3, 0.3, 0.3, 0.01);

        // Sweeping kill-box: Detect anything near the ball and erase them
        Box destructionBox = this.getBoundingBox().expand(2.5); // 5-block wide sphere of erasure
        DamageSource absoluteErasure = this.getDamageSources().create(DamageTypes.OUT_OF_WORLD, this.getOwner());

        for (LivingEntity entity : serverWorld.getEntitiesByClass(LivingEntity.class, destructionBox, e -> e != this.getOwner())) {
            // Apply maximum float value to guarantee a one-hit kill bypassing absorption, armor, and resistances
            entity.damage(absoluteErasure, Float.MAX_VALUE);
            entity.setHealth(0); // Hard override confirmation
        }
    }
}