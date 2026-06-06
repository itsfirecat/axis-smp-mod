package net.itsfirecat.axissmp.mixin;

import net.itsfirecat.axissmp.qte.client.ClientQTE;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void interceptQTEClicks(CallbackInfo ci) {
        // If a QTE minigame is actively running, redirect the click event
        if (ClientQTE.isActive() && !ClientQTE.isFailed()) {
            ClientQTE.handleClientRightClick();
            ci.cancel(); // Properly halts the remainder of the vanilla item use process
        }
    }
}