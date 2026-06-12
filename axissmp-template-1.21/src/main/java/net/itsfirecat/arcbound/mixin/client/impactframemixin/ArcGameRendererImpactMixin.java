package net.itsfirecat.arcbound.mixin.client.impactframemixin;

import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class ArcGameRendererImpactMixin {

    @Shadow @Mutable @Nullable PostEffectProcessor postProcessor;
    @Shadow @Mutable private boolean postProcessorEnabled;
    @Shadow @Final private MinecraftClient client;

    private static final Identifier FLASH_ID =
            Identifier.of("minecraft", "shaders/post/arc_flash.json");

    private static boolean flashLoaded = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void arcHandlePostEffects(RenderTickCounter counter, boolean tick, CallbackInfo ci) {
        boolean shouldFlash = ArcImpactHandler.playing && !ArcImpactHandler.suppressOverlay;

        if (shouldFlash) {
            if (!flashLoaded || postProcessor == null) {
                loadPost(FLASH_ID);
                flashLoaded = (postProcessor != null);
            }
            if (postProcessor != null) {
                applyFlashUniforms();
            }
        } else {
            if (postProcessor != null) {
                postProcessor.close();
                postProcessor = null;
            }
            postProcessorEnabled = false;
            flashLoaded = false;
        }
    }

    private void applyFlashUniforms() {
        if (postProcessor == null) return;

        float r, g, b;
        float mode = 0.0f;

        switch (ArcImpactHandler.currentFrameType) {
            case WHITE -> { r = 1.0f; g = 1.0f; b = 1.0f; }
            case BLACK -> { r = 0.0f; g = 0.0f; b = 0.0f; }
            case RED   -> { r = 1.0f; g = 0.0f; b = 0.0f; }
            case CYAN  -> { r = 0.0f; g = 1.0f; b = 1.0f; }
            case INVERT -> { r = 0.0f; g = 0.0f; b = 0.0f; mode = 1.0f; }
            default -> { r = 1.0f; g = 1.0f; b = 1.0f; }
        }

        postProcessor.setUniforms("FlashR", r);
        postProcessor.setUniforms("FlashG", g);
        postProcessor.setUniforms("FlashB", b);
        postProcessor.setUniforms("FlashMode", mode);
    }

    private void loadPost(Identifier id) {
        if (postProcessor != null) {
            postProcessor.close();
            postProcessor = null;
        }
        try {
            postProcessor = new PostEffectProcessor(
                    client.getTextureManager(),
                    client.getResourceManager(),
                    client.getFramebuffer(),
                    id
            );
            postProcessor.setupDimensions(
                    client.getWindow().getFramebufferWidth(),
                    client.getWindow().getFramebufferHeight()
            );
            postProcessorEnabled = true;
        } catch (Exception e) {
            System.err.println("[ArcMod] Failed to load post shader: " + id);
            e.printStackTrace();
            postProcessor = null;
            postProcessorEnabled = false;
        }
    }
}