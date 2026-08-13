package net.gobbob.mobends.client.render;

import java.awt.image.BufferedImage;

/** Builds ETF-compatible option-one blink data without changing the visible portions of a skin. */
public final class BlinkSkinExporter {
    private static final int MASK_METADATA_X = 24;
    private static final int MASK_METADATA_Y = 0;
    private static final int MASK_METADATA_MAGIC = 0x42;
    private static final int CONNECTED_MASK_VERSION = 0x43;
    private static final int PUPIL_BACKGROUND_X = 56;
    private static final int EYEBROW_COLOR_X = 56;
    private static final int EYEBROW_COLOR_Y = 16;
    private static final int EYEBROW_MASK_Y = 24;
    private static final int[] FEATURE_MARKER_ABGR = {
        0xff0000ff, 0xff00007f, 0xff0000ff, 0xff00ff00, 0xff007f00, 0xff00ff00,
        0xffff0000, 0xff7f0000, 0xffff0000, 0xffffffff, 0xffffffff, 0xffffffff
    };
    private static final int[] MARKER_X = {1, 0, 0, 2, 3, 3, 0, 0, 1, 3, 2, 3};
    private static final int[] MARKER_Y = {16, 16, 17, 16, 16, 17, 18, 19, 19, 18, 19, 18};

    private BlinkSkinExporter() {}

    public static BufferedImage export(BufferedImage source, byte[][] eyeGroups, boolean[][] pupils,
        int[][] closedColors, int[][] pupilBackgroundColors, byte[][] eyebrowGroups, int[][] eyebrowColors) {
        if (source == null || source.getWidth() < 64 || source.getHeight() < 32) {
            throw new IllegalArgumentException("Expressive skins must be at least 64 by 32 pixels.");
        }
        if (eyeGroups == null || eyeGroups.length != 8 || pupils == null || pupils.length != 8
            || closedColors == null || closedColors.length != 8
            || pupilBackgroundColors == null || pupilBackgroundColors.length != 8
            || eyebrowGroups == null || eyebrowGroups.length != 8
            || eyebrowColors == null || eyebrowColors.length != 8) {
            throw new IllegalArgumentException("Eye and pupil selections must be 8 by 8 face masks.");
        }
        BufferedImage result = copy(source);

        // ETF option one stores complete closed base and overlay face frames in otherwise unused skin areas.
        copyRegion(source, result, 8, 8, 8, 8, 0, 0);
        copyRegion(source, result, 40, 8, 8, 8, 32, 0);
        for (int x = 0; x < 8; ++x) {
            if (eyeGroups[x] == null || eyeGroups[x].length != 8 || pupils[x] == null || pupils[x].length != 8
                || closedColors[x] == null || closedColors[x].length != 8
                || pupilBackgroundColors[x] == null || pupilBackgroundColors[x].length != 8
                || eyebrowGroups[x] == null || eyebrowGroups[x].length != 8
                || eyebrowColors[x] == null || eyebrowColors[x].length != 8) {
                throw new IllegalArgumentException("Eye and pupil selections must be 8 by 8 face masks.");
            }
            for (int y = 0; y < 8; ++y) {
                if (eyeGroups[x][y] == 0) {
                    continue;
                }
                result.setRGB(x, y, 0xff000000 | closedColors[x][y] & 0xffffff);
                // The chosen color represents the final visible eyelid, so suppress the hat layer at this pixel.
                result.setRGB(32 + x, y, 0x00000000);
            }
        }

        for (int i = 0; i < FEATURE_MARKER_ABGR.length; ++i) {
            result.setRGB(MARKER_X[i], MARKER_Y[i], swapRedBlue(FEATURE_MARKER_ABGR[i]));
        }
        // ETF blink type one: complete closed face frames at [0,0] and [32,0].
        result.setRGB(52, 16, swapRedBlue(0xffff00ff));
        result.setRGB(52, 19, swapRedBlue(0xffff00ff));
        writeMasks(result, eyeGroups, pupils);
        writePupilBackgrounds(result, pupilBackgroundColors);
        writeEyebrows(result, eyebrowGroups, eyebrowColors);
        return result;
    }

