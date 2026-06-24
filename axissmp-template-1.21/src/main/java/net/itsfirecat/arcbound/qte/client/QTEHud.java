package net.itsfirecat.arcbound.qte.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class QTEHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();

        // ==========================================
        // FULL-SCREEN IMPACT FLASH OVERLAY
        // ==========================================
        if (ArcImpactHandler.playing) {
            int flashColor;
            switch (ArcImpactHandler.currentFrameType) {
                case WHITE  -> flashColor = 0xFFFFFFFF;
                case BLACK  -> flashColor = 0xFF000000;
                case RED    -> flashColor = 0xFFFF0000;
                case CYAN   -> flashColor = 0xFF00FFFF;
                case INVERT -> flashColor = 0x00000000; // invert handled per-entity only
                default     -> flashColor = 0xFFFFFFFF;
            }
            if (flashColor != 0x00000000) {
                drawContext.fill(0, 0, width, height, flashColor);
            }
        }

// ==========================================
// FADING FLASHBANG OVERLAY
// ==========================================
        if (ArcImpactHandler.fadingFlash) {
            float alpha = ArcImpactHandler.getFadeAlpha();
            if (alpha > 0.0f) {
                int baseColor = switch (ArcImpactHandler.getFadeColorType()) {
                    case WHITE -> 0xFFFFFF;
                    case BLACK -> 0x000000;
                    case RED   -> 0xFF0000;
                    case CYAN  -> 0x00FFFF;
                    default    -> 0xFFFFFF;
                };
                int alphaInt = (int)(alpha * 255) << 24;
                drawContext.fill(0, 0, width, height, baseColor | alphaInt);
            }
        }

        // ==========================================
        // QTE SLIDER BAR RENDERING
        // ==========================================
        if (!ClientQTE.isActive()) return;

        int barWidth = 100;
        int barHeight = 6;
        int x = (width - barWidth) / 2;
        int y = height - 65;

        if (ClientQTE.isFailed()) {
            long time = System.currentTimeMillis();
            x += (int) (Math.sin(time * 0.05) * 4);
        }

        int barColor = ClientQTE.isFailed() ? 0xFFFF0000 : 0xFF2D2D2D;
        int greenColor = ClientQTE.isFailed() ? 0xFFFF0000 : 0xFF00FF00;

        drawContext.fill(x, y, x + barWidth, y + barHeight, barColor);

        int greenLeft = x + (int)(barWidth * ClientQTE.getGreenZoneStart());
        int greenRight = x + (int)(barWidth * ClientQTE.getGreenZoneEnd());
        drawContext.fill(greenLeft, y, greenRight, y + barHeight, greenColor);

        if (!ClientQTE.isFailed()) {
            float progress = ClientQTE.getProgress(renderTickCounter);
            int markerX = x + (int)(barWidth * progress);
            drawContext.fill(markerX - 1, y - 2, markerX + 1, y + barHeight + 2, 0xFFFFFFFF);
        }
    }
}