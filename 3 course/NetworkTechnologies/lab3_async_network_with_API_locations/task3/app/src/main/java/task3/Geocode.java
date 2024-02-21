package task3;

import java.io.IOException;
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

public class Geocode {
   
    //final public static String url = "https://graphhopper.com/api/1/geocode?q={PLACE}&locale=de&key=11dfe928-a9bb-4025-a67e-848db971583e";

    CompletableFuture<String> getGeocode(String place) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url("https://graphhopper.com/api/1/geocode?q=" + place + "&locale=de&key=11dfe928-a9bb-4025-a67e-848db971583e") //экранирование query
            .get()
            .build();

        //Response response;
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

    List<Location> getLocations(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            ArrayNode locationsArray = (ArrayNode) rootNode.get("hits"); // "hits" - это наш массив с данными
            return objectMapper.convertValue(locationsArray, new TypeReference<ArrayList<Location>>() {});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
