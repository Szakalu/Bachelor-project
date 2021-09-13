package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 04.07.2017.
 */

public class Trip {

    private int tripId;
    private String userMoves;
    private String attractionOrTourId;
    private String attractionOrTour;
    private String data;
    private String stateName;
    private String owner;

    public Trip(int tripId, String userMoves, String attractionOrTourId, String attractionOrTour, String data, String stateName, String owner) {
        this.tripId = tripId;
        this.userMoves = userMoves;
        this.attractionOrTourId = attractionOrTourId;
        this.attractionOrTour = attractionOrTour;
        this.data = data;
        this.stateName = stateName;
        this.owner = owner;
    }

    public String getUserMoves() {
        return userMoves;
    }

    public String getAttractionOrTourId() {
        return attractionOrTourId;
    }

    public String getAttractionOrTour() {
        return attractionOrTour;
    }

    public String getData() {
        return data;
    }

    public int getTripId() {
        return tripId;
    }

    public String getStateName() {
        return stateName;
    }

    public String getOwner() {
        return owner;
    }
}
