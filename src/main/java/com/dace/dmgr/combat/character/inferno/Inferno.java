package com.dace.dmgr.combat.character.inferno;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Vanguard;
import com.dace.dmgr.combat.character.arkace.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 인페르노 클래스.
 */
public final class Inferno extends Vanguard {
    @Getter
    private static final Inferno instance = new Inferno();

    private Inferno() {
        super("인페르노", "DVInferno", '\u32D7', 2000, 0.9, 1.4);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "피가 뜨거운 건 좋지만, 조금 버거워서 말이지...",
                "꽤 뜨거운 싸움이었어. 잠시 열 좀 식히자구.",
                "의사씨, 심심하면 같이 안 다닐래?"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "기다려 봐. 아직 예열 중이야.",
                "조금만 더 온도를 높여보자구!",
                "오버히트 완료! 준비 됐어?"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "다들 모여! 여기서 열정을 불태우는거야!",
                "함께 모일 때 불꽃은 더욱 강해진다!"
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "파이어스톰, 다 태워버려!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        return new String[]{
                "안됐네, 다음엔 좀 더 노력해봐!",
                "어디 그 정도 공격으로 되겠어?",
                "열정이 부족해! 열정이!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "뭐... 이런 날도 있는거지.",
                "바닥은... 조금 차갑네.",
                "...뻔한 결말이려나."
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        StringJoiner text = new StringJoiner("    ");

        text.add("");

        return text.toString();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    @NonNull
    public ArkaceWeaponInfo getWeaponInfo() {
        return ArkaceWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
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
