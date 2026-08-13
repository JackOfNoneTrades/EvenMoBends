package net.gobbob.mobends.config;

import net.minecraftforge.common.config.Configuration;

/** Client-side controls for opt-in player and mob blinking. */
public final class BlinkConfig {
    private static final String CATEGORY = "blinking";

    private static boolean enabled = true;
    private static boolean players = true;
    private static boolean mobs = true;
    private static boolean smoothEyelids = true;
    private static int frequency = 150;
    private static int length = 1;

    private BlinkConfig() {}

    public static void load(Configuration config) {
        enabled = config.get(
            CATEGORY,
            "enabled",
            true,
            "Master switch for opt-in player and mob blinking.").getBoolean(true);
        players = config.get(
            CATEGORY,
            "players",
            true,
            "Blink for player skins carrying compatible embedded blink data.").getBoolean(true);
        mobs = config.get(
            CATEGORY,
            "mobs",
            true,
            "Blink pigs, including Angelica Random Mobs pig variants.").getBoolean(true);
        smoothEyelids = config.get(
            CATEGORY,
            "smoothEyelids",
            true,
            "Ease player and mob eyelids closed and open instead of switching blink textures instantly.")
            .getBoolean(true);
        frequency = config.getInt(
            "frequency",
            CATEGORY,
            150,
            1,
            1200,
            "Base interval in ticks. Each entity receives a stable interval between this and three times this value.");
        length = config.getInt(
            "length",
            CATEGORY,
            1,
            1,
            20,
            "Length in ticks of each half of a blink.");
    }

    public static void save(Configuration config) {
        config.get(CATEGORY, "enabled", true).set(enabled);
        config.get(CATEGORY, "players", true).set(players);
        config.get(CATEGORY, "mobs", true).set(mobs);
        config.get(CATEGORY, "smoothEyelids", true).set(smoothEyelids);
        config.get(CATEGORY, "frequency", 150).set(frequency);
        config.get(CATEGORY, "length", 1).set(length);
    }

    public static boolean arePlayersEnabled() {
        return enabled && players;
    }

    public static boolean areMobsEnabled() {
        return enabled && mobs;
    }

    public static boolean areSmoothEyelidsEnabled() {
        return enabled && smoothEyelids;
    }

    public static int getFrequency() {
        return frequency;
    }

    public static int getLength() {
        return length;
    }
}
