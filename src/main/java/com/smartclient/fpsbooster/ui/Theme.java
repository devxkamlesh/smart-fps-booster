package com.smartclient.fpsbooster.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared visual theme + drawing helpers for the Smart FPS Booster UI.
 *
 * Built on the 26.1+ {@link GuiGraphicsExtractor} render-state model using
 * gradients, outlines and lines for a modern dark dashboard look.
 */
public final class Theme {
    private Theme() {}

    // ── Palette (ARGB) ────────────────────────────────────────────────
    public static final int BACKDROP_TOP = 0xE0070A14;
    public static final int BACKDROP_BOT = 0xF0020308;

    public static final int PANEL_TOP    = 0xFF1B1E33;
    public static final int PANEL_BOT    = 0xFF0F1120;
    public static final int PANEL_BORDER = 0xFF313663;
    public static final int SHADOW       = 0x66000000;

    public static final int HEADER_TOP   = 0xFF2C4F94;
    public static final int HEADER_BOT   = 0xFF1C2C52;

    public static final int CARD_TOP     = 0xFF242843;
    public static final int CARD_BOT     = 0xFF171A2E;
    public static final int CARD_BORDER  = 0xFF2E3358;

    public static final int ACCENT       = 0xFF4C8DFF;
    public static final int ACCENT_LIGHT = 0xFF8FB6FF;

    public static final int TEXT         = 0xFFEAEEF7;
    public static final int TEXT_DIM     = 0xFF9AA1BD;
    public static final int TEXT_MUTED   = 0xFF5A6080;

    public static final int GOOD = 0xFF49D6A0;
    public static final int WARN = 0xFFF3C24B;
    public static final int BAD  = 0xFFFF5D78;

    public static final int TRACK = 0xFF2A2E4A;

    // ── Primitives ────────────────────────────────────────────────────
    public static void backdrop(GuiGraphicsExtractor g, int w, int h) {
        g.fillGradient(0, 0, w, h, BACKDROP_TOP, BACKDROP_BOT);
    }

    /** Draws a panel with drop shadow, vertical gradient body, border and a subtle top highlight. */
    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x + 4, y + 4, x + w + 4, y + h + 4, SHADOW);
        g.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOT);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x18FFFFFF); // top highlight
        g.outline(x, y, w, h, PANEL_BORDER);
    }

    /** Title bar across the top of a panel. Returns the Y just below it. */
    public static int titleBar(GuiGraphicsExtractor g, Font font, int x, int y, int w, String title) {
        int hgt = 24;
        g.fillGradient(x + 1, y + 1, x + w - 1, y + hgt, HEADER_TOP, HEADER_BOT);
        g.fill(x + 1, y + hgt, x + w - 1, y + hgt + 1, ACCENT); // accent underline
        // accent tab on the left
        g.fill(x + 1, y + 1, x + 4, y + hgt, ACCENT);
        centered(g, font, title, x + w / 2, y + 8, TEXT);
        return y + hgt + 1;
    }

    /** Section heading: small accent bar + uppercase label + faint divider line. */
    public static void section(GuiGraphicsExtractor g, Font font, int x, int y, int w, String label) {
        g.fill(x, y, x + 3, y + 8, ACCENT);
        g.text(font, label.toUpperCase(), x + 8, y, ACCENT_LIGHT, true);
        int tx = x + 8 + font.width(label.toUpperCase()) + 6;
        if (tx < x + w) g.horizontalLine(tx, x + w, y + 4, 0x33FFFFFF);
    }

    public static void card(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, CARD_TOP, CARD_BOT);
        g.outline(x, y, w, h, CARD_BORDER);
    }

    /** A labelled stat tile: small caption on top, coloured value below. */
    public static void stat(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                            String caption, String value, int valueColor) {
        card(g, x, y, w, h);
        g.text(font, caption, x + 6, y + 5, TEXT_MUTED, true);
        g.text(font, value, x + 6, y + h - 13, valueColor, true);
    }

    /** Horizontal progress/value bar with track, fill and border. */
    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float pct, int fill) {
        pct = Math.max(0f, Math.min(1f, pct));
        g.fill(x, y, x + w, y + h, TRACK);
        int fw = (int) (w * pct);
        if (fw > 0) g.fillGradient(x, y, x + fw, y + h, brighten(fill), fill);
        g.outline(x, y, w, h, 0xFF000000);
    }

    public static void centered(GuiGraphicsExtractor g, Font font, String text, int cx, int y, int color) {
        g.text(font, text, cx - font.width(text) / 2, y, color, true);
    }

    public static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.outline(x, y, w, h, color);
    }

    /** Colour for an FPS value relative to a target (good / warn / bad). */
    public static int fpsColor(int fps, int target) {
        if (fps >= target) return GOOD;
        if (fps >= Math.max(30, target * 0.6f)) return WARN;
        return BAD;
    }

    private static int brighten(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 40);
        int gg = Math.min(255, ((argb >> 8) & 0xFF) + 40);
        int b = Math.min(255, (argb & 0xFF) + 40);
        return (a << 24) | (r << 16) | (gg << 8) | b;
    }
}
