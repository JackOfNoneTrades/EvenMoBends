package net.gobbob.mobends.compat.wawelauth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.model.ModelRenderer;

import org.fentanylsolutions.wawelauth.api.SkinLayersHelper.EnumPlayerModelParts;
import org.junit.jupiter.api.Test;

class WawelAuthModelBendsPlayerTest {

    @Test
    void mapsNewWawelAuthPartApiToSegmentedModel() {
        WawelAuthModelBendsPlayer model = new WawelAuthModelBendsPlayer(0.0f);
        model.initModern();

        assertPart(model, EnumPlayerModelParts.CAPE, model.bipedCloak, model.bipedCloak);
        assertPart(model, EnumPlayerModelParts.JACKET, model.getBodyWear(), model.bipedBody);
        assertPart(model, EnumPlayerModelParts.LEFT_SLEEVE, model.getLeftArmWear(), model.bipedLeftArm);
        assertPart(model, EnumPlayerModelParts.RIGHT_SLEEVE, model.getRightArmWear(), model.bipedRightArm);
        assertPart(model, EnumPlayerModelParts.LEFT_PANTS, model.getLeftLegWear(), model.bipedLeftLeg);
        assertPart(model, EnumPlayerModelParts.RIGHT_PANTS, model.getRightLegWear(), model.bipedRightLeg);
        assertPart(model, EnumPlayerModelParts.HAT, model.bipedHeadwear, model.bipedHead);

        model.hidePart(EnumPlayerModelParts.JACKET, true);
        assertFalse(model.getBodyWear().showModel);
        model.hidePart(EnumPlayerModelParts.JACKET, false);
        assertTrue(model.getBodyWear().showModel);

        model.setSlim(true);
        assertPart(model, EnumPlayerModelParts.LEFT_SLEEVE, model.getLeftArmWear(), model.bipedLeftArm);
        assertPart(model, EnumPlayerModelParts.RIGHT_SLEEVE, model.getRightArmWear(), model.bipedRightArm);
    }

    private static void assertPart(WawelAuthModelBendsPlayer model, EnumPlayerModelParts part,
        ModelRenderer overlay, ModelRenderer base) {
        assertSame(overlay, model.rendererFromPart(part));
        assertSame(base, model.baseRendererFromPart(part));
        assertSame(part, model.partFromRenderer(overlay));
    }
}
