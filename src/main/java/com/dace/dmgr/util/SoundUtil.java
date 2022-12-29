package com.dace.dmgr.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * 소리 재생 기능을 제공하는 클래스.
 */
public class SoundUtil {
    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리
     * @param location 위치
     * @param volume   음량
     * @param pitch    음정. {@code 0.5 ~ 2} 사이의 값
     */
    public static void play(Sound sound, Location location, Float volume, Float pitch) {
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }

    /**
     * 지정한 위치에 소리를 재생한다.
     *
     * @param sound    소리
     * @param location 위치
     * @param volume   음량. {@code 1}을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch    음정. {@code 0.5 ~ 2} 사이의 값
     */
    public static void play(String sound, Location location, Float volume, Float pitch) {
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리
     * @param volume 음량. {@code 1}을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. {@code 0.5 ~ 2} 사이의 값
     * @param player 대상 플레이어
     */
    public static void play(Sound sound, Float volume, Float pitch, Player player) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    /**
     * 지정한 플레이어만 들을 수 있는 소리를 재생한다.
     *
     * @param sound  소리
     * @param volume 음량. {@code 1}을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. {@code 0.5 ~ 2} 사이의 값
     * @param player 대상 플레이어
     */
    public static void play(String sound, Float volume, Float pitch, Player player) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리
     * @param volume 음량. {@code 1}을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. {@code 0.5 ~ 2} 사이의 값
     */
    public static void playAll(Sound sound, Float volume, Float pitch) {
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch));
    }

    /**
     * 모든 플레이어에게 소리를 재생한다.
     *
     * @param sound  소리
     * @param volume 음량. {@code 1}을 초과하면 소리가 들리는 범위만 늘어난다.
     * @param pitch  음정. {@code 0.5 ~ 2} 사이의 값
     */
    public static void playAll(String sound, Float volume, Float pitch) {
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch));
    }
}
