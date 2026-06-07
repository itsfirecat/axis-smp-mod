package net.itsfirecat.arcbound.mixin;

import net.itsfirecat.arcbound.util.InfinityState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelInfinityDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if ((Object)this instanceof PlayerEntity player) {
            if (InfinityState.isActive(player)) {
                System.out.println("infinity blocked damage");
                cir.setReturnValue(false);
            }
        }
    }
}