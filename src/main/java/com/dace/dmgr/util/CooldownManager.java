package com.dace.dmgr.util;

import java.util.HashMap;

public class CooldownManager {
    private static final HashMap<String, Long> cooldownList = new HashMap<>();

    public static void setCooldown(String id, long duration) {
        long time = System.currentTimeMillis();
        time += duration * 50;
        cooldownList.put(id, time);
    }

    public static void setCooldown(YamlModel yamlModel, Enum id, long duration) {
        String saveId = yamlModel.getKey() + id;
        setCooldown(saveId, duration);
    }

    public static long getCooldown(String id) {
        Long time = cooldownList.get(id);
        if (time == null)
            return 0;
        else if (time - System.currentTimeMillis() <= 0) {
            cooldownList.remove(id);
            return 0;
        }

        return (time - System.currentTimeMillis()) / 50;
    }

    public static long getCooldown(YamlModel yamlModel, Enum id) {
        String saveId = yamlModel.getKey() + id;
        return getCooldown(saveId);
    }

    public static void addCooldown(String id, long duration) {
        Long time = cooldownList.get(id);
        if (time == null)
            setCooldown(id, duration);
        else {
            time += duration * 50;
            cooldownList.replace(id, time);
        }
    }

    public static void addCooldown(YamlModel yamlModel, Enum id, long duration) {
        String saveId = yamlModel.getKey() + id;
        addCooldown(saveId, duration);
    }
}
