package com.dace.dmgr.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.item.DisplayItem;
import com.dace.dmgr.system.EntityInfoRegistry;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 전투원 선택 GUI 클래스.
 */
public final class SelectChar extends Gui {
    @Getter
    private static final SelectChar instance = new SelectChar();

    public SelectChar() {
        super(6, "§c§l전투원 선택");
    }

    /**
     * GUI에 전투원 목록을 표시한다.
     *
     * @param inventory 인벤토리
     */
    private void displayCharacters(Inventory inventory) {
        int role1i = 2;
        int role2i = 11;
        int role3i = 20;
        int role4i = 29;
        int role5i = 38;
        int role6i = 47;

        for (CharacterType characterType : CharacterType.values()) {
            int j = 0;

            switch (characterType.getCharacter().getRole()) {
                case ASSASSIN:
                    j = role1i++;
                    break;
                case SCUFFLER:
                    j = role2i++;
                    break;
                case MARKSMAN:
                    j = role3i++;
                    break;
                case VANGUARD:
                    j = role4i++;
                    break;
                case GUARDIAN:
                    j = role5i++;
                    break;
                case SUPPORTER:
                    j = role6i++;
                    break;
            }

            inventory.setItem(j, ItemBuilder.fromPlayerSkull(characterType.getCharacter().getSkin())
                    .setName("§c" + characterType.getCharacter().getName())
                    .setLore("§f전투원 설명", characterType.toString())
                    .build());
        }
    }

    @Override
    protected void onOpen(Player player, Inventory inventory) {
        inventory.setItem(0,
                new ItemBuilder(Material.DIAMOND_HOE).setName(Role.ASSASSIN.getColor() + "§l" + Role.ASSASSIN.getName())
                        .setLore(Role.ASSASSIN.getDescription())
                        .setDamage((short) 4)
                        .addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
        inventory.setItem(9,
                new ItemBuilder(Material.IRON_SWORD).setName(Role.SCUFFLER.getColor() + "§l" + Role.SCUFFLER.getName())
                        .setLore(Role.SCUFFLER.getDescription())
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
        inventory.setItem(18,
                new ItemBuilder(Material.BOW).setName(Role.MARKSMAN.getColor() + "§l" + Role.MARKSMAN.getName())
                        .setLore(Role.MARKSMAN.getDescription())
                        .build());
        inventory.setItem(27,
                new ItemBuilder(Material.IRON_CHESTPLATE).setName(Role.VANGUARD.getColor() + "§l" + Role.VANGUARD.getName())
                        .setLore(Role.VANGUARD.getDescription())
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
        inventory.setItem(36,
                new ItemBuilder(Material.SHIELD).setName(Role.GUARDIAN.getColor() + "§l" + Role.GUARDIAN.getName())
                        .setLore(Role.GUARDIAN.getDescription())
                        .build());
        inventory.setItem(45,
                new ItemBuilder(Material.END_CRYSTAL).setName(Role.SUPPORTER.getColor() + "§l" + Role.SUPPORTER.getName())
                        .setLore(Role.SUPPORTER.getDescription())
                        .build());
        ItemMeta itemMetaRole1 = inventory.getItem(0).getItemMeta();
        itemMetaRole1.setUnbreakable(true);
        inventory.getItem(0).setItemMeta(itemMetaRole1);

        for (int i = 1; i < 47; i += 9)
            inventory.setItem(i, DisplayItem.EMPTY.getItemStack());

        displayCharacters(inventory);
    }

    @Override
    protected void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
            return;

        if (event.getClick() == ClickType.LEFT) {
            String name = event.getCurrentItem().getItemMeta().getLore().get(1);
            CharacterType characterType = CharacterType.valueOf(name);

            CombatUser combatUser = EntityInfoRegistry.getCombatUser(player);
            if (combatUser != null)
                combatUser.setCharacterType(characterType);

            player.closeInventory();
        }
    }

    /**
     * 전투원 선택 관련 메시지 목록.
     */
    public interface MESSAGES {
        /** 전투원 선택 알림 */
        String SELECT_CHARACTER = "§b§nF키§b를 눌러 전투원을 선택하십시오.";
        /** 전투원 변경 알림 */
        String CHANGE_CHARACTER = "§b§nF키§b를 눌러 전투원을 변경할 수 있습니다.";
    }
}
