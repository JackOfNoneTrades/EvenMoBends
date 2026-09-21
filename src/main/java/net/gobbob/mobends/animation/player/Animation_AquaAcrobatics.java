package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;

/** Bent-limb poses in AA's already-rotated coordinate system, with no second whole-body tilt. */
public final class Animation_AquaAcrobatics {
    private Animation_AquaAcrobatics() {}

    public static void apply(ModelBendsPlayer model, float blend, boolean inWater,
        float limbSwing, float limbSwingAmount, boolean animateLimbs) {
        float weight = MathHelper.clamp_float(blend, 0.0f, 1.0f);
        float moving = animateLimbs ? MathHelper.clamp_float(limbSwingAmount * 3.0f, 0.0f, 1.0f) : 0.0f;
        // Vanilla's distance-based, interpolated swing is stable for local and remote players,
        // including when a crawling player stops or touches the bottom while swimming.
        float phase = limbSwing * (inWater ? 0.35f : 0.8f);
        float stroke = MathHelper.cos(phase) * moving;
        float kick = MathHelper.cos(phase * (inWater ? 1.8f : 1.0f)) * moving;

        clear(model.renderOffset);
        clear(model.renderRotation);
        model.renderItemRotation.setSmoothZero(0.5f);
        pose(model.bipedBody, 0, inWater ? 0 : stroke * 3, 0, weight);
        pose(model.bipedHead, lerp(model.headRotationX, -55, weight),
            model.headRotationY * (1.0f - weight * 0.5f), 0, 1.0f);

        if (inWater) {
            float reach = -145.0f + stroke * 40.0f;
            float spread = 12.0f + (1.0f - MathHelper.sin(phase)) * 22.0f * moving;
            float elbow = -15.0f - (1.0f + stroke) * 20.0f * moving;
            pose(model.bipedRightArm, reach, 0, spread, weight);
            pose(model.bipedLeftArm, reach, 0, -spread, weight);
            pose(model.bipedRightForeArm, elbow, 0, 0, weight);
            pose(model.bipedLeftForeArm, elbow, 0, 0, weight);
            pose(model.bipedRightLeg, kick * 18, 0, 3, weight);
            pose(model.bipedLeftLeg, -kick * 18, 0, -3, weight);
            pose(model.bipedRightForeLeg, 8 + Math.max(0, -kick) * 18, 0, 0, weight);
            pose(model.bipedLeftForeLeg, 8 + Math.max(0, kick) * 18, 0, 0, weight);
        } else {
            // Alternate reaching arms and knees, keeping the torso low even when stationary.
            pose(model.bipedRightArm, -105 - stroke * 18, 0, 8, weight);
            pose(model.bipedLeftArm, -105 + stroke * 18, 0, -8, weight);
            pose(model.bipedRightForeArm, -60 + stroke * 20, 0, 0, weight);
            pose(model.bipedLeftForeArm, -60 - stroke * 20, 0, 0, weight);
            pose(model.bipedRightLeg, -8 + kick * 12, 0, 8, weight);
            pose(model.bipedLeftLeg, -8 - kick * 12, 0, -8, weight);
            pose(model.bipedRightForeLeg, 20 + Math.max(0, -kick) * 25, 0, 0, weight);
            pose(model.bipedLeftForeLeg, 20 + Math.max(0, kick) * 25, 0, 0, weight);
        }
    }

    private static void pose(ModelRenderer part, float x, float y, float z, float weight) {
        ModelRendererBends bends = (ModelRendererBends)part;
        bends.pre_rotation.setSmoothZero(0.5f);
        bends.rotation.setSmoothX(x * weight, 0.5f);
        bends.rotation.setSmoothY(y * weight, 0.5f);
        bends.rotation.setSmoothZ(z * weight, 0.5f);
    }

    private static void clear(SmoothVector3f vector) {
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
    }

    private static float lerp(float from, float to, float weight) {
        return from + (to - from) * weight;
    }
}
