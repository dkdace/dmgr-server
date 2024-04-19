package com.dace.dmgr.combat.action.info;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 동작(무기, 패시브 스킬, 액티브 스킬 등) 정보를 관리하는 클래스.
 *
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionInfo {
    /** 이름 */
    @Getter
    protected final String name;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;

    /**
     * 동작의 설명 아이템 객체를 반환한다.
     *
     * @return 설명 아이템 객체
     */
    @NonNull
    public final ItemStack getItemStack() {
        return itemStack.clone();
    }
}
