package com.dace.dmgr.util;

import org.bukkit.ChatColor;

import java.util.Arrays;

public class StringUtil {
    private static final char DEFAULT_SYMBOL = '■';

    public static String getBar(double current, double max, ChatColor color, int length, char symbol) {
        if (current < 0) current = 0;

        char[] filler = new char[length];
        Arrays.fill(filler, symbol);
        StringBuilder bar = new StringBuilder(new String(filler));

        double percent = current / max;

        if (current < max) {
            int index = ((int) Math.floor(percent * length));
            if (index < 0) index = 0;

            bar.insert(index, "§0");
        }
        bar.insert(0, color.toString());

        return bar.toString();
    }

    public static String getBar(double current, double max, ChatColor color, int length) {
        return getBar(current, max, color, length, DEFAULT_SYMBOL);
    }

    public static String getBar(double current, double max, ChatColor color) {
        return getBar(current, max, color, 10);
    }
}
