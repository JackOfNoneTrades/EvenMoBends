package net.gobbob.mobends.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.ClientProxy;
import net.gobbob.mobends.client.gui.GuiToggleButton;
import net.gobbob.mobends.pack.BendsAction;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class GuiMBMenu
extends GuiScreen {
    public static final ResourceLocation menuTitleTexture = new ResourceLocation("mobends", "textures/gui/menuTitle.png");
    public static final ResourceLocation displayBGTexture = new ResourceLocation("mobends", "textures/gui/displayBG.png");
    public static final ResourceLocation puzzle_animation = new ResourceLocation("mobends", "textures/gui/puzzle_animation.png");
    public static final ResourceLocation puzzle_model = new ResourceLocation("mobends", "textures/gui/puzzle_model.png");
    public static final ResourceLocation puzzle_action_set = new ResourceLocation("mobends", "textures/gui/puzzle_action_set.png");
    public static final ResourceLocation puzzle_action_add = new ResourceLocation("mobends", "textures/gui/puzzle_action_add.png");
    public static final ResourceLocation puzzle_action_substract = new ResourceLocation("mobends", "textures/gui/puzzle_action_substract.png");
    public static final ResourceLocation puzzle_action_multiply = new ResourceLocation("mobends", "textures/gui/puzzle_action_multiply.png");
    public static final ResourceLocation puzzle_mod = new ResourceLocation("mobends", "textures/gui/puzzle_mod.png");
    public static final ResourceLocation puzzle_add = new ResourceLocation("mobends", "textures/gui/puzzle_add.png");
    public static final ResourceLocation puzzle_delete = new ResourceLocation("mobends", "textures/gui/puzzle_delete.png");
    public static final ResourceLocation puzzle_rot = new ResourceLocation("mobends", "textures/gui/puzzle_rot.png");
    public static final ResourceLocation puzzle_scale = new ResourceLocation("mobends", "textures/gui/puzzle_scale.png");
    public static final ResourceLocation puzzle_prerot = new ResourceLocation("mobends", "textures/gui/puzzle_prerot.png");
    public static final ResourceLocation puzzle_mod_none = new ResourceLocation("mobends", "textures/gui/puzzle_mod_none.png");
    public static final ResourceLocation puzzle_mod_cos = new ResourceLocation("mobends", "textures/gui/puzzle_mod_cos.png");
    public static final ResourceLocation puzzle_mod_sin = new ResourceLocation("mobends", "textures/gui/puzzle_mod_sin.png");
    public static final ResourceLocation puzzle_mod_none_selected = new ResourceLocation("mobends", "textures/gui/puzzle_mod_none_selected.png");
    public static final ResourceLocation puzzle_mod_cos_selected = new ResourceLocation("mobends", "textures/gui/puzzle_mod_cos_selected.png");
    public static final ResourceLocation puzzle_mod_sin_selected = new ResourceLocation("mobends", "textures/gui/puzzle_mod_sin_selected.png");
    public static final ResourceLocation puzzle_calc_add = new ResourceLocation("mobends", "textures/gui/puzzle_calc_add.png");
    public static final ResourceLocation puzzle_calc_substract = new ResourceLocation("mobends", "textures/gui/puzzle_calc_substract.png");
    public static final ResourceLocation puzzle_calc_set = new ResourceLocation("mobends", "textures/gui/puzzle_calc_set.png");
    public static final ResourceLocation puzzle_calc_multiply = new ResourceLocation("mobends", "textures/gui/puzzle_calc_multiply.png");
    public static final ResourceLocation puzzle_calc_add_selected = new ResourceLocation("mobends", "textures/gui/puzzle_calc_add_selected.png");
    public static final ResourceLocation puzzle_calc_substract_selected = new ResourceLocation("mobends", "textures/gui/puzzle_calc_substract_selected.png");
    public static final ResourceLocation puzzle_calc_set_selected = new ResourceLocation("mobends", "textures/gui/puzzle_calc_set_selected.png");
    public static final ResourceLocation puzzle_calc_multiply_selected = new ResourceLocation("mobends", "textures/gui/puzzle_calc_multiply_selected.png");
    public float titleTransitionState = 0.0f;
    public boolean titleTransition = true;
    public float[] buttonPositions = new float[AnimatedEntity.animatedEntities.length];
    public float buttonRevealState = 0.0f;
    public float leftBgState = 0.0f;
    public float presetWindowState = 0.0f;
    public float previewRotation = 0.0f;
    public boolean customizeWindow;
    public boolean settingsWindow;
    public boolean packsWindow;
    public int animatedEntityID;
    public int custom_currentAction = 0;
    public int custom_currentChange = 0;
    public float scroll_x = 0.0f;
    public float scroll_y = 0.0f;
    public boolean scrolling_x = false;
    public boolean scrolling_y = false;
    public GuiTextField custom_AnimationNameText;
    public GuiTextField custom_PackTitle;
    public GuiTextField custom_ModelNameText;
    public GuiTextField custom_CalcValueText;

    public GuiMBMenu() {
        Keyboard.enableRepeatEvents((boolean)true);
        this.titleTransition = true;
        this.titleTransitionState = 0.0f;
    }

    @Override
    public void initGui() {
        int i;
        super.initGui();
        this.buttonList.clear();
        if (this.customizeWindow | this.settingsWindow | this.packsWindow) {
            this.buttonList.add(new GuiButton(0, this.packsWindow ? this.width - 70 : 10, this.height - 30, 60, 20, I18n.format("mobends.gui.back")));
        }
        if (!this.customizeWindow & !this.settingsWindow & !this.packsWindow) {
            this.buttonList.add(new GuiButton(1, -90 + (int)(this.leftBgState * 100.0f), this.height - 30, 60, 20, I18n.format("mobends.menu.settings")));
            this.buttonList.add(new GuiButton(3, this.width - (int)(this.leftBgState * 100.0f) + 30, this.height - 30, 60, 20, I18n.format("mobends.menu.packs")));
            this.buttonList.add(new GuiButton(6, this.width / 2 - 60, this.height - 30, 120, 20, I18n.format("mobends.blink.title")));
        }
        if (this.settingsWindow) {
            for (i = 0; i < SettingsNode.settings.length; ++i) {
                if (!(SettingsNode.settings[i] instanceof SettingsBoolean)) continue;
                this.buttonList.add(new GuiToggleButton(10 + i, (int)((float)this.width + this.presetWindowState * -500.0f + 20.0f), 50 + i * 25, ((SettingsBoolean)SettingsNode.settings[i]).data).setTitle(I18n.format("mobends.settings." + SettingsNode.settings[i].id), 100));
            }
        }
        if (this.customizeWindow) {
            this.buttonList.add(new GuiToggleButton(2, (int)((float)this.width + this.presetWindowState * -500.0f + 10.0f), 163, AnimatedEntity.animatedEntities[this.animatedEntityID].animate).setTitle(I18n.format("mobends.menu.animate"), 88));
            if (this.getCurrentAction() != null) {
                this.buttonList.add(new GuiButton(4, (int)(this.getModelSelectionLoc().x + this.getModelSelectionSize().x - 40.0f), (int)this.getModelSelectionLoc().y + 95, 20, 20, "+"));
                GuiButton minus = new GuiButton(5, (int)(this.getModelSelectionLoc().x + this.getModelSelectionSize().x - 20.0f), (int)this.getModelSelectionLoc().y + 95, 20, 20, "-");
                minus.enabled = this.getCurrentAction().calculations.size() > 0;
                this.buttonList.add(minus);
            }
        }
        for (i = 0; i < AnimatedEntity.animatedEntities.length; ++i) {
            this.buttonList.add(new GuiButton(100 + i, (int)(this.buttonPositions[i] - 80.0f + this.presetWindowState * -100.0f), 70 + i * 25, 80, 20, I18n.format("mobends.entity." + AnimatedEntity.animatedEntities[i].id)));
        }
        if (this.custom_AnimationNameText == null) {
            this.custom_AnimationNameText = new GuiTextField(this.fontRendererObj, (int)(this.getModelSelectionLoc().x + 5.0f), (int)(this.getModelSelectionLoc().y + 5.0f + 10.0f), (int)(this.getModelSelectionSize().x - 10.0f), 15);
            if (this.getCurrentAction() != null) {
                this.custom_AnimationNameText.setText(this.getCurrentAction().anim);
            }
        } else {
            this.custom_AnimationNameText.xPosition = (int)(this.getModelSelectionLoc().x + 5.0f);
            this.custom_AnimationNameText.yPosition = (int)(this.getModelSelectionLoc().y + 5.0f + 10.0f);
        }
        if (this.custom_ModelNameText == null) {
            this.custom_ModelNameText = new GuiTextField(this.fontRendererObj, (int)(this.getModelSelectionLoc().x + 5.0f), (int)(this.getModelSelectionLoc().y + 5.0f + 10.0f + 15.0f + 5.0f + 10.0f), (int)(this.getModelSelectionSize().x - 10.0f), 15);
            if (this.getCurrentAction() != null) {
                this.custom_ModelNameText.setText(this.getCurrentAction().model);
            }
        } else {
            this.custom_ModelNameText.xPosition = (int)(this.getModelSelectionLoc().x + 5.0f);
            this.custom_ModelNameText.yPosition = (int)(this.getModelSelectionLoc().y + 5.0f + 10.0f + 15.0f + 5.0f + 10.0f);
        }
        if (this.custom_CalcValueText == null) {
            this.custom_CalcValueText = new GuiTextField(this.fontRendererObj, (int)(this.getModelSelectionLoc().x + 5.0f), (int)(this.getModelSelectionLoc().y + 90.0f + 10.0f + 5.0f + 10.0f + 5.0f), (int)(this.getModelSelectionSize().x - 10.0f - 32.0f), 15);
            if (this.getCurrentAction() != null && this.getCurrentCalculation() != null) {
                this.custom_CalcValueText.setText(this.getCurrentCalculation().globalVar != null ? this.getCurrentCalculation().globalVar : String.valueOf(this.getCurrentCalculation().number));
            }
            this.custom_CalcValueText.setCursorPositionZero();
        } else {
            this.custom_CalcValueText.width = (int)(this.getModelSelectionSize().x - 10.0f - 32.0f);
            this.custom_CalcValueText.xPosition = (int)(this.getModelSelectionLoc().x + 5.0f + 32.0f);
            this.custom_CalcValueText.yPosition = (int)(this.getModelSelectionLoc().y + 90.0f + 10.0f + 5.0f + 10.0f + 5.0f);
        }
        if (this.custom_PackTitle == null) {
            this.custom_PackTitle = new GuiTextField(this.fontRendererObj, (int)(this.getActionWindowX() + 5.0f), 38, 150, 14);
            if (BendsPack.currentPack == 0) {
                this.custom_PackTitle.setText(I18n.format("mobends.pack.default.name"));
            } else {
                this.custom_PackTitle.setText(BendsPack.getCurrentPack().displayName);
            }
            this.custom_PackTitle.setCursorPositionZero();
        } else {
            this.custom_PackTitle.width = 150;
            this.custom_PackTitle.height = 14;
            this.custom_PackTitle.xPosition = (int)(this.getActionWindowX() + 5.0f);
            this.custom_PackTitle.yPosition = 38;
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        switch (par2) {
            case 1: {
                this.close();
            }
        }
        if (this.customizeWindow) {
            if (this.custom_AnimationNameText.isFocused()) {
                this.custom_AnimationNameText.textboxKeyTyped(par1, par2);
                this.assignAnimationToCurrentAction(this.custom_AnimationNameText.getText());
            } else if (this.custom_ModelNameText.isFocused()) {
                this.custom_ModelNameText.textboxKeyTyped(par1, par2);
                BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)this.custom_currentAction).model = this.custom_ModelNameText.getText();
            } else if (this.custom_CalcValueText.isFocused()) {
                this.custom_CalcValueText.textboxKeyTyped(par1, par2);
                this.assignCalcValue(this.custom_CalcValueText.getText());
            } else if (this.custom_PackTitle.isFocused()) {
                this.custom_PackTitle.textboxKeyTyped(par1, par2);
                if (BendsPack.currentPack == 0) {
                    this.createANewPack(this.custom_PackTitle.getText());
                }
                BendsPack.getCurrentPack().displayName = this.custom_PackTitle.getText();
            }
        }
    }

    public void close() {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents((boolean)false);
        MoBends.saveConfig();
        if (BendsPack.currentPack != 0) {
            try {
                BendsPack.getCurrentPack().save();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateScreen() {
        this.initGui();
        this.previewRotation += 2.0f;
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        if (this.presetWindowState > 0.0f & this.customizeWindow) {
            this.custom_AnimationNameText.updateCursorCounter();
            this.custom_ModelNameText.updateCursorCounter();
            this.custom_CalcValueText.updateCursorCounter();
            this.custom_PackTitle.updateCursorCounter();
            if (mouseY > 60 & (float)mouseY < 60.0f + this.getActionWindowHeight()) {
                String varAnim = "";
                int displayIndex = 0;
                if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) != null) {
                    for (int i = 0; i < BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size(); ++i) {
                        BendsAction action = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get(i);
                        if (!action.anim.equalsIgnoreCase(varAnim)) {
                            if (displayIndex > 0) {
                                ++displayIndex;
                            }
                            varAnim = action.anim;
                            ++displayIndex;
                        }
                        if ((float)mouseX >= this.getActionWindowX() + 10.0f & (float)mouseY >= (float)(65 + displayIndex * 18) + this.getYScrollAmount() & (float)mouseY <= (float)(65 + displayIndex * 18 + 18) + this.getYScrollAmount()) {
                            if (action.visual_DeletePopUp < 1.0f) {
                                action.visual_DeletePopUp += 0.2f;
                            }
                        } else if (action.visual_DeletePopUp > 0.0f) {
                            action.visual_DeletePopUp -= 0.2f;
                        }
                        ++displayIndex;
                    }
                    ++displayIndex;
                }
            }
        }
        if (this.scrolling_y) {
            this.scroll_y = (float)(mouseY - 60) / this.getActionWindowHeight() * (this.getActualActionWindowHeight() / this.getActionWindowHeight());
            if (this.scroll_y > 1.0f) {
                this.scroll_y = 1.0f;
            }
            if (this.scroll_y < 0.0f) {
                this.scroll_y = 0.0f;
            }
        }
        if (!Mouse.isButtonDown((int)0)) {
            this.scrolling_x = false;
            this.scrolling_y = false;
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int p_73864_3_) {
        if (this.packsWindow) {
            for (int i = 0; i < BendsPack.bendsPacks.size(); ++i) {
                if (!((float)x > (float)this.width + this.presetWindowState * -250.0f + 10.0f & (float)x < (float)this.width + this.presetWindowState * -250.0f + 10.0f + 200.0f & y > i * 70 + 30 & y < i * 70 + 30 + 64)) continue;
                if (i != BendsPack.currentPack && BendsPack.currentPack != 0) {
                    try {
                        BendsPack.getCurrentPack().save();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                BendsPack.currentPack = i;
                this.custom_PackTitle.setText((BendsPack.currentPack == 0 ? I18n.format("mobends.pack.default.name") : BendsPack.getCurrentPack().displayName));
                try {
                    BendsPack.getCurrentPack().apply();
                    continue;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.presetWindowState > 0.0f & this.customizeWindow) {
            this.custom_AnimationNameText.mouseClicked(x, y, p_73864_3_);
            this.custom_ModelNameText.mouseClicked(x, y, p_73864_3_);
            this.custom_PackTitle.mouseClicked(x, y, p_73864_3_);
            if (this.getActualActionWindowHeight() > this.getActionWindowHeight() && (float)x > this.getActionWindowX() + 500.0f - 128.0f - 20.0f - 10.0f & (float)x < this.getActionWindowX() + 500.0f - 128.0f - 20.0f - 10.0f + 10.0f & y > 60 & (float)y < 60.0f + this.getActionWindowHeight()) {
                this.scrolling_y = true;
            }
            if (this.getCurrentAction() != null) {
                if (y > this.custom_ModelNameText.yPosition + 15 + 5 + 10 & y < this.custom_ModelNameText.yPosition + 15 + 5 + 10 + 10) {
                    if ((float)x > this.getModelSelectionLoc().x + 5.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 18.0f) {
                        this.getCurrentAction().mod = null;
                    }
                    if ((float)x > this.getModelSelectionLoc().x + 5.0f + 18.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 36.0f) {
                        this.getCurrentAction().mod = BendsAction.EnumModifier.COS;
                    }
                    if ((float)x > this.getModelSelectionLoc().x + 5.0f + 36.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 54.0f) {
                        this.getCurrentAction().mod = BendsAction.EnumModifier.SIN;
                    }
                }
                if (this.getCurrentCalculation() != null) {
                    if (y > this.custom_ModelNameText.yPosition + 60 & y < this.custom_ModelNameText.yPosition + 60 + 10) {
                        if ((float)x > this.getModelSelectionLoc().x + 5.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 10.0f) {
                            this.getCurrentCalculation().operator = BendsAction.EnumOperator.ADD;
                        }
                        if ((float)x > this.getModelSelectionLoc().x + 5.0f + 10.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 20.0f) {
                            this.getCurrentCalculation().operator = BendsAction.EnumOperator.SUBSTRACT;
                        }
                        if ((float)x > this.getModelSelectionLoc().x + 5.0f + 20.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 30.0f) {
                            this.getCurrentCalculation().operator = BendsAction.EnumOperator.MULTIPLY;
                        }
                        if ((float)x > this.getModelSelectionLoc().x + 5.0f + 30.0f & (float)x < this.getModelSelectionLoc().x + 5.0f + 40.0f) {
                            this.getCurrentCalculation().operator = BendsAction.EnumOperator.SET;
                        }
                    }
                    this.custom_CalcValueText.mouseClicked(x, y, p_73864_3_);
                }
            }
            if ((BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) == null || BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size() <= 0) && (float)x >= this.getActionWindowX() & (float)x <= this.getActionWindowX() + 16.0f & (float)y >= 65.0f + this.getYScrollAmount() & (float)y <= 65.0f + this.getYScrollAmount() + 16.0f) {
                if (BendsPack.currentPack == 0) {
                    this.createANewPack(I18n.format("mobends.pack.untitled"));
                }
                this.addNewDefaultAction("all");
            }
            if (y > 60 & (float)y < 60.0f + this.getActionWindowHeight()) {
                String varAnim = "";
                int displayIndex = 0;
                if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) != null) {
                    for (int i = 0; i < BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size(); ++i) {
                        BendsAction action = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get(i);
                        if (!action.anim.equalsIgnoreCase(varAnim)) {
                            if (displayIndex > 0) {
                                if ((float)x >= this.getActionWindowX() + 10.0f & (float)x <= this.getActionWindowX() + 10.0f + 16.0f & (float)y >= (float)(65 + displayIndex * 18) + this.getYScrollAmount() & (float)y <= (float)(65 + displayIndex * 18) + this.getYScrollAmount() + 16.0f) {
                                    this.addNewDefaultAction(varAnim);
                                }
                                ++displayIndex;
                            }
                            varAnim = action.anim;
                            ++displayIndex;
                        }
                        if (action != null && (float)x > this.getActionWindowX() + 10.0f & (float)y > (float)(65 + displayIndex * 18) + this.getYScrollAmount() & (float)y < (float)(65 + displayIndex * 18 + 16) + this.getYScrollAmount()) {
                            if ((float)x > this.getActionWindowX() + 10.0f & (float)x < this.getActionWindowX() + 10.0f + 20.0f) {
                                if (action.prop == BendsAction.EnumBoxProperty.PREROT) {
                                    action.prop = BendsAction.EnumBoxProperty.ROT;
                                } else if (action.prop == BendsAction.EnumBoxProperty.ROT) {
                                    action.prop = BendsAction.EnumBoxProperty.SCALE;
                                } else if (action.prop == BendsAction.EnumBoxProperty.SCALE) {
                                    action.prop = BendsAction.EnumBoxProperty.PREROT;
                                }
                            }
                            if ((float)x > this.getActionWindowX() + 10.0f + 20.0f & (float)x < this.getActionWindowX() + 10.0f + 31.0f) {
                                if (action.axis == null) {
                                    action.axis = EnumAxis.X;
                                } else if (action.axis == EnumAxis.X) {
                                    action.axis = EnumAxis.Y;
                                } else if (action.axis == EnumAxis.Y) {
                                    action.axis = EnumAxis.Z;
                                } else if (action.axis == EnumAxis.Z) {
                                    action.axis = null;
                                }
                            }
                            this.custom_currentAction = i;
                            this.custom_currentChange = 0;
                            this.custom_AnimationNameText.setText(BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)this.custom_currentAction).anim);
                            this.custom_ModelNameText.setText(BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)this.custom_currentAction).model);
                            if (this.getCurrentCalculation() != null) {
                                this.custom_CalcValueText.setText(this.getCurrentCalculation().globalVar != null ? this.getCurrentCalculation().globalVar : String.valueOf(this.getCurrentCalculation().number));
                                this.custom_CalcValueText.setCursorPositionZero();
                            }
                            for (int s = 0; s < action.calculations.size(); ++s) {
                                BendsAction.Calculation calculation = action.calculations.get(s);
                                Minecraft.getMinecraft().renderEngine.bindTexture(calculation.operator == BendsAction.EnumOperator.ADD ? puzzle_action_add : (calculation.operator == BendsAction.EnumOperator.SUBSTRACT ? puzzle_action_substract : (calculation.operator == BendsAction.EnumOperator.SET ? puzzle_action_set : puzzle_action_multiply)));
                                if (!((float)x > this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(s * 57) & (float)x < this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(s * 57) + 64.0f - 7.0f)) continue;
                                this.custom_currentChange = s;
                                this.custom_CalcValueText.setText(this.getCurrentCalculation().globalVar != null ? this.getCurrentCalculation().globalVar : String.valueOf(this.getCurrentCalculation().number));
                                this.custom_CalcValueText.setCursorPositionZero();
                            }
                            if ((float)x > this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(action.calculations.size() * 57) & (float)x < this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(action.calculations.size() * 57) + 32.0f) {
                                BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.remove(i);
                                if (i <= this.custom_currentAction && this.custom_currentAction > 0) {
                                    --this.custom_currentAction;
                                }
                                --i;
                            }
                        }
                        ++displayIndex;
                    }
                    if ((float)x >= this.getActionWindowX() + 10.0f & (float)x <= this.getActionWindowX() + 10.0f + 16.0f & (float)y >= (float)(65 + displayIndex * 18) + this.getYScrollAmount() & (float)y <= (float)(65 + displayIndex * 18) + this.getYScrollAmount() + 16.0f) {
                        this.addNewDefaultAction(varAnim);
                    }
                    ++displayIndex;
                }
            }
        }
        super.mouseClicked(x, y, p_73864_3_);
    }

    public void createANewPack(String string) {
        BendsPack newPack = new BendsPack();
        newPack.filename = null;
        newPack.displayName = string;
        newPack.author = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
        newPack.description = I18n.format("mobends.pack.custom.description", Minecraft.getMinecraft().thePlayer.getCommandSenderName());
        BendsPack.bendsPacks.add(newPack);
        BendsPack.currentPack = BendsPack.bendsPacks.size() - 1;
        this.custom_PackTitle.setText(newPack.displayName);
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton.id >= 100) {
            this.animatedEntityID = par1GuiButton.id - 100;
            this.customizeWindow = true;
        } else if (par1GuiButton.id >= 10) {
            int s = 0;
            for (int i = 0; i < SettingsNode.settings.length; ++i) {
                if (!(SettingsNode.settings[i] instanceof SettingsBoolean)) continue;
                if (s == par1GuiButton.id - 10) {
                    ((SettingsBoolean)SettingsNode.settings[i]).data = !((SettingsBoolean)SettingsNode.settings[i]).data;
                    break;
                }
                ++s;
            }
        }
        switch (par1GuiButton.id) {
            case 0: {
                this.customizeWindow = false;
                this.settingsWindow = false;
                this.packsWindow = false;
                break;
            }
            case 1: {
                this.settingsWindow = true;
                break;
            }
            case 2: {
                AnimatedEntity.animatedEntities[this.animatedEntityID].animate = !AnimatedEntity.animatedEntities[this.animatedEntityID].animate;
                break;
            }
            case 3: {
                this.packsWindow = true;
                break;
            }
            case 4: {
                this.getCurrentAction().calculations.add(new BendsAction.Calculation(BendsAction.EnumOperator.SET, 0.0f));
                this.custom_currentChange = this.getCurrentAction().calculations.size() - 1;
                break;
            }
            case 5: {
                this.getCurrentAction().calculations.remove(this.getCurrentCalculation());
                --this.custom_currentChange;
                if (this.custom_currentChange >= 0) break;
                this.custom_currentChange = 0;
                break;
            }
            case 6: {
                Minecraft.getMinecraft().displayGuiScreen(new GuiBlinkSkinEditor(this));
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        GL11.glDisable((int)2896);
        GL11.glEnable((int)3042);
        if (this.titleTransition) {
            this.titleTransitionState += (128.0f - this.titleTransitionState) / 5.0f;
        } else {
            this.titleTransitionState += (100.0f - this.titleTransitionState) / 7.0f;
            for (float i = 0.0f; i < (float)this.buttonPositions.length; i += 1.0f) {
                if (!(this.buttonRevealState >= i / (float)this.buttonPositions.length)) continue;
                int n = (int)i;
                this.buttonPositions[n] = this.buttonPositions[n] + (90.0f - this.buttonPositions[(int)i]) / 4.0f;
            }
            this.buttonRevealState += 0.05f;
            this.leftBgState += (1.0f - this.leftBgState) / 4.0f;
        }
        if (this.titleTransitionState > 126.0f) {
            this.titleTransition = false;
        }
        this.presetWindowState = this.customizeWindow | this.settingsWindow | this.packsWindow ? (this.presetWindowState += (1.0f - this.presetWindowState) / 4.0f) : (this.presetWindowState += (0.0f - this.presetWindowState) / 4.0f);
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
        Draw.rectangle_xgradient(-this.leftBgState * -100.0f - 100.0f + this.presetWindowState * -100.0f, 0.0f, 100.0f, this.height, new Color(0.0f, 0.0f, 0.0f, 0.7f), new Color(0.0f, 0.0f, 0.0f, 0.3f));
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
        Draw.rectangle(-this.leftBgState * -105.0f - 105.0f + this.presetWindowState * -105.0f + 100.0f, 0.0f, 5.0f, this.height);
        GL11.glPopMatrix();
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        this.mc.renderEngine.bindTexture(menuTitleTexture);
        Draw.rectangle((float)(this.width / 2 - 64) + this.presetWindowState * (float)(-this.width / 2 + 80), this.titleTransitionState - 100.0f, 128.0f, 64.0f);
        if (this.presetWindowState > 0.0f) {
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
            GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
            float varWidth = this.packsWindow ? 250.0f : 500.0f;
            Draw.rectangle((float)this.width + this.presetWindowState * -varWidth, 0.0f, varWidth, 20.0f);
            Draw.rectangle((float)this.width + this.presetWindowState * -varWidth, 25.0f, varWidth, this.height);
            GL11.glPopMatrix();
        }
        if (this.presetWindowState > 0.0f & this.customizeWindow) {
            this.displayCustomizeWindow();
        }
        if (this.presetWindowState > 0.0f & this.settingsWindow) {
            String title = I18n.format("mobends.menu.settings");
            this.drawString(this.fontRendererObj, title, (int)((float)this.width + this.presetWindowState * -500.0f + 250.0f - (float)(this.fontRendererObj.getStringWidth(title) / 2)), 5, 0xFFFFFF);
        }
        if (this.presetWindowState > 0.0f & this.packsWindow) {
            String title = I18n.format("mobends.menu.packs");
            this.drawString(this.fontRendererObj, title, (int)((float)this.width + this.presetWindowState * -250.0f + 125.0f - (float)(this.fontRendererObj.getStringWidth(title) / 2)), 5, 0xFFFFFF);
            for (int i = 0; i < BendsPack.bendsPacks.size(); ++i) {
                String description = i == 0 ? I18n.format("mobends.pack.default.description") : BendsPack.bendsPacks.get(i).description;
                ArrayList<String> text = new ArrayList<String>();
                text.add("");
                int var1 = 0;
                int lineLength = 0;
                int s = 0;
                while (s < description.length()) {
                    text.set(var1, (String)text.get(var1) + description.charAt(s));
                    if ((float)this.fontRendererObj.getStringWidth((String)text.get(var1)) * 0.5f > 128.0f && description.charAt(s) == ' ') {
                        lineLength = 0;
                        ++var1;
                        text.add("");
                    }
                    ++s;
                    ++lineLength;
                }
                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
                GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
                if (i != BendsPack.currentPack) {
                    Draw.rectangle((float)this.width + this.presetWindowState * -250.0f + 10.0f, i * 70 + 30, 200.0f, 64.0f);
                } else {
                    Draw.rectangle_xgradient((float)this.width + this.presetWindowState * -250.0f + 10.0f, i * 70 + 30, 200.0f, 64.0f, new Color(0.0f, 0.0f, 0.0f, 0.5f), new Color(0.1f, 1.0f, 0.1f, 0.5f));
                }
                this.drawString(this.fontRendererObj, i == 0 ? I18n.format("mobends.pack.default.name") : BendsPack.bendsPacks.get((int)i).displayName, (int)((float)this.width + this.presetWindowState * -250.0f + 10.0f) + 5, i * 70 + 30 + 5, 0xFFFFFF);
                GL11.glPushMatrix();
                GL11.glTranslatef((float)((int)((float)this.width + this.presetWindowState * -250.0f + 10.0f) + 5), (float)(i * 70 + 30 + 5 + 10), (float)0.0f);
                GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
                this.drawString(this.fontRendererObj, I18n.format("mobends.pack.author", BendsPack.bendsPacks.get((int)i).author), 0, 0, 0x777777);
                for (s = 0; s < text.size(); ++s) {
                    this.drawString(this.fontRendererObj, (String)text.get(s), 0, 20 + s * 10, 0xFFFFFF);
                }
                GL11.glPopMatrix();
                GL11.glPopMatrix();
            }
        }
        int color = 47121212;
        super.drawScreen(par1, par2, par3);
    }

    public void renderLivingEntity(int argX, int argY, int scale, EntityLivingBase par5EntityLivingBase) {
        GL11.glEnable((int)2903);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)argX, (float)argY, (float)50.0f);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)this.previewRotation, (float)0.0f, (float)1.0f, (float)0.0f);
        float f2 = par5EntityLivingBase.renderYawOffset;
        float f3 = par5EntityLivingBase.rotationYaw;
        float f4 = par5EntityLivingBase.rotationPitch;
        float f5 = par5EntityLivingBase.prevRotationYawHead;
        float f6 = par5EntityLivingBase.rotationYawHead;
        GL11.glRotatef((float)135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef((float)-135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        par5EntityLivingBase.renderYawOffset = 0.0f;
        par5EntityLivingBase.rotationYaw = 0.0f;
        par5EntityLivingBase.rotationPitch = 0.0f;
        par5EntityLivingBase.rotationYawHead = par5EntityLivingBase.rotationYaw;
        par5EntityLivingBase.prevRotationYawHead = par5EntityLivingBase.rotationYaw;
        GL11.glTranslatef((float)0.0f, (float)par5EntityLivingBase.yOffset, (float)0.0f);
        RenderManager.instance.playerViewY = 180.0f;
        par5EntityLivingBase.moveForward = 1.0f;
        RenderManager.instance.renderEntityWithPosYaw(par5EntityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        par5EntityLivingBase.renderYawOffset = f2;
        par5EntityLivingBase.rotationYaw = f3;
        par5EntityLivingBase.rotationPitch = f4;
        par5EntityLivingBase.prevRotationYawHead = f5;
        par5EntityLivingBase.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable((int)32826);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable((int)3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    public void displayCustomizeWindow() {
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        String title = I18n.format("mobends.menu.customization");
        this.drawCenteredString(this.fontRendererObj, title, (int)((float)this.width + this.presetWindowState * -500.0f + 250.0f), 5, 0xFFFFFF);
        GL11.glPushMatrix();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.mc.renderEngine.bindTexture(displayBGTexture);
        Draw.rectangle((float)this.width + this.presetWindowState * -500.0f + 10.0f, 35.0f, 128.0f, 128.0f);
        GL11.glPopMatrix();
        if (AnimatedEntity.animatedEntities[this.animatedEntityID].id.equals("player")) {
            this.renderLivingEntity((int)((float)this.width + this.presetWindowState * -500.0f + 10.0f + 64.0f), 150, 50, Minecraft.getMinecraft().thePlayer);
        } else {
            AnimatedEntity.animatedEntities[this.animatedEntityID].entity.worldObj = Minecraft.getMinecraft().theWorld;
            this.renderLivingEntity((int)((float)this.width + this.presetWindowState * -500.0f + 10.0f + 64.0f), 150, 50, (EntityLivingBase)AnimatedEntity.animatedEntities[this.animatedEntityID].entity);
        }
        String warning = I18n.format("mobends.menu.restart");
        this.drawString(this.fontRendererObj, warning, (int)((float)this.width + this.presetWindowState * -500.0f + 20.0f), this.height - 20, 0xFFFFFF);
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
        Draw.rectangle(this.getModelSelectionLoc().x, this.getModelSelectionLoc().y, this.getModelSelectionSize().x, this.getModelSelectionSize().y);
        GL11.glPopMatrix();
        if (this.getCurrentAction() != null) {
            this.drawString(this.fontRendererObj, I18n.format("mobends.menu.animation"), this.custom_AnimationNameText.xPosition, this.custom_AnimationNameText.yPosition - 10, 0xFFFFFF);
            this.drawString(this.fontRendererObj, I18n.format("mobends.menu.model"), this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition - 10, 0xFFFFFF);
            this.drawString(this.fontRendererObj, I18n.format("mobends.menu.modifier"), this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition - 10 + 15 + 5 + 10, 0xFFFFFF);
            this.custom_AnimationNameText.drawTextBox();
            this.custom_ModelNameText.drawTextBox();
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentAction().mod == null ? puzzle_mod_none_selected : puzzle_mod_none);
            Draw.rectangle(this.getModelSelectionLoc().x + 5.0f, this.custom_ModelNameText.yPosition + 15 + 5 + 10, 32.0f, 16.0f);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentAction().mod == BendsAction.EnumModifier.COS ? puzzle_mod_cos_selected : puzzle_mod_cos);
            Draw.rectangle(this.getModelSelectionLoc().x + 5.0f + 18.0f, this.custom_ModelNameText.yPosition + 15 + 5 + 10, 32.0f, 16.0f);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentAction().mod == BendsAction.EnumModifier.SIN ? puzzle_mod_sin_selected : puzzle_mod_sin);
            Draw.rectangle(this.getModelSelectionLoc().x + 5.0f + 36.0f, this.custom_ModelNameText.yPosition + 15 + 5 + 10, 32.0f, 16.0f);
            GL11.glPopMatrix();
            this.drawString(this.fontRendererObj, I18n.format("mobends.menu.calculation"), this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition - 10 + 60, 0xFFFFFF);
            if (this.getCurrentCalculation() != null) {
                this.drawString(this.fontRendererObj, I18n.format("mobends.menu.value"), this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition + 15 + 5 + 60 - 1, this.isValidCalcValue(this.custom_CalcValueText.getText()) ? 0xFFFFFF : 0xFF0000);
                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentCalculation().operator == BendsAction.EnumOperator.ADD ? puzzle_calc_add_selected : puzzle_calc_add);
                Draw.rectangle(this.getModelSelectionLoc().x + 5.0f, this.custom_ModelNameText.yPosition + 60, 16.0f, 16.0f);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentCalculation().operator == BendsAction.EnumOperator.SUBSTRACT ? puzzle_calc_substract_selected : puzzle_calc_substract);
                Draw.rectangle(this.getModelSelectionLoc().x + 5.0f + 10.0f, this.custom_ModelNameText.yPosition + 60, 16.0f, 16.0f);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentCalculation().operator == BendsAction.EnumOperator.MULTIPLY ? puzzle_calc_multiply_selected : puzzle_calc_multiply);
                Draw.rectangle(this.getModelSelectionLoc().x + 5.0f + 20.0f, this.custom_ModelNameText.yPosition + 60, 16.0f, 16.0f);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(this.getCurrentCalculation().operator == BendsAction.EnumOperator.SET ? puzzle_calc_set_selected : puzzle_calc_set);
                Draw.rectangle(this.getModelSelectionLoc().x + 5.0f + 30.0f, this.custom_ModelNameText.yPosition + 60, 16.0f, 16.0f);
                GL11.glPopMatrix();
                this.custom_CalcValueText.drawTextBox();
            }
        }
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
        GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.7f);
        Draw.rectangle(this.getActionWindowX(), 35.0f, 342.0f, 20.0f);
        Draw.rectangle(this.getActionWindowX(), 60.0f, 342.0f, this.getActionWindowHeight());
        GL11.glPopMatrix();
        if (this.getActualActionWindowHeight() > this.getActionWindowHeight()) {
            GL11.glPushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            Draw.rectangle(this.getActionWindowX() + 500.0f - 128.0f - 20.0f - 10.0f, 60.0f + this.scroll_y * (this.getActualActionWindowHeight() - this.getActionWindowHeight()) * (this.getActionWindowHeight() / this.getActualActionWindowHeight()), 10.0f, this.getActionWindowHeight() * (this.getActionWindowHeight() / this.getActualActionWindowHeight()));
            GL11.glPopMatrix();
        }
        this.custom_PackTitle.drawTextBox();
        float scale = Minecraft.getMinecraft().displayWidth / this.width;
        GL11.glViewport((int)((int)((float)Minecraft.getMinecraft().displayWidth + (float)((int)(this.presetWindowState * -500.0f + 10.0f + 128.0f + 10.0f)) * scale)), (int)((int)(30.0f * scale)), (int)((int)(342.0f * scale)), (int)((int)((float)Minecraft.getMinecraft().displayHeight + -90.0f * scale)));
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)((float)this.width + this.presetWindowState * -500.0f + 10.0f + 128.0f + 10.0f), (double)((float)this.width + this.presetWindowState * -500.0f + 10.0f + 128.0f + 10.0f + 500.0f - 128.0f - 20.0f - 10.0f), (double)(60 + this.height - 25 - 40 - 25), (double)60.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
        String varAnim = "";
        int displayIndex = 0;
        if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) != null && BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size() > 0) {
            for (int i = 0; i < BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size(); ++i) {
                BendsAction action = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get(i);
                if (!action.anim.equalsIgnoreCase(varAnim)) {
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                    if (displayIndex > 0) {
                        Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
                        Draw.rectangle(this.getActionWindowX() + 10.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 16.0f, 16.0f);
                        ++displayIndex;
                    }
                    Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_animation);
                    Draw.rectangle(this.getActionWindowX(), (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 128.0f, 16.0f);
                    this.drawString(this.fontRendererObj, action.anim, (int)(this.getActionWindowX() + 5.0f), (int)((float)(69 + displayIndex * 18) + this.getYScrollAmount()), 0xFFFFFF);
                    varAnim = action.anim;
                    ++displayIndex;
                }
                if (this.custom_currentAction == i) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.0f);
                    Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
                    Draw.rectangle_xgradient(this.getActionWindowX() + 10.0f - 5.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount() - 1.0f, (float)(88 + (action.mod != null ? 25 : 0) + action.calculations.size() * 57 - 16 - 16) + 23.0f * action.visual_DeletePopUp + 50.0f + 5.0f, 18.0f, new Color(1.0f, 0.7f, 0.2f, 0.2f), new Color(1.0f, 0.7f, 0.2f, 1.0f));
                    GL11.glPopMatrix();
                }
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                Minecraft.getMinecraft().renderEngine.bindTexture(action.prop == BendsAction.EnumBoxProperty.ROT ? puzzle_rot : (action.prop == BendsAction.EnumBoxProperty.SCALE ? puzzle_scale : puzzle_prerot));
                Draw.rectangle(this.getActionWindowX() + 10.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 32.0f, 16.0f);
                if (action != null) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_delete);
                    Draw.rectangle(this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(action.calculations.size() * 57) - 16.0f - 16.0f + 23.0f * action.visual_DeletePopUp, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 32.0f, 16.0f);
                    this.drawString(this.fontRendererObj, action.axis == EnumAxis.X ? "x" : (action.axis == EnumAxis.Y ? "y" : (action.axis == EnumAxis.Z ? "z" : "?")), (int)(this.getActionWindowX() + 10.0f + 22.0f), (int)((float)(69 + displayIndex * 18) + this.getYScrollAmount()), 0xFFFFFF);
                    Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_model);
                    Draw.rectangle(this.getActionWindowX() + 10.0f + 31.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 64.0f, 16.0f);
                    this.drawString(this.fontRendererObj, action.model, (int)(this.getActionWindowX() + 5.0f + 10.0f + 31.0f), (int)((float)(69 + displayIndex * 18) + this.getYScrollAmount()), 0xFFFFFF);
                    if (action.mod != null) {
                        Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_mod);
                        Draw.rectangle(this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 32.0f, 16.0f);
                        this.drawString(this.fontRendererObj, action.mod == BendsAction.EnumModifier.COS ? "cos" : "sin", (int)(this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + 5.0f), (int)((float)(69 + displayIndex * 18) + this.getYScrollAmount()), 0xFFFFFF);
                    }
                    for (int s = 0; s < action.calculations.size(); ++s) {
                        BendsAction.Calculation calculation = action.calculations.get(s);
                        Minecraft.getMinecraft().renderEngine.bindTexture(calculation.operator == BendsAction.EnumOperator.ADD ? puzzle_action_add : (calculation.operator == BendsAction.EnumOperator.SUBSTRACT ? puzzle_action_substract : (calculation.operator == BendsAction.EnumOperator.SET ? puzzle_action_set : puzzle_action_multiply)));
                        GL11.glPushMatrix();
                        if (this.custom_currentChange != s | this.custom_currentAction != i) {
                            GL11.glColor4f((float)0.7f, (float)0.7f, (float)0.7f, (float)1.0f);
                        } else {
                            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                        }
                        Draw.rectangle(this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0) + (float)(s * 57), (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 64.0f, 16.0f);
                        GL11.glPopMatrix();
                        this.drawString(this.fontRendererObj, "" + (calculation.globalVar != null ? calculation.globalVar : Float.valueOf(calculation.number)), (int)(this.getActionWindowX() + 10.0f + 31.0f + 64.0f - 7.0f + (float)(action.mod != null ? 25 : 0)) + s * 57 + 20, (int)((float)(69 + displayIndex * 18) + this.getYScrollAmount()), 0xFFFFFF);
                    }
                }
                ++displayIndex;
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
            Draw.rectangle(this.getActionWindowX() + 10.0f, (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 16.0f, 16.0f);
            ++displayIndex;
        } else {
            GL11.glPushMatrix();
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
            Draw.rectangle(this.getActionWindowX(), (float)(65 + displayIndex * 18) + this.getYScrollAmount(), 16.0f, 16.0f);
            GL11.glPopMatrix();
        }
        GL11.glViewport((int)0, (int)0, (int)Minecraft.getMinecraft().displayWidth, (int)Minecraft.getMinecraft().displayHeight);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        ScaledResolution res = new ScaledResolution(this.mc, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glOrtho((double)0.0, (double)res.getScaledWidth(), (double)res.getScaledHeight(), (double)0.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
    }

    public float getActualActionWindowHeight() {
        String varAnim = "";
        int displayIndex = 0;
        if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) != null) {
            for (int i = 0; i < BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size(); ++i) {
                BendsAction action = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get(i);
                if (!action.anim.equalsIgnoreCase(varAnim)) {
                    if (displayIndex > 0) {
                        ++displayIndex;
                    }
                    varAnim = action.anim;
                    ++displayIndex;
                }
                ++displayIndex;
            }
            ++displayIndex;
        }
        return ++displayIndex * 18;
    }

    public float getActionWindowHeight() {
        return this.height - 25 - 40 - 25;
    }

    public float getYScrollAmount() {
        return -this.scroll_y * (this.getActualActionWindowHeight() - this.getActionWindowHeight());
    }

    public float getActionWindowX() {
        return this.getPropertiesWindowX() + 10.0f + 128.0f + 10.0f + 5.0f;
    }

    public float getPropertiesWindowX() {
        return (float)this.width + this.presetWindowState * -this.getPropertiesWindowWidth();
    }

    public float getPropertiesWindowWidth() {
        return this.packsWindow ? 250.0f : 500.0f;
    }

    public Vector2f getModelSelectionLoc() {
        return new Vector2f(this.getPropertiesWindowX() + 10.0f, 200.0f);
    }

    public Vector2f getModelSelectionSize() {
        return new Vector2f(128.0f, 200.0f);
    }

    public void assignAnimationToCurrentAction(String argAnim) {
        BendsTarget target = BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id);
        if (target == null) {
            return;
        }
        BendsAction action = target.actions.get(this.custom_currentAction);
        if (!argAnim.equalsIgnoreCase(action.anim)) {
            target.actions.remove(this.custom_currentAction);
            action.anim = argAnim;
            int newIndex = 0;
            ArrayList<BendsAction> newActionList = new ArrayList<BendsAction>();
            boolean done = false;
            boolean properAnim = false;
            for (int i = 0; i < target.actions.size(); ++i) {
                if (target.actions.get((int)i).anim.equalsIgnoreCase(argAnim)) {
                    properAnim = true;
                } else if (properAnim & !done) {
                    newActionList.add(action);
                    done = true;
                    newIndex = i;
                }
                newActionList.add(target.actions.get(i));
            }
            if (!done) {
                newActionList.add(action);
                newIndex = newActionList.size() - 1;
            }
            target.actions = newActionList;
            this.custom_currentAction = newIndex;
        }
    }

    public boolean isValidCalcValue(String value) {
        if (value == null | value.isEmpty()) {
            return false;
        }
        if (NumberUtils.isNumber((String)value)) {
            return true;
        }
        return BendsVar.getGlobalVar(value) != Float.POSITIVE_INFINITY;
    }

    public void assignCalcValue(String value) {
        if (this.isValidCalcValue(value)) {
            if (this.getCurrentCalculation() != null) {
                if (NumberUtils.isNumber((String)value)) {
                    this.getCurrentCalculation().globalVar = null;
                    System.out.println(value + " = " + Float.valueOf(value));
                    this.getCurrentCalculation().number = Float.valueOf(value).floatValue();
                } else {
                    this.getCurrentCalculation().globalVar = value;
                }
            }
        } else {
            this.getCurrentCalculation().globalVar = null;
            this.getCurrentCalculation().number = 0.0f;
        }
    }

    public BendsAction getCurrentAction() {
        if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) == null) {
            return null;
        }
        if (this.custom_currentAction < BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size()) {
            return BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get(this.custom_currentAction);
        }
        return null;
    }

    public BendsAction.Calculation getCurrentCalculation() {
        if (this.getCurrentAction() == null || this.getCurrentAction().calculations.size() <= 0) {
            return null;
        }
        return this.getCurrentAction().calculations.get(this.custom_currentChange);
    }

    public void addNewDefaultAction(String argAnim) {
        if (BendsPack.getTargetByID(AnimatedEntity.animatedEntities[this.animatedEntityID].id) == null) {
            BendsPack.targets.add(new BendsTarget(AnimatedEntity.animatedEntities[this.animatedEntityID].id));
        }
        BendsAction action = new BendsAction(argAnim, "", BendsAction.EnumBoxProperty.ROT, null, 0.3f, 1.0f);
        BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.add(action);
        this.custom_currentAction = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.size() - 1;
        action.anim = "NULL";
        this.assignAnimationToCurrentAction(argAnim);
        if (this.custom_currentAction > 0) {
            action.model = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)(this.custom_currentAction - 1)).model;
            action.prop = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)(this.custom_currentAction - 1)).prop;
            action.axis = BendsPack.getTargetByID((String)AnimatedEntity.animatedEntities[this.animatedEntityID].id).actions.get((int)(this.custom_currentAction - 1)).axis;
        }
    }
}
