/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Adapted from Smart Moving 15.8.2-maka's diving animation:
 * https://github.com/makamys/SmartMoving/blob/4fe6a9dd080162859b3630b6a886bfe47d50f009/src/main/java/net/smart/moving/render/SmartMovingModel.java
 */
package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/** A directional, streamlined pose used while travelling fully underwater. */
public class Animation_Diving extends Animation {
    @Override
    public String getName() {
        return "diving";
    }

    @Override
    public void animate(EntityLivingBase entity, ModelBase baseModel, EntityData entityData) {
        ModelBendsPlayer model = (ModelBendsPlayer)baseModel;
        Data_Player data = (Data_Player)entityData;

        double horizontalSpeed = Math.sqrt(data.motion.x * data.motion.x + data.motion.z * data.motion.z);
        double totalSpeed = Math.sqrt(horizontalSpeed * horizontalSpeed + data.motion.y * data.motion.y);
        float moving = MathHelper.clamp_float((float)(totalSpeed / 0.18), 0.0f, 1.0f);
        float phase = data.ticks * 0.24f;
        float armStroke = (MathHelper.cos(phase) + 1.0f) / 2.0f;
        float legKick = MathHelper.cos(phase * 1.8f) * moving;

        float travelPitch = (float)Math.toDegrees(Math.atan2(-data.motion.y, Math.max(horizontalSpeed, 0.02)));
        travelPitch = MathHelper.clamp_float(travelPitch, -35.0f, 35.0f);
        float bodyTilt = 80.0f + travelPitch;
        model.renderRotation.setSmoothX(bodyTilt, 0.25f);
        model.renderOffset.setSmoothZ(10.0f, 0.25f);

        ((ModelRendererBends)model.bipedBody).rotation.setSmoothX(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothY(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedBody).rotation.setSmoothZ(0.0f, 0.3f);

        // Use a full breaststroke underwater. The first pass held the arms nearly straight,
        // which read as gliding instead of swimming.
        float upperArmPitch = -45.0f - armStroke * 120.0f;
        float armSpread = armStroke * 20.0f;
        float strokeProgress = (phase % ((float)Math.PI * 2.0f)) / ((float)Math.PI * 2.0f);
        float elbowBend = Math.min(armStroke * 2.0f - 1.0f, 0.0f) * -60.0f;
        if (strokeProgress >= 0.55f && strokeProgress <= 0.9f) {
            elbowBend = -60.0f;
        }

        ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothY(-90.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothY(90.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(armSpread, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothZ(-armSpread, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(upperArmPitch, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(upperArmPitch, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothY(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothY(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(elbowBend, 0.3f);
        ((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(elbowBend, 0.3f);

        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(legKick * 35.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-legKick * 35.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(3.0f, 0.3f);
        ((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-3.0f, 0.3f);
        ((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(
            8.0f + Math.max(0.0f, -legKick) * 24.0f,
            0.3f);
        ((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(
            8.0f + Math.max(0.0f, legKick) * 24.0f,
            0.3f);

        ((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(0.0f, 0.3f);
        ((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX - bodyTilt * 0.45f, 0.3f);
        ((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY, 0.3f);
    }
}
