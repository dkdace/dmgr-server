package com.dace.dmgr.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * 쿨타임을 관리하는 클래스.
 */
@UtilityClass
public final class CooldownUtil {
    /** 진행중인 쿨타임 목록 (실행 주체 : (쿨타임 ID : 종료 시점)) */
    private static final WeakHashMap<@NonNull Object, HashMap<@NonNull String, Long>> cooldownMap = new WeakHashMap<>();

    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 식별자의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object   쿹라임을 적용할 대상
     * @param id       쿨타임 ID
     * @param duration 지속시간 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void setCooldown(@NonNull Object object, @NonNull String id, long duration) {
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");
        if (duration == -1)
            duration = Long.MAX_VALUE;

        cooldownMap.putIfAbsent(object, new HashMap<>());
        HashMap<String, Long> idMap = cooldownMap.get(object);
        if (duration == 0) {
            if (idMap != null)
                idMap.remove(id);

            return;
        }

        long time = System.currentTimeMillis() / 50 + duration;
        idMap.put(id, time);
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @param object 쿨타임을 실행하는 객체
     * @param id     쿨타임 ID
     * @return 남은 시간 (tick). 해당 ID의 쿨타임이 존재하지 않으면 0 반환
     */
    public static long getCooldown(@NonNull Object object, @NonNull String id) {
        if (cooldownMap.get(object) == null)
            return 0;

        HashMap<String, Long> idMap = cooldownMap.get(object);
        if (idMap == null)
            return 0;

        long time = idMap.getOrDefault(id, 0L) - System.currentTimeMillis() / 50;
        if (time <= 0) {
            idMap.remove(id);
            return 0;
        }

        return time;
    }
}
