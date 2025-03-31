package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Controller;
import com.dace.dmgr.combat.combatant.Role;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import lombok.Getter;
import lombok.NonNull;

/**
 * 전투원 - 벨리온 클래스.
 *
 * @see VellionWeapon
 * @see VellionP1
 * @see VellionP2
 * @see VellionA1
 * @see VellionA2
 * @see VellionA3
 * @see VellionUlt
 */
public final class Vellion extends Controller {
    /** 치유 점수 */
    public static final int HEAL_SCORE = 40;
    @Getter
    private static final Vellion instance = new Vellion();

    private Vellion() {
        super(Role.SUPPORT, "벨리온", "흑마법사", "DVVellion", '\u32D6', 3, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "날 치료해, 당장.";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "어서 날 치료해.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "치유 좀 부탁해.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "뭐가?";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "어서 준비하지 않고 뭐해?";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "때가 되면 시작할테니깐 기다리고 있어.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "나에게 모여라!",
                "함께 모여 저들에게 파멸과 죽음을!",
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "어디 한번 날뛰어봐. 할 수 있다면 말이야.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case SILIA:
                return new String[]{
                        "거슬리게 하지 말고 꺼져.",
                        "건방지긴."
                };
            case CHED:
                return new String[]{"...미안하구나."};
            default:
                return new String[]{
                        "내 계획의 일부분이 되어라.",
                        "너희들은 울부짖으며 쓰러져갈 뿐이다.",
                        "나락으로 떨어져라."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "안돼... 안돼, 아직 계획이..!",
                "망할...이 무식한 자식들!"
        };
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, boolean isCrit) {
        if (victim.isCreature()) {
            attacker.getSkill(VellionP2Info.getInstance()).setDamageAmount(damage);
            attacker.useAction(ActionKey.PERIODIC_1);
        }

        return true;
    }

    @Override
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        if (provider != target && target instanceof CombatUser)
            provider.addScore("치유", HEAL_SCORE * amount / target.getDamageModule().getMaxHealth());

        return true;
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !combatUser.getEntity().isFlying() && canJump(combatUser);
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        VellionP1 skillp1 = combatUser.getSkill(VellionP1Info.getInstance());
        return skillp1.canUse(skillp1.getDefaultActionKeys()[0]) && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        VellionA2 skill2 = combatUser.getSkill(VellionA2Info.getInstance());
        return combatUser.getSkill(VellionA1Info.getInstance()).isDurationFinished() && (skill2.isDurationFinished() || skill2.isEnabled())
                && combatUser.getSkill(VellionA3Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public VellionWeaponInfo getWeaponInfo() {
        return VellionWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{VellionP1Info.getInstance(), VellionP2Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{VellionA1Info.getInstance(), VellionA2Info.getInstance(), VellionA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public VellionUltInfo getUltimateSkillInfo() {
        return VellionUltInfo.getInstance();
    }
}
