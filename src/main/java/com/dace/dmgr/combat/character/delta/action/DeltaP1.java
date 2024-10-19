package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import lombok.NonNull;
import org.bukkit.entity.Player;

public final class DeltaP1 extends AbstractSkill {
    private static final String MODIFIER_ID = "DeltaP1";


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
        return super.canUse(actionKey) && !isEnemyNearby();
    }

    /**
     * 감지 범위 이내에 적이 있는지 체크합니다.
     * @return 적이 있는지 여부
     */
    public boolean isEnemyNearby() {
        CombatEntity enemy = CombatUtil.getNearCombatEntity(
                combatUser.getGame(), combatUser.getCenterLocation(), DeltaP1Info.DETECT_RADIUS, combatUser::isEnemy);

        return enemy != null;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        for (Player player: combatUser.getEntity().getWorld().getPlayers()) {
            CombatUser otherCombatUser = CombatUser.fromUser(User.fromPlayer(player));
            if (otherCombatUser == null)
                continue;
            if (combatUser.isEnemy(otherCombatUser))
                player.hidePlayer(DMGR.getPlugin(), combatUser.getEntity());
        }

        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, DeltaP1Info.SPEED_INCREMENT);
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);

        combatUser.getUser().sendAlert("암호화가 해제되었습니다!");
        combatUser.stopHiding();
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 델타의 '암호화' 패시브 스킬을 사용 중이라면 해제하고 쿨타임을 리셋시킵니다.
     * @param combatUser 사용자
     */
    public static void cancelAndReset(CombatUser combatUser) {
        DeltaP1 skill = combatUser.getSkill(DeltaP1Info.getInstance());
        if (!skill.isDurationFinished() && skill.isCancellable()) {
            skill.onCancelled();
        }
        skill.setCooldown();
    }
}
