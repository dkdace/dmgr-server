package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.action.ActiveSkill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class ArkaceA1 extends ActiveSkill {
    public static final long COOLDOWN = 7 * 20;
    public static final int DAMAGE_EXPLODE = 100;
    public static final int DAMAGE_DIRECT = 50;
    public static final int VELOCITY = 60;
    public static final float RADIUS = 3.5F;
    private static final ArkaceA1 instance = new ArkaceA1();

    public ArkaceA1() {
        super(1, "다이아코어 미사일",
                "",
                "§f소형 미사일을 3회 연속으로 발사하여 " + TextIcon.DAMAGE + " §c광역 피해",
                "§f를 입힙니다.",
                "",
                "§f" + TextIcon.DAMAGE + "   §c폭발 " + DAMAGE_EXPLODE + " + 직격 " + DAMAGE_DIRECT,
                "§f" + TextIcon.DAMAGE + "✸ §c3.5m",
                "§f" + TextIcon.COOLDOWN + "   §f7초",
                "",
                "§7§l[2] [좌클릭] §f사용");
    }

    public static ArkaceA1 getInstance() {
        return instance;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public void use(CombatUser combatUser, SkillController skillController) {
        if (!skillController.isUsing()) {
            skillController.setDuration(-1);

            WeaponController weaponController = combatUser.getWeaponController();
            weaponController.setCooldown(-1);

            new TaskTimer(5, 3) {
                @Override
                public boolean run(int i) {
                    Location location = LocationUtil.setRelativeOffset(combatUser.getEntity().getEyeLocation(),
                            combatUser.getEntity().getLocation().getDirection(), -0.2, -0.4, 0);
                    SoundUtil.play("random.gun.grenade", location, 3F, 1.5F);
                    SoundUtil.play(Sound.ENTITY_SHULKER_SHOOT, location, 3F, 1.2F);

                    new Projectile(combatUser, false, VELOCITY, 5) {
                        @Override
                        public void trail(Location location) {
                            ParticleUtil.play(Particle.CRIT_MAGIC, location, 1, 0, 0, 0, 0);
                            ParticleUtil.playRGB(location, 1, 0, 0, 0, 32, 250, 225);
                        }

                        @Override
                        public void onHit(Location location) {
                            explode(combatUser, location);
                        }

                        @Override
                        public void onHitEntity(Location location, ICombatEntity target) {
                            Combat.attack(combatUser, target, DAMAGE_DIRECT, "", false, true);
                        }
                    }.shoot(location);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    new TaskWait(4) {
                        @Override
                        public void run() {
                            skillController.setCooldown();
                            weaponController.setCooldown(0);
                        }
                    };
                }
            };
        }
    }

    private void explode(CombatUser combatUser, Location location) {
        SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4F, 0.8F);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4F, 1.4F);
        SoundUtil.play("random.gun_reverb2", location, 6F, 0.9F);
        ParticleUtil.playRGB(location, 180, 2F, 2F, 2F, 32, 250, 225);
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 40, 0.2F, 0.2F, 0.2F, 0.2F);

        if (location.distance(combatUser.getEntity().getLocation()) < RADIUS)
            Combat.attack(combatUser, combatUser, DAMAGE_EXPLODE, "", false, true);
        Combat.getNearEnemies(combatUser, location, RADIUS).forEach(target ->
                Combat.attack(combatUser, target, DAMAGE_EXPLODE, "", false, true));
    }
}
