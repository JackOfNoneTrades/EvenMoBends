/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Test vectors use player-skin blink data authored for Entity Texture Features:
 * https://github.com/Traben-0/Entity_Texture_Features
 */
package net.gobbob.mobends.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class BlinkingTexturesTest {
    @Test
    void acceptsEtfMarkerAndBlinkChoices() throws Exception {
        assertEtfSkin("blink-option1.png", 3, 5);
        assertEtfSkin("blink-option2.png", 4, 4);
        assertEtfSkin("blink-option3.png", 5, 3);
        assertEtfSkin("chicken.png", 2, 0);
        assertEtfSkin("slime.png", 2, 0);
    }

    @Test
    void rejectsAnUnmarkedSkin() throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        assertFalse(hasFeatureMarker(image));
    }

    @Test
    void wipesIntermediateEyelidFramesAtSubPixelResolution() throws Exception {
        BufferedImage open = new BufferedImage(1, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage closed = new BufferedImage(1, 2, BufferedImage.TYPE_INT_ARGB);
        open.setRGB(0, 0, 0xff101010);
        open.setRGB(0, 1, 0xff202020);
        closed.setRGB(0, 0, 0xffa0a0a0);
        closed.setRGB(0, 1, 0xffb0b0b0);

        Method method = BlinkingTextures.class.getDeclaredMethod(
            "wipe", BufferedImage.class, BufferedImage.class, Float.TYPE);
        method.setAccessible(true);
        BufferedImage half = (BufferedImage)method.invoke(null, open, closed, Float.valueOf(0.5f));
        assertEquals(4, half.getWidth());
        assertEquals(8, half.getHeight());
        assertEquals(0xffa0a0a0, half.getRGB(0, 3));
        assertEquals(0xff202020, half.getRGB(0, 4));
    }

    @Test
    void smoothlyWipesAOnePixelTallMobEye() throws Exception {
        BufferedImage open = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        BufferedImage closed = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        open.setRGB(0, 0, 0xff101010);
        closed.setRGB(0, 0, 0xffa0a0a0);

        Method method = BlinkingTextures.class.getDeclaredMethod(
            "wipe", BufferedImage.class, BufferedImage.class, Float.TYPE);
        method.setAccessible(true);
        BufferedImage quarter = (BufferedImage)method.invoke(null, open, closed, Float.valueOf(0.25f));
        assertEquals(4, quarter.getHeight());
        assertEquals(0xffa0a0a0, quarter.getRGB(0, 0));
        assertEquals(0xff101010, quarter.getRGB(0, 1));
    }

    private static void assertEtfSkin(String name, int blinkType, int eyeLine) throws Exception {
        BufferedImage image = ImageIO.read(
            BlinkingTexturesTest.class.getResourceAsStream("/etf-blink-skins/" + name));
        assertTrue(hasFeatureMarker(image), name);
        assertEquals(blinkType, colorNumber(image.getRGB(52, 16)), name);
        assertEquals(eyeLine, colorNumber(image.getRGB(52, 19)), name);
    }

    private static boolean hasFeatureMarker(BufferedImage image) throws Exception {
        Method method = BlinkingTextures.class.getDeclaredMethod("hasFeatureMarker", BufferedImage.class);
        method.setAccessible(true);
        return ((Boolean)method.invoke(null, image)).booleanValue();
    }

    private static int colorNumber(int color) throws Exception {
        Method method = BlinkingTextures.class.getDeclaredMethod("colorNumber", Integer.TYPE);
        method.setAccessible(true);
        return ((Integer)method.invoke(null, Integer.valueOf(color))).intValue();
    }
}
