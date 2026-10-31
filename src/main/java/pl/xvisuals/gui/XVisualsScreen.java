package pl.xvisuals.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.xvisuals.Config;
import pl.xvisuals.XVisualsClient;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.module.Modules;
import pl.xvisuals.setting.*;
import pl.xvisuals.util.ColorUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu X Visuals: otwiera się Right Shiftem, ma płynną animację otwarcia/zamknięcia,
 * zakładki (Visual / Settings), podzakładki modułów (Sky | Aura | Trail | ...) i animowane kontrolki.
 *
 * Cały układ jest liczony w "logicznych" współrzędnych panelu (W x H), a skalowanie/animacja
 * odbywa się macierzą - dzięki temu menu mieści się przy każdym GUI scale.
 */
public class XVisualsScreen extends Screen {
    private static final int W = 460, H = 280;
    private static final int SIDE_W = 112;
    private static final int CX0 = 120, CX1 = 452;
    private static final int VIEW_Y0 = 80, VIEW_Y1 = 272;

    private static final int TEXT = 0xE8E8F0;
    private static final int GRAY = 0x8A8A98;
    private static final int PANEL = 0x0E0E13;
    private static final int SIDE = 0x15151B;
    private static final int CARD = 0x24242C;

    private enum Kind { TAB_MAIN, TAB_SUB, TOGGLE, BOOL, MODE, SLIDER, HUE, BUTTON }

    private static final class Hit {
        final Kind kind;
        final Object target;
        final float x, y, w, h;
        final boolean scrolled;

        Hit(Kind kind, Object target, float x, float y, float w, float h, boolean scrolled) {
            this.kind = kind;
            this.target = target;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.scrolled = scrolled;
        }

        boolean contains(float px, float py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    private record Key(Object o, int n) {
    }

    private final List<Hit> hits = new ArrayList<>();
    private final Map<Object, float[]> anims = new HashMap<>();
    private final Map<Category, Module> selected = new EnumMap<>(Category.class);

    private Category category = Category.VISUAL;
    private Hit dragging;

    private float scroll, scrollTarget, maxScroll;
    private float openP, dt, alpha = 1f, tScale = 1f, slide;
    private long lastNanos;
    private final long openedAt = System.currentTimeMillis();
    private boolean closing;
    private int accent = 0x8866FF;

    public XVisualsScreen() {
        super(Text.literal("X Visuals"));
    }

    // ------------------------------------------------------------------ zamykanie / cykl życia

    private void beginClose() {
        closing = true;
        dragging = null;
    }

    private void finish() {
        Config.save();
        if (client != null) client.setScreen(null);
    }

    @Override
    public void close() {
        beginClose();
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    public boolean shouldPause() {
        return false;   // gra działa dalej, więc zmiany nieba/aury widać na żywo
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // własne tło rysujemy w render() (bez rozmycia vanilli)
    }

    // ------------------------------------------------------------------ pomocnicze

    private int col(int rgb, float a) {
        return ColorUtil.alpha(rgb, Math.max(0f, Math.min(1f, a * alpha)));
    }

    private float anim(Object key, float target, float speed) {
        float[] a = anims.computeIfAbsent(key, k -> new float[]{target});
        a[0] += (target - a[0]) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - a[0]) < 0.001f) a[0] = target;
        return a[0];
    }

    private void resetContentAnim() {
        anims.put("content", new float[]{0f});
        scroll = 0f;
        scrollTarget = 0f;
    }

    private float lx(double mouseX) {
        return (float) ((mouseX - width / 2.0) / tScale + W / 2.0);
    }

    private float ly(double mouseY) {
        return (float) ((mouseY - height / 2.0 - slide) / tScale + H / 2.0);
    }

    private int sx(float logicalX) {
        return Math.round(width / 2f + (logicalX - W / 2f) * tScale);
    }

    private int sy(float logicalY) {
        return Math.round(height / 2f + slide + (logicalY - H / 2f) * tScale);
    }

    private void text(DrawContext c, String s, float x, float y, int color) {
        Draw.text(c, textRenderer, s, x, y, color);
    }

    private void textScaled(DrawContext c, String s, float x, float y, float scale, int color) {
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, y, 0f);
        m.scale(scale, scale, 1f);
        Draw.text(c, textRenderer, s, 0, 0, color);
        m.pop();
    }

    private void textCentered(DrawContext c, String s, float cx, float y, int color) {
        text(c, s, cx - textRenderer.getWidth(s) / 2f, y, color);
    }

