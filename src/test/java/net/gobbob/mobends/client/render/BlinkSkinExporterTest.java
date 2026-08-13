package net.gobbob.mobends.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class BlinkSkinExporterTest {
    @Test
    void embedsClosedFaceWithoutChangingVisibleSkin() {
        BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                skin.setRGB(8 + x, 8 + y, 0xff100000 | x << 8 | y);
                skin.setRGB(40 + x, 8 + y, 0x00000000);
            }
        }
        skin.setRGB(9, 11, 0xff0000ff);
        byte[][] eyes = new byte[8][8];
        boolean[][] pupils = new boolean[8][8];
        int[][] colors = new int[8][8];
        int[][] backgrounds = new int[8][8];
        byte[][] eyebrowGroups = new byte[8][8];
        int[][] eyebrowColors = new int[8][8];
        eyes[1][3] = 1;
        eyes[6][3] = 2;
        pupils[1][3] = true;
        colors[1][3] = 0xffc08060;
        colors[6][3] = 0xffc08060;
        backgrounds[1][3] = 0xffabcdef;
        eyebrowGroups[2][2] = 1;
        eyebrowColors[2][2] = 0xff543210;

        BufferedImage exported = BlinkSkinExporter.export(
            skin, eyes, pupils, colors, backgrounds, eyebrowGroups, eyebrowColors);

        assertEquals(0xff0000ff, skin.getRGB(9, 11));
        assertEquals(0xff0000ff, exported.getRGB(9, 11));
        assertEquals(0xffc08060, exported.getRGB(1, 3));
        assertEquals(0x00000000, exported.getRGB(33, 3));
        assertEquals(0xff420808, exported.getRGB(25, 0));
        assertEquals(0xff420043, exported.getRGB(30, 1));
        assertEquals(0xffabcdef, exported.getRGB(57, 3));
        assertEquals(0xff543210, exported.getRGB(58, 18));
        assertEquals(0xff420400, exported.getRGB(58, 24));
        assertEquals(skin.getRGB(10, 12), exported.getRGB(2, 4));
        assertTrue(BlinkSkinExporter.hasBlinkData(exported));
        BlinkSkinExporter.EditorData editable = BlinkSkinExporter.readEditorData(exported);
        assertTrue(editable.eyes[1][3]);
        assertTrue(editable.eyes[6][3]);
        assertTrue(editable.pupils[1][3]);
        assertEquals(0xff543210, editable.eyebrowColors[2][2]);
    }
}
