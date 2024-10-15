package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Jumpable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusRestrictions;
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
 * 엔티티가 {@link LivingEntity}를 상속받는 클래스여야 한다.</p>
 *
 * @see Jumpable
 */
public final class JumpModule extends MoveModule {
    /**
     * 점프 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param speed        이동속도 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를
     *                                  상속받지 않으면 발생
     */
    public JumpModule(@NonNull Jumpable combatEntity, double speed) {
        super(combatEntity, speed);
        if (!(combatEntity.getEntity() instanceof LivingEntity))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 LivingEntity를 상속받지 않음");

        TaskUtil.addTask(combatEntity, new IntervalTask(i -> {
            if (canJump() && combatEntity.canJump()) {
                if (((LivingEntity) combatEntity.getEntity()).hasPotionEffect(PotionEffectType.JUMP) &&
                        ((LivingEntity) combatEntity.getEntity()).getPotionEffect(PotionEffectType.JUMP).getAmplifier() < 0)
                    ((LivingEntity) combatEntity.getEntity()).removePotionEffect(PotionEffectType.JUMP);
            } else
                ((LivingEntity) combatEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -6,
                        false, false), true);

            return true;
        }, 1));
    }

    /**
     * 점프 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public JumpModule(@NonNull Jumpable combatEntity) {
        this(combatEntity, (combatEntity.getEntity() instanceof Attributable) ?
                ((Attributable) combatEntity.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() : 0);
    }

    /**
     * 엔티티가 점프할 수 있는 기본 조건을 확인한다.
     *
     * @return 점프 가능 여부
     */
    private boolean canJump() {
        if (combatEntity instanceof Damageable
                && ((Damageable) combatEntity).getStatusEffectModule().hasAnyRestriction(StatusRestrictions.JUMP))
            return false;
        return combatEntity.getPropertyManager().getValue(Property.FREEZE) < JagerT1Info.NO_JUMP;
    }
}
