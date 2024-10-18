package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
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
        setCooldown();

        if (combatUser.getGame() == null)
            return;

        Arrays.stream(combatUser.getGame().getAllCombatEntities())
                .filter(target -> target.isEnemy(combatUser))
                .filter(target -> combatUser.getCenterLocation().distance(target.getCenterLocation())
                        <= DeltaP2Info.DETECT_RADIUS)
                .filter(target -> target instanceof Damageable)
                .map(target -> (Damageable) target)
                .filter(target -> target.getDamageModule().getHealth() <= target.getDamageModule().getMaxHealth() / 2)
                .forEach(combatEntity -> GlowUtil.setGlowing(
                        combatEntity.getEntity(), ChatColor.RED, combatUser.getEntity(), 1));
    }
}

