package net.gobbob.mobends.animation.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.renderer.SwimmingItemTransform;
import net.gobbob.mobends.client.renderer.SwimmingArmClearance;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelRenderer;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

class SwimmingHeldItemPoseTest {
    @Test
    void clearancePreservesTheOriginalJointCyclesWithEitherOrBothHandsOccupied() {
        for (int mode = 0; mode < 3; mode++) {
            for (int hands = 1; hands <= 3; hands++) {
                ModelBendsPlayer empty = new ModelBendsPlayer();
                ModelBendsPlayer carrying = new ModelBendsPlayer();
                carrying.heldItemRight = hands & 1;
                carrying.heldItemLeft = (hands >> 1) & 1;
                for (int step = 0; step < 64; step++) {
                    float phase = (float)(step * Math.PI / 32);
                    animate(empty, mode, phase);
                    animate(carrying, mode, phase);
                    assertPoseEquals(empty.bipedBody, carrying.bipedBody);
                    assertPoseEquals(empty.bipedHead, carrying.bipedHead);
                    assertPoseEquals(empty.bipedRightLeg, carrying.bipedRightLeg);
                    assertPoseEquals(empty.bipedLeftLeg, carrying.bipedLeftLeg);
                    for (boolean left : new boolean[] {false, true}) {
                        ModelRenderer arm = left ? carrying.bipedLeftArm : carrying.bipedRightArm;
                        ModelRenderer forearm = left ? carrying.bipedLeftForeArm : carrying.bipedRightForeArm;
                        assertPoseEquals(left ? empty.bipedLeftArm : empty.bipedRightArm, arm);
                        assertPoseEquals(left ? empty.bipedLeftForeArm : empty.bipedRightForeArm, forearm);
                    }
                    assertEquals(0, carrying.renderItemRotation.vFinal.lengthSquared());
                }
            }
        }
    }

