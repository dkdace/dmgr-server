package com.dace.dmgr.combat.character.silia;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Scuffler;
import com.dace.dmgr.combat.character.silia.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

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
    /** 암살 점수 제한시간 (tick) */
    public static final long FAST_KILL_SCORE_TIME_LIMIT = (long) (2.5 * 20);

    @Getter
    private static final Silia instance = new Silia();

    private Silia() {
        super(null, "실리아", "고요한 폭풍", "DVSilia", '\u32D1', 4, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "팔라스 언니? 거기 누구 없어...?",
                "아직 멀쩡해! 아마도...?",
                "난 괜찮아! 문제 없다구!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "바람이 모이고 있어! 조금만 기다려!",
                "곧 폭풍이 몰아칠 거야!",
                "준비 됐어? 다들 날아가지 않게 꽉 잡아!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
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
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
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
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "으... 눈이... 감겨..",
                "피곤해... 잠시 쉬어야겠어...",
                "미안해... 얘들아..."
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        SiliaA3 skill3 = combatUser.getSkill(SiliaA3Info.getInstance());
        SiliaUlt skill4 = combatUser.getSkill(SiliaUltInfo.getInstance());

        StringJoiner text = new StringJoiner("    ");

        String skill3Display = StringFormUtil.getActionbarDurationBar(SiliaA3Info.getInstance().toString(),
                skill3.getStateValue() / 20.0, skill3.getMaxStateValue() / 20.0);
        if (!skill3.isDurationFinished())
            skill3Display += "  §7[" + skill3.getDefaultActionKeys()[0] + "] §f해제";
        text.add(skill3Display);
        if (!skill4.isDurationFinished() && skill4.isEnabled()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(SiliaUltInfo.getInstance().toString(), skill4.getDuration() / 20.0,
                    skill4.getDefaultDuration() / 20.0);
            text.add(skill4Display);
        }

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        if (victim instanceof CombatUser && isCrit)
            attacker.addScore("백어택", SiliaT1Info.CRIT_SCORE);

        return true;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        super.onKill(attacker, victim, score, isFinalHit);

        if (!(victim instanceof CombatUser))
            return;

        SiliaA1 skill1 = attacker.getSkill(SiliaA1Info.getInstance());
        SiliaUlt skillUlt = attacker.getSkill(SiliaUltInfo.getInstance());

        if (((CombatUser) victim).getDamageSumRemainingTime(attacker) > GeneralConfig.getCombatConfig().getDamageSumTimeLimit() - FAST_KILL_SCORE_TIME_LIMIT)
            attacker.addScore("암살", FAST_KILL_SCORE * score / 100.0);
        if (!skill1.isCooldownFinished() || !skill1.isDurationFinished())
            skill1.setCooldown(2);
        if (!skillUlt.isDurationFinished()) {
            skillUlt.addDuration(SiliaUltInfo.DURATION_ADD_ON_KILL);
            attacker.addScore("궁극기 보너스", SiliaUltInfo.KILL_SCORE * score / 100.0);
        }
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(SiliaP1Info.getInstance()).canUse(ActionKey.SPACE);
    }

    @Override
    @NonNull
    public SiliaWeaponInfo getWeaponInfo() {
        return SiliaWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public TraitInfo getCharacterTraitInfo(int number) {
        switch (number) {
            case 1:
                return SiliaT1Info.getInstance();
            case 2:
                return SiliaT2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return SiliaP1Info.getInstance();
            case 2:
                return SiliaP2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return SiliaA1Info.getInstance();
            case 2:
                return SiliaA2Info.getInstance();
            case 3:
                return SiliaA3Info.getInstance();
            case 4:
                return SiliaUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public SiliaUltInfo getUltimateSkillInfo() {
        return SiliaUltInfo.getInstance();
    }
}
