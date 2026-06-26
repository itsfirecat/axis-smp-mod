package net.itsfirecat.arcbound.item.custom;

import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.arcbound.qte.QTEType;
import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.itsfirecat.arcbound.util.InfinityState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class InfinityArcItem extends Item {

    public InfinityArcItem(Settings settings) {
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

            // activate infinity (3 seconds)
            InfinityState.activate(user, 60); // 60 ticks = 3s

            // visual feedback
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0));

            world.playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.ENTITY_WITHER_SPAWN,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.8f
            );

            user.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));

            // ==========================================
            // QTE ENGAGEMENT
            // ==========================================
            // Fire off a 40-tick (2-second total loop: 1s right, 1s left bounce) timing window
            QTEManager.startQTE(serverPlayer, QTEType.INFINITY, 40, world.getTime());

            // 60 sec testing cooldown, production is 12000
            user.getItemCooldownManager().set(this, ArcboundCooldowns.getInfinityCooldown());

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }
}