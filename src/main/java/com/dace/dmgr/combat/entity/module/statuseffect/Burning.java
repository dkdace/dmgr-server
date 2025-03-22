package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.effect.SoundEffect;
import lombok.NonNull;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 화염 상태 효과를 처리하는 클래스.
 */
public class Burning extends StatusEffect {
    /** 화염 피해 효과음 */
    private static final SoundEffect BURNING_DAMAGE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT_ON_FIRE).volume(0.7).pitch(1).pitchVariance(0.1).build());

    /** 초당 피해량 */
    private final double damagePerSecond;
    /** 궁극기 제공 여부 */
    private final boolean isUlt;

    /**
     * 화염 상태 효과 인스턴스를 생성한다.
     *
     * @param damagePerSecond 초당 피해량
     * @param isUlt           궁극기 제공 여부
     */
    protected Burning(double damagePerSecond, boolean isUlt) {
        super(StatusEffectType.BURNING, false);

        this.damagePerSecond = damagePerSecond;
        this.isUlt = isUlt;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        combatEntity.getEntity().setFireTicks(4);

        if (i % 10 == 0 && provider instanceof Attacker && combatEntity.getDamageModule().damage((Attacker) provider,
                damagePerSecond * 10 / 20.0, DamageType.NORMAL, null, false, isUlt))
            BURNING_DAMAGE_SOUND.play(combatEntity.getLocation());
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
