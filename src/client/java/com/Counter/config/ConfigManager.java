package com.Counter.config;

import com.Counter.CounterMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

@SuppressWarnings({"FieldCanBeLocal", "CallToPrintStackTrace"})
public class ConfigManager<T> {
    private final String CONFIG_NAME = "config.json";
    private final String modName;
    private final Path fabricConfigPath;

    private final File modBaseDirectory;
    private final File modConfigFile;

    private final Gson gson;

    private T config;
    private final Class<T> clazz;

    public ConfigManager(Class<T> clazz, String modName) {
        this.modName = modName;
        this.fabricConfigPath = FabricLoader.getInstance().getConfigDir();

        this.modBaseDirectory = fabricConfigPath.resolve(this.modName).toFile();
        this.modConfigFile = new File(this.modBaseDirectory, CONFIG_NAME);

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.clazz = clazz;
        try {
            this.config = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        this.loadConfig();
    }

    public void loadConfig() {

        var createdSomething = createConfigIfNotExists();

        if(createdSomething) {
            this.saveConfig();
        }
        else {
            try {
                FileReader reader = new FileReader(this.modConfigFile);
                this.config = gson.fromJson(reader, this.clazz);
                reader.close();
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveConfig() {
        try {
            FileWriter writer = new FileWriter(this.modConfigFile);
            writer.write(gson.toJson(this.config));
            writer.close();
        }
        catch(Exception e) {
            CounterMod.LOGGER.error("Could not save configuration file:");
            e.printStackTrace();
        }
    }

    public T getConfig() {
        return this.config;
    }

    private boolean createConfigIfNotExists() {

        boolean createdSomething = false;

        // Create mod name directory:
        if(!this.modBaseDirectory.isDirectory()) {
            createdSomething = this.modBaseDirectory.mkdirs();
        }

        // Create the main mod config file:
        if(!this.modConfigFile.isFile()) {
            try {
                createdSomething = this.modConfigFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return createdSomething;
    }
}