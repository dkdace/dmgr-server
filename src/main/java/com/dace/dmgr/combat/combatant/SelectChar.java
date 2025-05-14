package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUIItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

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
            if (isClosed())
                return false;

            GameUser gameUser = GameUser.fromUser(User.fromPlayer(player));

            int[] columnIndexList = {2, 2, 2, 2, 2, 2};

            for (CombatantType combatantType : CombatantType.values()) {
                int rowIndex = combatantType.getCombatant().getRole().ordinal();
                int columnIndex = columnIndexList[rowIndex]++;

                set(rowIndex, columnIndex, createSelectItem(combatantType), itemBuilder -> {
                    if (gameUser != null && gameUser.getTeam().checkCombatantDuplication(combatantType))
                        itemBuilder.addLore("", "§c§l팀원이 이미 선택했습니다.");
                });
            }

            return true;
        }, 10);
    }

    /**
     * 지정한 전투원 종류에 해당하는 전투원 선택 아이템을 생성하여 반환한다.
     *
     * @param combatantType 전투원 종류
     * @return 전투원 선택 아이템
     */
    @NonNull
    private DefinedItem createSelectItem(@NonNull CombatantType combatantType) {
        Combatant combatant = combatantType.getCombatant();

        return new DefinedItem(new ItemBuilder(combatantType.getProfileItem())
                .setLore("",
                        MessageFormat.format("§f▍ 역할군 : {0}", combatant.getRole().getColor() + combatant.getRole().getName() +
                                (combatant.getSubRole() == null
                                        ? ""
                                        : " §f/ " + combatant.getSubRole().getColor() + combatant.getSubRole().getName())),
                        "",
                        "§7§n좌클릭§f하여 전투원을 선택합니다.",
                        "§7§n우클릭§f하여 전투원 정보를 확인합니다.")
                .build(),
                new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                    User user = User.fromPlayer(player);

                    GameUser gameUser = GameUser.fromUser(user);
                    if (gameUser != null && gameUser.getTeam().checkCombatantDuplication(combatantType))
                        return false;

                    CombatUser combatUser = CombatUser.fromUser(user);
                    if (combatUser == null)
                        combatUser = new CombatUser(combatantType, user);
                    else
                        combatUser.setCombatantType(combatantType);

                    player.closeInventory();

                    if (!user.getUserData().getCombatantRecord(combatantType).getCores().isEmpty())
                        combatUser.addTask(new DelayTask(() -> new SelectCore(player), 10));

                    return true;
                }),
                new DefinedItem.ClickHandler(ClickType.RIGHT, player -> {
                    new SelectCharInfo(player, combatantType);
                    return true;
                }));
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
