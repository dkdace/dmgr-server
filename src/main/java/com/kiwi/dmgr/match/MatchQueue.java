package com.kiwi.dmgr.match;

import org.bukkit.entity.Player;

public class MatchQueue {
    public static final Player[] matchQueue = new Player[100];

    public static int getMatchQueueCount(Player queue[]) {
        int i = 0;
        for (i=0; i<queue.length; i++) {
            if (queue[i] == null) {
                break;
            }
            i += 1;
        }

        return i + 1;
    }
}
