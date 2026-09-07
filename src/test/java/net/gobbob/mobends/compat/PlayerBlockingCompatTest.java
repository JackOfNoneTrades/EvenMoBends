package net.gobbob.mobends.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Test;

class PlayerBlockingCompatTest {
    @Test
    void resolvesFinalVanillaAndBackhandFlagsIndependently() {
        assertEquals(0, PlayerBlockingCompat.getBlockingHands(null, null, 0, 1));
        assertEquals(PlayerBlockingCompat.MAIN_HAND, PlayerBlockingCompat.getBlockingHands(null, null, 3, 1));
        assertEquals(PlayerBlockingCompat.OFF_HAND, PlayerBlockingCompat.getBlockingHands(null, null, 1, 3));
        assertEquals(3, PlayerBlockingCompat.getBlockingHands(null, null, 3, 3));
    }

    @Test
    void passiveOffhandShieldBlocksEvenWhenBackhandOnlyMarksItHeld() {
        ItemStack sword = new ItemStack(new Item());
        ItemStack shield = new ItemStack(new Item());
        assertEquals(PlayerBlockingCompat.OFF_HAND, PlayerBlockingCompat.getBlockingHands(sword, shield, 0, 1));
        assertEquals(PlayerBlockingCompat.OFF_HAND, PlayerBlockingCompat.getBlockingHands(null, shield, 0, 1));
    }

    @Test
    void mainHandShieldDoesNotRaiseTheOtherArm() {
        ItemStack shield = new ItemStack(new Item());
        assertEquals(PlayerBlockingCompat.MAIN_HAND, PlayerBlockingCompat.getBlockingHands(shield, shield, 1, 0));
    }

    @Test
    void retainsSwordBlockingWhenShieldModHasNotSuppressedIt() {
        ItemStack sword = new ItemStack(new Item());
        ItemStack shield = new ItemStack(new Item());
        assertEquals(3, PlayerBlockingCompat.getBlockingHands(sword, shield, 3, 1));
    }

    @Test
    void releasingBlockOrRemovingTheShieldDoesNotRetainState() {
        ItemStack sword = new ItemStack(new Item());
        ItemStack shield = new ItemStack(new Item());
        assertEquals(2, PlayerBlockingCompat.getBlockingHands(sword, shield, 0, 3));
        assertEquals(0, PlayerBlockingCompat.getBlockingHands(sword, null, 1, 0));
    }
}
