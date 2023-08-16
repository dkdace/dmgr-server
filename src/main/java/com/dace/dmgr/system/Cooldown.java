package com.dace.dmgr.system;

import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.config.GeneralConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 쿨타임 종류.
 */
@AllArgsConstructor
@Getter
public enum Cooldown {
    /** 채팅 */
    CHAT(GeneralConfig.getInstance().getChatCooldown()),
    /** 명령어 */
    COMMAND(GeneralConfig.getInstance().getCommandCooldown()),
    /** 피격 시 애니메이션 */
    DAMAGE_ANIMATION(6),
    /** 적 처치 기여 (데미지 누적) 제한시간 */
    DAMAGE_SUM_TIME_LIMIT(CombatUser.DAMAGE_SUM_TIME_LIMIT),
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 */
    FASTKILL_TIME_LIMIT(CombatUser.FASTKILL_TIME_LIMIT),
    /** 리스폰 시간 */
    RESPAWN_TIME(CombatUser.RESPAWN_TIME),
    /** 총기류의 초탄 반동 딜레이 */
    WEAPON_FIRST_RECOIL_DELAY(4),
    /** 재장전 */
    WEAPON_RELOAD(0),
    /** 달리기 금지 */
    NO_SPRINT(0),
    /** 스킬 쿨타임 */
    SKILL_COOLDOWN(0),
    /** 스킬 스택 충전 쿨타임 */
    SKILL_STACK_COOLDOWN(0),
    /** 스킬 지속시간 */
    SKILL_DURATION(0),
    /** 액션바 지속시간 */
    ACTION_BAR(0),
    /** 기절 지속시간 */
    STUN(0),
    /** 속박 지속시간 */
    SNARE(0),
    /** 고정 지속시간 */
    GROUNDING(0),
    /** 침묵 지속시간 */
    SILENCE(0),
    /** 화염 지속시간 */
    BURN(0),
    /** 2중 탄창 무기 교체 */
    WEAPON_SWAP(0),
    /** 빙결 수치 지속시간 */
    FREEZE_VALUE_DURATION(JagerT1Info.DURATION);

    /** 쿨타임 기본값 */
    private final long defaultValue;
}
