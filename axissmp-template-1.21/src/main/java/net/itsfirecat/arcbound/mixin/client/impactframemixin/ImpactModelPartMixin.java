package net.itsfirecat.arcbound.mixin.client.impactframemixin;

import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelPart.class)
public abstract class ImpactModelPartMixin {

    private static boolean shouldFlash() {
        return ArcImpactHandler.playing && ArcImpactHandler.renderingPlayer;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int forceLight(int light) {
        return shouldFlash() ? 15728880 : light;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int forceOverlay(int overlay) {
        if (!shouldFlash()) return overlay;
        return switch (ArcImpactHandler.currentFrameType) {
            case BLACK, CYAN -> 0; // white entities — use hurt flash overlay
            default -> OverlayTexture.DEFAULT_UV;
        };
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private VertexConsumer wrapConsumer(VertexConsumer original) {
        if (!shouldFlash()) return original;
        return new VertexConsumer() {
            @Override public VertexConsumer vertex(float x, float y, float z) {
                original.vertex(x, y, z); return this;
            }
            @Override public VertexConsumer color(int r, int g, int b, int a) {
                return switch (ArcImpactHandler.currentFrameType) {
                    case WHITE, RED  -> original.color(0, 0, 0, 255);
                    case BLACK, CYAN -> original.color(255, 255, 255, 255);
                    case INVERT      -> original.color(255 - r, 255 - g, 255 - b, a);
                };
            }
            @Override public VertexConsumer texture(float u, float v) {
                original.texture(u, v); return this;
            }
            @Override public VertexConsumer overlay(int u, int v) {
                original.overlay(u, v); return this;
            }
            @Override public VertexConsumer light(int u, int v) {
                original.light(240, 240); return this;
            }
            @Override public VertexConsumer normal(float x, float y, float z) {
                original.normal(x, y, z); return this;
            }
        };
    }
}