package task3;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

public class GeocodeAPI {
    CompletableFuture<String> getGeocode(String place) {
        String encodedPlace = URLEncoder.encode(place, StandardCharsets.UTF_8);
        System.out.println(encodedPlace);
        String APIKEY = "11dfe928-a9bb-4025-a67e-848db971583e";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://graphhopper.com/api/1/geocode?q=" + place + "&locale=de&key=" + APIKEY)
                .get()
                .build();

        CompletableFuture<String> result = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    result.complete(responseBody);
                } else {
                    result.completeExceptionally(new IOException("Request failed with code " + response.code()));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                result.completeExceptionally(e);
            }
        });

        return result;
    }

    List<Location> getLocationsFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            ArrayNode locationsArray = (ArrayNode) rootNode.get("hits");
            return objectMapper.convertValue(locationsArray, new TypeReference<ArrayList<Location>>() {});

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

}
