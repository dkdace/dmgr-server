package com.kiwi.dmgr.game.mode;

public interface IGameMode {

    /**
     * 해당 게임 타입으로서 시작 가능 여부를 리턴한다.
     *
     * @param playerCount 플레이어 수
     * @return 시작 가능 여부
     */
    boolean isStartAble(int playerCount);
}
