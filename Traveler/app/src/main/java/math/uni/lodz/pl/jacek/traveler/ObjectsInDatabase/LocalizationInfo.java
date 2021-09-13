package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 15.08.2017.
 */

public class LocalizationInfo {

    private int countryId;
    private int stateId;
    private String placeName;
    private String address;

    public LocalizationInfo() {
        countryId = -1;
        stateId = -1;
        placeName = null;
        address = null;
    }

    public LocalizationInfo(int countryId, int stateId, String placeName, String address) {
        this.countryId = countryId;
        this.stateId = stateId;
        this.placeName = placeName;
        this.address = address;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
