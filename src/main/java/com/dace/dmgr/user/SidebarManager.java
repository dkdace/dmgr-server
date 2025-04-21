package com.dace.dmgr.user;

import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;

/**
 * 플레이어의 사이드바 상태를 관리하는 클래스.
 */
public final class SidebarManager {
    /** 사이드바 인스턴스 */
    private final BPlayerBoard sidebar;

    /**
     * 사이드바 관리 인스턴스를 생성한다.
     *
     * @param user 대상 유저
     */
    SidebarManager(@NonNull User user) {
        this.sidebar = new BPlayerBoard(user.getPlayer(), "");
        clear();
    }

    /**
     * 사이드바의 이름을 지정한다.
     *
     * @param name 사이드바 이름
     */
    public void setName(@NonNull String name) {
        sidebar.setName(name);
    }

    /**
     * 사이드바의 내용을 설정한다.
     *
     * @param line    줄 번호. 0~14 사이의 값
     * @param content 내용
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void set(int line, @NonNull String content) {
        Validate.inclusiveBetween(0, 14, line, "14 >= line >= 0 (%d)", line);

        ChatColor[] chatColors = ChatColor.values();
        sidebar.set(chatColors[line] + content, 14 - line);
    }

    /**
     * 사이드바의 전체 내용을 설정한다.
     *
     * @param contents 내용 목록
     * @throws IllegalArgumentException {@code contents}의 길이가 15를 초과하면 발생
     */
    public void setAll(@NonNull String @NonNull ... contents) {
        Validate.inclusiveBetween(0, 15, contents.length, "contents.length <= 15 (%d)", contents.length);

        for (int i = 0; i < contents.length; i++)
            set(i, contents[i]);
    }

    /**
     * 사이드바의 내용을 초기화한다.
     */
    public void clear() {
        sidebar.clear();
    }

    /**
     * 사이드바를 제거한다.
     */
    void delete() {
        sidebar.delete();
    }
}
