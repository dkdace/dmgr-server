package com.dace.dmgr.combat.character.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.Support;
import com.dace.dmgr.combat.character.palas.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 팔라스 클래스.
 *
 * @see PalasWeapon
 * @see PalasP1
 * @see PalasA1
 * @see PalasA2
 * @see PalasA3
 * @see PalasUlt
 */
public final class Palas extends Support {
    /** 치유 점수 */
    public static final int HEAL_SCORE = 40;
    @Getter
    private static final Palas instance = new Palas();

    private Palas() {
        super(Role.MARKSMAN, "팔라스", "생물학 연구원", "DVPalas", '\u32D9', 4, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "약이 다 될 때까지 조금만...",
                "회복하는 동안 잠시 시간 좀 벌어주겠어요?",
                "이 정도 상처는 별 거 아니에요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "좀 기다려 봐요. 약 만드는게 쉬운 줄 알아요?",
                "기다려요. 이제 주사기에 넣기만 하면 되요. ",
                "전투 자극제 100cc, 투여할 수 있어요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "이리 모여봐요. 치료받기 싫으면 말고.",
                "잔말 말고, 빨리 여기로 와요.",
                "환자들은 이리로 따라오세요."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "투약 완료, 어디 한번 싸워봐요.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case SILIA:
                return new String[]{"어째서...어째서 너가..!"};
            default:
                return new String[]{
                        "어라, 연구원이라고 얕잡아봤어요?",
                        "얼마나 아픈지 1부터 10까지로 말해주겠어요?",
                        "걱정 마요. 100년 후엔 치료해줄테니깐."
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case SILIA:
                return new String[]{"괜찮아...해야할 게 있..잖아..?"};
            default:
                return new String[]{
                        "아파... 어서 진통제를...",
                        "당신, 날 다치게 한 걸 후회할 거에요!",
                        "정말 잔인한 사람이군요..."
                };
        }
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        PalasWeapon weapon = (PalasWeapon) combatUser.getWeapon();

        StringJoiner text = new StringJoiner("    ");

        String weaponDisplay = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon.getReloadModule().getRemainingAmmo(),
                PalasWeaponInfo.CAPACITY, PalasWeaponInfo.CAPACITY, '┃');
        weaponDisplay += (weapon.isActionCooldown() ? " §a■" : " §c□");
        text.add(weaponDisplay);

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        new PalasTarget(combatUser).shot();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingParticle(victim, location, damage);
    }

    @Override
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        super.onGiveHeal(provider, target, amount);

        if (provider != target && target instanceof CombatUser)
            provider.addScore("치유", HEAL_SCORE * amount / target.getDamageModule().getMaxHealth());

        return true;
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (!(victim instanceof CombatUser) || score >= 100)
            return;

        attacker.getSkill(PalasA1Info.getInstance()).applyAssistScore((CombatUser) victim);
        attacker.getSkill(PalasA3Info.getInstance()).applyAssistScore((CombatUser) victim);
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((PalasWeapon) combatUser.getWeapon()).getAimModule().isAiming();
    }

    @Override
    @NonNull
    public PalasWeaponInfo getWeaponInfo() {
        return PalasWeaponInfo.getInstance();
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
            return PalasP1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return PalasA1Info.getInstance();
            case 2:
                return PalasA2Info.getInstance();
            case 3:
                return PalasA3Info.getInstance();
            case 4:
                return PalasUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public PalasUltInfo getUltimateSkillInfo() {
        return PalasUltInfo.getInstance();
    }

    private static final class PalasTarget extends Target<Healable> {
        private PalasTarget(@NonNull CombatUser combatUser) {
            super(combatUser, PalasA2Info.MAX_DISTANCE, false, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            ((CombatUser) shooter).getUser().setGlowing(target.getEntity(), ChatColor.GREEN, Timespan.ofTicks(3));
        }
    }
}
