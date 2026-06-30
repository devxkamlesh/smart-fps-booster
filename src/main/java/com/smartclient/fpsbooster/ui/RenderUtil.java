package com.smartclient.fpsbooster.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Small drawing helpers for the 26.1+ render-state model.
 *
 * In Minecraft 26.1 the GUI rendering moved to a "render state extraction" model
 * ({@link GuiGraphicsExtractor}). Convenience methods that existed on the old
 * DrawContext (centered text, borders) are drawn manually here.
 */
public final class RenderUtil {
    private RenderUtil() {}

    /** Draw left-aligned text with shadow. */
    public static void text(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        g.text(font, text, x, y, color, true);
    }

    /** Draw horizontally centered text with shadow. */
    public static void centered(GuiGraphicsExtractor g, Font font, String text, int centerX, int y, int color) {
        int w = font.width(text);
        g.text(font, text, centerX - w / 2, y, color, true);
    }

    /** Draw a 1px rectangular outline (replacement for the old drawBorder). */
    public static void border(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        g.fill(x, y, x + width, y + 1, color);                    // top
        g.fill(x, y + height - 1, x + width, y + height, color);  // bottom
        g.fill(x, y, x + 1, y + height, color);                   // left
        g.fill(x + width - 1, y, x + width, y + height, color);   // right
    }
}
