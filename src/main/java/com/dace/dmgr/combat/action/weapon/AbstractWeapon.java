package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Weapon}의 기본 구현체, 모든 무기의 기반 클래스.
 */
public abstract class AbstractWeapon extends AbstractAction implements Weapon {
    /** 투명 아이템의 내구도 */
    private static final short INVISIBLE_ITEM_DURABILITY = 1561;

    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected AbstractWeapon(@NonNull CombatUser combatUser, @NonNull WeaponInfo weaponInfo) {
        super(combatUser, weaponInfo);

        combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.isGlobalCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        combatUser.getEntity().getInventory().clear(4);
    }

    @Override
    public final void displayDurability(short durability) {
        itemStack.setDurability(durability);
        if (combatUser.getEntity().getInventory().getItem(4).getDurability() != INVISIBLE_ITEM_DURABILITY)
            combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    public final void setGlowing(boolean isGlowing) {
        if (isGlowing)
            itemStack.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        else
            itemStack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
        if (combatUser.getEntity().getInventory().getItem(4).getDurability() != INVISIBLE_ITEM_DURABILITY)
            combatUser.getEntity().getInventory().setItem(4, itemStack);
    }

    @Override
    public final void setVisible(boolean isVisible) {
        ItemStack invisibleItem = itemStack.clone();
        invisibleItem.setDurability(INVISIBLE_ITEM_DURABILITY);

        if (isVisible)
            combatUser.getEntity().getInventory().setItem(4, itemStack);
        else
            combatUser.getEntity().getInventory().setItem(4, invisibleItem);
    }
}
