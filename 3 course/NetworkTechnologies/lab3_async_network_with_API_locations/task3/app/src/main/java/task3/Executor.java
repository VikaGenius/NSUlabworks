package task3;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.io.InputStreamReader;

public class Executor {

    public void Start(String place) {
        List<Location> locations = loadLocations(place);
        printLocations(locations);
        Location loc = chooseLocation(locations);


    }

    private List<Location> loadLocations(String place) {
        Geocode geocodeAPI = new Geocode();
        CompletableFuture<String> locationInFuture = geocodeAPI.getGeocode(place);
        String json = locationInFuture.join();
        return geocodeAPI.getLocations(json);
    }

    private void printLocations(List<Location> locations) {
        int index = 0;
        for (Location location : locations) {
            System.out.println(index + ". " + location.getCountry() + ", " + location.getCity() + ", " + location.getName() + ".");
            index++;
        }
    }

    private Location chooseLocation(List<Location> locations) {
         try (Scanner reader = new Scanner(new InputStreamReader(System.in))) {
            System.out.println("О какой из локаций вы хотите узнать больше? Введите номер: \n");
            int num = 0;
            num = reader.nextInt();
            System.out.println(locations.get(num).getName());
            return locations.get(num);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }



    
}
