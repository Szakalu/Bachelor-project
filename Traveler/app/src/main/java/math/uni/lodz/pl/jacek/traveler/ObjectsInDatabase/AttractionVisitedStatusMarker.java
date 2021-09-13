package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jacek on 28.06.2017.
 */

public class AttractionVisitedStatusMarker {

    private int attractionId;
    private int visited;
    private Marker marker;

    public AttractionVisitedStatusMarker(int attractionId, int visited, Marker marker) {
        this.attractionId = attractionId;
        this.visited = visited;
        this.marker = marker;
    }

    public int getAttractionId() {
        return attractionId;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
