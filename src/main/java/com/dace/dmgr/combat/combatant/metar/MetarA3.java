package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Consumer;

public final class MetarA3 extends ActiveSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @Getter
    private final BonusScoreModule bonusScoreModule;

    /** 활성화 완료 여부 */
    private boolean isEnabled = false;
    /** 현재 투사체 */
    @Nullable
    private MetarA3Projectile projectile;

    public MetarA3(@NonNull CombatUser combatUser) {
        super(combatUser, MetarA3Info.getInstance(), MetarA3Info.COOLDOWN, Timespan.MAX, 2);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", MetarA3Info.ASSIST_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return MetarA3Info.getInstance() + ActionBarStringUtil.getKeyInfo(this, "격발");
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            forceCancel();
            MetarA3Info.SOUND.DETONATE.play(combatUser.getLocation());

            return;
        }

        setDuration();

        combatUser.getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarA3Info.READY_DURATION);

        MetarA3Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;

            MetarA3Info.SOUND.USE_READY.play(combatUser.getLocation());

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            projectile = new MetarA3Projectile();
            projectile.shot(loc);
        }, MetarA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        isEnabled = false;
        setDuration(Timespan.ZERO);

        if (projectile != null && !projectile.isDestroyed())
            projectile.destroy();
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class MetarA3Projectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets = new HashSet<>();

        private MetarA3Projectile() {
            super(MetarA3.this, MetarA3Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(MetarA3Info.SIZE).duration(MetarA3Info.EXPLODE_DURATION).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            projectile = null;
            if (isEnabled)
                forceCancel();

            Location[] locs = new Location[30];
            Vector[] vecs = new Vector[30];

            for (int i = 0; i < locs.length; i++) {
                Vector vec = new Vector(Math.random() - Math.random(), Math.random() - Math.random(), Math.random() - Math.random())
                        .normalize().multiply(MetarA3Info.RADIUS);
                locs[i] = location.clone().add(vec);
                vecs[i] = vec.normalize().multiply(-0.35);
            }

            addTask(new IntervalTask(i -> {
                Location loc = location.clone().add(0, 0.1, 0);
                new MetarA3Area().emit(loc);

                for (int j = 0; j < locs.length; j++) {
                    Vector vec = vecs[j];
                    MetarA3Info.PARTICLE.DETONATE_TICK_DECO.play(locs[j].add(vec), i / 13.0, vec);
                }

                MetarA3Info.PARTICLE.DETONATE_TICK_CORE.play(loc);
                MetarA3Info.SOUND.DETONATE_TICK.play(loc, 1, i / 13.0);
            }, 1, MetarA3Info.DURATION.toTicks()));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, new Consumer<Location>() {
                private int i = 0;

                @Override
                public void accept(Location location) {
                    MetarA3Info.PARTICLE.BULLET_TRAIL.play(location);
                    if (i % 4 == 0)
                        MetarA3Info.SOUND.TICK.play(location);

                    i++;
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                setVelocity(new Vector(0, 0, 0));
                return true;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> false;
        }

        private final class MetarA3Area extends Area<Damageable> {
            private MetarA3Area() {
                super(combatUser, MetarA3Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return true;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(MetarA3Projectile.this, 0, DamageType.NORMAL, null, false, true)) {
                    if (target instanceof Movable) {
                        Vector dir = LocationUtil.getDirection(target.getLocation(), center).multiply(MetarA3Info.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir, true);
                    }

                    if (targets.add(target)) {
                        target.getDamageModule().damage(MetarA3Projectile.this, 1, DamageType.NORMAL, null, false, true);

                        if (target != combatUser && target instanceof CombatUser) {
                            combatUser.addScore("적 끌어당김", MetarA3Info.EFFECT_SCORE);
                            bonusScoreModule.addTarget((CombatUser) target, MetarA3Info.ASSIST_SCORE_TIME_LIMIT);
                        }
                    }
                }

                return true;
            }
        }
    }
}
