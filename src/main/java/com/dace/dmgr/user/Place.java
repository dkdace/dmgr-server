package com.dace.dmgr.user;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.trainingcenter.TrainingCenter;
import com.dace.dmgr.util.EntityUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.function.Consumer;

/**
 * 이용 가능한 장소 목록.
 */
@AllArgsConstructor
public enum Place {
    /** 로비 */
    LOBBY("로비", user -> EntityUtil.teleport(user.getPlayer(), GeneralConfig.getConfig().getLobbyLocation()),
            GeneralConfig.getConfig().getLobbyLocation()),
    /** 자유 전투 */
    FREE_COMBAT("자유 전투", FreeCombat.getInstance()::onStart, FreeCombat.getInstance().getWaitLocation()),
    /** 훈련장 */
    TRAINING_CENTER("훈련장", TrainingCenter.getInstance()::onStart, TrainingCenter.getInstance().getSpawnLocation());

    /** 이름 */
    private final String name;
    /** 이동 시 실행할 작업 */
    private final Consumer<User> onWarp;
    /** 시작 위치 */
    private final Location startLocation;

    /**
     * 이동 시 실행할 작업.
     *
     * @param user 대상 유저
     */
    void onWarp(@NonNull User user) {
        onWarp.accept(user);
    }

    /**
     * @return 시작 위치
     */
    @NonNull
    public Location getStartLocation() {
        return startLocation.clone();
    }

    @Override
    public String toString() {
        return name;
    }
}
