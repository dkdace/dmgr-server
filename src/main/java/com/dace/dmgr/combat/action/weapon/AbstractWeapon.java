package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Weapon}의 기본 구현체, 모든 무기의 기반 클래스.
 */
public abstract class AbstractWeapon extends AbstractAction implements Weapon {
    /** 투명 아이템의 내구도 */
    private static final short INVISIBLE_ITEM_DURABILITY = 1561;
    /** 무기 아이템 객체 */
    private final ItemStack itemStack;
    /** 무기 아이템 표시 여부 */
    private boolean isVisible = true;

    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param weaponInfo 무기 정보 객체
     */
    protected AbstractWeapon(@NonNull CombatUser combatUser, @NonNull WeaponInfo<? extends Weapon> weaponInfo) {
        super(combatUser);

        this.itemStack = weaponInfo.getStaticItem().getItemStack();
        display();
    }

    @Override
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) getCooldown());
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey)
                && combatUser.isGlobalCooldownFinished()
                && !combatUser.getStatusEffectModule().hasAnyRestriction(CombatRestrictions.USE_WEAPON);
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        super.dispose();

        combatUser.getEntity().getInventory().clear(4);
    }

    @Override
    public final void setMaterial(@NonNull Material material) {
        itemStack.setType(material);
        display();
    }

    @Override
    public final void setDurability(short durability) {
        if (durability < 0)
            throw new IllegalArgumentException("'durability'가 0 이상이어야 함");

        itemStack.setDurability(durability);
        display();
    }

    @Override
    public final void setGlowing(boolean isGlowing) {
        if (isGlowing)
            itemStack.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        else
            itemStack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);

        display();
    }

    @Override
    public final void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        if (isVisible) {
            display();
            return;
        }

        ItemStack invisibleItem = itemStack.clone();
        invisibleItem.setDurability(INVISIBLE_ITEM_DURABILITY);
        combatUser.getEntity().getInventory().setItem(4, invisibleItem);
    }

    /**
     * 무기 설명 아이템을 적용한다.
     */
    private void display() {
        ItemStack slotItem = combatUser.getEntity().getInventory().getItem(4);
        if (slotItem == null || !slotItem.equals(itemStack) && isVisible)
            combatUser.getEntity().getInventory().setItem(4, itemStack);
    }
}
