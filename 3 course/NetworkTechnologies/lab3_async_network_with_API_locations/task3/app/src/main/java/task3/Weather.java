package task3;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

public class Weather {
   
    //final public static String url = "https://graphhopper.com/api/1/geocode?q={PLACE}&locale=de&key=11dfe928-a9bb-4025-a67e-848db971583e";

    CompletableFuture<String> getWeather(String lat, String lon) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=b5880b76e735e8a73ff6ea944858fdd4") //экранирование query
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
}


    

