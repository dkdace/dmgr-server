package com.dace.dmgr.util;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.character.jager.action.JagerA3Info;
import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 쿨타임 종류.
 */
@AllArgsConstructor
@Getter
public enum Cooldown {
    /** 채팅 */
    CHAT(GeneralConfig.getConfig().getChatCooldown()),
    /** 명령어 */
    COMMAND(GeneralConfig.getConfig().getCommandCooldown()),
    /** 피격 시 애니메이션 */
    DAMAGE_ANIMATION(6),
    /** 적 처치 기여 (데미지 누적) 제한시간 */
    DAMAGE_SUM_TIME_LIMIT(CombatUser.DAMAGE_SUM_TIME_LIMIT),
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 */
    FASTKILL_TIME_LIMIT(CombatUser.FASTKILL_TIME_LIMIT),
    /** 리스폰 시간 */
    RESPAWN_TIME(GeneralConfig.getCombatConfig().getRespawnTime()),
    /** 총기류의 초탄 반동 딜레이 */
    WEAPON_FIRST_RECOIL_DELAY(4),
    /** 연사가 가능한 총기류의 쿨타임 */
    WEAPON_FULLAUTO_COOLDOWN(4),
    /** 연사가 가능한 총기류의 탄퍼짐 회복 시간 */
    WEAPON_FULLAUTO_RECOVERY_DELAY(4),
    /** 재장전 */
    WEAPON_RELOAD(0),
    /** 달리기 금지 */
    NO_SPRINT(0),
    /** 전역 쿨타임 */
    GLOBAL_COOLDOWN(0),
    /** 스킬 쿨타임 */
    SKILL_COOLDOWN(0),
    /** 스킬 스택 충전 쿨타임 */
    SKILL_STACK_COOLDOWN(0),
    /** 스킬 지속시간 */
    SKILL_DURATION(0),
    /** 획득 점수 표시 유지시간 */
    SCORE_DISPLAY_DURATION(CombatUser.SCORE_DISPLAY_DURATION),
    /** 액션바 지속시간 */
    ACTION_BAR(0),
    /** 타이틀 지속시간 */
    TITLE(0),
    /** 상태 효과 지속시간 */
    STATUS_EFFECT(0),
    /** 2중 탄창 무기 교체 */
    WEAPON_SWAP(0),
    /** 예거 - 빙결 수치 지속시간 */
    JAGER_FREEZE_VALUE_DURATION(JagerT1Info.DURATION),
    /** 예거 - 빙결 수류탄 폭파 시간 */
    JAGER_EXPLODE_DURATION(JagerA3Info.EXPLODE_DURATION);

    /** 쿨타임 기본값 (tick) */
    private final long defaultValue;
}
