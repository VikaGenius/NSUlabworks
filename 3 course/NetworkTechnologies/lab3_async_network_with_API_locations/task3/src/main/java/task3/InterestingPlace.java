package task3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestingPlace {
    @JsonProperty("xid")
    private String xid;

    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
}
