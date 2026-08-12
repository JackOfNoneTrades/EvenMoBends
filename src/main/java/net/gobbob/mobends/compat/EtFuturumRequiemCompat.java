package net.gobbob.mobends.compat;

import ganymedes01.etfuturum.api.elytra.IElytraPlayer;
import net.minecraft.entity.player.EntityPlayer;

/** Optional Et Futurum Requiem Elytra state access, isolated from normal class loading. */
public final class EtFuturumRequiemCompat {
    private EtFuturumRequiemCompat() {}

    public static boolean isElytraFlying(EntityPlayer player) {
        return CompatibilityPolicy.isEtFuturumRequiemLoaded()
            && Api.isElytraFlying(player);
    }

    /** This class is only loaded after FML confirms that Et Futurum Requiem is present. */
    private static final class Api {
        private Api() {}

        private static boolean isElytraFlying(EntityPlayer player) {
            return player instanceof IElytraPlayer
                && ((IElytraPlayer)player).etfu$isElytraFlying();
        }
    }
}
