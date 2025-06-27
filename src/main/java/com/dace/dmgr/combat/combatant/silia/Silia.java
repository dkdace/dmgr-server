package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Scuffler;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

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
        super("실리아", "고요한 폭풍", "ch_silia", null, Species.HUMAN, '\u32D1', 4,
                1000, 1.0, 1.0);
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
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case PALAS:
                return new String[]{"언니.. 괜찮아?"};
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
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case PALAS:
                return new String[]{"팔라스 언니...?"};
            default:
                return new String[]{
                        "으... 눈이... 감겨..",
                        "피곤해... 잠시 쉬어야겠어...",
                        "미안해... 얘들아..."
                };
        }
    }

    @Override
    public void onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, boolean isCrit) {
        attacker.getAbilityManager().getAbility(SiliaT1Info.getInstance()).onAttack(victim, isCrit);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, location, isCrit);
        victim.getAbilityManager().getAbility(SiliaA3Info.getInstance()).onDamage(damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, double contributionScore, boolean isFinalHit) {
        super.onKill(attacker, victim, contributionScore, isFinalHit);

        if (victim instanceof CombatUser && ((CombatUser) victim).getKillContributionElapsedTime(attacker).compareTo(FAST_KILL_SCORE_TIME_LIMIT) <= 0)
            attacker.addScore("암살", FAST_KILL_SCORE * contributionScore);

        AbilityManager abilityManager = attacker.getAbilityManager();
        abilityManager.getAbility(SiliaA1Info.getInstance()).onKill(victim);
        abilityManager.getAbility(SiliaUltInfo.getInstance()).onKill(victim, contributionScore);
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        SiliaP1 skillp1 = combatUser.getAbilityManager().getAbility(SiliaP1Info.getInstance());
        return skillp1.canUse(ActionKey.SPACE);
    }

    @Override
    @NonNull
    public SiliaWeaponInfo getWeaponInfo() {
        return SiliaWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected List<@NonNull TraitInfo<?>> getCombatantTraitInfos() {
        return Arrays.asList(SiliaT1Info.getInstance(), SiliaT2Info.getInstance());
    }

    @Override
    @NonNull
    public List<@NonNull PassiveSkillInfo<?>> getPassiveSkillInfos() {
        return Arrays.asList(SiliaP1Info.getInstance(), SiliaP2Info.getInstance());
    }

    @Override
    @NonNull
    public List<@NonNull ActiveSkillInfo<?>> getActiveSkillInfos() {
        return Arrays.asList(SiliaA1Info.getInstance(), SiliaA2Info.getInstance(), SiliaA3Info.getInstance(), getUltimateSkillInfo());
    }

    @Override
    @NonNull
    public SiliaUltInfo getUltimateSkillInfo() {
        return SiliaUltInfo.getInstance();
    }
}
