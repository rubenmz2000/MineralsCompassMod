package com.rmzsoftwares.materialscompass.events;

import com.rmzsoftwares.materialscompass.MaterialsCompass;
import com.rmzsoftwares.materialscompass.ModItems;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaterialsCompass.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabEvents {
    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.MINERAL_COMPASS);
        }
    }
}
