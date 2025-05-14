package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.info.*;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * 전투원 정보를 관리하는 클래스.
 *
 * @see Scuffler
 * @see Marksman
 * @see Vanguard
 * @see Guardian
 * @see Support
 * @see Controller
 */
@Getter
public abstract class Combatant {
    /** 이름 */
    @NonNull
    private final String name;
    /** 별명 */
    @NonNull
    private final String nickname;
    /** 스킨 */
    @NonNull
    private final PlayerSkin playerSkin;
    /** 주 역할군 */
    @NonNull
    private final Role role;
    /** 부 역할군 */
    @Nullable
    private final Role subRole;
    /** 종족 유형 */
    @NonNull
    private final Species species;
    /** 전투원 아이콘 */
    private final char icon;
    /** 난이도 */
    private final int difficulty;
    /** 체력 */
    private final int health;
    /** 이동속도 배수 */
    private final double speedMultiplier;
    /** 히트박스 크기 배수 */
    private final double hitboxMultiplier;

    /**
     * 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param role             주 역할군
     * @param subRole          부 역할군
     * @param species          종족 유형
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    Combatant(@NonNull String name, @NonNull String nickname, @NonNull String skinName, @NonNull Role role, @Nullable Role subRole,
              @NonNull Species species, char icon, int difficulty, int health, double speedMultiplier, double hitboxMultiplier) {
        this.name = name;
        this.nickname = nickname;
        this.playerSkin = PlayerSkin.fromName(skinName);
        this.role = role;
        this.subRole = subRole;
        this.species = species;
        this.icon = icon;
        this.difficulty = difficulty;
        this.health = health;
        this.speedMultiplier = speedMultiplier;
        this.hitboxMultiplier = hitboxMultiplier;
    }

    /**
     * 치명상일 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    protected abstract String getReqHealMentLow();

    /**
     * 체력이 절반 이하일 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    protected abstract String getReqHealMentHalf();

    /**
     * 체력이 충분할 때의 치료 요청 대사를 반환한다.
     *
     * @return 치료 요청 대사
     */
    @NonNull
    protected abstract String getReqHealMentNormal();

    /**
     * 현재 체력에 따른 치료 요청 대사를 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 치료 요청 대사
     */
    @NonNull
    public final String getReqHealMent(@NonNull CombatUser combatUser) {
        String state;
        String ment;
        if (combatUser.getDamageModule().isLowHealth()) {
            state = "치명상";
            ment = getReqHealMentLow();
        } else if (combatUser.getDamageModule().isHalfHealth()) {
            state = "체력 낮음";
            ment = getReqHealMentHalf();
        } else {
            state = "치료 요청";
            ment = getReqHealMentNormal();
        }

        return MessageFormat.format("§7[{0}] §f{1}", state, ment);
    }

    /**
     * 궁극기 게이지가 0~89%일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    protected abstract String getUltStateMentLow();

    /**
     * 궁극기 게이지가 90~99%일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    protected abstract String getUltStateMentNearFull();

    /**
     * 궁극기 게이지가 충전 상태일 때의 궁극기 상태 대사를 반환한다.
     *
     * @return 궁극기 상태 대사
     */
    @NonNull
    protected abstract String getUltStateMentFull();

    /**
     * 현재 궁극기 게이지에 따른 궁극기 상태 대사를 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 궁극기 상태 대사
     */
    @NonNull
    public final String getUltStateMent(@NonNull CombatUser combatUser) {
        String ment;
        if (combatUser.getUltGaugePercent() < 0.9)
            ment = getUltStateMentLow();
        else if (combatUser.getUltGaugePercent() < 1)
            ment = getUltStateMentNearFull();
        else
            ment = getUltStateMentFull();

        return MessageFormat.format("§7[궁극기 {0}%] §f{1}", Math.floor(combatUser.getUltGaugePercent() * 100), ment);
    }

    /**
     * 집결 요청 대사 목록을 반환한다.
     *
     * @return 집결 요청 대사 목록
     */
    @NonNull
    protected abstract String @NonNull [] getReqRallyMents();

