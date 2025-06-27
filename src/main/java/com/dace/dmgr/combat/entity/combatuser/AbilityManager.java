package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.combat.ability.Ability;
import com.dace.dmgr.combat.ability.Action;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.MeleeAttackAction;
import com.dace.dmgr.combat.ability.info.AbilityInfo;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.Skill;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.Swappable;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 전투 시스템 플레이어의 능력(무기, 스킬, 특성)을 관리하는 클래스.
 */
public final class AbilityManager {
    /** 궁극기 차단 점수 */
    static final int ULT_BLOCK_SCORE = 50;

    /** 능력 정보별 능력 목록 (능력 정보 : 능력) */
    private final HashMap<AbilityInfo<?>, Ability> abilityMap = new HashMap<>();
    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작 목록) */
    private final EnumMap<ActionKey, TreeSet<Action>> actionsMap = new EnumMap<>(ActionKey.class);
    /** 플레이어 인스턴스 */
    private final CombatUser combatUser;

    /**
     * 능력 관리 인스턴스를 생성하고, 플레이어의 능력 설정을 초기화한다.
     *
     * @param combatUser 대상 플레이어
     */
    AbilityManager(@NonNull CombatUser combatUser) {
        this.combatUser = combatUser;
        Combatant combatant = combatUser.getCombatantType().getCombatant();

        for (ActionKey actionKey : ActionKey.values())
            actionsMap.put(actionKey, new TreeSet<>(Comparator.comparing(Action::getPriority).reversed()));

        HashSet<Ability> abilities = new HashSet<>();
        abilities.add(new MeleeAttackAction(combatUser));

        combatant.getAbilityInfos().forEach(abilityInfo -> {
            Ability ability = abilityInfo.create(combatUser);

            abilityMap.put(abilityInfo, ability);
            abilities.add(ability);
        });

        abilities.forEach(ability -> {
            if (ability instanceof Action)
                ((Action) ability).getDefaultActionKeys().forEach(actionKey -> actionsMap.get(actionKey).add((Action) ability));
        });
    }

    /**
     * 무기를 반환한다.
     *
     * @return 무기 인스턴스
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Weapon> T getWeapon() {
        return (T) abilityMap.get(combatUser.getCombatantType().getCombatant().getWeaponInfo());
    }

    /**
     * 지정한 능력 정보에 해당하는 능력을 반환한다.
     *
     * @param abilityInfo 능력 정보
     * @param <T>         {@link Ability}을 상속받는 능력
     * @return 스킬 인스턴스
     * @throws NullPointerException 해당하는 능력이 존재하지 않으면 발생
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbility(@NonNull AbilityInfo<T> abilityInfo) {
        return Validate.notNull((T) abilityMap.get(abilityInfo), "일치하는 능력이 존재하지 않음");
    }

    /**
     * 궁극기 스킬을 반환한다.
     *
     * @return 궁극기 인스턴스
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends UltimateSkill> T getUltimateSkill() {
        return (T) abilityMap.get(combatUser.getCombatantType().getCombatant().getUltimateSkillInfo());
    }

    /**
     * 지정한 동작 사용 키에 해당하는 동작을 사용한다.
     *
     * @param actionKey 동작 사용 키
     */
    public void useAction(@NonNull ActionKey actionKey) {
        actionsMap.get(actionKey).forEach(action -> {
            if (combatUser.isDead() || action == null || combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.USE_ACTION))
                return;

            if (action instanceof MeleeAttackAction && action.canUse(actionKey)) {
                action.onUse(actionKey);
                return;
            }

            Weapon realWeapon = getWeapon();
            if (realWeapon instanceof Swappable && ((Swappable<?>) realWeapon).getSwapModule().isSwapped())
                realWeapon = ((Swappable<?>) realWeapon).getSwapModule().getSubweapon();

            if (action instanceof Weapon)
                handleUseWeapon(actionKey, realWeapon);
            else if (action instanceof Skill)
                handleUseSkill(actionKey, (Skill) action);
        });
    }

    /**
     * 무기 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param weapon    무기
     */
    private void handleUseWeapon(@NonNull ActionKey actionKey, @NonNull Weapon weapon) {
        if (weapon instanceof FullAuto && (((FullAuto) weapon).getFullAutoModule().getFullAutoKey() == actionKey))
            ((FullAuto) weapon).getFullAutoModule().use();
        else if (weapon.canUse(actionKey))
            weapon.onUse(actionKey);
    }

    /**
     * 스킬 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param skill     스킬
     */
    private void handleUseSkill(@NonNull ActionKey actionKey, @NonNull Skill skill) {
        if (skill.canUse(actionKey))
            skill.onUse(actionKey);
    }

    /**
     * 사용 중인 모든 동작을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelAction(@Nullable CombatUser attacker) {
        getWeapon().cancel();
        cancelSkill(attacker);
    }

    /**
     * 사용 중인 모든 스킬을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelSkill(@Nullable CombatUser attacker) {
        combatUser.getCombatantType().getCombatant().getSkillInfos().forEach(skillInfo -> {
            Skill skill = getAbility(skillInfo);
            if (!skill.cancel())
                return;

            if (attacker != null && !combatUser.isDead() && skill instanceof UltimateSkill)
                attacker.addScore("궁극기 차단", ULT_BLOCK_SCORE);
        });
    }

    /**
     * 적 처치 시 스킬의 보너스 점수 지급을 처리한다.
     *
     * @param victim            피격자
     * @param contributionScore 처치 기여도
     */
    void handleBonusScoreSkill(@NonNull Damageable victim, double contributionScore) {
        combatUser.getCombatantType().getCombatant().getSkillInfos().forEach(skillInfo -> {
            Skill skill = getAbility(skillInfo);
            if (skill instanceof HasBonusScore)
                ((HasBonusScore) skill).getBonusScoreModule().onKill(victim, contributionScore);
        });
    }

    /**
     * 무기와 모든 스킬의 {@link Action#reset()}을 호출한다.
     */
    void reset() {
        abilityMap.values().forEach(ability -> {
            if (ability instanceof Action)
                ((Action) ability).reset();
        });
    }

    /**
     * 무기와 모든 스킬의 {@link Action#remove()}을 호출한다.
     */
    void remove() {
        abilityMap.values().forEach(ability -> {
            if (ability instanceof Action)
                ((Action) ability).remove();
        });
    }
}
