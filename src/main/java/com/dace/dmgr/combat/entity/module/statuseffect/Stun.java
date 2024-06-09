package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 기절 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stun implements StatusEffect {
    @Getter
    static final Stun instance = new Stun();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.STUN;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        if (!(combatEntity instanceof CombatUser))
            return;

        if (provider instanceof CombatUser && !((CombatUser) combatEntity).isDead() &&
                ((CombatUser) combatEntity).getSkill(((CombatUser) combatEntity).getCharacterType().getCharacter().getUltimateSkillInfo()).isCancellable())
            ((CombatUser) provider).addScore("궁극기 차단", CombatUser.ULT_BLOCK_KILL_SCORE);
        ((CombatUser) combatEntity).cancelAction();
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l기절함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
