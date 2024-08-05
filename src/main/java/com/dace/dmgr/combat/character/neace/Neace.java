package com.dace.dmgr.combat.character.neace;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Support;
import com.dace.dmgr.combat.character.neace.action.*;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.GlowUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 니스 클래스.
 *
 * @see NeaceWeapon
 * @see NeaceP1
 * @see NeaceA1
 * @see NeaceA2
 * @see NeaceA3
 * @see NeaceUlt
 */
public final class Neace extends Support {
    /** 치유 점수 */
    public static final int HEAL_SCORE = 60;
    @Getter
    private static final Neace instance = new Neace();

    private Neace() {
        super("니스", "DVNis", '\u32D5', 1000, 1.0, 1.0);
    }

    /**
     * 대상 타겟팅이 가능한 동작의 사용 조건을 반환한다.
     *
     * @param combatUser 플레이어
     * @param target     사용 대상
     */
    public static boolean getTargetedActionCondition(@NonNull CombatUser combatUser, @NonNull CombatEntity target) {
        return target instanceof Healable && !target.isEnemy(combatUser) &&
                target != combatUser && LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation());
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "조금... 아주 조금만 쉬어도 될까요?",
                "이 정도 상처는 좀 있으면 낫는답니다.",
                "아직 더 버틸 수 있어요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "희망을 잃지 말아요.",
                "조금만 더 기다려줘요.",
                "제가 당신을 지켜드릴게요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "흩어지지 말아요.",
                "제 곁에 있으면 안전해요."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "모든 것은 평화를 위하여!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        return new String[]{
                "살짝 겁만 주려 했는데...",
                "내가...내 손으로...",
                "...미안해요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "어떻게 이런 짓을...",
                "이런 야만적인..."
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        NeaceA2 skill2 = (NeaceA2) combatUser.getSkill(NeaceA2Info.getInstance());
        NeaceA3 skill3 = (NeaceA3) combatUser.getSkill(NeaceA3Info.getInstance());
        NeaceUlt skill4 = (NeaceUlt) combatUser.getSkill(NeaceUltInfo.getInstance());

        double skill2Duration = skill2.getDuration() / 20.0;
        double skill2MaxDuration = skill2.getDefaultDuration() / 20.0;
        double skill4Duration = skill4.getDuration() / 20.0;
        double skill4MaxDuration = skill4.getDefaultDuration() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        if (!skill2.isDurationFinished()) {
            String skill2Display = StringFormUtil.getActionbarDurationBar(skill2.getSkillInfo().toString(), skill2Duration,
                    skill2MaxDuration, 10, '■') + "  §7[" + skill2.getDefaultActionKeys()[0].getName() + "] §f해제";
            text.add(skill2Display);
        }
        if (!skill3.isDurationFinished())
            text.add(skill3.getSkillInfo() + "  §7[" + skill3.getDefaultActionKeys()[0].getName() + "] §f해제");
        if (!skill4.isDurationFinished() && skill4.isEnabled()) {
            String skill4Display = StringFormUtil.getActionbarDurationBar(skill4.getSkillInfo().toString(), skill4Duration,
                    skill4MaxDuration, 10, '■');
            text.add(skill4Display);
        }

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        new NeaceTarget(combatUser).shoot();

        if (i % 5 == 0)
            combatUser.useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
        CooldownUtil.setCooldown(victim, NeaceP1.COOLDOWN_ID, NeaceP1Info.ACTIVATE_DURATION);
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
        return true;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        NeaceUlt skill4 = (NeaceUlt) combatUser.getSkill(NeaceUltInfo.getInstance());

        return skill4.isDurationFinished() || skill4.isEnabled();
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        NeaceUlt skill4 = (NeaceUlt) combatUser.getSkill(NeaceUltInfo.getInstance());

        return skill4.isDurationFinished() || skill4.isEnabled();
    }

    @Override
    @NonNull
    public NeaceWeaponInfo getWeaponInfo() {
        return NeaceWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return NeaceP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return NeaceA1Info.getInstance();
            case 2:
                return NeaceA2Info.getInstance();
            case 3:
                return NeaceA3Info.getInstance();
            case 4:
                return NeaceUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public NeaceUltInfo getUltimateSkillInfo() {
        return NeaceUltInfo.getInstance();
    }

    private static final class NeaceTarget extends Hitscan {
        private NeaceTarget(CombatUser combatUser) {
            super(combatUser, HitscanOption.builder().size(HitscanOption.TARGET_SIZE_DEFAULT).maxDistance(NeaceA1Info.MAX_DISTANCE)
                    .condition(combatEntity -> getTargetedActionCondition(combatUser, combatEntity)).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            GlowUtil.setGlowing(target.getEntity(), ChatColor.GREEN, shooter.getEntity(), 3);
            return false;
        }
    }
}
