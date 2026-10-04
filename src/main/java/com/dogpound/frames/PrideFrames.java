package com.dogpound.frames;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = PrideFrames.MODID, name = "Pride Frames", version = "0.1.0", clientSideOnly = true, acceptableRemoteVersions = "*")
public class PrideFrames {
    public static final String MODID = "prideframes";

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new com.dogpound.frames.gfx.GHooks());   // Pride Graphics options + menu buttons
    }

    @Config(modid = MODID, name = "prideframes")
    public static class Settings {
        @Config.Name("Frame Generation")
        @Config.Comment("1 = off (normal). 2, 3 or 4 = that many frames on screen for every frame the game renders. 0 = match your monitor's refresh rate.")
        @Config.RangeInt(min = 0, max = 4)
        public static int multiplier = 1;
    }
}
