package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.MultiSummonable;
import com.dace.dmgr.combat.action.skill.StackableSkill;
import com.dace.dmgr.combat.action.skill.module.MultiEntityModule;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
public final class MetarA2 extends StackableSkill implements MultiSummonable<MetarA2.MetarA2Entity> {
    /** 소환 엔티티 모듈 */
    @NonNull
    private final MultiEntityModule<MetarA2Entity> multiEntityModule;

    public MetarA2(@NonNull CombatUser combatUser) {
        super(combatUser, MetarA2Info.getInstance(), MetarA2Info.COOLDOWN, MetarA2Info.STACK_COOLDOWN, Timespan.MAX, MetarA2Info.MAX_STACK, 1);
        this.multiEntityModule = new MultiEntityModule<>(this, MetarA2Info.MAX_STACK);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        addStack(-1);

        MetarA2Info.Sounds.USE.play(combatUser.getLocation());

        Location loc = combatUser.getLocation().add(0, 0.75, 0);
        multiEntityModule.add(new MetarA2Entity(loc));
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 에너지 방벽 클래스.
     */
    public final class MetarA2Entity extends Barrier {
        private MetarA2Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, combatUser.getName() + "의 방벽", combatUser, MetarA2Info.HEALTH, MetarA2Info.DEATH_SCORE,
                    Hitbox.builder(7, 4, 0.3).axisOffsetY(1.5).build());
            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            entity.setHelmet(new ItemBuilder(Material.IRON_HOE).setDamage((short) 2).build());
            entity.setHeadPose(new EulerAngle(Math.toRadians(owner.getLocation().getPitch()), 0, 0));

            Vector dir = getLocation().getDirection().multiply(0.15);

            addTask(new IntervalTask(i -> {
                if (LocationUtil.isNonSolid(getCenterLocation().add(dir)))
                    entity.teleport(getLocation().add(dir));
            }, 1, 10));
            addTask(new DelayTask(() -> onDeath(null), MetarA2Info.DURATION.toTicks()));
        }

        @Override
        public double getWidth() {
            return 7;
        }

        @Override
        public double getHeight() {
            return 3;
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            super.onDamage(attacker, damage, reducedDamage, location, isCrit);

            combatUser.addScore("피해 막음", damage * MetarA2Info.BLOCK_SCORE / MetarA2Info.HEALTH);

            MetarA2Info.Sounds.DAMAGE.play(location == null ? getLocation() : location, 1 + damage * 0.001);
            if (location != null)
                MetarA2Info.Particles.DAMAGE.play(location, damage * 0.04);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            cancel();

            MetarA2Info.Sounds.DEATH.play(getCenterLocation());
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Location loc = LocationUtil.getLocationFromOffset(getCenterLocation(), -2.2 + i * 2.2, -0.9 + j * 1.8, 0);
                    MetarA2Info.Particles.DEATH.play(loc);
                }
            }
        }
    }
}
