package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * 무기 정보를 관리하는 클래스.
 */
public abstract class Weapon extends Action {
    /** 무기 아이템 타입 */
    public final static Material MATERIAL = Material.DIAMOND_HOE;
    /** 무기 이름의 접두사 */
    private static final String PREFIX = "§e§l[기본무기] §f";

    public Weapon(String name, ItemStack itemStack) {
        super(name, itemStack);
    }

    /**
     * 무기의 쿨타임을 반환한다.
     *
     * @return 쿨타임
     */
    public abstract long getCooldown();

    /**
     * 무기 사용 이벤트를 호출한다.
     *
     * @param combatUser       호출한 플레이어
     * @param weaponController 무기 컨트롤러 객체
     * @param actionKey        상호작용 키
     */
    public abstract void use(CombatUser combatUser, WeaponController weaponController, ActionKey actionKey);
}
