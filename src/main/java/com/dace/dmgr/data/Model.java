package com.dace.dmgr.data;

import com.dace.dmgr.DMGR;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Model {
    private YamlConfiguration config;
    private final String key;
    private final String modelName;
    private File file;

    protected Model(String modelName, String key) {
        this.modelName = modelName;
        this.key = key;
    }

    protected Model(String modelName) {
        this.modelName = modelName;
        this.key = "default";
    }

    public String getKey() {
        return key;
    }

    protected void initConfig() {
        file = new File(DMGR.getPlugin().getDataFolder(), modelName + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        try {
            if (!file.exists()) config.save(file);
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void setConfig(String path, Object value) {
        try {
            config.set(key + "." + path, value);
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected int getConfigInt(String path) {
        return config.getInt(key + "." + path, 0);
    }

    protected int getConfigInt(String path, int def) {
        return config.getInt(key + "." + path, def);
    }

    protected double getConfigDouble(String path) {
        return config.getDouble(key + "." + path, 0.0);
    }

    protected double getConfigDouble(String path, double def) {
        return config.getDouble(key + "." + path, def);
    }

    protected String getConfigString(String path) {
        return config.getString(key + "." + path, "");
    }

    protected String getConfigString(String path, String def) {
        return config.getString(key + "." + path, def);
    }

    protected boolean getConfigBoolean(String path) {
        return config.getBoolean(key + "." + path, false);
    }

    protected boolean getConfigBoolean(String path, boolean def) {
        return config.getBoolean(key + "." + path, def);
    }
}
