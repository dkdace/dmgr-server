package com.dace.dmgr.combat.action;

import com.comphenix.packetwrapper.WrapperPlayClientArmAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEffect;
import com.comphenix.packetwrapper.WrapperPlayServerRemoveEntityEffect;
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
        setCooldown(20);

        playUseSound(combatUser.getEntity().getEyeLocation());
        sendPotionEffect();
        combatUser.getEntity().getInventory().setHeldItemSlot(8);

        sendPotionEffect();

        TaskUtil.addTask(combatUser, new DelayTask(() -> {
            new MeleeAttack(combatUser, DAMAGE).shoot();

            WrapperPlayClientArmAnimation packet = new WrapperPlayClientArmAnimation();
            packet.receivePacket(combatUser.getEntity());

            WrapperPlayServerAnimation packet2 = new WrapperPlayServerAnimation();
            packet2.setAnimation(0);
            packet2.setEntityID(combatUser.getEntity().getEntityId());
            packet2.broadcastPacket();
        }, 2));

        TaskUtil.addTask(combatUser, new DelayTask(() -> combatUser.getEntity().getInventory().setHeldItemSlot(4), 16));
    }

    /**
     * 플레이어에게 성급함 포션 효과를 적용한다.
     */
    private void sendPotionEffect() {
        combatUser.getEntity().removePotionEffect(PotionEffectType.FAST_DIGGING);
        combatUser.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                20, -7, false, false), true);

        WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect();
        packet.setEntityID(combatUser.getEntity().getEntityId());
        packet.setEffect(PotionEffectType.FAST_DIGGING);
        packet.broadcastPacket();

        WrapperPlayServerEntityEffect packet2 = new WrapperPlayServerEntityEffect();
        packet2.setEntityID(combatUser.getEntity().getEntityId());
        packet2.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
        packet2.setAmplifier((byte) -7);
        packet2.setDuration(20);
        packet2.setHideParticles(true);
        packet2.broadcastPacket();
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
