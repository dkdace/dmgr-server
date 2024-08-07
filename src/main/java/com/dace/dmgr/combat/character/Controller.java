package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.Attacker;
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

/**
 * 역할군이 '제어'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Controller extends Character {
    /** 회복 쿨타임 ID */
    private static final String HEAL_COOLDOWN_ID = "RoleTrait2Heal";

    /**
     * 제어 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Controller(@NonNull String name, @NonNull String skinName, char icon, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, skinName, Role.CONTROLLER, icon, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0 && combatUser.getGame() != null && combatUser.getGameUser() != null) {
            combatUser.getGame().getTeamUserMap().get(combatUser.getGameUser().getTeam()).stream()
                    .map(gameUser -> CombatUser.fromUser(gameUser.getUser()))
                    .filter(combatUser2 -> combatUser2 != null && combatUser2 != combatUser && combatUser2.getDamageModule().isLowHealth())
                    .forEach(combatUser2 -> {
                        CombatUser target = (CombatUser) CombatUtil.getNearCombatEntity(combatUser.getGame(), combatUser2.getEntity().getLocation(),
                                RoleTrait1Info.DETECT_RADIUS, combatEntity -> combatEntity instanceof CombatUser && combatEntity.isEnemy(combatUser));

                        if (target != null)
                            GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, combatUser.getEntity(), 10);
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

    public static final class RoleTrait1Info extends TraitInfo {
        /** 감지 범위 */
        public static final int DETECT_RADIUS = 10;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(1, "역할: 제어 - 1");
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
            super(2, "역할: 지원 - 2");
        }
    }
}
