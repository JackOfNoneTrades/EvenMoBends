package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.compat.PlayerBlockingCompat;

/** A per-arm overlay, applied after locomotion and attacks so the other hand can still swing. */
public final class Animation_Blocking {
    private static final float SMOOTHNESS = 0.5f;

    private Animation_Blocking() {}

    public static void apply(ModelBendsPlayer model, int hands) {
        if ((hands & PlayerBlockingCompat.MAIN_HAND) != 0) {
            pose((ModelRendererBends)model.bipedRightArm, (ModelRendererBends)model.bipedRightForeArm, -30.0f);
            // Weapon animations normally add an extra item-only rotation. A blocking item
            // must instead follow the posed forearm and its own renderer's blocking transform.
            model.renderItemRotation.setSmoothZero(SMOOTHNESS);
        }
        if ((hands & PlayerBlockingCompat.OFF_HAND) != 0) {
            pose((ModelRendererBends)model.bipedLeftArm, (ModelRendererBends)model.bipedLeftForeArm, 0.0f);
        }
    }

    private static void pose(ModelRendererBends upper, ModelRendererBends lower, float yaw) {
        upper.pre_rotation.setSmoothZero(SMOOTHNESS);
        lower.pre_rotation.setSmoothZero(SMOOTHNESS);
        // Split vanilla's 54-degree blocking pitch at the elbow. Retaining the total
        // orientation lets Backhand and shield item renderers keep their own transforms.
        upper.rotation.setSmoothX(-24.0f, SMOOTHNESS);
        upper.rotation.setSmoothY(yaw, SMOOTHNESS);
        upper.rotation.setSmoothZ(0.0f, SMOOTHNESS);
        lower.rotation.setSmoothX(-30.0f, SMOOTHNESS);
        lower.rotation.setSmoothY(0.0f, SMOOTHNESS);
        lower.rotation.setSmoothZ(0.0f, SMOOTHNESS);
    }
}
