package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
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

        // 감지 범위 내에 있는 적 엔티티에 발광 부여
        Arrays.stream(combatUser.getGame().getAllCombatEntities())
                .filter(combatEntity -> !combatEntity.getTeamIdentifier().equals(combatUser.getTeamIdentifier()))
                .filter(combatEntity -> combatUser.getCenterLocation().distance(combatEntity.getCenterLocation())
                        <= DeltaP2Info.DETECT_RADIUS)
                .forEach(combatEntity -> GlowUtil.setGlowing(
                        combatEntity.getEntity(), ChatColor.RED, combatUser.getEntity(), 1));
    }
}

