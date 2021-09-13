package math.uni.lodz.pl.jacek.traveler.serializable;

import java.io.Serializable;

/**
 * Created by Jacek on 29.05.2017.
 */

@SuppressWarnings("serial")
public class SerializableTour implements Serializable {

    private String tourName;
    private String tourDescription;
    private int tourStateId;
    private String tourAuthor;
    private int attractionCount;


    public SerializableTour(String tourName, String tourDescription, int tourStateId, String tourAuthor, int attractionCount) {
        this.tourName = tourName;
        this.tourDescription = tourDescription;
        this.tourStateId = tourStateId;
        this.tourAuthor = tourAuthor;
        this.attractionCount = attractionCount;
    }

    public String getTourName() {

        return tourName;
    }

    public String getTourDescription() {
        return tourDescription;
    }

    public int getTourStateId() {
        return tourStateId;
    }

    public String getTourAuthor() {
        return tourAuthor;
    }

    public int getAttractionCount() {
        return attractionCount;
    }
}
