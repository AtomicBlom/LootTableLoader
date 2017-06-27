package com.github.atomicblom.loottableloader;

import com.google.common.io.Files;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_ID;
import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_NAME;

@Mod(modid = MOD_ID, name = MOD_NAME)
public class LootTableLoaderMod {
    public static final String MOD_ID = "loot_table_loader";
    public static final String MOD_NAME = "Loot Table Loader";
    @Nonnull
    public static File configDir;
    public static final FilenameFilter jsonFilter = (File dir, String name) -> name.toLowerCase().endsWith(".json");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File rootConfigDir = event.getModConfigurationDirectory();

        configDir = new File(rootConfigDir, "loot_table_loader");
        if (!configDir.exists()) {
            Logger.info("Loot table config directory does not exist, creating");
            configDir.mkdir();
        }

        int addedResources = 0;
        for (File file : configDir.listFiles(jsonFilter)) {
            String fileName = file.getName();
            int i = fileName.lastIndexOf(".");
            String resourceName = fileName.substring(0, i);
            LootTableList.register(new ResourceLocation(MOD_ID, resourceName));
            addedResources++;
        }
        Logger.info("Added %d additional loot tables", addedResources);
    }

    @NetworkCheckHandler
    public boolean networkCheckHandler(Map mods, Side side) {
        return true;
    }

    @Mod.EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        String folderName = event.getServer().getFolderName();
        File worldDirectory = event.getServer().getActiveAnvilConverter().getSaveLoader(folderName, false).getWorldDirectory();

        File lootTableDirectory = new File(new File(worldDirectory, "data"), "loot_tables");
        File ltmDirectory = new File(lootTableDirectory, LootTableLoaderMod.MOD_ID);
        if (!ltmDirectory.exists()) {
            Logger.info("Creating loot table directory in world for mod");
            if (!ltmDirectory.mkdirs()) {
                Logger.severe("Could not create Loot Table Manager world loot directory");
            }
        }

        int copiedLootTables = 0;
        for (File file : LootTableLoaderMod.configDir.listFiles(LootTableLoaderMod.jsonFilter)) {
            String fileName = file.getName();
            try {
                Files.copy(file, new File(ltmDirectory, fileName));
                copiedLootTables++;
            } catch (IOException e) {
                Logger.severe("Unable to copy %s to world loot directory", fileName);
            }
        }
        Logger.info("Copied %d loot tables", copiedLootTables);
    }
}
