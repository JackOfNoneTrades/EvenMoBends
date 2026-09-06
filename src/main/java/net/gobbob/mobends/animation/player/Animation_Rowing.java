package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.compat.EtFuturumRequiemCompat;
import net.gobbob.mobends.compat.EtFuturumRequiemCompat.BoatState;
import net.gobbob.mobends.config.PlayerAnimationConfig;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector3f;

/** EFR's two independent paddles drive the hands, including when turning in place. */
public class Animation_Rowing extends Animation {
    @Override
    public String getName() {
        return "rowing";
    }

    public static BoatState getBoatState(EntityPlayer player, float partialTicks) {
        return player.isRiding() && player.isEntityAlive() && PlayerAnimationConfig.isEnabled("riding")
            && PlayerAnimationConfig.isEnabled("rowing")
            ? EtFuturumRequiemCompat.getBoatState(player, partialTicks) : null;
    }

    public static float boatYaw(BoatState state, float partialTicks) {
        // RenderManager uses a linear yaw interpolation for non-living entities.
        return state.boat.prevRotationYaw + (state.boat.rotationYaw - state.boat.prevRotationYaw) * partialTicks;
    }

    /** World-space lift, applied before the player renderer's scale and Y flip. */
    public static float seatLift(boolean raft) {
        // Rafts already seat riders higher and have lower paddles, leaving less arm reach.
        return (raft ? 0.5f : 2.0f) * BoatRowingKinematics.PLAYER_SCALE / 16.0f;
    }

    static float bodyLean(boolean driver, boolean raft, float leftStroke, float rightStroke) {
        if (!driver) return 0.0f;
        // Rock around the hips through the stroke. Turning with one paddle gives half
        // the excursion. The shared paddle state also eases the lean back to rest.
        return (raft ? 14.0f : 0.0f) + 9.0f * (leftStroke + rightStroke);
    }

    @Override
    public void animate(EntityLivingBase entity, ModelBase baseModel, EntityData data) {
        ModelBendsPlayer model = (ModelBendsPlayer)baseModel;
        EntityPlayer player = (EntityPlayer)entity;
        float partialTicks = model.getPartialTicks();
        BoatState state = getBoatState(player, partialTicks);
        if (state == null) return;

        set(model.renderOffset, 0.0f, 0.0f, 0.0f);
        set(model.renderRotation, 0.0f, 0.0f, 0.0f);
        // Clear swimming/weapon pre-rotations before solving in this coordinate frame.
        for (ModelRenderer part : new ModelRenderer[] {model.bipedHead, model.bipedBody,
            model.bipedLeftArm, model.bipedRightArm, model.bipedLeftForeArm, model.bipedRightForeArm,
            model.bipedLeftLeg, model.bipedRightLeg, model.bipedLeftForeLeg, model.bipedRightForeLeg}) {
            set(((ModelRendererBends)part).pre_rotation, 0.0f, 0.0f, 0.0f);
        }

        pose(model.bipedRightLeg, -80.0f, 12.0f, 0.0f);
        pose(model.bipedLeftLeg, -80.0f, -12.0f, 0.0f);
        pose(model.bipedRightForeLeg, 65.0f, 0.0f, 0.0f);
        pose(model.bipedLeftForeLeg, 65.0f, 0.0f, 0.0f);

        float lean = bodyLean(state.driver, state.raft, state.paddles.leftStroke, state.paddles.rightStroke);
        pose(model.bipedBody, lean, 0.0f, 0.0f);
        float headYaw = player.prevRotationYawHead
            + MathHelper.wrapAngleTo180_float(player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        pose(model.bipedHead, model.headRotationX - lean,
            MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(headYaw - boatYaw(state, partialTicks)), -85.0f, 85.0f), 0.0f);

        if (state.driver) {
            reach(model, model.bipedLeftArm, model.bipedLeftForeArm, grip(player, state, 0, partialTicks), lean);
            reach(model, model.bipedRightArm, model.bipedRightForeArm, grip(player, state, 1, partialTicks), lean);
        } else {
            pose(model.bipedRightArm, -15.0f, 0.0f, 5.0f);
            pose(model.bipedLeftArm, -15.0f, 0.0f, -5.0f);
            pose(model.bipedRightForeArm, -35.0f, 0.0f, 0.0f);
            pose(model.bipedLeftForeArm, -35.0f, 0.0f, 0.0f);
        }
    }

    private static Vector3f grip(EntityPlayer player, BoatState state, int side, float partialTicks) {
        Entity boat = state.boat;
        double dx = lerp(boat.lastTickPosX - player.lastTickPosX, boat.posX - player.posX, partialTicks);
        double dy = lerp(boat.lastTickPosY - player.lastTickPosY, boat.posY - player.posY, partialTicks) + player.yOffset;
        double dz = lerp(boat.lastTickPosZ - player.lastTickPosZ, boat.posZ - player.posZ, partialTicks);
        if (player.isSneaking() && !(player instanceof EntityPlayerSP)) dy += 0.125;
        return BoatRowingKinematics.playerGrip(
            BoatRowingKinematics.paddleGrip(state.paddlePhase(side), side, state.raft),
            state.rockingDegrees, boatYaw(state, partialTicks), dx, dy, dz, seatLift(state.raft));
    }

    static void reach(ModelBendsPlayer model, ModelRenderer arm, ModelRenderer forearm, Vector3f grip, float lean) {
        grip.x -= model.bipedBody.rotationPointX;
        grip.y -= model.bipedBody.rotationPointY;
        grip.z -= model.bipedBody.rotationPointZ;
        BoatRowingKinematics.rotateX(grip, (float)Math.toRadians(-lean));
        grip.x -= arm.rotationPointX;
        grip.y -= arm.rotationPointY;
        grip.z -= arm.rotationPointZ;
        float halfWidth = model.isSlim() ? 1.5f : 2.0f;
        Vector3f angles = BoatRowingKinematics.solveArm(grip, forearm.rotationPointX, halfWidth);
        pose(arm, angles.x, 0.0f, angles.y);
        pose(forearm, angles.z, 0.0f, 0.0f);
    }

    private static double lerp(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

    private static void pose(ModelRenderer part, float x, float y, float z) {
        set(((ModelRendererBends)part).rotation, x, y, z);
    }

    private static void set(SmoothVector3f vector, float x, float y, float z) {
        // EFR already interpolates the paddles. Smoothing again would make the hands lag.
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
    }
}
