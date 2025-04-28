package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.MeleeAttackAction;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * 전투 시스템 플레이어의 동작(무기, 스킬)을 관리하는 클래스.
 */
public final class ActionManager {
    /** 궁극기 차단 점수 */
    static final int ULT_BLOCK_SCORE = 50;

    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작 목록) */
    private final EnumMap<ActionKey, TreeSet<Action>> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 목록 (스킬 정보 : 스킬) */
    private final HashMap<SkillInfo<?>, Skill> skillMap = new HashMap<>();
    /** 플레이어 인스턴스 */
    private final CombatUser combatUser;
    /** 무기 인스턴스 */
    @NonNull
    @Getter
    private final Weapon weapon;

    /**
     * 동작 관리 인스턴스를 생성하고, 플레이어의 동작 설정을 초기화한다.
     *
     * @param combatUser 대상 플레이어
     */
    ActionManager(@NonNull CombatUser combatUser) {
        this.combatUser = combatUser;
        Combatant combatant = combatUser.getCombatantType().getCombatant();

        for (ActionKey actionKey : ActionKey.values())
            actionMap.put(actionKey, new TreeSet<>(Comparator.comparing(Action::getPriority).reversed()));

        actionMap.get(ActionKey.SWAP_HAND).add(new MeleeAttackAction(combatUser));

        weapon = combatant.getWeaponInfo().createWeapon(combatUser);
        for (ActionKey actionKey : weapon.getDefaultActionKeys())
            actionMap.get(actionKey).add(weapon);

        for (SkillInfo<?> skillInfo : combatant.getSkillInfos()) {
            Skill skill = skillInfo.createSkill(combatUser);
            skillMap.put(skillInfo, skill);

            for (ActionKey actionKey : skill.getDefaultActionKeys())
                actionMap.get(actionKey).add(skill);
        }
    }

    /**
     * 지정한 스킬 정보에 해당하는 스킬을 반환한다.
     *
     * @param skillInfo 스킬 정보
     * @param <T>       {@link Skill}을 상속받는 스킬
     * @return 스킬 인스턴스
     * @throws NullPointerException 해당하는 스킬이 존재하지 않으면 발생
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Skill> T getSkill(@NonNull SkillInfo<T> skillInfo) {
        return Validate.notNull((T) skillMap.get(skillInfo), "일치하는 스킬이 존재하지 않음");
    }

    /**
     * 지정한 동작 사용 키에 해당하는 동작을 사용한다.
     *
     * @param actionKey 동작 사용 키
     */
    public void useAction(@NonNull ActionKey actionKey) {
        actionMap.get(actionKey).forEach(action -> {
            if (combatUser.isDead() || action == null || combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.USE_ACTION))
                return;

            if (action instanceof MeleeAttackAction && action.canUse(actionKey)) {
                action.onUse(actionKey);
                return;
            }

            Weapon realWeapon = weapon;
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
        weapon.cancel();
        cancelSkill(attacker);
    }

    /**
     * 사용 중인 모든 스킬을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelSkill(@Nullable CombatUser attacker) {
        skillMap.values().forEach(skill -> {
            if (!skill.cancel())
                return;

            if (attacker != null && !combatUser.isDead() && skill instanceof UltimateSkill)
                attacker.addScore("궁극기 차단", ULT_BLOCK_SCORE);
        });
    }

    /**
     * 적 처치 시 스킬의 보너스 점수 지급을 처리한다.
     *
     * @param victim 피격자
     * @param score  처치 기여 점수
     */
    void handleBonusScoreSkill(@NonNull Damageable victim, int score) {
        for (SkillInfo<?> skillInfo : combatUser.getCombatantType().getCombatant().getSkillInfos()) {
            Skill skill = getSkill(skillInfo);
            if (skill instanceof HasBonusScore)
                ((HasBonusScore) skill).getBonusScoreModule().onKill(victim, score);
        }
    }

    /**
     * 무기와 모든 스킬의 {@link Action#reset()}을 호출한다.
     */
    void reset() {
        weapon.reset();
        skillMap.values().forEach(Skill::reset);
    }

    /**
     * 무기와 모든 스킬의 {@link Action#remove()}을 호출한다.
     */
    void remove() {
        weapon.remove();
        skillMap.values().forEach(Skill::remove);
    }
}
