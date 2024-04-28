package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

            guiController.set(j, CharacterType.valueOf(characterType.toString()).getGuiItem());
        }
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillColumn(2, DisplayItem.EMPTY.getStaticItem());
        guiController.set(0, SelectCharInfoItem.ASSASSIN.staticItem);
        guiController.set(9, SelectCharInfoItem.SCUFFLER.staticItem);
        guiController.set(18, SelectCharInfoItem.MARKSMAN.staticItem);
        guiController.set(27, SelectCharInfoItem.VANGUARD.staticItem);
        guiController.set(36, SelectCharInfoItem.GUARDIAN.staticItem);
        guiController.set(45, SelectCharInfoItem.SUPPORTER.staticItem);

        displayCharacters(guiController);
    }

    private enum SelectCharInfoItem {
        ASSASSIN(Material.DIAMOND_HOE, 4, Role.ASSASSIN),
        SCUFFLER(Material.IRON_SWORD, 0, Role.SCUFFLER),
        MARKSMAN(Material.BOW, 0, Role.MARKSMAN),
        VANGUARD(Material.IRON_CHESTPLATE, 0, Role.VANGUARD),
        GUARDIAN(Material.SHIELD, 0, Role.GUARDIAN),
        SUPPORTER(Material.END_CRYSTAL, 0, Role.SUPPORTER);

        /** 정적 아이템 객체 */
        private final StaticItem staticItem;

        SelectCharInfoItem(Material material, int damage, Role role) {
            this.staticItem = new StaticItem("SelectCharInfo" + this, new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(role.getColor() + "§l" + role.getName())
                    .setLore(role.getDescription())
                    .build());
        }
    }
}
