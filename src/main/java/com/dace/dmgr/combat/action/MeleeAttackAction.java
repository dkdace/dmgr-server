package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.MeleeAttack;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;

/**
 * 기본 근접 공격 동작 클래스.
 */
public final class MeleeAttackAction extends AbstractAction {
    /** 피해량 */
    private static final int DAMAGE = 150;

    /**
     * 근접 공격 동작 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     */
    public MeleeAttackAction(@NonNull CombatUser combatUser) {
        super(combatUser, null);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SWAP_HAND};
    }

    @Override
    public long getDefaultCooldown() {
        return 20;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.getCharacterType().getCharacter().canUseMeleeAttack(combatUser) && combatUser.isGlobalCooldownFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(20);
        setCooldown();

        SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_USE, combatUser.getEntity().getLocation());
        combatUser.getEntity().getInventory().setHeldItemSlot(8);

        TaskUtil.addTask(combatUser, new DelayTask(() -> {
            new MeleeAttack(combatUser, DAMAGE).shoot();

            combatUser.playMeleeAttackAnimation(-7, 18, true);
        }, 2));

        TaskUtil.addTask(combatUser, new DelayTask(() -> combatUser.getEntity().getInventory().setHeldItemSlot(4), 16));
    }
}
