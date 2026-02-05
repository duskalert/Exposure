package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.util.NbtType;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public record CaptureParameters(String exposureId,
                                Optional<CameraId> cameraId,
                                Optional<Integer> cameraHolderId,
                                Optional<Double> fov,
                                float cropFactor,
                                Optional<ResourceLocation> filter,
                                Optional<Projection> projection,
                                Optional<ColorChannel> singleChannel,
                                FilmProperties filmProperties,
                                ExtraData extraData) {

    public static final NbtType<ShutterSpeed> SHUTTER_SPEED =
            NbtType.stringRepresentable("shutter_speed", ShutterSpeed::new);
    public static final NbtType<Boolean> FLASH =
            new NbtType<>("flash", CompoundTag::getBoolean, CompoundTag::putBoolean);
    public static final NbtType<Integer> LIGHT_LEVEL =
            new NbtType<>("light_level", CompoundTag::getInt, CompoundTag::putInt);

    // --

    public static final Codec<CaptureParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(CaptureParameters::exposureId),
            CameraId.CODEC.optionalFieldOf("camera_id").forGetter(CaptureParameters::cameraId),
            Codec.INT.optionalFieldOf("camera_holder_id").forGetter(CaptureParameters::cameraHolderId),
            Codecs.POSITIVE_DOUBLE.optionalFieldOf("fov").forGetter(CaptureParameters::fov),
            Codecs.floatRange(0.001f, 1f).optionalFieldOf("crop_factor", 1f).forGetter(CaptureParameters::cropFactor),
            ResourceLocation.CODEC.optionalFieldOf("filter").forGetter(CaptureParameters::filter),
            Projection.CODEC.optionalFieldOf("projection").forGetter(CaptureParameters::projection),
            ColorChannel.CODEC.optionalFieldOf("single_channel").forGetter(CaptureParameters::singleChannel),
            FilmProperties.CODEC.optionalFieldOf("film", FilmProperties.EMPTY).forGetter(CaptureParameters::filmProperties),
            ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(CaptureParameters::extraData)
    ).apply(instance, CaptureParameters::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(exposureId);
        buf.writeOptional(cameraId,(buf1, cameraId1) -> cameraId1.toPacket(buf1));
        buf.writeOptional(cameraHolderId, FriendlyByteBuf::writeInt);
        buf.writeOptional(fov,FriendlyByteBuf::writeDouble);
        buf.writeFloat(cropFactor);
        buf.writeOptional(filter,FriendlyByteBuf::writeResourceLocation);
        buf.writeOptional(projection,(buf1, projection1) -> projection1.toPacket(buf1));
        buf.writeOptional(singleChannel,FriendlyByteBuf::writeEnum);
        filmProperties.toPacket(buf);
        extraData.toPacket(buf);
    }

    public static CaptureParameters fromPacket(FriendlyByteBuf buf) {
        return new CaptureParameters(buf.readUtf(),buf.readOptional(CameraId::fromPacket),buf.readOptional(FriendlyByteBuf::readInt),
                buf.readOptional(FriendlyByteBuf::readDouble),buf.readFloat(),buf.readOptional(FriendlyByteBuf::readResourceLocation),
                buf.readOptional(Projection::fromPacket),buf.readOptional(buf1 -> buf1.readEnum(ColorChannel.class)),FilmProperties.fromPacket(buf),ExtraData.fromPacket(buf));
    }

    public ShutterSpeed getShutterSpeed() {
        return extraData.get(SHUTTER_SPEED).orElse(ShutterSpeed.DEFAULT);
    }

    public boolean getFlash() {
        return extraData.get(FLASH).orElse(false);
    }

    public Optional<Integer> getLightLevel() {
        return extraData.get(LIGHT_LEVEL);
    }

    public Builder mutable() {
        return new Builder(this);
    }

    public static final class Builder {
        private final String exposureId;
        private @Nullable CameraId cameraId;
        private @Nullable Integer cameraHolderEntityID;
        private @Nullable Double fov;
        private float cropFactor = 1f;
        private @Nullable ResourceLocation filter = null;
        private @Nullable Projection projection;
        private @Nullable ColorChannel chromaticChannel;
        private FilmProperties filmProperties = FilmProperties.EMPTY;
        private final ExtraData extraData = new ExtraData();

        public Builder(String exposureId) {
            this.exposureId = exposureId;
        }

        public Builder(CaptureParameters params) {
            this.exposureId = params.exposureId();
            this.cameraId = params.cameraId().orElse(null);
            this.cameraHolderEntityID = params.cameraHolderId().orElse(null);
            this.fov = params.fov.orElse(null);
            this.cropFactor = params.cropFactor();
            this.filter = params.filter().orElse(null);
            this.projection = params.projection().orElse(null);
            this.chromaticChannel = params.singleChannel().orElse(null);
            this.filmProperties = params.filmProperties();
            extraData.merge(params.extraData());
        }

        public Builder setCameraID(@Nullable CameraId cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder setCameraHolder(@Nullable CameraHolder holder) {
            if (holder == null) cameraHolderEntityID = null;
            else cameraHolderEntityID = holder.asHolderEntity().getId();
            return this;
        }

        public Builder setFilter(@Nullable ResourceLocation filter) {
            this.filter = filter;
            return this;
        }

        public Builder setFov(@Nullable Double fov) {
            this.fov = fov;
            return this;
        }

        public Builder setCropFactor(float cropFactor) {
            this.cropFactor = cropFactor;
            return this;
        }

        public Builder setProjectionInfo(@Nullable Projection projection) {
            this.projection = projection;
            return this;
        }

        public Builder setProjection(Optional<Projection> projection) {
            this.projection = projection.orElse(null);
            return this;
        }

        public Builder setChromaticChannel(@Nullable ColorChannel chromaticChannel) {
            this.chromaticChannel = chromaticChannel;
            return this;
        }

        public Builder setChromaticChannel(Optional<ColorChannel> chromaticChannel) {
            this.chromaticChannel = chromaticChannel.orElse(null);
            return this;
        }

        public Builder setFilmProperties(FilmProperties filmProperties) {
            this.filmProperties = filmProperties;
            return this;
        }

        public Builder extraData(Consumer<ExtraData> extraDataUpdater) {
            extraDataUpdater.accept(extraData);
            return this;
        }

        public <T> Builder extraData(NbtType<T> type, T value) {
            extraData.put(type, value);
            return this;
        }

        public CaptureParameters build() {
            return new CaptureParameters(exposureId,
                    Optional.ofNullable(this.cameraId),
                    Optional.ofNullable(this.cameraHolderEntityID),
                    Optional.ofNullable(this.fov),
                    this.cropFactor,
                    Optional.ofNullable(this.filter),
                    Optional.ofNullable(this.projection),
                    Optional.ofNullable(this.chromaticChannel),
                    this.filmProperties,
                    this.extraData);
        }
    }
}
