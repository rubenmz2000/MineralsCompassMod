package com.rmzsoftwares.materialscompass;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MaterialsCompass.MODID)
public class MaterialsCompass {
    public static final String MODID = "materialscompass";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public MaterialsCompass() {
        LOGGER.info("Materials Compass mod initialized!");
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Materials Compass client setup complete!");
    }
}
