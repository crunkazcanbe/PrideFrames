package com.dogpound.frames.gfx;

import static com.dogpound.frames.gfx.GCategory.*;

import java.util.ArrayList;
import java.util.List;

import com.dogpound.frames.PrideFrames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;

/**
 * Every Pride Graphics option, in her spec's order (GRAPHICS-SPEC.md). Wave 1 (2026-10-03) = the ones that work now;
 * the rest are listed as planned so she can see the whole plan fill in.
 */
public final class GOptions {
    private GOptions() {}

    public static final List<GOption> ALL = new ArrayList<GOption>();

    private static GameSettings gs() { return Minecraft.getMinecraft().gameSettings; }
    private static void reloadChunks() { Minecraft mc = Minecraft.getMinecraft(); if (mc.renderGlobal != null) mc.renderGlobal.loadRenderers(); }
    private static GOption add(GOption o) { ALL.add(o); return o; }
    private static void plan(GCategory c, String... names) { for (String n : names) add(GOption.planned(c, n, "Planned for Pride Graphics - not built yet.")); }

    public static List<GOption> of(GCategory c) {
        if (ALL.isEmpty()) build();
        List<GOption> out = new ArrayList<GOption>();
        for (GOption o : ALL) if (o.cat == c) out.add(o);
        return out;
    }

