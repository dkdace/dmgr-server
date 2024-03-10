package com.dace.dmgr.user;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.listener.OnPlayerResourcePackStatus;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.UUID;

/**
 * 유저 정보 및 상태를 관리하는 클래스.
 */
public final class User implements Disposable {
    /** {@link User#nameTagHider}의 고유 이름 */
    public static final String NAME_TAG_HIDER_CUSTOM_NAME = "nametag";
    /** 생성된 보스바 UUID 목록 (보스바 ID : UUID) */
    private final HashMap<String, UUID> bossBarMap = new HashMap<>();
    /** 플레이어 객체 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 데이터 정보 객체 */
    private final UserData userData;
    /** 이름표 숨기기용 갑옷 거치대 객체 */
    private ArmorStand nameTagHider;
    /** 플레이어 사이드바 */
    @Getter
    private BPlayerBoard sidebar;
    /** 플레이어 탭리스트 */
    @Getter
    private TableTabList tabList;
    /** 현재 핑 (ms) */
    @Getter
    @Setter
    private int ping = 0;
    /** 리소스팩 적용 여부 */
    private boolean resourcePack = false;
    /** 리소스팩 적용 상태 */
    @Setter
    private PlayerResourcePackStatusEvent.Status resourcePackStatus = null;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    private User(@NonNull Player player) {
        this.player = player;
        this.userData = UserData.fromPlayer(player);

        disableCollision();
        TaskUtil.addTask(this, new DelayTask(this::updateNameTagHider, 1));
        TaskUtil.addTask(this, new DelayTask(this::sendResourcePack, 10));
        SkinUtil.resetSkin(player).run();
        if (userData.isInitialized())
            onDataInit();

        UserRegistry.getInstance().add(player, this);
    }

    /**
     * 지정한 플레이어의 유저 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 인스턴스
     */
    @NonNull
    public static User fromPlayer(@NonNull Player player) {
        User user = UserRegistry.getInstance().get(player);
        if (user == null)
            user = new User(player);

        return user;
    }

