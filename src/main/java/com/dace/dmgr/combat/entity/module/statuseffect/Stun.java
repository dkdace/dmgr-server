package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * 기절 상태 효과를 처리하는 클래스.
 */
public class Stun extends StatusEffect {
    /** 공격자 */
    @Nullable
    private final CombatUser attacker;

    /**
     * 기절 상태 효과 인스턴스를 생성한다.
     *
     * @param attacker 공격자
     */
    public Stun(@Nullable CombatUser attacker) {
        super(StatusEffectType.STUN, false);
        this.attacker = attacker;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getActionManager().cancelAction(attacker);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, long i) {
        if (combatEntity instanceof CombatUser) {
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l기절함!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
            ((CombatUser) combatEntity).setYawAndPitch(combatEntity.getLocation().getYaw(), combatEntity.getLocation().getPitch());
        }
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.DEFAULT_MOVE, CombatRestriction.ACTION_MOVE, CombatRestriction.USE_ACTION);
    }
}
