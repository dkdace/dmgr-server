package com.kiwi.dmgr.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

@Getter
@Setter
public class GameUser {

    /** 플레이어 */
    private Player player;
    /** 플레이 중인 게임 (해당 게임유저가 적용중인 게임) */
    private Game game;
    /** 점수 */
    private int score;
    /** 킬 */
    private int kill;
    /** 데스 */
    private int death;
    /** 어시스트 */
    private int assist;
    /** 준 피해량 */
    private long outgoingDamage;
    /** 받은 피해량 */
    private long incomingDamage;
    /** 힐량 */
    private long heal;
    /** 막은 피해량 */
    private long blockDamage;

    /**
     * 게임 유저 인스턴스를 생성하고 {@link GameMapList#gameUserMap}에 추가한다.
     *
     * <p>플레이어가 서버에 접속할 때 호출해야 하며, 퇴장 시 {@link GameMapList#gameUserMap}에서
     * 제거해야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public GameUser(Player player) {
        this.player = player;
        this.game = null;
        this.score = 0;
        this.kill = 0;
        this.death = 0;
        this.assist = 0;
        this.outgoingDamage = 0;
        this.incomingDamage = 0;
        this.heal = 0;
        this.blockDamage = 0;

        gameUserMap.put(player, this);
    }

    /**
     * 두 유저가 게임 유저 이벤트에 유효한지의(사용 가능한지의) 여부를 출력한다.
     *
     * <p> 게임 유저 데이터가 존재하며, 한 게임에 있으면 true를 출력한다. </p>
     *
     * @param user1 유저1
     * @param user2 유저2
     * @return 이벤트 유효 여부
     */
    public static boolean isGameUserEventVaild(GameUser user1, GameUser user2) {
        return user1.getGame() == user2.getGame();
    }
}
