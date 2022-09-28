package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.Bullet;
import com.dace.dmgr.combat.Combat;
import com.dace.dmgr.combat.action.Weapon;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.ICombatEntity;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.util.SoundPlayer;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ArkaceWeapon extends Weapon {
    public static final int DAMAGE = 75;
    public static final int DAMAGE_DISTANCE = 25;
    private static final ArkaceWeapon instance = new ArkaceWeapon();

    public ArkaceWeapon() {
        super("HLN-12", ItemBuilder.fromCSItem("HLN-12").setLore(
                "§f",
                "§f뛰어난 안정성을 가진 전자동 돌격소총입니다.",
                "§7사격§f하여 \u4DC0 §c피해§f를 입힙니다.",
                "§f").build());
    }

    public static ArkaceWeapon getInstance() {
        return instance;
    }

    @Override
    public void use(CombatUser combatUser, WeaponController weaponController) {
        SoundPlayer.play("random.gun2.scarlight_1", combatUser.getEntity().getLocation(), 3F, 1F);
        SoundPlayer.play("random.gun_reverb", combatUser.getEntity().getLocation(), 5F, 1.2F);
        new Bullet(combatUser, 7) {
            @Override
            public void trail(Location location) {
                Location trailLoc = location.add(VectorUtil.getPitchAxis(location).multiply(-0.2)).add(0, -0.2, 0);
                location.getWorld().spawnParticle(Particle.CRIT, trailLoc, 1, 0, 0, 0, 0);
            }

            @Override
            public void onHitEntity(Location location, ICombatEntity target) {
                Combat.attack(combatUser, target, DAMAGE, "", false, false);
            }
        }.shoot();
    }
}
