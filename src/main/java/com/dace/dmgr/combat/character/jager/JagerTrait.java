package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 전투원 - 예거 특성 클래스.
 */
@UtilityClass
public final class JagerTrait {
    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     */
    public static void addFreezeValue(CombatEntity victim, int amount) {
        victim.getPropertyManager().addValue(Property.FREEZE, amount);
        CooldownUtil.setCooldown(victim, Cooldown.JAGER_FREEZE_VALUE_DURATION);
    }

    /**
     * 빙결 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Freeze extends Snare {
        @Getter
        private static final Freeze instance = new Freeze();

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, long i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l얼어붙음!", "", 0, 2, 10);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                    combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() / 2, 0), 5,
                    0.4F, 0.8F, 0.4F, 120, 220, 240);
        }
    }
}
