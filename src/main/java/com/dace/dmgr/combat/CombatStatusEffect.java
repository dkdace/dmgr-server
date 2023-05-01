package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;

/**
 * 전투 시스템의 상태 효과 기능을 제공하는 클래스.
 */
public class CombatStatusEffect {
    /**
     * 피격자에게 기절 효과를 적용한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     */
    public static void stun(CombatUser attacker, CombatEntity<?> victim, long duration) {
        final long finalDuration = getFinalDuration(attacker, victim, duration);

        if (victim instanceof CombatUser)
            ((CombatUser) victim).getEntity().getInventory().setHeldItemSlot(8);

        if (CooldownManager.getCooldown(victim, Cooldown.STUN) == 0) {
            CooldownManager.setCooldown(victim, Cooldown.STUN, finalDuration);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(victim, Cooldown.STUN);
                    if (combatEntityMap.get(victim.getEntity()) == null || cooldown <= 0)
                        return false;

                    if (victim instanceof CombatUser)
                        ((CombatUser) victim).getEntity().sendTitle(SUBTITLES.STUN,
                                StringFormUtil.getProgressBar(cooldown, finalDuration, ChatColor.RED), 0, 2, 10);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    if (victim instanceof CombatUser)
                        ((CombatUser) victim).getEntity().getInventory().setHeldItemSlot(4);
                }
            };
        } else if (CooldownManager.getCooldown(victim, Cooldown.STUN) < finalDuration)
            CooldownManager.setCooldown(victim, Cooldown.STUN, finalDuration);
    }

    /**
     * 피격자에게 속박 효과를 적용한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     */
    public static void snare(CombatUser attacker, CombatEntity<?> victim, long duration) {
        final long finalDuration = getFinalDuration(attacker, victim, duration);

        if (CooldownManager.getCooldown(victim, Cooldown.SNARE) == 0) {
            CooldownManager.setCooldown(victim, Cooldown.SNARE, finalDuration);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(victim, Cooldown.SNARE);
                    if (combatEntityMap.get(victim.getEntity()) == null || cooldown <= 0)
                        return false;

                    if (victim instanceof CombatUser)
                        ((CombatUser) victim).getEntity().sendTitle(SUBTITLES.SNARE,
                                StringFormUtil.getProgressBar(cooldown, finalDuration, ChatColor.RED), 0, 2, 10);

                    return true;
                }
            };
        } else if (CooldownManager.getCooldown(victim, Cooldown.SNARE) < finalDuration)
            CooldownManager.setCooldown(victim, Cooldown.SNARE, finalDuration);
    }

    /**
     * 피격자에게 고정 효과를 적용한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     */
    public static void grounding(CombatUser attacker, CombatEntity<?> victim, long duration) {
        final long finalDuration = getFinalDuration(attacker, victim, duration);

        if (CooldownManager.getCooldown(victim, Cooldown.GROUNDING) == 0) {
            CooldownManager.setCooldown(victim, Cooldown.GROUNDING, finalDuration);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(victim, Cooldown.GROUNDING);
                    if (combatEntityMap.get(victim.getEntity()) == null || cooldown <= 0)
                        return false;

                    if (victim instanceof CombatUser)
                        ((CombatUser) victim).getEntity().sendTitle(SUBTITLES.GROUNDING,
                                StringFormUtil.getProgressBar(cooldown, finalDuration, ChatColor.RED), 0, 2, 10);

                    return true;
                }
            };
        } else if (CooldownManager.getCooldown(victim, Cooldown.GROUNDING) < finalDuration)
            CooldownManager.setCooldown(victim, Cooldown.GROUNDING, finalDuration);
    }

    /**
     * 피격자에게 침묵 효과를 적용한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     */
    public static void silence(CombatUser attacker, CombatEntity<?> victim, long duration) {
        final long finalDuration = getFinalDuration(attacker, victim, duration);

        if (CooldownManager.getCooldown(victim, Cooldown.SILENCE) == 0) {
            CooldownManager.setCooldown(victim, Cooldown.SILENCE, finalDuration);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(victim, Cooldown.SILENCE);
                    if (combatEntityMap.get(victim.getEntity()) == null || cooldown <= 0)
                        return false;

                    if (victim instanceof CombatUser) {
                        ((CombatUser) victim).getEntity().sendTitle(SUBTITLES.SILENCE,
                                StringFormUtil.getProgressBar(cooldown, finalDuration, ChatColor.DARK_PURPLE), 0, 2, 10);
                        ((CombatUser) victim).getEntity().stopSound("");
                    }

                    return true;
                }
            };
        } else if (CooldownManager.getCooldown(victim, Cooldown.SILENCE) < finalDuration)
            CooldownManager.setCooldown(victim, Cooldown.SILENCE, finalDuration);
    }

    /**
     * 피격자에게 불을 붙여 지속 피해를 입힌다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     * @param damage   초당 피해량
     * @param type     타입
     * @param isUlt    궁극기 충전 여부
     */
    public static void burn(CombatUser attacker, CombatEntity<?> victim, long duration, int damage, String type, boolean isUlt) {
        final long finalDuration = getFinalDuration(attacker, victim, duration);

        if (CooldownManager.getCooldown(victim, Cooldown.BURN, type) == 0) {
            CooldownManager.setCooldown(victim, Cooldown.BURN, type, finalDuration);

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(victim, Cooldown.BURN, type);
                    if (combatEntityMap.get(victim.getEntity()) == null || cooldown <= 0)
                        return false;

                    victim.getEntity().setFireTicks(6);
                    if (i % 5 == 0)
                        attacker.attack(victim, damage / 4, "burn-" + type, false, isUlt);

                    return true;
                }
            };
        } else if (CooldownManager.getCooldown(victim, Cooldown.BURN, type) < finalDuration)
            CooldownManager.setCooldown(victim, Cooldown.BURN, type, finalDuration);
    }

    /**
     * 각종 변수를 계산하여 최종 상태 효과 지속시간을 반환한다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     * @param duration 지속시간 (tick)
     * @return 최종 지속시간 (tick)
     */
    private static long getFinalDuration(CombatUser attacker, CombatEntity<?> victim, long duration) {
        int bonus = 0;

        return duration * (100 + bonus) / 100;
    }

    /**
     * 상태 효과에 사용되는 자막(Subtitle) 종류.
     */
    private static class SUBTITLES {
        /** 기절 */
        static final String STUN = "§c§l기절함!";
        /** 속박 */
        static final String SNARE = "§c§l속박당함!";
        /** 고정 */
        static final String GROUNDING = "§c§l고정당함!";
        /** 침묵 */
        static final String SILENCE = "§5§l침묵당함!";
    }
}
