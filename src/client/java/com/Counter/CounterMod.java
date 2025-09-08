package com.Counter;

import com.Counter.config.ConfigManager;
import com.Counter.config.CounterConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterMod implements ModInitializer {
	public static final String MOD_ID = "CounterMod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Managers
    public static ConfigManager<CounterConfig> configManager;   // Config manager

	@Override
	public void onInitialize() {
		LOGGER.info("Counter mod initialized!");

        // Initialize managers
        configManager = new ConfigManager<>(CounterConfig.class, MOD_ID);

        // Load the config
        configManager.loadConfig();
	}

	public static void onShutdown() {
		saveConfig();
	}

	public static void saveConfig() {
		// Save all config data:
        CounterMod.configManager.saveConfig();
	}
}
