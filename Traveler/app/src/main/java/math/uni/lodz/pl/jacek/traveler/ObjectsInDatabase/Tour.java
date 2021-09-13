package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 24.05.2017.
 */

public class Tour {

    private int id;
    private String tourName;
    private String description;
    private int stateId;
    private String author;
    private int addedCorrectly;
    private int attractionsCount;
    private int whatToDo;

    public Tour(int id, String tourName, String description, int stateId, String author, int addedCorrectly, int attractionsCount, int whatToDo) {
        this.id = id;
        this.tourName = tourName;
        this.description = description;
        this.stateId = stateId;
        this.author = author;
        this.addedCorrectly = addedCorrectly;
        this.attractionsCount = attractionsCount;
        this.whatToDo = whatToDo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAddedCorrectly() {
        return addedCorrectly;
    }

    public void setAddedCorrectly(int addedCorrectly) {
        this.addedCorrectly = addedCorrectly;
    }

    public int getWhatToDo() {
        return whatToDo;
    }

    public void setWhatToDo(int whatToDo) {
        this.whatToDo = whatToDo;
    }

    public int getAttractionsCount() {
        return attractionsCount;
    }
}
