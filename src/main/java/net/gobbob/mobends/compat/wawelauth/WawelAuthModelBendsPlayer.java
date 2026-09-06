package net.gobbob.mobends.compat.wawelauth;

import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.model.ModelRenderer;

import org.fentanylsolutions.wawelauth.api.SkinLayersHelper.EnumPlayerModelParts;
import org.fentanylsolutions.wawelauth.client.render.IModelBipedModernExt;

/**
 * Implements Wawel Auth's enum-based model API without making Wawel Auth mandatory for the base player model.
 *
 * <p>Wawel Auth injects its model interface into {@code ModelBiped}. These methods override the injected methods at
 * runtime and map Wawel Auth's logical layer parts to Mo' Bends' segmented renderers.</p>
 */
public final class WawelAuthModelBendsPlayer extends ModelBendsPlayer implements IModelBipedModernExt {

    public WawelAuthModelBendsPlayer(float scale) {
        super(scale);
    }

    @Override
    public void renderPart3D(EnumPlayerModelParts part, float scale) {
        if (part == EnumPlayerModelParts.RIGHT_SLEEVE) {
            this.render3DRightArmWear(scale);
        }
    }

    @Override
    public ModelRenderer rendererFromPart(EnumPlayerModelParts part) {
        switch (part) {
            case CAPE:
                return this.bipedCloak;
            case JACKET:
                return this.getBodyWear();
            case LEFT_SLEEVE:
                return this.getLeftArmWear();
            case RIGHT_SLEEVE:
                return this.getRightArmWear();
            case LEFT_PANTS:
                return this.getLeftLegWear();
            case RIGHT_PANTS:
                return this.getRightLegWear();
            case HAT:
                return this.bipedHeadwear;
            default:
                throw new IllegalArgumentException("Unknown player model part: " + part);
        }
    }

    @Override
    public ModelRenderer baseRendererFromPart(EnumPlayerModelParts part) {
        switch (part) {
            case CAPE:
                return this.bipedCloak;
            case JACKET:
                return this.bipedBody;
            case LEFT_SLEEVE:
                return this.bipedLeftArm;
            case RIGHT_SLEEVE:
                return this.bipedRightArm;
            case LEFT_PANTS:
                return this.bipedLeftLeg;
            case RIGHT_PANTS:
                return this.bipedRightLeg;
            case HAT:
                return this.bipedHead;
            default:
                throw new IllegalArgumentException("Unknown player model part: " + part);
        }
    }

    @Override
    public EnumPlayerModelParts partFromRenderer(ModelRenderer renderer) {
        if (renderer == this.bipedCloak) return EnumPlayerModelParts.CAPE;
        if (renderer == this.getBodyWear()) return EnumPlayerModelParts.JACKET;
        if (renderer == this.getLeftArmWear()) return EnumPlayerModelParts.LEFT_SLEEVE;
        if (renderer == this.getRightArmWear()) return EnumPlayerModelParts.RIGHT_SLEEVE;
        if (renderer == this.getLeftLegWear()) return EnumPlayerModelParts.LEFT_PANTS;
        if (renderer == this.getRightLegWear()) return EnumPlayerModelParts.RIGHT_PANTS;
        if (renderer == this.bipedHeadwear) return EnumPlayerModelParts.HAT;
        return null;
    }
}
