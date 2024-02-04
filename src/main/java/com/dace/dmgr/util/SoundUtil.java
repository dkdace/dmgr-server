package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
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
     * @param volume   음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch) {
        validatePitch(pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param location         위치
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리 이름
     * @param location 위치
     * @param volume   음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch    음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch) {
        validatePitch(pitch);
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param location         위치
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Location location, double volume, double pitch, double pitchSpreadRange) {
        play(sound, location, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 종류
     * @param player 대상 플레이어
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch) {
        validatePitch(pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 종류
     * @param player           대상 플레이어
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull Sound sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리 이름
     * @param player 대상 플레이어
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch) {
        validatePitch(pitch);
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound            소리 이름
     * @param player           대상 플레이어
     * @param volume           음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch            음정. 0.5~2 사이의 값
     * @param pitchSpreadRange 음정의 분산도
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void play(@NonNull String sound, @NonNull Player player, double volume, double pitch, double pitchSpreadRange) {
        play(sound, player, volume, getFinalPitch(pitch, pitchSpreadRange));
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리 종류
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void broadcast(@NonNull Sound sound, double volume, double pitch) {
        validatePitch(pitch);
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch));
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리 이름
     * @param volume 음량. 1을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. 0.5~2 사이의 값
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    public static void broadcast(@NonNull String sound, double volume, double pitch) {
        validatePitch(pitch);
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, (float) volume, (float) pitch));
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
     * 음정 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param pitch 음정
     * @throws IllegalArgumentException {@code pitch}가 0.5~2 사이가 아니면 발생
     */
    private static void validatePitch(double pitch) {
        if (pitch < 0.5 || pitch > 2)
            throw new IllegalArgumentException("'pitch'가 0.5에서 2 사이여야 함");
    }
}
