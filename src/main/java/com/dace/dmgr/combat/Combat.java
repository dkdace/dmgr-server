package com.dace.dmgr.combat;

import com.comphenix.packetwrapper.WrapperPlayServerEntityStatus;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Dummy;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.RegionUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dace.dmgr.system.HashMapList.combatEntityMap;
import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class Combat {
    public static final int DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    public static final int FASTKILL_TIME_LIMIT = (int) 2.5 * 20;
    public static final int RESPAWN_TIME = 10 * 20;

    public static boolean isEnemy(ICombatEntity attacker, ICombatEntity victim) {
        return !attacker.getTeam().equals(victim.getTeam());
    }

    public static ICombatEntity getNearEnemy(ICombatEntity attacker, Location location, float range) {
        return combatEntityMap.values().stream()
                .min(Comparator.comparing(combatEntity ->
                        location.distance(combatEntity.getHitbox().getCenter())))
                .filter(combatEntity ->
                        combatEntity != attacker && isEnemy(attacker, combatEntity) &&
                                LocationUtil.isInHitbox(location, combatEntity.getHitbox(), range))
                .orElse(null);
    }

    public static Set<ICombatEntity> getNearEnemies(ICombatEntity attacker, Location location, float range) {
        return combatEntityMap.values().stream()
                .filter(combatEntity ->
                        combatEntity != attacker && isEnemy(attacker, combatEntity) &&
                                LocationUtil.isInHitbox(location, combatEntity.getHitbox(), range))
                .collect(Collectors.toSet());
    }

    public static void attack(CombatUser attacker, ICombatEntity victim, int damage, String type, boolean crit, boolean ult) {
        Player attackerEntity = attacker.getEntity();
        Entity victimEntity = victim.getEntity();
        boolean killed = false;

        if (!victimEntity.isDead()) {
            int rdamage = damage;

            if (victim instanceof CombatUser)
                if (((Player) victimEntity).getGameMode() != GameMode.SURVIVAL)
                    return;
            if (victimEntity.getType() != EntityType.ZOMBIE && victimEntity.getType() != EntityType.PLAYER)
                crit = false;
            if (crit)
                damage *= 1.5;

            int atkBonus = 0;
            int defBonus = 0;

            damage = damage * (100 + atkBonus - defBonus) / 100;

            if (attacker != victim) {
                if (crit) {
                    attackerEntity.sendTitle("", SUBTITLES.CRIT, 0, 2, 10);
                    SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.9F, attackerEntity);
                    SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.35F, 0F, attackerEntity);
                } else {
                    attackerEntity.sendTitle("", SUBTITLES.HIT, 0, 2, 10);
                    SoundUtil.play("random.stab", 0.4F, 2F, attackerEntity);
                    SoundUtil.play(Sound.ENTITY_GENERIC_SMALL_FALL, 0.4F, 1.5F, attackerEntity);
                }
            }

            if (CooldownManager.getCooldown(victim, Cooldown.DAMAGE_ANIMATION) == 0) {
                CooldownManager.setCooldown(victim, Cooldown.DAMAGE_ANIMATION);
                sendDamage(victimEntity);
            }

            if (RegionUtil.isInRegion(attackerEntity.getPlayer(), "BattleTrain") && victim.getHealth() - damage <= 0)
                victim.setHealth(1);
            else {
                if (victim.getHealth() - damage <= 0) killed = true;
                else victim.setHealth(victim.getHealth() - damage);
            }

            if ((victim instanceof CombatUser || victim instanceof Dummy) && attacker != victim) {
                if (ult)
                    if (!attacker.getSkillController(attacker.getCharacter().getUltimate()).isUsing())
                        attacker.addUlt((float) damage / attacker.getCharacter().getUltimate().getCost());

                if (victim instanceof CombatUser) {
                    if (CooldownManager.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, victimEntity.getEntityId()) == 0) {
                        CooldownManager.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT, victimEntity.getEntityId());
                    }
                    CooldownManager.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, victimEntity.getEntityId());

                    float sumDamage = ((CombatUser) victim).getDamageMap().getOrDefault(attacker, 0F);
                    if (killed)
                        ((CombatUser) victim).getDamageMap().put(attacker, sumDamage + (float) victim.getHealth() / victim.getMaxHealth());
                    else
                        ((CombatUser) victim).getDamageMap().put(attacker, sumDamage + (float) damage / victim.getMaxHealth());
                    if (sumDamage > 1)
                        ((CombatUser) victim).getDamageMap().put(attacker, 1F);
                }
            }

            if (killed && !RegionUtil.isInRegion(victimEntity, "BattleTrain")) {
                kill(attacker, victim);
            }
        }
    }

    private static void kill(CombatUser attacker, ICombatEntity victim) {
        Player attackerEntity = attacker.getEntity();
        Entity victimEntity = victim.getEntity();

        if (victim instanceof CombatUser) {
            victim.setHealth(victim.getMaxHealth());

            if (CooldownManager.getCooldown((CombatUser) victim, Cooldown.RESPAWN_TIME) == 0) {
                Map<CombatUser, Float> damageList = ((CombatUser) victim).getDamageMap();
                Set<String> attackerNames = damageList.keySet().stream().map((CombatUser _attacker) ->
                        "§f　§l" + attacker.getName()).collect(Collectors.toSet());
                String victimName = "§f　§l" + victim.getName();

                damageList.forEach((CombatUser _attacker, Float damage) -> {
                    Player _attackerEntity = _attacker.getEntity();

                    int score = Math.round(damage * 100);

                    _attackerEntity.sendTitle("", SUBTITLES.KILL_PLAYER, 0, 2, 10);
                    if (score > 30) {
                        _attackerEntity.sendMessage(DMGR.PREFIX.CHAT + "§e§n" + victim.getName() + "§f 처치 §a§l[+" + score + "]");
                    } else {
                        _attackerEntity.sendMessage(DMGR.PREFIX.CHAT + "§e§n" + victim.getName() + "§f 처치 도움 §a§l[+" + score + "]");
                    }
                    playKillSound(_attackerEntity);
                });

                if (damageList.size() > 0) {
                    Bukkit.getServer().broadcastMessage(DMGR.PREFIX.CHAT +
                            String.join(" ,", attackerNames) + " §4§l-> " + victimName);

                    damageList.clear();
                }
                respawn(attacker, (CombatUser) victim);
            }
        } else {
            attackerEntity.sendTitle("", SUBTITLES.KILL_ENTITY, 0, 2, 10);
            playKillSound(attackerEntity);

            ((TemporalEntity<?>) victim).remove();
        }
    }

    private static void respawn(CombatUser attacker, CombatUser victim) {
        Player attackerEntity = attacker.getEntity();
        Player victimEntity = victim.getEntity();

        Location deadLocation = victimEntity.getLocation().add(0, 0.5, 0);
        deadLocation.setPitch(90);

        CooldownManager.setCooldown(victim, Cooldown.RESPAWN_TIME);
        victimEntity.setGameMode(GameMode.SPECTATOR);
        victimEntity.setVelocity(new Vector());

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                long cooldown = CooldownManager.getCooldown(victim, Cooldown.RESPAWN_TIME);
                if (combatUserMap.get(victimEntity) == null || cooldown <= 0)
                    return false;

                victimEntity.sendTitle("§c§l죽었습니다!",
                        String.format("%.1f", (float) cooldown / 20F) + "초 후 부활합니다.", 0, 20, 10);
                victimEntity.teleport(deadLocation);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                victim.setHealth(victim.getMaxHealth());
                victimEntity.teleport(Lobby.lobby);
                victimEntity.setGameMode(GameMode.SURVIVAL);
            }
        };
    }

    public static void heal(CombatUser attacker, ICombatEntity victim, int amount, boolean ult) {
        if (victim.getHealth() == victim.getMaxHealth())
            return;

        int bonus = 0;

        amount = amount * (100 + bonus) / 100;
        victim.setHealth(victim.getHealth() + amount);

        if (amount > 100)
            ParticleUtil.play(Particle.HEART, LocationUtil.setRelativeOffset(victim.getEntity().getLocation(),
                            0, victim.getEntity().getHeight() + 0.3, 0), (int) Math.ceil(amount / 100F),
                    0.3F, 0.1F, 0.3F, 0);
        else if (amount / 100F > Math.random()) {
            ParticleUtil.play(Particle.HEART, LocationUtil.setRelativeOffset(victim.getEntity().getLocation(),
                    0, victim.getEntity().getHeight() + 0.3, 0), 1, 0.3F, 0.1F, 0.3F, 0);
        }

        if (ult)
            attacker.addUlt((float) amount / attacker.getCharacter().getUltimate().getCost());
    }

    private static void sendDamage(Entity entity) {
        WrapperPlayServerEntityStatus packet = new WrapperPlayServerEntityStatus();

        packet.setEntityID(entity.getEntityId());
        packet.setEntityStatus((byte) 2);

        packet.broadcastPacket();
    }

    private static void playKillSound(Player player) {
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.25F, player);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.25F, player);
    }

    private static class SUBTITLES {
        static final String HIT = "§f×";
        static final String CRIT = "§c§l×";
        static final String KILL_PLAYER = "§c§lKILL";
        static final String KILL_ENTITY = "§c✔";
    }
}
