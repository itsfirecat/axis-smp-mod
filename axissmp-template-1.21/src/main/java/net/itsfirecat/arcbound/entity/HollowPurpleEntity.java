package net.itsfirecat.arcbound.entity;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class HollowPurpleEntity extends ProjectileEntity {

    // -------------------------------------------------------
    // Tuning
    // -------------------------------------------------------
    private static final int   MAX_AGE          = 200;   // ticks before auto-discard
    private static final float SPEED            = 0.8f;  // blocks per tick
    private static final float HITBOX_SIZE      = 1.5f;  // width and height
    private static final float DAMAGE_RADIUS    = 2.5f;  // expand beyond hitbox

    private static final DustColorTransitionParticleEffect PURPLE_TRAIL =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.28f, 0.00f, 0.55f),
                    new Vector3f(0.50f, 0.00f, 0.70f),
                    1.8f
            );

    private int ageTicks = 0;

    public HollowPurpleEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public HollowPurpleEntity(EntityType<? extends ProjectileEntity> type, World world, LivingEntity owner) {
        super(type, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.4, owner.getZ());

        Vec3d look = owner.getRotationVec(1.0f);
        this.setVelocity(look.x * SPEED, look.y * SPEED, look.z * SPEED);
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.fixed(HITBOX_SIZE, HITBOX_SIZE);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    public void tick() {
        // DO NOT call super.tick() — it interferes with noClip movement
        this.ageTicks++;

        Vec3d vel = this.getVelocity();
        this.setPosition(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);

        if (this.getWorld().isClient()) {
            for (int i = 0; i < 4; i++) {
                double sx = (Math.random() - 0.5) * 1.0;
                double sy = (Math.random() - 0.5) * 1.0;
                double sz = (Math.random() - 0.5) * 1.0;
                this.getWorld().addParticle(PURPLE_TRAIL,
                        this.getX() + sx,
                        this.getY() + sy,
                        this.getZ() + sz,
                        -vel.x * 0.3, -vel.y * 0.3, -vel.z * 0.3);
            }
            return;
        }

        if (this.ageTicks > MAX_AGE) {
            this.discard();
            return;
        }

        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        Box destructionBox = this.getBoundingBox().expand(DAMAGE_RADIUS);
        DamageSource absoluteErasure = this.getDamageSources().create(
                DamageTypes.OUT_OF_WORLD, this.getOwner());

        for (LivingEntity entity : serverWorld.getEntitiesByClass(
                LivingEntity.class, destructionBox, e -> e != this.getOwner())) {
            entity.damage(absoluteErasure, Float.MAX_VALUE);
            entity.setHealth(0);
        }
    }
}