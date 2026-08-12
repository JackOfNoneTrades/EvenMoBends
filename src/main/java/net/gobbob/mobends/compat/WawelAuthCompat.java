package net.gobbob.mobends.compat;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;

/** Optional WawelAuth calls kept behind reflection so this fork has no hard dependency. */
public final class WawelAuthCompat {
    private static final String SKIN_MODEL_HELPER =
        "org.fentanylsolutions.wawelauth.client.render.SkinModelHelper";
    private static final String SKIN_LAYERS_CONFIG =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DConfig";

    private static boolean helperResolved;
    private static Method getSkinModel;
    private static boolean configResolved;
    private static Field modernSkinSupport;

    private WawelAuthCompat() {}

    public static boolean isLoaded() {
        return CompatibilityPolicy.isWawelAuthLoaded();
    }

    public static boolean isSlim(AbstractClientPlayer player) {
        if (!isLoaded() || player == null) {
            return false;
        }

        resolveSkinModelHelper();
        if (getSkinModel == null) {
            return false;
        }

        try {
            Object model = getSkinModel.invoke(null, player);
            return model instanceof Enum && ((Enum<?>)model).name().equals("SLIM");
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static boolean isModernSkinSupportEnabled() {
        if (!isLoaded()) {
            return false;
        }

        resolveSkinLayersConfig();
        if (modernSkinSupport == null) {
            return false;
        }

        try {
            return modernSkinSupport.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    public static boolean isRightSleeveHidden(EntityPlayer player) {
        if (!isLoaded() || player == null) {
            return false;
        }

        try {
            Method method = player.getClass().getMethod("wawelAuth$getHideRightSleeve");
            return Boolean.TRUE.equals(method.invoke(player));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static synchronized void resolveSkinModelHelper() {
        if (helperResolved) {
            return;
        }
        helperResolved = true;

        try {
            Class<?> helper = Class.forName(SKIN_MODEL_HELPER);
            getSkinModel = helper.getMethod("getSkinModel", AbstractClientPlayer.class);
        } catch (ReflectiveOperationException ignored) {
            getSkinModel = null;
        }
    }

    private static synchronized void resolveSkinLayersConfig() {
        if (configResolved) {
            return;
        }
        configResolved = true;

        try {
            Class<?> config = Class.forName(SKIN_LAYERS_CONFIG);
            modernSkinSupport = config.getField("modernSkinSupport");
        } catch (ReflectiveOperationException ignored) {
            modernSkinSupport = null;
        }
    }
}
