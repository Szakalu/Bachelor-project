package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 24.05.2017.
 */

public class TourAttraction {

    private int id;
    private int tourId;
    private int attractionId;
    private int whatToDo;

    public TourAttraction(int id, int tourId, int attractionId, int whatToDo) {
        this.id = id;
        this.tourId = tourId;
        this.attractionId = attractionId;
        this.whatToDo = whatToDo;
    }

    public int getId() {
        return id;
    }

    public int getTourId() {
        return tourId;
    }

    public int getAttractionId() {
        return attractionId;
    }

    public int getWhatToDo() {
        return whatToDo;
    }
}
