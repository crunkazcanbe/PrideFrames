package com.dogpound.frames.gfx;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** What makes the Pride Graphics options actually do something (events), plus the menu buttons. */
public class GHooks {
    static final int BUTTON_ID = 0x5047;   // "PG"

    /** particle groups she can switch off one by one */
    public enum PType {
        SMOKE("Smoke", EnumParticleTypes.SMOKE_NORMAL, EnumParticleTypes.SMOKE_LARGE),
        FLAME("Flames", EnumParticleTypes.FLAME),
        WATER("Water Splashes & Bubbles", EnumParticleTypes.WATER_SPLASH, EnumParticleTypes.WATER_BUBBLE, EnumParticleTypes.WATER_WAKE, EnumParticleTypes.WATER_DROP),
        LAVA("Lava Pops", EnumParticleTypes.LAVA),
        DRIP("Dripping", EnumParticleTypes.DRIP_WATER, EnumParticleTypes.DRIP_LAVA),
        EXPLOSION("Explosions", EnumParticleTypes.EXPLOSION_NORMAL, EnumParticleTypes.EXPLOSION_LARGE, EnumParticleTypes.EXPLOSION_HUGE),
        PORTAL("Portal", EnumParticleTypes.PORTAL),
        REDSTONE("Redstone Dust", EnumParticleTypes.REDSTONE),
        ENCHANT("Enchanting", EnumParticleTypes.ENCHANTMENT_TABLE),
        CRIT("Critical Hits", EnumParticleTypes.CRIT, EnumParticleTypes.CRIT_MAGIC),
        DAMAGE("Damage Hearts", EnumParticleTypes.DAMAGE_INDICATOR),
        HEART("Love Hearts", EnumParticleTypes.HEART),
        VILLAGER("Villager", EnumParticleTypes.VILLAGER_HAPPY, EnumParticleTypes.VILLAGER_ANGRY),
        POTION("Potion Swirls", EnumParticleTypes.SPELL, EnumParticleTypes.SPELL_INSTANT, EnumParticleTypes.SPELL_MOB, EnumParticleTypes.SPELL_MOB_AMBIENT, EnumParticleTypes.SPELL_WITCH),
        DUST("Block Dust", EnumParticleTypes.BLOCK_DUST, EnumParticleTypes.FALLING_DUST),
        NOTE("Music Notes", EnumParticleTypes.NOTE),
        SNOW("Snow", EnumParticleTypes.SNOWBALL, EnumParticleTypes.SNOW_SHOVEL);

        public final String label;
        final EnumParticleTypes[] types;
        PType(String label, EnumParticleTypes... types) { this.label = label; this.types = types; }
    }

    private static final boolean[] BLOCKED = new boolean[256];
    private static long blockedAt;

    /** called by the particle mixin for every vanilla-style particle */
    public static boolean particleBlocked(int id) {
        long now = System.currentTimeMillis();
        if (now - blockedAt > 500) {                      // re-read the toggles twice a second, not per particle
            blockedAt = now;
            java.util.Arrays.fill(BLOCKED, false);
            for (PType t : PType.values()) if (!GSettings.on("p." + t.name(), true))
                for (EnumParticleTypes e : t.types) if (e.getParticleID() < BLOCKED.length) BLOCKED[e.getParticleID()] = true;
        }
        return id >= 0 && id < BLOCKED.length && BLOCKED[id];
    }

    public static boolean blockBreakBlocked() { return !GSettings.on("p.blockBreak", true); }

    // ------------------------------------------------------------------ menu buttons: Options + Esc menu

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post e) {
        String n = e.getGui().getClass().getSimpleName();
        if (!(e.getGui() instanceof GuiOptions) && !(e.getGui() instanceof GuiIngameMenu) && !n.equals("DPOptions")) return;
        int w = e.getGui().width;
        e.getButtonList().add(new GuiButton(BUTTON_ID, w / 2 - 100, e.getGui() instanceof GuiIngameMenu ? 6 : e.getGui().height - 52, 200, 20, "✦ Pride Graphics"));
    }

    @SubscribeEvent
    public void click(GuiScreenEvent.ActionPerformedEvent.Post e) {
        if (e.getButton().id == BUTTON_ID) Minecraft.getMinecraft().displayGuiScreen(new GraphicsScreen(e.getGui()));
    }

    // ------------------------------------------------------------------ fog + clear water

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fog(EntityViewRenderEvent.RenderFogEvent e) {
        Entity ent = e.getEntity();
        if (ent != null && ent.isInsideOfMaterial(Material.WATER)) return;     // underwater fog is handled below
        if (ent != null && ent.isInsideOfMaterial(Material.LAVA)) return;
        float far = e.getFarPlaneDistance();
        if (!GSettings.on("fog", true)) {
            GlStateManager.setFogStart(far * 8F);
            GlStateManager.setFogEnd(far * 10F);
        } else if (e.getFogMode() >= 0) {
            GlStateManager.setFogStart(far * GSettings.get("fogStart", 75) / 100F);
        }
    }

    @SubscribeEvent
    public void waterFog(EntityViewRenderEvent.FogDensity e) {
        if (!GSettings.on("clearWater", false) || e.getEntity() == null || !e.getEntity().isInsideOfMaterial(Material.WATER)) return;
        GlStateManager.setFog(GlStateManager.FogMode.EXP);
        e.setDensity(0.012F);                              // vanilla underwater is ~0.1: about 8x clearer
        e.setCanceled(true);
    }

    // ------------------------------------------------------------------ anisotropic filtering + entity distance

    @SubscribeEvent
    public void stitched(TextureStitchEvent.Post e) { applyAniso(); }

    private boolean firstJoin = true;

    @SubscribeEvent
    public void joined(EntityJoinWorldEvent e) {
        if (!firstJoin || !e.getWorld().isRemote || e.getEntity() != Minecraft.getMinecraft().player) return;
        firstJoin = false;
        applyAniso();
        Entity.setRenderDistanceWeight(GSettings.get("entityDist", 100) / 100.0);
    }

    public static void applyAniso() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getTextureMapBlocks() == null) return;
            int level = 1 << GSettings.get("aniso", 0);    // 1, 2, 4, 8, 16
            float max = GL11.glGetFloat(0x84FF);           // GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
            GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 0x84FE, Math.max(1F, Math.min(max > 0 ? max : 16F, level)));   // GL_TEXTURE_MAX_ANISOTROPY_EXT
        } catch (Throwable t) {
            System.out.println("[PrideGraphics] anisotropic filtering not available: " + t);
        }
    }
}
