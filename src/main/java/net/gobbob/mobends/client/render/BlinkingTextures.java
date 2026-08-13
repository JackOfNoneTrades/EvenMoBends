/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * The player marker constants and embedded blink-frame layout are adapted from Entity Texture Features:
 * https://github.com/Traben-0/Entity_Texture_Features
 */
package net.gobbob.mobends.client.render;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.gobbob.mobends.config.BlinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

/** Selects opt-in blink textures without changing the global entity texture pipeline. */
public final class BlinkingTextures implements IResourceManagerReloadListener {
    private static final int SMOOTH_STEPS = 8;
    private static final int SMOOTH_SCALE = 4;
    private static final int[] FEATURE_MARKER = {
        0xff0000ff, 0xff00007f, 0xff0000ff, 0xff00ff00, 0xff007f00, 0xff00ff00,
        0xffff0000, 0xff7f0000, 0xffff0000, 0xffffffff, 0xffffffff, 0xffffffff
    };

    private static final BlinkingTextures INSTANCE = new BlinkingTextures();
    private static final Map<UUID, PlayerBlinkTextures> PLAYER_CACHE =
        new HashMap<UUID, PlayerBlinkTextures>();
    private static final Map<ResourceLocation, MobBlinkTextures> MOB_CACHE =
        new HashMap<ResourceLocation, MobBlinkTextures>();
    private static final Map<ResourceLocation, ResourceLocation> MOB_ALIAS_CACHE =
        new HashMap<ResourceLocation, ResourceLocation>();
    private static final Map<ResourceLocation, ResourceLocation> GENERATED_MOB_BLINKS =
        new HashMap<ResourceLocation, ResourceLocation>();

    private static boolean wawelReflectionResolved;
    private static Method wawelGetCachedImage;
    private static boolean angelicaReflectionResolved;
    private static Field angelicaMCPatcherEnabled;
    private static Field angelicaRandomMobsEnabled;
    private static Method angelicaRandomTexture;
    private static float renderPartialTicks;

    private BlinkingTextures() {}

