package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Particle;
import org.bukkit.block.Block;

/**
 * 기본 근접 공격 동작 클래스.
 */
public final class MeleeAttackAction extends AbstractAction {
    /** 쿨타임 (tick) */
    private static final long COOLDOWN = 1 * 20;
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
        Validate.notNull(combatUser.getCharacterType());
        return super.canUse(actionKey) && combatUser.getCharacterType().getCharacter().canUseMeleeAttack(combatUser) && combatUser.isGlobalCooldownFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) COOLDOWN);
        setCooldown();

        SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_USE, combatUser.getEntity().getLocation());
        combatUser.getEntity().getInventory().setHeldItemSlot(8);

        TaskUtil.addTask(combatUser, new DelayTask(() -> {
            new MeleeAttack().shoot();

            combatUser.playMeleeAttackAnimation(-7, 18, true);
        }, 2));

        TaskUtil.addTask(combatUser, new DelayTask(() -> combatUser.getEntity().getInventory().setHeldItemSlot(4), 16));
    }

    private final class MeleeAttack extends Hitscan {
        private MeleeAttack() {
            super(combatUser, HitscanOption.builder().size(SIZE).maxDistance(DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_HIT_BLOCK, getLocation());
            CombatEffectUtil.playBlockHitSound(getLocation(), hitBlock, 1);
            CombatEffectUtil.playBlockHitEffect(getLocation(), hitBlock, 1);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage((CombatUser) shooter, DAMAGE, DamageType.NORMAL, getLocation(), false, true))
                target.getKnockbackModule().knockback(getVelocity().clone().normalize().multiply(KNOCKBACK));

            SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_HIT_ENTITY, getLocation());
            ParticleUtil.play(Particle.CRIT, getLocation(), 10, 0, 0, 0, 0.4);

            return false;
        }
    }
}
