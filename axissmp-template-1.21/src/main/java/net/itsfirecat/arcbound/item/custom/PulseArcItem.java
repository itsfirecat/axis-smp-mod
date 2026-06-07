package net.itsfirecat.arcbound.item.custom;

import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.arcbound.qte.QTEType;
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
            // Guard with cooldown manager check
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }

            // ==========================================
            // BASE ABILITY: TRIPPED IMMEDIATELY ON FIRST USE
            // ==========================================
            double radius = 5.0;

            world.getEntitiesByClass(
                    LivingEntity.class,
                    user.getBoundingBox().expand(radius),
                    entity -> entity != user
            ).forEach(entity -> {
                Vec3d direction = entity.getPos()
                        .subtract(user.getPos())
                        .normalize();

                double strength = 2.5;

                entity.setVelocity(
                        direction.x * strength,
                        1.0, // preserved your verified slight pop vector height
                        direction.z * strength
                );
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

            user.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));

            // ==========================================
            // QTE ENGAGEMENT
            // ==========================================
            // Start a 40-tick (2-second total loop: 1s right, 1s left bounce) timing window
            QTEManager.startQTE(serverPlayer, QTEType.PULSE, 40, world.getTime());

            // Testing cooldown (3 seconds = 60 ticks). Replace with 12000 for your 10 min production tier later.
            user.getItemCooldownManager().set(this, 60);

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }
}