package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Support;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.Target;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

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
        super(null, "니스", "평화주의자", "DVNis", '\u32D5', 1, 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "조금... 아주 조금만 쉬어도 될까요?";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "이 정도 상처는 좀 있으면 낫는답니다.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "아직 더 버틸 수 있어요.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "희망을 잃지 말아요.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "조금만 더 기다려줘요.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "제가 당신을 지켜드릴게요.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
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
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "살짝 겁만 주려 했는데...",
                "내가...내 손으로...",
                "...미안해요."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "어떻게 이런 짓을...",
                "이런 야만적인..."
        };
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        new NeaceTarget(combatUser).shot();

        if (i % 5 == 0)
            combatUser.useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        victim.getSkill(NeaceP1Info.getInstance()).cancel();
    }

    @Override
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        if (provider != target && target.isGoalTarget())
            provider.addScore("치유", HEAL_SCORE * amount / target.getDamageModule().getMaxHealth());

        return true;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        NeaceUlt skill4 = combatUser.getSkill(NeaceUltInfo.getInstance());
        return skill4.isDurationFinished() || skill4.isEnabled();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return canSprint(combatUser);
    }

    @Override
    @NonNull
    public NeaceWeaponInfo getWeaponInfo() {
        return NeaceWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{NeaceP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{NeaceA1Info.getInstance(), NeaceA2Info.getInstance(), NeaceA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public NeaceUltInfo getUltimateSkillInfo() {
        return NeaceUltInfo.getInstance();
    }

    private static final class NeaceTarget extends Target<Healable> {
        private NeaceTarget(@NonNull CombatUser combatUser) {
            super(combatUser, NeaceA1Info.MAX_DISTANCE, CombatUtil.EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            ((CombatUser) shooter).getUser().setGlowing(target.getEntity(), ChatColor.GREEN, Timespan.ofTicks(3));
        }
    }
}
