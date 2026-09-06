package net.gobbob.mobends.client.renderer.entity;

import cpw.mods.fml.client.registry.RenderingRegistry;
import ganymedes01.etfuturum.client.model.ModelNewBoat;
import ganymedes01.etfuturum.client.model.ModelRaft;
import ganymedes01.etfuturum.client.renderer.entity.ChestBoatRenderer;
import ganymedes01.etfuturum.client.renderer.entity.NewBoatRenderer;
import ganymedes01.etfuturum.entities.EntityNewBoat;
import ganymedes01.etfuturum.entities.EntityNewBoatWithChest;
import net.gobbob.mobends.compat.EtFuturumPaddlePose;
import net.gobbob.mobends.compat.EtFuturumRequiemCompat;
import net.gobbob.mobends.compat.EtFuturumRowingState;
import net.minecraft.client.model.ModelRenderer;

/** Retains EFR's boat rendering while easing the paddles into the Mo' Bends rest pose. */
public class RenderRowingBoatEtFuturum extends NewBoatRenderer {
    public RenderRowingBoatEtFuturum() {
        this.modelBoat = new BoatModel();
        this.modelRaft = new RaftModel();
    }

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(EntityNewBoat.class, new RenderRowingBoatEtFuturum());
        RenderingRegistry.registerEntityRenderingHandler(EntityNewBoatWithChest.class, new ChestRenderer());
    }

    private static final class ChestRenderer extends ChestBoatRenderer {
        private ChestRenderer() {
            this.modelBoat = new BoatModel();
            this.modelRaft = new RaftModel();
        }
    }

    private static final class BoatModel extends ModelNewBoat {
        @Override
        public void renderPaddle(EntityNewBoat boat, int side, float scale, float partialTicks) {
            if (!renderBendsPaddle(boat, this.paddles[side], side, scale, partialTicks)) {
                super.renderPaddle(boat, side, scale, partialTicks);
            }
        }
    }

    private static final class RaftModel extends ModelRaft {
        @Override
        public void renderPaddle(EntityNewBoat boat, int side, float scale, float partialTicks) {
            if (!renderBendsPaddle(boat, this.paddles[side], side, scale, partialTicks)) {
                super.renderPaddle(boat, side, scale, partialTicks);
            }
        }
    }

    private static boolean renderBendsPaddle(EntityNewBoat boat, ModelRenderer paddle, int side, float scale, float partialTicks) {
        EtFuturumRowingState.Pose pose = EtFuturumRequiemCompat.getPaddlePose(boat, partialTicks);
        if (pose == null) return false;
        EtFuturumPaddlePose.applyPose(paddle, side == 0 ? pose.leftPhase : pose.rightPhase, side);
        paddle.render(scale);
        return true;
    }
}
