package net.gobbob.mobends.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.gobbob.mobends.compat.AquaAcrobaticsCompat.PoseApi;
import net.gobbob.mobends.compat.AquaAcrobaticsCompat.State;
import org.junit.jupiter.api.Test;

class AquaAcrobaticsCompatTest {
    @Test
    void noOptionalModDoesNotClaimThePose() {
        assertFalse(State.UNAVAILABLE.available);
        assertFalse(State.UNAVAILABLE.isActive());
    }

    @Test
    void swimmingAndCrawlingUseTheSameAuthoritativePronePose() {
        State swim = State.fromPose("SWIMMING", 1, true, false);
        State crawl = State.fromPose("SWIMMING", 1, false, false);
        assertTrue(swim.isActive());
        assertTrue(crawl.isActive());
        assertTrue(swim.inWater);
        assertFalse(crawl.inWater);
        // No velocity or onGround test: stopping and touching the bottom must not change the pose.
        assertTrue(swim.prone);
        assertTrue(crawl.prone);
    }

    @Test
    void suppressesTheOldBodyTransformOnTheVeryFirstProneFrame() {
        assertTrue(State.fromPose("SWIMMING", 0, true, false).isActive());
    }

    @Test
    void keepsOwnershipUntilTheStandUpTransitionHasFinished() {
        assertTrue(State.fromPose("STANDING", 0.5f, false, false).isActive());
        assertTrue(State.fromPose("CROUCHING", 0.01f, false, false).isActive());
        assertFalse(State.fromPose("STANDING", 0, false, false).isActive());
    }

    @Test
    void doesNotMistakeFlightSleepDeathOrMountingForCrawling() {
        for (String pose : new String[] {"FALL_FLYING", "SLEEPING", "DYING"}) {
            assertFalse(State.fromPose(pose, 1, false, false).isActive());
        }
        assertFalse(State.fromPose("SWIMMING", 1, true, true).isActive());
    }

    @Test
    void clampsTransitionWeight() {
        assertEquals(1, State.fromPose("SWIMMING", 2, true, false).blend);
        assertEquals(0, State.fromPose("STANDING", -1, true, false).blend);
        assertEquals(0, State.fromPose("STANDING", Float.NaN, true, false).blend);
    }

    @Test
    void optionalApiUsesEnumNamesAndForwardsPartialTicks() throws ReflectiveOperationException {
        PoseApi api = new PoseApi(TestPlayer.class);
        TestPlayer player = new TestPlayer();
        State state = api.read(player, 0.25f, true, false);
        assertEquals(0.25f, player.partialTicks);
        assertEquals(0.75f, state.blend);
        assertTrue(state.prone);
        assertTrue(state.available);
    }

    @Test
    void optionalApiIgnoresUnpatchedPlayers() throws ReflectiveOperationException {
        assertSame(State.UNAVAILABLE, new PoseApi(TestPlayer.class).read(new Object(), 0, true, false));
    }

    public enum TestPose {
        SWIMMING;

        @Override
        public String toString() {
            return "localized swimming";
        }
    }

    public static class TestPlayer {
        float partialTicks;

        public TestPose getPose() {
            return TestPose.SWIMMING;
        }

        public float getSwimAnimation(float partialTicks) {
            this.partialTicks = partialTicks;
            return 0.75f;
        }
    }
}
