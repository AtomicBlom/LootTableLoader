package com.github.atomicblom.loottableloader;

import com.google.common.io.Files;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_ID;
import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_NAME;
import static com.github.atomicblom.loottableloader.LootTableLoaderMod.MOD_VERSION;

@SuppressWarnings({"MethodMayBeStatic", "ObjectAllocationInLoop", "ClassHasNoToStringMethod"})
@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION)
public class LootTableLoaderMod {

    private final Logger logger = LogManager.getLogger(LootTableLoaderMod.MOD_ID);

    static final String MOD_ID = "loot_table_loader";
    static final String MOD_NAME = "Loot Table Loader";
    static final String MOD_VERSION = "@MOD_VERSION@";

    private static File configDir;
    private static final FilenameFilter jsonFilter = (File dir, String name) -> name.toLowerCase().endsWith(".json");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        final File rootConfigDir = event.getModConfigurationDirectory();

        configDir = new File(rootConfigDir, "loot_table_loader");
        if (!configDir.exists()) {
            logger.info("Loot table config directory does not exist, creating");
            if (!configDir.mkdir()) {
                logger.error("Could not create Loot Table Manager configuration loot directory");
            }
        }

        final File[] files = configDir.listFiles(jsonFilter);
        if (files == null || files.length == 0) {
            logger.info("No files found in the Loot table config directory.");
            return;
        }
        int addedResources = 0;
        for (final File file : files) {
            final String fileName = file.getName();
            final int i = fileName.lastIndexOf('.');
            final String resourceName = fileName.substring(0, i);
            LootTableList.register(new ResourceLocation(MOD_ID, resourceName));
            addedResources++;
        }
        logger.info(String.format("Added %d additional loot tables", addedResources));
    }

    @NetworkCheckHandler
    public boolean networkCheckHandler(Map mods, Side side) {
        return true;
    }

    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        final String folderName = event.getServer().getFolderName();
        final File worldDirectory = event.getServer().getActiveAnvilConverter().getSaveLoader(folderName, false).getWorldDirectory();

        final File lootTableDirectory = new File(new File(worldDirectory, "data"), "loot_tables");
        final File ltmDirectory = new File(lootTableDirectory, MOD_ID);
        if (!ltmDirectory.exists()) {
            logger.info("Creating loot table directory in world for mod");
            if (!ltmDirectory.mkdirs()) {
                logger.error("Could not create Loot Table Manager world loot directory");
            }
        }

        final File[] files = configDir.listFiles(jsonFilter);
        if (files == null || files.length == 0) {
            logger.info("No files found in the Loot table config directory.");
            return;
        }
        int copiedLootTables = 0;
        for (final File file : files) {
            final String fileName = file.getName();
            try {
                Files.copy(file, new File(ltmDirectory, fileName));
                copiedLootTables++;
            } catch (final IOException e) {
                logger.error(String.format("Unable to copy %s to world loot directory", fileName));
            }
        }
        logger.info(String.format("Copied %d loot tables", copiedLootTables));
    }
}
