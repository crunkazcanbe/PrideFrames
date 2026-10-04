package com.dogpound.frames.gfx;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import net.minecraft.client.Minecraft;

/**
 * Values for options that are ours (not stored by Minecraft or another mod), in config/prideframes-graphics.properties.
 * Minecraft's own settings are saved by Minecraft (options.txt).
 */
public final class GSettings {
    private GSettings() {}

    private static final Properties P = new Properties();
    private static File file;
    private static boolean dirty;

    private static File file() {
        if (file == null) {
            file = new File(Minecraft.getMinecraft().mcDataDir, "config/prideframes-graphics.properties");
            try (FileReader r = new FileReader(file)) { P.load(r); } catch (Exception ignored) {}
        }
        return file;
    }

    public static int get(String key, int def) {
        file();
        try { return Integer.parseInt(P.getProperty(key, String.valueOf(def)).trim()); } catch (Exception e) { return def; }
    }

    public static void set(String key, int v) {
        file();
        P.setProperty(key, String.valueOf(v));
        dirty = true;
    }

    public static boolean on(String key, boolean def) { return get(key, def ? 1 : 0) != 0; }

    public static void save() {
        if (!dirty) {
            try { Minecraft.getMinecraft().gameSettings.saveOptions(); } catch (Throwable ignored) {}
            return;
        }
        dirty = false;
        try (FileWriter w = new FileWriter(file())) { P.store(w, "Pride Graphics (Pride Frames)"); } catch (Exception e) {
            System.out.println("[PrideGraphics] could not save: " + e);
        }
        try { Minecraft.getMinecraft().gameSettings.saveOptions(); } catch (Throwable ignored) {}
    }
}
