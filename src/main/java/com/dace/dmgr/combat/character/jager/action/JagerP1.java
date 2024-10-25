package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.GlowUtil;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;

@Setter
public final class JagerP1 extends AbstractSkill {
    /** 현재 사용 대상 */
    private Damageable target = null;

    public JagerP1(@NonNull CombatUser combatUser) {
        super(combatUser, JagerP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
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
        if (target.getDamageModule().isLiving())
            GlowUtil.setGlowing(target.getEntity(), ChatColor.RED, combatUser.getEntity(), JagerP1Info.DURATION);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
