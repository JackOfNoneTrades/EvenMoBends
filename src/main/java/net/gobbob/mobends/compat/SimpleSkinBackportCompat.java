package net.gobbob.mobends.compat;

import java.lang.reflect.Method;

import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;

/** Optional SimpleSkinBackport integration for the separate, vanilla first-person arm model. */
public final class SimpleSkinBackportCompat {
    private static boolean resolved;
    private static boolean failureLogged;
    private static SkinApi api;

    private SimpleSkinBackportCompat() {}

    public static boolean prepareFirstPersonModel(ModelBiped model, EntityPlayer player) {
        if (!CompatibilityPolicy.isSimpleSkinBackportLoaded()) {
            return false;
        }

        try {
            if (!resolved) {
                resolved = true;
                api = new SkinApi(
                    Class.forName("roadhog360.simpleskinbackport.ducks.INewBipedModel"),
                    Class.forName("roadhog360.simpleskinbackport.ducks.IArmsState"));
            }
            return api != null && api.prepare(model, player);
        } catch (ReflectiveOperationException | LinkageError e) {
            if (!failureLogged) {
                failureLogged = true;
                BendsLogger.log("Could not prepare SimpleSkinBackport first-person skin: " + e, BendsLogger.ERROR);
            }
            return false;
        }
    }

    /** Resolves only duck methods; no Minecraft member names that need obfuscation mappings. */
    static final class SkinApi {
        private final Method set64x;
        private final Method setSlim;
        private final Method isSlim;

        SkinApi(Class<?> modelType, Class<?> armsStateType) throws NoSuchMethodException {
            this.set64x = modelType.getMethod("ssb$set64x");
            this.setSlim = modelType.getMethod("ssb$setSlim", Boolean.TYPE);
            this.isSlim = armsStateType.getMethod("ssb$isSlim");
        }

        boolean prepare(Object model, Object player) throws ReflectiveOperationException {
            if (!this.set64x.getDeclaringClass().isInstance(model)
                || !this.isSlim.getDeclaringClass().isInstance(player)) {
                return false;
            }

            boolean slim = Boolean.TRUE.equals(this.isSlim.invoke(player));
            this.set64x.invoke(model);
            this.setSlim.invoke(model, slim);
            return true;
        }
    }
}
