package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Scuffler;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 전투원 - 실리아 클래스.
 *
 * @see SiliaWeapon
 * @see SiliaP1
 * @see SiliaP2
 * @see SiliaA1
 * @see SiliaA2
 * @see SiliaA3
 * @see SiliaUlt
 */
public final class Silia extends Scuffler {
    /** 암살 점수 */
    public static final int FAST_KILL_SCORE = 20;
    /** 암살 점수 제한시간 */
    public static final Timespan FAST_KILL_SCORE_TIME_LIMIT = Timespan.ofSeconds(2.5);

    @Getter
    private static final Silia instance = new Silia();

    private Silia() {
        super(null, "실리아", "고요한 폭풍", "DVSilia", '\u32D1', 4, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "팔라스 언니? 거기 누구 없어...?";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "아직 멀쩡해! 아마도...?";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "난 괜찮아! 문제 없다구!";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "바람이 모이고 있어! 조금만 기다려!";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "곧 폭풍이 몰아칠 거야!";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "준비 됐어? 다들 날아가지 않게 꽉 잡아!";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "나랑 같이 놀 사람~ 여기여기 붙어라!",
                "이리 와서 나랑 같이 놀자!",
                "거기 너! 나랑 같이 놀래?"
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "시원하게 날려버리자!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            default:
                return new String[]{
                        "잡았다! 이번엔 내가 이겼네?",
                        "바람 앞에서는 도망칠 수 없다구!",
                        "짜잔! 응? 벌써 끝난거야?"
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "으... 눈이... 감겨..",
                "피곤해... 잠시 쉬어야겠어...",
                "미안해... 얘들아..."
        };
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, boolean isCrit) {
        if (attacker != victim && victim.isGoalTarget() && isCrit)
            attacker.addScore("백어택", SiliaT1Info.CRIT_SCORE);

        return true;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        SiliaA3 skill3 = victim.getActionManager().getSkill(SiliaA3Info.getInstance());
        if (skill3.isDurationFinished())
            return;

        skill3.setDamage(skill3.getDamage() + damage);
        if (skill3.getDamage() >= victim.getDamageModule().getMaxHealth() * SiliaA3Info.CANCEL_DAMAGE_RATIO) {
            skill3.cancel();
            skill3.setCooldown(SiliaA3Info.COOLDOWN_FORCE);
        }
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, double contributionScore, boolean isFinalHit) {
        super.onKill(attacker, victim, contributionScore, isFinalHit);

        if (!victim.isGoalTarget())
            return;

        if (victim instanceof CombatUser && ((CombatUser) victim).getKillContributionElapsedTime(attacker).compareTo(FAST_KILL_SCORE_TIME_LIMIT) <= 0)
            attacker.addScore("암살", FAST_KILL_SCORE * contributionScore);

        ActionManager actionManager = attacker.getActionManager();

        SiliaA1 skill1 = actionManager.getSkill(SiliaA1Info.getInstance());
        if (!skill1.isCooldownFinished() || !skill1.isDurationFinished())
            skill1.setCooldown(Timespan.ZERO);

        SiliaUlt skillUlt = actionManager.getSkill(SiliaUltInfo.getInstance());
        if (!skillUlt.isDurationFinished()) {
            skillUlt.addDuration(SiliaUltInfo.DURATION_ADD_ON_KILL);
            attacker.addScore("궁극기 보너스", SiliaUltInfo.KILL_SCORE * contributionScore);
        }
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getActionManager().getSkill(SiliaP1Info.getInstance()).canUse(ActionKey.SPACE);
    }

    @Override
    @NonNull
    public SiliaWeaponInfo getWeaponInfo() {
        return SiliaWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[]{SiliaT1Info.getInstance(), SiliaT2Info.getInstance()};
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{SiliaP1Info.getInstance(), SiliaP2Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{SiliaA1Info.getInstance(), SiliaA2Info.getInstance(), SiliaA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public SiliaUltInfo getUltimateSkillInfo() {
        return SiliaUltInfo.getInstance();
    }
}
