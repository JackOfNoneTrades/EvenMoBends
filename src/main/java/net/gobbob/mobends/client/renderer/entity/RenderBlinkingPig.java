package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.render.BlinkingTextures;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

/** Vanilla pig renderer with texture-only blinking. */
public class RenderBlinkingPig extends RenderPig {
    private static final ResourceLocation PIG_TEXTURE = new ResourceLocation("textures/entity/pig/pig.png");

    public RenderBlinkingPig() {
        super(new ModelPig(), new ModelPig(0.5f), 0.7f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPig pig) {
        return BlinkingTextures.forMob(pig, PIG_TEXTURE);
    }
}
