package net.gobbob.mobends.client.renderer;

import java.nio.FloatBuffer;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/** Leaves the ordinary attachment intact, with a local grip rotation near the head. */
public final class SwimmingItemTransform {
    private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f TOOL = vanillaTool();
    private static final Vector4f GRIP = Matrix4f.transform(TOOL, new Vector4f(13, 3, -0.5f, 1), null);

    private SwimmingItemTransform() {}

    public static void apply(ModelBendsPlayer model, boolean left, float scale) {
        if ((left ? model.swimmingItemPose.getY() : model.swimmingItemPose.getX()) <= 0.001f) return;
        BUFFER.clear();
        matrix(model, left, scale).store(BUFFER);
        BUFFER.flip();
        GL11.glMultMatrix(BUFFER);
    }

    public static Matrix4f matrix(ModelBendsPlayer model, boolean left, float scale) {
        float weight = left ? model.swimmingItemPose.getY() : model.swimmingItemPose.getX();
        Vector4f adjustment = left ? model.swimmingLeftItemAdjustment : model.swimmingRightItemAdjustment;
        Matrix4f result = new Matrix4f();
        if (weight <= 0 || adjustment.w == 0) return result;
        Vector3f grip = grip(left, scale);
        result.translate(grip);
        result.rotate((float)Math.toRadians(adjustment.w),
            new Vector3f(adjustment.x, adjustment.y, adjustment.z));
        result.translate(new Vector3f(-grip.x, -grip.y, -grip.z));
        return result;
    }

    static Vector3f grip(boolean left, float scale) {
        return new Vector3f((left ? -1 : 1) * (-1 + GRIP.x) * scale,
            (7 + GRIP.y) * scale, (1 + GRIP.z) * scale);
    }

    static Vector3f blade(boolean left) {
        return direction(TOOL, -1, 1, left ? -1 : 1);
    }

    static Vector4f palm(ModelBendsPlayer model, boolean left) {
        ModelRenderer lower = left ? model.bipedLeftForeArm : model.bipedRightForeArm;
        return new Vector4f(lower.rotationPointX + (model.isSlim() ? 1.5f : 2),
            lower.rotationPointY + 4.5f, lower.rotationPointZ - 2, 1);
    }

    private static Vector3f direction(Matrix4f matrix, float x, float y, float side) {
        Vector4f v = Matrix4f.transform(matrix, new Vector4f(x, y, 0, 0), null);
        return new Vector3f(v.x * side, v.y, v.z).normalise(null);
    }

    static void rotatePart(Matrix4f matrix, ModelRenderer part) {
        ModelRendererBends bends = (ModelRendererBends)part;
        rotate(matrix, -bends.pre_rotation.getY(), 0, 1, 0);
        rotate(matrix, bends.pre_rotation.getX(), 1, 0, 0);
        rotate(matrix, bends.pre_rotation.getZ(), 0, 0, 1);
        rotate(matrix, bends.rotation.getZ(), 0, 0, 1);
        rotate(matrix, bends.rotation.getY(), 0, 1, 0);
        rotate(matrix, bends.rotation.getX(), 1, 0, 0);
    }

    static Matrix4f vanillaTool() {
        // Vanilla full-3D item + icon transforms, expressed in model pixels.
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
