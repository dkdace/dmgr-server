package com.dace.dmgr.util;

import org.bukkit.ChatColor;

import java.util.Arrays;

public class StringUtil {
    public static String getBar(double current, double max, ChatColor color, int length) {
        if (current < 0) current = 0;

        char[] filler = new char[length];
        Arrays.fill(filler, '■');
        StringBuilder bar = new StringBuilder(new String(filler));

        double percent = current / max;
        int index = ((int) Math.floor(percent * length)) - 1;
        if (index < 0) index = 0;

        bar.insert(index, "§0");
        bar.insert(0, color.toString());

        return bar.toString();
    }

    public static String getBar(double current, double max, ChatColor color) {
        return getBar(current, max, color, 10);
    }
}
