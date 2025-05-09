package com.dace.dmgr.game;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.user.LobbyTabListProfile;
import com.dace.dmgr.user.TabListProfile;
import com.dace.dmgr.util.task.AsyncTask;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 게임 탭리스트 프로필 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class GameTabListProfile implements TabListProfile {
    /** 미공개 상태의 플레이어 머리 스킨 */
    private static final PlayerSkin.Async UNKNOWN_PLAYER_SKIN = PlayerSkin.fromName("DTabUnknown");
    /** 게임 유저 인스턴스 */
    private final GameUser gameUser;

    /**
     * 모든 머리 스킨을 불러온다.
     *
     * <p>플러그인 활성화 시 호출해야 한다.</p>
     */
    @NonNull
    public static AsyncTask<Void> loadSkins() {
        return UNKNOWN_PLAYER_SKIN.init().onFinish(() -> {
        });
    }

    @Override
    @NonNull
    public String getHeader() {
        Game game = gameUser.getGame();

        return MessageFormat.format("\n{0} §f{1}\n",
                (game.getGamePlayMode().isRanked() ? "§6§l[ 랭크 ]" : "§a§l[ 일반 ]"),
                game.getGamePlayMode().getName());
    }

    @Override
    @NonNull
    public String getFooter() {
        return LobbyTabListProfile.FOOTER;
    }

    @Override
    public void updateItems(@Nullable Item @NonNull [] @NonNull [] items) {
        Game game = gameUser.getGame();

        boolean isHeadReveal = game.isPlaying() && game.getElapsedTime().compareTo(GeneralConfig.getGameConfig().getHeadRevealTimeAfterStart()) > 0;

        int column = 0;
        for (Team team : new Team[]{game.getRedTeam(), game.getBlueTeam()}) {
            ChatColor targetTeamColor = team.getType().getColor();

            items[++column][0] = new Item(MessageFormat.format("{0}§l§n {1} §f({2}명)",
                    targetTeamColor,
                    team.getType().getName(),
                    team.getTeamUsers().size()), PlayerSkin.fromSkin(Skins.getDot(targetTeamColor)));

            Iterator<GameUser> iterator = team.getTeamUsers().stream()
                    .sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .iterator();

            for (int i = 0; iterator.hasNext(); i++) {
                int row = i * 3 + 1;

                GameUser target = iterator.next();
                String name = MessageFormat.format("{0} {1}", targetTeamColor, target.getPlayer().getName());

                items[column][row] = gameUser.getTeam() == team || isHeadReveal
                        ? new Item(name, target.getUser())
                        : new Item(name, UNKNOWN_PLAYER_SKIN.get());
                items[column][row + 1] = new Item(MessageFormat.format("§7{0} §f{1}   §7{2} §f{3}   §7{4} §f{5}   §7{6} §f{7}  ",
                        "✪",
                        (int) target.getScore(),
                        TextIcon.DAMAGE,
                        target.getKill(),
                        TextIcon.POISON,
                        target.getDeath(),
                        "✔",
                        target.getAssist()));
            }
        }
    }
}
