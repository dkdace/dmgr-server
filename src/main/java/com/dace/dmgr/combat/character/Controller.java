package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.GlowUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * 역할군이 '제어'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Controller extends Character {
    /** 회복 쿨타임 ID */
    private static final String HEAL_COOLDOWN_ID = "RoleTrait2Heal";

    /**
     * 제어 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param subRole          부 역할군
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Controller(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty,
                         int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.CONTROLLER, subRole, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0 && combatUser.getGame() != null && combatUser.getGameUser() != null && combatUser.getGameUser().getTeam() != null) {
            Arrays.stream(combatUser.getGameUser().getTeam().getTeamUsers())
                    .map(gameUser -> CombatUser.fromUser(gameUser.getUser()))
                    .filter(target -> target != null && target != combatUser && target.getDamageModule().isLowHealth())
                    .forEach(target -> {
                        CombatEntity targetCombatEntity = CombatUtil.getNearCombatEntity(combatUser.getGame(), target.getEntity().getLocation(),
                                RoleTrait1Info.DETECT_RADIUS, combatEntity -> combatEntity instanceof CombatUser && combatEntity.isEnemy(combatUser));

                        if (targetCombatEntity != null)
                            GlowUtil.setGlowing(targetCombatEntity.getEntity(), ChatColor.RED, combatUser.getEntity(), 10);
                    });
        }

        if (CooldownUtil.getCooldown(combatUser, HEAL_COOLDOWN_ID) == 0)
            combatUser.getDamageModule().heal(combatUser, RoleTrait2Info.HEAL_PER_SECOND / 20, false);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CooldownUtil.setCooldown(victim, HEAL_COOLDOWN_ID, RoleTrait2Info.ACTIVATE_DURATION);
    }

    @Override
    @Nullable
    public final TraitInfo getTraitInfo(int number) {
        if (number == 1)
            return RoleTrait1Info.instance;
        else if (number == 2)
            return RoleTrait2Info.instance;

        return getCharacterTraitInfo(number - 2);
    }

    /**
     * @see Character#getTraitInfo(int)
     */
    @Nullable
    public abstract TraitInfo getCharacterTraitInfo(int number);

    public static final class RoleTrait1Info extends TraitInfo {
        /** 감지 범위 */
        public static final int DETECT_RADIUS = 10;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 제어 - 1",
                    "",
                    "§f▍ 치명상인 아군 근처의 적을 탐지합니다.",
                    "",
                    MessageFormat.format("§f{0} {1}m", TextIcon.RADIUS, DETECT_RADIUS));
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 40;
        /** 활성화 시간 (tick) */
        public static final long ACTIVATE_DURATION = 4 * 20;
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 제어 - 2",
                    "",
                    "§f▍ 일정 시간동안 피해를 받지 않으면 §a" + TextIcon.HEAL + " 회복§f합니다.",
                    "",
                    MessageFormat.format("§7{0} §f{1}초", TextIcon.DURATION, ACTIVATE_DURATION / 20.0),
                    MessageFormat.format("§a{0} §f{1}/초", TextIcon.HEAL, HEAL_PER_SECOND));
        }
    }
}
