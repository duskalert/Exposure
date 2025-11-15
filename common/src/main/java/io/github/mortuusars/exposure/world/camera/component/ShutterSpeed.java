package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShutterSpeed implements StringRepresentable {
    public static final ShutterSpeed DEFAULT = new ShutterSpeed("1/60");

    public static final Codec<ShutterSpeed> CODEC = Codec.STRING.comapFlatMap(str -> {
        try {
            return DataResult.success(new ShutterSpeed(str));
        } catch (Exception e) {
            return DataResult.error(e::getMessage);
        }
    }, ShutterSpeed::getNotation);

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(notation);
    }

    public static ShutterSpeed fromPacket(FriendlyByteBuf buf) {
        return new ShutterSpeed(buf.readUtf());
    }

    private final float valueMilliseconds;
    private final String notation;

    /**
     * Expected format is 1/60, 1/125, 2", 15", etc.
     */
    public ShutterSpeed(String notation) {
        notation = notation.trim();

        if (notation.endsWith("\"")) {
            this.valueMilliseconds = Integer.parseInt(notation.replace("\"", "")) * 1000;
            this.notation = notation;
        } else if (notation.contains("1/")) {
            this.valueMilliseconds = 1f / Integer.parseInt(notation.replace("1/", "")) * 1000;
            this.notation = notation;
        } else {
            throw new IllegalArgumentException("'%s' is not a valid shutter speed. Format should be 1/60, 2\", etc.".formatted(notation));
        }
    }

    public String getNotation() {
        return notation;
    }

    public float getDurationMilliseconds() {
        return valueMilliseconds;
    }

    /**
     * Should be at least 1 tick. Otherwise, it's probably not going to work correctly.
     */
    public int getDurationTicks() {
        return Math.max(1, (int) (valueMilliseconds / 50));
    }

    public boolean shouldCauseTickingSound() {
        return valueMilliseconds > 999; // 1" and above
    }

    public float getStopsDifference(ShutterSpeed relative) {
        return (float) (Math.log(valueMilliseconds / relative.getDurationMilliseconds()) / Math.log(2));
    }

    public float getStops() {
        return getStopsDifference(DEFAULT);
    }

    public float getBrightness() {
        return 1f + getStops() * 0.2f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShutterSpeed that = (ShutterSpeed) o;
        return Float.compare(valueMilliseconds, that.valueMilliseconds) == 0 && Objects.equals(notation, that.notation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueMilliseconds, notation);
    }

    @Override
    public @NotNull String getSerializedName() {
        return getNotation();
    }
}
