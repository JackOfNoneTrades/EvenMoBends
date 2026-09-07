package net.gobbob.mobends.compat;

import java.lang.reflect.Method;

import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Translates vanilla/Backhand arm flags and Just A Shield's passive blocking into per-hand poses. */
public final class PlayerBlockingCompat {
    public static final int MAIN_HAND = 1;
    public static final int OFF_HAND = 2;

    private static boolean shieldApiResolved;
    private static boolean failureLogged;
    private static Method getShieldInUse;

    private PlayerBlockingCompat() {}

    public static int getBlockingHands(EntityPlayer player, ModelBiped model) {
        return getBlockingHands(player.getHeldItem(), getShieldInUse(player), model.heldItemRight, model.heldItemLeft);
    }

    static int getBlockingHands(ItemStack mainHand, ItemStack shield, int rightArmState, int leftArmState) {
        // Backhand and Just A Shield finalize these flags during RenderLivingEvent.Pre.
        int hands = (rightArmState == 3 ? MAIN_HAND : 0) | (leftArmState == 3 ? OFF_HAND : 0);
        if (shield != null) {
            // ShieldUtil returns either the main-hand shield or Backhand's offhand shield.
            // Passive blocking while the other hand is in use does not always set heldItemLeft=3.
            hands |= shield == mainHand ? MAIN_HAND : OFF_HAND;
        }
        return hands;
    }

    private static ItemStack getShieldInUse(EntityPlayer player) {
        if (!CompatibilityPolicy.isJustAShieldLoaded()) {
            return null;
        }
        try {
            if (!shieldApiResolved) {
                shieldApiResolved = true;
                getShieldInUse = Class.forName("invalid.myask.undertow.util.ShieldUtil")
                    .getMethod("getShieldInUse", EntityPlayer.class);
            }
            return getShieldInUse == null ? null : (ItemStack)getShieldInUse.invoke(null, player);
        } catch (ReflectiveOperationException | LinkageError e) {
            if (!failureLogged) {
                failureLogged = true;
                BendsLogger.log("Could not read Just A Shield blocking state: " + e, BendsLogger.ERROR);
            }
            return null;
        }
    }
}
