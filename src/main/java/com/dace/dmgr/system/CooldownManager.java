package com.dace.dmgr.system;

import java.util.HashMap;

public class CooldownManager {
    private static final HashMap<String, Long> cooldownList = new HashMap<>();

    public static void setCooldown(String id, long duration) {
        if (duration == 0)
            cooldownList.remove(id);
        else {
            if (duration == -1)
                duration = 99999;
            long time = System.currentTimeMillis();
            time += duration * 50;
            cooldownList.put(id, time);
        }
    }

    public static void setCooldown(Object object, Cooldown id, long duration) {
        String saveId = object.toString() + id;
        setCooldown(saveId, duration);
    }

    public static <T> void setCooldown(Object object, Cooldown id, T subId, long duration) {
        String saveId = object.toString() + id + subId;
        setCooldown(saveId, duration);
    }

    public static void setCooldown(Object object, Cooldown id) {
        setCooldown(object.toString(), id, id.getDefaultValue());
    }

    public static <T> void setCooldown(Object object, Cooldown id, T subId) {
        setCooldown(object.toString(), id, subId, id.getDefaultValue());
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

    public static long getCooldown(Object object, Cooldown id) {
        String saveId = object.toString() + id;
        return getCooldown(saveId);
    }

    public static <T> long getCooldown(Object object, Cooldown id, T subId) {
        String saveId = object.toString() + id + subId;
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

    public static void addCooldown(Object object, Cooldown id, long duration) {
        String saveId = object.toString() + id;
        addCooldown(saveId, duration);
    }

    public static <T> void addCooldown(Object object, Cooldown id, T subId, long duration) {
        String saveId = object.toString() + id + subId;
        addCooldown(saveId, duration);
    }
}
