package task3;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;

public class InterestingPlaces {
   
    //final public static String url = "https://graphhopper.com/api/1/geocode?q={PLACE}&locale=de&key=11dfe928-a9bb-4025-a67e-848db971583e";

    CompletableFuture<String> getInterestingPlaces(String lonMin, String lonMax, String latMin, String latMax) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url("http://api.opentripmap.com/0.1/ru/places/bbox?lon_min=" + lonMin + "&lat_min=" + latMin + "&lon_max=" + lonMax + "&lat_max=" + latMax + "&format=geojson&apikey=5ae2e3f221c38a28845f05b6363094207caf1c54c0c3e8792180764b") //экранирование query
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
}
