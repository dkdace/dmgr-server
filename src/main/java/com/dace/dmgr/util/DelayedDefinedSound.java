package com.dace.dmgr.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * 딜레이가 포함된 일련의 효과음 재생 기능을 제공하는 클래스.
 *
 * <p>무기 재장전 효과음 등의 소리를 재생하기 위해 사용한다.</p>
 *
 * @see DefinedSound
 */
public final class DelayedDefinedSound {
    /** 딜레이별 일련의 효과음 목록 (딜레이 : 효과음) */
    private final HashMap<Long, DefinedSound> delayedNamedSounds;

    private DelayedDefinedSound(Builder builder) {
        this.delayedNamedSounds = builder.delayedNamedSounds;
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

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 위치에 소리를 재생한다.
     *
     * @param delayIndex       딜레이 인덱스 (tick). 0 이상의 값
     * @param location         위치
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @param pitchAdder       음정 증감량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Location location, double volumeMultiplier, double pitchAdder) {
        Validate.inclusiveBetween(0, Long.MAX_VALUE, delayIndex);
        Validate.inclusiveBetween(0, Double.MAX_VALUE, volumeMultiplier);

        DefinedSound definedSound = delayedNamedSounds.get(delayIndex);
        if (definedSound != null)
            definedSound.play(location, volumeMultiplier, pitchAdder);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 위치에 소리를 재생한다.
     *
     * @param delayIndex       딜레이 인덱스 (tick). 0 이상의 값
     * @param location         위치
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Location location, double volumeMultiplier) {
        play(delayIndex, location, volumeMultiplier, 0);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 위치에 소리를 재생한다.
     *
     * @param delayIndex 딜레이 인덱스 (tick). 0 이상의 값
     * @param location   위치
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Location location) {
        play(delayIndex, location, 1, 0);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param delayIndex       딜레이 인덱스 (tick). 0 이상의 값
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @param pitchAdder       음정 증감량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Player player, double volumeMultiplier, double pitchAdder) {
        DefinedSound definedSound = delayedNamedSounds.get(delayIndex);
        if (definedSound != null)
            definedSound.play(player, volumeMultiplier, pitchAdder);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param delayIndex       딜레이 인덱스 (tick). 0 이상의 값
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Player player, double volumeMultiplier) {
        play(delayIndex, player, volumeMultiplier, 0);
    }

    /**
     * 지정한 딜레이 인덱스에 해당하는 효과음이 있으면, 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param delayIndex 딜레이 인덱스 (tick). 0 이상의 값
     * @param player     대상 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(long delayIndex, @NonNull Player player) {
        play(delayIndex, player, 1, 0);
    }

    /**
     * {@link DelayedDefinedSound}의 빌더 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final HashMap<Long, DefinedSound> delayedNamedSounds = new HashMap<>();

        /**
         * 지정한 딜레이 인덱스에 해당하는 효과음을 추가한다.
         *
         * @param delayIndex   딜레이 인덱스 (tick). 0 이상의 값
         * @param soundEffects 효과음 목록
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public Builder add(long delayIndex, @NonNull DefinedSound.SoundEffect @NonNull ... soundEffects) {
            Validate.inclusiveBetween(0, Long.MAX_VALUE, delayIndex);

            delayedNamedSounds.put(delayIndex, new DefinedSound(soundEffects));
            return this;
        }

        /**
         * 딜레이가 포함된 일련의 효과음을 생성하여 반환한다.
         *
         * @return {@link DelayedDefinedSound}
         */
        @NonNull
        public DelayedDefinedSound build() {
            return new DelayedDefinedSound(this);
        }
    }
}
