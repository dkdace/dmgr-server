package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Scuffler;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;

/**
 * 전투원 - 인페르노 클래스.
 *
 * @see MagrittaWeapon
 * @see MagrittaP1
 * @see MagrittaA1
 * @see MagrittaA2
 * @see MagrittaUlt
 */
public final class Magritta extends Scuffler {
    @Getter
    private static final Magritta instance = new Magritta();

    private Magritta() {
        super(null, "마그리타", "방화광", "DVMagrita", '\u32D8', 2, 1200, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "의사놈들 어딨어! 당장 튀어나와!";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "꽤 아프네, 여기 치료해주는 녀석은 없어?";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "치료 좀 해야겠어. 의사 없어?";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "지금 예열 중이야. 어때, 재밌겠지?";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "곧 화려한 불꽃놀이를 보여주지!";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "불꽃놀이, 포화 준비 완료!";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "여기 와봐, 불구경 시켜줄게.",
                "다들 이리로 모여봐."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "화끈하게 쓸어보자고!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case MAGRITTA:
                return new String[]{"어라? 나잖아?"};
            case METAR:
                return new String[]{"고철은 역시 고철이군."};
            default:
                return new String[]{
                        "여긴 마그리타, 하나 처리했어.",
                        "나중에 내가 직접 화장해주지."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case MAGRITTA:
                return new String[]{"넌 뭐야!"};
            case CHED:
                return new String[]{"불이야!"};
            default:
                return new String[]{
                        "아직 다 못 불태웠는데...",
                        "이렇게 뒤질 바에 불타는 게 낫겠어..."
                };
        }
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0)
            combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        ActionManager actionManager = combatUser.getActionManager();
        return actionManager.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getActionManager().getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getActionManager().getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public MagrittaWeaponInfo getWeaponInfo() {
        return MagrittaWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[]{MagrittaT1Info.getInstance()};
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{MagrittaP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{MagrittaA1Info.getInstance(), MagrittaA2Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public MagrittaUltInfo getUltimateSkillInfo() {
        return MagrittaUltInfo.getInstance();
    }
}
