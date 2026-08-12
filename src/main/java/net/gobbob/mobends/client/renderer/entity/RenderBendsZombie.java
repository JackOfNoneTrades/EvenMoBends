package net.gobbob.mobends.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

@SideOnly(value=Side.CLIENT)
public class RenderBendsZombie
extends RenderBiped {
    private static final ResourceLocation zombiePigmanTextures = new ResourceLocation("textures/entity/zombie_pigman.png");
    private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation zombieVillagerTextures = new ResourceLocation("textures/entity/zombie/zombie_villager.png");
    private ModelBiped zombieRegularModel = this.modelBipedMain;
    private ModelBendsZombieVillager zombieVillagerModel = new ModelBendsZombieVillager();
    protected ModelBiped zombieRegularArmorChestplate;
    protected ModelBiped zombieRegularArmor;
    protected ModelBiped villagerRegularArmorChestplate;
    protected ModelBiped villagerRegularArmor;
    private int field_82431_q = 1;
    public int refreshModel = 0;

    public RenderBendsZombie() {
        super(new ModelBendsZombie(), 0.5f, 1.0f);
    }

    @Override
    protected void func_82421_b() {
        this.field_82423_g = new ModelBendsZombie(1.0f, true);
        this.field_82425_h = new ModelBendsZombie(0.5f, true);
        this.zombieRegularArmorChestplate = this.field_82423_g;
        this.zombieRegularArmor = this.field_82425_h;
        this.villagerRegularArmorChestplate = new ModelBendsZombieVillager(1.0f, 0.0f, true);
        this.villagerRegularArmor = new ModelBendsZombieVillager(0.5f, 0.0f, true);
    }

    protected int shouldRenderPass(EntityZombie p_77032_1_, int p_77032_2_, float p_77032_3_) {
        this.func_82427_a(p_77032_1_);
        return super.shouldRenderPass(p_77032_1_, p_77032_2_, p_77032_3_);
    }

    public void doRender(EntityZombie p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        if (this.refreshModel != MoBends.refreshModel) {
            this.mainModel = new ModelBendsZombie();
            this.zombieRegularModel = this.modelBipedMain = (ModelBendsZombie)this.mainModel;
            this.zombieVillagerModel = new ModelBendsZombieVillager();
            this.refreshModel = MoBends.refreshModel;
        }
        this.func_82427_a(p_76986_1_);
        super.doRender(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    protected ResourceLocation getEntityTexture(EntityZombie p_110775_1_) {
        return p_110775_1_ instanceof EntityPigZombie ? zombiePigmanTextures : (p_110775_1_.isVillager() ? zombieVillagerTextures : zombieTextures);
    }

    protected void renderEquippedItems(EntityZombie p_77029_1_, float p_77029_2_) {
        this.func_82427_a(p_77029_1_);
        super.renderEquippedItems(p_77029_1_, p_77029_2_);
    }

    private void func_82427_a(EntityZombie p_82427_1_) {
        if (p_82427_1_.isVillager()) {
            if (this.field_82431_q != 10) {
                this.zombieVillagerModel = new ModelBendsZombieVillager();
                this.field_82431_q = 10;
                this.villagerRegularArmorChestplate = new ModelBendsZombieVillager(1.0f, 0.0f, true);
                this.villagerRegularArmor = new ModelBendsZombieVillager(0.5f, 0.0f, true);
            }
            this.mainModel = this.zombieVillagerModel;
            this.field_82423_g = this.villagerRegularArmorChestplate;
            this.field_82425_h = this.villagerRegularArmor;
        } else {
            this.mainModel = this.zombieRegularModel;
            this.field_82423_g = this.zombieRegularArmorChestplate;
            this.field_82425_h = this.zombieRegularArmor;
        }
        this.modelBipedMain = (ModelBiped)this.mainModel;
    }

    protected void rotateCorpse(EntityZombie p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_) {
        if (p_77043_1_.isConverting()) {
            p_77043_3_ += (float)(Math.cos((double)p_77043_1_.ticksExisted * 3.25) * Math.PI * 0.25);
        }
        super.rotateCorpse((EntityLivingBase)p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
    }

    @Override
    protected void renderEquippedItems(EntityLiving p_77029_1_, float p_77029_2_) {
        this.renderEquippedItems((EntityZombie)p_77029_1_, p_77029_2_);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLiving p_110775_1_) {
        return this.getEntityTexture((EntityZombie)p_110775_1_);
    }

    @Override
    public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        this.doRender((EntityZombie)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    @Override
    protected int shouldRenderPass(EntityLiving p_77032_1_, int p_77032_2_, float p_77032_3_) {
        return this.shouldRenderPass((EntityZombie)p_77032_1_, p_77032_2_, p_77032_3_);
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_) {
        return this.shouldRenderPass((EntityZombie)p_77032_1_, p_77032_2_, p_77032_3_);
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_) {
        this.renderEquippedItems((EntityZombie)p_77029_1_, p_77029_2_);
    }

    protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_) {
        this.rotateCorpse((EntityZombie)p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
    }

    @Override
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        this.doRender((EntityZombie)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return this.getEntityTexture((EntityZombie)p_110775_1_);
    }

    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
        this.doRender((EntityZombie)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}
