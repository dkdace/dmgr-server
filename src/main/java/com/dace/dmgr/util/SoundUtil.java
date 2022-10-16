package com.dace.dmgr.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundUtil {
    public static void play(Sound sound, Location location, Float volume, Float pitch) {
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void play(String sound, Location location, Float volume, Float pitch) {
        location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void play(Sound sound, Float volume, Float pitch, Player player) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void play(String sound, Float volume, Float pitch, Player player) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void playAll(Sound sound, Float volume, Float pitch) {
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch));
    }

    public static void playAll(String sound, Float volume, Float pitch) {
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch));
    }
}
