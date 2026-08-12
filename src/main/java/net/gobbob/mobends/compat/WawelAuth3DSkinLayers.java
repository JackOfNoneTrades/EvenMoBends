package net.gobbob.mobends.compat;

import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Reflection bridge for WawelAuth's optional voxel skin layers.
 *
 * <p>The upstream WawelAuth meshes use twelve-pixel vanilla limbs. Mo' Bends needs separate six-pixel meshes so
 * elbows and knees can move independently. This bridge asks WawelAuth's own mesh builder to create those halves from
 * the authenticated skin image and removes the two internal joint caps.</p>
 */
public final class WawelAuth3DSkinLayers {
    private static final String CONFIG_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DConfig";
    private static final String SETUP_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DSetup";
    private static final String STATE_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DState";
    private static final String SKIN_DATA_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DSkinData";
    private static final String MESH_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DMesh";

    private static final int SEGMENT_HEIGHT = 6;
    private static final int LIMB_DEPTH = 4;

    private static final Map<UUID, Layers> CACHE = new HashMap<UUID, Layers>();

    private static boolean resolved;
    private static boolean available;
    private static Method getState;
    private static Method updateState;
    private static Method createOrUpdate;
    private static Method getSkinImage;
    private static Method buildMesh;
    private static Method meshRender;
    private static Method meshCleanup;
    private static Method meshIsCompiled;
    private static Method meshSetPosition;
    private static Method meshSetOffset;
    private static Method meshSetRotation;
    private static Constructor<?> skinDataConstructor;
    private static Field stateInitialized;
    private static Field stateSlim;
    private static Field stateSkinLocation;
    private static Field stateHatMesh;
    private static Field stateJacketMesh;
    private static Field stateRightSleeveMesh;
    private static Field stateLeftSleeveMesh;
    private static Field stateRightPantsMesh;
    private static Field stateLeftPantsMesh;
    private static Field enabled3D;
    private static Field modernSkinSupport;
    private static Field enableHat3D;
    private static Field enableJacket3D;
    private static Field enableRightSleeve3D;
    private static Field enableLeftSleeve3D;
    private static Field enableRightPants3D;
    private static Field enableLeftPants3D;
    private static Field baseVoxelSize;
    private static Field bodyVoxelWidthSize;
    private static Field headVoxelSize;

    private WawelAuth3DSkinLayers() {}

