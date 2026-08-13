package net.gobbob.mobends.client.renderer.entity;

import ganymedes01.etfuturum.client.renderer.entity.TechnobladeCrownRenderer;
import net.gobbob.mobends.client.render.BlinkingTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

/** EFR's pig renderer with Even Mo' Bends texture-only blinking. */
public class RenderBlinkingPigEtFuturum extends TechnobladeCrownRenderer {
    private static final ResourceLocation PIG_TEXTURE = new ResourceLocation("textures/entity/pig/pig.png");

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return entity instanceof EntityPig ? BlinkingTextures.forMob((EntityPig)entity, PIG_TEXTURE) : PIG_TEXTURE;
    }
}
