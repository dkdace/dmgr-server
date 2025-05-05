package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.util.ReflectionUtil;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 로비 탭리스트 프로필 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class LobbyTabListProfile implements TabListProfile {
    /** 헤더 문자열 */
    public static final String HEADER = "\n" + GeneralConfig.getConfig().getMessagePrefix() + "§e스킬 PVP 미니게임 서버 §f:: §d§nDMGR.mcsv.kr\n";
    /** 푸터 문자열 */
    public static final String FOOTER = "\n§7현재 서버는 테스트 단계이며, 시스템 상 문제점이나 버그가 발생할 수 있습니다.\n";

    /** 유저 인스턴스 */
    private final User user;

    /**
     * 서버의 최근 TPS (Ticks Per Second)를 반환한다.
     *
     * @return 최근 TPS
     */
    private static double getTPS() {
        try {
            Class<?> minecraftServerClass = ReflectionUtil.getClass("net.minecraft.server.v1_12_R1.MinecraftServer");
            Object minecraftServer = ReflectionUtil.getMethod(minecraftServerClass, "getServer").invoke(null);
            Field recentTpsField = ReflectionUtil.getField(minecraftServerClass, "recentTps");

            double[] recent = (double[]) recentTpsField.get(minecraftServer);
            return recent[0];
        } catch (Exception ex) {
            ConsoleLogger.severe("서버 TPS를 구할 수 없음", ex);
        }

        return -1;
    }

    @Override
    @NonNull
    public String getHeader() {
        return HEADER;
    }

    @Override
    @NonNull
    public String getFooter() {
        return FOOTER;
    }

    public void updateItems(@Nullable Item @NonNull [] @NonNull [] items) {
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long memory = totalMemory - freeMemory;
        double memoryPercent = (double) memory / totalMemory;
        double tps = getTPS();
        UserData userData = user.getUserData();

        ChatColor memoryColor = ChatColor.RED;
        if (memoryPercent < 0.5)
            memoryColor = ChatColor.GREEN;
        else if (memoryPercent < 0.75)
            memoryColor = ChatColor.YELLOW;
        else if (memoryPercent < 0.9)
            memoryColor = ChatColor.GOLD;

        ChatColor pingColor = ChatColor.RED;
        if (user.getPing() < 70)
            pingColor = ChatColor.GREEN;
        else if (user.getPing() < 100)
            pingColor = ChatColor.YELLOW;
        else if (user.getPing() < 130)
            pingColor = ChatColor.GOLD;

        ChatColor tpsColor = ChatColor.GREEN;
        if (tps < 19)
            tpsColor = ChatColor.RED;
        else if (tps < 19.4)
            tpsColor = ChatColor.GOLD;
        else if (tps < 19.7)
            tpsColor = ChatColor.YELLOW;

        items[0][0] = new Item("§f§l§n 서버 상태 ", Skin.SERVER_STATUS);
        items[0][2] = new Item(MessageFormat.format("§f PING §7:: {0}{1} ms", pingColor, user.getPing()), Skin.PING);
        items[0][3] = new Item(MessageFormat.format("§f 메모리 §7:: {0}{1} §f/ {2} (MB)", memoryColor, memory, totalMemory), Skin.MEMORY);
        items[0][4] = new Item(MessageFormat.format("§f TPS §7:: {0}{1} tick/s", tpsColor, tps), Skin.TPS);
        items[0][5] = new Item(MessageFormat.format("§f 접속자 수 §7:: §f{0}명", Bukkit.getOnlinePlayers().size()), Skin.ONLINE);
        items[0][7] = new Item(MessageFormat.format("§f§n §e§n{0}§f§l§n님의 전적 ", user.getPlayer().getName()),
                PlayerSkin.fromUUID(user.getPlayer().getUniqueId()));
        items[0][9] = new Item(MessageFormat.format("§e 승률 §7:: §b{0}승 §f/ §c{1}패 §f({2}%)",
                userData.getWinCount(),
                userData.getLoseCount(),
                (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100), Skin.WIN_RATE);
        items[0][10] = new Item(MessageFormat.format("§e 탈주 §7:: §c{0}회", userData.getQuitCount()), Skin.QUIT);
        items[0][11] = new Item(MessageFormat.format("§e 플레이 시간 §7:: §f{0}",
                DurationFormatUtils.formatDuration(userData.getPlayTime().toMilliseconds(), "d일 H시간 m분")), Skin.PLAY_TIME);

        Iterator<User> lobbyUsersIterator = User.getAllUsers().stream()
                .sorted(Comparator.comparing(target -> target.getPlayer().getName()))
                .filter(target -> !target.getPlayer().isOp())
                .limit(38)
                .iterator();
        Iterator<User> adminUsersIterator = User.getAllUsers().stream()
                .sorted(Comparator.comparing(target -> target.getPlayer().getName()))
                .filter(target -> target.getPlayer().isOp())
                .limit(19)
                .iterator();

        int i = 0;
        for (; lobbyUsersIterator.hasNext(); i++) {
            User lobbyUser = lobbyUsersIterator.next();
            items[1 + i % 2][i / 2 + 1] = new Item(getTabListPlayerName(lobbyUser), lobbyUser);
        }
        items[1][0] = new Item(MessageFormat.format("§a§l§n 접속 인원 §f({0}명)", i), Skin.LOBBY_USERS);

        int j = 0;
        for (; adminUsersIterator.hasNext(); j++) {
            User adminUser = adminUsersIterator.next();
            items[3][j + 1] = new Item(getTabListPlayerName(adminUser), adminUser);
        }
        items[3][0] = new Item(MessageFormat.format("§b§l§n 관리자 §f({0}명)", j), Skin.ADMIN_USERS);
    }

    /**
     * 탭리스트에 사용되는 플레이어의 이름을 반환한다.
     *
     * @param user 대상 플레이어
     * @return 이름
     */
    @NonNull
    private String getTabListPlayerName(@NonNull User user) {
        String prefix = "§7[로비]";

        if (user.getGameRoom() != null)
            prefix = user.getGameRoom().getName();
        else if (user.getCurrentPlace() != Place.LOBBY)
            prefix = MessageFormat.format("§7[{0}]", user.getCurrentPlace());

        return MessageFormat.format(" {0} §f{1}", prefix, user.getPlayer().getName());
    }

    /**
     * 머리 스킨 목록.
     */
    @UtilityClass
    private static final class Skin {
        /** 서버 상태 */
        private static final PlayerSkin SERVER_STATUS = PlayerSkin.fromName("DTabServerStatus");
        /** 핑 */
        private static final PlayerSkin PING = PlayerSkin.fromName("DTabPing");
        /** 메모리 */
        private static final PlayerSkin MEMORY = PlayerSkin.fromName("DTabMemory");
        /** TPS */
        private static final PlayerSkin TPS = PlayerSkin.fromName("DTabTPS");
        /** 접속자 수 */
        private static final PlayerSkin ONLINE = PlayerSkin.fromName("DTabOnline");
        /** 승률 */
        private static final PlayerSkin WIN_RATE = PlayerSkin.fromName("DTabWinRate");
        /** 탈주 */
        private static final PlayerSkin QUIT = PlayerSkin.fromName("DTabQuit");
        /** 플레이 시간 */
        private static final PlayerSkin PLAY_TIME = PlayerSkin.fromName("DTabPlayTime");
        /** 접속 인원 */
        private static final PlayerSkin LOBBY_USERS = PlayerSkin.fromSkin(Skins.getDot(ChatColor.GREEN));
        /** 관리자 */
        private static final PlayerSkin ADMIN_USERS = PlayerSkin.fromSkin(Skins.getDot(ChatColor.AQUA));
    }
}
