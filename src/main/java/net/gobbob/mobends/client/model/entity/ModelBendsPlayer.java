package net.gobbob.mobends.client.model.entity;

import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.client.model.ModelBoxBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelBendsPlayer
extends ModelBiped {
    private static final int UPPER_SEGMENT_HEIGHT = 6;

    public ModelRenderer bipedRightForeArm;
    public ModelRenderer bipedLeftForeArm;
    public ModelRenderer bipedRightForeLeg;
    public ModelRenderer bipedLeftForeLeg;
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    public SwordTrail swordTrail = new SwordTrail();
    public float headRotationX;
    public float headRotationY;
    public float armSwing;
    public float armSwingAmount;
    private final float modelScale;
    private final float modelYOffset;

    private boolean modern;
    private boolean slim;

    private ModelRendererBends bodyWear;
    private ModelRendererBends rightLegWear;
    private ModelRendererBends rightForeLegWear;
    private ModelRendererBends leftLegWear;
    private ModelRendererBends leftForeLegWear;

    private ModelRendererBends classicRightArm;
    private ModelRendererBends classicRightForeArm;
    private ModelRendererBends classicLeftArm;
    private ModelRendererBends classicLeftForeArm;
    private ModelRendererBends slimRightArm;
    private ModelRendererBends slimRightForeArm;
    private ModelRendererBends slimLeftArm;
    private ModelRendererBends slimLeftForeArm;

    private ModelRendererBends classicRightArmWear;
    private ModelRendererBends classicRightForeArmWear;
    private ModelRendererBends classicLeftArmWear;
    private ModelRendererBends classicLeftForeArmWear;
    private ModelRendererBends slimRightArmWear;
    private ModelRendererBends slimRightForeArmWear;
    private ModelRendererBends slimLeftArmWear;
    private ModelRendererBends slimLeftForeArmWear;

    public ModelBendsPlayer() {
        this(0.0f);
    }

    public ModelBendsPlayer(float p_i1148_1_) {
        this(p_i1148_1_, 0.0f, 64, 32);
    }

    public ModelBendsPlayer(float p_i1149_1_, float p_i1149_2_, int p_i1149_3_, int p_i1149_4_) {
        this.modelScale = p_i1149_1_;
        this.modelYOffset = p_i1149_2_;
        this.textureWidth = p_i1149_3_;
        this.textureHeight = p_i1149_4_;
        this.bipedCloak = new ModelRendererBends(this, 0, 0);
        this.bipedCloak.addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1, p_i1149_1_);
        this.bipedEars = new ModelRendererBends(this, 24, 0);
        this.bipedEars.addBox(-3.0f, -6.0f, -1.0f, 6, 6, 1, p_i1149_1_);
        this.bipedHead = new ModelRendererBends(this, 0, 0);
        this.bipedHead.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i1149_1_);
        this.bipedHead.setRotationPoint(0.0f, 0.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, p_i1149_1_ + 0.5f);
        this.bipedHeadwear.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.bipedBody = new ModelRendererBends(this, 16, 16).setShowChildIfHidden(true);
        this.bipedBody.addBox(-4.0f, -12.0f, -2.0f, 8, 12, 4, p_i1149_1_);
        this.bipedBody.setRotationPoint(0.0f, 0.0f + p_i1149_2_ + 12.0f, 0.0f);
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody);
        this.bipedRightArm.addBox(-3.0f, -2.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightArm.setRotationPoint(-5.0f, 2.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends)this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0f, -2.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftArm.setRotationPoint(5.0f, 2.0f + p_i1149_2_ - 12.0f, 0.0f);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightLeg.setRotationPoint(-1.9f, 12.0f + p_i1149_2_, 0.0f);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftLeg.setRotationPoint(1.9f, 12.0f + p_i1149_2_, 0.0f);
        this.bipedRightForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedRightForeArm.addBox(0.0f, 0.0f, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeArm.setRotationPoint(-3.0f, 4.0f, 2.0f);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, 3, 0.0f, -6.0f);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().hideQuad(ModelBoxBends.TOP);
        this.bipedLeftForeArm = new ModelRendererBends(this, 40, 22);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0.0f, 0.0f, -4.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 2.0f);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, 3, 0.0f, -6.0f);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().hideQuad(ModelBoxBends.TOP);
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedRightForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i1149_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg, 3, 0.0f, -6.0f);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().hideQuad(ModelBoxBends.TOP);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 22);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0f, 0.0f, 0.0f, 4, 6, 4, p_i1149_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -2.0f);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg, 3, 0.0f, -6.0f);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().hideQuad(ModelBoxBends.TOP);
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        this.bipedHead.addChild(this.bipedHeadwear);
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends)this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends)this.bipedLeftForeArm);
        ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0.0f, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    }

    @Override
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        this.prepareModernOverlays();
        if (this.isChild) {
            float f6 = 2.0f;
            GL11.glPushMatrix();
            GL11.glScalef((float)(1.5f / f6), (float)(1.5f / f6), (float)(1.5f / f6));
            GL11.glTranslatef((float)0.0f, (float)(16.0f * p_78088_7_), (float)0.0f);
            this.bipedHead.render(p_78088_7_);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef((float)(1.0f / f6), (float)(1.0f / f6), (float)(1.0f / f6));
            GL11.glTranslatef((float)0.0f, (float)(24.0f * p_78088_7_), (float)0.0f);
            this.bipedBody.render(p_78088_7_);
            this.bipedRightArm.render(p_78088_7_);
            this.bipedLeftArm.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
            this.bipedHeadwear.render(p_78088_7_);
            GL11.glPopMatrix();
        } else {
            this.bipedBody.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
        }
    }

    @Override
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity) {
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        if (Minecraft.getMinecraft().theWorld.isRemote && Minecraft.getMinecraft().isGamePaused()) {
            return;
        }
        Data_Player data = Data_Player.get(argEntity.getEntityId());
        this.armSwing = argSwingTime;
        this.armSwingAmount = argSwingAmount;
        this.headRotationX = argHeadX;
        this.headRotationY = argHeadY;
        if (Minecraft.getMinecraft().currentScreen != null) {
            this.headRotationY = 0.0f;
        }
        ((ModelRendererBends)this.bipedHead).sync(data.head);
        ((ModelRendererBends)this.bipedHeadwear).sync(data.headwear);
        ((ModelRendererBends)this.bipedBody).sync(data.body);
        ((ModelRendererBends)this.bipedRightArm).sync(data.rightArm);
        ((ModelRendererBends)this.bipedLeftArm).sync(data.leftArm);
        ((ModelRendererBends)this.bipedRightLeg).sync(data.rightLeg);
        ((ModelRendererBends)this.bipedLeftLeg).sync(data.leftLeg);
        ((ModelRendererBends)this.bipedRightForeArm).sync(data.rightForeArm);
        ((ModelRendererBends)this.bipedLeftForeArm).sync(data.leftForeArm);
        ((ModelRendererBends)this.bipedRightForeLeg).sync(data.rightForeLeg);
        ((ModelRendererBends)this.bipedLeftForeLeg).sync(data.leftForeLeg);
        this.renderOffset.set(data.renderOffset);
        this.renderRotation.set(data.renderRotation);
        this.renderItemRotation.set(data.renderItemRotation);
        this.swordTrail = data.swordTrail;
        if (Data_Player.get(argEntity.getEntityId()).canBeUpdated()) {
            this.renderOffset.setSmooth(new Vector3f(0.0f, -1.0f, 0.0f), 0.5f);
            this.renderRotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
            this.renderItemRotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
            ((ModelRendererBends)this.bipedHead).resetScale();
            ((ModelRendererBends)this.bipedHeadwear).resetScale();
            ((ModelRendererBends)this.bipedBody).resetScale();
            ((ModelRendererBends)this.bipedRightArm).resetScale();
            ((ModelRendererBends)this.bipedLeftArm).resetScale();
            ((ModelRendererBends)this.bipedRightLeg).resetScale();
            ((ModelRendererBends)this.bipedLeftLeg).resetScale();
            ((ModelRendererBends)this.bipedRightForeArm).resetScale();
            ((ModelRendererBends)this.bipedLeftForeArm).resetScale();
            ((ModelRendererBends)this.bipedRightForeLeg).resetScale();
            ((ModelRendererBends)this.bipedLeftForeLeg).resetScale();
            BendsVar.tempData = Data_Player.get(argEntity.getEntityId());
            if (argEntity.isRiding()) {
                AnimatedEntity.getByEntity(argEntity).get("riding").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "riding");
            } else if (argEntity.isInWater()) {
                AnimatedEntity.getByEntity(argEntity).get("swimming").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "swimming");
            } else if (!Data_Player.get(argEntity.getEntityId()).isOnGround() | Data_Player.get((int)argEntity.getEntityId()).ticksAfterTouchdown < 2.0f) {
                AnimatedEntity.getByEntity(argEntity).get("jump").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "jump");
            } else {
                if (Data_Player.get((int)argEntity.getEntityId()).motion.x == 0.0f & Data_Player.get((int)argEntity.getEntityId()).motion.z == 0.0f) {
                    AnimatedEntity.getByEntity(argEntity).get("stand").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                    BendsPack.animate(this, "player", "stand");
                } else if (argEntity.isSprinting()) {
                    AnimatedEntity.getByEntity(argEntity).get("sprint").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                    BendsPack.animate(this, "player", "sprint");
                } else {
                    AnimatedEntity.getByEntity(argEntity).get("walk").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                    BendsPack.animate(this, "player", "walk");
                }
                if (argEntity.isSneaking()) {
                    AnimatedEntity.getByEntity(argEntity).get("sneak").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                    BendsPack.animate(this, "player", "sneak");
                }
            }
            if (this.aimedBow) {
                AnimatedEntity.getByEntity(argEntity).get("bow").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "bow");
            } else if (((EntityPlayer)argEntity).getCurrentEquippedItem() != null && ((EntityPlayer)argEntity).getCurrentEquippedItem().getItem() instanceof ItemPickaxe || ((EntityPlayer)argEntity).getCurrentEquippedItem() != null && Block.getBlockFromItem(((EntityPlayer)argEntity).getCurrentEquippedItem().getItem()) != Blocks.air) {
                AnimatedEntity.getByEntity(argEntity).get("mining").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "mining");
            } else if (((EntityPlayer)argEntity).getCurrentEquippedItem() != null && ((EntityPlayer)argEntity).getCurrentEquippedItem().getItem() instanceof ItemAxe) {
                AnimatedEntity.getByEntity(argEntity).get("axe").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "axe");
            } else {
                AnimatedEntity.getByEntity(argEntity).get("attack").animate((EntityLivingBase)argEntity, this, Data_Player.get(argEntity.getEntityId()));
                BendsPack.animate(this, "player", "attack");
            }
            ((ModelRendererBends)this.bipedHead).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedHeadwear).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedBody).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedLeftArm).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedRightArm).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedLeftLeg).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedRightLeg).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedLeftForeArm).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedRightForeArm).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedLeftForeLeg).update(data.ticksPerFrame);
            ((ModelRendererBends)this.bipedRightForeLeg).update(data.ticksPerFrame);
            this.renderOffset.update(data.ticksPerFrame);
            this.renderRotation.update(data.ticksPerFrame);
            this.renderItemRotation.update(data.ticksPerFrame);
            this.swordTrail.update(data.ticksPerFrame);
            data.updatedThisFrame = true;
        }
        Data_Player.get(argEntity.getEntityId()).syncModelInfo(this);
    }

    public void postRender(float argScale) {
        GL11.glTranslatef((float)(this.renderOffset.vSmooth.x * argScale), (float)(this.renderOffset.vSmooth.y * argScale), (float)(this.renderOffset.vSmooth.z * argScale));
        GL11.glRotatef((float)(-this.renderRotation.getX()), (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)(-this.renderRotation.getY()), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)this.renderRotation.getZ(), (float)0.0f, (float)0.0f, (float)1.0f);
    }

    public void postRenderArm(float argScale) {
        this.bipedRightArm.postRender(argScale);
        this.bipedRightForeArm.postRender(argScale);
        GL11.glTranslatef((float)(2.0f * argScale), (float)(4.0f * argScale), (float)(2.0f * argScale));
        GL11.glRotatef((float)this.renderItemRotation.vSmooth.x, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)this.renderItemRotation.vSmooth.y, (float)0.0f, (float)-1.0f, (float)0.0f);
        GL11.glRotatef((float)this.renderItemRotation.vSmooth.z, (float)0.0f, (float)0.0f, (float)1.0f);
    }

    public void updateWithEntityData(AbstractClientPlayer argPlayer) {
        Data_Player data = Data_Player.get(argPlayer.getEntityId());
        if (data != null) {
            this.renderOffset.set(data.renderOffset);
            this.renderRotation.set(data.renderRotation);
            this.renderItemRotation.set(data.renderItemRotation);
        }
    }

    /**
     * Overrides WawelAuth's interface method after its ModelBiped mixin is applied.
     * The segmented Mo' Bends hierarchy is retained instead of being replaced with vanilla limbs.
     */
    public void initModern() {
        if (this.modern) {
            return;
        }

        this.textureWidth = 64;
        this.textureHeight = 64;

        replaceBox((ModelRendererBends)this.bipedHead, 0, 0, false, -4.0f, -8.0f, -4.0f, 8, 8, 8, this.modelScale);
        replaceBox((ModelRendererBends)this.bipedHeadwear, 32, 0, false, -4.0f, -8.0f, -4.0f, 8, 8, 8, this.modelScale + 0.5f);
        replaceBox((ModelRendererBends)this.bipedBody, 16, 16, false, -4.0f, -12.0f, -2.0f, 8, 12, 4, this.modelScale);
        replaceBox((ModelRendererBends)this.bipedRightLeg, 0, 16, false, -2.0f, 0.0f, -2.0f, 4, 6, 4, this.modelScale);
        replaceBox((ModelRendererBends)this.bipedLeftLeg, 16, 48, false, -2.0f, 0.0f, -2.0f, 4, 6, 4, this.modelScale);
        replaceLowerBox((ModelRendererBends)this.bipedRightForeLeg, 0, 16, false, -2.0f, 0.0f, 0.0f, 4, 6, 4, this.modelScale);
        replaceLowerBox((ModelRendererBends)this.bipedLeftForeLeg, 16, 48, false, -2.0f, 0.0f, 0.0f, 4, 6, 4, this.modelScale);

        clearBox((ModelRendererBends)this.bipedRightArm);
        clearBox((ModelRendererBends)this.bipedRightForeArm);
        clearBox((ModelRendererBends)this.bipedLeftArm);
        clearBox((ModelRendererBends)this.bipedLeftForeArm);

        this.classicRightArm = createBox(40, 16, false, -3.0f, -2.0f, -2.0f, 4, 6, 4, this.modelScale, false);
        this.classicRightForeArm = createBox(40, 16, false, 0.0f, 0.0f, -4.0f, 4, 6, 4, this.modelScale, true);
        this.classicLeftArm = createBox(32, 48, false, -1.0f, -2.0f, -2.0f, 4, 6, 4, this.modelScale, false);
        this.classicLeftForeArm = createBox(32, 48, false, 0.0f, 0.0f, -4.0f, 4, 6, 4, this.modelScale, true);
        this.slimRightArm = createBox(40, 16, false, -2.0f, -2.0f, -2.0f, 3, 6, 4, this.modelScale, false);
        this.slimRightForeArm = createBox(40, 16, false, 0.0f, 0.0f, -4.0f, 3, 6, 4, this.modelScale, true);
        this.slimLeftArm = createBox(32, 48, false, -1.0f, -2.0f, -2.0f, 3, 6, 4, this.modelScale, false);
        this.slimLeftForeArm = createBox(32, 48, false, 0.0f, 0.0f, -4.0f, 3, 6, 4, this.modelScale, true);

        separateUpperJoint(this.classicRightArm);
        separateUpperJoint(this.classicLeftArm);
        separateUpperJoint(this.slimRightArm);
        separateUpperJoint(this.slimLeftArm);
        separateUpperJoint((ModelRendererBends)this.bipedRightLeg);
        separateUpperJoint((ModelRendererBends)this.bipedLeftLeg);

        float overlay = this.modelScale + 0.25f;
        this.bodyWear = createBox(16, 32, false, -4.0f, -12.0f, -2.0f, 8, 12, 4, overlay, false);
        this.rightLegWear = createBox(0, 32, false, -2.0f, 0.0f, -2.0f, 4, 6, 4, overlay, false);
        this.rightForeLegWear = createBox(0, 32, false, -2.0f, 0.0f, 0.0f, 4, 6, 4, overlay, true);
        this.leftLegWear = createBox(0, 48, false, -2.0f, 0.0f, -2.0f, 4, 6, 4, overlay, false);
        this.leftForeLegWear = createBox(0, 48, false, -2.0f, 0.0f, 0.0f, 4, 6, 4, overlay, true);

        this.classicRightArmWear = createBox(40, 32, false, -3.0f, -2.0f, -2.0f, 4, 6, 4, overlay, false);
        this.classicRightForeArmWear = createBox(40, 32, false, 0.0f, 0.0f, -4.0f, 4, 6, 4, overlay, true);
        this.classicLeftArmWear = createBox(48, 48, false, -1.0f, -2.0f, -2.0f, 4, 6, 4, overlay, false);
        this.classicLeftForeArmWear = createBox(48, 48, false, 0.0f, 0.0f, -4.0f, 4, 6, 4, overlay, true);
        this.slimRightArmWear = createBox(40, 32, false, -2.0f, -2.0f, -2.0f, 3, 6, 4, overlay, false);
        this.slimRightForeArmWear = createBox(40, 32, false, 0.0f, 0.0f, -4.0f, 3, 6, 4, overlay, true);
        this.slimLeftArmWear = createBox(48, 48, false, -1.0f, -2.0f, -2.0f, 3, 6, 4, overlay, false);
        this.slimLeftForeArmWear = createBox(48, 48, false, 0.0f, 0.0f, -4.0f, 3, 6, 4, overlay, true);

        separateUpperJoint(this.classicRightArmWear);
        separateUpperJoint(this.classicLeftArmWear);
        separateUpperJoint(this.slimRightArmWear);
        separateUpperJoint(this.slimLeftArmWear);
        separateUpperJoint(this.rightLegWear);
        separateUpperJoint(this.leftLegWear);

        this.bipedBody.addChild(this.bodyWear);
        this.bipedRightLeg.addChild(this.rightLegWear);
        this.bipedRightForeLeg.addChild(this.rightForeLegWear);
        this.bipedLeftLeg.addChild(this.leftLegWear);
        this.bipedLeftForeLeg.addChild(this.leftForeLegWear);

        addArmChildren((ModelRendererBends)this.bipedRightArm, this.classicRightArm, this.slimRightArm, this.classicRightArmWear, this.slimRightArmWear);
        addArmChildren((ModelRendererBends)this.bipedRightForeArm, this.classicRightForeArm, this.slimRightForeArm, this.classicRightForeArmWear, this.slimRightForeArmWear);
        addArmChildren((ModelRendererBends)this.bipedLeftArm, this.classicLeftArm, this.slimLeftArm, this.classicLeftArmWear, this.slimLeftArmWear);
        addArmChildren((ModelRendererBends)this.bipedLeftForeArm, this.classicLeftForeArm, this.slimLeftForeArm, this.classicLeftForeArmWear, this.slimLeftForeArmWear);

        this.modern = true;
        this.setSlim(false);
    }

    public void setSlim(boolean slim) {
        if (!this.modern) {
            return;
        }

        this.slim = slim;
        this.classicRightArm.showModel = !slim;
        this.classicRightForeArm.showModel = !slim;
        this.classicLeftArm.showModel = !slim;
        this.classicLeftForeArm.showModel = !slim;
        this.slimRightArm.showModel = slim;
        this.slimRightForeArm.showModel = slim;
        this.slimLeftArm.showModel = slim;
        this.slimLeftForeArm.showModel = slim;

        this.classicRightArmWear.showModel = !slim;
        this.classicLeftArmWear.showModel = !slim;
        this.slimRightArmWear.showModel = slim;
        this.slimLeftArmWear.showModel = slim;

        float armY = 2.0f + this.modelYOffset - 12.0f + (slim ? 0.5f : 0.0f);
        this.bipedRightArm.rotationPointY = armY;
        this.bipedLeftArm.rotationPointY = armY;
        this.bipedRightForeArm.rotationPointX = slim ? -2.0f : -3.0f;
    }

    public boolean isModern() {
        return this.modern;
    }

    public void render3DRightArmWear(float scale) {
        // WawelAuth's voxel meshes use vanilla single-piece transforms. The segmented model intentionally falls back to 2D.
    }

    public void setCurrentPlayerUuid(java.util.UUID uuid) {
        // Kept for WawelAuth's injected interface. UUID state is owned by WawelAuth.
    }

    public ModelRenderer getBodyWear() {
        return this.bodyWear;
    }

    public ModelRenderer getRightArmWear() {
        return this.slim ? this.slimRightArmWear : this.classicRightArmWear;
    }

    public ModelRenderer getLeftArmWear() {
        return this.slim ? this.slimLeftArmWear : this.classicLeftArmWear;
    }

    public ModelRenderer getRightLegWear() {
        return this.rightLegWear;
    }

    public ModelRenderer getLeftLegWear() {
        return this.leftLegWear;
    }

    public void prepareModernOverlays() {
        if (!this.modern) {
            return;
        }

        this.classicRightForeArmWear.showModel = !this.slim && this.classicRightArmWear.showModel;
        this.classicLeftForeArmWear.showModel = !this.slim && this.classicLeftArmWear.showModel;
        this.slimRightForeArmWear.showModel = this.slim && this.slimRightArmWear.showModel;
        this.slimLeftForeArmWear.showModel = this.slim && this.slimLeftArmWear.showModel;
        this.rightForeLegWear.showModel = this.rightLegWear.showModel;
        this.leftForeLegWear.showModel = this.leftLegWear.showModel;
    }

    private void addArmChildren(ModelRendererBends parent, ModelRendererBends classicPart, ModelRendererBends slimPart,
        ModelRendererBends classicWear, ModelRendererBends slimWear) {
        parent.addChild(classicPart);
        parent.addChild(slimPart);
        parent.addChild(classicWear);
        parent.addChild(slimWear);
    }

    private void separateUpperJoint(ModelRendererBends renderer) {
        renderer.getBox().expandXZ(0.01f);
    }

    private ModelRendererBends createBox(int textureX, int textureY, boolean mirror, float x, float y, float z,
        int width, int height, int depth, float scale, boolean lowerSegment) {
        ModelRendererBends renderer = new ModelRendererBends(this, textureX, textureY);
        renderer.setTextureSize(64, 64);
        renderer.mirror = mirror;
        renderer.addBox(x, y, z, width, height, depth, scale);
        if (lowerSegment) {
            offsetLowerSegmentUvs(renderer);
            if (scale > 0.0f) {
                renderer.getBox().insetMinY(scale * 2.0f);
            }
        }
        return renderer;
    }

    private void replaceBox(ModelRendererBends renderer, int textureX, int textureY, boolean mirror, float x,
        float y, float z, int width, int height, int depth, float scale) {
        renderer.setTextureSize(64, 64);
        renderer.txOffsetX = textureX;
        renderer.txOffsetY = textureY;
        renderer.mirror = mirror;
        renderer.cubeList.clear();
        renderer.addBox(x, y, z, width, height, depth, scale);
        renderer.compiled = false;
    }

    private void replaceLowerBox(ModelRendererBends renderer, int textureX, int textureY, boolean mirror, float x,
        float y, float z, int width, int height, int depth, float scale) {
        replaceBox(renderer, textureX, textureY, mirror, x, y, z, width, height, depth, scale);
        offsetLowerSegmentUvs(renderer);
    }

    private void clearBox(ModelRendererBends renderer) {
        renderer.cubeList.clear();
        renderer.compiled = false;
    }

    private void offsetLowerSegmentUvs(ModelRendererBends renderer) {
        ModelBoxBends box = renderer.getBox();
        box.hideQuad(ModelBoxBends.TOP);
        box.offsetTextureQuad(renderer, ModelBoxBends.LEFT, 0.0f, UPPER_SEGMENT_HEIGHT);
        box.offsetTextureQuad(renderer, ModelBoxBends.RIGHT, 0.0f, UPPER_SEGMENT_HEIGHT);
        box.offsetTextureQuad(renderer, ModelBoxBends.FRONT, 0.0f, UPPER_SEGMENT_HEIGHT);
        box.offsetTextureQuad(renderer, ModelBoxBends.BACK, 0.0f, UPPER_SEGMENT_HEIGHT);
    }
}
