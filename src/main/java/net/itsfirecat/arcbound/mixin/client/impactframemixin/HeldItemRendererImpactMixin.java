package net.itsfirecat.arcbound.mixin.client.impactframemixin;

import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererImpactMixin {

    @ModifyVariable(
            method = "renderFirstPersonItem",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private VertexConsumerProvider wrapProvider(VertexConsumerProvider original) {
        if (!ArcImpactHandler.playing) return original;
        final boolean white = ArcImpactHandler.whiteFrame;
        return layer -> {
            VertexConsumer base = original.getBuffer(layer);
            return new VertexConsumer() {
                @Override public VertexConsumer vertex(float x, float y, float z) { base.vertex(x, y, z); return this; }
                @Override public VertexConsumer color(int r, int g, int b, int a) {

                    return white ? base.color(0, 0, 0, 255) : base.color(255, 255, 255, 255);
                }
                @Override public VertexConsumer texture(float u, float v) { base.texture(u, v); return this; }
                @Override public VertexConsumer overlay(int u, int v) { base.overlay(u, v); return this; }
                @Override public VertexConsumer light(int u, int v) { base.light(240, 240); return this; }
                @Override public VertexConsumer normal(float x, float y, float z) { base.normal(x, y, z); return this; }
            };
        };
    }
}