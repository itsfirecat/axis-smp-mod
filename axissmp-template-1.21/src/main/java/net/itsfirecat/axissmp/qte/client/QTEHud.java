package net.itsfirecat.axissmp.qte.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class QTEHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (!ClientQTE.isActive()) return;

        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();

        int barWidth = 100;
        int barHeight = 6;
        int x = (width - barWidth) / 2;
        int y = height - 65;

        // Apply Shaking Effect if failed
        if (ClientQTE.isFailed()) {
            long time = System.currentTimeMillis();
            x += (int) (Math.sin(time * 0.05) * 4); // Fast side-to-side shake
        }

        // 1. Bar Color Selection
        int barColor = ClientQTE.isFailed() ? 0xFFFF0000 : 0xFF2D2D2D; // Red background on fail
        int greenColor = ClientQTE.isFailed() ? 0xFFFF0000 : 0xFF00FF00;

        // Draw track
        drawContext.fill(x, y, x + barWidth, y + barHeight, barColor);

        // 2. Expanded Green Target Box (0.40 to 0.60 map width range)
        int greenLeft = x + (int)(barWidth * 0.40f);
        int greenRight = x + (int)(barWidth * 0.60f);
        drawContext.fill(greenLeft, y, greenRight, y + barHeight, greenColor);

        // 3. Draw Slider Cursor (Only if not failed)
        if (!ClientQTE.isFailed()) {
            float progress = ClientQTE.getProgress(renderTickCounter);
            int markerX = x + (int)(barWidth * progress);
            drawContext.fill(markerX - 1, y - 2, markerX + 1, y + barHeight + 2, 0xFFFFFFFF);
        }
    }
}