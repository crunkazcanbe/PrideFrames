package com.dogpound.frames.gfx;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

/**
 * Pride Graphics: every visual option in one Pride-style menu. Categories on the left, options on the right
 * (click = next value, right-click = previous, drag a slider), the hovered option's explanation at the bottom.
 */
public class GraphicsScreen extends GuiScreen {
    private final GuiScreen parent;
    private GCategory cat = GCategory.GENERAL;
    private int scroll, catScroll;
    private GOption dragging;
    private String status = "";
    private long statusAt;
    private PrideFrame f;

    private static final int ROW = 22, TAB = 18, LEFT = 150;

    public GraphicsScreen(GuiScreen parent) { this.parent = parent; GOptions.build(); }

    @Override public boolean doesGuiPauseGame() { return true; }

    @Override
    public void initGui() { f = PrideFrame.fit(width, height); }

    private int contentX() { return f.cx + LEFT + 8; }
    private int contentW() { return f.cw - LEFT - 8; }
    private int listTop() { return f.cy + 4; }
    private int listH() { return f.ch - 34; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        int working = 0, total = GOptions.ALL.size();
        for (GOption o : GOptions.ALL) if (!o.coming) working++;
        f.draw(this, "Pride Graphics", "§a" + working + " working §7· §e" + (total - working) + " coming");

        // ---- categories
        GCategory[] cats = GCategory.values();
        int maxCat = Math.max(0, cats.length * TAB - listH() - 30);
        catScroll = Math.max(0, Math.min(catScroll, maxCat));
        PrideFrame.clip(f.cx, listTop(), LEFT, listH() + 30);
        for (int i = 0; i < cats.length; i++) {
            int y = listTop() + i * TAB - catScroll;
            boolean hover = mx >= f.cx && mx < f.cx + LEFT && my >= y && my < y + TAB - 2;
            PrideFrame.tile(f.cx, y, LEFT, TAB - 2, PrideFrame.RAINBOW[i % PrideFrame.RAINBOW.length], hover, cats[i] == cat);
            int ready = 0, all = 0;
            for (GOption o : GOptions.of(cats[i])) { all++; if (!o.coming) ready++; }
            fontRenderer.drawStringWithShadow(cats[i].icon + " " + cats[i].title, f.cx + 6, y + 5, 0xFFFFFF);
            String n = ready + "/" + all;
            fontRenderer.drawStringWithShadow(n, f.cx + LEFT - 6 - fontRenderer.getStringWidth(n), y + 5, ready == all ? 0x80FF80 : 0xA090C0);
        }
        PrideFrame.unclip();

        // ---- options of the category
        List<GOption> opts = GOptions.of(cat);
        int x = contentX(), w = contentW(), top = listTop(), h = listH();
        int maxScroll = Math.max(0, opts.size() * ROW - h);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        PrideFrame.card(x, top - 2, w, h + 4, PrideFrame.PINK);
        GOption hovered = null;
        PrideFrame.clip(x, top, w, h);
        for (int i = 0; i < opts.size(); i++) {
            GOption o = opts.get(i);
            int y = top + 4 + i * ROW - scroll;
            if (y + ROW < top || y > top + h) continue;
            boolean rowHover = mx >= x && mx < x + w && my >= y && my < y + ROW - 2 && my >= top && my < top + h;
            if (rowHover) { hovered = o; Gui.drawRect(x + 2, y - 1, x + w - 2, y + ROW - 3, 0x30FFFFFF); }
            fontRenderer.drawStringWithShadow((o.coming ? "§8" : "") + o.name + (o.restart ? " §6⟳" : ""), x + 8, y + 5, o.coming ? 0x777777 : 0xFFFFFF);
            int bw = Math.min(170, w / 2 - 10), bx = x + w - bw - 8;
            if (o.kind == GOption.Kind.SLIDER && !o.coming) {
                Gui.drawRect(bx, y + 2, bx + bw, y + ROW - 4, PrideFrame.BUTTON);
                int knob = bx + (int) (o.fraction() * (bw - 6));
                Gui.drawRect(bx, y + 2, knob + 3, y + ROW - 4, 0x806A3FA0);
                Gui.drawRect(knob, y + 1, knob + 6, y + ROW - 3, PrideFrame.PINK);
                String v = o.display();
                fontRenderer.drawStringWithShadow(v, bx + (bw - fontRenderer.getStringWidth(v)) / 2F, y + 5, 0xFFFFFF);
            } else {
                PrideFrame.button(bx, y + 2, bw, ROW - 6, o.display(), o.coming ? 0xFF1A1622 : PrideFrame.BUTTON, mx, my);
            }
        }
        PrideFrame.unclip();
        PrideFrame.scrollbar(x + w - 4, top, h, scroll, h, opts.size() * ROW);

        // ---- explanation line + back
        int by = f.cy + f.ch - 24;
        String tip = hovered != null ? hovered.tip + (hovered.restart ? "  §6⟳ applies after a restart" : "")
                : System.currentTimeMillis() - statusAt < 4000 ? status : "§7Click = next value · right-click = back · drag sliders · scroll the lists";
        fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(tip, f.cw - 110), f.cx + 2, by + 7, 0xDDDDEE);
        PrideFrame.button(f.cx + f.cw - 100, by, 100, 22, "◀ Done", PrideFrame.BUTTON, mx, my);
        super.drawScreen(mx, my, pt);
    }

    private GOption optionAt(int mx, int my) {
        List<GOption> opts = GOptions.of(cat);
        int x = contentX(), w = contentW(), top = listTop(), h = listH();
        if (mx < x || mx >= x + w || my < top || my >= top + h) return null;
        int i = (my - top - 4 + scroll) / ROW;
        return i >= 0 && i < opts.size() ? opts.get(i) : null;
    }

    private void slide(GOption o, int mx) {
        int w = contentW(), bw = Math.min(170, w / 2 - 10), bx = contentX() + w - bw - 8;
        o.setFraction(Math.max(0F, Math.min(1F, (mx - bx) / (float) (bw - 6))));
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        int by = f.cy + f.ch - 24;
        if (mx >= f.cx + f.cw - 100 && my >= by && my < by + 22) { mc.displayGuiScreen(parent); return; }
        if (mx >= f.cx && mx < f.cx + LEFT && my >= listTop()) {
            int i = (my - listTop() + catScroll) / TAB;
            if (i >= 0 && i < GCategory.values().length) { cat = GCategory.values()[i]; scroll = 0; }
            return;
        }
        GOption o = optionAt(mx, my);
        if (o == null) return;
        if (o.coming) { status = "§e🛠 " + o.name + " is on the plan - not built yet."; statusAt = System.currentTimeMillis(); return; }
        if (o.kind == GOption.Kind.SLIDER) { dragging = o; slide(o, mx); }
        else o.cycle(button == 1 || isShiftKeyDown());
        if (o.restart) { status = "§6⟳ " + o.name + " applies the next time the game starts."; statusAt = System.currentTimeMillis(); }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long time) { if (dragging != null) slide(dragging, mx); }

    @Override
    protected void mouseReleased(int mx, int my, int state) { dragging = null; }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d == 0) return;
        int mx = Mouse.getEventX() * width / mc.displayWidth;
        if (mx < f.cx + LEFT) catScroll -= Integer.signum(d) * TAB * 2; else scroll -= Integer.signum(d) * ROW * 2;
    }

    @Override
    public void onGuiClosed() { GSettings.save(); }
}
