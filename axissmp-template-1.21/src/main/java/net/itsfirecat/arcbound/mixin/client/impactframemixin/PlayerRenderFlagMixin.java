package net.itsfirecat.arcbound.mixin.client.impactframemixin;

import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public class PlayerRenderFlagMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void onRenderStart(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                                MatrixStack matrices, VertexConsumerProvider provider,
                                int light, CallbackInfo ci) {
        ArcImpactHandler.renderingPlayer = true;
    }

    @Inject(
            method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN")
    )
    private void onRenderEnd(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                              MatrixStack matrices, VertexConsumerProvider provider,
                              int light, CallbackInfo ci) {
        ArcImpactHandler.renderingPlayer = false;
    }
}
