package com.dace.dmgr;

import lombok.NonNull;

import java.util.Map;

/**
 * 인스턴스를 저장하는 클래스.
 *
 * <p>{@link Map}의 Wrapper 클래스로 사용된다.</p>
 *
 * @param <K> 키
 * @param <V> 값
 */
public abstract class Registry<K, V> {
    /**
     * @return Map 객체
     */
    protected abstract Map<K, V> getMap();

    /**
     * 지정한 키에 해당하는 값을 추가한다.
     *
     * @param key   키
     * @param value 값
     */
    public final void add(@NonNull K key, @NonNull V value) {
        getMap().putIfAbsent(key, value);
    }

    /**
     * 지정한 키에 해당하는 값을 제거한다.
     *
     * @param key 키
     */
    public final void remove(@NonNull K key) {
        getMap().remove(key);
    }

    /**
     * 지정한 키에 해당하는 값을 반환한다.
     *
     * @param key 키
     * @return 값
     */
    public final V get(@NonNull K key) {
        return getMap().get(key);
    }
}
