package com.kiwi.dmgr.game;

import com.dace.dmgr.system.task.TaskTimer;

public class GameScheduler extends Game {

    /**
     * 게임 스케쥴러를 실행한다.
     *
     * 게임 인원이 시작 가능 인원이 되면 시작 카운트가 시작된다.
     *
     * 인원이 0명이면 방이 삭제된다.
     */
    public static void run(Game game) {
        new TaskTimer(20) {

            int playerCount = 0;
            int startTimer = 0;

            @Override
            public boolean run(int i) {

                playerCount = game.getPlayerList().size();

                if (playerCount == 0) {
                    game.delete();
                    return false;
                }

                // 테스트로 대기 플레이어에게 메세지로 전송함.
                if (game.getMode().isStartAble(playerCount)) {

                    if (startTimer == 0)
                        game.sendAlertMessage("게임이 60초 뒤에 시작합니다.");
                    else if (startTimer == 30)
                        game.sendAlertMessage("게임이 30초 뒤에 시작합니다.");
                    else if (startTimer == 50)
                        game.sendAlertMessage("게임이 10초 뒤에 시작합니다.");
                    else if (startTimer == 57)
                        game.sendAlertMessage("게임이 3초 뒤에 시작합니다.");
                    else if (startTimer == 58)
                        game.sendAlertMessage("게임이 2초 뒤에 시작합니다.");
                    else if (startTimer == 59)
                        game.sendAlertMessage("게임이 1초 뒤에 시작합니다.");
                    else if (startTimer == 60) {
                        game.getMode().run(game);
                        return false;
                    }

                    startTimer += 1;

                } else {
                    startTimer = 0;
                }

                return true;
            }
        };
    }
}
