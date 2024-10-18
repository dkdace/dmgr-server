package com.dace.dmgr.combat.character.delta;

import com.dace.dmgr.combat.action.info.*;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Controller;
import com.dace.dmgr.combat.character.delta.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 델타 클래스
 *
 *
 */
public final class Delta extends Controller {
    @Getter
    private static final Delta instance = new Delta();

    private Delta() {
        super(null, "델타", "데이드리머", "DVDelta", '\u32DA', 5, 1024, 1.0, 1.0);
    }

    @Override
    public @NonNull String @NonNull [] getReqHealMent() {
        return new String[] {
                "데이터 손상이 심각합니다. 즉시 백업을...",
                "배드 섹터 발생... 가급적 빨리 수리해야겠군요.",
                "딜레이 발생. 조금 손상을 입은 모양이네요."
        };
    }

    @Override
    public @NonNull String @NonNull [] getUltStateMent() {
        return new String[] {
                "프로그램 0의 암호를 푸는 중이에요.",
                "암호 해독이 끝나기까지 얼마 안 남았어요.",
                "암호 해독 완료. 실행 가능합니다!"
        };
    }

    @Override
    public @NonNull String @NonNull [] getReqRallyMent() {
        return new String[] {
                "상황 파악 완료, 모두 제 지시를 따라주세요!",
                "여기는 델타, 지원을 요청합니다!",
                "모두, 이 쪽으로 모여주세요!"
        };
    }

    @Override
    public @NonNull String getUltUseMent() {
        return "프로그램 0, 연산 개시. Divide-by-ze̵̓r̵̊.̷̝̘̽.̶͉̐̿.̸";
    }

    @Override
    public @NonNull String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            // TODO: No.7, METAR 추가 시 주석 해제
//             case NO_7:
//                    return new String[] {
//                            "훗, 당신과의 교전은 학습할 가치조차 없군요.",
//                            "You Are Loser. 분명 그런 뜻이었던가요?"
//                    };
//             case METAR:
//                 return new String[] {"훗, 당신과의 교전은 학습할 가치조차 없군요."};
            case DELTA:
                return new String[] {"프로젝트의 카피 모델을 제거했어요. 구식이네요."};
            default:
                return new String[] {
                        "보셨나요? 기계의 승리입니다!",
                        "학습된 데이터는 틀릴 리가 없죠.",
                        "playerKill 변수 증가. 수고하셨습니다."
                };
        }
    }

    @Override
    public @NonNull String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            // TODO: No.7, METAR 추가 시 주석 해제
//            case NO_7:
//            case METAR:
//                return new String[] {"이런 하드웨어만 단단한 깡통들이..."};
            default:
                return new String[] {
                        "페이탈 에러... 명령을 수행할 수 없습니다...",
                        "패인 분석 완료. 다음엔 지지 않습니다!",
                        "두고 보시죠. 저는 계속 학습하고 있습니다."
                };
        }
    }

    @Override
    public @NonNull String getActionbarString(@NonNull CombatUser combatUser) {
        StringJoiner text = new StringJoiner("    ");

        // TODO

        return text.toString();
    }

    @Override
    public @NonNull DeltaWeaponInfo getWeaponInfo() {
        return DeltaWeaponInfo.getInstance();
    }

    @Override
    public @Nullable PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return DeltaP1Info.getInstance();
            case 2:
                return DeltaP2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public @Nullable ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        return null;
    }

    @Override
    public @NonNull UltimateSkillInfo<? extends UltimateSkill> getUltimateSkillInfo() {
        return null;
    }


    @Override
    public @Nullable TraitInfo getCharacterTraitInfo(int number) {
        switch (number) {
            case 1:
                return DeltaT1Info.getInstance();
            default:
                return null;
        }
    }
}
