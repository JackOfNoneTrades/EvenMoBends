package net.gobbob.mobends.animation.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.compat.EtFuturumPaddlePose;
import net.gobbob.mobends.compat.EtFuturumRowingState;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector3f;

class BoatRowingKinematicsTest {
    @Test
    void handsReachHandlesThroughFullStrokesForBothSkinWidthsAndSeatOffsets() {
        for (int skin = 0; skin < 3; skin++) {
            boolean slim = skin == 2;
            ModelBendsPlayer model = new ModelBendsPlayer();
            if (skin != 0) model.initModern();
            model.setSlim(slim);
            for (boolean raft : new boolean[] {false, true}) {
                // EFR moves the driver forward with a passenger or a chest aboard.
                for (float seat : new float[] {0.0f, 0.2f}) {
                    for (int mode = 0; mode < 3; mode++) {
                        assertStroke(model, skin, raft, seat, mode);
                    }
                }
            }
        }
    }

    private static void assertStroke(ModelBendsPlayer model, int skin, boolean raft, float seat, int mode) {
        for (int step = 0; step <= 128; step++) {
            float phase = (float)(step * Math.PI / 64.0);
            float lean = Animation_Rowing.bodyLean(true, raft,
                mode != 2 ? MathHelper.sin(phase) : 0.0f, mode != 1 ? MathHelper.sin(phase) : 0.0f);
            for (int side = 0; side < 2; side++) {
                float paddlePhase = mode == 0 || mode - 1 == side ? phase : EtFuturumPaddlePose.REST_PHASE;
                assertHandAtPaddle(model, skin, raft, seat, lean, side, paddlePhase);
            }
        }
    }

    private static void assertHandAtPaddle(ModelBendsPlayer model, int skin, boolean raft, float seat,
        float lean, int side, float paddlePhase) {
        Vector3f target = BoatRowingKinematics.playerGrip(
            BoatRowingKinematics.paddleGrip(paddlePhase, side, raft),
            0.0f, 0.0f, 0.0, raft ? 0.2 : 0.5, -seat, Animation_Rowing.seatLift(raft));
        ModelRenderer arm = side == 0 ? model.bipedLeftArm : model.bipedRightArm;
        ModelRenderer forearm = side == 0 ? model.bipedLeftForeArm : model.bipedRightForeArm;
        Animation_Rowing.reach(model, arm, forearm, new Vector3f(target), lean);
        Vector3f hand = handPosition(model, arm, forearm, lean, skin == 2);
        // A tenth of a skin pixel keeps the shaft well inside the hand,
        // including the small clamp at the elbow's maximum bend.
        assertEquals(0.0, Vector3f.sub(hand, target, null).length(), 0.1,
            "skin=" + skin + " raft=" + raft + " seat=" + seat + " phase=" + paddlePhase + " side=" + side + " lean=" + lean);
    }

    @Test
    void rowingRocksAroundTheHipsAndPassengersStayUpright() {
        assertEquals(18.0f, Animation_Rowing.bodyLean(true, false, 1.0f, 1.0f), 0.001);
        assertEquals(-18.0f, Animation_Rowing.bodyLean(true, false, -1.0f, -1.0f), 0.001);
        assertEquals(9.0f, Animation_Rowing.bodyLean(true, false, 1.0f, 0.0f), 0.001);
        assertEquals(0.0f, Animation_Rowing.bodyLean(true, false, 0.0f, 0.0f), 0.001);
        assertEquals(0.0f, Animation_Rowing.bodyLean(false, true, 1.0f, 1.0f), 0.001);
    }

    @Test
    void handsFollowPaddlesWhileSettlingFromAnyPartOfTheStroke() {
        for (int skin = 0; skin < 3; skin++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            if (skin != 0) model.initModern();
            model.setSlim(skin == 2);
            for (boolean raft : new boolean[] {false, true}) {
                for (float seat : new float[] {0.0f, 0.2f}) {
                    for (int step = 0; step < 32; step++) {
                        EtFuturumRowingState state = new EtFuturumRowingState();
                        float phase = (float)(step * Math.PI / 16.0);
                        state.sample(0.0, true, phase, true, phase);
                        for (int frame = 1; frame <= 48; frame++) {
                            EtFuturumRowingState.Pose pose = state.sample(frame / 8.0, false, 0.0f, false, 0.0f);
                            float lean = Animation_Rowing.bodyLean(true, raft, pose.leftStroke, pose.rightStroke);
                            assertHandAtPaddle(model, skin, raft, seat, lean, 0, pose.leftPhase);
                            assertHandAtPaddle(model, skin, raft, seat, lean, 1, pose.rightPhase);
                        }
                    }
                }
            }
        }
    }

    @Test
    void handsFollowPaddlesWhileStartingForwardOrTurning() {
        for (int skin = 0; skin < 3; skin++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            if (skin != 0) model.initModern();
            model.setSlim(skin == 2);
            for (boolean raft : new boolean[] {false, true}) {
                for (float seat : new float[] {0.0f, 0.2f}) {
                    for (int mode = 0; mode < 3; mode++) {
                        EtFuturumRowingState state = new EtFuturumRowingState();
                        state.sample(0.0, false, 0.0f, false, 0.0f);
                        for (int frame = 1; frame <= 48; frame++) {
                            float phase = (float)((frame / 8.0 - 1.0) * Math.PI / 8.0);
                            EtFuturumRowingState.Pose pose = state.sample(frame / 8.0,
                                mode != 2, mode != 2 ? phase : 0.0f, mode != 1, mode != 1 ? phase : 0.0f);
                            float lean = Animation_Rowing.bodyLean(true, raft, pose.leftStroke, pose.rightStroke);
                            assertHandAtPaddle(model, skin, raft, seat, lean, 0, pose.leftPhase);
                            assertHandAtPaddle(model, skin, raft, seat, lean, 1, pose.rightPhase);
                        }
                    }
                }
            }
        }
    }

