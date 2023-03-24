package com.dace.dmgr.system;

import java.util.HashMap;

/**
 * 쿨타임을 관리하는 클래스.
 */
public class CooldownManager {
    /** 진행중인 쿨타임 목록 (쿨타임 ID : 남은 시간) */
    private static final HashMap<String, Long> cooldownMap = new HashMap<>();

    /**
     * 쿨타임을 설정한다.
     *
     * @param id       쿨타임 ID
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    private static void setCooldown(String id, long duration) {
        if (duration == 0)
            cooldownMap.remove(id);
        else {
            if (duration == -1)
                duration = 99999;
            long time = System.currentTimeMillis();
            time += duration * 50;
            cooldownMap.put(id, time);
        }
    }

    /**
     * 쿨타임을 설정한다.
     *
     * <p>객체는 {@link Object#hashCode()}으로 식별하며,
     * 이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object   대상
     * @param id       쿨타임 ID
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public static void setCooldown(Object object, Cooldown id, long duration) {
        setCooldown("" + object.hashCode() + id, duration);
    }

    /**
     * 쿨타임을 설정한다.
     *
     * <p>객체는 {@link Object#hashCode()}으로 식별하며,
     * 이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object   대상
     * @param id       쿨타임 ID
     * @param subId    쿨타임 부 ID
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public static void setCooldown(Object object, Cooldown id, Object subId, long duration) {
        setCooldown("" + object.hashCode() + id + subId, duration);
    }

    /**
     * 쿨타임을 설정한다.
     *
     * <p>객체는 {@link Object#hashCode()}으로 식별하며,
     * 이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * <p>지속시간은 쿨타임 ID의 기본값인 {@link Cooldown#getDefaultValue()}으로 설정된다.</p>
     *
     * @param object 대상
     * @param id     쿨타임 ID
     */
    public static void setCooldown(Object object, Cooldown id) {
        setCooldown(object, id, id.getDefaultValue());
    }

    /**
     * 쿨타임을 설정한다.
     *
     * <p>객체는 {@link Object#hashCode()}으로 식별하며,
     * 이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * <p>지속시간은 쿨타임 ID의 기본값인 {@link Cooldown#getDefaultValue()}으로 설정된다.</p>
     *
     * @param object 대상
     * @param id     쿨타임 ID
     * @param subId  쿨타임 부 ID
     */
    public static void setCooldown(Object object, Cooldown id, Object subId) {
        setCooldown(object, id, subId, id.getDefaultValue());
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * <p>해당 ID의 쿨타임이 존재하지 않으면 {@code 0}을 반환한다.</p>
     *
     * @param id 쿨타임 ID
     * @return 남은 시간 (tick)
     */
    private static long getCooldown(String id) {
        Long time = cooldownMap.get(id);
        if (time == null)
            return 0;
        else if (time - System.currentTimeMillis() <= 0) {
            cooldownMap.remove(id);
            return 0;
        }

        return (time - System.currentTimeMillis()) / 50;
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * <p>해당 ID의 쿨타임이 존재하지 않으면 {@code 0}을 반환한다.</p>
     *
     * @param object 대상
     * @param id     쿨타임 ID
     * @return 남은 시간 (tick)
     */
    public static long getCooldown(Object object, Cooldown id) {
        return getCooldown("" + object.hashCode() + id);
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * <p>해당 ID의 쿨타임이 존재하지 않으면 {@code 0}을 반환한다.</p>
     *
     * @param object 대상
     * @param id     쿨타임 ID
     * @param subId  쿨타임 부 ID
     * @return 남은 시간 (tick)
     */
    public static long getCooldown(Object object, Cooldown id, Object subId) {
        return getCooldown("" + object.hashCode() + id + subId);
    }

    /**
     * 쿨타임의 남은 시간을 추가한다.
     *
     * @param id       쿨타임 ID
     * @param duration 추가할 시간 (tick)
     */
    private static void addCooldown(String id, long duration) {
        Long time = cooldownMap.get(id);
        if (time == null)
            setCooldown(id, duration);
        else {
            time += duration * 50;
            cooldownMap.replace(id, time);
        }
    }

    /**
     * 쿨타임의 남은 시간을 추가한다.
     *
     * @param object   대상
     * @param id       쿨타임 ID
     * @param duration 추가할 시간 (tick)
     */
    public static void addCooldown(Object object, Cooldown id, long duration) {
        setCooldown(object, id, getCooldown(object, id) + duration);
    }

    /**
     * 쿨타임의 남은 시간을 추가한다.
     *
     * @param object   대상
     * @param id       쿨타임 ID
     * @param subId    쿨타임 부 ID
     * @param duration 추가할 시간 (tick)
     */
    public static void addCooldown(Object object, Cooldown id, Object subId, long duration) {
        setCooldown(object, id, subId, getCooldown(object, id, subId) + duration);
    }
}
