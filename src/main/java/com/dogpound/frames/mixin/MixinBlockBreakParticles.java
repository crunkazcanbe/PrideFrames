package com.dogpound.frames.mixin;

import com.dogpound.frames.gfx.GHooks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pride Graphics "Block Breaking" particles switch. */
@Mixin(value = ParticleManager.class, remap = false)
public abstract class MixinBlockBreakParticles {
    @Inject(method = "func_180533_a", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$destroy(BlockPos pos, IBlockState state, CallbackInfo ci) { if (GHooks.blockBreakBlocked()) ci.cancel(); }

    @Inject(method = "func_180532_a", at = @At("HEAD"), cancellable = true, require = 0)
    private void pride$hit(BlockPos pos, EnumFacing side, CallbackInfo ci) { if (GHooks.blockBreakBlocked()) ci.cancel(); }
}