    public static boolean isEnabled() {
        if (!WawelAuthCompat.isLoaded() || CompatibilityPolicy.isMobOnlyMode()) {
            return false;
        }
        resolve();
        if (!available) {
            return false;
        }
        try {
            return enabled3D.getBoolean(null) && modernSkinSupport.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    /** Ensure WawelAuth has generated the player's full source meshes. Used by first-person rendering. */
    public static void ensurePlayerState(AbstractClientPlayer player, boolean slim) {
        if (!isEnabled() || player == null) {
            return;
        }
        try {
            UUID uuid = player.getUniqueID();
            Object existing = getState.invoke(null, uuid);
            Object updated = createOrUpdate.invoke(null, player, existing, Boolean.valueOf(slim));
            updateState.invoke(null, uuid, updated);
        } catch (ReflectiveOperationException ignored) {
            // Compatibility remains optional. Flat overlays stay visible if WawelAuth's internals change.
        }
    }

    public static Layers getLayers(UUID uuid, boolean slim) {
        if (!isEnabled() || uuid == null) {
            return null;
        }

        try {
            Object state = getState.invoke(null, uuid);
            if (state == null || !stateInitialized.getBoolean(state)) {
                discard(uuid);
                return null;
            }

            boolean stateUsesSlimArms = stateSlim.getBoolean(state);
            if (stateUsesSlimArms != slim) {
                return null;
            }

            Layers cached = CACHE.get(uuid);
            if (cached != null && cached.sourceState == state && cached.slim == slim) {
                return cached;
            }

            if (cached != null) {
                cached.cleanup();
            }
            Layers layers = buildLayers(state, slim);
            CACHE.put(uuid, layers);
            return layers;
        } catch (ReflectiveOperationException ignored) {
            discard(uuid);
            return null;
        }
    }

    public static float getBaseVoxelSize() {
        return getConfigFloat(baseVoxelSize, 1.15f);
    }

    public static float getBodyVoxelWidthSize() {
        return getConfigFloat(bodyVoxelWidthSize, 1.05f);
    }

    public static float getHeadVoxelSize() {
        return getConfigFloat(headVoxelSize, 1.18f);
    }

    /** Render a WawelAuth mesh at the current OpenGL transform. */
    public static boolean renderMesh(Object mesh, float scale, float scaleX, float scaleY, float scaleZ,
        float localOffsetX, float localOffsetY, float localOffsetZ) {
        if (mesh == null || !isEnabled()) {
            return false;
        }
        try {
            if (!Boolean.TRUE.equals(meshIsCompiled.invoke(mesh))) {
                return false;
            }
            meshSetPosition.invoke(mesh, Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(0.0f));
            meshSetOffset.invoke(mesh, Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(0.0f));
            meshSetRotation.invoke(mesh, Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(0.0f));
            meshRender.invoke(
                mesh,
                Float.valueOf(scale),
                Float.valueOf(scaleX),
                Float.valueOf(scaleY),
                Float.valueOf(scaleZ),
                Float.valueOf(localOffsetX),
                Float.valueOf(localOffsetY),
                Float.valueOf(localOffsetZ));
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static Layers buildLayers(Object state, boolean slim) throws ReflectiveOperationException {
        Layers layers = new Layers(state, slim);
        layers.hat = stateHatMesh.get(state);
        layers.jacket = stateJacketMesh.get(state);

        ResourceLocation skinLocation = (ResourceLocation)stateSkinLocation.get(state);
        BufferedImage skin = skinLocation == null ? null : (BufferedImage)getSkinImage.invoke(null, skinLocation);
        if (skin == null || skin.getWidth() != 64 || skin.getHeight() != 64) {
            return layers;
        }

        int armWidth = slim ? 3 : 4;
        if (stateRightSleeveMesh.get(state) != null) {
            layers.rightArm = buildSegmentMesh(skin, armWidth, 40, 32, -2.0f, true);
            layers.rightForeArm = buildSegmentMesh(skin, armWidth, 40, 32, 0.0f, false);
        }
        if (stateLeftSleeveMesh.get(state) != null) {
            layers.leftArm = buildSegmentMesh(skin, armWidth, 48, 48, -2.0f, true);
            layers.leftForeArm = buildSegmentMesh(skin, armWidth, 48, 48, 0.0f, false);
        }
        if (stateRightPantsMesh.get(state) != null) {
            layers.rightLeg = buildSegmentMesh(skin, 4, 0, 32, 0.0f, true);
            layers.rightForeLeg = buildSegmentMesh(skin, 4, 0, 32, 0.0f, false);
        }
        if (stateLeftPantsMesh.get(state) != null) {
            layers.leftLeg = buildSegmentMesh(skin, 4, 0, 48, 0.0f, true);
            layers.leftForeLeg = buildSegmentMesh(skin, 4, 0, 48, 0.0f, false);
        }
        return layers;
    }

    private static Object buildSegmentMesh(BufferedImage source, int width, int textureU, int textureV,
        float rotationOffset, boolean upper) throws ReflectiveOperationException {
        BufferedImage segmentSkin = copyImage(source);
        if (upper) {
            // VoxelSurfaceBuilder calls the lower end UP. Remove that internal elbow/knee cap.
            clearRect(segmentSkin, textureU + LIMB_DEPTH + width, textureV, width, LIMB_DEPTH);
        } else {
            int textureBoxWidth = LIMB_DEPTH + width + LIMB_DEPTH + width;
            int sourceY = textureV + LIMB_DEPTH + SEGMENT_HEIGHT;
            int targetY = textureV + LIMB_DEPTH;
            for (int x = 0; x < textureBoxWidth; ++x) {
                for (int y = 0; y < SEGMENT_HEIGHT; ++y) {
                    segmentSkin.setRGB(textureU + x, targetY + y, source.getRGB(textureU + x, sourceY + y));
                }
            }
            // VoxelSurfaceBuilder calls the upper end DOWN. Remove that internal elbow/knee cap.
            clearRect(segmentSkin, textureU + LIMB_DEPTH, textureV, width, LIMB_DEPTH);
        }

        Object skinData = skinDataConstructor.newInstance(segmentSkin);
        return buildMesh.invoke(
            null,
            skinData,
            Integer.valueOf(width),
            Integer.valueOf(SEGMENT_HEIGHT),
            Integer.valueOf(LIMB_DEPTH),
            Integer.valueOf(textureU),
            Integer.valueOf(textureV),
            Boolean.TRUE,
            Float.valueOf(rotationOffset));
    }

    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.setRGB(
            0,
            0,
            source.getWidth(),
            source.getHeight(),
            source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth()),
            0,
            source.getWidth());
        return copy;
    }

    private static void clearRect(BufferedImage image, int x, int y, int width, int height) {
        for (int px = x; px < x + width; ++px) {
            for (int py = y; py < y + height; ++py) {
                image.setRGB(px, py, 0);
            }
        }
    }

    private static float getConfigFloat(Field field, float fallback) {
        if (!isEnabled() || field == null) {
            return fallback;
        }
        try {
            return field.getFloat(null);
        } catch (IllegalAccessException ignored) {
            return fallback;
        }
    }

    private static boolean getConfigBoolean(Field field) {
        if (!isEnabled() || field == null) {
            return false;
        }
        try {
            return field.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static void discard(UUID uuid) {
        Layers old = CACHE.remove(uuid);
        if (old != null) {
            old.cleanup();
        }
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        try {
            Class<?> config = Class.forName(CONFIG_CLASS);
            Class<?> setup = Class.forName(SETUP_CLASS);
            Class<?> state = Class.forName(STATE_CLASS);
            Class<?> skinData = Class.forName(SKIN_DATA_CLASS);
            Class<?> mesh = Class.forName(MESH_CLASS);

            enabled3D = config.getField("enabled3D");
            modernSkinSupport = config.getField("modernSkinSupport");
            enableHat3D = config.getField("enableHat3D");
            enableJacket3D = config.getField("enableJacket3D");
            enableRightSleeve3D = config.getField("enableRightSleeve3D");
            enableLeftSleeve3D = config.getField("enableLeftSleeve3D");
            enableRightPants3D = config.getField("enableRightPants3D");
            enableLeftPants3D = config.getField("enableLeftPants3D");
            baseVoxelSize = config.getField("baseVoxelSize");
            bodyVoxelWidthSize = config.getField("bodyVoxelWidthSize");
            headVoxelSize = config.getField("headVoxelSize");

            getState = setup.getMethod("getState", UUID.class);
            updateState = setup.getMethod("updateState", UUID.class, state);
            createOrUpdate = setup.getMethod("createOrUpdate", AbstractClientPlayer.class, state, Boolean.TYPE);
            getSkinImage = setup.getDeclaredMethod("getSkinImage", ResourceLocation.class);
            getSkinImage.setAccessible(true);
            buildMesh = setup.getDeclaredMethod(
                "buildMesh",
                skinData,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Boolean.TYPE,
                Float.TYPE);
            buildMesh.setAccessible(true);

            skinDataConstructor = skinData.getConstructor(BufferedImage.class);
            stateInitialized = state.getField("initialized");
            stateSlim = state.getField("slim");
            stateSkinLocation = state.getField("lastSkinLocation");
            stateHatMesh = state.getField("hatMesh");
            stateJacketMesh = state.getField("jacketMesh");
            stateRightSleeveMesh = state.getField("rightSleeveMesh");
            stateLeftSleeveMesh = state.getField("leftSleeveMesh");
            stateRightPantsMesh = state.getField("rightPantsMesh");
            stateLeftPantsMesh = state.getField("leftPantsMesh");

            meshRender = mesh.getMethod(
                "render",
                Float.TYPE,
                Float.TYPE,
                Float.TYPE,
                Float.TYPE,
                Float.TYPE,
                Float.TYPE,
                Float.TYPE);
            meshCleanup = mesh.getMethod("cleanup");
            meshIsCompiled = mesh.getMethod("isCompiled");
            meshSetPosition = mesh.getMethod("setPosition", Float.TYPE, Float.TYPE, Float.TYPE);
            meshSetOffset = mesh.getMethod("setOffset", Float.TYPE, Float.TYPE, Float.TYPE);
            meshSetRotation = mesh.getMethod("setRotation", Float.TYPE, Float.TYPE, Float.TYPE);
            available = true;
        } catch (ReflectiveOperationException ignored) {
            available = false;
        }
    }

    public static final class Layers {
        private final Object sourceState;
        private final boolean slim;
        public Object hat;
        public Object jacket;
        public Object rightArm;
        public Object rightForeArm;
        public Object leftArm;
        public Object leftForeArm;
        public Object rightLeg;
        public Object rightForeLeg;
        public Object leftLeg;
        public Object leftForeLeg;

        private Layers(Object sourceState, boolean slim) {
            this.sourceState = sourceState;
            this.slim = slim;
        }

        public boolean hasRightSleeve() {
            return getConfigBoolean(enableRightSleeve3D) && rightArm != null && rightForeArm != null;
        }

        public boolean hasLeftSleeve() {
            return getConfigBoolean(enableLeftSleeve3D) && leftArm != null && leftForeArm != null;
        }

        public boolean hasRightPants() {
            return getConfigBoolean(enableRightPants3D) && rightLeg != null && rightForeLeg != null;
        }

        public boolean hasLeftPants() {
            return getConfigBoolean(enableLeftPants3D) && leftLeg != null && leftForeLeg != null;
        }

        public boolean hasHat() {
            return getConfigBoolean(enableHat3D) && hat != null;
        }

        public boolean hasJacket() {
            return getConfigBoolean(enableJacket3D) && jacket != null;
        }

        private void cleanup() {
            cleanupMesh(rightArm);
            cleanupMesh(rightForeArm);
            cleanupMesh(leftArm);
            cleanupMesh(leftForeArm);
            cleanupMesh(rightLeg);
            cleanupMesh(rightForeLeg);
            cleanupMesh(leftLeg);
            cleanupMesh(leftForeLeg);
        }

        private static void cleanupMesh(Object mesh) {
            if (mesh == null || meshCleanup == null) {
                return;
            }
            try {
                meshCleanup.invoke(mesh);
            } catch (ReflectiveOperationException ignored) {
                // The owning GL context is shutting down or WawelAuth changed its implementation.
            }
        }
    }
}
