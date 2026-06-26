package net.itsfirecat.arcbound.item.custom;

import net.itsfirecat.arcbound.network.ArcVisualPayload;
import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.arcbound.qte.QTEType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PulseArcItem extends Item {
    public PulseArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }

            // ==========================================
            // BASE ABILITY: TRIGGERED IMMEDIATELY ON USE
            // ==========================================
            double radius = 5.0;

            world.getEntitiesByClass(
                    LivingEntity.class,
                    user.getBoundingBox().expand(radius),
                    entity -> entity != user
            ).forEach(entity -> {
                Vec3d direction = entity.getPos().subtract(user.getPos()).normalize();
                double strength = 2.5;
                entity.setVelocity(direction.x * strength, 1.0, direction.z * strength);
                entity.velocityModified = true;
            });

            world.playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.2f
            );

            // FORCE PACKET BROADCAST TO SHOW SHOCKWAVE IMMEDIATELY ON USE
            // stateId 5 = The expanding horizontal pulse ring
            for (ServerPlayerEntity trackingPlayer : serverPlayer.getServerWorld().getPlayers()) {
                ServerPlayNetworking.send(trackingPlayer, new ArcVisualPayload(serverPlayer.getUuid(), 5));
            }

            user.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));

            // ==========================================
            // QTE ENGAGEMENT & PRODUCTION COOLDOWN
            // ==========================================
            QTEManager.startQTE(serverPlayer, QTEType.PULSE, 40, world.getTime());

            // Set final tier production cooldown: 10 minutes = 12000 ticks
            user.getItemCooldownManager().set(this, ArcboundCooldowns.getPulseCooldown());

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }
}