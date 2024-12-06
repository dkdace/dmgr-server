package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundEffect;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.block.Block;

/**
 * 실탄을 발사하는 화기의 총알을 관리하는 클래스.
 */
public abstract class GunHitscan extends Hitscan {
    /** 블록 타격 효과음 */
    private static final SoundEffect HIT_BLOCK_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("random.gun.ricochet").volume(0.8).pitch(0.975).pitchVariance(0.05).build());

    /**
     * @see Hitscan#Hitscan(CombatEntity, HitscanOption)
     */
    protected GunHitscan(@NonNull CombatEntity shooter, @NonNull HitscanOption option) {
        super(shooter, option);
    }

    /**
     * @see Hitscan#Hitscan(CombatEntity)
     */
    protected GunHitscan(@NonNull CombatEntity shooter) {
        super(shooter);
    }

    @Override
    protected boolean onHitBlock(@NonNull Block hitBlock) {
        HIT_BLOCK_SOUND.play(getLocation());
        CombatEffectUtil.playBlockHitSound(getLocation(), hitBlock, 1);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), getLocation(),
                3, 0, 0, 0, 0.1);
        ParticleUtil.play(Particle.TOWN_AURA, getLocation(), 10, 0, 0, 0, 0);

        return false;
    }
}
