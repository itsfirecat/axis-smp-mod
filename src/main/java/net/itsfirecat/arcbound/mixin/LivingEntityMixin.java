package net.itsfirecat.arcbound.mixin;

import net.itsfirecat.arcbound.util.InfinityState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelInfinityDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if ((Object)this instanceof PlayerEntity player) {
            // If the damage source is out-of-world, creative-override, or a system command (/kill), DO NOT BLOCK IT
            if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return;
            }

            if (InfinityState.isActive(player)) {
                cir.setReturnValue(false);
            }
        }
    }
}