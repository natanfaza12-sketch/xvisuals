package pl.xvisuals.util;

public final class ColorUtil {
    private ColorUtil() {
    }

    /** HSV (0..1) -> RGB (0xRRGGBB). */
    public static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float r, g, b;
        float hh = h * 6f;
        int i = (int) Math.floor(hh);
        float f = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float t = v * (1f - s * (1f - f));
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    /** Dodaje kanał alfa (0..1) do koloru RGB. */
    public static int alpha(int rgb, float a) {
        int alpha = clamp255(a);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    /** Miesza dwa kolory RGB. */
    public static int mix(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 0xFF) * (1f - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >> 8) & 0xFF) * (1f - t) + ((c2 >> 8) & 0xFF) * t);
        int b = (int) ((c1 & 0xFF) * (1f - t) + (c2 & 0xFF) * t);
        return (r << 16) | (g << 8) | b;
    }

    public static int clamp255(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255f)));
    }
}
