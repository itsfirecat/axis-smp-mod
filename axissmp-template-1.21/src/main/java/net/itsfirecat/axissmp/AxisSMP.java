package net.itsfirecat.axissmp;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.itsfirecat.axissmp.item.ModItems;
import net.itsfirecat.axissmp.item.ModItemGroups;
import net.itsfirecat.axissmp.util.InfinityState;
import net.itsfirecat.axissmp.qte.QTEManager;
import net.itsfirecat.block.ModBlocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxisSMP implements ModInitializer {
	public static final String MOD_ID = "axissmp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			InfinityState.tick();
			QTEManager.tick(server.getTicks());
		});
	}
}