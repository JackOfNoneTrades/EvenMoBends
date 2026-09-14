package net.gobbob.mobends.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiToggleButton
extends GuiButton {
    public boolean toggleState;
    public String title;
    public int titleWidth;

    public GuiToggleButton(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, boolean state) {
        super(p_i1020_1_, p_i1020_2_, p_i1020_3_, "");
        this.toggleState = state;
        this.width = 40;
        this.height = 20;
        this.displayString = state ? I18n.format("mobends.gui.on") : I18n.format("mobends.gui.off");
    }

    public GuiToggleButton setTitle(String argTitle, int argWidth) {
        this.title = argTitle;
        this.titleWidth = argWidth;
        this.xPosition += argWidth;
        return this;
    }

    @Override
    public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_) {
        if (this.visible) {
            FontRenderer fontrenderer = p_146112_1_.fontRenderer;
            p_146112_1_.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.field_146123_n = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GL11.glEnable((int)3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc((int)770, (int)771);
            GL11.glPushMatrix();
            GL11.glColor3f((float)0.5f, (float)0.5f, (float)0.5f);
            this.drawTexturedModalRect(this.xPosition - this.titleWidth, this.yPosition, 0, 46 + k * 20, (this.titleWidth + this.width) / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.titleWidth / 2 - this.titleWidth, this.yPosition, 200 - (this.titleWidth + this.width) / 2, 46 + k * 20, (this.titleWidth + this.width) / 2, this.height);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            if (this.toggleState) {
                GL11.glColor3f((float)0.0f, (float)1.0f, (float)0.0f);
            } else {
                GL11.glColor3f((float)1.0f, (float)0.0f, (float)0.0f);
            }
            this.drawTexturedModalRect(this.titleWidth + this.xPosition - this.titleWidth, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.titleWidth + this.xPosition + this.width / 2 - this.titleWidth, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            GL11.glPopMatrix();
            this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
            int l = 0xE0E0E0;
            if (this.packedFGColour != 0) {
                l = this.packedFGColour;
            } else if (!this.enabled) {
                l = 0xA0A0A0;
            } else if (this.field_146123_n) {
                l = 0xFFFFA0;
            }
            this.drawString(fontrenderer, this.title, this.xPosition + 15 - this.titleWidth, this.yPosition + (this.height - 8) / 2, l);
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
            GL11.glBlendFunc((int)770, (int)771);
        }
    }
}
