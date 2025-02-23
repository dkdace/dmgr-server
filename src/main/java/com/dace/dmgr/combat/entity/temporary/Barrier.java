package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.GameUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 플레이어가 소환할 수 있는 방벽 클래스.
 *
 * <p>방벽으로 받은 피해는 게임 진행 시 막은 피해({@link GameUser#getDefend()})로 취급한다.</p>
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 */
@Getter
public abstract class Barrier<T extends Entity> extends SummonEntity<T> implements Damageable {
    /** 넉백 모듈 */
    @NonNull
    protected final KnockbackModule knockbackModule;
    /** 상태 효과 모듈 */
    @NonNull
    protected final StatusEffectModule statusEffectModule;
    /** 피해 모듈 */
    @NonNull
    protected final DamageModule damageModule;

    /**
     * 방벽 인스턴스를 생성한다.
     *
     * @param entity    대상 엔티티
     * @param name      이름
     * @param owner     엔티티를 소환한 플레이어
     * @param score     죽었을 때 공격자에게 주는 점수
     * @param maxHealth 최대 체력
     * @param hitboxes  히트박스 목록
     * @throws IllegalStateException 해당 {@code entity}의 CombatEntity가 이미 존재하면 발생
     */
    protected Barrier(@NonNull T entity, @NonNull String name, @NonNull CombatUser owner, int score, int maxHealth, @NonNull Hitbox @NonNull ... hitboxes) {
        super(entity, name, owner, false, false, hitboxes);

        knockbackModule = new KnockbackModule(this, 2);
        statusEffectModule = new StatusEffectModule(this);
        damageModule = new DamageModule(this, false, false, false, score, maxHealth);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                         boolean isCrit, boolean isUlt) {
        if (owner.getGameUser() != null)
            owner.getGameUser().addDefend(damage);
    }
}
