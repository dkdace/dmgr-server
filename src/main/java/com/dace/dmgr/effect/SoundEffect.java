package com.dace.dmgr.effect;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 효과음 재생 기능을 제공하는 클래스.
 */
public final class SoundEffect extends PlayableEffect {
    /** 소리 이름 */
    private final String sound;
    /** 음량 */
    private final float volume;
    /** 음정 */
    private final float pitch;
    /** 음정의 분산도 */
    private final double pitchVariance;

    private SoundEffect(Builder builder) {
        this.sound = builder.sound;
        this.volume = (float) builder.volume;
        this.pitch = (float) builder.pitch;
        this.pitchVariance = builder.pitchVariance;
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @param sound 소리 이름
     * @return {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull String sound) {
        return new Builder(sound);
    }

    /**
     * 빌더 인스턴스를 생성하여 반환한다.
     *
     * @param sound 소리 종류
     * @return {@link Builder}
     */
    @NonNull
    public static Builder builder(@NonNull Sound sound) {
        return new Builder(sound.toString());
    }

    @Override
    public void play(@NonNull Location location, @NonNull Player player) {
        float finalPitch = (float) Math.max(0.5, Math.min(pitch + pitchVariance * (Math.random() - Math.random()) * 0.5, 2));

        if (sound.toUpperCase().equals(sound))
            player.playSound(location, Sound.valueOf(sound), volume, finalPitch);
        else
            player.playSound(location, sound, volume, finalPitch);
    }

    /**
     * {@link SoundEffect}의 빌더 클래스.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final String sound;
        private double volume = 1;
        private double pitch = 1;
        private double pitchVariance = 0;

        /**
         * 음량을 설정한다.
         *
         * <p>1을 초과하면 소리가 들리는 범위만 늘어난다.</p>
         *
         * @param volume 음량. 0 이상의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder volume(double volume) {
            Validate.isTrue(volume >= 0, "volume >= 0", volume);

            this.volume = volume;
            return this;
        }

        /**
         * 음정을 설정한다.
         *
         * @param pitch 음정. 0.5~2 사이의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder pitch(double pitch) {
            Validate.inclusiveBetween(0.5, 2.0, pitch, "2 >= pitch >= 0.5 (%f)", pitch);

            this.pitch = pitch;
            return this;
        }

        /**
         * 음정의 분산도를 설정한다.
         *
         * @param pitchVariance 음정의 분산도. 0 이상의 값
         * @return {@link Builder}
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        @NonNull
        public Builder pitchVariance(double pitchVariance) {
            Validate.isTrue(pitchVariance >= 0, "pitchVariance >= 0 (%f)", pitchVariance);

            this.pitchVariance = pitchVariance;
            return this;
        }

        /**
         * 효과음 인스턴스를 생성하여 반환한다.
         *
         * @return {@link SoundEffect}
         */
        @NonNull
        public SoundEffect build() {
            return new SoundEffect(this);
        }
    }
}
