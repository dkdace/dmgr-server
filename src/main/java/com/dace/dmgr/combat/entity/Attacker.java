package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.AttackModule;
import lombok.NonNull;
import org.bukkit.Location;

/**
 * 다른 엔티티를 공격할 수 있는 엔티티의 인터페이스.
 */
public interface Attacker extends CombatEntity {
    /**
     * @return 공격 모듈
     */
    @NonNull
    AttackModule getAttackModule();

    /**
     * 엔티티가 다른 엔티티를 공격했을 때 실행될 작업.
     *
     * @param victim 피격자
     * @param damage 피해량
     * @param isCrit 치명타 여부
     * @param isUlt  궁극기 충전 여부
     * @see Damageable#onDamage(Attacker, double, double, Location, boolean)
     */
    void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt);

    /**
     * 엔티티가 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim 피격자
     * @see Damageable#onDeath(Attacker)
     */
    void onKill(@NonNull Damageable victim);

    /**
     * 엔티티가 기본 공격을 했을 때 실행될 작업.
     *
     * @param victim 피격자
     */
    default void onDefaultAttack(@NonNull Damageable victim) {
        // 미사용
    }
}
