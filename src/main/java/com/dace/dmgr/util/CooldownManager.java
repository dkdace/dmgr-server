package com.dace.dmgr.util;

import com.dace.dmgr.data.Model;

import java.util.HashMap;

public class CooldownManager {
    private static final HashMap<String, Long> cooldownList = new HashMap<>();

    public static void setCooldown(String id, long duration) {
        long time = System.currentTimeMillis();
        time += duration * 50;
        cooldownList.put(id, time);
    }

    public static void setCooldown(Model model, Enum id, long duration) {
        String saveId = model.getKey() + id;

        long time = System.currentTimeMillis();
        time += duration * 50;
        cooldownList.put(saveId, time);
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

    public static long getCooldown(Model model, Enum id) {
        String saveId = model.getKey() + id;

        Long time = cooldownList.get(saveId);
        if (time == null)
            return 0;
        else if (time - System.currentTimeMillis() <= 0) {
            cooldownList.remove(saveId);
            return 0;
        }

        return (time - System.currentTimeMillis()) / 50;
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

    public static void addCooldown(Model model, Enum id, long duration) {
        String saveId = model.getKey() + id;

        Long time = cooldownList.get(saveId);
        if (time == null)
            setCooldown(saveId, duration);
        else {
            time += duration * 50;
            cooldownList.replace(saveId, time);
        }
    }
}
