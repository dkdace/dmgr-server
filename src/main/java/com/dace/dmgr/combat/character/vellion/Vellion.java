package com.dace.dmgr.combat.character.vellion;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Controller;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.vellion.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

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
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "날 치료해, 당장.",
                "어서 날 치료해.",
                "치유 좀 부탁해."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "뭐가?",
                "어서 준비하지 않고 뭐해?",
                "때가 되면 시작할테니깐 기다리고 있어."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
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
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case SILIA:
                return new String[]{
                        "거슬리게 하지 말고 꺼져.",
                        "건방지긴."
                };
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
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "안돼... 안돼, 아직 계획이..!",
                "망할...이 무식한 자식들!"
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        VellionP1 skillp1 = combatUser.getSkill(VellionP1Info.getInstance());
        VellionA2 skill2 = combatUser.getSkill(VellionA2Info.getInstance());
        VellionUlt skill4 = combatUser.getSkill(VellionUltInfo.getInstance());

        double skillp1Cooldown = skillp1.getCooldown() / 20.0;
        double skillp1MaxCooldown = skillp1.getDefaultCooldown() / 20.0;
        double skillp1Duration = skillp1.getDuration() / 20.0;
        double skillp1MaxDuration = skillp1.getDefaultDuration() / 20.0;
        double skill4Duration = skill4.getDuration() / 20.0;
        double skill4MaxDuration = skill4.getDefaultDuration() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        if (!skillp1.isDurationFinished()) {
            String skillp1Display = StringFormUtil.getActionbarDurationBar(VellionP1Info.getInstance().toString(), skillp1Duration, skillp1MaxDuration,
                    10, '■') + "  §7[" + skillp1.getDefaultActionKeys()[0].getName() + "] §f해제";
            text.add(skillp1Display);
        } else if (!skillp1.isCooldownFinished()) {
            String skillp1Display = StringFormUtil.getActionbarCooldownBar(VellionP1Info.getInstance().toString(), skillp1Cooldown, skillp1MaxCooldown,
                    10, '■');
            text.add(skillp1Display);
        }
        if (!skill2.isDurationFinished() && skill2.isEnabled())
            text.add(VellionA2Info.getInstance() + "  §7[" + skill2.getDefaultActionKeys()[0].getName() + "] §f해제");
        if (!skill4.isDurationFinished() && skill4.isEnabled()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(VellionUltInfo.getInstance().toString(), skill4Duration,
                    skill4MaxDuration, 10, '■');
            text.add(skill4Display);
        }

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        if (victim.getDamageModule().isLiving()) {
            VellionP2 skillp2 = attacker.getSkill(VellionP2Info.getInstance());
            skillp2.setDamageAmount(damage);
            attacker.useAction(ActionKey.PERIODIC_1);
        }

        return true;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, damageType, location, isCrit);

        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (!(victim instanceof CombatUser) || score >= 100)
            return;

        if (CooldownUtil.getCooldown(attacker, VellionA2.ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("처치 지원", VellionA2Info.ASSIST_SCORE);
        if (CooldownUtil.getCooldown(attacker, VellionA3.ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("처치 지원", VellionA3Info.ASSIST_SCORE);
        if (CooldownUtil.getCooldown(attacker, VellionUlt.ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("처치 지원", VellionUltInfo.ASSIST_SCORE);
    }

    @Override
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, int amount) {
        super.onGiveHeal(provider, target, amount);

        if (provider != target && target instanceof CombatUser)
            provider.addScore("치유", (double) (HEAL_SCORE * amount) / target.getDamageModule().getMaxHealth());

        return true;
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        VellionA2 skill2 = combatUser.getSkill(VellionA2Info.getInstance());

        return !combatUser.getEntity().isFlying() && combatUser.getSkill(VellionA1Info.getInstance()).isDurationFinished() &&
                (skill2.isDurationFinished() || skill2.isEnabled()) && combatUser.getSkill(VellionA3Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(VellionP1Info.getInstance()).isCooldownFinished() && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        VellionA2 skill2 = combatUser.getSkill(VellionA2Info.getInstance());

        return combatUser.getSkill(VellionA1Info.getInstance()).isDurationFinished() && (skill2.isDurationFinished() || skill2.isEnabled()) &&
                combatUser.getSkill(VellionA3Info.getInstance()).isDurationFinished() && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public VellionWeaponInfo getWeaponInfo() {
        return VellionWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public TraitInfo getCharacterTraitInfo(int number) {
        return null;
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return VellionP1Info.getInstance();
            case 2:
                return VellionP2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return VellionA1Info.getInstance();
            case 2:
                return VellionA2Info.getInstance();
            case 3:
                return VellionA3Info.getInstance();
            case 4:
                return VellionUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public VellionUltInfo getUltimateSkillInfo() {
        return VellionUltInfo.getInstance();
    }
}
