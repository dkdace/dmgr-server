package com.dace.dmgr.lobby;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.BossBarUtil;
import com.dace.dmgr.util.SkinUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

/**
 * 유저 정보를 관리하는 클래스.
 */
@Getter
public final class User implements HasTask {
    /** 플레이어 객체 */
    private final Player player;
    /** 유저 데이터 정보 객체 */
    private final UserData userData;
    /** 플레이어 사이드바 */
    @Setter
    private BPlayerBoard sidebar;
    /** 리소스팩 적용 여부 */
    @Setter
    private boolean resourcePack = false;
    /** 리소스팩 적용 상태 */
    @Setter
    private PlayerResourcePackStatusEvent.Status resourcePackStatus = null;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    public User(Player player) {
        this.player = player;
        this.userData = new UserData(player.getUniqueId());
        this.sidebar = new BPlayerBoard(player, "lobby");
    }

    /**
     * 유저를 초기화한다.
     */
    public void init() {
        EntityInfoRegistry.addUser(player, this);
        Lobby.lobbyTick(this);
    }

    /**
     * 유저를 제거한다.
     */
    public void remove() {
        EntityInfoRegistry.removeUser(player);
        TaskManager.clearTask(this);

        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser != null)
            gameUser.getGame().removePlayer(player);
    }

    @Override
    public String getTaskIdentifier() {
        return "User@" + player.getName();
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    private void playLevelUpEffect() {
    }

    /**
     * 티어 승급 시 효과를 재생한다.
     */
    private void playTierUpEffect() {
    }

    /**
     * 티어 강등 시 효과를 재생한다.
     */
    private void playTierDownEffect() {
    }

    /**
     * 플레이어의 채팅창을 청소한다.
     */
    public void clearChat() {
        for (int i = 0; i < 100; i++) {
            player.sendMessage("§f");
        }
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정한다.
     */
    public void reset() {
        SkinUtil.resetSkin(player);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));

        sidebar.delete();
        sidebar = new BPlayerBoard(player, "lobby");

        BossBarUtil.clearBossBar(player);

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser != null) {
            combatUser.reset();
            combatUser.remove();
        }
    }
}
