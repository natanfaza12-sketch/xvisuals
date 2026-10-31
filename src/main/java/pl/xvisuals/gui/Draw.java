package pl.xvisuals.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Proste prymitywy 2D: zaokrąglone prostokąty, koła, tekst z bezpiecznym alfa. */
public final class Draw {
    private Draw() {
    }

    public static void rrect(DrawContext c, float fx, float fy, float fw, float fh, int r, int color) {
        if ((color >>> 24) == 0) return;
        int x = Math.round(fx), y = Math.round(fy), w = Math.round(fw), h = Math.round(fh);
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            c.fill(x, y, x + w, y + h, color);
            return;
        }
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * (double) r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            c.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
        if (h > 2 * r) {
            c.fill(x, y + r, x + w, y + h - r, color);
        }
    }

    public static void circle(DrawContext c, float x, float y, int diameter, int color) {
        rrect(c, x, y, diameter, diameter, diameter / 2, color);
    }

    /** Tekst; przy alfa < ~4/255 Minecraft robiłby go nieprzezroczystym, więc taki tekst pomijamy. */
    public static void text(DrawContext c, TextRenderer tr, String s, float x, float y, int color) {
        if (((color >>> 24) & 0xFF) < 10) return;
        c.drawText(tr, s, Math.round(x), Math.round(y), color, false);
    }
}
