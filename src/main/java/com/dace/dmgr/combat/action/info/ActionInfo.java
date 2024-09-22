package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.item.StaticItem;
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
    /** 설명 정적 아이템 객체 */
    @Getter
    protected final StaticItem staticItem;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;

    /**
     * 동작 정보 인스턴스를 생성한다.
     *
     * @param name       이름
     * @param staticItem 설명 정적 아이템 객체
     */
    protected ActionInfo(@NonNull String name, @NonNull StaticItem staticItem) {
        this.name = name;
        this.staticItem = staticItem;
        this.itemStack = staticItem.getItemStack();
    }
}
