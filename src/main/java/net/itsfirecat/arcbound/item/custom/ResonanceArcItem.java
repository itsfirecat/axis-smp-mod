package net.itsfirecat.arcbound.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.itsfirecat.arcbound.network.ResonancePulsePayload;
import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.arcbound.qte.QTEType;
import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class ResonanceArcItem extends Item {
    public ResonanceArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }

            // 1. Core Mechanics
            executeBaseAbility(serverPlayer, (ServerWorld) world);

            // 2. Client Effects Hook (Applies local shader tint or darkness overlay)
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 30, 0, false, false, false));

            // 3. Network Sync - Send payload to trigger client side terrain sonar particles
            ServerPlayNetworking.send(serverPlayer, new ResonancePulsePayload(serverPlayer.getPos(), false));

            // 4. QTE Initialization (40 ticks)
            QTEManager.startQTE(serverPlayer, QTEType.RESONANCE, 40, world.getTime());

            // 5. Apply variable tick limiters
            user.getItemCooldownManager().set(this, ArcboundCooldowns.getResonanceCooldown());
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    public static void executeBaseAbility(ServerPlayerEntity player, ServerWorld world) {
        double radius = 10.0;
        Box area = player.getBoundingBox().expand(radius);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, area, entity -> entity != player);

        for (LivingEntity entity : targets) {
            // Added vector height offset bump to separate entities cleanly from surface friction
            Vec3d direction = player.getPos().subtract(entity.getPos()).normalize().multiply(1.2).add(0, 0.2, 0);
            entity.setVelocity(direction);
            entity.velocityModified = true;

            // Force immediate velocity updates over the wire for other connected players
            if (entity instanceof ServerPlayerEntity targetPlayer) {
                targetPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(targetPlayer));
            }
        }
    }
}