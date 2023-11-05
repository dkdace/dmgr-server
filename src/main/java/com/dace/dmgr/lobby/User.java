package com.dace.dmgr.lobby;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.SkinUtil;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

/**
 * 유저 정보를 관리하는 클래스.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public final class User extends UserData implements HasTask {
    /** 플레이어 객체 */
    private final Player player;
    /** 로비 사이드바 */
    private final BPlayerBoard lobbySidebar;
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
        super(player.getUniqueId());
        this.player = player;
        this.lobbySidebar = new BPlayerBoard(player, "lobbySidebar");
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
    }

    @Override
    public String getTaskIdentifier() {
        return "User@" + player.getName();
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

        CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
        if (combatUser != null) {
            combatUser.reset();
            combatUser.remove();
        }
        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser != null)
            gameUser.remove();
    }
}
