package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Vanguard;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

/**
 * 전투원 - 인페르노 클래스.
 *
 * @see InfernoWeapon
 * @see InfernoP1
 * @see InfernoA1
 * @see InfernoA2
 * @see InfernoUlt
 */
public final class Inferno extends Vanguard {
    @Getter
    private static final Inferno instance = new Inferno();
    /** 발소리 */
    private static final SoundEffect FOOTSTEP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("new.entity.panda.step").volume(0.4).pitch(0.9).pitchVariance(0.1).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_LLAMA_STEP).volume(0.3).pitch(0.7).pitchVariance(0.1).build());

    private Inferno() {
        super(null, "인페르노", "화염 돌격병", "DVInferno", '\u32D7', 1, 2000, 0.9, 1.4);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "피가 뜨거운 건 좋지만, 조금 버거워서 말이지...";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "꽤 뜨거운 싸움이었어. 잠시 열 좀 식히자구.";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "의사씨, 심심하면 같이 안 다닐래?";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "기다려 봐. 아직 예열 중이야.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "조금만 더 온도를 높여보자구!";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "오버히트 완료! 준비 됐어?";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
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
    public String @NonNull [] getKillMent(@NonNull CombatantType combatantType) {
        switch (combatantType) {
            case MAGRITTA:
                return new String[]{"머리 좀 식혀, 멍청아."};
            default:
                return new String[]{
                        "안됐네, 다음엔 좀 더 노력해봐!",
                        "어디 그 정도 공격으로 되겠어?",
                        "열정이 부족해! 열정이!"
                };
        }

    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CombatantType combatantType) {
        return new String[]{
                "뭐... 이런 날도 있는거지.",
                "바닥은... 조금 차갑네.",
                "...뻔한 결말이려나."
        };
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0)
            combatUser.useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        FOOTSTEP_SOUND.play(combatUser.getLocation(), volume);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        if (victim.getSkill(InfernoUltInfo.getInstance()).isDurationFinished()) {
            CombatEffectUtil.playBleedingParticle(victim, location, damage);
            return;
        }

        InfernoUltInfo.SOUND.DAMAGE.play(victim.getLocation(), 1 + damage * 0.001);
        if (location != null)
            InfernoUltInfo.PARTICLE.DAMAGE.play(location, damage * 0.04);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        super.onKill(attacker, victim, score, isFinalHit);

        if (!(victim instanceof CombatUser))
            return;

        InfernoUlt skillUlt = attacker.getSkill(InfernoUltInfo.getInstance());
        if (!skillUlt.isDurationFinished())
            attacker.addScore("궁극기 보너스", InfernoUltInfo.KILL_SCORE * score / 100.0);
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(InfernoA1Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public InfernoWeaponInfo getWeaponInfo() {
        return InfernoWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected TraitInfo @NonNull [] getCombatantTraitInfos() {
        return new TraitInfo[0];
    }

    @Override
    @NonNull
    public PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos() {
        return new PassiveSkillInfo[]{InfernoP1Info.getInstance()};
    }

    @Override
    @NonNull
    public ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos() {
        return new ActiveSkillInfo[]{InfernoA1Info.getInstance(), InfernoA2Info.getInstance(), getUltimateSkillInfo()};
    }

    @Override
    @NonNull
    public InfernoUltInfo getUltimateSkillInfo() {
        return InfernoUltInfo.getInstance();
    }
}
