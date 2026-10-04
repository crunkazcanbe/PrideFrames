package com.dogpound.frames;

import java.util.Collections;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

/**
 * Pride Frames' patches load LATE (after mods are constructed). As an early coremod config they made Mixin's config
 * list change while it was being read during mod loading: ConcurrentModificationException at startup, twice in a row
 * (2026-10-03). RenderGlobal / ParticleManager are created after this point anyway.
 */
public class FramesLateMixins implements ILateMixinLoader {
    @Override public List<String> getMixinConfigs() { return Collections.singletonList("prideframes.mixins.json"); }
}
