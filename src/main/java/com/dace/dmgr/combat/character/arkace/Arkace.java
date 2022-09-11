package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.Bullet;
import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.HasCSWeapon;
import com.dace.dmgr.combat.character.HasSprintEvent;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.util.SoundPlayer;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Arkace extends Character implements HasCSWeapon, HasSprintEvent {
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", Weapon.ARKACE, new ArkaceStats(), "DVArkace");
    }

    public static Arkace getInstance() {
        return instance;
    }

    @Override
    public void useWeaponShoot(CombatUser combatUser) {
        SoundPlayer.play("random.gun2.scarlight_1", combatUser.getEntity(), 3F, 1F);
        SoundPlayer.play("random.gun_reverb", combatUser.getEntity(), 5F, 1.2F);
        new Bullet(combatUser, 7) {
            @Override
            public void trail(Location location) {
                Location trailLoc = location.add(VectorUtil.getPitchAxis(location).multiply(-0.2)).add(0, -0.2, 0);
                location.getWorld().spawnParticle(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
            }

            @Override
            public void onHitEntity(Location location, ICombatEntity target) {
                Combat.attack(combatUser, target, ArkaceStats.Normal.DAMAGE, "", false, false);
            }
        }.shoot();
    }

    @Override
    public void onSprintToggle(CombatUser combatUser, boolean sprint) {
        if (sprint)
            combatUser.addSpeedIncrement(ArkaceStats.Passive1.SPRINT_SPEED);
        else
            combatUser.addSpeedIncrement(-ArkaceStats.Passive1.SPRINT_SPEED);
    }

    @Override
    public void useWeaponLeft(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("left click");
    }

    @Override
    public void useWeaponRight(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("right click");
    }

    @Override
    public void useSkill1(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("skill 1");
    }

    @Override
    public void useSkill2(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("skill 2");
    }

    @Override
    public void useSkill3(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("skill 3");
    }

    @Override
    public void useUltimate(CombatUser combatUser) {
        combatUser.getEntity().sendMessage("ultimate");
    }
}
