package io.github.mortuusars.exposure.util;

import com.google.gson.*;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PatreonSupporters {
    public static final UUID MORTUUSARS_UUID = UUID.fromString("19266046-b14b-428f-919b-75a21474ba07");

    private static @Nullable Map<Tier, List<Supporter>> supporters = null;
    private static long lastQueryTime = -1L;

    public static boolean hasGoldenCamera(UUID uuid) {
        if (uuid.equals(MORTUUSARS_UUID)) return true;
        Map<Tier, List<Supporter>> supporters = getOrQuery();
        return supporters.getOrDefault(Tier.GOLD, Collections.emptyList()).stream().anyMatch(s -> s.uuid().equals(uuid))
                || supporters.getOrDefault(Tier.DIAMOND, Collections.emptyList()).stream().anyMatch(s -> s.uuid().equals(uuid));
    }

    public static Map<Tier, List<Supporter>> getOrQuery() {
        if (supporters != null) return supporters;

        if (System.currentTimeMillis() - lastQueryTime < 60000) { // 1 min
            return Collections.emptyMap();
        }

        try {
            lastQueryTime = System.currentTimeMillis();

            for (Tier tier : Tier.values()) {
                readFileFromURL(tier.getUuidsUrl()).thenAccept(json -> {
                    if (json == null) return;

                    List<Supporter> parsedSupporters = parseSupporters(json);

                    Minecrft.execute(() -> {
                        if (supporters == null) {
                            supporters = new HashMap<>();
                        }
                        supporters.put(tier, parsedSupporters);
                    });
                });
            }
        } catch (Exception e) {
            Exposure.LOGGER.warn("Cannot get list of supporters.", e);
        }

        return supporters;
    }

    private static CompletableFuture<@Nullable String> readFileFromURL(URI uri) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .exceptionally(e -> {
                        Exposure.LOGGER.warn("Cannot get list of supporters.", e);
                        return null;
                    });
        } catch (Exception e) {
            Exposure.LOGGER.warn("Cannot get list of supporters from {}.", uri, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public static @NotNull List<Supporter> parseSupporters(String json) {
        Gson gson = new Gson();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        List<Supporter> supporters = new ArrayList<>();

        for (JsonElement element : array) {
            try {
                Supporter p = gson.fromJson(element, Supporter.class);
                supporters.add(p);
            } catch (Exception e) {
                Exposure.LOGGER.warn("Cannot parse supporter from '{}'", element, e);
            }
        }

        return supporters;
    }

    public enum Tier {
        COPPER,
        IRON,
        GOLD,
        DIAMOND;

        public URI getUuidsUrl() {
            return URI.create("https://raw.githubusercontent.com/mortuusars/resources/refs/heads/main/patreon/uuids/"
                    + name().toLowerCase() + ".json");
        }
    }

    public record Supporter(String name, UUID uuid) { }
}
