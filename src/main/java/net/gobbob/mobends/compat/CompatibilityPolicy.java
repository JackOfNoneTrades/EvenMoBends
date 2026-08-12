package net.gobbob.mobends.compat;

import cpw.mods.fml.common.Loader;
import net.gobbob.mobends.util.BendsLogger;

/** Centralizes ownership decisions for other player-rendering mods. */
public final class CompatibilityPolicy {
    private static final boolean SMART_MOVING_LOADED = Loader.isModLoaded("SmartMoving");
    private static final boolean SMART_RENDER_LOADED = Loader.isModLoaded("SmartRender");
    private static final boolean WAWEL_AUTH_LOADED = Loader.isModLoaded("wawelauth");
    private static final boolean MOB_ONLY_MODE = SMART_MOVING_LOADED || SMART_RENDER_LOADED;

    private static boolean modeLogged;

    private CompatibilityPolicy() {}

    public static boolean arePlayerAnimationsEnabled() {
        return !MOB_ONLY_MODE;
    }

    public static boolean isMobOnlyMode() {
        return MOB_ONLY_MODE;
    }

    public static boolean isWawelAuthLoaded() {
        return WAWEL_AUTH_LOADED;
    }

    public static void logActiveMode() {
        if (modeLogged) {
            return;
        }
        modeLogged = true;

        if (MOB_ONLY_MODE) {
            String owner = SMART_MOVING_LOADED ? "Smart Moving" : "Smart Render";
            BendsLogger.log(
                owner + " detected: disabling all Mo' Bends player rendering and keeping mob animations enabled.",
                BendsLogger.INFO);
        }
    }
}
