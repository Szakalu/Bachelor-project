package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 27.04.2017.
 */

public class State {

    private int id;
    private String stateName;
    private int countryId;
    private int toSynchronized;
    private int whatToDo;

    public State(int id, String stateName, int countryId, int toSynchronized, int whatToDo) {
        this.id = id;
        this.stateName = stateName;
        this.countryId = countryId;
        this.toSynchronized = toSynchronized;
        this.whatToDo = whatToDo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getToSynchronized() {
        return toSynchronized;
    }

    public void setToSynchronized(int toSynchronized) {
        this.toSynchronized = toSynchronized;
    }

    public int getWhatToDo() {
        return whatToDo;
    }

    public void setWhatToDo(int whatToDo) {
        this.whatToDo = whatToDo;
    }
}
