package com.kiwi.dmgr.match.type;

import com.kiwi.dmgr.game.mode.GameMode;
import com.kiwi.dmgr.game.mode.TeamDeathMatch;
import com.kiwi.dmgr.match.Match;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 일반전
 */
public class Unranked extends Match {

    private final static ArrayList<GameMode> GAMEMODE = new ArrayList<GameMode>(
            Arrays.asList(new TeamDeathMatch()));

    public Unranked() {
        super(GAMEMODE, true, false, true, false);
    }
}