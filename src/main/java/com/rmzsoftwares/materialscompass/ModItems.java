package com.rmzsoftwares.materialscompass;

import com.rmzsoftwares.materialscompass.items.MineralCompassItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MaterialsCompass.MODID);

    public static final RegistryObject<Item> MINERAL_COMPASS = ITEMS.register("mineral_compass", () -> new MineralCompassItem(new Item.Properties().stacksTo(1)));
}
