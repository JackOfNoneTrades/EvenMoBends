package net.gobbob.mobends.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.CommonProxy;
import net.gobbob.mobends.compat.CompatibilityPolicy;
import net.gobbob.mobends.event.EventHandler_DataUpdate;
import net.gobbob.mobends.event.EventHandler_Keyboard;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class ClientProxy
extends CommonProxy {
    public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    public static final ResourceLocation texture_NULL = new ResourceLocation("mobends", "textures/white.png");
    public static final ResourceLocation GOBLIN_CAPE = new ResourceLocation("mobends", "textures/goblinCape.png");

    @Override
    public void preInit(Configuration config) {
        CompatibilityPolicy.logActiveMode();
        for (int i = 0; i < AnimatedEntity.animatedEntities.length; ++i) {
            AnimatedEntity.animatedEntities[i].animate = config.get("Animate", AnimatedEntity.animatedEntities[i].id, true).getBoolean();
        }
        AnimatedEntity.registerRendering();
        ClientRegistry.registerKeyBinding(EventHandler_Keyboard.key_Menu);
        MinecraftForge.EVENT_BUS.register(new EventHandler_DataUpdate());
        FMLCommonHandler.instance().bus().register(new EventHandler_DataUpdate());
        FMLCommonHandler.instance().bus().register(new EventHandler_Keyboard());
        ((SettingsBoolean)SettingsNode.getSetting((String)"swordTrail")).data = config.get("General", "Sword Trail", true).getBoolean();
        BendsPack.preInit(config);
    }
}
