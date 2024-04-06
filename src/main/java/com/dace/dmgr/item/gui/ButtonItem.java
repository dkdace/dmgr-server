package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * GUI에서 사용하는 클릭 가능한 버튼 아이템 목록.
 */
public interface ButtonItem {
    /**
     * 나가기 버튼.
     */
    class EXIT extends GuiItem {
        protected EXIT(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
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
    abstract class LEFT extends GuiItem {
        protected LEFT(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 9)
                    .setName("§6§l이전")
                    .build());
        }
    }

    /**
     * 다음 버튼.
     */
    abstract class RIGHT extends GuiItem {
        protected RIGHT(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 10)
                    .setName("§6§l다음")
                    .build());
        }
    }

    /**
     * 위 버튼.
     */
    abstract class UP extends GuiItem {
        protected UP(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 11)
                    .setName("§6§l위로")
                    .build());
        }
    }

    /**
     * 아래 버튼.
     */
    abstract class DOWN extends GuiItem {
        protected DOWN(@NonNull String identifier) {
            super(identifier, new ItemBuilder(Material.CARROT_STICK)
                    .setDamage((short) 12)
                    .setName("§6§l아래로")
                    .build());
        }
    }
}
