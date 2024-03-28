package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * 기본 근접 공격의 판정을 관리하는 클래스.
 */
public final class MeleeAttack extends Hitscan {
    /** 기본 판정 크기. (단위: 블록) */
    private static final double SIZE = 0.5;
    /** 사거리. (단위: 블록) */
    private static final double DISTANCE = 2;
    /** 피해량 */
    private final int damage;

    /**
     * 근접 공격 판정 인스턴스를 생성한다.
     *
     * @param shooter 발사자
     * @param damage  피해량
     */
    public MeleeAttack(@NonNull CombatUser shooter, int damage) {
        super(shooter, HitscanOption.builder().size(SIZE).maxDistance(DISTANCE).condition(shooter::isEnemy).build());
        this.damage = damage;
    }

    @Override
    protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
        SoundUtil.play(NamedSound.COMBAT_MELEE_ATTACK_HIT_BLOCK, location);
        SoundUtil.playBlockHitSound(location, hitBlock, 1);
        ParticleUtil.playBlockHitEffect(location, hitBlock, 1);

        return false;
    }

    @Override
    protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
        target.getDamageModule().damage((CombatUser) shooter, damage, DamageType.NORMAL, location, false, true);
        target.getKnockbackModule().knockback(velocity.clone().normalize().multiply(0.3));

        SoundUtil.play(NamedSound.COMBAT_MELEE_ATTACK_HIT_ENTITY, location);
        ParticleUtil.play(Particle.CRIT, location, 10, 0, 0, 0, 0.4);

        return false;
    }
}
