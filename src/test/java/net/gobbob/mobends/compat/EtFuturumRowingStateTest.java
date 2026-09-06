package net.gobbob.mobends.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.MathHelper;
import org.junit.jupiter.api.Test;

class EtFuturumRowingStateTest {
    @Test
    void idlePaddlesStartInTheAdvancedPoseWithoutLeaningTheTorso() {
        EtFuturumRowingState.Pose pose = new EtFuturumRowingState().sample(10.0, false, 0.0f, false, 0.0f);
        assertEquals(EtFuturumPaddlePose.REST_PHASE, pose.leftPhase);
        assertEquals(EtFuturumPaddlePose.REST_PHASE, pose.rightPhase);
        assertEquals(0.0f, pose.leftStroke);
        assertEquals(0.0f, pose.rightStroke);
    }

    @Test
    void steadyRowingKeepsEfrTimingAndIndependentPaddles() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        state.sample(10.0, true, 0.4f, true, 1.2f);
        EtFuturumRowingState.Pose pose = state.sample(10.5, true, 0.6f, true, 1.4f);
        assertEquals(0.6f, pose.leftPhase);
        assertEquals(1.4f, pose.rightPhase);
        assertEquals(MathHelper.sin(0.6f), pose.leftStroke);
        assertEquals(MathHelper.sin(1.4f), pose.rightStroke);
    }

    @Test
    void stoppingEasesBothTheOarAndTorsoOverSixTicks() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        state.sample(10.0, true, 4.0f, true, 4.0f);
        EtFuturumRowingState.Pose first = state.sample(10.1, false, 0.0f, false, 0.0f);
        assertEquals(4.0f, first.leftPhase, 0.01f);
        assertEquals(MathHelper.sin(4.0f), first.leftStroke, 0.01f);
        EtFuturumRowingState.Pose middle = state.sample(13.0, false, 0.0f, false, 0.0f);
        assertEquals(3.0f, middle.leftPhase, 0.0001f);
        assertEquals(MathHelper.sin(4.0f) / 2.0f, middle.leftStroke, 0.0001f);
        EtFuturumRowingState.Pose end = state.sample(16.0, false, 0.0f, false, 0.0f);
        assertEquals(EtFuturumPaddlePose.REST_PHASE, end.leftPhase);
        assertEquals(0.0f, end.leftStroke);
    }

    @Test
    void stoppingOnePaddleDoesNotChangeTheOtherPaddlesTiming() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        state.sample(10.0, true, 4.0f, true, 4.0f);
        EtFuturumRowingState.Pose pose = state.sample(13.0, false, 0.0f, true, 5.2f);
        assertEquals(3.0f, pose.leftPhase, 0.0001f);
        assertEquals(5.2f, pose.rightPhase);
    }

    @Test
    void restartingBlendsFromRestAndCatchesUpWithEfr() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        state.sample(10.0, false, 0.0f, false, 0.0f);
        EtFuturumRowingState.Pose first = state.sample(10.1, true, 0.0f, true, 0.0f);
        assertEquals(EtFuturumPaddlePose.REST_PHASE, first.leftPhase, 0.01f);
        EtFuturumRowingState.Pose end = state.sample(14.0, true, 1.5f, true, 1.5f);
        assertEquals(1.5f, end.leftPhase);
        assertEquals(MathHelper.sin(1.5f), end.leftStroke);
    }

    @Test
    void accumulatedCyclesTakeTheShortPathToRest() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        float phase = (float)(40.0 * Math.PI + 6.0);
        state.sample(10.0, true, phase, true, phase);
        EtFuturumRowingState.Pose middle = state.sample(13.0, false, 0.0f, false, 0.0f);
        assertTrue(middle.leftPhase > 0.8f && middle.leftPhase < 0.9f);
    }

    @Test
    void multipleRenderPassesShareOnePoseAndDoNotAdvanceTime() {
        EtFuturumRowingState state = new EtFuturumRowingState();
        EtFuturumRowingState.Pose pose = state.sample(10.0, true, 0.4f, true, 0.6f);
        assertSame(pose, state.sample(10.0, true, 0.4f, true, 0.6f));
        assertSame(pose, state.sample(9.9, true, 0.3f, true, 0.5f));
    }
}
