package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * 소리 재생 기능을 제공하는 클래스.
 */
@UtilityClass
public final class SoundUtil {
    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리 종류
     * @param location 위치
     * @param volume   음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch) {
        validateArgs(volume, pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param location         위치
     * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리 이름
     * @param location 위치
     * @param volume   음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch) {
        validateArgs(volume, pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param location         위치
     * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 종류
     * @param player 대상 플레이어
     * @param volume 음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch) {
        validateArgs(volume, pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param player           대상 플레이어
     * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 이름
     * @param player 대상 플레이어
     * @param volume 음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch) {
        validateArgs(volume, pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param player           대상 플레이어
     * @param volume           음량. 0 이상의 값. 1을 초과하면 소리가 들리는 범위만 늘어남
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 위치에 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound       이름이 지정된 효과음
     * @param location         위치
     * @param volumeMultiplier 음량 배수
     * @param pitchAdder       음정 증감량
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Location location, double volumeMultiplier, double pitchAdder) {
        for (NamedSound.DefinedSound definedSound : namedSound.getDefinedSounds()) {
            String soundName = definedSound.getSound();
            if (soundName.toUpperCase().equals(soundName))
                play(Sound.valueOf(soundName), location, definedSound.getVolume() * volumeMultiplier, definedSound.getPitch() + pitchAdder,
                        definedSound.getPitchSpreadRange());
            else
                play(soundName, location, definedSound.getVolume() * volumeMultiplier, definedSound.getPitch(), definedSound.getPitchSpreadRange());
        }
    }

    /**
     * 지정한 위치에 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound       이름이 지정된 효과음
     * @param location         위치
     * @param volumeMultiplier 음량 배수
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Location location, double volumeMultiplier) {
        playNamedSound(namedSound, location, volumeMultiplier, 0);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound       이름이 지정된 효과음
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수
     * @param pitchAdder       음정 증감량
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Player player, double volumeMultiplier, double pitchAdder) {
        for (NamedSound.DefinedSound definedSound : namedSound.getDefinedSounds()) {
            String soundName = definedSound.getSound();
            if (soundName.toUpperCase().equals(soundName))
                play(Sound.valueOf(soundName), player, definedSound.getVolume() * volumeMultiplier, definedSound.getPitch() + pitchAdder,
                        definedSound.getPitchSpreadRange());
            else
                play(soundName, player, definedSound.getVolume() * volumeMultiplier, definedSound.getPitch(), definedSound.getPitchSpreadRange());
        }
    }

    /**
     * 지정한 플레이어만 들을 수 있는 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound       이름이 지정된 효과음
     * @param player           대상 플레이어
     * @param volumeMultiplier 음량 배수
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Player player, double volumeMultiplier) {
        playNamedSound(namedSound, player, volumeMultiplier, 0);
    }

    /**
     * 지정한 위치에 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound 이름이 지정된 효과음
     * @param location   위치
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Location location) {
        playNamedSound(namedSound, location, 1);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 이름이 지정된 일련의 효과음을 재생한다.
     *
     * @param namedSound 이름이 지정된 효과음
     * @param player     대상 플레이어
     * @see NamedSound
     */
    public static void playNamedSound(@NonNull NamedSound namedSound, @NonNull Player player) {
        playNamedSound(namedSound, player, 1);
    }

    /**
     * 음정 분산도를 적용한 최종 음정 값을 반환한다.
     *
     * @param pitch            음정
     * @param pitchSpreadRange 음정의 분산도
     * @return 최종 음정 값
     */
    private static float getFinalPitch(double pitch, double pitchSpreadRange) {
        double spread = pitchSpreadRange * (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 0.5;
        return (float) Math.max(0.5, Math.min(pitch + spread, 2));
    }

    /**
     * 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param volume 음량
     * @param pitch  음정
     */
    private static void validateArgs(double volume, double pitch) {
        if (volume < 0)
            throw new IllegalArgumentException("'volume'이 0 이상이어야 함");
        if (pitch < 0.5 || pitch > 2)
            throw new IllegalArgumentException("'pitch'가 0.5에서 2 사이여야 함");
    }
}
