package com.dace.dmgr.effect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 특정 플레이어에게 재생 가능한 효과 기능을 제공하는 클래스.
 *
 * @see ParticleEffect
 * @see SoundEffect
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PlayableEffect {
    /** 효과 없음 */
    public static final PlayableEffect NONE = new PlayableEffect() {
        @Override
        public void play(@NonNull Location location, @NonNull Player player) {
            // 미사용
        }
    };

    /**
     * 지정한 효과 목록을 재생하는 효과를 생성하여 반환한다.
     *
     * @param playableEffects 효과 목록
     * @return 재생 가능한 효과
     */
    @NonNull
    public static PlayableEffect list(@NonNull PlayableEffect @NonNull ... playableEffects) {
        return new PlayableEffect() {
            @Override
            public void play(@NonNull Location location, @NonNull Player player) {
                for (PlayableEffect playableEffect : playableEffects)
                    playableEffect.play(location, player);
            }
        };
    }

    /**
     * 지정한 위치에 특정 플레이어를 대상으로 효과를 재생한다.
     *
     * @param location 재생 위치
     * @param player   대상 플레이어
     */
    public abstract void play(@NonNull Location location, @NonNull Player player);

    /**
     * 지정한 플레이어에게 효과를 재생한다.
     *
     * @param player 대상 플레이어
     */
    public final void play(@NonNull Player player) {
        play(player.getLocation(), player);
    }

    /**
     * 지정한 위치에 모든 플레이어를 대상으로 효과를 재생한다.
     *
     * @param location 재생 위치
     */
    public final void play(@NonNull Location location) {
        location.getWorld().getPlayers().forEach(player -> play(location, player));
    }

    /**
     * 재생 가능한 효과를 처리하는 인터페이스.
     *
     * <p>하나의 입력값을 받아 효과를 반환한다.</p>
     *
     * @param <T> 입력값의 타입
     */
    @FunctionalInterface
    public interface Function<T> {
        /**
         * 효과를 반환한다.
         *
         * @param value 입력값
         * @return 효과
         */
        @NonNull
        PlayableEffect apply(T value);
    }

    /**
     * 재생 가능한 효과를 처리하는 인터페이스.
     *
     * <p>두 개의 입력값을 받아 효과를 반환한다.</p>
     *
     * @param <T> 첫 번째 입력값의 타입
     * @param <U> 두 번째 입력값의 타입
     */
    @FunctionalInterface
    public interface BiFunction<T, U> {
        /**
         * 효과를 반환한다.
         *
         * @param value1 첫 번째 입력값
         * @param value2 두 번째 입력값
         * @return 효과
         */
        @NonNull
        PlayableEffect apply(T value1, U value2);
    }

    /**
     * 재생 가능한 효과를 처리하는 인터페이스.
     *
     * <p>세 개의 입력값을 받아 효과를 반환한다.</p>
     *
     * @param <T> 첫 번째 입력값의 타입
     * @param <U> 두 번째 입력값의 타입
     * @param <V> 세 번째 입력값의 타입
     */
    @FunctionalInterface
    public interface TriFunction<T, U, V> {
        /**
         * 효과를 반환한다.
         *
         * @param value1 첫 번째 입력값
         * @param value2 두 번째 입력값
         * @param value3 세 번째 입력값
         * @return 효과
         */
        @NonNull
        PlayableEffect apply(T value1, U value2, V value3);
    }
}
