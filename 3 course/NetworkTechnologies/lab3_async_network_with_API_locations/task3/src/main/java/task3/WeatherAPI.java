package task3;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

public class WeatherAPI {
    CompletableFuture<String> getWeather(String lat, String lon) {
        String encodedLon = URLEncoder.encode(lon, StandardCharsets.UTF_8);
        String encodedLat = URLEncoder.encode(lat, StandardCharsets.UTF_8);
        String APIKEY = "b5880b76e735e8a73ff6ea944858fdd4";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + APIKEY)
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

    public WeatherData getWeatherDataFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, WeatherData.class);

    }
}




