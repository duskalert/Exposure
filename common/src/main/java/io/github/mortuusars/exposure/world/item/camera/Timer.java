package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class Timer {
    public boolean isTicking(CameraHolder holder, ItemStack stack) {
        return getReleaseTick(stack) > holder.asHolderEntity().level().getGameTime();
    }

    public int getRemainingTicks(CameraHolder holder, ItemStack stack) {
        return (int)Math.max(-1, getReleaseTick(stack) - holder.asHolderEntity().level().getGameTime());
    }

    public long getReleaseTick(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.RELEASE_TICK, -1L);
    }

    public void setReleaseTick(ItemStack stack, long tick) {
        stack.set(Exposure.DataComponents.RELEASE_TICK, tick);
    }

    public void set(CameraHolder holder, ItemStack stack, int seconds) {
        setReleaseTick(stack, holder.asHolderEntity().level().getGameTime() + seconds * 20L);
    }

    /**
     * @return true if state has changed.
     */
    public boolean tick(CameraHolder holder, ServerLevel level, ItemStack stack) {
        long releaseTick = getReleaseTick(stack);
        if (releaseTick <= -1L) return false;
        long currentTick = level.getGameTime();
        long remainingTicks = releaseTick - currentTick;

        if (remainingTicks < -10) {
            // Ignore if release tick was passed some time ago.
            // To not release when player drops or puts camera in chest and then picks up after some time.
            setReleaseTick(stack, -1L);
            return true;
        }

        if (remainingTicks == 0) {
            setReleaseTick(stack, currentTick);
            if (stack.getItem() instanceof CameraItem cameraItem) {
                cameraItem.release(holder, stack);
            }
            setReleaseTick(stack, -1L);
            return true;
        }

        if (remainingTicks % getTickingInterval(remainingTicks) == 0) {
            playTickSound(holder);
        }

        return false;
    }

    protected void playTickSound(CameraHolder holder) {
        Sound.play(holder.asHolderEntity(), Exposure.SoundEvents.CAMERA_TIMER_TICK.get(), SoundSource.PLAYERS, 1, 0.8f);
    }

    protected int getTickingInterval(long remainingTicks) {
        if (remainingTicks > 100) return 10;
        if (remainingTicks > 50) return 6;
        if (remainingTicks > 25) return 4;
        return 2;
    }
}
