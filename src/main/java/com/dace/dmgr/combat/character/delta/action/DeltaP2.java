package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.util.GlowUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class DeltaP2 extends AbstractSkill {
    public DeltaP2(CombatUser combatUser) {
        super(combatUser, DeltaP2Info.getInstance());
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.PERIODIC_2};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }
    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        CombatEntity[] targets = CombatUtil.getNearCombatEntities(
                combatUser.getGame(), combatUser.getCenterLocation(), DeltaP2Info.DETECT_RADIUS, this::isTarget);

        for (CombatEntity target: targets) {
            GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, combatUser.getEntity(), 5);
        }
    }

    private boolean isTarget(CombatEntity combatEntity) {
        if (!(combatEntity instanceof Damageable))
            return false;

        DamageModule damageModule = ((Damageable) combatEntity).getDamageModule();

        return combatEntity.isEnemy(combatUser)
                && damageModule.getHealth() <= damageModule.getMaxHealth() / 2;
    }
}

