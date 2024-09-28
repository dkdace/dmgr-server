package com.dace.dmgr.combat.character.ched;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Marksman;
import com.dace.dmgr.combat.character.ched.action.*;
import com.dace.dmgr.combat.character.inferno.action.InfernoUltInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 체드 클래스.
 *
 * @see ChedWeapon
 * @see ChedP1
 * @see ChedA1
 */
public final class Ched extends Marksman {
    @Getter
    private static final Ched instance = new Ched();

    private Ched() {
        super("체드", "화염 궁수", "DVChed", '\u32D4', 4, 1000, 1.1, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "여기는 체드, 빠른 치료 부탁한다.",
                "여기는 체드, 치유가 필요하다.",
                "여기는 체드, 지원 바란다."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "기술을 준비하고 있다.",
                "기술이 거의 다 준비되었다.",
                "불사조를 소환할 준비가 되었다."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "여기는 체드, 화력 지원이 필요하다.",
                "이곳으로 뭉쳐야 한다."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "붉게 타오르는 불사조여, 날아가라!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case VELLION:
                return new String[]{"...불충을 용서하십시오."};
            case CHED:
                return new String[]{"적 궁병을 처리했다."};
            default:
                return new String[]{
                        "명중이다.",
                        "불타 죽어라.",
                        "궁술은 기사의 기본 소양이지."
                };
        }

    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case VELLION:
                return new String[]{"모시게 되어... 영광이었습니다."};
            case CHED:
                return new String[]{"내가 화살을 맞을 날이 올 줄이야..."};
            default:
                return new String[]{
                        "벨리온님.. 불충을..",
                        "아아.. 주군..",
                        "이걸로 끝인가..."
                };
        }
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        ChedA1 skill1 = combatUser.getSkill(ChedA1Info.getInstance());

        double skillp1Duration = skillp1.getHangTick() / 20.0;
        double skillp1MaxDuration = ChedP1Info.HANG_DURATION / 20.0;

        StringJoiner text = new StringJoiner("    ");

        if (!skillp1.isDurationFinished()) {
            String skillp1Display = StringFormUtil.getActionbarDurationBar(ChedP1Info.getInstance().toString(), skillp1Duration,
                    skillp1MaxDuration, 10, '■');
            text.add(skillp1Display);
        }
        if (!skill1.isDurationFinished() && skill1.isEnabled())
            text.add(ChedA1Info.getInstance() + "  §7[" + skill1.getDefaultActionKeys()[0].getName() + "] §f해제");

        return text.toString();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    @NonNull
    public ChedWeaponInfo getWeaponInfo() {
        return ChedWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public TraitInfo getCharacterTraitInfo(int number) {
        return null;
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        if (number == 1)
            return ChedP1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ChedA1Info.getInstance();
            case 4:
                return InfernoUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public InfernoUltInfo getUltimateSkillInfo() {
        return InfernoUltInfo.getInstance();
    }
}