    @Test
    void advancedBoatRestPoseOpensTheElbowsMoreThanEfrsOriginalRestPose() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        for (float seat : new float[] {0.0f, 0.2f}) {
            for (int side = 0; side < 2; side++) {
                ModelRendererBends forearm = (ModelRendererBends)(side == 0 ? model.bipedLeftForeArm : model.bipedRightForeArm);
                assertHandAtPaddle(model, 0, false, seat, 0.0f, side, 0.0f);
                float originalBend = Math.abs(forearm.rotation.getX());
                assertHandAtPaddle(model, 0, false, seat, 0.0f, side, EtFuturumPaddlePose.REST_PHASE);
                assertTrue(Math.abs(forearm.rotation.getX()) < originalBend);
            }
        }
    }

    @Test
    void liftingTheSeatCompensatesTheHandTargetsByTheSameAmount() {
        for (boolean raft : new boolean[] {false, true}) {
            float lift = Animation_Rowing.seatLift(raft);
            assertTrue(lift > 0.0f);
            Vector3f original = BoatRowingKinematics.playerGrip(
                BoatRowingKinematics.paddleGrip(1.0f, 0, raft), 0.0f, 0.0f, 0.0, 0.5, 0.0, 0.0f);
            Vector3f raised = BoatRowingKinematics.playerGrip(
                BoatRowingKinematics.paddleGrip(1.0f, 0, raft), 0.0f, 0.0f, 0.0, 0.5, 0.0, lift);
            assertEquals(original.x, raised.x, 0.0001);
            assertEquals(original.z, raised.z, 0.0001);
            assertEquals(original.y + (raft ? 0.5f : 2.0f), raised.y, 0.0001);
        }
    }

    @Test
    void paddleSidesAndRaftOffsetsMatchEfrGeometry() {
        for (int step = 0; step <= 32; step++) {
            float phase = (float)(step * Math.PI / 16.0);
            Vector3f left = BoatRowingKinematics.paddleGrip(phase, 0, false);
            Vector3f right = BoatRowingKinematics.paddleGrip(phase, 1, false);
            assertTrue(left.x > 0.0f);
            assertEquals(-left.x, right.x, 0.0001);
            Vector3f raftLeft = BoatRowingKinematics.paddleGrip(phase, 0, true);
            Vector3f raftRight = BoatRowingKinematics.paddleGrip(phase, 1, true);
            assertEquals(left.y + 1.0f, raftLeft.y, 0.0001);
            assertEquals(right.x + 1.0f, raftRight.x, 0.0001);
            assertEquals(0.0, Vector3f.sub(left,
                BoatRowingKinematics.paddleGrip(phase + (float)(2 * Math.PI), 0, false), null).length(), 0.001);
        }
    }

    @Test
    void seatDisplacementRotatesWithBoatAndDoesNotDependOnWorldHeading() {
        Vector3f expected = BoatRowingKinematics.playerGrip(
            BoatRowingKinematics.paddleGrip(1.0f, 0, false), 0.0f, 0.0f, 0.0, 0.5, -0.2, Animation_Rowing.seatLift(false));
        for (int yaw = -360; yaw <= 360; yaw += 15) {
            double radians = Math.toRadians(yaw);
            Vector3f actual = BoatRowingKinematics.playerGrip(
                BoatRowingKinematics.paddleGrip(1.0f, 0, false), 0.0f, yaw,
                0.2 * Math.sin(radians), 0.5, -0.2 * Math.cos(radians), Animation_Rowing.seatLift(false));
            assertEquals(0.0, Vector3f.sub(expected, actual, null).length(), 0.0001);
        }
    }

    @Test
    void unreachableOrCoincidentHandlesDoNotProduceInvalidRotations() {
        for (Vector3f target : new Vector3f[] {new Vector3f(), new Vector3f(100.0f, 100.0f, 100.0f),
            new Vector3f(0.0f, 0.0f, -5.0f)}) {
            Vector3f angles = BoatRowingKinematics.solveArm(target, -3.0f, 2.0f);
            assertTrue(Float.isFinite(angles.x));
            assertTrue(Float.isFinite(angles.y));
            assertTrue(Float.isFinite(angles.z));
            assertTrue(angles.z >= -130.0f && angles.z <= 0.0f);
        }
    }

    private static Vector3f handPosition(ModelBendsPlayer model, ModelRenderer arm, ModelRenderer forearm,
        float lean, boolean slim) {
        Vector3f hand = new Vector3f(slim ? 1.5f : 2.0f, 5.0f, -2.0f);
        rotate(hand, forearm);
        translate(hand, forearm);
        rotate(hand, arm);
        translate(hand, arm);
        BoatRowingKinematics.rotateX(hand, (float)Math.toRadians(lean));
        translate(hand, model.bipedBody);
        return hand;
    }

    private static void rotate(Vector3f point, ModelRenderer part) {
        ModelRendererBends bends = (ModelRendererBends)part;
        BoatRowingKinematics.rotateX(point, (float)Math.toRadians(bends.rotation.getX()));
        BoatRowingKinematics.rotateY(point, (float)Math.toRadians(bends.rotation.getY()));
        BoatRowingKinematics.rotateZ(point, (float)Math.toRadians(bends.rotation.getZ()));
    }

    private static void translate(Vector3f point, ModelRenderer part) {
        point.translate(part.rotationPointX, part.rotationPointY, part.rotationPointZ);
    }
}
