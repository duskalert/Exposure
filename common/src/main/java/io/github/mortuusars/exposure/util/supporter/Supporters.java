package io.github.mortuusars.exposure.util.supporter;

import com.google.gson.*;
import io.github.mortuusars.exposure.Exposure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Supporters {
    private static final Gilded gilded = new Gilded();
    private static final Patreon patreon = new Patreon();

    public static void query() {
        gilded.query();
        patreon.query();
    }

    public static Gilded gilded() {
        return gilded;
    }

    public static Patreon patreon() {
        return patreon;
    }

    // --

    public static boolean hasAccessToGoldenSkin(UUID uuid) {
        return gilded().hasAccessToGoldenSkin(uuid) || patreon().hasAccessToGoldenSkin(uuid);
    }

    public static class Loader {
        public CompletableFuture<@Nullable String> readFileFromURL(URI uri) {
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

        public @NotNull List<Supporter> parseSupporters(String json) {
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
    }
}
