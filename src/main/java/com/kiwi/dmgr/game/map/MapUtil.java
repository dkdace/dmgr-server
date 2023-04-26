package com.kiwi.dmgr.game.map;

import com.kiwi.dmgr.game.GameMapList;
import com.kiwi.dmgr.game.map.teamdeathmatch.TestMap;

/**
 * 게임 맵을 관리하는 클래스
 */
public class MapUtil {

    /**
     * 맵을 전부 로드시키고 맵 리스트에 저장된 맵을 추가한다.
     *
     * <p> 서버가 실행되면 이 함수가 작동되어야 한다. </p>
     */
    public static void mapLoad() {
        GameMapList.addMap(new TestMap());
    }
}
