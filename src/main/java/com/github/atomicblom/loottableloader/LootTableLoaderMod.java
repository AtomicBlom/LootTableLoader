package com.github.atomicblom.loottableloader;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.FilenameFilter;

import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_ID;
import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_NAME;

@Mod(modid = MOD_ID, name = MOD_NAME)
public class LootTableLoaderMod {
    public static final String MOD_ID = "loot_table_loader";
    public static final String MOD_NAME = "Loot Table Loader";
    public static File configDir;
    public static final FilenameFilter jsonFilter = (File dir, String name) -> {
        return name.toLowerCase().endsWith(".json");
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File rootConfigDir = event.getModConfigurationDirectory();

        configDir = new File(rootConfigDir, "loot_table_loader");
        if (!configDir.exists()) {
            Logger.info("Loot table config directory does not exist, creating");
            configDir.mkdir();
        }


        for (File file : configDir.listFiles(jsonFilter)) {
            String fileName = file.getName();
            int i = fileName.lastIndexOf(".");
            String resourceName = fileName.substring(0, i);
            LootTableList.register(new ResourceLocation(MOD_ID, resourceName));
        }
    }
}