    /**
     * 유저 데이터 정보 객체({@link UserData}) 초기화 완료 시 실행할 작업.
     */
    void onDataInit() {
        sidebar = new BPlayerBoard(player, "lobby");
        tabList = (TableTabList) DMGR.getTabbed().getTabList(player);
        if (tabList == null)
            tabList = DMGR.getTabbed().newTableTabList(player);
        HologramUtil.addHologram(player.getName(), player.getLocation(), userData.getDisplayName());
        HologramUtil.bindHologram(player.getName(), player, 0, 2.25, 0);
        HologramUtil.setHologramVisibility(player.getName(), false, player);

        TaskUtil.addTask(User.this, new IntervalTask(i -> User.this.onSecond(), 20));

        if (!userData.getConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");
    }

    /**
     * 유저를 제거하고 등록 해제한다.
     *
     * <p>플레이어 퇴장 시 호출해야 한다.</p>
     */
    public void dispose() {
        checkAccess();

        reset();
        TaskUtil.clearTask(this);
        sidebar.delete();
        tabList.disable();
        nameTagHider.remove();
        HologramUtil.removeHologram(player.getName());

        GameUser gameUser = GameUser.fromUser(User.this);
        if (gameUser != null)
            gameUser.dispose();
        UserRegistry.getInstance().remove(player);

        if (DMGR.getPlugin().isEnabled())
            userData.save().run();
        else {
            userData.saveSync();
        }
    }

    @Override
    public boolean isDisposed() {
        return !player.isOnline();
    }

    /**
     * 플레이어끼리 밀치는 것을 비활성화한다.
     */
    private void disableCollision() {
        Scoreboard scoreBoard = player.getScoreboard();
        Team team = scoreBoard.getTeam("Default");
        if (team == null)
            team = scoreBoard.registerNewTeam("Default");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.addEntry(player.getName());
    }

    /**
     * 이름표 숨기기 객체를 업데이트한다.
     */
    private void updateNameTagHider() {
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

    /**
     * 플레이어에게 리소스팩을 전송하고, 적용하지 않을 시 강제 퇴장 시킨다.
     */
    private void sendResourcePack() {
        if (!resourcePack) {
            resourcePack = true;
            player.setResourcePack(GeneralConfig.getConfig().getResourcePackUrl());

            TaskUtil.addTask(this, new DelayTask(() -> {
                if (resourcePackStatus == null || resourcePackStatus == PlayerResourcePackStatusEvent.Status.DECLINED)
                    player.kickPlayer(GeneralConfig.getConfig().getMessagePrefix() + OnPlayerResourcePackStatus.MESSAGE_KICK_DENY);
            }, 160));
        }
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private boolean onSecond() {
        if (userData.getConfig().isNightVision())
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0, false, false));
        else
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        if (CombatUser.fromUser(this) == null)
            updateSidebar();
        if (GameUser.fromUser(this) == null || GameUser.fromUser(this).getGame().getPhase() == Game.Phase.WAITING)
            updateTablist();

        return true;
    }

    /**
     * 로비 사이드바를 업데이트한다.
     */
    private void updateSidebar() {
        int reqXp = userData.getNextLevelXp();
        int rank = userData.isRanked() ? userData.getRankRate() : 0;
        int reqRank = userData.getTier().getMaxScore();
        int curRank = userData.getTier().getMinScore();

        switch (userData.getTier()) {
            case STONE:
                curRank = userData.getTier().getMaxScore();
                break;
            case DIAMOND:
            case NETHERITE:
                reqRank = userData.getTier().getMinScore();
                break;
            case NONE:
                reqRank = 1;
                curRank = 0;
                break;
        }

        sidebar.clear();
        sidebar.setName("§b§n" + player.getName());
        sidebar.setAll(
                "§f",
                "§e보유 중인 돈",
                "§6" + String.format("%,d", userData.getMoney()),
                "§f§f",
                "§f레벨 : " + userData.getLevelPrefix(),
                StringFormUtil.getProgressBar(userData.getXp(), reqXp, ChatColor.DARK_GREEN) + " §2[" + userData.getXp() + "/" + reqXp + "]",
                "§f§f§f",
                "§f랭크 : " + userData.getTier().getPrefix(),
                StringFormUtil.getProgressBar(rank - curRank, reqRank - curRank, ChatColor.DARK_AQUA) + " §3[" + rank + "/" + reqRank + "]"
        );
    }

    /**
     * 로비 탭리스트를 업데이트한다.
     */
    private void updateTablist() {
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long memory = totalMemory - freeMemory;
        double memoryPercent = (double) memory / totalMemory;
        double tps = DMGR.getTps();

        ChatColor memoryColor = ChatColor.RED;
        if (memoryPercent < 0.5)
            memoryColor = ChatColor.GREEN;
        else if (memoryPercent < 0.75)
            memoryColor = ChatColor.YELLOW;
        else if (memoryPercent < 0.9)
            memoryColor = ChatColor.GOLD;

        ChatColor pingColor = ChatColor.RED;
        if (ping < 60)
            pingColor = ChatColor.GREEN;
        else if (ping < 120)
            pingColor = ChatColor.YELLOW;
        else if (ping < 180)
            pingColor = ChatColor.GOLD;

        ChatColor tpsColor = ChatColor.GREEN;
        if (tps < 19)
            tpsColor = ChatColor.RED;
        else if (tps < 19.4)
            tpsColor = ChatColor.GOLD;
        else if (tps < 19.7)
            tpsColor = ChatColor.YELLOW;

        tabList.setHeader("\n" + GeneralConfig.getConfig().getMessagePrefix() + "§e스킬 PVP 미니게임 서버 §f:: §d§nDMGR.mcsv.kr\n");
        tabList.setFooter("\n§7현재 서버는 테스트 단계이며, 시스템 상 문제점이나 버그가 발생할 수 있습니다.\n");

        tabList.set(0, 0, new TextTabItem("§f§l§n 서버 상태 ", 0, Skins.getPlayer("computer_")));
        tabList.set(0, 2, new TextTabItem(
                MessageFormat.format("§f PING §7:: {0}{1} ms", pingColor, ping),
                0, Skins.getPlayer("FranciRoma")));
        tabList.set(0, 3, new TextTabItem(
                MessageFormat.format("§f 메모리 §7:: {0}{1} §f/ {2} (MB)", memoryColor, memory, totalMemory),
                0, Skins.getPlayer("AddelBurgh")));
        tabList.set(0, 4, new TextTabItem(
                MessageFormat.format("§f TPS §7:: {0}{1} tick/s", tpsColor, tps),
                0, Skins.getPlayer("CommandBlock")));
        tabList.set(0, 5, new TextTabItem(
                MessageFormat.format("§f 접속자 수 §7:: §f{0}명", Bukkit.getOnlinePlayers().size()),
                0, Skins.getPlayer("MHF_Steve")));
        tabList.set(0, 7, new TextTabItem(
                MessageFormat.format("§f§n §e§n{0}§f§l§n님의 전적 ", player.getName()),
                0, Skins.getPlayer(player)));
        tabList.set(0, 9, new TextTabItem(
                MessageFormat.format("§e 승률 §7:: §b{0}승 §f/ §c{1}패 §f({2}%)", 0, 0, 0),
                0, Skins.getPlayer("goldblock")));
        tabList.set(0, 10, new TextTabItem("§e 탈주 §7:: §c0회 §f(0%)", 0, Skins.getPlayer("MHF_TNT2")));
        tabList.set(0, 11, new TextTabItem("§e 플레이 시간 §7:: §fnull", 0, Skins.getPlayer("Olaf_C")));

        Player[] lobbyPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player2 -> GameUser.fromUser(User.fromPlayer(player2)) == null && !player2.isOp())
                .toArray(Player[]::new);
        for (int i = 0; i < 19; i++) {
            if (i > lobbyPlayers.length - 1)
                tabList.remove(1, i + 1);
            else
                tabList.set(1, i + 1, new TextTabItem(UserData.fromPlayer(lobbyPlayers[i]).getDisplayName(), 0,
                        Skins.getPlayer(lobbyPlayers[i])));
        }

        tabList.set(1, 0, new TextTabItem(
                MessageFormat.format("§a§l§n 로비 인원 §f({0}명)", lobbyPlayers.length),
                0, Skins.getDot(ChatColor.GREEN)));

        Player[] gamePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player2 -> GameUser.fromUser(User.fromPlayer(player2)) != null && !player2.isOp())
                .toArray(Player[]::new);
        for (int i = 0; i < 19; i++) {
            if (i > gamePlayers.length - 1)
                tabList.remove(2, i + 1);
            else
                tabList.set(2, i + 1, new TextTabItem(UserData.fromPlayer(gamePlayers[i]).getDisplayName(), 0,
                        Skins.getPlayer(gamePlayers[i])));
        }

