package net.gobbob.mobends.util;

import net.gobbob.mobends.util.Color;
import org.lwjgl.opengl.GL11;

public class Draw {
    public static void rectangle(float x, float y, float w, float h) {
        GL11.glBegin((int)7);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex3f((float)(x + 0.0f), (float)(y + 0.0f), (float)0.0f);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex3f((float)(x + 0.0f), (float)(y + h), (float)0.0f);
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0.0f);
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex3f((float)(x + w), (float)(y + 0.0f), (float)0.0f);
        GL11.glEnd();
    }

    public static void rectangle_xgradient(float x, float y, float w, float h, Color color0, Color color1) {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glHint((int)3152, (int)4354);
        GL11.glShadeModel((int)7425);
        GL11.glBegin((int)7);
        GL11.glColor4f((float)color0.r, (float)color0.g, (float)color0.b, (float)color0.a);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex3f((float)(x + 0.0f), (float)(y + 0.0f), (float)0.0f);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex3f((float)(x + 0.0f), (float)(y + h), (float)0.0f);
        GL11.glColor4f((float)color1.r, (float)color1.g, (float)color1.b, (float)color1.a);
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0.0f);
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex3f((float)(x + w), (float)(y + 0.0f), (float)0.0f);
        GL11.glEnd();
    }
}