    public static synchronized void build() {
        if (!ALL.isEmpty()) return;
        // ---------------------------------------------------------------- General
        add(GOption.slider(GENERAL, "renderDistance", "Render Distance", "How far real chunks are drawn (Distant Horizons draws the land past this).",
                2, 32, 1, " chunks", () -> gs().renderDistanceChunks, v -> { gs().renderDistanceChunks = v; Minecraft.getMinecraft().renderGlobal.setDisplayListEntitiesDirty(); }));
        add(GOption.slider(GENERAL, "maxFps", "Max FPS", "Frame cap. The far right = unlimited.", 10, 260, 10, " fps",
                () -> gs().limitFramerate, v -> gs().limitFramerate = v));
        add(GOption.toggle(GENERAL, "vsync", "VSync", "Match your monitor's refresh (no tearing, adds a little delay).",
                () -> gs().enableVsync ? 1 : 0, v -> { gs().enableVsync = v != 0; org.lwjgl.opengl.Display.setVSyncEnabled(v != 0); }));
        add(GOption.slider(GENERAL, "fov", "Field of View", "How wide you see.", 30, 110, 1, "°",
                () -> (int) gs().fovSetting, v -> gs().fovSetting = v));
        add(GOption.slider(GENERAL, "brightness", "Brightness", "Moody (0) to Bright (100).", 0, 100, 5, "%",
                () -> Math.round(gs().gammaSetting * 100), v -> gs().gammaSetting = v / 100F));
        add(GOption.toggle(GENERAL, "bobbing", "View Bobbing", "The camera bounces as you walk.",
                () -> gs().viewBobbing ? 1 : 0, v -> gs().viewBobbing = v != 0));
        add(GOption.choice(GENERAL, "graphics", "Graphics", "Fast or Fancy leaves, water and clouds.", new String[] {"Fast", "Fancy"},
                () -> gs().fancyGraphics ? 1 : 0, v -> { gs().fancyGraphics = v != 0; reloadChunks(); }));
        add(GOption.choice(GENERAL, "frameGen", "Frame Generation", "Pride Frames: extra smooth frames made from your camera movement. Being built.",
                new String[] {"Match monitor", "1x (off)", "2x", "3x", "4x"},
                () -> PrideFrames.Settings.multiplier, v -> PrideFrames.Settings.multiplier = v).coming());
        plan(GENERAL, "Quality Preset (Fast → Cinematic)", "Weather Distance", "Shadow Distance", "Fullscreen Resolution");

        // ---------------------------------------------------------------- Terrain
        add(GOption.choice(TERRAIN, "smoothLight", "Smooth Lighting", "Soft shading where blocks meet.", new String[] {"Off", "Minimum", "Maximum"},
                () -> gs().ambientOcclusion, v -> { gs().ambientOcclusion = v; reloadChunks(); }));
        add(GOption.slider(TERRAIN, "mipmaps", "Mipmap Levels", "Smoother far-away textures. Applies after a restart (reloading 750 mods' textures live takes minutes).",
                0, 4, 1, "", () -> gs().mipmapLevels, v -> gs().mipmapLevels = v).needsRestart());
        add(GOption.choice(TERRAIN, "aniso", "Anisotropic Filtering", "Keeps textures sharp when you look at them at an angle (the ground far ahead). Works best with mipmaps on.",
                new String[] {"Off", "2x", "4x", "8x", "16x"}, () -> GSettings.get("aniso", 0), v -> { GSettings.set("aniso", v); GHooks.applyAniso(); }));
        plan(TERRAIN, "Connected Textures", "Better Grass", "Better Snow", "Biome Color Smoothing", "Random Block Textures",
                "Face Culling", "Ambient Occlusion Strength", "Block Mesh Optimization (experimental)");

        // ---------------------------------------------------------------- Vegetation
        plan(VEGETATION, "3D Grass & Flowers", "3D Crops", "Better Leaves", "Bushes", "Grass Density", "Random Plant Rotation",
                "Wind / Waving Plants", "Vegetation Quality");

        // ---------------------------------------------------------------- Water
        add(GOption.toggle(WATER, "clearWater", "Clear Water", "See much farther underwater (thinner underwater fog).",
                () -> GSettings.on("clearWater", false) ? 1 : 0, v -> GSettings.set("clearWater", v)));
        plan(WATER, "Water Transparency", "Water Reflections", "Water Waves", "Underwater Caustics", "Rain Ripples", "Waterfall Splash");

        // ---------------------------------------------------------------- Fire & lava
        plan(FIRE, "Fire Glow", "Lava Glow", "Lava Brightness", "Heat Haze", "Ember Particles", "Smoke Density");

        // ---------------------------------------------------------------- Lighting / shadows
        add(GOption.toggle(SHADOWS, "entityShadows", "Entity Shadows", "The round shadow under mobs and players.",
                () -> gs().entityShadows ? 1 : 0, v -> gs().entityShadows = v != 0));
        plan(LIGHTING, "Dynamic Lights (Off/Fast/Fancy/Ultra)", "Dynamic Light Distance", "Torch Flicker", "Bloom", "Light Shafts", "Colored Lighting");
        plan(SHADOWS, "Shadow Quality", "Shadow Distance", "Shadow Softness", "Cloud Shadows", "Shadow Cascades");

        // ---------------------------------------------------------------- Sky
        add(GOption.choice(SKY, "clouds", "Minecraft Clouds", "Minecraft's own flat clouds (Weather2 has its own fluffy ones).", new String[] {"Off", "Fast", "Fancy"},
                () -> gs().clouds, v -> gs().clouds = v));
        add(GOption.toggle(SKY, "sun", "Sun", "Draw the sun.", () -> GSettings.on("sun", true) ? 1 : 0, v -> GSettings.set("sun", v)).coming());
        add(GOption.toggle(SKY, "moon", "Moon", "Draw the moon.", () -> GSettings.on("moon", true) ? 1 : 0, v -> GSettings.set("moon", v)).coming());
        add(GOption.toggle(SKY, "stars", "Stars", "Draw the stars at night.", () -> GSettings.on("stars", true) ? 1 : 0, v -> GSettings.set("stars", v)).coming());
        add(GOption.slider(SKY, "w2Layer0", "Weather2 Cloud Height", "Height of Weather2's main cloud layer. Applies when the game restarts.",
                100, 400, 10, "", () -> W2.get("Cloud Layer 0 Height", 200), v -> W2.set("Cloud Layer 0 Height", v)).needsRestart());
        add(GOption.toggle(SKY, "w2Layer1", "Weather2 Cloud Layer 2", "A second, higher Weather2 cloud layer. Applies when the game restarts.",
                () -> W2.get("Enable Cloud Layer 1", 0), v -> W2.set("Enable Cloud Layer 1", v)).needsRestart());
        add(GOption.toggle(SKY, "w2Layer2", "Weather2 Cloud Layer 3", "A third, highest Weather2 cloud layer. Applies when the game restarts.",
                () -> W2.get("Enable Cloud Layer 2", 0), v -> W2.set("Enable Cloud Layer 2", v)).needsRestart());
        plan(SKY, "Cloud Opacity", "Cloud Speed", "Star Density", "Sunrise/Sunset Quality", "Aurora", "Shooting Stars", "Milky Way", "Dynamic Sky");

        // ---------------------------------------------------------------- Weather
        plan(WEATHER, "Rain Quality", "Rain Density", "Puddles", "Rain Ripples", "Snow Density", "Lightning Brightness");

        // ---------------------------------------------------------------- Particles
        add(GOption.choice(PARTICLES, "particles", "Particles", "How many particles Minecraft makes overall.", new String[] {"All", "Decreased", "Minimal"},
                () -> gs().particleSetting, v -> gs().particleSetting = v));
        for (GHooks.PType t : GHooks.PType.values())
            add(GOption.toggle(PARTICLES, "p." + t.name(), t.label, "Show " + t.label.toLowerCase() + " particles.",
                    () -> GSettings.on("p." + t.name(), true) ? 1 : 0, v -> GSettings.set("p." + t.name(), v)));
        add(GOption.toggle(PARTICLES, "p.blockBreak", "Block Breaking", "Bits flying when you mine a block.",
                () -> GSettings.on("p.blockBreak", true) ? 1 : 0, v -> GSettings.set("p.blockBreak", v)));
        plan(PARTICLES, "Particle Distance", "Particle Size", "Particle Lifetime");

        // ---------------------------------------------------------------- Entities
        add(GOption.slider(ENTITIES, "entityDist", "Entity Distance", "How far mobs, items and players are drawn - separate from terrain. 100% = normal.",
                25, 500, 25, "%", () -> GSettings.get("entityDist", 100), v -> { GSettings.set("entityDist", v); Entity.setRenderDistanceWeight(v / 100.0); }));
        plan(ENTITIES, "Nameplate Distance", "Armor Rendering", "3D Dropped Items", "Item Bobbing", "Entity LOD");

        // ---------------------------------------------------------------- Animations
        plan(ANIMATIONS, "Water Animation", "Lava Animation", "Fire Animation", "Portal Animation", "Animation FPS");

        // ---------------------------------------------------------------- Fog
        add(GOption.toggle(FOG, "fog", "Fog", "The haze at the edge of what you can see.",
                () -> GSettings.on("fog", true) ? 1 : 0, v -> GSettings.set("fog", v)));
        add(GOption.slider(FOG, "fogStart", "Fog Start", "Where the haze begins, as % of your render distance.", 0, 100, 5, "%",
                () -> GSettings.get("fogStart", 75), v -> GSettings.set("fogStart", v)));
        plan(FOG, "Fog Density", "Fog Color", "Height Fog", "Volumetric Fog");

        // ---------------------------------------------------------------- the rest of her plan
        plan(POST, "FXAA", "Sharpen", "Bloom", "Vignette", "Color Grading", "Saturation", "Contrast", "Exposure", "Presets (Cinematic, Vibrant…)", "Motion Blur", "Depth of Field", "God Rays");
        plan(AA, "Anti-Aliasing (FXAA/SMAA/TAA)");
        plan(RESOLUTION, "Render Resolution (50–200%)", "Dynamic Resolution (target FPS)");
        plan(CHUNKS, "Chunk Builder Threads", "Chunk Updates per Frame", "Lazy Chunk Loading", "Chunk Loading Animation");
        plan(DISTANT, "LOD Distance", "Generator (Surface first / Chunks)", "Generation Threads", "Distant Horizons Clouds");
        plan(PERFORMANCE, "Automatic Optimization", "Smart Animations", "Entity Culling");
        plan(EXPERIMENTAL, "Screen-Space Reflections", "SSAO", "Volumetric Clouds", "Parallax Textures");
        plan(PROFILES, "Potato / Low / Medium / High / Ultra / Cinematic / Maximum", "Save My Own Profile");
        plan(BENCHMARK, "Run Graphics Benchmark");
    }
}