    // ------------------------------------------------------------------ render

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float real = lastNanos == 0 ? 0.016f : Math.min(0.05f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        dt = real * Modules.INTERFACE.speed();

        openP = Math.max(0f, Math.min(1f, openP + (closing ? -dt : dt) / 0.24f));
        if (closing && openP <= 0f) {
            finish();
            return;
        }
        float ease = 1f - (float) Math.pow(1f - openP, 3);
        float fit = Math.min(1f, Math.min((width - 12f) / W, (height - 12f) / H));
        tScale = fit * (0.92f + 0.08f * ease);
        slide = (1f - ease) * 16f;
        accent = Modules.INTERFACE.accent();
        alpha = openP;

        float mx = closing ? -9999f : lx(mouseX);
        float my = closing ? -9999f : ly(mouseY);

        if (Modules.INTERFACE.dim.get()) {
            ctx.fill(0, 0, width, height, ColorUtil.alpha(0x000000, 0.5f * openP));
        }

        hits.clear();
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(width / 2f, height / 2f + slide, 0f);
        m.scale(tScale, tScale, 1f);
        m.translate(-W / 2f, -H / 2f, 0f);

        drawShell(ctx);
        drawSidebar(ctx, mx, my);
        drawContent(ctx, mx, my);

        m.pop();
    }

    private void drawShell(DrawContext ctx) {
        for (int i = 4; i >= 1; i--) {
            Draw.rrect(ctx, -i * 2, -i * 2 + 4, W + i * 4, H + i * 4, 10 + i * 2, col(0x000000, 0.06f));
        }
        Draw.rrect(ctx, 0, 0, W, H, 10, col(PANEL, 0.95f));
    }

    private void drawSidebar(DrawContext ctx, float mx, float my) {
        Draw.rrect(ctx, 6, 6, SIDE_W - 12, H - 12, 8, col(SIDE, 0.97f));

        textScaled(ctx, "X", 16, 14, 2.2f, col(accent, 1f));
        text(ctx, "VISUALS", 34, 22, col(TEXT, 1f));
        Draw.rrect(ctx, 14, 42, SIDE_W - 28, 2, 1, col(accent, 0.55f));

        Category[] cats = Category.values();
        float pillY = anim("pillY", category.ordinal() * 28f, 16f);
        Draw.rrect(ctx, 12, 54 + pillY, SIDE_W - 24, 22, 6, col(accent, 0.2f));
        Draw.rrect(ctx, 12, 58 + pillY, 3, 14, 1, col(accent, 1f));

        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            float ty = 54 + i * 28;
            boolean over = mx >= 12 && mx < SIDE_W - 12 && my >= ty && my < ty + 22;
            float hover = anim(new Key(c, 0), over ? 1f : 0f, 16f);
            float sel = anim(new Key(c, 1), c == category ? 1f : 0f, 14f);
            Draw.rrect(ctx, 12, ty, SIDE_W - 24, 22, 6, col(0xFFFFFF, 0.05f * hover * (1f - sel)));
            text(ctx, c.label, 24, ty + 7, col(ColorUtil.mix(GRAY, TEXT, Math.max(sel, hover * 0.6f)), 1f));
            hits.add(new Hit(Kind.TAB_MAIN, c, 12, ty, SIDE_W - 24, 22, false));
        }

