package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
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
     * @return 상태 효과 모듈
     */
    @NonNull
    StatusEffectModule getStatusEffectModule();

    /**
     * 엔티티가 살아있는 생명체인지 확인한다.
     *
     * @return 살아있는 생명체이면 {@code true} 반환
     */
    boolean isCreature();

    /**
     * 죽었을 때 공격자(플레이어)에게 주는 점수를 반환한다.
     *
     * @return 죽었을 때 공격자에게 주는 점수. 0 이상의 값
     * @implSpec 0
     */
    default double getScore() {
        return 0;
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업.
     *
     * @param attacker      공격자
     * @param damage        피해량
     * @param reducedDamage 방어력에 의해 경감된 피해량
     * @param location      맞은 위치
     * @param isCrit        치명타 여부
     * @see Attacker#onAttack(Damageable, double, boolean, boolean)
     */
    void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit);

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see Attacker#onKill(Damageable)
     */
    void onDeath(@Nullable Attacker attacker);
}