    /**
     * 무작위 집결 요청 대사를 반환한다.
     *
     * @return 집결 요청 대사
     */
    @NonNull
    public final String getReqRallyMent() {
        String[] ments = getReqRallyMents();
        return MessageFormat.format("§7[집결 요청] §f{0}", ments[RandomUtils.nextInt(0, ments.length)]);
    }

    /**
     * 궁극기 사용 대사를 반환한다.
     *
     * @return 궁극기 사용 대사
     */
    @NonNull
    public abstract String getUltUseMent();

    /**
     * 전투원 처치 시 대사 목록을 반환한다.
     *
     * @param combatantType 피격자의 전투원 종류
     * @return 전투원 처치 대사 목록
     */
    @NonNull
    protected abstract String @NonNull [] getKillMents(@NonNull CombatantType combatantType);

    /**
     * 전투원 처치 시 무작위 대사 목록을 반환한다.
     *
     * @param combatantType 피격자의 전투원 종류
     * @return 전투원 처치 대사
     */
    @NonNull
    public final String getKillMent(@NonNull CombatantType combatantType) {
        String[] ments = getKillMents(combatantType);
        return ments[RandomUtils.nextInt(0, ments.length)];
    }

    /**
     * 사망 시 대사 목록을 반환한다.
     *
     * @param combatantType 공격자의 전투원 종류
     * @return 전투원 사망 대사 목록
     */
    @NonNull
    protected abstract String @NonNull [] getDeathMents(@NonNull CombatantType combatantType);

    /**
     * 사망 시 무작위 대사 목록을 반환한다.
     *
     * @param combatantType 공격자의 전투원 종류
     * @return 전투원 사망 대사
     */
    @NonNull
    public final String getDeathMent(@NonNull CombatantType combatantType) {
        String[] ments = getDeathMents(combatantType);
        return ments[RandomUtils.nextInt(0, ments.length)];
    }

    /**
     * 액션바에 무기 및 스킬 상태를 표시하기 위한 문자열을 반환한다.
     *
     * @param combatUser 대상 플레이어
     * @return 액션바 문자열
     */
    @NonNull
    public final String getActionBarString(@NonNull CombatUser combatUser) {
        ArrayList<String> texts = new ArrayList<>();

        ActionManager actionManager = combatUser.getActionManager();
        Weapon weapon = actionManager.getWeapon();
        String weaponText = weapon.getActionBarString();
        if (weaponText != null) {
            texts.add(weaponText);

            if (weapon instanceof Swappable) {
                Weapon subweapon = ((Swappable<?>) weapon).getSwapModule().getSubweapon();
                String subweaponText = subweapon.getActionBarString();
                if (subweaponText != null)
                    texts.add(subweaponText);
            }

            texts.add("");
        }

        for (SkillInfo<?> skillInfo : getSkillInfos()) {
            String actionBarString = actionManager.getSkill(skillInfo).getActionBarString();
            if (actionBarString != null)
                texts.add(actionBarString);
        }

        return String.join("    ", texts);
    }

    /**
     * 전투원을 선택했을 때 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     */
    public void onSet(@NonNull CombatUser combatUser) {
        // 미사용
    }

