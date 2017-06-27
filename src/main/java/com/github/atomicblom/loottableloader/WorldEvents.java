package com.github.atomicblom.loottableloader;

import com.google.common.io.Files;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created by codew on 27/06/2017.
 */
@Mod.EventBusSubscriber
public class WorldEvents {
    @SubscribeEvent
    public static void theEvent(WorldEvent.Load worldEvent) {
        World world = worldEvent.getWorld();
        if (world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)world;

            File lootTableDirectory = new File(new File(worldServer.getSaveHandler().getWorldDirectory(), "data"), "loot_tables");
            File ltmDirectory = new File(lootTableDirectory, LootTableLoaderMod.MOD_ID);
            if (!ltmDirectory.exists()) {
                Logger.info("Creating loot table directory in world for mod");
                ltmDirectory.mkdirs();
            }

            for (File file : LootTableLoaderMod.configDir.listFiles(LootTableLoaderMod.jsonFilter)) {
                String fileName = file.getName();
                try {
                    Files.copy(file, new File(ltmDirectory, fileName));
                } catch (IOException e) {
                    Logger.severe("Unable to copy {} to world loot directory", fileName);
                }
            }
        }
    }
}
