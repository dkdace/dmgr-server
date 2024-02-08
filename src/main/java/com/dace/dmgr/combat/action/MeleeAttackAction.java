package com.dace.dmgr.combat.action;

import com.comphenix.packetwrapper.WrapperPlayClientArmAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.MeleeAttack;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

        playUseSound(combatUser.getEntity().getEyeLocation());
        combatUser.getEntity().removePotionEffect(PotionEffectType.FAST_DIGGING);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,
                19, 3, false, false), true);
        combatUser.getEntity().getInventory().setHeldItemSlot(8);

        TaskUtil.addTask(combatUser, new DelayTask(() -> {
            new MeleeAttack(combatUser, DAMAGE).shoot();

            WrapperPlayClientArmAnimation packet = new WrapperPlayClientArmAnimation();
            packet.receivePacket(combatUser.getEntity());

            WrapperPlayServerAnimation packet2 = new WrapperPlayServerAnimation();
            packet2.setEntityID(combatUser.getEntity().getEntityId());
            packet2.setAnimation(0);
            packet2.broadcastPacket();
        }, 2));

        TaskUtil.addTask(combatUser, new DelayTask(() -> combatUser.getEntity().getInventory().setHeldItemSlot(4), 16));
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_SWEEP, location, 0.6, 1.1, 0.1);
    }
}
