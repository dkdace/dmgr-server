package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatEntityRegistry;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.interaction.Target;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 역할군이 '지원'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Support extends Combatant {
    /**
     * 지원 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param subRole          부 역할군
     * @param species          종족 유형
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Support(@NonNull String name, @NonNull String nickname, @NonNull String skinName, @Nullable Role subRole, @NonNull Species species,
                      char icon, int difficulty, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.SUPPORT, subRole, species, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        combatUser.getAbilityManager().getAbility(RoleTrait1Info.instance).onTick(i);
        combatUser.getAbilityManager().getAbility(RoleTrait2Info.instance).onTick();
    }

    @Override
    @MustBeInvokedByOverriders
    public void onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, double amount) {
        provider.getAbilityManager().getAbility(RoleTrait2Info.instance).onGiveHeal(target);
    }

    @Override
    @NonNull
    final List<@NonNull TraitInfo<?>> getDefaultTraitInfos() {
        return Arrays.asList(RoleTrait1Info.instance, RoleTrait2Info.instance);
    }

    /**
     * 특성 1번 정보 클래스.
     */
    private static final class RoleTrait1Info extends TraitInfo<RoleTrait1> {
        /** 이동속도 증가량 */
        private static final int SPEED = 20;
        /** 감지 범위 (단위: 블록) */
        private static final int DETECT_RADIUS = 20;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(RoleTrait1.class, "역할: 지원 - 1",
                    new AbilityInfoLore(AbilityInfoLore.Section
                            .builder("체력이 절반 이하인 아군이 범위 밖에 있을 때 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                            .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                            .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                            .build()));
        }
    }

    /**
     * 특성 1번 클래스.
     */
    private static final class RoleTrait1 extends Trait {
        /** 수정자 */
        private static final Modifier MODIFIER = new Modifier(RoleTrait1Info.SPEED);

        public RoleTrait1(@NonNull CombatUser combatUser, @NonNull RoleTrait1Info traitInfo) {
            super(combatUser, traitInfo);
        }

        private void onTick(long i) {
            if (i % 5 == 0) {
                boolean isActive = !CombatEntityRegistry.getCombatEntities(combatUser.getLocation().getWorld(), EntityCondition.team(combatUser)
                        .exclude(combatUser).and(combatEntity -> combatEntity.isGoalTarget()
                                && combatEntity.getDamageModule().isHalfHealth()
                                && combatEntity.getLocation().distance(combatUser.getLocation()) >= RoleTrait1Info.DETECT_RADIUS)).isEmpty();

                if (isActive)
                    combatUser.getMoveModule().addModifier(MODIFIER);
                else
                    combatUser.getMoveModule().removeModifier(MODIFIER);
            }
        }
    }

    /**
     * 특성 2번 정보 클래스.
     */
    private static final class RoleTrait2Info extends TraitInfo<RoleTrait2> {
        /** 초당 치유량 */
        private static final int HEAL_PER_SECOND = 50;
        /** 지속시간 */
        private static final Timespan DURATION = Timespan.ofSeconds(3);

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(RoleTrait2.class, "역할: 지원 - 2",
                    new AbilityInfoLore(AbilityInfoLore.Section
                            .builder("아군을 치유하면 일정 시간동안 <:HEAL:회복>합니다.")
                            .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION.toSeconds())
                            .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL_PER_SECOND)
                            .build()));
        }
    }

    /**
     * 특성 2번 클래스.
     */
    private static final class RoleTrait2 extends Trait {
        /** 마지막 치유 시점 */
        private Timestamp lastGiveHealTimestamp = Timestamp.now();

        public RoleTrait2(@NonNull CombatUser combatUser, @NonNull RoleTrait2Info traitInfo) {
            super(combatUser, traitInfo);
        }

        private void onTick() {
            if (lastGiveHealTimestamp.plus(RoleTrait2Info.DURATION).isAfter(Timestamp.now()))
                combatUser.getHealModule().heal(combatUser, RoleTrait2Info.HEAL_PER_SECOND / 20.0, false);
        }

        private void onGiveHeal(@NonNull Healable target) {
            if (combatUser != target)
                lastGiveHealTimestamp = Timestamp.now();
        }
    }

    /**
     * 아군 하이라이트 타겟팅 히트스캔 클래스.
     */
    protected static final class TeamTarget extends Target<Healable> {
        /**
         * 아군 하이라이트 타겟팅 히트스캔 인스턴스를 생성한다.
         *
         * @param combatUser  발사자
         * @param maxDistance 최대 사거리. (단위: 블록). 0 이상의 값
         */
        public TeamTarget(@NonNull CombatUser combatUser, double maxDistance) {
            super(combatUser, maxDistance, EntityCondition.team(combatUser).exclude(combatUser));
        }

        @Override
        protected void onFindEntity(@NonNull Healable target) {
            ((CombatUser) shooter).setGlowing(target, Timespan.ofTicks(3));
        }
    }
}
