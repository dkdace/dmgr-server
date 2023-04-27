package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;

/**
 * 스킬 정보를 관리하는 클래스.
 */
public abstract class Skill extends Action {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[스킬] §c";
    /** 번호 */
    @Getter
    private final int number;

    public Skill(int number, String name, String... lore) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 15)
                .setLore(lore)
                .build());
        this.number = number;
    }

    /**
     * 스킬의 쿨타임을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    public abstract long getCooldown();

    /**
     * 스킬 사용 이벤트를 호출한다.
     *
     * @param combatUser      호출한 플레이어
     * @param skillController 스킬 컨트롤러 객체
     * @param actionKey       상호작용 키
     */
    public abstract void use(CombatUser combatUser, SkillController skillController, ActionKey actionKey);
}
