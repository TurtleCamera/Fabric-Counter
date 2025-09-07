package com.Counter;

import com.Counter.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterMod implements ModInitializer {
	public static final String MOD_ID = "CounterMod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Counter mod initialized!");
	}

	public static void onShutdown() {
		saveConfig();
	}

	public static void saveConfig() {
		// Save all config data:
	}
}