        tabList.set(2, 0, new TextTabItem(
                MessageFormat.format("§c§l§n 게임 인원 §f({0}명)", gamePlayers.length),
                0, Skins.getDot(ChatColor.RED)));

        Player[] adminPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .toArray(Player[]::new);
        for (int i = 0; i < 19; i++) {
            if (i > adminPlayers.length - 1)
                tabList.remove(3, i + 1);
            else
                tabList.set(3, i + 1, new TextTabItem(UserData.fromPlayer(adminPlayers[i]).getDisplayName(), 0,
                        Skins.getPlayer(adminPlayers[i])));
        }

        tabList.set(3, 0, new TextTabItem(
                MessageFormat.format("§b§l§n 관리자 §f({0}명)", adminPlayers.length),
                0, Skins.getDot(ChatColor.AQUA)));

        tabList.batchUpdate();
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    public void playLevelUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play("random.good", player, 10, 1);
            sendTitle(userData.getLevelPrefix() + " §e§l달성!", "", 8,
                    40, 30, 40);
        }, 100));
    }

    /**
     * 티어 승급 시 효과를 재생한다.
     */
    public void playTierUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play(Sound.UI_TOAST_CHALLENGE_COMPLETE, player, 10, 1.5);
            sendTitle("§b§l등급 상승", userData.getTier().getPrefix(), 8, 40, 30, 40);
        }, 80));
    }

    /**
     * 티어 강등 시 효과를 재생한다.
     */
    public void playTierDownEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play(Sound.ENTITY_BLAZE_DEATH, player, 10, 0.5);
            sendTitle("§c§l등급 강등", userData.getTier().getPrefix(), 8, 40, 30, 40);
        }, 80));
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정하고 스폰으로 이동시킨다.
     */
    public void reset() {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));
        HologramUtil.setHologramVisibility(player.getName(), true, Bukkit.getOnlinePlayers().toArray(new Player[0]));
        HologramUtil.setHologramVisibility(player.getName(), false, player);
        Bukkit.getOnlinePlayers().forEach(player2 -> GlowUtil.removeGlowing(player, player2));

        sidebar.clear();
        for (int i = 0; i < 80; i++)
            tabList.remove(i);
        clearBossBar();
        teleport(LocationUtil.getLobbyLocation());
        if (DMGR.getPlugin().isEnabled())
            SkinUtil.resetSkin(player).run();

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.dispose();
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
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * <p>'§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * user.sendMessageInfo("§aHello, §rWorld!");
     * }</pre>
     *
     * @param message 메시지
     */
    public void sendMessageInfo(@NonNull String message) {
        message = message.replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + message);
    }

    /**
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * <p>'§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <연두색>Hello, <흰색>World!
     * user.sendMessageInfo("§aHello, §r{0}!", "World");
     * }</pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public void sendMessageInfo(@NonNull String message, @NonNull Object... arguments) {
        message = MessageFormat.format(message, arguments).replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + message);
    }

    /**
     * 플레이어의 채팅창에 경고 메시지를 전송한다.
     *
     * <p>'§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * user.sendMessageWarn("§aHello, §rWorld!");
     * }</pre>
     *
     * @param message 메시지
     */
    public void sendMessageWarn(@NonNull String message) {
        message = message.replace("§r", "§c")
                .replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + ChatColor.RED + message);
    }

    /**
     * 플레이어의 채팅창에 경고 메시지를 전송한다.
     *
     * <p>'§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <연두색>Hello, <빨간색>World!
     * user.sendMessageWarn("§aHello, §r{0}!", "World");
     * }</pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public void sendMessageWarn(@NonNull String message, @NonNull Object... arguments) {
        message = MessageFormat.format(message, arguments)
                .replace("§r", "§c")
                .replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + ChatColor.RED + message);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message       메시지
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 액션바 출력을 무시한다.
     */
    public void sendActionBar(@NonNull String message, long overrideTicks) {
        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, Cooldown.ACTION_BAR, overrideTicks);
        else if (CooldownUtil.getCooldown(this, Cooldown.ACTION_BAR) > 0)
            return;

        TextComponent actionBar = new TextComponent(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message 메시지
     */
    public void sendActionBar(@NonNull String message) {
        sendActionBar(message, 0);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title         제목
     * @param subtitle      부제목
     * @param fadeIn        나타나는 시간 (tick)
     * @param stay          유지 시간 (tick)
     * @param fadeOut       사라지는 시간 (tick)
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 타이틀 출력을 무시한다.
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut, long overrideTicks) {
        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, Cooldown.TITLE, overrideTicks);
        else if (CooldownUtil.getCooldown(this, Cooldown.TITLE) > 0)
            return;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title    제목
     * @param subtitle 부제목
     * @param fadeIn   나타나는 시간 (tick)
     * @param stay     유지 시간 (tick)
     * @param fadeOut  사라지는 시간 (tick)
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(title, subtitle, fadeIn, stay, fadeOut, 0);
    }

    /**
     * 플레이어에게 보스바를 표시한다.
     *
     * <p>이미 해당 ID의 보스바가 존재할 경우 덮어쓴다.</p>
     *
     * @param id       보스바 ID
     * @param message  내용
     * @param color    막대 색
     * @param style    막대 스타일
     * @param progress 진행률. 0~1 사이의 값
     * @throws IllegalArgumentException {@code progress}가 0~1 사이가 아니면 발생
     */
    public void addBossBar(@NonNull String id, @NonNull String message, @NonNull BarColor color, @NonNull WrapperPlayServerBoss.BarStyle style,
                           double progress) {
        if (progress < 0 || progress > 1)
            throw new IllegalArgumentException("'progress'가 0에서 1 사이여야 함");

        UUID uuid = bossBarMap.getOrDefault(id, UUID.randomUUID());

        if (bossBarMap.get(id) == null) {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setUniqueId(uuid);
            packet.setAction(WrapperPlayServerBoss.Action.ADD);
            packet.setTitle(WrappedChatComponent.fromText(message));
            packet.setColor(color);
            packet.setStyle(style);
            packet.setHealth((float) progress);

            packet.sendPacket(player);

            bossBarMap.put(id, uuid);
        } else {
            WrapperPlayServerBoss packet1 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet2 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet3 = new WrapperPlayServerBoss();
            packet1.setUniqueId(uuid);
            packet2.setUniqueId(uuid);
            packet3.setUniqueId(uuid);

            packet1.setAction(WrapperPlayServerBoss.Action.UPDATE_NAME);
            packet1.setTitle(WrappedChatComponent.fromText(message));
            packet2.setAction(WrapperPlayServerBoss.Action.UPDATE_STYLE);
            packet2.setColor(color);
            packet2.setStyle(style);
            packet3.setAction(WrapperPlayServerBoss.Action.UPDATE_PCT);
            packet3.setHealth((float) progress);

            packet1.sendPacket(player);
            packet2.sendPacket(player);
            packet3.sendPacket(player);
        }
    }

    /**
     * 플레이어의 보스바를 제거한다.
     *
     * @param id 보스바 ID
     */
    public void removeBossBar(@NonNull String id) {
        UUID uuid = bossBarMap.get(id);
        if (uuid == null)
            return;

        WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

        packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
        packet.setUniqueId(uuid);

        packet.sendPacket(player);

        bossBarMap.remove(id, uuid);
    }

    /**
     * 플레이어의 모든 보스바를 제거한다.
     */
    public void clearBossBar() {
        bossBarMap.forEach((id, uuid) -> {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
            packet.setUniqueId(uuid);

            packet.sendPacket(player);
        });

        bossBarMap.clear();
    }

    /**
     * 플레이어를 지정한 위치로 순간이동 시킨다.
     *
     * <p>이름표 숨기기 기능으로 인해 기본 텔레포트가 되지 않기 때문에 사용한다.</p>
     *
     * @param location 이동할 위치
     */
    public void teleport(@NonNull Location location) {
        player.removePassenger(nameTagHider);
        player.teleport(location);
        nameTagHider.teleport(location);
        player.addPassenger(nameTagHider);
    }
}
