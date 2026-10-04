package com.dogpound.frames.gfx;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;

/** Reads/writes Weather2 Remastered's ConfigStorm.cfg (cloud layer heights + toggles); Weather2 reads it at startup. */
final class W2 {
    private W2() {}

    private static File file() { return new File(Minecraft.getMinecraft().mcDataDir, "config/weather2remaster/ConfigStorm.cfg"); }

    static int get(String key, int def) {
        try {
            for (String l : Files.readAllLines(file().toPath(), StandardCharsets.UTF_8)) {
                Matcher m = Pattern.compile("^\\s*[IB]:\"" + Pattern.quote(key) + "\"=(\\S+)").matcher(l);
                if (m.find()) { String v = m.group(1); return v.equals("true") ? 1 : v.equals("false") ? 0 : Integer.parseInt(v); }
            }
        } catch (Exception ignored) {}
        return def;
    }

    static void set(String key, int v) {
        try {
            List<String> lines = Files.readAllLines(file().toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                Matcher m = Pattern.compile("^(\\s*)([IB]):\"" + Pattern.quote(key) + "\"=").matcher(l);
                if (m.find()) lines.set(i, m.group(1) + m.group(2) + ":\"" + key + "\"=" + (m.group(2).equals("B") ? (v != 0 ? "true" : "false") : String.valueOf(v)));
            }
            Files.write(file().toPath(), lines, StandardCharsets.UTF_8);
        } catch (Exception e) { System.out.println("[PrideGraphics] Weather2 config: " + e); }
    }
}
