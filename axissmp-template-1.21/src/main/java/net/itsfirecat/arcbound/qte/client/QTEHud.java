package net.itsfirecat.arcbound.qte.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

public class QTEHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();

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

        int greenLeft = x + (int)(barWidth * 0.40f);
        int greenRight = x + (int)(barWidth * 0.60f);
        drawContext.fill(greenLeft, y, greenRight, y + barHeight, greenColor);

        if (!ClientQTE.isFailed()) {
            float progress = ClientQTE.getProgress(renderTickCounter);
            int markerX = x + (int)(barWidth * progress);
            drawContext.fill(markerX - 1, y - 2, markerX + 1, y + barHeight + 2, 0xFFFFFFFF);
        }
    }
}