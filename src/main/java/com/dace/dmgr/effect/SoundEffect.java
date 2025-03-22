package com.dace.dmgr.effect;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 일련의 효과음 재생 기능을 제공하는 클래스.
 */
public final class SoundEffect {
    /** 효과음 목록 */
    private final SoundInfo[] soundInfos;

    /**
     * 일련의 효과음을 생성한다.
     *
     * @param soundInfos 효과음 목록
     */
    public SoundEffect(@NonNull SoundInfo @NonNull ... soundInfos) {
        this.soundInfos = soundInfos;
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location    위치
     * @param volumeScale 음량 규모. 0 이상의 값
     * @param pitchScale  음정 규모. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Location location, double volumeScale, double pitchScale) {
        Validate.isTrue(volumeScale >= 0, "volumeScale >= 0 (%f)", volumeScale);
        Validate.isTrue(pitchScale >= 0, "pitchScale >= 0 (%f)", pitchScale);

        for (SoundInfo soundInfo : soundInfos)
            soundInfo.play(location, volumeScale, pitchScale);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location    위치
     * @param volumeScale 음량 규모. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Location location, double volumeScale) {
        play(location, volumeScale, 1);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location 위치
     */
    public void play(@NonNull Location location) {
        play(location, 1, 1);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player      대상 플레이어
     * @param volumeScale 음량 규모. 0 이상의 값
     * @param pitchScale  음정 규모. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Player player, double volumeScale, double pitchScale) {
        Validate.isTrue(volumeScale >= 0, "volumeScale >= 0 (%f)", volumeScale);
        Validate.isTrue(pitchScale >= 0, "pitchScale >= 0 (%f)", pitchScale);

        for (SoundInfo soundInfo : soundInfos)
            soundInfo.play(player, volumeScale, pitchScale);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player      대상 플레이어
     * @param volumeScale 음량 규모. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Player player, double volumeScale) {
        play(player, volumeScale, 1);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player 대상 플레이어
     */
    public void play(@NonNull Player player) {
        play(player, 1, 1);
    }

    /**
     * 효과음을 나타내는 클래스.
     */
    public static final class SoundInfo {
        /** 소리 이름 */
        private final String sound;
        /** 최소 음량 */
        private final double minVolume;
        /** 최대 음량 */
        private final double maxVolume;
        /** 최소 음정 */
        private final double minPitch;
        /** 최대 음정 */
        private final double maxPitch;
        /** 음정의 분산도 */
        private final double pitchVariance;

        private SoundInfo(Builder builder) {
            this.sound = builder.sound;
            this.minVolume = builder.minVolume;
            this.maxVolume = builder.maxVolume;
            this.minPitch = builder.minPitch;
            this.maxPitch = builder.maxPitch;
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

        /**
         * 최종 음정 값을 반환한다.
         *
         * @param pitchScale 음정 규모
         * @return 최종 음정 값
         */
        private float getFinalPitch(double pitchScale) {
            double pitch = minPitch + pitchScale * (maxPitch - minPitch);
            return (float) Math.max(0.5, Math.min(pitch + pitchVariance * (Math.random() - Math.random()) * 0.5, 2));
        }

        /**
         * 지정한 위치에 소리를 재생한다.
         *
         * @param location    위치
         * @param volumeScale 음량 규모
         * @param pitchScale  음정 규모
         */
        private void play(@NonNull Location location, double volumeScale, double pitchScale) {
            float finalVolume = (float) (minVolume + volumeScale * (maxVolume - minVolume));
            float finalPitch = getFinalPitch(pitchScale);

            if (sound.toUpperCase().equals(sound))
                location.getWorld().playSound(location, Sound.valueOf(sound), finalVolume, finalPitch);
            else
                location.getWorld().playSound(location, sound, finalVolume, finalPitch);
        }

        /**
         * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
         *
         * @param player      대상 플레이어
         * @param volumeScale 음량 규모
         * @param pitchScale  음정 규모
         */
        private void play(@NonNull Player player, double volumeScale, double pitchScale) {
            float finalVolume = (float) (minVolume + volumeScale * (maxVolume - minVolume));
            float finalPitch = getFinalPitch(pitchScale);

            if (sound.toUpperCase().equals(sound))
                player.playSound(player.getLocation(), Sound.valueOf(sound), finalVolume, finalPitch);
            else
                player.playSound(player.getLocation(), sound, finalVolume, finalPitch);
        }

        /**
         * {@link SoundInfo}의 빌더 클래스.
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {
            private final String sound;
            private double minVolume = 0;
            private double maxVolume = 1;
            private double minPitch = 1;
            private double maxPitch = 1;
            private double pitchVariance = 0;

            /**
             * 음량을 설정한다.
             *
             * <p>1을 초과하면 소리가 들리는 범위만 늘어난다.</p>
             *
             * @param minVolume 최소 음량. 0 이상의 값
             * @param maxVolume 최대 음량. {@code minVolume} 이상의 값
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder volume(double minVolume, double maxVolume) {
                Validate.isTrue(minVolume >= 0, "minVolume >= 0", minVolume);
                Validate.isTrue(maxVolume >= minVolume, "maxVolume >= %f (%f)", minVolume, maxVolume);

                this.minVolume = minVolume;
                this.maxVolume = maxVolume;

                return this;
            }

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
                return volume(0, volume);
            }

            /**
             * 음정을 설정한다.
             *
             * @param minPitch 최소 음정. 0.5~2 사이의 값
             * @param maxPitch 최대 음정. {@code minPitch}~2 사이의 값
             * @return {@link Builder}
             * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
             */
            @NonNull
            public Builder pitch(double minPitch, double maxPitch) {
                Validate.inclusiveBetween(0.5, 2.0, minPitch, "2 >= minPitch >= 0.5 (%f)", minPitch);
                Validate.inclusiveBetween(minPitch, 2.0, maxPitch, "2 >= maxPitch >= %f (%f)", minPitch, maxPitch);

                this.minPitch = minPitch;
                this.maxPitch = maxPitch;

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
                return pitch(pitch, pitch);
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
             * @return {@link SoundInfo}
             */
            @NonNull
            public SoundInfo build() {
                return new SoundInfo(this);
            }
        }
    }
}
