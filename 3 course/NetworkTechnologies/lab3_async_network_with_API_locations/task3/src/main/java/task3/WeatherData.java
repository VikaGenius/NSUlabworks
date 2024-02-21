package task3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    @JsonProperty("weather")
    private List<Weather> weather;

    @JsonProperty("main")
    private Main main;

    public Weather getWeather() {
        return weather.get(0);
    }

    public void setWeather(Weather weather) {
        this.weather.add(weather);
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Weather {
        private String main;
        private String description;

        public String getDescription() {
            return description;
        }

        public String getMain() {
            return main;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMain(String main) {
            this.main = main;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Main {
        private double temp;
        private double feels_like;

        public double getFeels_like() {
            return feels_like;
        }

        public double getTemp() {
            return temp;
        }

        public void setFeels_like(double feels_like) {
            this.feels_like = feels_like;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }
    }
}
