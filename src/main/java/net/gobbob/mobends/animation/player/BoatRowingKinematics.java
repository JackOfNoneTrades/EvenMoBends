package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.compat.EtFuturumPaddlePose;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector3f;

/** Geometry in model pixels; rotations follow ModelRenderer's Z, Y, X order. */
final class BoatRowingKinematics {
    static final float PLAYER_SCALE = 0.9375f;

    private BoatRowingKinematics() {}

    /** Center of the inboard end of EFR's paddle shaft, in the boat's facing frame. */
    static Vector3f paddleGrip(float phase, int side, boolean raft) {
        Vector3f grip = new Vector3f(0.0f, 1.0f, -4.0f);
        rotateX(grip, EtFuturumPaddlePose.pitch(phase));
        rotateY(grip, EtFuturumPaddlePose.yaw(phase, side));
        rotateZ(grip, 0.19634955f);
        grip.x += 3.0f;
        grip.y += raft ? -4.0f : -5.0f;
        // ModelRaft uses a -17 pixel offset on its second paddle, rather than -18.
        grip.z += side == 0 ? 9.0f : raft ? -8.0f : -9.0f;
        return new Vector3f(grip.z, grip.y, -grip.x); // ModelNewBoat's extra 90 degree turn.
    }

    /** Invert the boat/player render transforms, including player scale and seat displacement. */
    static Vector3f playerGrip(Vector3f grip, float rockingDegrees, float boatYaw,
        double boatMinusPlayerX, double boatMinusPlayerY, double boatMinusPlayerZ, float seatLift) {
        // The boat rocks before the renderer flips X/Y; in model space the sign is reversed.
        rotateX(grip, (float)Math.toRadians(-rockingDegrees));
        Vector3f displacement = new Vector3f((float)(boatMinusPlayerX * 16.0),
            (float)((boatMinusPlayerY + 0.375 - seatLift) * 16.0), (float)(boatMinusPlayerZ * 16.0));
        rotateY(displacement, (float)Math.toRadians(boatYaw - 180.0f));
        grip.x = (grip.x - displacement.x) / PLAYER_SCALE;
        grip.y = (grip.y - displacement.y) / PLAYER_SCALE + 24.125f;
        grip.z = (grip.z + displacement.z) / PLAYER_SCALE;
        return grip;
    }

    /**
     * Solve the real segmented arm, whose elbow pivot is at (x, 4, 2), not at
     * the center of a six-pixel bone. The hand center is (halfWidth, 5, -2).
     * Result: upper-arm X/Z and forearm X, in degrees. Upper-arm Y stays zero.
     */
    static Vector3f solveArm(Vector3f target, float elbowX, float halfWidth) {
        double centerX = elbowX + halfWidth;
        double distanceSquared = target.lengthSquared();
        double cosine = (distanceSquared - centerX * centerX - 49.0) / Math.hypot(32.0, 36.0);
        double flex = Math.atan2(36.0, 32.0) - Math.acos(Math.max(-1.0, Math.min(1.0, cosine)));
        flex = Math.max(Math.toRadians(-130.0), Math.min(0.0, flex));
        double wristY = 4.0 + 5.0 * Math.cos(flex) + 2.0 * Math.sin(flex);
        double wristZ = 2.0 + 5.0 * Math.sin(flex) - 2.0 * Math.cos(flex);
        double reach = Math.sqrt(centerX * centerX + wristY * wristY + wristZ * wristZ);
        // Clamp unreachable targets without stretching the skin or producing NaNs.
        double scale = distanceSquared > 0.000001 ? reach / Math.sqrt(distanceSquared) : 1.0;
        double x = target.x * scale;
        double y = target.y * scale;
        double z = target.z * scale;
        double yzLength = Math.hypot(wristY, wristZ);
        z = Math.max(-yzLength, Math.min(yzLength, z));
        double unrolledY = Math.sqrt(Math.max(0.0, yzLength * yzLength - z * z));
        double pitch = Math.atan2(z, unrolledY) - Math.atan2(wristZ, wristY);
        double roll = Math.atan2(y, x) - Math.atan2(unrolledY, centerX);
        return new Vector3f(MathHelper.wrapAngleTo180_float((float)Math.toDegrees(pitch)),
            MathHelper.wrapAngleTo180_float((float)Math.toDegrees(roll)), (float)Math.toDegrees(flex));
    }

    static void rotateX(Vector3f v, float angle) {
        float c = (float)Math.cos(angle), s = (float)Math.sin(angle);
        float y = v.y * c - v.z * s;
        v.z = v.y * s + v.z * c;
        v.y = y;
    }

    static void rotateY(Vector3f v, float angle) {
        float c = (float)Math.cos(angle), s = (float)Math.sin(angle);
        float x = v.x * c + v.z * s;
        v.z = -v.x * s + v.z * c;
        v.x = x;
    }

    static void rotateZ(Vector3f v, float angle) {
        float c = (float)Math.cos(angle), s = (float)Math.sin(angle);
        float x = v.x * c - v.y * s;
        v.y = v.x * s + v.y * c;
        v.x = x;
    }
}
