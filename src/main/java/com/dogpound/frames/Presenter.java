package com.dogpound.frames;

import java.lang.reflect.Method;

/**
 * Frame hand-off point (every Minecraft.updateDisplay). Stage M0: pass-through - it only proves the hook is in place
 * and does exactly what Display.update() did. The presenter thread arrives in M1 (see DESIGN.md: contexts must be
 * split at window creation, before any framebuffer exists, because FBOs are not shared between GL contexts).
 */
public final class Presenter {
    private Presenter() {}

    private static Method update;
    private static boolean logged;

    public static void frameDone() {
        try {
            if (update == null) {
                Class<?> d;
                try { d = Class.forName("org.lwjglx.opengl.Display"); } catch (ClassNotFoundException e) { d = Class.forName("org.lwjgl.opengl.Display"); }
                update = d.getMethod("update");
                if (!logged) { logged = true; System.out.println("[PrideFrames] frame hook active via " + d.getName()); }
            }
            update.invoke(null);
        } catch (Throwable t) {
            throw new RuntimeException("[PrideFrames] Display.update failed", t);
        }
    }
}
