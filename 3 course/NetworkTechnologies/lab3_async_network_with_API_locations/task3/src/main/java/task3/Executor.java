package task3;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Executor {
    public void Execute(String location) {
        List<Location> locations = loadLocations(location);
        printer.printLocations(locations);

        Location loc = chooseLocation(locations);
        if (loc == null) {
            return;
        }
        CompletableFuture<String> weatherInFuture = loadWeather(loc);

        CompletableFuture<List<String>> futurePlaces = loadInterestingPlacesWithInfo(loc);
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(weatherInFuture, futurePlaces);

        allOfFuture.join();
        List<PlaceInfo> places = placesInfoAPI.getPlaceInfoFromJson(futurePlaces.join());
        WeatherData weatherData = weatherAPI.getWeatherDataFromJson(weatherInFuture.join());

        printer.printWeather(weatherData);
        printer.printInterestingPlace(places);
    }

    private CompletableFuture<List<String>> loadInterestingPlacesWithInfo(Location loc) {
        String lat = Double.toString(loc.getPoint().getLat());
        String lon = Double.toString(loc.getPoint().getLng());

        CompletableFuture<String> interestingPlacesInFuture = interestingPlacesAPI.getInterestingPlaces(lon, lat);

        return interestingPlacesInFuture.thenCompose(str -> {
            PlacesInfoAPI placesInfoAPI = new PlacesInfoAPI();
            List<InterestingPlace> interestingPlaces = interestingPlacesAPI.getInterestingPlacesFromJson(str);
            CompletableFuture<String>[] placesInfoFutures = new CompletableFuture[interestingPlaces.size()];

            for (int i = 0; i < interestingPlaces.size(); i++) {
                InterestingPlace interestingPlace = interestingPlaces.get(i);
                placesInfoFutures[i] = placesInfoAPI.getPlacesInfo(interestingPlace.getXid());
            }

            CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(placesInfoFutures);

            return allOfFuture.thenApply(voidResult -> Arrays.stream(placesInfoFutures)
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
        });

    }

    private CompletableFuture<String> loadWeather(Location loc) {
        String lat = Double.toString(loc.getPoint().getLat());
        String lon = Double.toString(loc.getPoint().getLng());
        return weatherAPI.getWeather(lat, lon);
    }

    private List<Location> loadLocations(String place) {
        CompletableFuture<String> locationInFuture = geocodeAPI.getGeocode(place);
        String json = locationInFuture.join();
        return geocodeAPI.getLocationsFromJson(json);
    }

    private Location chooseLocation(List<Location> locations) {
        try (Scanner reader = new Scanner(new InputStreamReader(System.in))) {
            System.out.println("What matches your request? Enter number: ");

            int num = reader.nextInt();
            return locations.get(num);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private final PrinterToConsole printer = new PrinterToConsole();
    private final GeocodeAPI geocodeAPI = new GeocodeAPI();
    private final WeatherAPI weatherAPI = new WeatherAPI();
    private final InterestingPlacesAPI interestingPlacesAPI = new InterestingPlacesAPI();
    private final PlacesInfoAPI placesInfoAPI = new PlacesInfoAPI();
}
