package net.itsfirecat.axissmp.item.custom;

import net.itsfirecat.axissmp.util.InfinityState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class InfinityArcItem extends Item {

    public InfinityArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {

            // activate infinity (3 seconds)
            InfinityState.activate(user, 60); // 60 ticks = 3s

            // cooldown (10 min = 12000 ticks)
            user.getItemCooldownManager().set(this, 12000);

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
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}