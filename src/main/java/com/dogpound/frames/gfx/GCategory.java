package com.dogpound.frames.gfx;

/** The Pride Graphics tabs, in her order (GRAPHICS-SPEC.md). */
public enum GCategory {
    GENERAL("General", "⚙"), TERRAIN("Terrain", "▦"), VEGETATION("Vegetation", "✿"), WATER("Water", "≈"),
    FIRE("Fire & Lava", "🔥"), LIGHTING("Lighting", "☀"), SHADOWS("Shadows", "◐"), SKY("Sky & Clouds", "☁"),
    WEATHER("Weather", "☂"), PARTICLES("Particles", "✦"), ENTITIES("Entities", "☺"), ANIMATIONS("Animations", "↻"),
    FOG("Fog", "░"), POST("Post Processing", "✧"), AA("Anti-Aliasing", "◇"), RESOLUTION("Resolution", "▣"),
    CHUNKS("Chunk Rendering", "▤"), DISTANT("Distant Horizons", "⛰"), PERFORMANCE("Performance", "⚡"),
    EXPERIMENTAL("Experimental", "⚗"), PROFILES("Profiles", "★"), BENCHMARK("Benchmark", "⏱");

    public final String title, icon;
    GCategory(String title, String icon) { this.title = title; this.icon = icon; }
}
