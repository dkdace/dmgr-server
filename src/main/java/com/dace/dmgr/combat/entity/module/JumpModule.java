package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Jumpable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 점프가 가능한 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Jumpable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link Attributable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Jumpable
 */
public final class JumpModule extends MoveModule {
    /**
     * 점프 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param speed        이동속도 기본값
     * @throws IllegalArgumentException 대상 엔티티가 {@link Attributable}을 상속받지 않으면 발생
     */
    public JumpModule(@NonNull Jumpable combatEntity, double speed) {
        super(combatEntity, speed);

        TaskUtil.addTask(combatEntity, new IntervalTask(i -> {
            if (combatEntity.canJump())
                ((LivingEntity) combatEntity.getEntity()).removePotionEffect(PotionEffectType.JUMP);
            else
                ((LivingEntity) combatEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999, -6,
                        false, false), true);

            return true;
        }, 1));
    }

    /**
     * 점프 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link Attributable}을 상속받지 않으면 발생
     */
    public JumpModule(@NonNull Jumpable combatEntity) {
        this(combatEntity, (combatEntity.getEntity() instanceof Attributable) ?
                ((Attributable) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() : 0);
    }
}