    /**
     * 전투원이 매 틱마다 실행할 작업.
     *
     * @param combatUser 대상 플레이어
     * @param i          인덱스
     */
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (!combatUser.isDead() && combatUser.getDamageModule().isLowHealth())
            species.getReaction().onTickLowHealth(combatUser);
    }

    /**
     * 전투원이 걸을 때 실행할 작업.
     *
     * <p>주로 발소리 재생에 사용한다.</p>
     *
     * @param combatUser 대상 플레이어
     * @param volume     발소리 음량
     */
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        // 미사용
    }

    /**
     * 전투원이 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param damage   피해량
     * @param isCrit   치명타 여부
     * @see Combatant#onDamage(CombatUser, Attacker, double, Location, boolean)
     */
    public void onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, double damage, boolean isCrit) {
        // 미사용
    }

    /**
     * 전투원이 피해를 입었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @param damage   피해량
     * @param location 맞은 위치
     * @param isCrit   치명타 여부
     * @see Combatant#onAttack(CombatUser, Damageable, double, boolean)
     */
    @MustBeInvokedByOverriders
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        if (victim.getDamageModule().getTotalShield() == 0)
            species.getReaction().onDamage(victim, damage, location);
    }

    /**
     * 전투원이 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param provider 제공자
     * @param target   수급자
     * @param amount   치유량
     * @see Combatant#onTakeHeal(CombatUser, Healer, double)
     */
    public void onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        // 미사용
    }

    /**
     * 전투원이 치유를 받았을 때 실행될 작업.
     *
     * @param target   수급자
     * @param provider 제공자
     * @param amount   치유량
     * @see Combatant#onGiveHeal(CombatUser, Healable, double)
     */
    public void onTakeHeal(@NonNull CombatUser target, @Nullable Healer provider, double amount) {
        // 미사용
    }

    /**
     * 전투원이 힐 팩을 사용했을 때 실행될 작업.
     *
     * @param combatUser 대상 플레이어
     */
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        // 미사용
    }

    /**
     * 전투원이 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param attacker          공격자
     * @param victim            피격자
     * @param contributionScore 처치 기여도
     * @param isFinalHit        결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see Combatant#onDeath(CombatUser, Attacker)
     */
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, double contributionScore, boolean isFinalHit) {
        // 미사용
    }

    /**
     * 전투원이 죽었을 때 실행될 작업.
     *
     * @param victim   피격자
     * @param attacker 공격자
     * @see Combatant#onKill(CombatUser, Damageable, double, boolean)
     */
    @MustBeInvokedByOverriders
    public void onDeath(@NonNull CombatUser victim, @Nullable Attacker attacker) {
        species.getReaction().onDeath(victim);
    }

    /**
     * 전투원이 기본 근접 공격을 사용할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 근접 공격을 사용할 수 있으면 {@code true} 반환
     */
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 달리기를 할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 달리기 가능 여부
     */
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 비행할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 비행 가능 여부
     */
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    /**
     * 전투원이 점프를 할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 점프 가능 여부
     */
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원이 공격 및 치유를 통해 궁극기 게이지를 충전할 수 있는지 확인한다.
     *
     * @param combatUser 대상 플레이어
     * @return 궁극기 충전 가능 여부
     */
    public boolean canChargeUlt(@NonNull CombatUser combatUser) {
        return true;
    }

    /**
     * 전투원의 무기 정보를 반환한다.
     *
     * @return 무기 정보
     */
    @NonNull
    public abstract WeaponInfo<?> getWeaponInfo();

    /**
     * 전투원의 특성 목록을 반환한다.
     *
     * @return 특성 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public final TraitInfo @NonNull [] getTraitInfos() {
        return ArrayUtils.addAll(getDefaultTraitInfos(), getCombatantTraitInfos());
    }

    /**
     * 전투원의 역할군 기본 특성 목록을 반환한다.
     *
     * @return 특성 목록
     */
    @NonNull
    abstract TraitInfo @NonNull [] getDefaultTraitInfos();

    /**
     * 전투원의 개별 특성 목록을 반환한다.
     *
     * @return 특성 목록
     */
    @NonNull
    protected abstract TraitInfo @NonNull [] getCombatantTraitInfos();

    /**
     * 전투원의 모든 스킬 목록을 반환한다.
     *
     * @return 모든 스킬 목록. 길이가 0~8인 배열
     */
    @NonNull
    public final SkillInfo<?> @NonNull [] getSkillInfos() {
        return ArrayUtils.addAll(ArrayUtils.addAll(new SkillInfo[0], getPassiveSkillInfos()), getActiveSkillInfos());
    }

    /**
     * 전투원의 패시브 스킬 정보 목록을 반환한다.
     *
     * @return 패시브 스킬 정보 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public abstract PassiveSkillInfo<?> @NonNull [] getPassiveSkillInfos();

    /**
     * 전투원의 액티브 스킬 정보 목록을 반환한다.
     *
     * @return 액티브 스킬 정보 목록. 길이가 0~4 사이인 배열
     */
    @NonNull
    public abstract ActiveSkillInfo<?> @NonNull [] getActiveSkillInfos();

    /**
     * 전투원의 궁극기 정보를 반환한다.
     *
     * @return 궁극기 정보
     */
    @NonNull
    public abstract UltimateSkillInfo<?> getUltimateSkillInfo();

    /**
     * 전투원의 종족 유형.
     */
    @AllArgsConstructor
    @Getter
    protected enum Species {
        /** 인간 */
        HUMAN(new HumanReaction()),
        /** 로봇 */
        ROBOT(new RobotReaction());

        /** 이벤트 반응 */
        @NonNull
        private final Reaction reaction;

        /**
         * 종족별 공통된 이벤트 반응을 처리하는 인터페이스.
         */
        private interface Reaction {
            /**
             * 치명상일 때 매 틱마다 실행할 작업.
             *
             * @param combatUser 대상 플레이어
             */
            void onTickLowHealth(@NonNull CombatUser combatUser);

            /**
             * 피해를 입었을 때 실행할 작업.
             *
             * @param combatUser 대상 플레이어
             * @param damage     피해량
             * @param location   맞은 위치
             */
            void onDamage(@NonNull CombatUser combatUser, double damage, @Nullable Location location);

            /**
             * 죽었을 때 실행할 작업.
             *
             * @param combatUser 대상 플레이어
             */
            void onDeath(@NonNull CombatUser combatUser);
        }

        private static class HumanReaction implements Reaction {
            /** 치명상 효과 */
            private static final ParticleEffect LOW_HEALTH_PARTICLE = new ParticleEffect(
                    ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.REDSTONE_BLOCK, 0)
                            .horizontalSpread(0.15).verticalSpread(0.45).speed(0.03).build());

            @Override
            public void onTickLowHealth(@NonNull CombatUser combatUser) {
                LOW_HEALTH_PARTICLE.play(combatUser.getCenterLocation());
            }

            @Override
            public void onDamage(@NonNull CombatUser combatUser, double damage, @Nullable Location location) {
                CombatEffectUtil.playBleedingParticle(combatUser, location, damage);
            }

            @Override
            public void onDeath(@NonNull CombatUser combatUser) {
                // 미사용
            }
        }

        private static class RobotReaction implements Reaction {
            /** 치명상 효과 */
            private static final ParticleEffect LOW_HEALTH_PARTICLE = new ParticleEffect(
                    ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.COAL_BLOCK, 0)
                            .horizontalSpread(0.15).verticalSpread(0.45).speed(0.03).build(),
                    ParticleEffect.NormalParticleInfo.builder(Particle.CRIT).horizontalSpread(0.15).verticalSpread(0.45).speed(0.2).build());
            /** 피격 효과음 */
            private static final SoundEffect DAMAGE_SOUND = new SoundEffect(
                    SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(0.15).pitch(1.3).pitchVariance(0.2).build());
            /** 사망 효과 */
            private static final ParticleEffect DEATH_PARTICLE = new ParticleEffect(
                    ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).build(),
                    ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(300)
                            .horizontalSpread(0.2).verticalSpread(0.2).speed(0.3).build());
            /** 사망 효과음 */
            private static final SoundEffect DEATH_SOUND = new SoundEffect(
                    SoundEffect.SoundInfo.builder(Sound.ENTITY_GENERIC_EXPLODE).volume(2).pitch(0.5).build(),
                    SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(2).pitch(0.5).build(),
                    SoundEffect.SoundInfo.builder("random.metalhit").volume(2).pitch(0.5).build(),
                    SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_BREAK).volume(2).pitch(0.5).build());

            @Override
            public void onTickLowHealth(@NonNull CombatUser combatUser) {
                LOW_HEALTH_PARTICLE.play(combatUser.getCenterLocation());
            }

            @Override
            public void onDamage(@NonNull CombatUser combatUser, double damage, @Nullable Location location) {
                CombatEffectUtil.playBreakParticle(combatUser, location, damage);
                DAMAGE_SOUND.play(combatUser.getLocation(), 1 + damage * 0.001);
            }

            @Override
            public void onDeath(@NonNull CombatUser combatUser) {
                DEATH_PARTICLE.play(combatUser.getCenterLocation());
                DEATH_SOUND.play(combatUser.getLocation());
            }
        }
    }
}
