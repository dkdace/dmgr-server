package com.dace.dmgr.combat.character.magritta;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Scuffler;
import com.dace.dmgr.combat.character.magritta.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

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
        super("마그리타", "DVMagrita", '\u32D8', 1200, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "의사놈들 어딨어! 당장 튀어나와!",
                "꽤 아프네, 여기 치료해주는 녀석은 없어?",
                "치료 좀 해야겠어. 의사 없어?"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "지금 예열 중이야. 어때, 재밌겠지?",
                "곧 화려한 불꽃놀이를 보여주지!",
                "불꽃놀이, 포화 준비 완료!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
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
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case MAGRITTA:
                return new String[]{"어라? 나잖아?"};
            default:
                return new String[]{
                        "여긴 마그리타, 하나 처리했어.",
                        "나중에 내가 직접 화장해주지."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case MAGRITTA:
                return new String[]{"넌 뭐야!"};
            default:
                return new String[]{
                        "아직 다 못 불태웠는데...",
                        "이렇게 뒤질 바에 불타는 게 낫겠어..."
                };
        }
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        MagrittaWeapon weapon = (MagrittaWeapon) combatUser.getWeapon();
        MagrittaP1 skillp1 = combatUser.getSkill(MagrittaP1Info.getInstance());
        MagrittaA2 skill2 = combatUser.getSkill(MagrittaA2Info.getInstance());
        MagrittaUlt skill4 = combatUser.getSkill(MagrittaUltInfo.getInstance());

        int weaponAmmo = weapon.getReloadModule().getRemainingAmmo();
        double skillp1Duration = CooldownUtil.getCooldown(combatUser, MagrittaP1.COOLDOWN_ID) / 20.0;
        double skillp1MaxDuration = MagrittaP1Info.DURATION / 20.0;
        double skill2Duration = skill2.getDuration() / 20.0;
        double skill2MaxDuration = skill2.getDefaultDuration() / 20.0;
        double skill4Duration = skill4.getDuration() / 20.0;
        double skill4MaxDuration = skill4.getDefaultDuration() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        String weaponDisplay = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weaponAmmo, MagrittaWeaponInfo.CAPACITY,
                8, '┃');

        text.add(weaponDisplay);
        text.add("");
        if (!skillp1.isDurationFinished()) {
            String skillp1Display = StringFormUtil.getActionbarDurationBar(MagrittaP1Info.getInstance().toString(), skillp1Duration,
                    skillp1MaxDuration, 10, '■');
            text.add(skillp1Display);
        }
        if (!skill2.isDurationFinished()) {
            String skill2Display = StringFormUtil.getActionbarDurationBar(MagrittaA2Info.getInstance().toString(), skill2Duration,
                    skill2MaxDuration, 10, '■');
            text.add(skill2Display);
        }
        if (!skill4.isDurationFinished() && skill4.isEnabled()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(MagrittaUltInfo.getInstance().toString(), skill4Duration,
                    skill4MaxDuration, 10, '■');
            text.add(skill4Display);
        }

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0)
            combatUser.useAction(ActionKey.PERIODIC_1);
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

        MagrittaUlt skillUlt = attacker.getSkill(MagrittaUltInfo.getInstance());

        if (!skillUlt.isDurationFinished())
            attacker.addScore("궁극기 보너스", MagrittaUltInfo.KILL_SCORE * score / 100.0);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished() && combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public MagrittaWeaponInfo getWeaponInfo() {
        return MagrittaWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        if (number == 1)
            return MagrittaP1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return MagrittaA1Info.getInstance();
            case 2:
                return MagrittaA2Info.getInstance();
            case 4:
                return MagrittaUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public MagrittaUltInfo getUltimateSkillInfo() {
        return MagrittaUltInfo.getInstance();
    }
}
