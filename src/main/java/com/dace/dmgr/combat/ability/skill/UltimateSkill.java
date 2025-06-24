package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.info.UltimateSkillInfo;
import com.dace.dmgr.combat.entity.CombatEntityRegistry;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.EnumSet;
import java.util.Set;

/**
 * 궁극기 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class UltimateSkill extends ActiveSkill {
    /** 궁극기 준비 효과음 */
    private static final SoundEffect ULTIMATE_READY_SOUND = SoundEffect.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(0.5).pitch(2).build();
    /** 궁극기 사용 효과음 */
    private static final SoundEffect ULTIMATE_USE_SOUND = SoundEffect.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(2).build();

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
    protected UltimateSkill(@NonNull CombatUser combatUser, @NonNull UltimateSkillInfo<?> ultimateSkillInfo, @NonNull Timespan defaultDuration,
                            int cost) {
        super(combatUser, ultimateSkillInfo, Timespan.MAX, defaultDuration, 3);
        this.cost = cost;
    }

    @Override
    @NonNull
    protected ChatColor getDisplayNameColor() {
        return ChatColor.LIGHT_PURPLE;
    }

    @Override
    @NonNull
    public final Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_4);
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

        CombatEntityRegistry.getCombatEntities(combatUser.getEntity().getWorld(), EntityCondition.of(CombatUser.class))
                .forEach(target -> combatUser.sendMentMessage(target, "§e" + combatUser.getCombatantType().getCombatant().getUltUseMent()));
    }
}
