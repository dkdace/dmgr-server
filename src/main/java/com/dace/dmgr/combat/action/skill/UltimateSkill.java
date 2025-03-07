package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.game.GameUser;
import lombok.NonNull;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
public abstract class UltimateSkill extends ActiveSkill {
    /** 궁극기 준비 효과음 */
    private static final SoundEffect ULTIMATE_READY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(0.5).pitch(2).build());
    /** 궁극기 사용 효과음 */
    private static final SoundEffect ULTIMATE_USE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(2).build());

    /**
     * 궁극기 스킬 인스턴스를 생성한다.
     *
     * @param combatUser        대상 플레이어
     * @param ultimateSkillInfo 궁극기 정보 객체
     */
    protected UltimateSkill(@NonNull CombatUser combatUser, @NonNull UltimateSkillInfo<? extends UltimateSkill> ultimateSkillInfo) {
        super(combatUser, ultimateSkillInfo, 3);
    }

    @Override
    @NonNull
    public final ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_4};
    }

    @Override
    public final long getDefaultCooldown() {
        return -1;
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        ULTIMATE_READY_SOUND.play(combatUser.getEntity());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.setUltGaugePercent(0);
        ULTIMATE_USE_SOUND.play(combatUser.getLocation());

        GameUser gameUser = combatUser.getGameUser();
        if (gameUser != null)
            gameUser.broadcastChatMessage("§l" + combatUser.getCombatantType().getCombatant().getUltUseMent(), false);
    }

    /**
     * 필요 충전량을 반환한다.
     *
     * @return 필요 충전량
     */
    public abstract int getCost();
}
