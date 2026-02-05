package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataChangedS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExposureRepository {
    public static final int EXPECTED_TIMEOUT_SECONDS = 60;
    public static final String EXPOSURES_DIRECTORY_NAME = "exposures";

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final MinecraftServer server;
    protected final DimensionDataStorage dataStorage;
    protected final Path worldFolderPath;
    protected final Path exposuresFolderPath;

    protected final Map<ServerPlayer, Set<ExpectedExposure>> expectedExposures = new HashMap<>();

    public ExposureRepository(MinecraftServer server) {
        this.server = server;
        this.dataStorage = server.overworld().getDataStorage();
        this.worldFolderPath = server.getWorldPath(LevelResource.ROOT);
        this.exposuresFolderPath = worldFolderPath.resolve("data/" + EXPOSURES_DIRECTORY_NAME);
    }

    public List<String> getAllIds() {
        // Save exposures that are in cache and waiting to be saved:
        dataStorage.save();

        File folder = exposuresFolderPath.toFile();

        @Nullable File[] filesList = folder.listFiles();
        if (filesList == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(filesList)
                .filter(file -> file != null && file.isFile())
                .map(file -> com.google.common.io.Files.getNameWithoutExtension(file.getName()))
                .toList();
    }

    public RequestedPalettedExposure load(@NotNull String id) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(!id.isBlank(), "Cannot load exposure: id is empty.");

        String name = EXPOSURES_DIRECTORY_NAME + "/" + id;
        @Nullable ExposureData exposureData = dataStorage.get(ExposureData::load, name);

        if (exposureData == null) {
            File filepath = exposuresFolderPath.resolve(id + ".dat").toFile();
            if (!filepath.exists()) {
                LOGGER.error("Exposure '{}' was not loaded. File '{}' does not exist.", id, filepath);
                return RequestedPalettedExposure.NOT_FOUND;
            }

            LOGGER.error("Exposure '{}' was not loaded. Check above messages for errors.", id);
            return RequestedPalettedExposure.CANNOT_LOAD;
        }

        return RequestedPalettedExposure.success(exposureData);
    }

    public void save(@NotNull String id, ExposureData data) {
        Preconditions.checkArgument(!id.isBlank(), "Cannot save exposure: id is null or empty.");

        if (ensureExposuresDirectoryExists()) {
            String saveDataName = EXPOSURES_DIRECTORY_NAME + "/" + id;
            dataStorage.set(saveDataName, data);
            data.setDirty();
            Packets.sendToAllClients(new ExposureDataChangedS2CP(id));
        }
    }

    public void update(@NotNull String id, Function<ExposureData, ExposureData> updateFunction) {
        Preconditions.checkArgument(!id.isBlank(), "Cannot update exposure: id is null or empty.");

        load(id).getData().ifPresent(exposure -> {
            ExposureData updatedExposure = updateFunction.apply(exposure);
            if (!updatedExposure.equals(exposure)) {
                save(id, updatedExposure);
            }
        });
    }

    public boolean delete(String id) throws IOException {
        return Files.deleteIfExists(exposuresFolderPath.resolve(id + ".dat"));
    }

    public void expect(ServerPlayer player, String id, BiConsumer<ServerPlayer, String> onReceived) {
        Preconditions.checkArgument(!id.isBlank(), "id cannot be null or empty.");
        Set<ExpectedExposure> exposures = expectedExposures.computeIfAbsent(player, pl -> new HashSet<>());
        exposures.add(new ExpectedExposure(id, UnixTimestamp.Seconds.fromNow(EXPECTED_TIMEOUT_SECONDS), onReceived));
    }

    public void expect(ServerPlayer player, String id) {
        expect(player, id, (pl, i) -> {});
    }

    public void handleClientRequest(ServerPlayer player, String id) {
        RequestedPalettedExposure result;

        if (id.isBlank()) {
            LOGGER.error("Null or empty id cannot be used to get an exposure data. Player: '{}'", player.getScoreboardName());
            result = RequestedPalettedExposure.INVALID_ID;
        } else {
            result = load(id);
        }

        Packets.sendToClient(new ExposureDataResponseS2CP(id, result), player);
    }

    public void receiveClientUpload(ServerPlayer player, String id, ExposureData exposure) {
        if (!validateUpload(player, id)) {
            return;
        }

        save(id, exposure);
        onExposureReceived(player, id);

        LOGGER.debug("Saved exposure '{}' uploaded by '{}'.", id, player.getScoreboardName());
    }

    // --

    private final Map<String, AccumulativeExposureData> CHUNKED_EXPOSURES = new HashMap<>();

    public void receiveChunkedClientUploadHeader(ServerPlayer player, String id, int width, int height,
                                                 ResourceLocation palette, ExposureData.Tag tag) {
        if (!validateUpload(player, id)) {
            return;
        }

        if (CHUNKED_EXPOSURES.containsKey(id)) {
            LOGGER.error("Received duplicate header of chunked exposure '{}'. " +
                  "New header will override existing one and can cause unforeseen consequences.", id);
        }

        CHUNKED_EXPOSURES.put(id, new AccumulativeExposureData(id, width, height, palette, tag,
              new byte[width * height]));
    }

    public void receiveChunkedClientUploadChunk(ServerPlayer player, String id, int offset, byte[] bytes) {
        if (!validateUpload(player, id)) {
            return;
        }

        @Nullable AccumulativeExposureData data = CHUNKED_EXPOSURES.get(id);
        if (data == null) {
            LOGGER.error("Received chunk of exposure '{}' from {}, but the header for it does not exist. Discarding.",
                  id, player.getScoreboardName());
            return;
        }

        try {
            System.arraycopy(bytes, 0, data.pixels(), offset, bytes.length);

            if (offset + bytes.length >= data.pixels().length) {
                ExposureData exposureData = new ExposureData(data.width(), data.height(), data.pixels(), data.palette(), data.tag());
                receiveClientUpload(player, id, exposureData);
                CHUNKED_EXPOSURES.remove(id);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to append exposure data chunk. Chunk will be discarded. Error: ", e);
        }
    }

    private record AccumulativeExposureData(String id, int width, int height, ResourceLocation palette,
                                            ExposureData.Tag tag, byte[] pixels) {
    }

    public void clearExpectedExposuresTimedOutLongAgo() {
        for (Map.Entry<ServerPlayer, Set<ExpectedExposure>> exposures : expectedExposures.entrySet()) {
            AtomicInteger cleared = new AtomicInteger();
            exposures.getValue().removeIf(expected -> {
                if (expected.isTimedOut(UnixTimestamp.Seconds.now() - EXPECTED_TIMEOUT_SECONDS)) {
                    CHUNKED_EXPOSURES.remove(expected.id());
                    cleared.getAndIncrement();
                    return true;
                }
                return false;
            });

            if (cleared.get() > 0) {
                LOGGER.info("Cleared {} timed out expected exposures of player: '{}'", cleared.get(), exposures.getKey().getScoreboardName());
            }
        }
    }

    protected boolean validateUpload(ServerPlayer player, String id) {
        if (id.isBlank()) {
            LOGGER.error("Null or empty id cannot be used to save captured exposure. Player: '{}'", player.getScoreboardName());
            return false;
        }

        @Nullable ExpectedExposure expectedExposure = expectedExposures.getOrDefault(player, Collections.emptySet())
                .stream()
                .filter(ee -> ee.id().equals(id))
                .findFirst()
                .orElse(null);

        if (expectedExposure == null) {
            LOGGER.error("Received unexpected upload from player '{}' with ID '{}'. Discarding.", player.getScoreboardName(), id);
            return false;
        } else if (expectedExposure.isTimedOut(UnixTimestamp.Seconds.now())) {
            LOGGER.error("Received expected upload from player '{}' with ID '{}' - {}seconds later than expected. Discarding.",
                    player.getScoreboardName(), id, UnixTimestamp.Seconds.now() - expectedExposure.timeoutAt());
            return false;
        }

        return true;
    }

    protected void onExposureReceived(ServerPlayer player, String id) {
        expectedExposures.getOrDefault(player, Collections.emptySet())
                .removeIf(expectedExposure -> {
                    if (expectedExposure.id().equals(id)) {
                        expectedExposure.onReceived().accept(player, id);
                        return true;
                    }
                    return false;
                });
    }

    protected boolean ensureExposuresDirectoryExists() {
        try {
            return Files.exists(exposuresFolderPath) || exposuresFolderPath.toFile().mkdirs();
        } catch (Exception e) {
            LOGGER.error("Failed to create exposure storage directory: {}", e.toString());
            return false;
        }
    }
}
