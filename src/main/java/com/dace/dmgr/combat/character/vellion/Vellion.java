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

        StringJoiner text = new StringJoiner("    ");

        if (!skillp1.isDurationFinished()) {
            String skillp1Display = StringFormUtil.getActionbarDurationBar(VellionP1Info.getInstance().toString(), skillp1.getDuration() / 20.0,
                    skillp1.getDefaultDuration() / 20.0) + "  §7[" + skillp1.getDefaultActionKeys()[0] + "] §f해제";
            text.add(skillp1Display);
        } else if (!skillp1.isCooldownFinished()) {
            String skillp1Display = StringFormUtil.getActionbarCooldownBar(VellionP1Info.getInstance().toString(), skillp1.getCooldown() / 20.0,
                    skillp1.getDefaultCooldown() / 20.0);
            text.add(skillp1Display);
        }
        if (!skill2.isDurationFinished() && skill2.isEnabled())
            text.add(VellionA2Info.getInstance() + "  §7[" + skill2.getDefaultActionKeys()[0] + "] §f해제");
        if (!skill4.isDurationFinished() && skill4.isEnabled()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(VellionUltInfo.getInstance().toString(), skill4.getDuration() / 20.0,
                    skill4.getDefaultDuration() / 20.0);
            text.add(skill4Display);
        }

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, @NonNull DamageType damageType, boolean isCrit) {
        if (victim.getDamageModule().isLiving()) {
            attacker.getSkill(VellionP2Info.getInstance()).setDamageAmount(damage);
            attacker.useAction(ActionKey.PERIODIC_1);
        }

        return true;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, damageType, location, isCrit);

        CombatEffectUtil.playBleedingEffect(location, victim, damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (!(victim instanceof CombatUser) || score >= 100)
            return;

        attacker.getSkill(VellionA2Info.getInstance()).applyAssistScore((CombatUser) victim);
        attacker.getSkill(VellionA3Info.getInstance()).applyAssistScore((CombatUser) victim);
        attacker.getSkill(VellionUltInfo.getInstance()).applyAssistScore((CombatUser) victim);
    }

    @Override
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        super.onGiveHeal(provider, target, amount);

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
                && combatUser.getSkill(VellionA3Info.getInstance()).isDurationFinished() && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
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
