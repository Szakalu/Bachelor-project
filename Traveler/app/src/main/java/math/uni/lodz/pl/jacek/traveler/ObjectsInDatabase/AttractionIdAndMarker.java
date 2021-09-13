package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jacek on 29.06.2017.
 */

public class AttractionIdAndMarker {

    private int attractionId;
    private Marker marker;

    public AttractionIdAndMarker(int attractionId, Marker marker) {
        this.attractionId = attractionId;
        this.marker = marker;
    }

    public int getAttractionId() {
        return attractionId;
    }

    public Marker getMarker() {
        return marker;
    }
}