    @Test
    void toolsFollowTheStrokeAndRetainTheirNormalAngleWhenClear() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.heldItemRight = model.heldItemLeft = 1;
        int[] normalFrames = new int[2];
        int[] adjustedFrames = new int[2];
        float[] lowest = {1, 1};
        float[] highest = {-1, -1};
        for (int mode = 0; mode < 3; mode++) {
            for (int step = 0; step < 64; step++) {
                animate(model, mode, (float)(step * Math.PI / 32));
                for (int hand = 0; hand < 2; hand++) {
                    boolean left = hand == 1;
                    Matrix4f correction = SwimmingItemTransform.matrix(model, left, 1);
                    float angle = left ? model.swimmingLeftItemAdjustment.w : model.swimmingRightItemAdjustment.w;
                    if (angle == 0) {
                        normalFrames[hand]++;
                        Vector4f tip = new Vector4f(-6, 12, 3, 1);
                        assertEquals(tip, Matrix4f.transform(correction, tip, null),
                            "a clear tool must retain its normal attachment");
                    } else adjustedFrames[hand]++;
                    Matrix4f world = new Matrix4f();
                    rotate(world, -model.swimmingItemPose.getZ(), 1, 0, 0);
                    rotate(world, -model.renderRotation.getX(), 1, 0, 0);
                    world.scale(new Vector3f(-1, -1, 1));
                    Matrix4f.mul(world, matrix(model.bipedBody), world);
                    Matrix4f.mul(world, armMatrix(model, left), world);
                    Matrix4f.mul(world, forearmMatrix(model, left), world);
                    Matrix4f.mul(world, correction, world);
                    if (left) world.scale(new Vector3f(-1, 1, 1));
                    Matrix4f.mul(world, vanillaTool(), world);
                    Vector4f blade = Matrix4f.transform(world, new Vector4f(-1, 1, 0, 0), null);
                    float up = blade.y / blade.length();
                    lowest[hand] = Math.min(lowest[hand], up);
                    highest[hand] = Math.max(highest[hand], up);
                }
            }
        }
        for (int hand = 0; hand < 2; hand++) {
            assertTrue(normalFrames[hand] > 20 && adjustedFrames[hand] > 20);
            assertTrue(highest[hand] - lowest[hand] > 0.5f, "tools should follow the stroke, not remain vertical");
        }
    }

    @Test
    void stoppingTravelRestoresNormalToolsInEverySwimmingMode() {
        for (int mode = 0; mode < 3; mode++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            model.heldItemRight = model.heldItemLeft = 1;
            animate(model, mode, 0);
            if (mode == 2) Animation_AquaAcrobatics.apply(model, 1, true, 0, 0, true);
            else if (mode == 1) Animation_Diving.apply(model, 0, new Vector3f(0, 0.02f, 0));
            else Animation_Swimming.apply(model, 0, false);
            finishPose(model);
            assertEquals(0, model.swimmingItemPose.getX());
            assertEquals(0, model.swimmingItemPose.getY());
            assertEquals(0, model.swimmingRightArmAdjustment.w);
            assertEquals(0, model.swimmingLeftArmAdjustment.w);
            assertEquals(0, model.swimmingRightItemAdjustment.w);
            assertEquals(0, model.swimmingLeftItemAdjustment.w);
            for (boolean left : new boolean[] {false, true}) {
                Vector4f tip = new Vector4f(-6, 12, 3, 1);
                assertEquals(tip, Matrix4f.transform(SwimmingItemTransform.matrix(model, left, 1), tip, null));
            }
        }
    }

    @Test
    void stoppingBlendsTheWholeCorrectionBackToNormal() {
        ModelBendsPlayer full = new ModelBendsPlayer();
        ModelBendsPlayer halfway = new ModelBendsPlayer();
        full.heldItemRight = full.heldItemLeft = 1;
        halfway.heldItemRight = halfway.heldItemLeft = 1;
        animate(full, 0, 0);
        animate(halfway, 0, 0);
        halfway.swimmingRightArmAdjustment.set(0, 0, 0, 0);
        halfway.swimmingLeftArmAdjustment.set(0, 0, 0, 0);
        halfway.swimmingElbowAdjustment.set(0, 0, 0);
        halfway.swimmingRightItemAdjustment.set(0, 0, 0, 0);
        halfway.swimmingLeftItemAdjustment.set(0, 0, 0, 0);
        halfway.swimmingItemPose.setX(0.5f);
        halfway.swimmingItemPose.setY(0.5f);
        SwimmingArmClearance.update(halfway);
        assertEquals(full.swimmingRightArmAdjustment.w * 0.5f, halfway.swimmingRightArmAdjustment.w, 0.001);
        assertEquals(full.swimmingLeftArmAdjustment.w * 0.5f, halfway.swimmingLeftArmAdjustment.w, 0.001);
        assertEquals(full.swimmingRightItemAdjustment.w * 0.5f, halfway.swimmingRightItemAdjustment.w, 0.001);
        assertEquals(full.swimmingLeftItemAdjustment.w * 0.5f, halfway.swimmingLeftItemAdjustment.w, 0.001);
    }

    @Test
    void swimmingClearsTurnedHeadsWithEitherHandAndBothSkinWidths() throws IOException {
        Matrix4f item = vanillaTool();
        List<Vector4f> vertices = toolVertices();
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.initModern();
        model.heldItemRight = model.heldItemLeft = 1;
        for (boolean slim : new boolean[] {false, true}) {
            model.setSlim(slim);
            for (int mode = 0; mode < 3; mode++) {
                for (float pitch : new float[] {-60, -30, 0, 30, 60}) {
                    for (float yaw : new float[] {-60, -30, 0, 30, 60}) {
                        model.headRotationX = pitch;
                        model.headRotationY = yaw;
                        for (int step = 0; step < 64; step++) {
                            animate(model, mode, (float)(step * Math.PI / 32));
                            Matrix4f inverseHead = Matrix4f.invert(matrix(model.bipedHead), null);
                            for (boolean left : new boolean[] {false, true}) {
                                ModelRenderer forearm = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
                                Matrix4f tool = Matrix4f.mul(armMatrix(model, left), forearmMatrix(model, left), null);
                                tool.translate(new Vector3f(-forearm.rotationPointX, -forearm.rotationPointY, -forearm.rotationPointZ));
                                Matrix4f.mul(tool, SwimmingItemTransform.matrix(model, left, 1), tool);
                                // Backhand mirrors X before using vanilla's item placement.
                                if (left) tool.scale(new Vector3f(-1, 1, 1));
                                tool.translate(new Vector3f(-1, 7, 1));
                                Matrix4f.mul(tool, item, tool);
                                Matrix4f.mul(inverseHead, tool, tool);
                                for (Vector4f vertex : vertices) {
                                    Vector4f p = Matrix4f.transform(tool, vertex, null);
                                    boolean inside = Math.abs(p.x) < 4.5f && Math.abs(p.y + 4) < 4.5f && Math.abs(p.z) < 4.5f;
                                    assertFalse(inside, "mode=" + mode + " phase=" + step + " left=" + left + " slim=" + slim + " pitch=" + pitch + " yaw=" + yaw + " correction="
                                        + (left ? model.swimmingLeftArmAdjustment.w : model.swimmingRightArmAdjustment.w));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void tiltingToolsKeepsTheOriginalGripInsideThePalm() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.initModern();
        model.heldItemRight = model.heldItemLeft = 1;
        Vector4f handle = Matrix4f.transform(vanillaTool(), new Vector4f(13, 3, -0.5f, 1), null);
        for (boolean slim : new boolean[] {false, true}) {
            model.setSlim(slim);
            for (int mode = 0; mode < 3; mode++) {
                for (int step = 0; step < 64; step++) {
                    animate(model, mode, (float)(step * Math.PI / 32));
                    for (boolean left : new boolean[] {false, true}) {
                        ModelRenderer forearm = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
                        for (float scale : new float[] {1, 0.0625f}) {
                            Vector4f grip = new Vector4f((left ? -1 : 1) * (-1 + handle.x) * scale,
                                (7 + handle.y) * scale, (1 + handle.z) * scale, 1);
                            Vector4f placed = Matrix4f.transform(SwimmingItemTransform.matrix(model, left, scale), grip, null);
                            assertEquals(grip.x, placed.x, 0.00001);
                            assertEquals(grip.y, placed.y, 0.00001);
                            assertEquals(grip.z, placed.z, 0.00001);
                            float x = placed.x / scale - forearm.rotationPointX;
                            float y = placed.y / scale - forearm.rotationPointY;
                            float z = placed.z / scale - forearm.rotationPointZ;
                            assertTrue(x > 0 && x < (slim ? 3 : 4) && y > 3 && y < 6 && z > -4 && z < 0,
                                "handle must sit inside the actual fist");
                        }
                    }
                }
            }
        }
    }

    @Test
    void leavingSwimmingRestoresTheNormalAttachment() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.heldItemRight = model.heldItemLeft = 1;
        animate(model, 0, 0);
        assertTrue(model.swimmingRightArmAdjustment.w > 0);
        model.swimmingItemPose.setX(0);
        model.swimmingItemPose.setY(0);
        SwimmingArmClearance.update(model);
        assertEquals(0, model.swimmingRightArmAdjustment.w);
        assertEquals(0, model.swimmingLeftArmAdjustment.w);
        assertEquals(0, model.swimmingElbowAdjustment.lengthSquared());
        Vector4f grip = new Vector4f(1, 9, 1, 1);
        for (boolean left : new boolean[] {false, true}) {
            assertEquals(grip, Matrix4f.transform(SwimmingItemTransform.matrix(model, left, 1), grip, null));
        }
    }

    @Test
    void clearanceOnlyAdjustsOccupiedSwimmingHands() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.heldItemRight = 1;
        animate(model, 0, 0);
        assertTrue(model.swimmingRightArmAdjustment.w > 0);
        assertEquals(0, model.swimmingLeftArmAdjustment.w);
        model.heldItemRight = 3;
        SwimmingArmClearance.update(model);
        assertEquals(0, model.swimmingRightArmAdjustment.w);
        model.heldItemRight = model.heldItemLeft = 1;
        model.aimedBow = true;
        SwimmingArmClearance.update(model);
        assertEquals(0, model.swimmingRightArmAdjustment.w);
        assertEquals(0, model.swimmingLeftArmAdjustment.w);
        assertEquals(0, model.swimmingElbowAdjustment.lengthSquared());
    }

    @Test
    void aquaClearanceFollowsAContinuousMovingStroke() {
        ModelBendsPlayer model = new ModelBendsPlayer();
        model.heldItemRight = model.heldItemLeft = 1;
        Vector4f[] first = new Vector4f[2];
        Vector4f[] previous = new Vector4f[2];
        float[] travel = new float[2];
        Quaternion[] previousTools = new Quaternion[2];
        for (int step = 0; step < 256; step++) {
            animate(model, 2, (float)(step * Math.PI / 128));
            for (int hand = 0; hand < 2; hand++) {
                boolean left = hand == 1;
                Matrix4f matrix = Matrix4f.mul(armMatrix(model, left), forearmMatrix(model, left), null);
                Vector4f palm = Matrix4f.transform(matrix, new Vector4f(2, 4.5f, -2, 1), null);
                if (previous[hand] != null) {
                    assertTrue(Vector4f.sub(palm, previous[hand], null).length() < 0.5f,
                        "clearance should not snap between poses");
                    travel[hand] = Math.max(travel[hand], Vector4f.sub(palm, first[hand], null).length());
                } else first[hand] = palm;
                previous[hand] = palm;
                Quaternion tool = new Quaternion()
                    .setFromMatrix(SwimmingItemTransform.matrix(model, left, 1));
                tool.normalise();
                if (previousTools[hand] != null) {
                    float dot = Math.abs(Quaternion.dot(tool, previousTools[hand]));
                    assertTrue(Math.toDegrees(2 * Math.acos(Math.min(1, dot))) < 2,
                        "the tool should tilt gradually around the grip");
                }
                previousTools[hand] = tool;
            }
        }
        assertTrue(travel[0] > 2 && travel[1] > 2, "both occupied hands must keep moving");
    }

    @Test
    void strokeEndEasesTheToolBackWithoutClippingEitherHand() throws IOException {
        List<Vector4f> vertices = toolVertices();
        for (int mode = 0; mode < 3; mode++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            model.heldItemRight = model.heldItemLeft = 1;
            Quaternion[] previous = new Quaternion[2];
            float[] previousAngles = new float[2];
            int releaseFrames = 0;
            for (int frame = 0; frame < 720; frame++) {
                advancePose(model, mode, frame / 3.0f, 1 / 3.0f);
                Matrix4f inverseHead = Matrix4f.invert(matrix(model.bipedHead), null);
                for (int hand = 0; hand < 2; hand++) {
                    boolean left = hand == 1;
                    Matrix4f correction = SwimmingItemTransform.matrix(model, left, 1);
                    Quaternion current = new Quaternion().setFromMatrix(correction);
                    current.normalise();
                    float angle = left ? model.swimmingLeftItemAdjustment.w : model.swimmingRightItemAdjustment.w;
                    if (frame > 120 && angle < previousAngles[hand]) {
                        float dot = Math.abs(Quaternion.dot(current, previous[hand]));
                        double change = Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
                        assertTrue(change < 4.1, "release snapped: mode=" + mode + " frame=" + frame + " left=" + left);
                        releaseFrames++;
                    }
                    previous[hand] = current;
                    previousAngles[hand] = angle;
                    if (frame <= 120) continue;
                    ModelRenderer forearm = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
                    Matrix4f tool = Matrix4f.mul(armMatrix(model, left), forearmMatrix(model, left), null);
                    tool.translate(new Vector3f(-forearm.rotationPointX, -forearm.rotationPointY, -forearm.rotationPointZ));
                    Matrix4f.mul(tool, correction, tool);
                    if (left) tool.scale(new Vector3f(-1, 1, 1));
                    tool.translate(new Vector3f(-1, 7, 1));
                    Matrix4f.mul(tool, vanillaTool(), tool);
                    Matrix4f.mul(inverseHead, tool, tool);
                    for (Vector4f vertex : vertices) {
                        Vector4f p = Matrix4f.transform(tool, vertex, null);
                        assertFalse(Math.abs(p.x) < 4.5f && Math.abs(p.y + 4) < 4.5f && Math.abs(p.z) < 4.5f,
                            "eased tool entered the head: mode=" + mode + " frame=" + frame + " left=" + left);
                    }
                }
            }
            assertTrue(releaseFrames > 100, "exercise repeated returns from the clearance pose");
        }
    }

    @Test
    void clearanceDoesNotChatterOrHoldTheToolsFarFromTheirNormalRotation() {
        for (int mode = 0; mode < 3; mode++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            model.heldItemRight = model.heldItemLeft = 1;
            Quaternion[] previous = new Quaternion[2];
            Vector3f[] previousStep = new Vector3f[2];
            float correctionSum = 0;
            float accelerationSum = 0;
            int samples = 0;
            for (int frame = 0; frame < 720; frame++) {
                advancePose(model, mode, frame / 3.0f, 1 / 3.0f);
                for (int hand = 0; hand < 2; hand++) {
                    Quaternion rotation = new Quaternion().setFromMatrix(SwimmingItemTransform.matrix(model, hand == 1, 1));
                    rotation.normalise();
                    if (previous[hand] != null) {
                        Quaternion delta = Quaternion.mul(rotation, Quaternion.negate(previous[hand], null), null);
                        if (delta.w < 0) delta.set(-delta.x, -delta.y, -delta.z, -delta.w);
                        Vector3f step = new Vector3f(delta.x, delta.y, delta.z);
                        float length = step.length();
                        if (length > 0) step.scale((float)Math.toDegrees(2 * Math.atan2(length, delta.w)) / length);
                        if (frame > 120) {
                            accelerationSum += Vector3f.sub(step, previousStep[hand], null).length();
                            correctionSum += hand == 1 ? model.swimmingLeftItemAdjustment.w : model.swimmingRightItemAdjustment.w;
                            samples++;
                        }
                        previousStep[hand] = step;
                    }
                    previous[hand] = rotation;
                }
            }
            // At 60 FPS, stop/start escape steps produced about 1 degree/frame²
            // of average chatter and held surface-swimming tools over 60 degrees away.
            assertTrue(accelerationSum / samples < 0.5f, "clearance direction jitters in mode " + mode);
            assertTrue(correctionSum / samples < 45, "clearance holds an unnecessarily large tilt in mode " + mode);
        }
    }

    @Test
    void lookingDownDoesNotWindTheToolsAroundInsideEitherHand() {
        for (boolean slim : new boolean[] {false, true}) {
            for (float pitch : new float[] {60, 85}) {
                for (int mode = 0; mode < 2; mode++) {
                    ModelBendsPlayer model = new ModelBendsPlayer();
                    model.initModern();
                    model.setSlim(slim);
                    model.heldItemRight = model.heldItemLeft = 1;
                    model.headRotationX = pitch;
                    Quaternion[] previous = new Quaternion[2];
                    float total = 0;
                    int samples = 0;
                    for (int frame = 0; frame < 720; frame++) {
                        advancePose(model, mode, frame / 3.0f, 1 / 3.0f);
                        for (int hand = 0; hand < 2; hand++) {
                            float tilt = hand == 1 ? model.swimmingLeftItemAdjustment.w : model.swimmingRightItemAdjustment.w;
                            Quaternion rotation = new Quaternion().setFromMatrix(SwimmingItemTransform.matrix(model, hand == 1, 1));
                            rotation.normalise();
                            if (frame > 120) {
                                assertTrue(tilt < 90, "tool turned across the grip: pitch=" + pitch + " mode=" + mode + " slim=" + slim);
                                float dot = Math.abs(Quaternion.dot(rotation, previous[hand]));
                                assertTrue(Math.toDegrees(2 * Math.acos(Math.min(1, dot))) < 8,
                                    "downward clearance must not snap between rotation paths");
                                total += tilt;
                                samples++;
                            }
                            previous[hand] = rotation;
                        }
                    }
                    assertTrue(total / samples < 40, "looking down should not sustain a large twist inside the grip");
                }
            }
        }
    }

    @Test
    void continuousClearanceHandlesTurningHeadsAtDifferentFrameRates() throws IOException {
        List<Vector4f> vertices = toolVertices();
        for (int fps : new int[] {30, 144}) {
            for (boolean slim : new boolean[] {false, true}) {
                for (int mode = 0; mode < 3; mode++) {
                    ModelBendsPlayer model = new ModelBendsPlayer();
                    model.initModern();
                    model.setSlim(slim);
                    model.heldItemRight = model.heldItemLeft = 1;
                    for (int frame = 0; frame < fps * 8; frame++) {
                        float seconds = (float)frame / fps;
                        model.headRotationX = 85 * (float)Math.cos(seconds * 1.2f);
                        model.headRotationY = 60 * (float)Math.sin(seconds * 0.9f);
                        advancePose(model, mode, seconds * 20, 20.0f / fps);
                        if (frame < fps) continue;
                        Matrix4f inverseHead = Matrix4f.invert(matrix(model.bipedHead), null);
                        for (boolean left : new boolean[] {false, true}) {
                            ModelRenderer forearm = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
                            Matrix4f fist = Matrix4f.mul(armMatrix(model, left), forearmMatrix(model, left), null);
                            Matrix4f.mul(inverseHead, fist, fist);
                            for (int x = 0; x <= (slim ? 3 : 4); x++) {
                                for (int y = 3; y <= 6; y++) {
                                    for (int z = -4; z <= 0; z++) {
                                        Vector4f point = Matrix4f.transform(fist, new Vector4f(x, y, z, 1), null);
                                        assertFalse(Math.abs(point.x) < 4.5f && Math.abs(point.y + 4) < 4.5f && Math.abs(point.z) < 4.5f,
                                            "fist entered head: mode=" + mode + " fps=" + fps + " frame=" + frame + " left=" + left + " slim=" + slim);
                                    }
                                }
                            }
                            Matrix4f tool = Matrix4f.mul(armMatrix(model, left), forearmMatrix(model, left), null);
                            tool.translate(new Vector3f(-forearm.rotationPointX, -forearm.rotationPointY, -forearm.rotationPointZ));
                            Matrix4f.mul(tool, SwimmingItemTransform.matrix(model, left, 1), tool);
                            if (left) tool.scale(new Vector3f(-1, 1, 1));
                            tool.translate(new Vector3f(-1, 7, 1));
                            Matrix4f.mul(tool, vanillaTool(), tool);
                            Matrix4f.mul(inverseHead, tool, tool);
                            for (Vector4f vertex : vertices) {
                                Vector4f point = Matrix4f.transform(tool, vertex, null);
                                assertFalse(Math.abs(point.x) < 4.5f && Math.abs(point.y + 4) < 4.5f && Math.abs(point.z) < 4.5f,
                                    "moving head clips tool: mode=" + mode + " fps=" + fps + " frame=" + frame + " left=" + left + " slim=" + slim);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void releaseTimingIsIndependentOfFrameRate() {
        float[] remaining = new float[3];
        int[] rates = {30, 60, 144};
        for (int run = 0; run < rates.length; run++) {
            ModelBendsPlayer model = new ModelBendsPlayer();
            model.heldItemRight = model.heldItemLeft = 1;
            model.swimmingItemPose.setX(1);
            model.swimmingItemPose.setY(1);
            // Isolate release timing in a pose well clear of the head.
            model.bipedHead.rotationPointY = -40;
            model.swimmingRightItemAdjustment.set(1, 0, 0, 60);
            model.swimmingLeftItemAdjustment.set(1, 0, 0, 60);
            for (int frame = 0; frame < rates[run] / 2; frame++) {
                SwimmingArmClearance.update(model, 20.0f / rates[run]);
            }
            remaining[run] = model.swimmingRightItemAdjustment.w;
            assertEquals(remaining[run], model.swimmingLeftItemAdjustment.w, 0.001);
            assertTrue(remaining[run] > 0.01f && remaining[run] < 1, "release should settle gradually");
        }
        assertEquals(remaining[0], remaining[1], 0.05);
        assertEquals(remaining[1], remaining[2], 0.05);
    }

    private static void advancePose(ModelBendsPlayer model, int mode, float ticks, float elapsed) {
        if (mode == 2) Animation_AquaAcrobatics.apply(model, 1, true, ticks * 0.1625f / 0.35f, 1, true);
        else if (mode == 1) Animation_Diving.apply(model, ticks, new Vector3f(0, 0, 0.18f));
        else Animation_Swimming.apply(model, ticks, true);
        for (ModelRenderer bone : new ModelRenderer[] {model.bipedBody, model.bipedHead,
            model.bipedRightArm, model.bipedRightForeArm, model.bipedLeftArm, model.bipedLeftForeArm}) {
            part(bone).update(elapsed);
        }
        model.renderRotation.update(elapsed);
        model.swimmingItemPose.update(elapsed);
        SwimmingArmClearance.update(model, elapsed);
    }

    private static void animate(ModelBendsPlayer model, int mode, float phase) {
        if (mode == 2) Animation_AquaAcrobatics.apply(model, 1, true, phase / 0.35f, 1, true);
        else if (mode == 1) Animation_Diving.apply(model, phase / 0.24f, new Vector3f(0, 0, 0.18f));
        else Animation_Swimming.apply(model, phase / 0.1625f, true);
        finishPose(model);
    }

    private static void finishPose(ModelBendsPlayer model) {
        for (ModelRenderer part : new ModelRenderer[] {model.bipedBody, model.bipedHead,
            model.bipedRightArm, model.bipedRightForeArm, model.bipedLeftArm, model.bipedLeftForeArm}) {
            finish(part(part).rotation);
            finish(part(part).pre_rotation);
        }
        finish(model.renderRotation);
        finish(model.swimmingItemPose);
        SwimmingArmClearance.update(model);
    }

    private static void finish(SmoothVector3f vector) {
        vector.setX(vector.vFinal.x);
        vector.setY(vector.vFinal.y);
        vector.setZ(vector.vFinal.z);
    }

    private static List<Vector4f> toolVertices() throws IOException {
        // Use the actual tool silhouettes from the game's existing test classpath.
        // Transparent corners of an icon sheet are not part of the rendered tool.
        boolean[][] occupied = new boolean[17][17];
        for (String name : new String[] {"diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe"}) {
            try (InputStream stream = SwimmingHeldItemPoseTest.class.getResourceAsStream("/assets/minecraft/textures/items/" + name + ".png")) {
                assertNotNull(stream, name);
                BufferedImage image = ImageIO.read(stream);
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        if ((image.getRGB(x, y) >>> 24) == 0) continue;
                        for (int dx = 0; dx <= 1; dx++) {
                            for (int dy = 0; dy <= 1; dy++) occupied[16 - x - dx][16 - y - dy] = true;
                        }
                    }
                }
            }
        }
        List<Vector4f> vertices = new ArrayList<>();
        for (int x = 0; x <= 16; x++) {
            for (int y = 0; y <= 16; y++) {
                if (!occupied[x][y]) continue;
                vertices.add(new Vector4f(x, y, 0, 1));
                vertices.add(new Vector4f(x, y, -1, 1));
            }
        }
        return vertices;
    }

    private static ModelRendererBends part(ModelRenderer part) {
        return (ModelRendererBends)part;
    }

    private static void assertPoseEquals(ModelRenderer expected, ModelRenderer actual) {
        assertEquals(part(expected).rotation.vFinal, part(actual).rotation.vFinal);
        assertEquals(part(expected).pre_rotation.vFinal, part(actual).pre_rotation.vFinal);
    }

    private static Matrix4f armMatrix(ModelBendsPlayer model, boolean left) {
        ModelRenderer arm = left ? model.bipedLeftArm : model.bipedRightArm;
        Vector4f adjustment = left ? model.swimmingLeftArmAdjustment : model.swimmingRightArmAdjustment;
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(arm.rotationPointX, arm.rotationPointY, arm.rotationPointZ));
        if (adjustment.w != 0) rotate(matrix, adjustment.w, adjustment.x, adjustment.y, adjustment.z);
        matrix.translate(new Vector3f(-arm.rotationPointX, -arm.rotationPointY, -arm.rotationPointZ));
        return Matrix4f.mul(matrix, matrix(arm), matrix);
    }

    private static Matrix4f forearmMatrix(ModelBendsPlayer model, boolean left) {
        ModelRenderer forearm = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(forearm.rotationPointX, forearm.rotationPointY, forearm.rotationPointZ));
        rotate(matrix, left ? model.swimmingElbowAdjustment.y : model.swimmingElbowAdjustment.x, 1, 0, 0);
        matrix.translate(new Vector3f(-forearm.rotationPointX, -forearm.rotationPointY, -forearm.rotationPointZ));
        return Matrix4f.mul(matrix, matrix(forearm), matrix);
    }

    private static Matrix4f matrix(ModelRenderer part) {
        ModelRendererBends bends = part(part);
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(part.rotationPointX, part.rotationPointY, part.rotationPointZ));
        rotate(matrix, -bends.pre_rotation.getY(), 0, 1, 0);
        rotate(matrix, bends.pre_rotation.getX(), 1, 0, 0);
        rotate(matrix, bends.pre_rotation.getZ(), 0, 0, 1);
        rotate(matrix, bends.rotation.getZ(), 0, 0, 1);
        rotate(matrix, bends.rotation.getY(), 0, 1, 0);
        rotate(matrix, bends.rotation.getX(), 1, 0, 0);
        return matrix;
    }

    private static Matrix4f vanillaTool() {
        // Full-3D tool placement and ItemRenderer's icon transform in model pixels.
        Matrix4f matrix = new Matrix4f();
        matrix.translate(new Vector3f(0, 3, 0));
        matrix.scale(new Vector3f(0.625f, -0.625f, 0.625f));
        rotate(matrix, -100, 1, 0, 0);
        rotate(matrix, 45, 0, 1, 0);
        matrix.translate(new Vector3f(0, -4.8f, 0));
        matrix.scale(new Vector3f(1.5f, 1.5f, 1.5f));
        rotate(matrix, 50, 0, 1, 0);
        rotate(matrix, 335, 0, 0, 1);
        matrix.translate(new Vector3f(-15, -1, 0));
        return matrix;
    }

    private static void rotate(Matrix4f matrix, float degrees, float x, float y, float z) {
        matrix.rotate((float)Math.toRadians(degrees), new Vector3f(x, y, z));
    }
}
