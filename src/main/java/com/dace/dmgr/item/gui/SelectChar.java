package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SkinUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

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
     * @param guiController GUI 컨트롤러 객체
     */
    private void displayCharacters(@NonNull GuiController guiController) {
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

            guiController.set(j, SelectCharItem.valueOf(characterType.toString()).guiItem);
        }
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillColumn(2, DisplayItem.EMPTY.getGuiItem());
        guiController.set(0, SelectCharInfoItem.ASSASSIN.guiItem);
        guiController.set(9, SelectCharInfoItem.SCUFFLER.guiItem);
        guiController.set(18, SelectCharInfoItem.MARKSMAN.guiItem);
        guiController.set(27, SelectCharInfoItem.VANGUARD.guiItem);
        guiController.set(36, SelectCharInfoItem.GUARDIAN.guiItem);
        guiController.set(45, SelectCharInfoItem.SUPPORTER.guiItem);

        displayCharacters(guiController);
    }

    @Override
    protected void onClick(InventoryClickEvent event, @NonNull Player player, @NonNull GuiItem<?> guiItem) {
        if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
            return;

        if (event.getClick() == ClickType.LEFT) {
            String name = event.getCurrentItem().getItemMeta().getLore().get(1);
            CharacterType characterType = CharacterType.valueOf(name);

            CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
            if (combatUser != null)
                combatUser.setCharacterType(characterType);

            player.closeInventory();
        }
    }

    private enum SelectCharInfoItem {
        ASSASSIN(Material.DIAMOND_HOE, 4, Role.ASSASSIN),
        SCUFFLER(Material.IRON_SWORD, 0, Role.SCUFFLER),
        MARKSMAN(Material.BOW, 0, Role.MARKSMAN),
        VANGUARD(Material.IRON_CHESTPLATE, 0, Role.VANGUARD),
        GUARDIAN(Material.SHIELD, 0, Role.GUARDIAN),
        SUPPORTER(Material.END_CRYSTAL, 0, Role.SUPPORTER);

        /** GUI 아이템 객체 */
        private final GuiItem<SelectCharInfoItem> guiItem;

        SelectCharInfoItem(Material material, int damage, Role role) {
            this.guiItem = new GuiItem<SelectCharInfoItem>(this, new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(role.getColor() + "§l" + role.getName())
                    .setLore(role.getDescription())
                    .build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return false;
                }
            };
        }
    }

    @Getter
    public enum SelectCharItem {
        ARKACE(CharacterType.ARKACE),
        JAGER(CharacterType.JAGER);

        /** GUI 아이템 객체 */
        private final GuiItem<SelectCharItem> guiItem;

        SelectCharItem(CharacterType characterType) {
            this.guiItem = new GuiItem<SelectCharItem>(this, ItemBuilder.fromPlayerSkull(SkinUtil.getSkinUrl(characterType.getCharacter().getSkinName()))
                    .setName("§c" + characterType.getCharacter().getName())
                    .setLore("§f전투원 설명", characterType.toString())
                    .build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return true;
                }
            };
        }
    }
}
