package com.dace.dmgr.combat.entity;

import lombok.NonNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 전투 중 상태 효과 등으로 제한되는 행동의 종류.
 */
public enum CombatRestriction {
    /** 행동 제한 없음 */
    NONE(),
    /** 피해 입기 */
    DAMAGED,
    /** 치료 받기 */
    HEALED,
    /** 사운드 듣기 */
    HEAR,
    /** 기본 이동 */
    DEFAULT_MOVE,
    /** 달리기 */
    SPRINT,
    /** 점프 */
    JUMP,
    /** 스스로 밀음 */
    PUSH,
    /** 텔레포트함 */
    TELEPORT,
    /** 비행 */
    FLY,
    /** 무기 사용 */
    USE_WEAPON(),
    /** 스킬 사용 */
    USE_SKILL,
    /** 기본 근접 공격 */
    MELEE_ATTACK,
    /** 이동기 */
    ACTION_MOVE(SPRINT, JUMP, PUSH, TELEPORT, FLY),
    /** 행동하기 */
    USE_ACTION(USE_WEAPON, USE_SKILL, MELEE_ATTACK);

    private final CombatRestriction[] restrictionValues;

    CombatRestriction() {
        this.restrictionValues = new CombatRestriction[]{this};
    }

    CombatRestriction(@NonNull CombatRestriction @NonNull ... restrictionValues) {
        this.restrictionValues = restrictionValues;
    }

    /**
     * 현재 항목이 제한하는 행동을 반환한다.
     *
     * @return 제한하는 행동 목록
     */
    @NonNull
    @UnmodifiableView
    public Set<@NonNull CombatRestriction> restrictionValues() {
        EnumSet<CombatRestriction> combatRestrictions = EnumSet.noneOf(CombatRestriction.class);
        combatRestrictions.addAll(Arrays.asList(restrictionValues));

        return Collections.unmodifiableSet(combatRestrictions);
    }
}