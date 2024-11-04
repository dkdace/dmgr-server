package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 액티브 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link ActiveSkill}을 상속받는 액티브 스킬
 */
public abstract class ActiveSkillInfo<T extends ActiveSkill> extends SkillInfo<T> {
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§c§l[액티브 스킬] §4";

    /**
     * 액티브 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass     액티브 스킬 클래스
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected ActiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        super(skillClass, name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 14)
                .setLore(actionInfoLore.toString())
                .build());
    }

    /**
     * @see SkillInfo#SkillInfo(Class, String, ItemStack)
     */
    ActiveSkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull ItemStack itemStack) {
        super(skillClass, name, itemStack);
    }

    @Override
    public String toString() {
        return "§c［" + name + "］";
    }
}
