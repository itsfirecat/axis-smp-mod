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

    private static final int   MAX_AGE       = 200;
    private static final float SPEED         = 0.8f;
    private static final float HITBOX_SIZE   = 1.5f;
    private static final float DAMAGE_RADIUS = 2.5f;

    private static final DustColorTransitionParticleEffect PURPLE_TRAIL =
            new DustColorTransitionParticleEffect(
                    new Vector3f(0.28f, 0.00f, 0.55f),
                    new Vector3f(0.50f, 0.00f, 0.70f),
                    2.5f
            );

    private int ageTicks = 0;

    public HollowPurpleEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    public HollowPurpleEntity(EntityType<? extends ProjectileEntity> type, World world, LivingEntity owner) {
        super(type, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.4, owner.getZ());
        this.noClip = true;

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
        this.ageTicks++;

        Vec3d vel = this.getVelocity();
        this.setPosition(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);

        if (this.getWorld().isClient()) {
            // Dense sphere of particles forming the visible "ball"
            for (int i = 0; i < 30; i++) {
                double theta  = Math.random() * 2 * Math.PI;
                double phi    = Math.acos(2 * Math.random() - 1);
                double radius = 0.9 + Math.random() * 0.3;

                double ox = radius * Math.sin(phi) * Math.cos(theta);
                double oy = radius * Math.sin(phi) * Math.sin(theta);
                double oz = radius * Math.cos(phi);

                this.getWorld().addParticle(PURPLE_TRAIL,
                        this.getX() + ox,
                        this.getY() + oy,
                        this.getZ() + oz,
                        0.0, 0.0, 0.0);
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