package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.combat.ability.skill.Skill;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link Skill}을 상속받는 스킬
 */
public abstract class SkillInfo<T extends Skill> extends AbilityInfo<T> {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;

    /**
     * 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass      스킬 클래스
     * @param name            이름
     * @param itemStack       설명 아이템
     * @param abilityInfoLore 능력 정보 설명
     */
    protected SkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull ItemStack itemStack,
                        @NonNull AbilityInfoLore abilityInfoLore) {
        super(skillClass, name, itemStack, abilityInfoLore);
    }
}
