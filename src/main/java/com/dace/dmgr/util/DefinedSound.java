package com.dace.dmgr.util;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 일련의 효과음 재생 기능을 제공하는 클래스.
 */
public final class DefinedSound {
    /** 효과음 목록 */
    private final SoundEffect[] soundEffects;

    /**
     * 일련의 효과음을 생성한다.
     *
     * @param soundEffects 효과음 목록
     */
    public DefinedSound(@NonNull SoundEffect @NonNull ... soundEffects) {
        this.soundEffects = soundEffects;
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location         위치
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @param pitchAdder       음정 증감량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Location location, double volumeMultiplier, double pitchAdder) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, volumeMultiplier);

        for (SoundEffect soundEffect : soundEffects) {
            String soundName = soundEffect.sound;
            double volume = soundEffect.volume * volumeMultiplier;
            double pitch = soundEffect.getFinalPitch(pitchAdder);

            if (soundName.toUpperCase().equals(soundName))
                location.getWorld().playSound(location, Sound.valueOf(soundName), (float) volume, (float) pitch);
            else
                location.getWorld().playSound(location, soundName, (float) volume, (float) pitch);
        }
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location         위치
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Location location, double volumeMultiplier) {
        play(location, volumeMultiplier, 0);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param location 위치
     */
    public void play(@NonNull Location location) {
        play(location, 1, 0);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @param pitchAdder       음정 증감량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Player player, double volumeMultiplier, double pitchAdder) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, volumeMultiplier);

        for (SoundEffect soundEffect : soundEffects) {
            String soundName = soundEffect.sound;
            double volume = soundEffect.volume * volumeMultiplier;
            double pitch = soundEffect.getFinalPitch(pitchAdder);

            if (soundName.toUpperCase().equals(soundName))
                player.playSound(player.getLocation(), Sound.valueOf(soundName), (float) volume, (float) pitch);
            else
                player.playSound(player.getLocation(), soundName, (float) volume, (float) pitch);
        }
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void play(@NonNull Player player, double volumeMultiplier) {
        play(player, volumeMultiplier, 0);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param player 대상 플레이어
     */
    public void play(@NonNull Player player) {
        play(player, 1, 0);
    }

    /**
     * 효과음을 나타내는 클래스.
     */
    public static final class SoundEffect {
        /** 소리 이름 */
        private final String sound;
        /** 음량 */
        private final double volume;
        /** 음정 */
        private final double pitch;
        /** 음정의 분산도 */
        private final double pitchSpreadRange;

        /**
         * 효과음 인스턴스를 생성한다.
         *
         * @param sound            소리 이름
         * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
         * @param pitch            음정. 0.5~2 사이의 값
         * @param pitchSpreadRange 음정의 분산도
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public SoundEffect(@NonNull String sound, double volume, double pitch, double pitchSpreadRange) {
            Validate.inclusiveBetween(0, Double.MAX_VALUE, volume);
            Validate.inclusiveBetween(0.5, 2, pitch);

            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.pitchSpreadRange = pitchSpreadRange;
        }

        /**
         * 효과음 인스턴스를 생성한다.
         *
         * @param sound  소리 이름
         * @param volume 음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
         * @param pitch  음정. 0.5~2 사이의 값
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public SoundEffect(@NonNull String sound, double volume, double pitch) {
            this(sound, volume, pitch, 0);
        }

        /**
         * 효과음 인스턴스를 생성한다.
         *
         * @param sound            소리 종류
         * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
         * @param pitch            음정. 0.5~2 사이의 값
         * @param pitchSpreadRange 음정의 분산도
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public SoundEffect(@NonNull Sound sound, double volume, double pitch, double pitchSpreadRange) {
            this(sound.toString(), volume, pitch, pitchSpreadRange);
        }

        /**
         * 효과음 인스턴스를 생성한다.
         *
         * @param sound  소리 종류
         * @param volume 음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
         * @param pitch  음정. 0.5~2 사이의 값
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public SoundEffect(@NonNull Sound sound, double volume, double pitch) {
            this(sound.toString(), volume, pitch, 0);
        }

        /**
         * 최종 음정 값을 반환한다.
         *
         * @param pitchAdder 음정 증감량
         * @return 최종 음정 값
         */
        private double getFinalPitch(double pitchAdder) {
            double pitchSpread = pitchSpreadRange * (Math.random() - Math.random()) * 0.5;
            return Math.max(0.5, Math.min(pitch + pitchAdder + pitchSpread, 2));
        }
    }
}
