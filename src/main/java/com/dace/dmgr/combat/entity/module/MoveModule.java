package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.AbilityStatus;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;

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
     * @param speed        이동속도 기본값
     * @throws IllegalArgumentException 대상 엔티티가 {@link Attributable}를 상속받지 않으면 발생
     */
    public MoveModule(@NonNull Movable combatEntity, double speed) {
        if (!(combatEntity.getEntity() instanceof Attributable))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 Attributable을 상속받지 않음");

        this.combatEntity = combatEntity;
        this.speedStatus = new AbilityStatus(speed);
        TaskUtil.addTask(combatEntity, new IntervalTask(i -> {
            speedStatus.addModifier("JagerT1", -combatEntity.getPropertyManager().getValue(Property.FREEZE));
            if (CooldownUtil.getCooldown(combatEntity, Cooldown.JAGER_FREEZE_VALUE_DURATION) == 0)
                combatEntity.getPropertyManager().setValue(Property.FREEZE, 0);

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
}
