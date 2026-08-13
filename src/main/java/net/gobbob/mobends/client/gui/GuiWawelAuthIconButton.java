package net.gobbob.mobends.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Compact Wawel Auth dragon button using the same icon as its multiplayer screen. */
final class GuiWawelAuthIconButton extends GuiButton {
    private static final ResourceLocation ICON =
        new ResourceLocation("wawelauth", "textures/gui/logo_2_outline.png");

    GuiWawelAuthIconButton(int id, int x, int y) {
        super(id, x, y, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        super.drawButton(minecraft, mouseX, mouseY);
        if (!this.visible) return;
        minecraft.getTextureManager().bindTexture(ICON);
        float brightness = this.enabled ? 1.0f : 0.5f;
        GL11.glColor4f(brightness, brightness, brightness, 1.0f);
        Gui.func_146110_a(this.xPosition + 3, this.yPosition + 3, 0.0f, 0.0f, 14, 14, 14, 14);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
