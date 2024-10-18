package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Arrays;

public final class DeltaP1 extends AbstractSkill {

    public DeltaP1(@NonNull CombatUser combatUser) {
        super(combatUser, DeltaP1Info.getInstance());
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return DeltaP1Info.ACTIVATE_DURATION;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        CombatEntity enemy = CombatUtil.getNearCombatEntity(
                combatUser.getGame(), combatUser.getCenterLocation(), DeltaP1Info.DETECT_RADIUS, combatUser::isEnemy);

        return super.canUse(actionKey) && enemy == null;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (combatUser.getGame() == null)
            return;

        setDuration();

        for (CombatEntity combatEntity: combatUser.getGame().getAllCombatEntities()) {
            if (combatEntity instanceof CombatUser && combatEntity.isEnemy(combatEntity))
                ((Player) combatEntity.getEntity()).hidePlayer(DMGR.getPlugin(), combatUser.getEntity());
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        combatUser.getUser().sendAlert("암호화가 해제되었습니다!");

        for (Player player: DMGR.getPlugin().getServer().getOnlinePlayers()) {
            combatUser.getEntity().showPlayer(DMGR.getPlugin(), player);
        }
    }

    /**
     * 델타의 '암호화' 패시브 스킬을 사용 중이라면 해제합니다.
     * @param combatUser 사용자
     */
    public static void cancel(CombatUser combatUser) {
        DeltaP1 skill = combatUser.getSkill(DeltaP1Info.getInstance());
        if (!skill.isDurationFinished() && skill.isCancellable())
            skill.onCancelled();
    }
}
