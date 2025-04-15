package com.dace.dmgr.item.gui;

import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 전투원 선택 정보 GUI 클래스.
 */
public final class SelectCharInfo extends ChestGUI {
    /**
     * 지정한 전투원의 선택 정보 GUI 인스턴스를 생성한다.
     *
     * @param player        GUI 표시 대상 플레이어
     * @param combatantType 전투원 종류
     */
    public SelectCharInfo(@NonNull Player player, @NonNull CombatantType combatantType) {
        super(6, "§c§l전투원 정보", player);

        Combatant combatant = combatantType.getCombatant();

        fillAll(GUIItem.EMPTY);
        set(0, 3, combatantType.getInfoItem());

        set(0, 5, combatant.getWeaponInfo().getDefinedItem());

        set(2, 2, SelectCharInfoItem.TRAIT.definedItem);
        displayActions(2, SelectCharInfoItem.TRAIT, combatant);
        set(3, 2, SelectCharInfoItem.PASSIVE_SKILL.definedItem);
        displayActions(3, SelectCharInfoItem.PASSIVE_SKILL, combatant);
        set(4, 2, SelectCharInfoItem.ACTIVE_SKILL.definedItem);
        displayActions(4, SelectCharInfoItem.ACTIVE_SKILL, combatant);

        set(5, 8, new GUIItem.Previous(SelectChar::new));
    }

    /**
     * 지정한 동작 종류에 해당하는 모든 동작 정보를 표시한다.
     *
     * @param row        행 번호
     * @param actionType 동작 종류
     * @param combatant  전투원
     */
    private void displayActions(int row, @NonNull SelectCharInfoItem actionType, @NonNull Combatant combatant) {
        ActionInfo[] actionInfos;
        switch (actionType) {
            case TRAIT:
                actionInfos = combatant.getTraitInfos();
                break;
            case PASSIVE_SKILL:
                actionInfos = combatant.getPassiveSkillInfos();
                break;
            case ACTIVE_SKILL:
                actionInfos = combatant.getActiveSkillInfos();
                break;
            default:
                return;
        }

        for (int i = 0; i < 4; i++)
            if (i > actionInfos.length - 1)
                remove(row, i + 3);
            else
                set(row, i + 3, actionInfos[i].getDefinedItem());
    }

    /**
     * 전투원 선택 정보의 아이템 목록.
     */
    private enum SelectCharInfoItem {
        TRAIT("OWZjMzJjOTE0Mjc2ZjY4NjE0MTc5NmE1YTAyM2MzOWVmZGZlZDE0ZDNkN2M5YzQyNzkyNTEzODQ2ZjdmYTRiMyJ9fX0=",
                "§b§l특성 §7§oTrait"),
        PASSIVE_SKILL("NjVlZDYwMzVhNTEyYzYzZmY1MzEyNTczZjk1MTFiMzE0M2NlN2Q3YWFiYTIyMzQ1ZGZmNDM5NzM2ZDUxYzFjIn19fQ==",
                "§e§l패시브 스킬 §7§oPassive Skill"),
        ACTIVE_SKILL("NTFkMzgzNDAxZjc3YmVmZmNiOTk4YzJjZjc5YjdhZmVlMjNmMThjNDFkOGE1NmFmZmVkNzliYjU2ZTIyNjdhMyJ9fX0=",
                "§c§l액티브 스킬 §7§oActive Skill");

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        SelectCharInfoItem(String skinUrl, String name) {
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkin.fromURL(skinUrl)).setName(name).build());
        }
    }
}
