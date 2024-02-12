package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * 기본 근접 공격의 판정을 관리하는 클래스.
 */
public final class MeleeAttack extends Hitscan {
    /** 판정 반경 배수 */
    private static final double HITBOX_MULTIPLIER = 12;
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
        super(shooter, HitscanOption.builder().hitboxMultiplier(HITBOX_MULTIPLIER).maxDistance(DISTANCE).build());
        this.damage = damage;
    }

    @Override
    public void trail(@NonNull Location location) {
        // 미사용
    }

    @Override
    public boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
        playHitBlockEffect(location, hitBlock);
        return false;
    }

    @Override
    public boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
        target.getDamageModule().damage((CombatUser) shooter, damage, DamageType.NORMAL, false, true);

        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, location, 0.8, 1.1, 0.1);
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, location, 0.8, 1.1, 0.1);
        ParticleUtil.play(Particle.CRIT, location, 10, 0, 0, 0, 0.4);

        return false;
    }

    /**
     * 맞았을 때 효과를 재생한다.
     *
     * @param location 맞은 위치
     * @param hitBlock 맞은 블록
     */
    private void playHitBlockEffect(@NonNull Location location, @NonNull Block hitBlock) {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                6, 0.05, 0.05, 0.05, 0.1);
        ParticleUtil.play(Particle.TOWN_AURA, location, 20, 0.05, 0.05, 0.05, 0);
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_WEAK, location, 0.8, 0.9, 0.05);

        switch (ParticleUtil.getBlockTexture(hitBlock.getType())) {
            case GRASS:
                SoundUtil.play(Sound.BLOCK_GRASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case DIRT:
                SoundUtil.play(Sound.BLOCK_GRAVEL_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case STONE:
                SoundUtil.play(Sound.BLOCK_STONE_BREAK, location, 1, 0.9, 0.1);
                break;
            case METAL:
                SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, location, 0.5, 1.95, 0.1);
                SoundUtil.play("random.metalhit", location, 0.8, 1.95, 0.1);
                break;
            case WOOD:
                SoundUtil.play(Sound.BLOCK_WOOD_BREAK, location, 0.8, 0.8, 0.1);
                SoundUtil.play("random.stab", location, 0.8, 1.95, 0.1);
                break;
            case GLASS:
                SoundUtil.play(Sound.BLOCK_GLASS_BREAK, location, 0.8, 0.7, 0.1);
                break;
            case WOOL:
                SoundUtil.play(Sound.BLOCK_CLOTH_BREAK, location, 1, 0.8, 0.1);
                break;
        }
    }
}
