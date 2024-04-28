package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 스킬 정보를 관리하는 클래스.
 */
@Getter
public abstract class SkillInfo extends ActionInfo {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[스킬] §c";
    /** 스킬 번호 */
    private final int number;

    /**
     * 스킬 정보 인스턴스를 생성한다.
     *
     * @param number 스킬 번호
     * @param name   이름
     * @param lores  설명 목록
     */
    protected SkillInfo(int number, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 15)
                .setLore(lores)
                .build());
        this.number = number;
    }

    /**
     * 스킬 인스턴스를 생성하여 반환한다.
     *
     * @param combatUser 플레이어 객체
     * @return 스킬 객체
     */
    @NonNull
    public abstract Skill createSkill(@NonNull CombatUser combatUser);
}
