package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.Trait;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 역할군이 '제어'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Controller extends Combatant {
    /**
     * 제어 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param subRole          부 역할군
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param species          종족 유형
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Controller(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, @NonNull Species species,
                         char icon, int difficulty, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.CONTROLLER, subRole, species, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        if (i % 5 == 0)
            CombatUtil.getCombatEntities(combatUser.getLocation().getWorld(), EntityCondition.team(combatUser).exclude(combatUser)
                            .and(combatEntity -> combatEntity.isGoalTarget() && combatEntity.getDamageModule().isLowHealth()))
                    .forEach(target -> {
                        Damageable targetCombatEntity = CombatUtil.getNearCombatEntity(target.getLocation(), RoleTrait1Info.DETECT_RADIUS,
                                EntityCondition.enemy(combatUser).and(Damageable::isGoalTarget));

                        if (targetCombatEntity != null)
                            combatUser.setGlowing(targetCombatEntity, Timespan.ofTicks(10));
                    });

        combatUser.getActionManager().getTrait(RoleTrait2Info.instance).onUse();
    }

    @Override
    @MustBeInvokedByOverriders
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, double damage, @Nullable Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, location, isCrit);
        victim.getActionManager().getTrait(RoleTrait2Info.instance).lastDamageTimestamp = Timestamp.now();
    }

    @Override
    @NonNull
    final TraitInfo @NonNull [] getDefaultTraitInfos() {
        return new TraitInfo[]{RoleTrait1Info.instance, RoleTrait2Info.instance};
    }

    /**
     * 특성 1번 정보 클래스.
     */
    private static final class RoleTrait1Info extends TraitInfo {
        /** 감지 범위 (단위: 블록) */
        private static final int DETECT_RADIUS = 10;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 제어 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("치명상인 아군 근처의 적을 탐지합니다.")
                            .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                            .build()));
        }
    }

    /**
     * 특성 2번 정보 클래스.
     */
    private static final class RoleTrait2Info extends DynamicTraitInfo<RoleTrait2> {
        /** 초당 치유량 */
        private static final int HEAL_PER_SECOND = 40;
        /** 활성화 시간 */
        private static final Timespan ACTIVATE_DURATION = Timespan.ofSeconds(4);

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(RoleTrait2.class, "역할: 제어 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("일정 시간동안 피해를 받지 않으면 <:HEAL:회복>합니다.")
                            .addValueInfo(TextIcon.DURATION, Format.TIME, ACTIVATE_DURATION.toSeconds())
                            .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL_PER_SECOND)
                            .build()));
        }
    }

    /**
     * 특성 2번 클래스.
     */
    private static final class RoleTrait2 extends Trait {
        /** 마지막 피격 시점 */
        private Timestamp lastDamageTimestamp = Timestamp.now();

        public RoleTrait2(@NonNull CombatUser combatUser) {
            super(combatUser, RoleTrait2Info.instance);
        }

        private void onUse() {
            if (lastDamageTimestamp.plus(RoleTrait2Info.ACTIVATE_DURATION).isBefore(Timestamp.now()))
                combatUser.getDamageModule().heal(combatUser, RoleTrait2Info.HEAL_PER_SECOND / 20.0, false);
        }
    }
}
