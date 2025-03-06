package com.dace.dmgr.combat.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.MainHand;

/**
 * 기본 근접 공격 동작 클래스.
 */
public final class MeleeAttackAction extends AbstractAction {
    /** 사용 효과음 */
    private static final SoundEffect USE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_SWEEP).volume(0.6).pitch(1.1).pitchVariance(0.1).build());
    /** 엔티티 타격 효과음 */
    private static final SoundEffect HIT_ENTITY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(1).pitch(1.1).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK).volume(1).pitch(1.1).pitchVariance(0.1).build()
    );
    /** 블록 타격 효과음 */
    private static final SoundEffect HIT_BLOCK_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_ATTACK_WEAK).volume(1).pitch(0.9).pitchVariance(0.05).build());
    /** 엔티티 타격 효과 */
    private static final ParticleEffect HIT_ENTITY_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).count(10).speed(0.4).build());

    /** 쿨타임 (tick) */
    private static final long COOLDOWN = 20;
    /** 피해량 */
    private static final int DAMAGE = 150;
    /** 사거리 (단위: 블록) */
    private static final double DISTANCE = 2;
    /** 판정 크기 (단위: 블록) */
    private static final double SIZE = 0.5;
    /** 넉백 강도 */
    private static final double KNOCKBACK = 0.3;

    /**
     * 근접 공격 동작 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     */
    public MeleeAttackAction(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SWAP_HAND};
    }

    @Override
    public long getDefaultCooldown() {
        return COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey)
                && combatUser.getCharacterType().getCharacter().canUseMeleeAttack(combatUser)
                && !combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.MELEE_ATTACK)
                && combatUser.isGlobalCooldownFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(Timespan.ofTicks(COOLDOWN));
        setCooldown();

        USE_SOUND.play(combatUser.getLocation());
        combatUser.getEntity().getInventory().setHeldItemSlot(8);

        combatUser.addTask(new DelayTask(() -> {
            new MeleeAttack().shot();

            combatUser.playMeleeAttackAnimation(-7, Timespan.ofTicks(18), MainHand.RIGHT);
        }, 2));

        combatUser.addTask(new DelayTask(() -> combatUser.getEntity().getInventory().setHeldItemSlot(4), 16));
    }

    private final class MeleeAttack extends Hitscan<Damageable> {
        private MeleeAttack() {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser), Option.builder().size(SIZE).maxDistance(DISTANCE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> true;
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                HIT_BLOCK_SOUND.play(location);
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 1);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(combatUser, DAMAGE, DamageType.NORMAL, location, false, true) && target instanceof Movable)
                    ((Movable) target).getMoveModule().knockback(getVelocity().normalize().multiply(KNOCKBACK));

                HIT_ENTITY_SOUND.play(location);
                HIT_ENTITY_PARTICLE.play(location);

                return false;
            };
        }
    }
}
