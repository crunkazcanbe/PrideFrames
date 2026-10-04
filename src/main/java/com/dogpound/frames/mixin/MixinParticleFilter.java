package com.dogpound.frames.mixin;

import com.dogpound.frames.gfx.GHooks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pride Graphics per-type particle switches: every world.spawnParticle(...) ends up here (RenderGlobal.spawnParticle0). */
@Mixin(value = RenderGlobal.class, remap = false)
public abstract class MixinParticleFilter {
    @Inject(method = "func_190571_b", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$filter(int id, boolean ignoreRange, boolean minParticles, double x, double y, double z,
                              double dx, double dy, double dz, int[] params, CallbackInfoReturnable<Particle> cir) {
        if (GHooks.particleBlocked(id)) cir.setReturnValue(null);
    }
}
