package net.gobbob.mobends.compat;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;

/** Shared paddle angles for EFR's rendered oars and the Mo' Bends hand targets. */
public final class EtFuturumPaddlePose {
    // About five ticks into EFR's sixteen-tick cycle, with the handles farther forward.
    public static final float REST_PHASE = 2.0f;

    private EtFuturumPaddlePose() {}

    public static float pitch(float phase) {
        return (float)MathHelper.denormalizeClamp(-1.0471975803375244, -0.2617993950843811,
            (MathHelper.sin(-phase) + 1.0f) / 2.0f);
    }

    public static float yaw(float phase, int side) {
        float yaw = (float)MathHelper.denormalizeClamp(-Math.PI / 4.0, Math.PI / 4.0,
            (MathHelper.sin(-phase + 1.0f) + 1.0f) / 2.0f);
        return side == 1 ? (float)Math.PI - yaw : yaw;
    }

    public static void applyPose(ModelRenderer paddle, float phase, int side) {
        paddle.rotateAngleX = pitch(phase);
        paddle.rotateAngleY = yaw(phase, side);
    }
}
