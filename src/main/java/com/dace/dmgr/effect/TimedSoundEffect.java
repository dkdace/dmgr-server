package com.dace.dmgr.effect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * 시간이 지정된 일련의 효과음 재생 기능을 제공하는 클래스.
 *
 * <p>무기 재장전 효과음 등의 소리를 재생하기 위해 사용한다.</p>
 *
 * @see SoundEffect
 */
public final class TimedSoundEffect {
    /** 딜레이별 일련의 효과음 목록 (딜레이 : 효과음) */
    private final HashMap<Long, SoundEffect> timedSoundEffects;

    private TimedSoundEffect(Builder builder) {
        this.timedSoundEffects = builder.timedSoundEffects;
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @return {@link Builder}
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    private static void validateDelayIndex(long delayIndex) {
        Validate.isTrue(delayIndex >= 0, "delayIndex >= 0 (%d)", delayIndex);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 위치에 소리를 재생한다.
     *
     * @param delayIndex 딜레이 인덱스 (tick). 0 이상의 값
     * @param location   위치
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Location location) {
        validateDelayIndex(delayIndex);

        SoundEffect soundEffect = timedSoundEffects.get(delayIndex);
        if (soundEffect != null)
            soundEffect.play(location);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param delayIndex 딜레이 인덱스 (tick). 0 이상의 값
     * @param player     대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Player player) {
        validateDelayIndex(delayIndex);

        SoundEffect soundEffect = timedSoundEffects.get(delayIndex);
        if (soundEffect != null)
            soundEffect.play(player);
    }

    /**
     * {@link TimedSoundEffect}의 빌더 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final HashMap<Long, SoundEffect> timedSoundEffects = new HashMap<>();

        /**
         * 지정한 딜레이 인덱스에 해당하는 효과음을 추가한다.
         *
         * @param delayIndex 딜레이 인덱스 (tick). 0 이상의 값
         * @param soundInfos 효과음 목록
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder add(long delayIndex, @NonNull SoundEffect.SoundInfo @NonNull ... soundInfos) {
            validateDelayIndex(delayIndex);

            timedSoundEffects.put(delayIndex, new SoundEffect(soundInfos));
            return this;
        }

        /**
         * 시간이 지정된 일련의 효과음을 생성하여 반환한다.
         *
         * @return {@link TimedSoundEffect}
         */
        @NonNull
        public TimedSoundEffect build() {
            return new TimedSoundEffect(this);
        }
    }
}
