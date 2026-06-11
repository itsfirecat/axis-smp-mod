package net.itsfirecat.arcbound.entity;

import net.itsfirecat.arcbound.arcbound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.itsfirecat.arcbound.network.ArcVisualPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.World;

public class HollowPurpleChargeEntity extends Entity {

    // -------------------------------------------------------
    // Timing — adjust these freely (in ticks, 20 ticks = 1 second)
    // -------------------------------------------------------
    public static final int PHASE1_START_TICK  = 1;   // Blue sphere appears
    public static final int PHASE2_START_TICK  = 50;  // Purple core (after 2500ms, client merge finishes at 2000ms)
    public static final int FIRE_TICK          = 60;  // Projectile launches (3 seconds total)

    // Cooldown in ticks — stored per-player on the server
    public static final int COOLDOWN_TICKS     = 200; // 10 seconds

    private final LivingEntity caster;
    private int ticksExisted = 0;

    public HollowPurpleChargeEntity(EntityType<?> type, World world) {
        super(type, world);
        this.caster = null;
    }

    public HollowPurpleChargeEntity(World world, LivingEntity caster) {
        super(EntityType.MARKER, world);
        this.caster = caster;
        this.setPosition(caster.getX(), caster.getY(), caster.getZ());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    public void tick() {
        if (this.getWorld().isClient() || caster == null || !caster.isAlive()) {
            this.discard();
            return;
        }

        this.ticksExisted++;

        // Freeze caster in place
        caster.setVelocity(0, 0, 0);
        caster.velocityModified = true;

        // Phase 1: Blue + Red spheres
        if (this.ticksExisted == PHASE1_START_TICK) {
            broadcastVisualState(2);
        }

        // Phase 2: Purple core
        if (this.ticksExisted == PHASE2_START_TICK) {
            broadcastVisualState(3);
        }

        // Fire
        if (this.ticksExisted >= FIRE_TICK) {
            // Impact frame
            broadcastVisualState(4);

            // Spawn projectile
            HollowPurpleEntity projectile = new HollowPurpleEntity(
                    arcbound.HOLLOW_PURPLE_ENTITY, this.getWorld(), caster);
            this.getWorld().spawnEntity(projectile);

            // Reset client animation
            broadcastVisualState(0);

            // Apply cooldown to caster if player
            if (caster instanceof ServerPlayerEntity player) {
                player.getItemCooldownManager().set(
                        net.minecraft.item.Items.BLAZE_ROD, // swap to your ability item
                        COOLDOWN_TICKS
                );
            }

            this.discard();
        }
    }

    private void broadcastVisualState(int stateId) {
        if (caster == null) return;
        var players = this.getWorld().getEntitiesByClass(
                ServerPlayerEntity.class,
                this.getBoundingBox().expand(50),
                p -> true
        );
        for (ServerPlayerEntity p : players) {
            ServerPlayNetworking.send(p, new ArcVisualPayload(caster.getUuid(), stateId));
        }
    }

    @Override protected void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {}
}