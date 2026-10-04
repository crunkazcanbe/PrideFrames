package com.dogpound.frames.mixin;

import com.dogpound.frames.Presenter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft.updateDisplay() = Display.update() (swap + input). While frame generation runs, the presenter thread owns
 * the window and does the swapping; the game thread only hands over its finished frame and processes input.
 * Two targets because Cleanroom relocates LWJGL2's Display to lwjglx (only one of them matches at runtime).
 */
@Mixin(value = Minecraft.class, remap = false)
public abstract class MixinMinecraftPresent {
    @Redirect(method = "func_175601_h", require = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;update()V"))
    private void pride$update() { Presenter.frameDone(); }

    @Redirect(method = "func_175601_h", require = 0, at = @At(value = "INVOKE", target = "Lorg/lwjglx/opengl/Display;update()V"))
    private void pride$updateX() { Presenter.frameDone(); }
}
