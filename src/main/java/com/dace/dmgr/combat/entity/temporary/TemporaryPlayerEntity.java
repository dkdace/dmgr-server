package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.user.User;
import lombok.NonNull;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.skinsrestorer.api.property.IProperty;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어 형태의 엔티티 클래스.
 *
 * <p>실제 플레이어가 아니므로 {@link User#fromPlayer(Player)}를 호출하면 안 된다.</p>
 */
public abstract class TemporaryPlayerEntity extends TemporaryEntity<Player> {
    /** Citizens NPC 인스턴스 */
    @NonNull
    protected final NPC npc;

    /**
     * 일시적 플레이어 엔티티 인스턴스를 생성한다.
     *
     * @param playerSkin    플레이어 스킨
     * @param spawnLocation 생성 위치
     * @param name          이름
     * @param game          소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄
     * @param hitboxes      히트박스 목록
     */
    protected TemporaryPlayerEntity(@NonNull PlayerSkin playerSkin, @NonNull Location spawnLocation, @NonNull String name, @Nullable Game game,
                                    @NonNull Hitbox @NonNull ... hitboxes) {
        super(spawnPlayerNPC(name, playerSkin, spawnLocation), name, game, hitboxes);

        this.npc = DMGR.getNpcRegistry().getNPC(entity);
        addOnRemove(npc::destroy);
    }

    /**
     * 플레이어 NPC를 생성한다.
     *
     * @param name          이름
     * @param playerSkin    플레이어 스킨
     * @param spawnLocation 생성 위치
     * @return 플레이어 엔티티
     */
    @NonNull
    private static Player spawnPlayerNPC(@NonNull String name, @NonNull PlayerSkin playerSkin, @NonNull Location spawnLocation) {
        NPC npc = DMGR.getNpcRegistry().createNPC(EntityType.PLAYER, name);

        IProperty property = playerSkin.getProperty();
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(property.getName(), property.getSignature(), property.getValue());
        npc.setAlwaysUseNameHologram(false);
        npc.spawn(spawnLocation);

        return (Player) npc.getEntity();
    }
}
