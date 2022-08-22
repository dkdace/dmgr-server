package com.dace.dmgr.util;

import java.util.HashMap;

public class CooldownManager {
    private static final HashMap<String, Long> cooldownList = new HashMap<>();

    public static void setCooldown(String id, long duration) {
        long time = System.currentTimeMillis();
        time += duration * 50;
        cooldownList.put(id, time);
    }

    public static void setCooldown(HasCooldown object, Cooldown id, long duration) {
        String saveId = object.getCooldownKey() + id;
        setCooldown(saveId, duration);
    }

    public static <T> void setCooldown(HasCooldown object, Cooldown id, T subId, long duration) {
        String saveId = object.getCooldownKey() + id + subId;
        setCooldown(saveId, duration);
    }

    public static void setCooldown(HasCooldown object, Cooldown id) {
        setCooldown(object, id, id.getDefaultValue());
    }

    public static <T> void setCooldown(HasCooldown object, Cooldown id, T subId) {
        setCooldown(object, id, subId, id.getDefaultValue());
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

    public static long getCooldown(HasCooldown object, Cooldown id) {
        String saveId = object.getCooldownKey() + id;
        return getCooldown(saveId);
    }

    public static <T> long getCooldown(HasCooldown object, Cooldown id, T subId) {
        String saveId = object.getCooldownKey() + id + subId;
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

    public static void addCooldown(HasCooldown object, Cooldown id, long duration) {
        String saveId = object.getCooldownKey() + id;
        addCooldown(saveId, duration);
    }

    public static <T> void addCooldown(HasCooldown object, Cooldown id, T subId, long duration) {
        String saveId = object.getCooldownKey() + id + subId;
        addCooldown(saveId, duration);
    }
}
