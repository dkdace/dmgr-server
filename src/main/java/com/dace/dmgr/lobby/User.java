package com.dace.dmgr.lobby;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.HasTask;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.*;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

/**
 * 유저 정보를 관리하는 클래스.
 */
@Getter
public final class User implements HasTask {
    /** {@link User#nameTagHider}의 고유 이름 */
    public static final String NAME_TAG_HIDER_CUSTOM_NAME = "nametag";
    /** 플레이어 객체 */
    private final Player player;
    /** 유저 데이터 정보 객체 */
    private final UserData userData;
    /** 이름표 숨기기용 갑옷 거치대 객체 */
    private ArmorStand nameTagHider;
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
        hideNameTag();
    }

    /**
     * 유저를 제거한다.
     */
    public void remove() {
        reset();

        EntityInfoRegistry.removeUser(player);
        TaskManager.clearTask(this);
        BossBarUtil.clearBossBar(player);
        sidebar.delete();
        nameTagHider.remove();

        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser != null)
            gameUser.getGame().removePlayer(player);
    }

    /**
     * 플레이어의 이름표를 숨긴다.
     */
    private void hideNameTag() {
        if (nameTagHider == null) {
            nameTagHider = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
            nameTagHider.setCustomName(NAME_TAG_HIDER_CUSTOM_NAME);
            nameTagHider.setSilent(true);
            nameTagHider.setInvulnerable(true);
            nameTagHider.setGravity(false);
            nameTagHider.setAI(false);
            nameTagHider.setMarker(true);
            nameTagHider.setVisible(false);
        }

        if (!player.getPassengers().contains(nameTagHider))
            player.addPassenger(nameTagHider);
    }

    @Override
    public String getTaskIdentifier() {
        return "User@" + player.getName();
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    public void playLevelUpEffect() {
        TaskManager.addTask(this, new TaskWait(100) {
            @Override
            protected void onEnd() {
                SoundUtil.play("random.good", 10F, 1F, player);
                MessageUtil.sendTitle(player, StringFormUtil.getLevelPrefix(userData.getLevel()) + " §e§l달성!", "", 8,
                        40, 30, 40);
            }
        });
    }

    /**
     * 티어 승급 시 효과를 재생한다.
     */
    public void playTierUpEffect() {
        TaskManager.addTask(this, new TaskWait(80) {
            @Override
            protected void onEnd() {
                SoundUtil.play(Sound.UI_TOAST_CHALLENGE_COMPLETE, 10F, 1.5F, player);
                MessageUtil.sendTitle(player, "§b§l등급 상승", userData.getTier().getPrefix(), 8, 40, 30, 40);
            }
        });
    }

    /**
     * 티어 강등 시 효과를 재생한다.
     */
    public void playTierDownEffect() {
        TaskManager.addTask(this, new TaskWait(80) {
            @Override
            protected void onEnd() {
                SoundUtil.play(Sound.ENTITY_BLAZE_DEATH, 10F, 0F, player);
                MessageUtil.sendTitle(player, "§c§l등급 강등", userData.getTier().getPrefix(), 8, 40, 30, 40);
            }
        });
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정한다.
     */
    public void reset() {
        if (DMGR.getPlugin().isEnabled())
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
