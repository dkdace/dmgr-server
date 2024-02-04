package com.dace.dmgr.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * 쿨타임을 관리하는 클래스.
 *
 * @see Cooldown
 */
@UtilityClass
public final class CooldownUtil {
    /** 진행중인 쿨타임 목록 (실행 주체 : (식별자 : 종료 시점)) */
    private static final WeakHashMap<Object, HashMap<String, Long>> cooldownMap = new WeakHashMap<>();

    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 식별자의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object     쿹라임을 적용할 대상
     * @param identifier 쿨타임 식별자
     * @param duration   지속시간 (tick). -1로 설정 시 무한 지속
     */
    private static void setCooldown(@NonNull Object object, @NonNull String identifier, long duration) {
        cooldownMap.putIfAbsent(object, new HashMap<>());
        HashMap<String, Long> identifierMap = cooldownMap.get(object);
        if (duration == 0) {
            if (identifierMap != null)
                identifierMap.remove(identifier);

            return;
        }

        if (duration == -1)
            duration = 99999;

        long time = System.currentTimeMillis() / 50 + duration;
        identifierMap.put(identifier, time);
    }

    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object   쿨타임을 적용할 대상
     * @param cooldown 쿨타임 종류
     * @param id       쿨타임 ID
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public static void setCooldown(@NonNull Object object, @NonNull Cooldown cooldown, @NonNull String id, long duration) {
        setCooldown(object, cooldown + id, duration);
    }


    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 ID의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * <p>지속시간은 쿨타임의 기본값인 {@link Cooldown#getDefaultValue()}으로 설정된다.</p>
     *
     * @param object   쿨타임을 적용할 대상
     * @param cooldown 쿨타임 종류
     * @param id       쿨타임 ID
     */
    public static void setCooldown(@NonNull Object object, @NonNull Cooldown cooldown, @NonNull String id) {
        setCooldown(object, cooldown, id, cooldown.getDefaultValue());
    }

    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 종류의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * @param object   쿨타임을 적용할 대상
     * @param cooldown 쿨타임 종류
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public static void setCooldown(@NonNull Object object, @NonNull Cooldown cooldown, long duration) {
        setCooldown(object, cooldown.toString(), duration);
    }

    /**
     * 지정한 객체가 실행하는 쿨타임을 설정한다.
     *
     * <p>이미 해당 종류의 쿨타임이 존재할 경우 덮어쓴다.</p>
     *
     * <p>지속시간은 쿨타임의 기본값인 {@link Cooldown#getDefaultValue()}으로 설정된다.</p>
     *
     * @param object   쿨타임을 적용할 대상
     * @param cooldown 쿨타임 종류
     */
    public static void setCooldown(@NonNull Object object, @NonNull Cooldown cooldown) {
        setCooldown(object, cooldown, cooldown.getDefaultValue());
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @param object     쿨타임을 실행하는 객체
     * @param identifier 쿨타임 식별자
     * @return 남은 시간 (tick). 해당 식별자의 쿨타임이 존재하지 않으면 0 반환
     */
    private static long getCooldown(@NonNull Object object, @NonNull String identifier) {
        if (cooldownMap.get(object) == null)
            return 0;

        HashMap<String, Long> identifierMap = cooldownMap.get(object);
        if (identifierMap == null)
            return 0;

        long time = identifierMap.getOrDefault(identifier, 0L) - System.currentTimeMillis() / 50;
        if (time <= 0) {
            identifierMap.remove(identifier);
            return 0;
        }

        return time;
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @param object   쿨타임을 실행하는 객체
     * @param cooldown 쿨타임 종류
     * @return 남은 시간 (tick). 해당 종류의 쿨타임이 존재하지 않으면 0 반환
     */
    public static long getCooldown(@NonNull Object object, @NotNull Cooldown cooldown) {
        return getCooldown(object, cooldown.toString());
    }

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @param object   쿨타임을 실행하는 객체
     * @param cooldown 쿨타임 종류
     * @param id       쿨타임 ID
     * @return 남은 시간 (tick). 해당 ID의 쿨타임이 존재하지 않으면 0 반환
     */
    public static long getCooldown(@NonNull Object object, @NonNull Cooldown cooldown, @NonNull String id) {
        return getCooldown(object, cooldown + id);
    }
}
