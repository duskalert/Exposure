package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("AddedMixinMembersNamePattern")
public class CameraOperatorAccess {
    public static CameraOperator op(Player player) {
        return (CameraOperator) (Object) player;
    }

    public static CameraHolder holder(Player player) {
        return (CameraHolder) (Object) player;
    }
}
