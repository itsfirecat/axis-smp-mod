package net.itsfirecat.arcbound.item.custom;

import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FreezeArcItem extends Item {
    public FreezeArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            double radius = 10.0;

            world.getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class,
                    user.getBoundingBox().expand(radius),
                    entity -> entity != user
            ).forEach(entity -> {
                entity.addStatusEffect(
                        new net.minecraft.entity.effect.StatusEffectInstance(
                                net.minecraft.entity.effect.StatusEffects.SLOWNESS,
                                60,     // 3 secs = 15ticks (maybe not sure)
                                255,
                                false,
                                false,
                                false
                        )
                );
            });

            // cooldown (7 minutes = 8400t)
            user.getItemCooldownManager().set(this, ArcboundCooldowns.getFreezeCooldown());

            world.playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.BLOCK_GLASS_BREAK,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.8f
            );

            user.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}