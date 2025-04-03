package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Function;

/**
 * GUI에서 사용되는 빈 칸 또는 값 표시를 위한 아이템 및 버튼 등의 구성 요소 목록.
 */
@UtilityClass
public final class GUIItem {
    /** 빈 칸 */
    public static final DisplayItem EMPTY = new DisplayItem(1);
    /** 빈 칸 (왼쪽) */
    public static final DisplayItem EMPTY_LEFT = new DisplayItem(2);
    /** 빈 칸 (오른쪽) */
    public static final DisplayItem EMPTY_RIGHT = new DisplayItem(3);
    /** 빈 칸 (위쪽) */
    public static final DisplayItem EMPTY_UP = new DisplayItem(4);
    /** 빈 칸 (아래쪽) */
    public static final DisplayItem EMPTY_DOWN = new DisplayItem(5);
    /** 비활성화 */
    public static final DisplayItem DISABLED = new DisplayItem(6);
    /** 활성화 */
    public static final DisplayItem ENABLED = new DisplayItem(7);

    /** 나가기 버튼 */
    public static final ButtonItem EXIT = new ButtonItem(8, "§c§l나가기",
            new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                player.closeInventory();
                return true;
            }));

    /**
     * 표시용 GUI 아이템.
     */
    public static final class DisplayItem extends DefinedItem {
        private DisplayItem(int damage) {
            super(new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) damage)
                    .setName("§f")
                    .build());
        }
    }

    /**
     * 버튼 GUI 아이템.
     */
    public static class ButtonItem extends DefinedItem {
        private ButtonItem(int damage, @NonNull String name, @NonNull DefinedItem.ClickHandler clickHandler) {
            super(new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) damage)
                    .setName(name)
                    .build(), clickHandler);
        }
    }

    /**
     * 이전 버튼.
     */
    public static final class Previous extends ButtonItem {
        /**
         * 이전 버튼을 생성한다.
         *
         * @param onClick 클릭했을 때 이동할 GUI 반환에 실행할 작업.
         */
        public Previous(@NonNull Function<@NonNull Player, @NonNull GUI> onClick) {
            super(9, "§6§l이전", new ClickHandler(ClickType.LEFT, player -> {
                onClick.apply(player);
                return true;
            }));
        }
    }

    /**
     * 다음 버튼.
     */
    public static final class Next extends ButtonItem {
        public Next(@NonNull DefinedItem.ClickHandler clickHandler) {
            super(10, "§6§l다음", clickHandler);
        }
    }

    /**
     * 위 버튼.
     */
    public static final class Up extends ButtonItem {
        public Up(@NonNull DefinedItem.ClickHandler clickHandler) {
            super(11, "§6§l위로", clickHandler);
        }
    }

    /**
     * 아래 버튼.
     */
    public static final class Down extends ButtonItem {
        public Down(@NonNull DefinedItem.ClickHandler clickHandler) {
            super(12, "§6§l아래로", clickHandler);
        }
    }
}
