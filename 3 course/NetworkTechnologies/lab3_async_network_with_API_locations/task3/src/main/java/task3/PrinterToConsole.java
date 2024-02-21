package task3;

import java.util.List;

public class PrinterToConsole {
    public void printLocations(List<Location> locations) {
        int index = 0;
        for (Location location : locations) {
            System.out.println(index + ". " + location.getCountry() + ", " + location.getCity() + ", " + location.getName() + ".");
            index++;
        }
        System.out.println();
    }

    public void printWeather(WeatherData weather) {
        System.out.println("Weather on the location: ");
        System.out.println("Temperature is " + weather.getMain().getTemp() + ", feel like " + weather.getMain().getFeels_like());
        System.out.println(weather.getWeather().getMain() + ", " + weather.getWeather().getDescription());
        System.out.println();
    }

    public void printInterestingPlace(List<PlaceInfo> placesInfo) {
        System.out.println("Interesting places near location:");
        System.out.println();
        System.out.println(placesInfo.size());
        for (PlaceInfo placeInfo: placesInfo) {
            System.out.println("Name: " + placeInfo.getName());
            System.out.println("Info about this place: " + placeInfo.getKinds());
            PlaceInfo.Address address = placeInfo.getAddress();
            System.out.println("Address: " + address.getCountry() + ", " + address.getCity() + ", " + address.getRoad() + " " + address.getHouse_number());
            System.out.println("Check on the map: " + placeInfo.getOtm());
            System.out.println((placeInfo.getWikipedia_extracts() == null) ? " " : "Here's what Wikipedia says: " + placeInfo.getWikipedia_extracts().getText());
            System.out.println();
        }
    }

}
