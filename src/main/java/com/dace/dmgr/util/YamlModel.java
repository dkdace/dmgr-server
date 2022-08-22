package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class YamlModel {
    private final String key;
    private final String modelName;
    private YamlConfiguration config;
    private File file;

    protected YamlModel(String modelName, String key) {
        this.modelName = modelName;
        this.key = key;
        initConfig();
    }

    protected YamlModel(String modelName) {
        this.modelName = modelName;
        this.key = "default";
        initConfig();
    }

    private void initConfig() {
        file = new File(DMGR.getPlugin().getDataFolder(), modelName + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        try {
            if (!file.exists()) config.save(file);
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveValue(String path, Object value) {
        try {
            config.set(key + "." + path, value);
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected <T> T loadValue(String path) {
        return (T) config.get(key + "." + path);
    }

    protected <T> T loadValue(String path, T def) {
        return (T) config.get(key + "." + path, def);
    }

    protected <T> List<T> loadValues(String path) {
        return (List<T>) config.getList(key + "." + path);
    }

    protected <T> List<T> loadValues(String path, List<T> def) {
        return (List<T>) config.getList(key + "." + path, def);
    }
}
