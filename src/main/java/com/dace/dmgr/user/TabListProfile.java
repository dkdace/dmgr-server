package com.dace.dmgr.user;

import com.dace.dmgr.PlayerSkin;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.util.Skins;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 탭리스트의 프로필 클래스.
 */
public interface TabListProfile {
    /**
     * 탭리스트의 헤더(상단부)의 내용을 반환한다.
     *
     * @return 헤더 내용
     */
    @NonNull
    default String getHeader() {
        return "";
    }

    /**
     * 탭리스트의 푸터(하단부)의 내용을 반환한다.
     *
     * @return 푸터 내용
     */
    @NonNull
    default String getFooter() {
        return "";
    }

    /**
     * 탭리스트의 항목을 업데이트한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * &#64;Override
     * public void updateItems(Item[][] items) {
     *     // 0번째 열, 3번째 행의 항목을 지정
     *     items[0][3] = new Item("Hello", user);
     * }
     * </code></pre>
     *
     * @param items 탭리스트의 항목 목록. 탭리스트의 열과 행을 나타내는 길이가 [4][20]인 2차원 배열
     */
    void updateItems(@Nullable Item @NonNull [] @NonNull [] items);

    /**
     * 탭리스트의 항목을 나타내는 클래스.
     */
    @Getter(AccessLevel.PACKAGE)
    final class Item {
        /** 공백 항목 */
        public static final Item BLANK_ITEM = new Item("");
        /** Tabbed 텍스트 항목 인스턴스 */
        @NonNull
        private final TextTabItem textTabItem;

        /**
         * 탭리스트 항목 인스턴스를 생성한다.
         *
         * @param content 내용
         */
        public Item(@NonNull String content) {
            this.textTabItem = new TextTabItem(content, -1, Skins.DEFAULT_SKIN);
        }

        /**
         * 탭리스트 항목 인스턴스를 생성한다.
         *
         * @param content    내용
         * @param playerSkin 머리 스킨
         */
        public Item(@NonNull String content, @NonNull PlayerSkin playerSkin) {
            this.textTabItem = new TextTabItem(content, -1, playerSkin.getSkin());
        }

        /**
         * 탭리스트 항목 인스턴스를 생성한다.
         *
         * @param content 내용
         * @param user    표시할 플레이어
         */
        public Item(@NonNull String content, @NonNull User user) {
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

            this.textTabItem = new TextTabItem(content, realPing, Skins.getPlayer(user.getPlayer()));
        }
    }
}
