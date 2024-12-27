package com.dace.dmgr.effect;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * 보스바 표시 기능을 제공하는 클래스.
 */
public final class BossBarDisplay {
    /** 보스바 인스턴스 */
    private final BossBar bossBar;

    /**
     * 보스바를 생성한다.
     *
     * @param title    제목
     * @param color    막대 색
     * @param style    막대 스타일
     * @param progress 진행률. 0~1 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public BossBarDisplay(@NonNull String title, @NonNull BarColor color, @NonNull BarStyle style, double progress) {
        this.bossBar = Bukkit.createBossBar(title, color, style);
        setProgress(progress);
    }

    /**
     * 보스바를 생성한다.
     *
     * @param title 제목
     * @param color 막대 색
     * @param style 막대 스타일
     */
    public BossBarDisplay(@NonNull String title, @NonNull BarColor color, @NonNull BarStyle style) {
        this(title, color, style, 0);
    }

    /**
     * 보스바를 생성한다.
     *
     * @param title 제목
     */
    public BossBarDisplay(@NonNull String title) {
        this(title, BarColor.WHITE, BarStyle.SOLID, 0);
    }

    /**
     * 보스바의 제목을 반환한다.
     *
     * @return 제목
     */
    @NonNull
    public String getTitle() {
        return bossBar.getTitle();
    }

    /**
     * 보스바의 제목을 설정한다.
     *
     * @param title 제목
     */
    public void setTitle(@NonNull String title) {
        bossBar.setTitle(title);
    }

    /**
     * 보스바의 진행률을 반환한다.
     *
     * @return 진행률
     */
    public double getProgress() {
        return bossBar.getProgress();
    }

    /**
     * 보스바의 진행률을 설정한다.
     *
     * @param progress 진행률. 0~1 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setProgress(double progress) {
        Validate.inclusiveBetween(0, 1, progress);
        bossBar.setProgress(progress);
    }

    /**
     * 지정한 플레이어에게 보스바를 표시한다.
     *
     * @param player 대상 플레이어
     */
    public void show(@NonNull Player player) {
        bossBar.addPlayer(player);
    }

    /**
     * 지정한 플레이어에게서 보스바를 숨긴다.
     *
     * @param player 대상 플레이어
     */
    public void hide(@NonNull Player player) {
        bossBar.removePlayer(player);
    }
}
