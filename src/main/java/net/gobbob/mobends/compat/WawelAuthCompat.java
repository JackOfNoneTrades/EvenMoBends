package net.gobbob.mobends.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;

/** Optional Wawel Auth integration with its API-bound model adapter loaded only when the mod is present. */
public final class WawelAuthCompat {
    private static final String SKIN_MODEL_HELPER =
        "org.fentanylsolutions.wawelauth.client.render.SkinModelHelper";
    private static final String SKIN_LAYERS_CONFIG =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DConfig";
    private static final String SKIN_LAYERS_HELPER =
        "org.fentanylsolutions.wawelauth.api.SkinLayersHelper";
    private static final String MODEL_PARTS = SKIN_LAYERS_HELPER + "$EnumPlayerModelParts";
    private static final String MODEL_ADAPTER =
        "net.gobbob.mobends.compat.wawelauth.WawelAuthModelBendsPlayer";

    private static boolean helperResolved;
    private static Method getSkinModel;
    private static boolean configResolved;
    private static Field modernSkinSupport;
    private static boolean skinLayersHelperResolved;
    private static Method isSkinLayerHidden;
    private static Object rightSleeve;

    private WawelAuthCompat() {}

    public static boolean isLoaded() {
        return CompatibilityPolicy.isWawelAuthLoaded();
    }

    /**
     * Creates the model adapter only when Wawel Auth is present, keeping its API types out of the normal player model.
     */
    public static ModelBendsPlayer createPlayerModel(float scale) {
        if (isLoaded()) {
            try {
                Class<?> adapter = Class.forName(MODEL_ADAPTER);
                Constructor<?> constructor = adapter.getConstructor(Float.TYPE);
                return (ModelBendsPlayer)constructor.newInstance(Float.valueOf(scale));
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Older Wawel Auth versions continue to use the legacy no-argument model methods below.
            }
        }
        return new ModelBendsPlayer(scale);
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

        resolveSkinLayersHelper();
        if (isSkinLayerHidden != null && rightSleeve != null) {
            try {
                return Boolean.TRUE.equals(isSkinLayerHidden.invoke(null, player, rightSleeve));
            } catch (ReflectiveOperationException ignored) {
                // Fall through to Wawel Auth 1.0.5's player duck method.
            }
        }

        try {
            Method method = player.getClass().getMethod("wawelAuth$getHideRightSleeve");
            return Boolean.TRUE.equals(method.invoke(player));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static synchronized void resolveSkinLayersHelper() {
        if (skinLayersHelperResolved) {
            return;
        }
        skinLayersHelperResolved = true;

        try {
            Class<?> helper = Class.forName(SKIN_LAYERS_HELPER);
            Class<?> parts = Class.forName(MODEL_PARTS);
            isSkinLayerHidden = helper.getMethod("isSkinLayerHidden", EntityPlayer.class, parts);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object resolvedRightSleeve = Enum.valueOf((Class<? extends Enum>)parts.asSubclass(Enum.class), "RIGHT_SLEEVE");
            rightSleeve = resolvedRightSleeve;
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            isSkinLayerHidden = null;
            rightSleeve = null;
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
