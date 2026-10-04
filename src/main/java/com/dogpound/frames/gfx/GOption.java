package com.dogpound.frames.gfx;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * One Pride Graphics option. Every option is an int under the hood: a toggle is 0/1, a choice is an index into
 * `labels`, a slider is a value between min and max. `get` reads the live game value, `set` changes it right away.
 * Options that are planned but not built yet are created with {@link #coming()} so the menu shows the whole plan
 * without pretending they work.
 */
public final class GOption {
    public enum Kind { TOGGLE, CHOICE, SLIDER }

    public final String id, name, tip;
    public final GCategory cat;
    public final Kind kind;
    final int min, max, step;
    final String[] labels;
    final String suffix;
    private final IntSupplier get;
    private final Consumer<Integer> set;
    boolean restart, coming;

    private GOption(GCategory cat, String id, String name, String tip, Kind kind, int min, int max, int step, String[] labels,
                    String suffix, IntSupplier get, Consumer<Integer> set) {
        this.cat = cat; this.id = id; this.name = name; this.tip = tip; this.kind = kind;
        this.min = min; this.max = max; this.step = step; this.labels = labels; this.suffix = suffix;
        this.get = get; this.set = set;
    }

    public static GOption toggle(GCategory c, String id, String name, String tip, IntSupplier get, Consumer<Integer> set) {
        return new GOption(c, id, name, tip, Kind.TOGGLE, 0, 1, 1, null, "", get, set);
    }

    public static GOption choice(GCategory c, String id, String name, String tip, String[] labels, IntSupplier get, Consumer<Integer> set) {
        return new GOption(c, id, name, tip, Kind.CHOICE, 0, labels.length - 1, 1, labels, "", get, set);
    }

    public static GOption slider(GCategory c, String id, String name, String tip, int min, int max, int step, String suffix,
                                 IntSupplier get, Consumer<Integer> set) {
        return new GOption(c, id, name, tip, Kind.SLIDER, min, max, step, null, suffix, get, set);
    }

    /** a planned option: shown greyed with "coming", does nothing yet */
    public static GOption planned(GCategory c, String name, String tip) {
        GOption o = new GOption(c, "planned." + name, name, tip, Kind.TOGGLE, 0, 0, 1, null, "", () -> 0, v -> {});
        o.coming = true;
        return o;
    }

    public GOption needsRestart() { restart = true; return this; }
    public GOption coming() { coming = true; return this; }

    public int value() {
        try { return get.getAsInt(); } catch (Throwable t) { return min; }
    }

    public void apply(int v) {
        if (coming) return;
        v = Math.max(min, Math.min(max, v));
        try { set.accept(v); } catch (Throwable t) { System.out.println("[PrideGraphics] " + id + " failed: " + t); }
        GSettings.save();
    }

    /** next value on a click (right click / shift = back) */
    public void cycle(boolean back) {
        int v = value();
        if (kind == Kind.SLIDER) v += back ? -step : step;
        else v = back ? (v - 1 < min ? max : v - 1) : (v + 1 > max ? min : v + 1);
        apply(Math.max(min, Math.min(max, v)));
    }

    public String display() {
        if (coming) return "🛠 coming";
        int v = value();
        switch (kind) {
            case TOGGLE: return v != 0 ? "§aON" : "§7OFF";
            case CHOICE: return labels[Math.max(0, Math.min(labels.length - 1, v))];
            default: return v + suffix;
        }
    }

    public float fraction() { return max == min ? 0 : (value() - min) / (float) (max - min); }

    public void setFraction(float f) {
        int v = min + Math.round(f * (max - min) / step) * step;
        apply(v);
    }
}
