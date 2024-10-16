package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.statuseffect.CombatRestrictions;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 움직일 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Movable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link Attributable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Movable
 */
@Getter
public class MoveModule {
    /** 엔티티 객체 */
    @NonNull
    protected final Movable combatEntity;
    /** 이동속도 값 */
    @NonNull
    private final AbilityStatus speedStatus;

    /**
     * 이동 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param speed        이동속도 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link Attributable}를
     *                                  상속받지 않으면 발생
     */
    public MoveModule(@NonNull Movable combatEntity, double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("'speed'가 0 이상이어야 함");
        if (!(combatEntity.getEntity() instanceof Attributable))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 Attributable을 상속받지 않음");

        this.combatEntity = combatEntity;
        this.speedStatus = new AbilityStatus(speed);

        TaskUtil.addTask(combatEntity, new IntervalTask(i -> {
            double movementSpeed = speedStatus.getValue();
            if (!canMove() || !combatEntity.canMove())
                movementSpeed = 0.0001;
            ((Attributable) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(movementSpeed);

            return true;
        }, 1));
    }

    /**
     * 이동 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link Attributable}를 상속받지 않으면 발생
     */
    public MoveModule(@NonNull Movable combatEntity) {
        this(combatEntity, (combatEntity.getEntity() instanceof Attributable) ?
                ((Attributable) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() : 0);
    }

    /**
     * 엔티티가 움직일 수 있는 기본 조건을 확인한다.
     *
     * @return 이동 가능 여부
     */
    private boolean canMove() {
        return combatEntity instanceof Damageable
                && !((Damageable) combatEntity).getStatusEffectModule().hasAllRestriction(CombatRestrictions.DEFAULT_MOVE);
    }

    /**
     * 엔티티를 지정한 속도로 밀어낸다. (이동기).
     *
     * @param velocity 속도
     * @param isReset  초기화 여부. {@code true}로 지정 시 기존 속도 초기화.
     */
    public final void push(@NonNull Vector velocity, boolean isReset) {
        if (combatEntity instanceof Damageable
                && !((Damageable) combatEntity).getKnockbackModule().isKnockbacked()
                && !((Damageable) combatEntity).getStatusEffectModule().hasAnyRestriction(CombatRestrictions.PUSH))
            combatEntity.getEntity().setVelocity(isReset ? velocity : combatEntity.getEntity().getVelocity().add(velocity));
    }

    /**
     * 엔티티를 지정한 속도로 밀어낸다. (이동기).
     *
     * @param velocity 속도
     */
    public final void push(@NonNull Vector velocity) {
        push(velocity, false);
    }

    /**
     * 엔티티를 지정한 위치로 순간이동 시킨다.
     *
     * @param location 이동할 위치
     */
    public final void teleport(@NonNull Location location) {
        if (combatEntity instanceof Damageable
                && ((Damageable) combatEntity).getStatusEffectModule().hasAnyRestriction(CombatRestrictions.TELEPORT))
            return;

        if (combatEntity.getEntity() instanceof Player) {
            User user = User.fromPlayer(combatEntity.getEntity());
            user.teleport(location);
        } else
            combatEntity.getEntity().teleport(location);
    }
}
