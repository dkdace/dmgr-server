package com.kiwi.dmgr.game;

import com.dace.dmgr.system.task.TaskTimer;

/**
 * 게임에 매 주기마다 기능을 추가해 게임을 원활하게 하는 게임 스케쥴러 클래스
 */
public class GameScheduler extends Game {

    /**
     * 게임 스케쥴러를 실행한다.
     *
     * <p> 게임 인원이 시작 가능 인원이 되면 시작 카운트가 시작된다.
     *     인원이 0명이면 방이 삭제된다. </p>
     */
    public static void run(Game game) {
        new TaskTimer(20) {

            int playerCount = 0;
            int startTimer = 60;

            @Override
            public boolean run(int i) {

                playerCount = game.getPlayerList().size();

                if (playerCount == 0) {
                    game.delete();
                    return false;
                }

                // 테스트로 대기 플레이어에게 메세지로 전송
                if (game.getMode().isStartAble(playerCount)) {

                    if (startTimer == 60)
                        game.sendAlertMessage("게임이 60초 뒤에 시작합니다.");
                    else if (startTimer == 30)
                        game.sendAlertMessage("게임이 30초 뒤에 시작합니다.");
                    else if (startTimer == 10)
                        game.sendAlertMessage("게임이 10초 뒤에 시작합니다.");
                    else if (startTimer == 3)
                        game.sendAlertMessage("게임이 3초 뒤에 시작합니다.");
                    else if (startTimer == 2)
                        game.sendAlertMessage("게임이 2초 뒤에 시작합니다.");
                    else if (startTimer == 1)
                        game.sendAlertMessage("게임이 1초 뒤에 시작합니다.");
                    else if (startTimer == 0) {
                        game.start();
                        // 해당 게임 모드의 스케쥴러를 실행
                        game.getMode().run(game);

                        return false;
                    }

                    startTimer -= 1;

                } else {
                    if (startTimer != 60)
                        game.sendAlertMessage("게임 시작 인원이 충족하지 못하여 대기중입니다.");
                    startTimer = 60;
                }

                return true;
            }
        };
    }
}
