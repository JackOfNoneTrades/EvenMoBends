package net.gobbob.mobends.compat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;

/** Reflection bridge to Wawel Auth's existing active-account skin upload functions. */
public final class WawelAuthSkinUpload {
    private static final String CLIENT_CLASS =
        "org.fentanylsolutions.wawelauth.wawelclient.WawelClient";
    private static final String TEXTURE_TYPE_CLASS =
        "org.fentanylsolutions.wawelauth.wawelcore.data.TextureType";
    private static final String BUILTIN_PROVIDERS_CLASS =
        "org.fentanylsolutions.wawelauth.wawelclient.BuiltinProviders";
    private static final String SKIN_LAYERS_SETUP_CLASS =
        "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DSetup";

    private WawelAuthSkinUpload() {}

    public static Account activeAccount() {
        if (!WawelAuthCompat.isLoaded()) {
            return null;
        }
        try {
            Object client = client();
            if (client == null) return null;
            Object sessionBridge = client.getClass().getMethod("getSessionBridge").invoke(client);
            Object account = sessionBridge.getClass().getMethod("getActiveAccount").invoke(sessionBridge);
            if (account == null) return null;
            Class<?> type = account.getClass();
            return new Account(
                ((Number)type.getMethod("getId").invoke(account)).longValue(),
                (String)type.getMethod("getProfileName").invoke(account),
                (UUID)type.getMethod("getProfileUuid").invoke(account),
                (String)type.getMethod("getProviderName").invoke(account));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static void upload(Account account, BufferedImage image, boolean slim, Completion completion) {
        if (account == null || image == null) {
            completion.finished(null, "No active Wawel Auth account or generated skin is available.");
            return;
        }
        File file = null;
        boolean persistent = false;
        try {
            persistent = isOfflineProvider(account.provider);
            file = persistent ? offlineSkinFile(account) : File.createTempFile("evenmobends-skin-", ".png");
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Could not create " + parent.getAbsolutePath());
            }
            if (!ImageIO.write(image, "png", file)) {
                throw new IllegalStateException("No PNG writer is available.");
            }

            Object client = client();
            if (client == null) throw new IllegalStateException("Wawel Auth client is not running.");
            Object manager = client.getClass().getMethod("getAccountManager").invoke(client);
            Class<?> textureType = Class.forName(TEXTURE_TYPE_CLASS);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object skin = Enum.valueOf((Class<? extends Enum>)textureType.asSubclass(Enum.class), "SKIN");
            Method upload = manager.getClass()
                .getMethod("uploadTexture", long.class, textureType, File.class, boolean.class);
            Object result = upload.invoke(manager, account.id, skin, file, slim);
            if (!(result instanceof CompletableFuture)) {
                throw new IllegalStateException("Wawel Auth returned an unsupported upload result.");
            }
            final File uploadedFile = file;
            final boolean keepFile = persistent;
            ((CompletableFuture<?>)result).whenComplete((message, error) -> {
                if (!keepFile) uploadedFile.delete();
                Throwable cause = unwrap(error);
                Minecraft.getMinecraft().func_152344_a(() -> {
                    if (cause == null) {
                        invalidate(client, account.uuid);
                        completion.finished(message == null ? "Uploaded skin." : String.valueOf(message), null);
                    } else {
                        completion.finished(null, usefulMessage(cause));
                    }
                });
            });
        } catch (Throwable error) {
            if (file != null && !persistent) file.delete();
            completion.finished(null, usefulMessage(unwrap(error)));
        }
    }

    private static Object client() throws ReflectiveOperationException {
        return Class.forName(CLIENT_CLASS).getMethod("instance").invoke(null);
    }

    private static boolean isOfflineProvider(String provider) {
        try {
            return Boolean.TRUE.equals(
                Class.forName(BUILTIN_PROVIDERS_CLASS)
                    .getMethod("isOfflineProvider", String.class)
                    .invoke(null, provider));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static File offlineSkinFile(Account account) {
        File directory = new File(Minecraft.getMinecraft().mcDataDir, "config/wawelauth/evenmobends-skins");
        String identity = account.uuid == null ? Long.toString(account.id) : account.uuid.toString();
        return new File(directory, identity + ".png");
    }

    private static void invalidate(Object client, UUID uuid) {
        if (uuid == null) return;
        try {
            Object resolver = client.getClass().getMethod("getTextureResolver").invoke(client);
            resolver.getClass().getMethod("invalidate", UUID.class).invoke(resolver, uuid);
        } catch (ReflectiveOperationException ignored) {
            // The upload still succeeded; a reconnect will refresh older Wawel Auth versions.
        }
        try {
            Class<?> setup = Class.forName(SKIN_LAYERS_SETUP_CLASS);
            Class<?> state = Class.forName(
                "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DState");
            setup.getMethod("updateState", UUID.class, state).invoke(null, uuid, null);
            Class<?> skull = Class.forName(
                "org.fentanylsolutions.wawelauth.client.render.skinlayers.SkinLayers3DSetup$SkullMeshCache");
            setup.getMethod("updateSkullCache", UUID.class, skull).invoke(null, uuid, null);
        } catch (ReflectiveOperationException ignored) {
            // Optional cache cleanup for Wawel Auth versions with 3D skin layers.
        }
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while ((current instanceof InvocationTargetException && current.getCause() != null)
            || (current instanceof java.util.concurrent.CompletionException && current.getCause() != null)) {
            current = current.getCause();
        }
        return current;
    }

    private static String usefulMessage(Throwable error) {
        if (error == null) return "Unknown upload error.";
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }

    public interface Completion {
        void finished(String result, String error);
    }

    public static final class Account {
        public final long id;
        public final String name;
        public final UUID uuid;
        public final String provider;

        private Account(long id, String name, UUID uuid, String provider) {
            this.id = id;
            this.name = name == null ? "?" : name;
            this.uuid = uuid;
            this.provider = provider == null ? "?" : provider;
        }
    }
}
