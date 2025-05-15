package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Role;
import com.dace.dmgr.combat.combatant.Vanguard;
import com.dace.dmgr.combat.combatant.inferno.InfernoUltInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;

/**
 * 전투원 - No.7 클래스.
 *
 * @see No7Weapon
 * @see No7A1
 */
public final class No7 extends Vanguard {
    @Getter
    private static final No7 instance = new No7();
    /** 발소리 */
    private static final SoundEffect FOOTSTEP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_SHEEP_STEP).volume(0.3).pitch(0.7).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_STEP).volume(0.6).pitch(1).pitchVariance(0.1).build());

    private No7() {
        super("No.7", "전기 깡통 로봇", "ch_no7", Role.GUARDIAN, Species.ROBOT, '\u32DB', 2,
                2000, 0.9, 1.6);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "기체에 심각한 손상 발견. 정비 필요.";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "기체에 손상 발견. 정비 필요.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "기체에 약한 손상 발견.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "에너지 충전 중.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "에너지 충전 완료 근접.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "에너지 충전 완료. 고에너지 방출 가능.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{"효율적인 전투 방식 탐색. 집결 요청.",};
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "Y.A.L.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        return new String[]{"목표 제거"};
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        return new String[]{"기체 파손됨. 작전 수행 불가."};
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);
        combatUser.getActionManager().getTrait(No7T1Info.getInstance()).addShield(-No7T1Info.DECREASE_PER_SECOND / 20.0);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        FOOTSTEP_SOUND.play(combatUser.getLocation(), volume);
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return ((No7Weapon) combatUser.getActionManager().getWeapon()).canSprint();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return canSprint(combatUser);
    }

    @Override
    @NonNull
    public No7WeaponInfo getWeaponInfo() {
        return No7WeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[]{No7T1Info.getInstance()};
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[0];
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{No7A1Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public InfernoUltInfo getUltimateSkillInfo() {
        return InfernoUltInfo.getInstance();
    }
}
