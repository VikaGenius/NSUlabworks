package task3;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;

public class InterestingPlacesAPI {
    CompletableFuture<String> getInterestingPlaces(String lon, String lat) {
        String encodedLon = URLEncoder.encode(lon, StandardCharsets.UTF_8);
        String encodedLat = URLEncoder.encode(lat, StandardCharsets.UTF_8);
        String APIKEY = "5ae2e3f221c38a28845f05b6363094207caf1c54c0c3e8792180764b";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://api.opentripmap.com/0.1/ru/places/radius?lon=" + lon + "&lat=" + lat + "&radius=1000&format=json&apikey=" + APIKEY)
                .get()
                .build();

        //Response response;
        CompletableFuture<String> result = new CompletableFuture<>();

        client.newCall(request).enqueue(new okhttp3.Callback() {
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

    List<InterestingPlace> getInterestingPlacesFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<List<InterestingPlace>>(){}.getType());
    }
}
