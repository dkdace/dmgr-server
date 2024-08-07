package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.interaction.DamageType;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 생명력 수치를 조정하고 피해를 입을 수 있는 엔티티의 인터페이스.
 */
public interface Damageable extends CombatEntity {
    /**
     * @return 피해 모듈
     */
    @NonNull
    DamageModule getDamageModule();

    /**
     * @return 넉백 모듈
     */
    @NonNull
    KnockbackModule getKnockbackModule();

    /**
     * @return 상태 효과 모듈
     */
    @NonNull
    StatusEffectModule getStatusEffectModule();

    /**
     * 엔티티가 피해를 받을 수 있는 지 확인한다.
     *
     * @return 피해를 받을 수 있으면 {@code true} 반환
     * @implSpec {@code true}
     */
    default boolean canTakeDamage() {
        return true;
    }

    /**
     * 엔티티가 죽을 수 있는 지 확인한다.
     *
     * @return 죽을 수 있으면 {@code true} 반환
     * @implSpec {@code true}
     */
    default boolean canDie() {
        return true;
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업.
     *
     * @param attacker      공격자
     * @param damage        피해량
     * @param reducedDamage 방어력에 의해 경감된 피해량
     * @param damageType    타입
     * @param location      맞은 위치
     * @param isCrit        치명타 여부
     * @param isUlt         궁극기 충전 여부
     * @see Attacker#onAttack(Damageable, int, DamageType, boolean, boolean)
     */
    void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                  boolean isCrit, boolean isUlt);

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see Attacker#onKill(Damageable)
     */
    void onDeath(@Nullable Attacker attacker);
}
