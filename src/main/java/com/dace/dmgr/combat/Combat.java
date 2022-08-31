package com.dace.dmgr.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.combat.entity.TemporalEntity;
import com.dace.dmgr.user.Lobby;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownManager;
import com.dace.dmgr.util.RegionUtil;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dace.dmgr.system.EntityList.combatEntityList;
import static com.dace.dmgr.system.EntityList.combatUserList;

public class Combat {
    public static final float HITS_HITBOX = 0.15F;
    public static final float PROJ_HITBOX = 0.3F;
    public static final float MELEE_HITBOX = 0.6F;
    public static final int DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    public static final int FASTKILL_TIME_LIMIT = (int) 2.5 * 20;
    public static final int RESPAWN_TIME = 10 * 20;

    public static boolean isEnemy(ICombatEntity attacker, ICombatEntity victim) {
        return !attacker.getTeam().equals(victim.getTeam());
    }

    public static ICombatEntity getNearEnemy(ICombatEntity attacker, Location location, float range) {
        ICombatEntity retTarget = null;
        double dist = range;

        for (Entity entity : attacker.getEntity().getWorld().getNearbyEntities(location, range, range, range)) {
            ICombatEntity target = combatEntityList.get(entity.getEntityId());

            if (target != null) {
                if (target != attacker && isEnemy(attacker, target)) {
                    Location eLocation = entity.getLocation();
                    double hitboxWidth = entity.getWidth();
                    double hitboxHeight = entity.getHeight();

                    if (Math.abs(eLocation.getPitch()) > 35)
                        hitboxHeight -= 0.1;
                    if (Math.abs(eLocation.getPitch()) > 70)
                        hitboxHeight -= 0.1;
                    if (entity.getType() == EntityType.PLAYER) {
                        if (((Player) entity).isSneaking())
                            hitboxHeight -= 0.35;

                        float statHitbox = ((CombatUser) entity).getCharacter().getStats().getHitbox();
                        hitboxWidth += statHitbox - 1.0;
                        hitboxHeight += statHitbox - 1.0;
                    } else if (entity.getType() == EntityType.IRON_GOLEM) {
                        hitboxWidth += 0.3;
                        hitboxHeight += 1.5;
                    }

                    eLocation.setY(location.getY());
                    if (eLocation.getY() > entity.getLocation().add(0, hitboxHeight, 0).getY())
                        eLocation.setY(entity.getLocation().add(0, hitboxHeight, 0).getY());
                    if (eLocation.getY() < entity.getLocation().getY())
                        eLocation.setY(entity.getLocation().getY());

                    Vector v = location.toVector().subtract(eLocation.toVector());
                    v.normalize().multiply((hitboxWidth / 2) + 0.1);
                    eLocation.add(v);

                    if (dist >= location.distance(eLocation)) {
                        dist = location.distance(eLocation);
                        retTarget = combatEntityList.get(entity.getEntityId());

                    }

                }
            }
        }
        return retTarget;
    }

    public static void attack(CombatUser attacker, ICombatEntity victim, int damage, String type, boolean crit, boolean ult) {
        Player attackerEntity = attacker.getEntity();
        LivingEntity victimEntity = victim.getEntity();
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
                    SoundPlayer.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, attackerEntity, 0.6F, 1.9F);
                    SoundPlayer.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, attackerEntity, 0.35F, 0F);
                } else {
                    attackerEntity.sendTitle("", SUBTITLES.HIT, 0, 2, 10);
                    SoundPlayer.play("random.stab", attackerEntity, 0.4F, 2F);
                    SoundPlayer.play(Sound.ENTITY_GENERIC_SMALL_FALL, attackerEntity, 0.4F, 1.5F);
                }
            }

            if (CooldownManager.getCooldown(attacker, Cooldown.DAMAGE_ANIMATION) == 0) {
                CooldownManager.setCooldown(attacker, Cooldown.DAMAGE_ANIMATION);
                sendDamage(victimEntity);
            }

            if (RegionUtil.isInRegion(attackerEntity.getPlayer(), "BattleTrain") && victim.getHealth() - damage <= 0)
                victim.setHealth(1);
            else {
                if (victim.getHealth() - damage <= 0) killed = true;
                else victim.setHealth(victim.getHealth() - damage);
            }

            if (attacker != victim && victim instanceof CombatUser) {
                if (ult)
                    attacker.addUlt((float) damage / attacker.getCharacter().getStats().getUltimate().getCost());

                if (CooldownManager.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, victimEntity.getEntityId()) == 0) {
                    CooldownManager.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT, victimEntity.getEntityId());
                }
                CooldownManager.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, victimEntity.getEntityId());

                if (killed)
                    ((CombatUser) victim).getDamageList().put(attacker, (float) victim.getHealth() / victim.getMaxHealth());
                else
                    ((CombatUser) victim).getDamageList().put(attacker, (float) damage / victim.getMaxHealth());
                if (((CombatUser) victim).getDamageList().get(attacker) > 1)
                    ((CombatUser) victim).getDamageList().put(attacker, 1F);
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
                Map<CombatUser, Float> damageList = ((CombatUser) victim).getDamageList();
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
                    respawn(attacker, (CombatUser) victim);
                }
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

        new BukkitRunnable() {
            @Override
            public void run() {
                long cooldown = CooldownManager.getCooldown(victim, Cooldown.RESPAWN_TIME);
                if (combatUserList.get(victimEntity.getUniqueId()) == null || cooldown <= 0) cancel();

                victimEntity.sendTitle("§c§l죽었습니다!",
                        String.format("%.1f", (float) cooldown / 20) + "초 후 부활합니다.", 0, 20, 10);
                victimEntity.teleport(deadLocation);

                if (isCancelled()) {
                    victim.setHealth(victim.getMaxHealth());
                    victimEntity.teleport(Lobby.lobby);
                    victimEntity.setGameMode(GameMode.SURVIVAL);
                }
            }
        }.runTaskTimer(DMGR.getPlugin(), 0, 1);
    }

    private static void sendDamage(Entity entity) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_STATUS);

        packet.getIntegers().writeSafely(0, entity.getEntityId());
        packet.getBytes().writeSafely(0, (byte) 2);
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void playKillSound(Player player) {
        SoundPlayer.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player, 1F, 1.25F);
        SoundPlayer.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player, 0.6F, 1.25F);
    }

    private static class SUBTITLES {
        static final String HIT = "§f×";
        static final String CRIT = "§c§l×";
        static final String KILL_PLAYER = "§c§lKILL";
        static final String KILL_ENTITY = "§c✔";
    }
}
