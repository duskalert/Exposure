package io.github.mortuusars.exposure.client;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.level.storage.RequestedExposureStatus;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.mortuusars.exposure.world.level.storage.RequestedExposureStatus.*;

public class ExposureStore {
    public static final Logger LOGGER = LogUtils.getLogger();
    private final ExposureRequester requester = new ExposureRequester(200);

    private final Map<String, RequestedPalettedExposure> exposures = new ConcurrentHashMap<>();

    public RequestedPalettedExposure getOrRequest(@NotNull String id) {
        if (id.isBlank()) {
            return RequestedPalettedExposure.INVALID_ID;
        }

        RequestedPalettedExposure exposure = exposures.getOrDefault(id, RequestedPalettedExposure.NOT_REQUESTED);

        if (exposure.is(SUCCESS)) {
            return exposure;
        }

        if (exposure.is(NOT_REQUESTED)) {
            return request(id);
        }

        if (exposure.is(AWAITED) && requester.isTimedOut(id)) {
            Exposure.LOGGER.info("Exposure '{}' was not received in {} seconds. Requesting again.", id, requester.getTimeoutSeconds());
            return request(id);
        }

        return exposure;
    }

    public void receive(@NotNull String id, RequestedPalettedExposure result) {
        if (id.isBlank()) {
            return;
        }

        exposures.put(id, result);
        requester.requestFulfilled(id);

        RequestedExposureStatus status = result.getStatus();
        if (status != SUCCESS && status != NEEDS_REFRESH) {
            Exposure.LOGGER.error("Received unsuccessful exposure '{}'. Status: {}", id, status);
        }
    }

    // --

    private final Map<String, AccumulativeExposureData> CHUNKED_EXPOSURES = new HashMap<>();

    public void receiveChunkedResponseHeader(String id, int width, int height, ResourceLocation palette, ExposureData.Tag tag) {
        if (CHUNKED_EXPOSURES.containsKey(id)) {
            LOGGER.error("Received duplicate header of chunked exposure '{}'. " +
                  "New header will override existing one and can cause unforeseen consequences.", id);
        }

        CHUNKED_EXPOSURES.put(id, new AccumulativeExposureData(id, width, height, palette, tag,
              new byte[width * height]));
    }

    public void receiveChunkedResponseChunk(String id, int offset, byte[] bytes) {
        @Nullable AccumulativeExposureData data = CHUNKED_EXPOSURES.get(id);
        if (data == null) {
            LOGGER.error("Received chunk of exposure '{}', but the header for it does not exist. Discarding.", id);
            return;
        }

        try {
            System.arraycopy(bytes, 0, data.pixels(), offset, bytes.length);

            if (offset + bytes.length >= data.pixels().length) {
                ExposureData exposureData = new ExposureData(data.width(), data.height(), data.pixels(), data.palette(), data.tag());
                receive(id, RequestedPalettedExposure.success(exposureData));
                CHUNKED_EXPOSURES.remove(id);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to append exposure data chunk. Chunk will be discarded. Error: ", e);
        }
    }

    private record AccumulativeExposureData(String id, int width, int height, ResourceLocation palette,
                                            ExposureData.Tag tag, byte[] pixels) {
    }

    // --

    public void refresh(String id) {
        exposures.compute(id, (identifier, exposure) -> {
            if (exposure != null && exposure.is(SUCCESS)) {
                return RequestedPalettedExposure.needsRefresh(exposure);
            }
            return null;
        });
        requester.refresh(id);
    }

    public void clear() {
        exposures.clear();
        requester.clear();
    }

    private RequestedPalettedExposure request(String id) {
        ExposureRequester.Status requestStatus = requester.request(id);
        RequestedPalettedExposure requestResult = RequestedPalettedExposure.fromRequestStatus(requestStatus);
        exposures.put(id, requestResult);
        return requestResult;
    }
}
