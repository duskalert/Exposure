package io.github.mortuusars.exposure.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class CameraStandRedstoneControl {
    public int delay = 2;

    protected CameraStandEntity stand;

    protected boolean hasSignal;
    protected int releaseDelay = -1;

    public CameraStandRedstoneControl(CameraStandEntity stand) {
        this.stand = stand;
    }

    /**
     * @return 'true' if shutter was released.
     */
    public boolean tick() {
        boolean hasSignal = stand.level().hasNeighborSignal(stand.blockPosition());

        if (hasSignal && !this.hasSignal && releaseDelay < 0) {
            releaseDelay = delay; // Start delay countdown when receiving a new pulse
            // Delay helps to resolve some visual issues. Redstone will lit up on the client in that time, for example.
        }

        boolean released = false;

        if (releaseDelay > 0) {
            releaseDelay--;
            if (releaseDelay == 0) {
                releaseDelay = -1;
                stand.release(); // Due to release delay, fastest shooting speed seems to be every 3 ticks.
                released = true;
            }
        }

        this.hasSignal = hasSignal;
        return released;
    }

    public void load(CompoundTag tag) {
        hasSignal = tag.getBoolean("HasRedstoneSignal");
        if (tag.contains("RedstoneReleaseDelay", Tag.TAG_INT)) {
            releaseDelay = tag.getInt("RedstoneReleaseDelay");
        }
    }

    public void save(CompoundTag tag) {
        if (hasSignal) {
            tag.putBoolean("HasRedstoneSignal", true);
        }

        if (releaseDelay > -1) {
            tag.putInt("RedstoneReleaseDelay", releaseDelay);
        }
    }
}
