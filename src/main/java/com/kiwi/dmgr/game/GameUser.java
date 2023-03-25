package com.kiwi.dmgr.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

@Getter
@Setter
public class GameUser {

    /** 플레이어 */
    private Player player;
    /** 점수 */
    private int score;
    /** 킬 */
    private int kill;
    /** 데스 */
    private int death;
    /** 어시스트 */
    private int assist;
    /** 준 데미지량 */
    private long outgoingDamage;
    /** 받은 데미지량 */
    private long incomingDamage;
    /** 받은 데미지의 플레이어 맵 (킬/어시스트 판별) */
    private final HashMap<GameUser, Long> incomingDamagePlayerMap;
    /** 힐량 */
    private long heal;
    /** 생존 시간 */
    private long surviveTime;

    /**
     * 게임 유저 인스턴스를 생성하고 {@link GameMapList#gameUserMap}에 추가한다.
     *
     * <p>플레이어가 서버에 접속할 때 호출해야 하며, 퇴장 시 {@link GameMapList#gameUserMap}에서
     * 제거해야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public GameUser(Player player) {
        this.score = 0;
        this.kill = 0;
        this.death = 0;
        this.assist = 0;
        this.outgoingDamage = 0;
        this.incomingDamage = 0;
        this.incomingDamagePlayerMap = new HashMap<>();
        this.heal = 0;
        this.surviveTime = 0;

        gameUserMap.put(player, this);
    }
}
