package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class UltimateSkill extends ActiveSkill {
    /** 궁극기 준비 효과음 */
    private static final SoundEffect ULTIMATE_READY_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(0.5).pitch(2).build());
    /** 궁극기 사용 효과음 */
    private static final SoundEffect ULTIMATE_USE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(2).build());

    /** 필요 충전량 */
    protected final int cost;

    /**
     * 궁극기 스킬 인스턴스를 생성한다.
     *
     * @param combatUser        사용자 플레이어
     * @param ultimateSkillInfo 궁극기 정보 인스턴스
     * @param defaultDuration   기본 지속시간
     * @param cost              필요 충전량
     */
    protected UltimateSkill(@NonNull CombatUser combatUser, @NonNull UltimateSkillInfo<?> ultimateSkillInfo, @NonNull Timespan defaultDuration, int cost) {
        super(combatUser, ultimateSkillInfo, Timespan.MAX, defaultDuration, 3);
        this.cost = cost;
    }

    @Override
    @NonNull
    public UltimateSkillInfo<?> getSkillInfo() {
        return (UltimateSkillInfo<?>) super.getSkillInfo();
    }

    @Override
    @NonNull
    public final ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_4};
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

        CombatUtil.getCombatEntities(combatUser.getEntity().getWorld(), EntityCondition.of(CombatUser.class))
                .forEach(target -> combatUser.sendMentMessage(target, "§e" + combatUser.getCombatantType().getCombatant().getUltUseMent()));
    }
}
