package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

/**
 * 전투원 선택 GUI 클래스.
 */
public final class SelectChar extends ChestGUI {
    /**
     * 전투원 선택 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public SelectChar(@NonNull Player player) {
        super(6, "§c§l전투원 선택", player);

        fillColumn(1, GUIItem.EMPTY);
        set(0, 0, SelectCharInfoItem.SCUFFLER.definedItem);
        set(1, 0, SelectCharInfoItem.MARKSMAN.definedItem);
        set(2, 0, SelectCharInfoItem.VANGUARD.definedItem);
        set(3, 0, SelectCharInfoItem.GUARDIAN.definedItem);
        set(4, 0, SelectCharInfoItem.SUPPORT.definedItem);
        set(5, 0, SelectCharInfoItem.CONTROLLER.definedItem);

        new IntervalTask(i -> {
            if (isDisposed())
                return false;

            CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
            if (combatUser == null)
                return false;

            GameUser gameUser = combatUser.getGameUser();

            int[] columnIndexList = {2, 2, 2, 2, 2, 2};

            for (CharacterType characterType : CharacterType.values()) {
                int rowIndex = characterType.getCharacter().getRole().ordinal();
                int columnIndex = columnIndexList[rowIndex]++;

                set(rowIndex, columnIndex, characterType.getSelectItem(), itemBuilder -> {
                    if (gameUser == null)
                        return;

                    boolean isDuplicated = gameUser.getTeam() != null && gameUser.getTeam().getTeamUsers().stream()
                            .anyMatch(targetGameUser -> {
                                CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
                                Validate.notNull(targetCombatUser);

                                return targetCombatUser.getCharacterType() == characterType;
                            });

                    if (isDuplicated)
                        itemBuilder.addLore("", "§c§l팀원이 이미 선택했습니다.");
                });
            }

            return true;
        }, 10);
    }

    /**
     * 전투원 선택 아이템 목록.
     */
    private enum SelectCharInfoItem {
        SCUFFLER(Material.IRON_SWORD, 4, Role.SCUFFLER),
        MARKSMAN(Material.BOW, 0, Role.MARKSMAN),
        VANGUARD(Material.IRON_CHESTPLATE, 0, Role.VANGUARD),
        GUARDIAN(Material.SHIELD, 0, Role.GUARDIAN),
        SUPPORT(Material.END_CRYSTAL, 0, Role.SUPPORT),
        CONTROLLER(Material.EYE_OF_ENDER, 0, Role.CONTROLLER);

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        SelectCharInfoItem(Material material, int damage, Role role) {
            this.definedItem = new DefinedItem(new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(MessageFormat.format("{0}§l{1} §7§o{2}",
                            role.getColor(),
                            role.getName(),
                            StringUtils.capitalize(toString().toLowerCase())))
                    .setLore(role.getDescription())
                    .build());
        }
    }
}