        String key = XVisualsClient.openKey == null ? "RSHIFT"
                : XVisualsClient.openKey.getBoundKeyLocalizedText().getString();
        text(ctx, "[" + key + "] close", 14, H - 22, col(GRAY, 0.8f));
    }

    private void drawContent(DrawContext ctx, float mx, float my) {
        // karta
        Draw.rrect(ctx, SIDE_W - 2, 6, W - SIDE_W - 4, H - 12, 8, col(SIDE, 0.55f));

        List<Module> mods = Modules.of(category);
        Module mod = selected.computeIfAbsent(category, k -> mods.get(0));

        // podzakładki
        float bx = CX0 + 8;
        float selX = bx, selW = 40;
        float[] xs = new float[mods.size()];
        float[] ws = new float[mods.size()];
        for (int i = 0; i < mods.size(); i++) {
            ws[i] = textRenderer.getWidth(mods.get(i).name) + 14;
            xs[i] = bx;
            if (mods.get(i) == mod) {
                selX = bx;
                selW = ws[i];
            }
            bx += ws[i] + 4;
        }
        float pillX = anim("subX", selX, 18f);
        float pillW = anim("subW", selW, 18f);
        Draw.rrect(ctx, pillX, 10, pillW, 20, 6, col(accent, 0.22f));
        Draw.rrect(ctx, pillX + 6, 28, pillW - 12, 2, 1, col(accent, 1f));
        for (int i = 0; i < mods.size(); i++) {
            Module t = mods.get(i);
            boolean over = mx >= xs[i] && mx < xs[i] + ws[i] && my >= 10 && my < 30;
            float hover = anim(new Key(t, 0), over ? 1f : 0f, 16f);
            float sel = anim(new Key(t, 1), t == mod ? 1f : 0f, 14f);
            text(ctx, t.name, xs[i] + 7, 16, col(ColorUtil.mix(GRAY, TEXT, Math.max(sel, hover * 0.6f)), 1f));
            hits.add(new Hit(Kind.TAB_SUB, t, xs[i], 10, ws[i], 20, false));
        }

        // nagłówek modułu
        textScaled(ctx, mod.name, CX0 + 10, 40, 1.3f, col(TEXT, 1f));
        text(ctx, mod.description, CX0 + 10, 57, col(GRAY, 1f));
        if (mod.togglable) {
            float tx = CX1 - 10 - 34, ty = 44;
            boolean over = mx >= tx && mx < tx + 34 && my >= ty && my < ty + 16;
            drawSwitch(ctx, tx, ty, 34, 16, anim(new Key(mod, 5), mod.rawEnabled() ? 1f : 0f, 14f),
                    anim(new Key(mod, 6), over ? 1f : 0f, 16f));
            hits.add(new Hit(Kind.TOGGLE, mod, tx, ty, 34, 16, false));
        }
        ctx.fill(CX0 + 8, 72, CX1 - 8, 73, col(0xFFFFFF, 0.08f));

        // ustawienia (przewijana lista)
        List<Setting> visible = mod.settings.stream().filter(Setting::isVisible).toList();
        int total = 4;
        for (Setting s : visible) total += s.height() + 2;
        maxScroll = Math.max(0, total - (VIEW_Y1 - VIEW_Y0) + 4);
        scrollTarget = Math.max(0f, Math.min(maxScroll, scrollTarget));
        scroll += (scrollTarget - scroll) * (1f - (float) Math.exp(-14f * dt));

        float ca = anim("content", 1f, 10f);
        float prevAlpha = alpha;
        alpha = openP * ca;

        ctx.enableScissor(sx(CX0), sy(VIEW_Y0), sx(CX1), sy(VIEW_Y1));
        float y = VIEW_Y0 + 4 - scroll + (1f - ca) * 12f;
        for (Setting s : visible) {
            int h = s.height();
            if (y + h >= VIEW_Y0 && y <= VIEW_Y1) {
                drawSetting(ctx, s, CX0 + 6, y, CX1 - CX0 - 12, h, mx, my);
            }
            y += h + 2;
        }
        ctx.disableScissor();

        if (maxScroll > 0) {
            float trackH = VIEW_Y1 - VIEW_Y0 - 8;
            float thumbH = Math.max(18f, trackH * (VIEW_Y1 - VIEW_Y0) / (total + 4f));
            float thumbY = VIEW_Y0 + 4 + (trackH - thumbH) * (scroll / maxScroll);
            Draw.rrect(ctx, CX1 - 5, thumbY, 3, thumbH, 1, col(accent, 0.6f));
        }
        alpha = prevAlpha;
    }

    // ------------------------------------------------------------------ kontrolki

    private void drawSwitch(DrawContext ctx, float x, float y, int w, int h, float on, float hover) {
        int track = ColorUtil.mix(0x3A3A46, accent, on);
        Draw.rrect(ctx, x, y, w, h, h / 2, col(track, 1f));
        int d = h - 4;
        float kx = x + 2 + on * (w - h);
        Draw.circle(ctx, kx, y + 2, d, col(ColorUtil.mix(0xD0D0D8, 0xFFFFFF, hover), 1f));
    }

    private void drawSetting(DrawContext ctx, Setting s, float x, float y, float w, int h, float mx, float my) {
        boolean inView = my >= VIEW_Y0 && my <= VIEW_Y1;
        boolean over = inView && mx >= x && mx < x + w && my >= y && my < y + h;
        float hover = anim(new Key(s, 0), over ? 1f : 0f, 16f);
        Draw.rrect(ctx, x + 2, y, w - 4, h, 6, col(0xFFFFFF, 0.045f * hover));

        float labelY = y + 4;

        if (s instanceof BoolSetting b) {
            text(ctx, b.name, x + 10, y + (h - 9) / 2f, col(TEXT, 1f));
            float sx = x + w - 10 - 26, sy = y + (h - 14) / 2f;
            drawSwitch(ctx, sx, sy, 26, 14, anim(new Key(s, 1), b.get() ? 1f : 0f, 14f), hover);
            hits.add(new Hit(Kind.BOOL, s, x, y, w, h, true));

        } else if (s instanceof ModeSetting m) {
            text(ctx, m.name, x + 10, y + (h - 9) / 2f, col(TEXT, 1f));
            float bw = 116, bh = 18, bx = x + w - 10 - bw, by = y + (h - bh) / 2f;
            boolean overBox = inView && mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            float hb = anim(new Key(s, 2), overBox ? 1f : 0f, 16f);
            Draw.rrect(ctx, bx, by, bw, bh, 5, col(ColorUtil.mix(CARD, accent, hb * 0.35f), 1f));
            textCentered(ctx, m.get(), bx + bw / 2f, by + 5, col(TEXT, 1f));
            text(ctx, "<", bx + 6, by + 5, col(ColorUtil.mix(GRAY, accent, hb), 1f));
            text(ctx, ">", bx + bw - 12, by + 5, col(ColorUtil.mix(GRAY, accent, hb), 1f));
            hits.add(new Hit(Kind.MODE, s, bx, by, bw, bh, true));

        } else if (s instanceof FloatSetting f) {
            text(ctx, f.name, x + 10, labelY, col(TEXT, 1f));
            String v = f.display();
            text(ctx, v, x + w - 10 - textRenderer.getWidth(v), labelY, col(accent, 1f));
            float tx = x + 10, tw = w - 20, ty = y + 20;
            float fr = anim(new Key(s, 3), f.fraction(), 22f);
            Draw.rrect(ctx, tx, ty, tw, 4, 2, col(0x2C2C36, 1f));
            Draw.rrect(ctx, tx, ty, Math.max(4f, fr * tw), 4, 2, col(accent, 1f));
            Draw.circle(ctx, tx + fr * tw - 5, ty - 3, 10, col(0xFFFFFF, 1f));
            hits.add(new Hit(Kind.SLIDER, s, tx, y + 14, tw, 16, true));

        } else if (s instanceof ColorSetting c) {
            text(ctx, c.name, x + 10, labelY, col(TEXT, 1f));
            Draw.rrect(ctx, x + w - 10 - 24, y + 3, 24, 10, 3, col(c.rgb(), 1f));
            float tx = x + 10, tw = w - 20, ty = y + 20;
            int steps = (int) tw;
            for (int i = 0; i < steps; i++) {
                int rgb = ColorUtil.hsv(i / (float) steps, 0.85f, 1f);
                ctx.fill(Math.round(tx) + i, Math.round(ty), Math.round(tx) + i + 1, Math.round(ty) + 5, col(rgb, 1f));
            }
            float hue = anim(new Key(s, 4), c.hue(), 22f);
            Draw.circle(ctx, tx + hue * tw - 5, ty - 2, 10, col(0xFFFFFF, 1f));
            hits.add(new Hit(Kind.HUE, s, tx, y + 14, tw, 16, true));

        } else if (s instanceof ButtonSetting btn) {
            float bx = x + 10, by = y + 4, bw = w - 20, bh = h - 8;
            Draw.rrect(ctx, bx, by, bw, bh, 6, col(ColorUtil.mix(CARD, accent, hover * 0.55f), 1f));
            textCentered(ctx, btn.name, bx + bw / 2f, by + (bh - 9) / 2f, col(TEXT, 1f));
            hits.add(new Hit(Kind.BUTTON, s, bx, by, bw, bh, true));
        }
    }

    // ------------------------------------------------------------------ wejście

    private void applyDrag(Hit h, float mx) {
        float f = (mx - h.x) / h.w;
        if (h.target instanceof FloatSetting fs) fs.setFraction(f);
        else if (h.target instanceof ColorSetting cs) cs.setHue(f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing || openP < 0.6f) return true;
        float mx = lx(mouseX), my = ly(mouseY);
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(mx, my)) continue;
            if (h.scrolled && (my < VIEW_Y0 || my > VIEW_Y1)) continue;
            switch (h.kind) {
                case TAB_MAIN -> {
                    if (category != h.target) {
                        category = (Category) h.target;
                        resetContentAnim();
                    }
                }
                case TAB_SUB -> {
                    if (selected.get(category) != h.target) {
                        selected.put(category, (Module) h.target);
                        resetContentAnim();
                    }
                }
                case TOGGLE -> ((Module) h.target).toggle();
                case BOOL -> ((BoolSetting) h.target).toggle();
                case MODE -> {
                    ModeSetting m = (ModeSetting) h.target;
                    if (button == 1) m.previous();
                    else m.next();
                }
                case SLIDER, HUE -> {
                    dragging = h;
                    applyDrag(h, mx);
                }
                case BUTTON -> ((ButtonSetting) h.target).click();
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            applyDrag(dragging, lx(mouseX));
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != null) {
            dragging = null;
            Config.save();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget = Math.max(0f, Math.min(maxScroll, scrollTarget - (float) verticalAmount * 30f));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean openKey = XVisualsClient.openKey != null && XVisualsClient.openKey.matchesKey(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || (openKey && System.currentTimeMillis() - openedAt > 250)) {
            beginClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