    /** Stores one eye mask; renderers infer coherent eyes from four-directionally connected components. */
    private static void writeMasks(BufferedImage image, byte[][] eyes, boolean[][] pupils) {
        for (int x = 0; x < 8; ++x) {
            int eyeBits = 0;
            int pupilBits = 0;
            for (int y = 0; y < 8; ++y) {
                if (eyes[x][y] != 0) {
                    eyeBits |= 1 << y;
                }
                if (pupils[x][y]) {
                    pupilBits |= 1 << y;
                }
            }
            image.setRGB(MASK_METADATA_X + x, MASK_METADATA_Y,
                0xff000000 | MASK_METADATA_MAGIC << 16 | eyeBits << 8 | pupilBits);
            image.setRGB(MASK_METADATA_X + x, MASK_METADATA_Y + 1,
                0xff000000 | MASK_METADATA_MAGIC << 16 | CONNECTED_MASK_VERSION);
        }
    }

    public static EditorData readEditorData(BufferedImage skin) {
        if (skin == null || skin.getWidth() < 64 || skin.getHeight() < 32 || !hasBlinkData(skin)) {
            return null;
        }
        EditorData data = new EditorData();
        boolean custom = true;
        boolean connectedFormat = true;
        for (int x = 0; x < 8; ++x) {
            int first = skin.getRGB(MASK_METADATA_X + x, MASK_METADATA_Y);
            int second = skin.getRGB(MASK_METADATA_X + x, MASK_METADATA_Y + 1);
            if ((first >> 16 & 255) != MASK_METADATA_MAGIC || (second >> 16 & 255) != MASK_METADATA_MAGIC) {
                custom = false;
                break;
            }
            connectedFormat &= (second & 255) == CONNECTED_MASK_VERSION;
        }
        if (custom) {
            for (int x = 0; x < 8; ++x) {
                int first = skin.getRGB(MASK_METADATA_X + x, MASK_METADATA_Y);
                int second = skin.getRGB(MASK_METADATA_X + x, MASK_METADATA_Y + 1);
                int eyes = first >> 8 & 255;
                if (!connectedFormat) {
                    eyes |= second >> 8 & 255;
                }
                int pupils = first & 255;
                int brows = skin.getRGB(EYEBROW_COLOR_X + x, EYEBROW_MASK_Y);
                boolean browsValid = (brows >> 16 & 255) == MASK_METADATA_MAGIC;
                for (int y = 0; y < 8; ++y) {
                    data.eyes[x][y] = ((eyes >> y) & 1) != 0;
                    data.pupils[x][y] = ((pupils >> y) & 1) != 0;
                    data.closedColors[x][y] = compositeFacePixel(skin, x, y, true);
                    data.pupilBackgroundColors[x][y] = skin.getRGB(PUPIL_BACKGROUND_X + x, y);
                    if (browsValid) {
                        data.eyebrowGroups[x][y] = (byte)((((brows >> 8 & 255) >> y) & 1) != 0 ? 1
                            : (((brows & 255) >> y) & 1) != 0 ? 2 : 0);
                        data.eyebrowColors[x][y] = skin.getRGB(EYEBROW_COLOR_X + x, EYEBROW_COLOR_Y + y);
                    }
                }
            }
            data.customExpressions = true;
            return data;
        }

        // ETF option-one skins contain complete open and closed faces, so recover every changed eyelid pixel.
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                int open = compositeFacePixel(skin, x, y, false);
                int closed = compositeFacePixel(skin, x, y, true);
                if (open != closed) {
                    data.eyes[x][y] = true;
                    data.closedColors[x][y] = closed;
                }
            }
        }
        return data.hasEyes() ? data : null;
    }

    private static int compositeFacePixel(BufferedImage image, int x, int y, boolean closed) {
        int base = image.getRGB((closed ? 0 : 8) + x, (closed ? 0 : 8) + y);
        int overlay = image.getRGB((closed ? 32 : 40) + x, (closed ? 0 : 8) + y);
        int alpha = overlay >>> 24;
        if (alpha == 0) return 0xff000000 | base & 0xffffff;
        if (alpha == 255) return overlay;
        int inverse = 255 - alpha;
        int red = (((overlay >> 16) & 255) * alpha + ((base >> 16) & 255) * inverse) / 255;
        int green = (((overlay >> 8) & 255) * alpha + ((base >> 8) & 255) * inverse) / 255;
        int blue = ((overlay & 255) * alpha + (base & 255) * inverse) / 255;
        return 0xff000000 | red << 16 | green << 8 | blue;
    }

    public static final class EditorData {
        public final boolean[][] eyes = new boolean[8][8];
        public final boolean[][] pupils = new boolean[8][8];
        public final int[][] closedColors = new int[8][8];
        public final int[][] pupilBackgroundColors = new int[8][8];
        public final byte[][] eyebrowGroups = new byte[8][8];
        public final int[][] eyebrowColors = new int[8][8];
        public boolean customExpressions;

        private boolean hasEyes() {
            for (int x = 0; x < 8; ++x) {
                for (int y = 0; y < 8; ++y) {
                    if (this.eyes[x][y]) return true;
                }
            }
            return false;
        }
    }

    /** Stores the exact 8x8 pupil-underlay palette in the unused overlay-head metadata tile. */
    private static void writePupilBackgrounds(BufferedImage image, int[][] backgrounds) {
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                image.setRGB(PUPIL_BACKGROUND_X + x, y, 0xff000000 | backgrounds[x][y] & 0xffffff);
            }
        }
    }

    /** Stores superposed brow colors plus their left/right group masks in the unused right-side strip. */
    private static void writeEyebrows(BufferedImage image, byte[][] groups, int[][] colors) {
        for (int x = 0; x < 8; ++x) {
            int leftBits = 0;
            int rightBits = 0;
            for (int y = 0; y < 8; ++y) {
                image.setRGB(EYEBROW_COLOR_X + x, EYEBROW_COLOR_Y + y,
                    groups[x][y] == 0 ? 0 : 0xff000000 | colors[x][y] & 0xffffff);
                if (groups[x][y] == 1) {
                    leftBits |= 1 << y;
                } else if (groups[x][y] == 2) {
                    rightBits |= 1 << y;
                }
            }
            image.setRGB(EYEBROW_COLOR_X + x, EYEBROW_MASK_Y,
                0xff000000 | MASK_METADATA_MAGIC << 16 | leftBits << 8 | rightBits);
        }
    }

    public static boolean hasBlinkData(BufferedImage image) {
        if (image == null || image.getWidth() < 53 || image.getHeight() < 20) {
            return false;
        }
        for (int i = 0; i < FEATURE_MARKER_ABGR.length; ++i) {
            if (swapRedBlue(image.getRGB(MARKER_X[i], MARKER_Y[i])) != FEATURE_MARKER_ABGR[i]) {
                return false;
            }
        }
        return true;
    }

    private static int swapRedBlue(int color) {
        return color & 0xff00ff00 | color >> 16 & 0xff | color << 16 & 0xff0000;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, source.getWidth(), source.getHeight(),
            source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth()), 0,
            source.getWidth());
        return result;
    }

    private static void copyRegion(BufferedImage source, BufferedImage target, int sourceX, int sourceY,
        int width, int height, int targetX, int targetY) {
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                target.setRGB(targetX + x, targetY + y, source.getRGB(sourceX + x, sourceY + y));
            }
        }
    }
}
