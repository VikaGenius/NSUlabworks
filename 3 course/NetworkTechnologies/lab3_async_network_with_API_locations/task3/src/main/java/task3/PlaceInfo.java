package task3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceInfo {
    @JsonProperty("otm")
    String otm;
    @JsonProperty("wikipedia_extracts")
    private WikipediaExtracts wikipedia_extracts;
    @JsonProperty("name")
    private String name;
    @JsonProperty("kinds")
    private String kinds;
    @JsonProperty("address")
    private Address address;

    public String getOtm() {
        return otm;
    }

    public void setOtm(String otm) {
        this.otm = otm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKinds() {
        return kinds;
    }

    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public WikipediaExtracts getWikipedia_extracts() {
        return wikipedia_extracts;
    }

    public void setWikipedia_extracts(WikipediaExtracts wikipedia_extracts) {
        this.wikipedia_extracts = wikipedia_extracts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class WikipediaExtracts {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Address {
        private String country;
        private String city;
        private String road;
        private String house_number;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getHouse_number() {
            return house_number;
        }

        public void setHouse_number(String house_number) {
            this.house_number = house_number;
        }

        public String getRoad() {
            return road;
        }

        public void setRoad(String road) {
            this.road = road;
        }
    }

}
