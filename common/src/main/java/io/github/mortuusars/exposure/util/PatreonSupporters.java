package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PatreonSupporters {
    private static @Nullable Map<Tier, List<UUID>> supporters = null;
    private static long lastQueryTime = -1L;

    public static boolean hasGoldenCamera(UUID uuid) {
        Map<Tier, List<UUID>> supporters = getOrQuery();
        return supporters.getOrDefault(Tier.GOLD, Collections.emptyList()).contains(uuid)
                || supporters.getOrDefault(Tier.DIAMOND, Collections.emptyList()).contains(uuid);
    }

    public static Map<Tier, List<UUID>> getOrQuery() {
        if (supporters != null) return supporters;

        if (System.currentTimeMillis() - lastQueryTime < 60000) { // 1 min
            return Collections.emptyMap();
        }

        try {
            lastQueryTime = System.currentTimeMillis();

            for (Tier tier : Tier.values()) {
                readFileFromURL(tier.getUuidsUrl()).thenAccept(lines -> {
                    if (lines == null) return;

                    List<UUID> ids = parseUuids(lines);
                    
                    Minecrft.execute(() -> {
                        if (supporters == null) {
                            supporters = new HashMap<>();
                        }
                        supporters.put(tier, ids);
                    });
                });
            }
        } catch (Exception e) {
            Exposure.LOGGER.warn("Cannot get list of supporters.", e);
        }

        return supporters;
    }

    private static CompletableFuture<@Nullable List<String>> readFileFromURL(URI uri) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.body().lines().toList())
                    .exceptionally(e -> {
                        Exposure.LOGGER.warn("Cannot get list of supporters.", e);
                        return null;
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static @NotNull List<UUID> parseUuids(List<String> lines) {
        return lines.stream().map(line -> {
                    try {
                        return UUID.fromString(line);
                    } catch (Exception e) {
                        Exposure.LOGGER.warn("Cannot parse UUID from '{}'", line, e);
                        return Util.NIL_UUID;
                    }
                })
                .filter(id -> !id.equals(Util.NIL_UUID))
                .toList();
    }

    public enum Tier {
        COPPER,
        IRON,
        GOLD,
        DIAMOND;

        public URI getUuidsUrl() {
            return URI.create("https://raw.githubusercontent.com/mortuusars/resources/refs/heads/main/patreon/uuids/"
                    + name().toLowerCase() + ".txt");
        }
    }
}
