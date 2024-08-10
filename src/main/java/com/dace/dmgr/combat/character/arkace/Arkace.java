package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Marksman;
import com.dace.dmgr.combat.character.arkace.action.*;
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
 * 전투원 - 아케이스 클래스.
 *
 * @see ArkaceWeapon
 * @see ArkaceP1
 * @see ArkaceA1
 * @see ArkaceA2
 * @see ArkaceUlt
 */
public final class Arkace extends Marksman {
    @Getter
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", "DVArkace", '\u32D0', 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "여기는 아케이스, 신속한 치료가 필요하다!",
                "부상 발생, 치료를 요청한다!",
                "아직은 더 버틸 수 있다."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "에너지 충전 중이다.",
                "에너지 충전이 얼마 남지 않았다.",
                "에너지 증폭이 준비되었다."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "여기는 아케이스, 지원을 요청한다.",
                "화력 지원 바란다."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "에너지 증폭 활성화.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case SILIA:
                return new String[]{"그 원시적인 무기로 뭘 하겠다고 그러나?"};
            case JAGER:
                return new String[]{
                        "총은 그렇게 쓰는 물건이 아니다.",
                        "끈질긴 놈이군."
                };
            default:
                return new String[]{
                        "똑바로 해라.",
                        "연습이 아닌 실전이다.",
                        "적을 사살했다."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "제법..이군..",
                "운수 한 번... 안 좋은 날이군...",
                "여기서... 끝인 건가..."
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        ArkaceWeapon weapon = (ArkaceWeapon) combatUser.getWeapon();
        ArkaceA2 skill2 = combatUser.getSkill(ArkaceA2Info.getInstance());
        ArkaceUlt skill4 = combatUser.getSkill(ArkaceUltInfo.getInstance());

        int weaponAmmo = weapon.getReloadModule().getRemainingAmmo();
        double skill2Duration = skill2.getDuration() / 20.0;
        double skill2MaxDuration = skill2.getDefaultDuration() / 20.0;
        double skill4Duration = skill4.getDuration() / 20.0;
        double skill4MaxDuration = skill4.getDefaultDuration() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        String weaponDisplay = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weaponAmmo, ArkaceWeaponInfo.CAPACITY,
                ArkaceWeaponInfo.CAPACITY, '|');

        text.add(weaponDisplay);
        text.add("");
        if (!skill2.isDurationFinished()) {
            String skill2Display = StringFormUtil.getActionbarDurationBar(ArkaceA2Info.getInstance().toString(), skill2Duration,
                    skill2MaxDuration, 10, '■');
            text.add(skill2Display);
        }
        if (!skill4.isDurationFinished()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(ArkaceUltInfo.getInstance().toString(), skill4Duration,
                    skill4MaxDuration, 10, '■');
            text.add(skill4Display);
        }

        return text.toString();
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

        ArkaceUlt skillUlt = attacker.getSkill(ArkaceUltInfo.getInstance());

        if (!skillUlt.isDurationFinished())
            attacker.addScore("궁극기 보너스", ArkaceUltInfo.KILL_SCORE * score / 100.0);
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().isReloading();
    }

    @Override
    @NonNull
    public ArkaceWeaponInfo getWeaponInfo() {
        return ArkaceWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        if (number == 1)
            return ArkaceP1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ArkaceA1Info.getInstance();
            case 2:
                return ArkaceA2Info.getInstance();
            case 4:
                return ArkaceUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public ArkaceUltInfo getUltimateSkillInfo() {
        return ArkaceUltInfo.getInstance();
    }
}
