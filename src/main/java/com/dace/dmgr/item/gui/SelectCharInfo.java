package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * 전투원 선택 정보 GUI 클래스.
 */
public final class SelectCharInfo extends ChestGUI {
    /**
     * 지정한 전투원의 선택 정보 GUI 인스턴스를 생성한다.
     *
     * @param player        GUI 표시 대상 플레이어
     * @param characterType 전투원 종류
     */
    public SelectCharInfo(@NonNull Player player, @NonNull CharacterType characterType) {
        super(6, "§c§l전투원 정보", player);

        Character character = characterType.getCharacter();

        fillAll(GUIItem.EMPTY);
        set(0, 3, new DefinedItem(characterType.getProfileItem()), itemBuilder -> itemBuilder
                .setLore("",
                        "§e✪ 난이도 §7:: §f{0}",
                        "§a{1} 체력 §7:: §f{2}",
                        "§b{3} 이동속도 배수 §7:: §f{4}",
                        "§6⬜ 히트박스 배수 §7:: §f{5}")
                .formatLore(
                        StringFormUtil.getProgressBar(character.getDifficulty(), 5, ChatColor.YELLOW, 5, '✰')
                                .replace("§0", "§8"),
                        TextIcon.HEAL,
                        character.getHealth(),
                        TextIcon.WALK_SPEED,
                        character.getSpeedMultiplier(),
                        character.getHitboxMultiplier()));

        set(0, 5, character.getWeaponInfo().getDefinedItem());

        set(2, 2, SelectCharInfoItem.TRAIT.definedItem);
        displayActions(2, SelectCharInfoItem.TRAIT, character);
        set(3, 2, SelectCharInfoItem.PASSIVE_SKILL.definedItem);
        displayActions(3, SelectCharInfoItem.PASSIVE_SKILL, character);
        set(4, 2, SelectCharInfoItem.ACTIVE_SKILL.definedItem);
        displayActions(4, SelectCharInfoItem.ACTIVE_SKILL, character);

        set(5, 8, new GUIItem.Previous(SelectChar::new));
    }

    /**
     * 지정한 동작 종류에 해당하는 모든 동작 정보를 표시한다.
     *
     * @param row        행 번호
     * @param actionType 동작 종류
     * @param character  전투원
     */
    private void displayActions(int row, @NonNull SelectCharInfoItem actionType, @NonNull Character character) {
        for (int i = 1; i <= 4; i++) {
            ActionInfo actionInfo = null;
            switch (actionType) {
                case TRAIT:
                    actionInfo = character.getTraitInfo(i);
                    break;
                case PASSIVE_SKILL:
                    actionInfo = character.getPassiveSkillInfo(i);
                    break;
                case ACTIVE_SKILL:
                    actionInfo = character.getActiveSkillInfo(i);
                    break;
            }

            if (actionInfo == null)
                remove(row, i + 2);
            else
                set(row, i + 2, actionInfo.getDefinedItem());
        }
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
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkullUtil.fromURL(skinUrl)).setName(name).build());
        }
    }
}
