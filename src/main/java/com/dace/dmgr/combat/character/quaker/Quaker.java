package com.dace.dmgr.combat.character.quaker;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Guardian;
import com.dace.dmgr.combat.character.quaker.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 퀘이커 클래스.
 *
 * @see QuakerWeapon
 * @see QuakerA1
 * @see QuakerA2
 * @see QuakerA3
 * @see QuakerUlt
 */
public final class Quaker extends Guardian {
    @Getter
    private static final Quaker instance = new Quaker();
    /** 특성 수정자 */
    private static final String TRAIT_MODIFIER_ID = "QuakerT1";

    private Quaker() {
        super("퀘이커", "불굴의 방패", "DVQuaker", '\u32D3', 1, 2500, 0.85, 1.8);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "재정비가 시급하다!",
                "퀘이커, 치유를 요청한다!",
                "갑옷에 기름칠좀 해야겠군."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "아직 준비 중이다.",
                "거의 다 준비됐다!",
                "어서 저 놈들을 쓸어버리자고!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "이곳을 점거해야 한다!",
                "방패 뒤로!"
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "다 쓸어버리겠다!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case ARKACE:
                return new String[]{
                        "조국의 방패는 납덩어리에 굴복하지 않는다!"
                };
            default:
                return new String[]{
                        "적을 제거했다!",
                        "방해꾼을 처리했다!",
                        "어딜 방어선을 넘보는가!"
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "나는 굴복하지 않는다!",
                "죽어도 항복은 없다!"
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        QuakerA1 skill1 = combatUser.getSkill(QuakerA1Info.getInstance());

        int skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String skill1Display = StringFormUtil.getActionbarProgressBar(QuakerA1Info.getInstance().toString(), skill1Health, skill1MaxHealth,
                10, '■');

        if (!skill1.isDurationFinished())
            skill1Display += "  §7[" + skill1.getDefaultActionKeys()[0].getName() + "][" + skill1.getDefaultActionKeys()[1].getName() + "] §f해제";
        text.add(skill1Display);

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier(TRAIT_MODIFIER_ID, QuakerT1Info.STATUS_EFFECT_RESISTANCE);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        if (!combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished())
            volume = 1.4;
        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_FOOTSTEP, combatUser.getEntity().getLocation(), volume);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (!(victim instanceof CombatUser) || score >= 100)
            return;

        if (CooldownUtil.getCooldown(attacker, QuakerA2.ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("처치 지원", QuakerA2Info.ASSIST_SCORE);
        if (CooldownUtil.getCooldown(attacker, QuakerUlt.ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("처치 지원", QuakerUltInfo.ASSIST_SCORE);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(QuakerA3Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerA3Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public QuakerWeaponInfo getWeaponInfo() {
        return QuakerWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public TraitInfo getCharacterTraitInfo(int number) {
        if (number == 1)
            return QuakerT1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return QuakerA1Info.getInstance();
            case 2:
                return QuakerA2Info.getInstance();
            case 3:
                return QuakerA3Info.getInstance();
            case 4:
                return QuakerUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public QuakerUltInfo getUltimateSkillInfo() {
        return QuakerUltInfo.getInstance();
    }
}