    public static void registerReloadListener() {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        if (manager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager)manager).registerReloadListener(INSTANCE);
        }
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            renderPartialTicks = event.renderTickTime;
        }
    }

    public static ResourceLocation forPlayer(AbstractClientPlayer player, ResourceLocation normalTexture) {
        if (!BlinkConfig.arePlayersEnabled() || player == null || normalTexture == null) {
            return normalTexture;
        }

        PlayerBlinkTextures textures = getPlayerTextures(player, normalTexture);
        if (textures == null || textures.blink == null) {
            return normalTexture;
        }
        if (textures.expressionData != null && getBlinkProgress(player) == 0.0f) {
            return expressionTexture(player, textures);
        }
        if (BlinkConfig.areSmoothEyelidsEnabled() && textures.smoothFrames != null) {
            float progress = getBlinkProgress(player);
            int frame = Math.min(
                textures.smoothFrames.length - 1,
                Math.max(0, Math.round(progress * (textures.smoothFrames.length - 1))));
            return frame == 0 ? normalTexture : textures.smoothFrames[frame];
        }
        int frame = getBlinkFrame(player, textures.blink2 != null);
        if (frame == 1) {
            return textures.blink;
        }
        if (frame == 2) {
            return textures.blink2;
        }
        return normalTexture;
    }

    /** Returns a detached copy of the player's currently loaded skin, including Wawel Auth skins. */
    public static BufferedImage copyPlayerSkin(AbstractClientPlayer player) {
        if (player == null) {
            return null;
        }
        ResourceLocation location = player.getLocationSkin();
        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(location);
        SourceImage source = getSourceImage(location, texture);
        return source == null || source.image == null ? null : copy(source.image);
    }

    /** Discard generated blink and expression textures after a player's source skin changes. */
    public static void invalidatePlayer(UUID uuid) {
        if (uuid == null) return;
        PlayerBlinkTextures cached = PLAYER_CACHE.remove(uuid);
        if (cached == null) return;
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if (cached.blink != null) manager.deleteTexture(cached.blink);
        if (cached.blink2 != null) manager.deleteTexture(cached.blink2);
        if (cached.smoothFrames != null) {
            for (ResourceLocation frame : cached.smoothFrames) {
                if (frame != null) manager.deleteTexture(frame);
            }
        }
        deleteExpressionTextures(manager, cached);
    }

    public static ResourceLocation forMob(EntityLivingBase entity, ResourceLocation normalTexture) {
        if (!BlinkConfig.areMobsEnabled() || entity == null || normalTexture == null) {
            return normalTexture;
        }

        ResourceLocation selectedTexture = selectAngelicaVariant(entity, normalTexture);
        MobBlinkTextures textures = getMobTextures(selectedTexture);
        if (BlinkConfig.areSmoothEyelidsEnabled() && textures.smoothFrames != null) {
            float progress = getBlinkProgress(entity);
            int frame = Math.min(
                textures.smoothFrames.length - 1,
                Math.max(0, Math.round(progress * (textures.smoothFrames.length - 1))));
            return frame == 0 ? protectFromAngelica(selectedTexture) : textures.smoothFrames[frame];
        }
        int frame = getBlinkFrame(entity, textures.blink2 != null);
        if (frame == 1 && textures.blink != null) {
            return protectFromAngelica(textures.blink);
        }
        if (frame == 2 && textures.blink2 != null) {
            return protectFromAngelica(textures.blink2);
        }
        // Angelica's global bind hook would otherwise randomize the already-selected variant a second time.
        return protectFromAngelica(selectedTexture);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        TextureManager textures = Minecraft.getMinecraft().getTextureManager();
        for (PlayerBlinkTextures entry : PLAYER_CACHE.values()) {
            if (entry.blink != null) {
                textures.deleteTexture(entry.blink);
            }
            if (entry.blink2 != null) {
                textures.deleteTexture(entry.blink2);
            }
            if (entry.smoothFrames != null) {
                for (ResourceLocation frame : entry.smoothFrames) {
                    if (frame != null) {
                        textures.deleteTexture(frame);
                    }
                }
            }
            deleteExpressionTextures(textures, entry);
        }
        for (ResourceLocation alias : MOB_ALIAS_CACHE.values()) {
            textures.deleteTexture(alias);
        }
        for (ResourceLocation generated : GENERATED_MOB_BLINKS.values()) {
            textures.deleteTexture(generated);
        }
        for (MobBlinkTextures entry : MOB_CACHE.values()) {
            if (entry.smoothFrames != null) {
                for (ResourceLocation frame : entry.smoothFrames) {
                    if (frame != null) {
                        textures.deleteTexture(frame);
                    }
                }
            }
        }
        PLAYER_CACHE.clear();
        MOB_CACHE.clear();
        MOB_ALIAS_CACHE.clear();
        GENERATED_MOB_BLINKS.clear();
    }

    private static PlayerBlinkTextures getPlayerTextures(AbstractClientPlayer player, ResourceLocation normalTexture) {
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject textureObject = manager.getTexture(normalTexture);
        UUID uuid = player.getUniqueID();
        PlayerBlinkTextures cached = PLAYER_CACHE.get(uuid);
        if (cached != null && cached.normalTexture.equals(normalTexture) && cached.sourceIdentity == textureObject) {
            return cached;
        }
        if (cached != null) {
            if (cached.blink != null) {
                manager.deleteTexture(cached.blink);
            }
            if (cached.blink2 != null) {
                manager.deleteTexture(cached.blink2);
            }
            if (cached.smoothFrames != null) {
                for (ResourceLocation frame : cached.smoothFrames) {
                    if (frame != null) {
                        manager.deleteTexture(frame);
                    }
                }
            }
            deleteExpressionTextures(manager, cached);
            PLAYER_CACHE.remove(uuid);
        }

        SourceImage source = getSourceImage(normalTexture, textureObject);
        if (source == null || source.image == null || source.image.getWidth() < 64 || source.image.getHeight() < 20) {
            return null;
        }
        PlayerBlinkTextures created = createPlayerTextures(uuid, normalTexture, source);
        if (created == null) {
            created = new PlayerBlinkTextures(normalTexture, textureObject, null, null, null, null);
        }
        PLAYER_CACHE.put(uuid, created);
        return created;
    }

    private static PlayerBlinkTextures createPlayerTextures(UUID uuid, ResourceLocation normalTexture, SourceImage source) {
        BufferedImage skin = source.image;
        if (!hasFeatureMarker(skin)) {
            return null;
        }

        int blinkType = colorNumber(skin.getRGB(52, 16));
        if (blinkType < 1 || blinkType > 5) {
            return null;
        }
        int eyeLine = colorNumber(skin.getRGB(52, 19));
        if (eyeLine < 1 || eyeLine > 8) {
            eyeLine = 1;
        }

        BufferedImage blink = copy(skin);
        BufferedImage blink2 = blinkType == 2 || blinkType == 4 || blinkType == 5 ? copy(skin) : null;
        if (blinkType == 1 || blinkType == 2) {
            copyRegion(skin, blink, 0, 0, 8, 8, 8, 8);
            copyRegion(skin, blink, 32, 0, 8, 8, 40, 8);
            if (blink2 != null) {
                copyRegion(skin, blink2, 24, 0, 8, 8, 8, 8);
                copyRegion(skin, blink2, 56, 0, 8, 8, 40, 8);
            }
        } else if (blinkType == 3) {
            copyRegion(skin, blink, 12, 16, 8, 1, 8, 8 + eyeLine - 1);
        } else if (blinkType == 4) {
            copyRegion(skin, blink, 12, 16, 8, 2, 8, 8 + eyeLine - 1);
            copyRegion(skin, blink2, 12, 18, 8, 2, 8, 8 + eyeLine - 1);
        } else {
            copyRegion(skin, blink, 12, 16, 8, 4, 8, 8 + eyeLine - 1);
            copyRegion(skin, blink2, 36, 16, 8, 4, 8, 8 + eyeLine - 1);
        }

        String key = uuid.toString().replace("-", "");
        ResourceLocation blinkLocation = new ResourceLocation("mobends", "dynamic/blink/" + key + "_blink.png");
        ResourceLocation blink2Location = blink2 == null
            ? null
            : new ResourceLocation("mobends", "dynamic/blink/" + key + "_blink2.png");
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        manager.loadTexture(blinkLocation, new DynamicTexture(blink));
        if (blink2Location != null) {
            manager.loadTexture(blink2Location, new DynamicTexture(blink2));
        }
        ResourceLocation[] smoothFrames = createSmoothFrames(key, skin, blink, blink2);
        ExpressionData expressionData = readExpressionData(skin);
        return new PlayerBlinkTextures(
            normalTexture, source.identity, blinkLocation, blink2Location, smoothFrames, expressionData);
    }

    private static ResourceLocation expressionTexture(AbstractClientPlayer player, PlayerBlinkTextures textures) {
        float relativeYaw = wrapDegrees(player.rotationYawHead - player.renderYawOffset);
        int seed = player.getUniqueID().hashCode();
        float age = player.ticksExisted + renderPartialTicks;
        float idleX = (float)Math.sin((age + positiveModulo(seed, 97)) / 23.0) * 1.5f;
        float idleY = (float)Math.sin((age + positiveModulo(seed, 71)) / 31.0) * 0.75f;
        int offsetX = Math.round(clamp(relativeYaw / 45.0f, -1.0f, 1.0f) * 2.0f + idleX);
        int offsetY = Math.round(clamp(player.rotationPitch / 60.0f, -1.0f, 1.0f) * 1.25f + idleY);
        if (offsetX == 0 && offsetY == 0) {
            return textures.normalTexture;
        }
        int xIndex = offsetX + 3;
        int yIndex = offsetY + 2;
        ResourceLocation cached = textures.expressionLocations[xIndex][yIndex];
        if (cached != null) {
            return cached;
        }
        String key = player.getUniqueID().toString().replace("-", "");
        ResourceLocation location = new ResourceLocation(
            "mobends", "dynamic/expression/" + key + "_" + offsetX + "_" + offsetY + ".png");
        BufferedImage image = createExpressionFrame(textures.expressionData, offsetX, offsetY);
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, new DynamicTexture(image));
        textures.expressionLocations[xIndex][yIndex] = location;
        return location;
    }

    private static BufferedImage createExpressionFrame(ExpressionData data, int offsetX, int offsetY) {
        BufferedImage image = scaleNearest(data.skin, SMOOTH_SCALE);
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                if (!data.pupils[x][y]) {
                    continue;
                }
                fillScaledPixel(image, 8 + x, 8 + y, data.backgrounds[x][y]);
                fillScaledPixel(image, 40 + x, 8 + y, 0);
            }
        }
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                if (!data.pupils[x][y]) {
                    continue;
                }
                int group = data.eyeGroups[x][y];
                int color = compositeFacePixel(data.skin, x, y);
                for (int subX = 0; subX < SMOOTH_SCALE; ++subX) {
                    for (int subY = 0; subY < SMOOTH_SCALE; ++subY) {
                        int targetX = x * SMOOTH_SCALE + subX + offsetX;
                        int targetY = y * SMOOTH_SCALE + subY + offsetY;
                        if (targetX < 0 || targetY < 0
                            || targetX >= 8 * SMOOTH_SCALE || targetY >= 8 * SMOOTH_SCALE
                            || data.eyeGroups[targetX / SMOOTH_SCALE][targetY / SMOOTH_SCALE] != group) {
                            continue;
                        }
                        image.setRGB(8 * SMOOTH_SCALE + targetX, 8 * SMOOTH_SCALE + targetY, color);
                        image.setRGB(40 * SMOOTH_SCALE + targetX, 8 * SMOOTH_SCALE + targetY, 0);
                    }
                }
            }
        }
        return image;
    }

    private static ExpressionData readExpressionData(BufferedImage skin) {
        byte[][] eyes = new byte[8][8];
        boolean[][] pupils = new boolean[8][8];
        int[][] backgrounds = new int[8][8];
        byte[][] brows = new byte[8][8];
        int[][] browColors = new int[8][8];
        boolean valid = true;
        boolean connectedFormat = true;
        for (int x = 0; x < 8; ++x) {
            int first = skin.getRGB(24 + x, 0);
            int second = skin.getRGB(24 + x, 1);
            int browMask = skin.getRGB(56 + x, 24);
            if ((first >> 16 & 255) != 0x42 || (second >> 16 & 255) != 0x42) {
                valid = false;
                break;
            }
            connectedFormat &= (second & 255) == 0x43;
            int leftBits = first >> 8 & 255;
            int pupilBits = first & 255;
            int rightBits = second >> 8 & 255;
            boolean browsValid = (browMask >> 16 & 255) == 0x42;
            int leftBrowBits = browMask >> 8 & 255;
            int rightBrowBits = browMask & 255;
            for (int y = 0; y < 8; ++y) {
                eyes[x][y] = (byte)((((leftBits | rightBits) >> y) & 1) != 0 ? 1 : 0);
                pupils[x][y] = ((pupilBits >> y) & 1) != 0;
                backgrounds[x][y] = skin.getRGB(56 + x, y);
                if (browsValid) {
                    brows[x][y] = (byte)(((leftBrowBits >> y) & 1) != 0 ? 1
                        : ((rightBrowBits >> y) & 1) != 0 ? 2 : 0);
                    browColors[x][y] = skin.getRGB(56 + x, 16 + y);
                }
            }
        }
        if (valid) {
            inferConnectedComponents(eyes);
        }
        return valid ? new ExpressionData(copy(skin), eyes, pupils, backgrounds, brows, browColors) : null;
    }

    private static void inferConnectedComponents(byte[][] eyes) {
        boolean[][] mask = new boolean[8][8];
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                mask[x][y] = eyes[x][y] != 0;
                eyes[x][y] = 0;
            }
        }
        int group = 0;
        int[] queueX = new int[64];
        int[] queueY = new int[64];
        for (int startX = 0; startX < 8; ++startX) {
            for (int startY = 0; startY < 8; ++startY) {
                if (!mask[startX][startY] || eyes[startX][startY] != 0) continue;
                ++group;
                int first = 0;
                int last = 1;
                queueX[0] = startX;
                queueY[0] = startY;
                eyes[startX][startY] = (byte)group;
                while (first < last) {
                    int x = queueX[first];
                    int y = queueY[first++];
                    int[] dx = {-1, 1, 0, 0};
                    int[] dy = {0, 0, -1, 1};
                    for (int i = 0; i < 4; ++i) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8
                            && mask[nx][ny] && eyes[nx][ny] == 0) {
                            eyes[nx][ny] = (byte)group;
                            queueX[last] = nx;
                            queueY[last++] = ny;
                        }
                    }
                }
            }
        }
    }

    public static ResourceLocation eyebrowTexture(
        AbstractClientPlayer player, ResourceLocation normalTexture, int group) {
        PlayerBlinkTextures textures = getPlayerTextures(player, normalTexture);
        if (textures == null || textures.expressionData == null || !textures.expressionData.hasEyebrows) {
            return null;
        }
        int index = group - 1;
        if (index < 0 || index >= textures.eyebrowLocations.length) {
            return null;
        }
        if (textures.eyebrowLocations[index] == null) {
            BufferedImage overlay = new BufferedImage(
                textures.expressionData.skin.getWidth(), textures.expressionData.skin.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < 8; ++x) {
                for (int y = 0; y < 8; ++y) {
                    if (textures.expressionData.eyebrows[x][y] == group) {
                        overlay.setRGB(8 + x, 8 + y, textures.expressionData.eyebrowColors[x][y]);
                    }
                }
            }
            textures.eyebrowLocations[index] = new ResourceLocation(
                "mobends", "dynamic/expression/" + player.getUniqueID().toString().replace("-", "")
                    + "_brow_" + group + ".png");
            Minecraft.getMinecraft().getTextureManager().loadTexture(
                textures.eyebrowLocations[index], new DynamicTexture(overlay));
        }
        return textures.eyebrowLocations[index];
    }

    private static void deleteExpressionTextures(TextureManager manager, PlayerBlinkTextures textures) {
        for (ResourceLocation[] row : textures.expressionLocations) {
            for (ResourceLocation location : row) {
                if (location != null) {
                    manager.deleteTexture(location);
                }
            }
        }
        for (ResourceLocation location : textures.eyebrowLocations) {
            if (location != null) {
                manager.deleteTexture(location);
            }
        }
    }

    private static ResourceLocation[] createSmoothFrames(String key, BufferedImage normal, BufferedImage closed,
        BufferedImage halfClosed) {
        ResourceLocation[] frames = new ResourceLocation[SMOOTH_STEPS + 1];
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        for (int step = 1; step <= SMOOTH_STEPS; ++step) {
            float progress = (float)step / (float)SMOOTH_STEPS;
            BufferedImage frame;
            if (halfClosed != null && progress < 0.5f) {
                frame = wipe(normal, halfClosed, progress * 2.0f);
            } else if (halfClosed != null) {
                frame = wipe(halfClosed, closed, progress * 2.0f - 1.0f);
            } else {
                frame = wipe(normal, closed, progress);
            }
            ResourceLocation location = new ResourceLocation(
                "mobends", "dynamic/blink/" + key + "_smooth_" + step + ".png");
            manager.loadTexture(location, new DynamicTexture(frame));
            frames[step] = location;
        }
        return frames;
    }

    private static SourceImage getSourceImage(ResourceLocation location, ITextureObject textureObject) {
        BufferedImage image = getBufferedImageField(textureObject);
        if (image != null) {
            return new SourceImage(image, textureObject);
        }

        image = getWawelCachedImage(location);
        if (image != null) {
            return new SourceImage(image, textureObject);
        }

        if (textureObject instanceof DynamicTexture) {
            int[] pixels = ((DynamicTexture)textureObject).getTextureData();
            int side = (int)Math.sqrt(pixels.length);
            if (side * side == pixels.length && side >= 64) {
                BufferedImage dynamicImage = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
                dynamicImage.setRGB(0, 0, side, side, pixels, 0, side);
                return new SourceImage(dynamicImage, textureObject);
            }
        }

        if (textureObject == null) {
            return null;
        }
        IResource resource = null;
        try {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            InputStream input = resource.getInputStream();
            try {
                image = ImageIO.read(input);
            } finally {
                input.close();
            }
            return image == null ? null : new SourceImage(image, textureObject);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static BufferedImage getBufferedImageField(ITextureObject textureObject) {
        if (textureObject == null) {
            return null;
        }
        if (textureObject instanceof ThreadDownloadImageData) {
            try {
                return ReflectionHelper.getPrivateValue(
                    ThreadDownloadImageData.class,
                    (ThreadDownloadImageData)textureObject,
                    "bufferedImage",
                    "field_110560_d");
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        Class<?> type = textureObject.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("bufferedImage");
                field.setAccessible(true);
                Object value = field.get(textureObject);
                return value instanceof BufferedImage ? (BufferedImage)value : null;
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static BufferedImage getWawelCachedImage(ResourceLocation location) {
        if (!wawelReflectionResolved) {
            wawelReflectionResolved = true;
            try {
                Class<?> loader = Class.forName("org.fentanylsolutions.wawelauth.client.render.LocalTextureLoader");
                wawelGetCachedImage = loader.getMethod("getCachedImage", ResourceLocation.class);
            } catch (ReflectiveOperationException ignored) {
                wawelGetCachedImage = null;
            }
        }
        if (wawelGetCachedImage == null) {
            return null;
        }
        try {
            Object image = wawelGetCachedImage.invoke(null, location);
            return image instanceof BufferedImage ? (BufferedImage)image : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static MobBlinkTextures getMobTextures(ResourceLocation normalTexture) {
        MobBlinkTextures cached = MOB_CACHE.get(normalTexture);
        if (cached != null) {
            return cached;
        }
        ResourceLocation blink = companion(normalTexture, "_blink.png");
        ResourceLocation blink2 = companion(normalTexture, "_blink2.png");
        blink = resourceExists(blink) ? blink : createGeneratedMobBlink(normalTexture);
        blink2 = resourceExists(blink2) ? blink2 : null;
        ResourceLocation[] smoothFrames = null;
        if (blink != null) {
            BufferedImage normalImage = readTextureImage(normalTexture);
            BufferedImage blinkImage = readTextureImage(blink);
            BufferedImage blink2Image = blink2 == null ? null : readTextureImage(blink2);
            if (normalImage != null && blinkImage != null
                && normalImage.getWidth() == blinkImage.getWidth()
                && normalImage.getHeight() == blinkImage.getHeight()) {
                String key = "mob_" + Integer.toHexString(normalTexture.toString().hashCode());
                smoothFrames = createSmoothFrames(key, normalImage, blinkImage, blink2Image);
            }
        }
        MobBlinkTextures result = new MobBlinkTextures(blink, blink2, smoothFrames);
        MOB_CACHE.put(normalTexture, result);
        return result;
    }

    private static BufferedImage readTextureImage(ResourceLocation location) {
        IResource resource = null;
        try {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            InputStream input = resource.getInputStream();
            try {
                return ImageIO.read(input);
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(location);
            if (!(texture instanceof DynamicTexture)) {
                return null;
            }
            int[] pixels = ((DynamicTexture)texture).getTextureData();
            int side = (int)Math.sqrt(pixels.length);
            if (side * side != pixels.length) {
                return null;
            }
            BufferedImage image = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, side, side, pixels, 0, side);
            return image;
        }
    }

    private static ResourceLocation createGeneratedMobBlink(ResourceLocation normalTexture) {
        ResourceLocation cached = GENERATED_MOB_BLINKS.get(normalTexture);
        if (cached != null) {
            return cached;
        }

        IResource resource = null;
        try {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(normalTexture);
            InputStream input = resource.getInputStream();
            BufferedImage source;
            try {
                source = ImageIO.read(input);
            } finally {
                input.close();
            }
            if (source == null || source.getWidth() < 16 || source.getHeight() < 14) {
                return null;
            }

            BufferedImage blink = copy(source);
            // Replace the open black/white pixels with the skin immediately above them, then draw a dark two-pixel
            // crease one row lower. Merely widening the black pupil still reads as an open eye at gameplay distance.
            copyRegion(source, blink, 8, 10, 2, 1, 8, 11);
            copyRegion(source, blink, 14, 10, 2, 1, 14, 11);
            blink.setRGB(8, 12, source.getRGB(8, 11));
            blink.setRGB(9, 12, source.getRGB(8, 11));
            blink.setRGB(14, 12, source.getRGB(15, 11));
            blink.setRGB(15, 12, source.getRGB(15, 11));

            ResourceLocation generated = new ResourceLocation(
                "mobends",
                "dynamic/blink/generated_" + Integer.toHexString(normalTexture.toString().hashCode()) + ".png");
            Minecraft.getMinecraft().getTextureManager().loadTexture(generated, new DynamicTexture(blink));
            GENERATED_MOB_BLINKS.put(normalTexture, generated);
            return generated;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static ResourceLocation selectAngelicaVariant(EntityLivingBase entity, ResourceLocation normalTexture) {
        if (!isAngelicaRandomMobsEnabled()) {
            return normalTexture;
        }
        try {
            Object selected = angelicaRandomTexture.invoke(null, entity, normalTexture);
            return selected instanceof ResourceLocation ? (ResourceLocation)selected : normalTexture;
        } catch (ReflectiveOperationException ignored) {
            return normalTexture;
        }
    }

    private static ResourceLocation protectFromAngelica(ResourceLocation texture) {
        if (!isAngelicaRandomMobsEnabled()) {
            return texture;
        }
        ResourceLocation cached = MOB_ALIAS_CACHE.get(texture);
        if (cached != null) {
            return cached;
        }

        IResource resource = null;
        try {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(texture);
            InputStream input = resource.getInputStream();
            BufferedImage image;
            try {
                image = ImageIO.read(input);
            } finally {
                input.close();
            }
            if (image == null) {
                return texture;
            }
            ResourceLocation alias = new ResourceLocation(
                "mobends",
                "dynamic/blink/mob_" + Integer.toHexString(texture.toString().hashCode()) + ".png");
            Minecraft.getMinecraft().getTextureManager().loadTexture(alias, new DynamicTexture(image));
            MOB_ALIAS_CACHE.put(texture, alias);
            return alias;
        } catch (IOException ignored) {
            return texture;
        }
    }

    private static boolean isAngelicaRandomMobsEnabled() {
        resolveAngelica();
        if (angelicaRandomTexture == null || angelicaMCPatcherEnabled == null || angelicaRandomMobsEnabled == null) {
            return false;
        }
        try {
            return angelicaMCPatcherEnabled.getBoolean(null) && angelicaRandomMobsEnabled.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static synchronized void resolveAngelica() {
        if (angelicaReflectionResolved) {
            return;
        }
        angelicaReflectionResolved = true;
        try {
            Class<?> angelicaConfig = Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig");
            Class<?> randomMobsConfig = Class.forName("jss.notfine.config.MCPatcherForgeConfig$RandomMobs");
            Class<?> randomizer = Class.forName("com.prupe.mcpatcher.mob.MobRandomizer");
            angelicaMCPatcherEnabled = angelicaConfig.getField("enableMCPatcherForgeFeatures");
            angelicaRandomMobsEnabled = randomMobsConfig.getField("enabled");
            angelicaRandomTexture = randomizer.getMethod(
                "randomTexture", EntityLivingBase.class, ResourceLocation.class);
        } catch (ReflectiveOperationException ignored) {
            angelicaMCPatcherEnabled = null;
            angelicaRandomMobsEnabled = null;
            angelicaRandomTexture = null;
        }
    }

    private static ResourceLocation companion(ResourceLocation texture, String suffix) {
        String path = texture.getResourcePath();
        int dot = path.lastIndexOf('.');
        path = dot < 0 ? path + suffix : path.substring(0, dot) + suffix;
        return new ResourceLocation(texture.getResourceDomain(), path);
    }

    private static boolean resourceExists(ResourceLocation location) {
        IResource resource = null;
        try {
            resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            return true;
        } catch (IOException ignored) {
            return false;
        } finally {
            if (resource != null) {
                try {
                    resource.getInputStream().close();
                } catch (IOException ignored) {}
            }
        }
    }

    private static int getBlinkFrame(EntityLivingBase entity, boolean hasSecondFrame) {
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer)entity;
            if (player.isPlayerSleeping()) {
                return 1;
            }
        }
        if (Potion.blindness != null && entity.isPotionActive(Potion.blindness)) {
            return hasSecondFrame ? 2 : 1;
        }

        int frequency = BlinkConfig.getFrequency();
        int hash = entity.getUniqueID() == null ? entity.getEntityId() : entity.getUniqueID().hashCode();
        int interval = frequency + 20 + positiveModulo(hash, frequency * 2);
        int phase = positiveModulo(entity.ticksExisted, interval);
        int length = BlinkConfig.getLength();
        if (phase > length * 2) {
            return 0;
        }
        if (!hasSecondFrame) {
            return 1;
        }
        return phase == 0 || phase > length ? 2 : 1;
    }

    private static float getBlinkProgress(EntityLivingBase entity) {
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer)entity;
            if (player.isPlayerSleeping()) {
                return 1.0f;
            }
        }
        if (Potion.blindness != null && entity.isPotionActive(Potion.blindness)) {
            return 1.0f;
        }

        int frequency = BlinkConfig.getFrequency();
        int hash = entity.getUniqueID() == null ? entity.getEntityId() : entity.getUniqueID().hashCode();
        int interval = frequency + 20 + positiveModulo(hash, frequency * 2);
        float phase = positiveModulo(entity.ticksExisted, interval)
            + renderPartialTicks;
        float length = BlinkConfig.getLength();
        if (phase >= length * 2.0f) {
            return 0.0f;
        }
        float linear = phase <= length ? phase / length : (length * 2.0f - phase) / length;
        // Smoothstep keeps the eyelid from snapping at either end of the blink.
        return linear * linear * (3.0f - 2.0f * linear);
    }

    private static int positiveModulo(int value, int divisor) {
        int result = value % divisor;
        return result < 0 ? result + divisor : result;
    }

    private static boolean hasFeatureMarker(BufferedImage image) {
        int[] xs = {1, 0, 0, 2, 3, 3, 0, 0, 1, 3, 2, 3};
        int[] ys = {16, 16, 17, 16, 16, 17, 18, 19, 19, 18, 19, 18};
        for (int i = 0; i < FEATURE_MARKER.length; ++i) {
            if (toAbgr(image.getRGB(xs[i], ys[i])) != FEATURE_MARKER[i]) {
                return false;
            }
        }
        return true;
    }

    private static int colorNumber(int argb) {
        switch (toAbgr(argb)) {
            case 0xffff00ff: return 1;
            case 0xffffff00: return 2;
            case 0xff0000ff: return 3;
            case 0xff00ff00: return 4;
            case 0xff00407f: return 5;
            case 0xffff0000: return 6;
            case 0xff007fff: return 7;
            case 0xff22ffff: return 8;
            default: return 0;
        }
    }

    private static int toAbgr(int argb) {
        return argb & 0xff00ff00 | argb >> 16 & 0xff | argb << 16 & 0xff0000;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float wrapDegrees(float value) {
        value %= 360.0f;
        if (value >= 180.0f) {
            value -= 360.0f;
        }
        if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }

    private static BufferedImage scaleNearest(BufferedImage source, int scale) {
        BufferedImage result = new BufferedImage(
            source.getWidth() * scale, source.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                int color = source.getRGB(x, y);
                for (int subX = 0; subX < scale; ++subX) {
                    for (int subY = 0; subY < scale; ++subY) {
                        result.setRGB(x * scale + subX, y * scale + subY, color);
                    }
                }
            }
        }
        return result;
    }

    private static void fillScaledPixel(BufferedImage image, int x, int y, int color) {
        for (int subX = 0; subX < SMOOTH_SCALE; ++subX) {
            for (int subY = 0; subY < SMOOTH_SCALE; ++subY) {
                image.setRGB(x * SMOOTH_SCALE + subX, y * SMOOTH_SCALE + subY, color);
            }
        }
    }

    private static int compositeFacePixel(BufferedImage skin, int x, int y) {
        int base = skin.getRGB(8 + x, 8 + y);
        int overlay = skin.getRGB(40 + x, 8 + y);
        int alpha = overlay >>> 24;
        if (alpha == 0) {
            return base;
        }
        if (alpha == 255) {
            return overlay;
        }
        int inverse = 255 - alpha;
        int red = (((overlay >> 16) & 255) * alpha + ((base >> 16) & 255) * inverse) / 255;
        int green = (((overlay >> 8) & 255) * alpha + ((base >> 8) & 255) * inverse) / 255;
        int blue = ((overlay & 255) * alpha + (base & 255) * inverse) / 255;
        return 0xff000000 | red << 16 | green << 8 | blue;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, source.getWidth(), source.getHeight(),
            source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth()), 0,
            source.getWidth());
        return result;
    }

    private static BufferedImage wipe(BufferedImage from, BufferedImage to, float amount) {
        int width = from.getWidth();
        int height = from.getHeight();
        BufferedImage result = new BufferedImage(
            width * SMOOTH_SCALE, height * SMOOTH_SCALE, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; ++x) {
            int firstChangedY = height;
            int lastChangedY = -1;
            for (int y = 0; y < height; ++y) {
                if (from.getRGB(x, y) != to.getRGB(x, y)) {
                    firstChangedY = Math.min(firstChangedY, y);
                    lastChangedY = y;
                }
            }
            float boundary = (firstChangedY + (lastChangedY - firstChangedY + 1) * amount) * SMOOTH_SCALE;
            for (int y = 0; y < height; ++y) {
                int fromColor = from.getRGB(x, y);
                int toColor = to.getRGB(x, y);
                for (int subX = 0; subX < SMOOTH_SCALE; ++subX) {
                    for (int subY = 0; subY < SMOOTH_SCALE; ++subY) {
                        int scaledY = y * SMOOTH_SCALE + subY;
                        int color = fromColor != toColor && scaledY < boundary ? toColor : fromColor;
                        result.setRGB(x * SMOOTH_SCALE + subX, scaledY, color);
                    }
                }
            }
        }
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

    private static final class SourceImage {
        final BufferedImage image;
        final Object identity;

        SourceImage(BufferedImage image, Object identity) {
            this.image = image;
            this.identity = identity;
        }
    }

    private static final class PlayerBlinkTextures {
        final ResourceLocation normalTexture;
        final Object sourceIdentity;
        final ResourceLocation blink;
        final ResourceLocation blink2;
        final ResourceLocation[] smoothFrames;
        final ExpressionData expressionData;
        final ResourceLocation[][] expressionLocations = new ResourceLocation[7][5];
        final ResourceLocation[] eyebrowLocations = new ResourceLocation[2];

        PlayerBlinkTextures(ResourceLocation normalTexture, Object sourceIdentity, ResourceLocation blink,
            ResourceLocation blink2, ResourceLocation[] smoothFrames, ExpressionData expressionData) {
            this.normalTexture = normalTexture;
            this.sourceIdentity = sourceIdentity;
            this.blink = blink;
            this.blink2 = blink2;
            this.smoothFrames = smoothFrames;
            this.expressionData = expressionData;
        }
    }

    private static final class ExpressionData {
        final BufferedImage skin;
        final byte[][] eyeGroups;
        final boolean[][] pupils;
        final int[][] backgrounds;
        final byte[][] eyebrows;
        final int[][] eyebrowColors;
        final boolean hasEyebrows;

        ExpressionData(BufferedImage skin, byte[][] eyeGroups, boolean[][] pupils, int[][] backgrounds,
            byte[][] eyebrows, int[][] eyebrowColors) {
            this.skin = skin;
            this.eyeGroups = eyeGroups;
            this.pupils = pupils;
            this.backgrounds = backgrounds;
            this.eyebrows = eyebrows;
            this.eyebrowColors = eyebrowColors;
            boolean anyEyebrow = false;
            for (int x = 0; x < 8; ++x) {
                for (int y = 0; y < 8; ++y) {
                    anyEyebrow |= eyebrows[x][y] != 0;
                }
            }
            this.hasEyebrows = anyEyebrow;
        }
    }

    private static final class MobBlinkTextures {
        final ResourceLocation blink;
        final ResourceLocation blink2;
        final ResourceLocation[] smoothFrames;

        MobBlinkTextures(ResourceLocation blink, ResourceLocation blink2, ResourceLocation[] smoothFrames) {
            this.blink = blink;
            this.blink2 = blink2;
            this.smoothFrames = smoothFrames;
        }
    }
}
