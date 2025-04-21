package com.dace.dmgr.user;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.PlayerSkin;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TabList;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 플레이어의 탭리스트 상태를 관리하는 클래스.
 */
public final class TabListManager {
    /** 탭리스트의 공백 항목 */
    private static final TextTabItem BLANK_TAB_ITEM = new TextTabItem("", -1);
    /** 탭리스트 인스턴스 */
    private final TableTabList tabList;

    /**
     * 탭리스트 관리 인스턴스를 생성한다.
     *
     * @param user 대상 유저
     */
    TabListManager(@NonNull User user) {
        Player player = user.getPlayer();
        TabList tableTabList = DMGR.getTabbed().getTabList(player);
        this.tabList = tableTabList == null ? DMGR.getTabbed().newTableTabList(player) : (TableTabList) tableTabList;

        this.tabList.setBatchEnabled(true);
        clearItems();
    }

    private static void validateColumnRow(int column, int row) {
        Validate.inclusiveBetween(0, 3, column, "3 >= column >= 0 (%d)", column);
        Validate.inclusiveBetween(0, 19, row, "19 >= row >= 0 (%d)", row);
    }

    /**
     * 탭리스트 헤더(상단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setHeader(@NonNull String content) {
        tabList.setHeader(content);
    }

    /**
     * 탭리스트 푸터(하단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setFooter(@NonNull String content) {
        tabList.setFooter(content);
    }

    /**
     * 지정한 번호의 항목을 설정한다.
     *
     * @param column     열 번호. 0~3 사이의 값
     * @param row        행 번호. 0~19 사이의 값
     * @param content    내용
     * @param playerSkin 머리 스킨. {@code null}로 지정 시 머리 스킨 표시 안 함
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setItem(int column, int row, @NonNull String content, @Nullable PlayerSkin playerSkin) {
        validateColumnRow(column, row);
        tabList.set(column, row, new TextTabItem(content, -1, playerSkin == null ? Skins.DEFAULT_SKIN : playerSkin.getSkin()));
    }

    /**
     * 지정한 번호의 항목을 설정한다.
     *
     * @param column  열 번호. 0~3 사이의 값
     * @param row     행 번호. 0~19 사이의 값
     * @param content 내용
     * @param user    표시할 플레이어
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setItem(int column, int row, @NonNull String content, @NonNull User user) {
        validateColumnRow(column, row);

        int realPing;
        if (user.getPing() < 50)
            realPing = 0;
        else if (user.getPing() < 70)
            realPing = 150;
        else if (user.getPing() < 100)
            realPing = 300;
        else if (user.getPing() < 130)
            realPing = 600;
        else
            realPing = 1000;

        tabList.set(column, row, new TextTabItem(content, realPing, Skins.getPlayer(user.getPlayer())));
    }

    /**
     * 지정한 번호의 항목을 제거한다.
     *
     * @param column 열 번호. 0~3 사이의 값
     * @param row    행 번호. 0~19 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void removeItem(int column, int row) {
        validateColumnRow(column, row);
        tabList.set(column, row, BLANK_TAB_ITEM);
    }

    /**
     * 탭리스트의 모든 항목을 제거한다.
     */
    public void clearItems() {
        for (int i = 0; i < 80; i++)
            tabList.set(i, BLANK_TAB_ITEM);
    }

    /**
     * 탭리스트 변경 사항을 업데이트한다.
     *
     * <p>탭리스트 내용 변경 후 호출해야 한다.</p>
     */
    void update() {
        tabList.batchUpdate();
    }

    /**
     * 탭리스트를 제거한다.
     */
    void delete() {
        tabList.disable();
    }
}
