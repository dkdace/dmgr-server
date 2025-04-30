package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Guardian;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;

/**
 * 전투원 - 퀘이커 클래스.
 *
 * @see QuakerWeapon
 * @see QuakerA1
 * @see QuakerA2
 * @see QuakerA3
 * @see QuakerUlt
 */
public final class Quaker extends Guardian {
    @Getter
    private static final Quaker instance = new Quaker();
    /** 특성 수정자 */
    private static final AbilityStatus.Modifier TRAIT_MODIFIER = new AbilityStatus.Modifier(QuakerT1Info.STATUS_EFFECT_RESISTANCE);
    /** 발소리 */
    private static final SoundEffect FOOTSTEP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_COW_STEP).volume(0.3).pitch(0.9).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder("new.entity.ravager.step").volume(0.2).pitch(0.8).pitchVariance(0.1).build());

    private Quaker() {
        super(null, "퀘이커", "불굴의 방패", "DVQuaker", '\u32D3', 1, 2500, 0.85, 1.8);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "재정비가 시급하다!";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "퀘이커, 치유를 요청한다!";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "갑옷에 기름칠좀 해야겠군.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "아직 준비 중이다.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "거의 다 준비됐다!";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "어서 저 놈들을 쓸어버리자고!";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
        return new String[]{
                "이곳을 점거해야 한다!",
                "방패 뒤로!"
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "다 쓸어버리겠다!";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case ARKACE:
                return new String[]{"조국의 방패는 납덩어리에 굴복하지 않는다!"};
            case METAR:
                return new String[]{"찌그러져 있어라, 깡통!"};
            default:
                return new String[]{
                        "적을 제거했다!",
                        "방해꾼을 처리했다!",
                        "어딜 방어선을 넘보는가!"
                };
        }
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case CHED:
                return new String[]{"정정당당하게 싸워라! 비겁자들!"};
            default:
                return new String[]{
                        "나는 굴복하지 않는다!",
                        "죽어도 항복은 없다!"
                };
        }
    }

    @Override
    @NonNull
    public Combatant.Species getSpecies() {
        return Species.HUMAN;
    }

    @Override
    public void onSet(@NonNull CombatUser combatUser) {
        super.onSet(combatUser);
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier(TRAIT_MODIFIER);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        if (!combatUser.getActionManager().getSkill(QuakerA1Info.getInstance()).isDurationFinished())
            volume = 1.4;

        FOOTSTEP_SOUND.play(combatUser.getLocation(), volume);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        ActionManager actionManager = combatUser.getActionManager();
        return actionManager.getSkill(QuakerA1Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(QuakerA2Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(QuakerA3Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        ActionManager actionManager = combatUser.getActionManager();
        return actionManager.getSkill(QuakerA2Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(QuakerA3Info.getInstance()).isDurationFinished()
                && actionManager.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public QuakerWeaponInfo getWeaponInfo() {
        return QuakerWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[]{QuakerT1Info.getInstance()};
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[0];
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{QuakerA1Info.getInstance(), QuakerA2Info.getInstance(), QuakerA3Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public QuakerUltInfo getUltimateSkillInfo() {
        return QuakerUltInfo.getInstance();
    }
}
