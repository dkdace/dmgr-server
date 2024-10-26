package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.util.SkinUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * 전투원 선택 정보 GUI 클래스.
 */
public final class SelectCharInfo extends Gui {
    /** 이전 버튼 GUI 아이템 객체 */
    private static final GuiItem buttonLeft = new ButtonItem.Left("SelectCharInfoLeft") {
        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            SelectChar.getInstance().open(player);
            return true;
        }
    };
    /** 대상 전투원 종류 */
    private final CharacterType characterType;

    /**
     * 지정한 전투원의 선택 정보 GUI 인스턴스를 생성한다.
     *
     * @param characterType 전투원 종류
     */
    public SelectCharInfo(@NonNull CharacterType characterType) {
        super(6, "§c§l전투원 정보");
        this.characterType = characterType;
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        Character character = characterType.getCharacter();

        guiController.fillAll(DisplayItem.EMPTY.getStaticItem());
        guiController.set(3, characterType.getGuiItem(), itemBuilder -> itemBuilder.setLore("",
                "§e✪ 난이도 §7:: §f" + StringFormUtil.getProgressBar(character.getDifficulty(), 5, ChatColor.YELLOW, 5, '✰')
                        .replace("§0", "§8"),
                "§a" + TextIcon.HEAL + " 체력 §7:: §f" + character.getHealth(),
                "§b" + TextIcon.WALK_SPEED + " 이동속도 배수 §7:: §f" + character.getSpeedMultiplier(),
                "§6⬜ 히트박스 배수 §7:: §f" + character.getHitboxMultiplier()));
        guiController.set(5, character.getWeaponInfo().getStaticItem());

        guiController.set(20, SelectCharInfoItem.TRAIT.staticItem);
        for (int i = 21; i <= 24; i++) {
            TraitInfo traitInfo = character.getTraitInfo(i - 20);
            if (traitInfo == null)
                guiController.remove(i);
            else
                guiController.set(i, traitInfo.getStaticItem());
        }

        guiController.set(29, SelectCharInfoItem.PASSIVE_SKILL.staticItem);
        for (int i = 30; i <= 33; i++) {
            PassiveSkillInfo<?> passiveSkillInfo = character.getPassiveSkillInfo(i - 29);
            if (passiveSkillInfo == null)
                guiController.remove(i);
            else
                guiController.set(i, passiveSkillInfo.getStaticItem());
        }

        guiController.set(38, SelectCharInfoItem.ACTIVE_SKILL.staticItem);
        for (int i = 39; i <= 42; i++) {
            ActiveSkillInfo<?> activeSkillInfo = character.getActiveSkillInfo(i - 38);
            if (activeSkillInfo == null)
                guiController.remove(i);
            else
                guiController.set(i, activeSkillInfo.getStaticItem());
        }

        guiController.set(53, buttonLeft);
    }

    private enum SelectCharInfoItem {
        TRAIT("OWZjMzJjOTE0Mjc2ZjY4NjE0MTc5NmE1YTAyM2MzOWVmZGZlZDE0ZDNkN2M5YzQyNzkyNTEzODQ2ZjdmYTRiMyJ9fX0=",
                "§b§l특성 §7§oTrait"),
        PASSIVE_SKILL("NjVlZDYwMzVhNTEyYzYzZmY1MzEyNTczZjk1MTFiMzE0M2NlN2Q3YWFiYTIyMzQ1ZGZmNDM5NzM2ZDUxYzFjIn19fQ==",
                "§e§l패시브 스킬 §7§oPassive Skill"),
        ACTIVE_SKILL("NTFkMzgzNDAxZjc3YmVmZmNiOTk4YzJjZjc5YjdhZmVlMjNmMThjNDFkOGE1NmFmZmVkNzliYjU2ZTIyNjdhMyJ9fX0=",
                "§c§l액티브 스킬 §7§oActive Skill");

        /** 정적 아이템 객체 */
        private final StaticItem staticItem;

        SelectCharInfoItem(String skinUrl, String name) {
            this.staticItem = new StaticItem("PlayerOption" + this, new ItemBuilder(Material.SKULL_ITEM)
                    .setDamage((short) 3)
                    .setSkullOwner(SkinUtil.TOKEN_PREFIX + skinUrl)
                    .setName(name)
                    .build());
        }
    }
}
