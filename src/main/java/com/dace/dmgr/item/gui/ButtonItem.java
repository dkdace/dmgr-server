package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * GUI에서 사용하는 클릭 가능한 버튼 아이템 목록.
 */
@UtilityClass
public final class ButtonItem {
    /**
     * 나가기 버튼.
     */
    public static final class Exit extends GuiItem {
        @Getter
        private static final Exit instance = new Exit();

        private Exit() {
            super("Exit", new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 8)
                    .setName("§c§l나가기")
                    .build());
        }

        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            player.closeInventory();
            return true;
        }
    }

    /**
     * 이전 버튼.
     */
    public abstract static class Left extends GuiItem {
        protected Left(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 9)
                    .setName("§6§l이전")
                    .build());
        }
    }

    /**
     * 다음 버튼.
     */
    public abstract static class Right extends GuiItem {
        protected Right(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 10)
                    .setName("§6§l다음")
                    .build());
        }
    }

    /**
     * 위 버튼.
     */
    public abstract static class Up extends GuiItem {
        protected Up(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 11)
                    .setName("§6§l위로")
                    .build());
        }
    }

    /**
     * 아래 버튼.
     */
    public abstract static class Down extends GuiItem {
        protected Down(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 12)
                    .setName("§6§l아래로")
                    .build());
        }
    }
}
