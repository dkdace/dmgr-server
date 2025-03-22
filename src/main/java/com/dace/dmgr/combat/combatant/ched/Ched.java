package com.dace.dmgr.combat.combatant.ched;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Marksman;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 전투원 - 체드 클래스.
 *
 * @see ChedWeapon
 * @see ChedP1
 * @see ChedA1
 * @see ChedA2
 * @see ChedA3
 * @see ChedUlt
 */
public final class Ched extends Marksman {
    @Getter
    private static final Ched instance = new Ched();

    private Ched() {
        super(null, "체드", "화염 궁수", "DVChed", '\u32D4', 4, 1000, 1.1, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "여기는 체드, 빠른 치료 부탁한다.";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "여기는 체드, 치유가 필요하다.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "여기는 체드, 지원 바란다.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "기술을 준비하고 있다.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "기술이 거의 다 준비되었다.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "불사조를 소환할 준비가 되었다.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
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
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
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
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
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
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingParticle(victim, location, damage);
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(ChedA2Info.getInstance()).canUse(ActionKey.SPACE);
    }

    @Override
    @NonNull
    public ChedWeaponInfo getWeaponInfo() {
        return ChedWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{ChedP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{ChedA1Info.getInstance(), ChedA2Info.getInstance(), ChedA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public ChedUltInfo getUltimateSkillInfo() {
        return ChedUltInfo.getInstance();
    }
}
