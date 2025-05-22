package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Role;
import com.dace.dmgr.combat.combatant.Support;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Target;
import lombok.Getter;
import lombok.NonNull;

/**
 * 전투원 - 팔라스 클래스.
 *
 * @see PalasWeapon
 * @see PalasP1
 * @see PalasA1
 * @see PalasA2
 * @see PalasA3
 * @see PalasUlt
 */
public final class Palas extends Support {
    /** 치유 점수 */
    public static final int HEAL_SCORE = 40;
    @Getter
    private static final Palas instance = new Palas();

    private Palas() {
        super("팔라스", "생물학 연구원", "ch_palas", Role.MARKSMAN, Species.HUMAN, '\u32D9', 4,
                1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "약이 다 될 때까지 조금만...";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "회복하는 동안 잠시 시간 좀 벌어주겠어요?";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "이 정도 상처는 별 거 아니에요.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "좀 기다려 봐요. 약 만드는게 쉬운 줄 알아요?";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "기다려요. 이제 주사기에 넣기만 하면 되요.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "전투 자극제 100cc, 투여할 수 있어요.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "이리 모여봐요. 치료받기 싫으면 말고.",
                "잔말 말고, 빨리 여기로 와요.",
                "환자들은 이리로 따라오세요."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "투약 완료, 어디 한번 싸워봐요.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case SILIA:
                return new String[]{"어째서...어째서 너가..!"};
            default:
                return new String[]{
                        "어라, 연구원이라고 얕잡아봤어요?",
                        "얼마나 아픈지 1부터 10까지로 말해주겠어요?",
                        "걱정 마요. 100년 후엔 치료해줄테니깐."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case SILIA:
                return new String[]{"괜찮아...해야할 게 있..잖아..?"};
            default:
                return new String[]{
                        "아파... 어서 진통제를...",
                        "당신, 날 다치게 한 걸 후회할 거에요!",
                        "정말 잔인한 사람이군요..."
                };
        }
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        new PalasTarget(combatUser).shot();
    }

    @Override
    public void onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        super.onGiveHeal(provider, target, amount);

        if (provider != target && target.isGoalTarget())
            provider.addScore("치유", HEAL_SCORE * amount / target.getDamageModule().getMaxHealth());
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((PalasWeapon) combatUser.getActionManager().getWeapon()).getAimModule().isAiming();
    }

    @Override
    @NonNull
    public PalasWeaponInfo getWeaponInfo() {
        return PalasWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{PalasP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{PalasA1Info.getInstance(), PalasA2Info.getInstance(), PalasA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public PalasUltInfo getUltimateSkillInfo() {
        return PalasUltInfo.getInstance();
    }

    private static final class PalasTarget extends Target<Healable> {
        private PalasTarget(@NonNull CombatUser combatUser) {
            super(combatUser, PalasA2Info.MAX_DISTANCE, EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            ((CombatUser) shooter).setGlowing(target, Timespan.ofTicks(3));
        }
    }
}
