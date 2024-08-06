package com.dace.dmgr.user;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.UUID;

/**
 * 유저 정보 및 상태를 관리하는 클래스.
 *
 * @see UserData
 */
public final class User implements Disposable {
    /** 타자기 효과 타이틀 쿨타임 ID */
    public static final String TYPEWRITER_TITLE_COOLDOWN_ID = "TypewriterTitle";
    /** 타이틀 쿨타임 ID */
    private static final String TITLE_COOLDOWN_ID = "Title";
    /** 액션바 쿨타임 ID */
    private static final String ACTION_BAR_COOLDOWN_ID = "ActionBar";
    /** 리소스팩 적용 시간 제한 (tick) */
    private static final long RESOURCE_PACK_TIMEOUT = 8 * 20L;
    /** 오류 발생으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_ERR = "§c유저 데이터를 불러오는 중 오류가 발생했습니다." +
            "\n" +
            "\n§f잠시 후 다시 시도하거나, 관리자에게 문의하십시오." +
            "\n" +
            "\n§7오류 문의 : " + GeneralConfig.getConfig().getAdminContact();
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_DENY = "§c리소스팩 적용을 활성화 하십시오." +
            "\n" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : " + GeneralConfig.getConfig().getAdminContact();

    /** 플레이어 객체 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 데이터 정보 객체 */
    @NonNull
    @Getter
    private final UserData userData;
    /** 생성된 보스바 UUID 목록 (보스바 ID : UUID) */
    private final HashMap<String, UUID> bossBarMap = new HashMap<>();
    /** 이름표 숨기기용 갑옷 거치대 객체 */
    @Nullable
    private ArmorStand nameTagHider;
    /** 플레이어 사이드바 */
    @Nullable
    private BPlayerBoard sidebar;
    /** 플레이어 탭리스트 */
    @Nullable
    private TableTabList tabList;
    /** 현재 핑 (ms) */
    @Getter
    @Setter
    private int ping = 0;
    /** 리소스팩 적용 수락 여부 */
    @Setter
    private boolean isResourcePackAccepted = false;
    /** 현재 귓속말 대상 */
    @Nullable
    @Getter
    @Setter
    private User messageTarget;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    private User(@NonNull Player player) {
        this.player = player;
        this.userData = UserData.fromPlayer(player);

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
     * 유저 초기화 작업을 수행한다.
     */
    public void init() {
        validate();

        if (userData.isInitialized())
            onInit();
        else
            TaskUtil.addTask(this, userData.init()
                    .onFinish(this::onInit)
                    .onError(ex -> TaskUtil.addTask(User.this, new DelayTask(() ->
                            player.kickPlayer(MESSAGE_KICK_ERR), 60))));
    }

    /**
     * 유저 초기화 완료 시 실행할 작업.
     */
    private void onInit() {
        disableCollision();
        TaskUtil.addTask(this, new DelayTask(this::updateNameTagHider, 1));
        TaskUtil.addTask(this, new DelayTask(this::sendResourcePack, 10));
        TaskUtil.addTask(this, SkinUtil.resetSkin(player));

        sidebar = new BPlayerBoard(player, "lobby");
        clearSidebar();
        tabList = (TableTabList) DMGR.getTabbed().getTabList(player);
        if (tabList == null)
            tabList = DMGR.getTabbed().newTableTabList(player);
        HologramUtil.addHologram(player.getName(), player, 0, 2.25, 0, userData.getDisplayName());
        HologramUtil.setHologramVisibility(player.getName(), false, player);

        if (!userData.getConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");

        TaskUtil.addTask(this, new IntervalTask(i -> {
            onSecond();
            return true;
        }, 20));
    }

    /**
     * 유저를 제거하고 등록 해제한다.
     *
     * <p>플레이어 퇴장 시 호출해야 한다.</p>
     */
    public void dispose() {
        validate();

        reset();
        TaskUtil.clearTask(this);
        HologramUtil.removeHologram(player.getName());

        GameUser gameUser = GameUser.fromUser(this);
        if (gameUser != null)
            gameUser.dispose();
        UserRegistry.getInstance().remove(player);

        if (userData.isInitialized()) {
            if (sidebar != null) {
                sidebar.delete();
                sidebar = null;
            }
            if (tabList != null) {
                tabList.disable();
                tabList = null;
            }
            if (nameTagHider != null) {
                nameTagHider.remove();
                nameTagHider = null;
            }

            if (DMGR.getPlugin().isEnabled())
                userData.save();
            else
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
        if (nameTagHider != null)
            return;

        nameTagHider = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        nameTagHider.setCustomName(DMGR.TEMPORARY_ENTITY_CUSTOM_NAME);
        nameTagHider.setSilent(true);
        nameTagHider.setInvulnerable(true);
        nameTagHider.setGravity(false);
        nameTagHider.setAI(false);
        nameTagHider.setMarker(true);
        nameTagHider.setVisible(false);

        if (!player.getPassengers().contains(nameTagHider))
            player.addPassenger(nameTagHider);
    }

    /**
     * 플레이어에게 리소스팩을 전송하고, 적용하지 않을 시 강제 퇴장 시킨다.
     */
    private void sendResourcePack() {
        player.setResourcePack(GeneralConfig.getConfig().getResourcePackUrl());

        TaskUtil.addTask(this, new DelayTask(() -> {
            if (!isResourcePackAccepted)
                player.kickPlayer(GeneralConfig.getConfig().getMessagePrefix() + MESSAGE_KICK_DENY);
        }, RESOURCE_PACK_TIMEOUT));
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        if (userData.getConfig().isNightVision())
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        else
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        if (CombatUser.fromUser(this) == null)
            updateSidebar();

        GameUser gameUser = GameUser.fromUser(this);
        if (gameUser == null || gameUser.getGame().getPhase() == Game.Phase.WAITING)
            updateTablist();
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
            default:
                break;
        }

        setSidebarName("§b§n" + player.getName());
        editSidebar(
                "§f",
                "§e보유 중인 돈",
                "§6" + String.format("%,d", userData.getMoney()),
                "§f§f",
                "§f레벨 : " + userData.getLevelPrefix(),
                MessageFormat.format("{0} §2[{1}/{2}]",
                        StringFormUtil.getProgressBar(userData.getXp(), reqXp, ChatColor.DARK_GREEN), userData.getXp(), reqXp),
                "§f§f§f",
                "§f랭크 : " + userData.getTier().getPrefix(),
                MessageFormat.format("{0} §3[{1}/{2}]",
                        StringFormUtil.getProgressBar(rank - curRank, reqRank - curRank, ChatColor.DARK_AQUA), rank, reqRank)
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

        setTabListHeader("\n" + GeneralConfig.getConfig().getMessagePrefix() + "§e스킬 PVP 미니게임 서버 §f:: §d§nDMGR.mcsv.kr\n");
        setTabListFooter("\n§7현재 서버는 테스트 단계이며, 시스템 상 문제점이나 버그가 발생할 수 있습니다.\n");

        setTabListItem(0, 0, "§f§l§n 서버 상태 ", Skins.getPlayer("TimmyTimothy"));
        setTabListItem(0, 2, MessageFormat.format("§f PING §7:: {0}{1} ms", pingColor, ping),
                Skins.getPlayer("FranciRoma"));
        setTabListItem(0, 3, MessageFormat.format("§f 메모리 §7:: {0}{1} §f/ {2} (MB)", memoryColor, memory, totalMemory),
                Skins.getPlayer("AddelBurgh"));
        setTabListItem(0, 4, MessageFormat.format("§f TPS §7:: {0}{1} tick/s", tpsColor, tps),
                Skins.getPlayer("CommandBlock"));
        setTabListItem(0, 5, MessageFormat.format("§f 접속자 수 §7:: §f{0}명", Bukkit.getOnlinePlayers().size()),
                Skins.getPlayer("MHF_Steve"));
        setTabListItem(0, 7, MessageFormat.format("§f§n §e§n{0}§f§l§n님의 전적 ", player.getName()),
                Skins.getPlayer(player));
        setTabListItem(0, 9, MessageFormat.format("§e 승률 §7:: §b{0}승 §f/ §c{1}패 §f({2}%)", userData.getWinCount(), userData.getLoseCount(),
                        (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100),
                Skins.getPlayer("goldblock"));
        setTabListItem(0, 10, MessageFormat.format("§e 탈주 §7:: §c{0}회 §f({1}%)", userData.getQuitCount(),
                        (double) userData.getQuitCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100),
                Skins.getPlayer("MHF_TNT2"));
        setTabListItem(0, 11, MessageFormat.format("§e 플레이 시간 §7:: §f{0}",
                        DurationFormatUtils.formatDuration(userData.getPlayTime() * 1000L, "d일 H시간 m분")),
                Skins.getPlayer("Olaf_C"));

        Player[] lobbyPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(target -> GameUser.fromUser(User.fromPlayer(target)) == null && !target.isOp())
                .toArray(Player[]::new);
        Player[] gamePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(target -> GameUser.fromUser(User.fromPlayer(target)) != null && !target.isOp())
                .toArray(Player[]::new);
        Player[] adminPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .toArray(Player[]::new);

        for (int i = 0; i < 19; i++) {
            if (i > lobbyPlayers.length - 1)
                removeTabListItem(1, i + 1);
            else
                setTabListItem(1, i + 1, UserData.fromPlayer(lobbyPlayers[i]).getDisplayName(), Skins.getPlayer(lobbyPlayers[i]));
        }
        setTabListItem(1, 0, MessageFormat.format("§a§l§n 로비 인원 §f({0}명)", lobbyPlayers.length), Skins.getDot(ChatColor.GREEN));

        for (int i = 0; i < 19; i++) {
            if (i > gamePlayers.length - 1)
                removeTabListItem(2, i + 1);
            else
                setTabListItem(2, i + 1, UserData.fromPlayer(gamePlayers[i]).getDisplayName(), Skins.getPlayer(gamePlayers[i]));
        }
        setTabListItem(2, 0, MessageFormat.format("§c§l§n 게임 인원 §f({0}명)", gamePlayers.length), Skins.getDot(ChatColor.RED));

        for (int i = 0; i < 19; i++) {
            if (i > adminPlayers.length - 1)
                removeTabListItem(3, i + 1);
            else
                setTabListItem(3, i + 1, UserData.fromPlayer(adminPlayers[i]).getDisplayName(), Skins.getPlayer(adminPlayers[i]));
        }
        setTabListItem(3, 0, MessageFormat.format("§b§l§n 관리자 §f({0}명)", adminPlayers.length), Skins.getDot(ChatColor.AQUA));
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    public void playLevelUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.playNamedSound(NamedSound.GENERAL_SUCCESS, player);
            sendTitle(userData.getLevelPrefix() + " §e§l달성!", "", 8, 40, 30, 40);
        }, 100));
    }

    /**
     * 티어 승급 시 효과를 재생한다.
     */
    public void playTierUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.playNamedSound(NamedSound.GAME_WIN, player);
            sendTitle("§b§l등급 상승", userData.getTier().getPrefix(), 8, 40, 30, 40);
        }, 80));
    }

    /**
     * 티어 강등 시 효과를 재생한다.
     */
    public void playTierDownEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.playNamedSound(NamedSound.GAME_LOSE, player);
            sendTitle("§c§l등급 강등", userData.getTier().getPrefix(), 8, 40, 30, 40);
        }, 80));
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정하고 스폰으로 이동시킨다.
     */
    public void reset() {
        validate();

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
        Bukkit.getOnlinePlayers().forEach(target -> GlowUtil.removeGlowing(this.player, target));

        clearBossBar();
        teleport(LocationUtil.getLobbyLocation());
        if (DMGR.getPlugin().isEnabled())
            SkinUtil.resetSkin(player);

        if (userData.isInitialized()) {
            clearSidebar();
            clearTabListItems();
        }

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.dispose();
    }

    /**
     * 플레이어의 채팅창을 청소한다.
     */
    public void clearChat() {
        for (int i = 0; i < 100; i++)
            player.sendMessage("§f");
    }

    /**
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * <p>'§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     * // <연두색>Hello, <흰색>World!
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
    public void sendMessageInfo(@NonNull String message, @NonNull Object @NonNull ... arguments) {
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
     * // <연두색>Hello, <빨간색>World!
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
    public void sendMessageWarn(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        message = MessageFormat.format(message, arguments)
                .replace("§r", "§c")
                .replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + ChatColor.RED + message);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message       메시지
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 액션바 출력을 무시함
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendActionBar(@NonNull String message, long overrideTicks) {
        if (overrideTicks < 0)
            throw new IllegalArgumentException("'overrideTicks'가 0 이상이어야 함");

        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, ACTION_BAR_COOLDOWN_ID, overrideTicks);
        else if (CooldownUtil.getCooldown(this, ACTION_BAR_COOLDOWN_ID) > 0)
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
     * 플레이어에게 경고 액션바를 전송한다.
     *
     * @param message 경고 메시지
     */
    public void sendAlert(@NonNull String message) {
        SoundUtil.playNamedSound(NamedSound.GENERAL_ALERT, player);
        TaskUtil.addTask(this, new IntervalTask(i -> {
            ChatColor color = ChatColor.YELLOW;
            if (i == 1)
                color = ChatColor.GOLD;
            else if (i == 2)
                color = ChatColor.RED;

            sendActionBar(color + ChatColor.stripColor(message), 16);

            return true;
        }, 1, 3));
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title         제목
     * @param subtitle      부제목
     * @param fadeIn        나타나는 시간 (tick). 0 이상의 값
     * @param stay          유지 시간 (tick). 0 이상의 값
     * @param fadeOut       사라지는 시간 (tick). 0 이상의 값
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 타이틀 출력을 무시함
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut, long overrideTicks) {
        if (overrideTicks < 0 || fadeIn < 0 || stay < 0 || fadeOut < 0)
            throw new IllegalArgumentException("'fadeIn', 'stay', 'fadeOut' 및 'overrideTicks'가 0 이상이어야 함");

        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, TITLE_COOLDOWN_ID, overrideTicks);
        else if (CooldownUtil.getCooldown(this, TITLE_COOLDOWN_ID) > 0)
            return;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title    제목
     * @param subtitle 부제목
     * @param fadeIn   나타나는 시간 (tick). 0 이상의 값
     * @param stay     유지 시간 (tick). 0 이상의 값
     * @param fadeOut  사라지는 시간 (tick). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(title, subtitle, fadeIn, stay, fadeOut, 0);
    }

    /**
     * 플레이어에게 타자기 효과 타이틀을 전송한다.
     *
     * <p>순차적으로 한 글자씩 나타나는 효과이다.</p>
     *
     * @param prefix  접두사
     * @param message 메시지
     */
    public void sendTypewriterTitle(@NonNull String prefix, @NonNull String message) {
        int delay = 0;
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char nextChar = message.charAt(i);

            TaskUtil.addTask(this, new DelayTask(() -> {
                text.append(nextChar);
                CooldownUtil.setCooldown(this, TYPEWRITER_TITLE_COOLDOWN_ID, 50);

                sendTitle("", prefix + " §f" + text, 0, 40, 10);
                SoundUtil.playNamedSound(NamedSound.TYPEWRITER_TITLE, player);
            }, delay));

            if (nextChar == '.' || nextChar == ',')
                delay += 4;
            else if (nextChar == ' ')
                delay += 2;
            else
                delay += 1;
        }
    }

    /**
     * 플레이어의 사이드바 이름을 지정한다.
     *
     * @param name 사이드바 이름
     */
    public void setSidebarName(@NonNull String name) {
        Validate.notNull(sidebar).setName(name);
    }

    /**
     * 플레이어의 사이드바 내용을 업데이트한다.
     *
     * @param line    줄 번호. 0~14 사이의 값
     * @param content 내용
     * @throws IndexOutOfBoundsException {@code line}이 유효 범위를 초과하면 발생
     */
    public void editSidebar(int line, @NonNull String content) {
        Validate.notNull(sidebar);
        if (line < 0 || line > 14)
            throw new IndexOutOfBoundsException("'line'이 0에서 14 사이여야 함");

        ChatColor[] chatColors = ChatColor.values();
        sidebar.set(content.isEmpty() ? String.valueOf(chatColors[line]) : content, 14 - line);
    }

    /**
     * 플레이어의 사이드바 내용을 업데이트한다.
     *
     * @param contents 내용 목록
     * @throws IndexOutOfBoundsException {@code contents}의 길이가 15를 초과하면 발생
     */
    public void editSidebar(@NonNull String @NonNull ... contents) {
        Validate.notNull(sidebar);
        if (contents.length > 15)
            throw new IndexOutOfBoundsException("'contents'의 길이가 16 미만이어야 함");

        sidebar.setAll(contents);
    }

    /**
     * 플레이어의 사이드바 내용을 초기화한다.
     */
    public void clearSidebar() {
        Validate.notNull(sidebar).clear();
    }

    /**
     * 플레이어의 탭리스트 헤더(상단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setTabListHeader(@NonNull String content) {
        Validate.notNull(tabList).setHeader(content);
    }

    /**
     * 플레이어의 탭리스트 푸터(하단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setTabListFooter(@NonNull String content) {
        Validate.notNull(tabList).setFooter(content);
    }

    /**
     * 플레이어의 탭리스트에서 지정한 항목을 설정한다.
     *
     * @param column  열 번호. 0~3 사이의 값
     * @param row     행 번호. 0~19 사이의 값
     * @param content 내용
     * @param skin    머리 스킨. {@code null}로 지정 시 머리 스킨 표시 안 함
     * @throws IndexOutOfBoundsException {@code column} 또는 {@code row}가 유효 범위를 초과하면 발생
     */
    public void setTabListItem(int column, int row, @NonNull String content, @Nullable Skin skin) {
        Validate.notNull(tabList);
        if (column < 0 || column > 3)
            throw new IndexOutOfBoundsException("'column'이 0에서 3 사이여야 함");
        if (row < 0 || row > 19)
            throw new IndexOutOfBoundsException("'row'가 0에서 19 사이여야 함");

        tabList.set(column, row, skin == null ? new TextTabItem(content, 0) : new TextTabItem(content, 0, skin));
    }

    /**
     * 플레이어의 탭리스트에서 지정한 항목을 제거한다.
     *
     * @param column 열 번호. 0~3 사이의 값
     * @param row    행 번호. 0~19 사이의 값
     * @throws IndexOutOfBoundsException {@code column} 또는 {@code row}가 유효 범위를 초과하면 발생
     */
    public void removeTabListItem(int column, int row) {
        Validate.notNull(tabList);
        if (column < 0 || column > 3)
            throw new IndexOutOfBoundsException("'column'이 0에서 3 사이여야 함");
        if (row < 0 || row > 19)
            throw new IndexOutOfBoundsException("'row'가 0에서 19 사이여야 함");

        tabList.remove(column, row);
    }

    /**
     * 플레이어의 탭리스트에서 모든 항목을 제거한다.
     */
    public void clearTabListItems() {
        Validate.notNull(tabList);

        for (int i = 0; i < 80; i++)
            tabList.remove(i);
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
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
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
        validate();

        if (nameTagHider == null)
            player.teleport(location);
        else {
            player.removePassenger(nameTagHider);
            player.teleport(location);
            nameTagHider.teleport(location);
            player.addPassenger(nameTagHider);
        }
    }
}
