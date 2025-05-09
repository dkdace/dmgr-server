package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Guardian;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;

/**
 * 전투원 - METAR 클래스.
 *
 * @see MetarWeapon
 * @see MetarP1
 * @see MetarA1
 * @see MetarA2
 * @see MetarA3
 * @see MetarUlt
 */
public final class Metar extends Guardian {
    @Getter
    private static final Metar instance = new Metar();
    /** 발소리 */
    private static final SoundEffect FOOTSTEP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("new.entity.hoglin.step").volume(0.4).pitch(0.8).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_IRONGOLEM_STEP).volume(0.7).pitch(0.85).pitchVariance(0.1).build());

    private Metar() {
        super(null, "METAR", "군용 차세대 전술 돌격 로봇", "DVMetar", Species.ROBOT, '\u32DA', 2, 2500, 0.8, 1.8);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "기체 파손율 심각. 신속한 재정비가 필요합니다.";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "기체 파손율 높음. 재정비가 필요합니다.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "기체 손상이 감지되었습니다.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "에너지 충전이 필요합니다.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "에너지 충전이 거의 완료되었습니다.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "반전자 분열포를 사용할 수 있습니다.";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "효율적인 방어를 위해 여기에 집결해야 합니다.",
                "안전을 위해 이 지역에서 방어해야 합니다."
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "반전자 분열포를 가동합니다. 접근 금지.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        return new String[]{
                "적대적 개체를 제거합니다.",
                "위협 요소를 제거했습니다."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        return new String[]{"위험 상황. 심각한 기체 손상이 발생했습니다.",};
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        if (combatUser.getEntity().isSneaking())
            combatUser.getActionManager().useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        FOOTSTEP_SOUND.play(combatUser.getLocation(), volume);
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        ActionManager actionManager = combatUser.getActionManager();
        return ((MetarWeapon) actionManager.getWeapon()).canSprint() && actionManager.getSkill(MetarUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return canSprint(combatUser);
    }

    @Override
    @NonNull
    public MetarWeaponInfo getWeaponInfo() {
        return MetarWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{MetarP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{MetarA1Info.getInstance(), MetarA2Info.getInstance(), MetarA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public MetarUltInfo getUltimateSkillInfo() {
        return MetarUltInfo.getInstance();
    }
}
