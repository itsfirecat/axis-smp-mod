package net.itsfirecat.arcbound.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Inject(method = "getFovMultiplier", at = @At("HEAD"), cancellable = true)
    private void preventFovStretching(CallbackInfoReturnable<Float> cir) {
        // Check if our high-velocity pulse arc or cinematic movement is active
        if (/* Your condition here, e.g., ArcVisuals.isPulseActive() */ true) {

            // Grab the user's exact current FOV setting from their game options
            float currentSettingsFov = MinecraftClient.getInstance().options.getFov().getValue().floatValue();

            // Minecraft's default internal FOV baseline is 70.0.
            // The multiplier returns 1.0f when it perfectly matches their current slider setting.
            cir.setReturnValue(1.0f);
        }
    }
}