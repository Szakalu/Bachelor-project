package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.serializable.SerializableTour;

public class TourAndTourAttractions {

    private SerializableTour serializableTour;
    private ArrayList<Integer> tourAttractionsIds;

    public TourAndTourAttractions(SerializableTour serializableTour, ArrayList<Integer> tourAttractionsIds) {
        this.serializableTour = serializableTour;
        this.tourAttractionsIds = tourAttractionsIds;
    }

    public SerializableTour getSerializableTour() {
        return serializableTour;
    }

    public ArrayList<Integer> getTourAttractionsIds() {
        return tourAttractionsIds;
    }
}
