package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;

/**
 * 스킬 정보를 관리하는 클래스.
 */
public abstract class SkillInfo extends ActionInfo {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[스킬] §c";
    /** 번호 */
    @Getter
    private final int number;

    protected SkillInfo(int number, String name, String... lore) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 15)
                .setLore(lore)
                .build());
        this.number = number;
    }

    /**
     * 스킬 인스턴스를 생성하여 반환한다.
     *
     * @param combatUser 플레이어 객체
     * @return 스킬 객체
     */
    public abstract Skill createSkill(CombatUser combatUser);
}
