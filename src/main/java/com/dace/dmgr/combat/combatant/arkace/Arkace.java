package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Marksman;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;

/**
 * 전투원 - 아케이스 클래스.
 *
 * @see ArkaceWeapon
 * @see ArkaceP1
 * @see ArkaceA1
 * @see ArkaceA2
 * @see ArkaceUlt
 */
public final class Arkace extends Marksman {
    @Getter
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super(null, "아케이스", "슈퍼 솔저", "DVArkace", '\u32D0', 1, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "여기는 아케이스, 신속한 치료가 필요하다!";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "부상 발생, 치료를 요청한다!";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "아직은 더 버틸 수 있다.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "에너지 충전 중이다.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "에너지 충전이 얼마 남지 않았다.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "에너지 증폭이 준비되었다.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "여기는 아케이스, 지원을 요청한다.",
                "화력 지원 바란다."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "에너지 증폭 활성화.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case SILIA:
            case CHED:
                return new String[]{"그런 원시적인 무기로 뭘 하겠다고 그러나?"};
            case JAGER:
                return new String[]{
                        "총은 그렇게 쓰는 물건이 아니다.",
                        "끈질긴 놈이군."
                };
            default:
                return new String[]{
                        "똑바로 해라.",
                        "연습이 아닌 실전이다.",
                        "적을 사살했다."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "제법..이군..",
                "운수 한 번... 안 좋은 날이군...",
                "여기서... 끝인 건가..."
        };
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        if (combatUser.getEntity().isSprinting())
            combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }

    @Override
    @NonNull
    public ArkaceWeaponInfo getWeaponInfo() {
        return ArkaceWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{ArkaceP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{ArkaceA1Info.getInstance(), ArkaceA2Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public ArkaceUltInfo getUltimateSkillInfo() {
        return ArkaceUltInfo.getInstance();
    }
}
