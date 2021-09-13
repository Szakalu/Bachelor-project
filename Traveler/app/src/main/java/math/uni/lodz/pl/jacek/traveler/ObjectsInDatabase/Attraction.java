package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 03.05.2017.
 */

public class Attraction {

    private int id;
    private String name;
    private int stateId;
    private String placeName;
    private String address;
    private int categoryId;
    private String description;
    private double latitude;
    private double longitude;
    private String photoPath;
    private String author;
    private int whatToDo;

    public Attraction(int id, String name, int stateId, String placeName, String address, int categoryId, String description, double latitude, double longitude, String photoPath, String author, int whatToDo) {
        this.id = id;
        this.name = name;
        this.stateId = stateId;
        this.placeName = placeName;
        this.address = address;
        this.categoryId = categoryId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoPath = photoPath;
        this.author = author;
        this.whatToDo = whatToDo;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getWhatToDo() {
        return whatToDo;
    }

    public void setWhatToDo(int whatToDo) {
        this.whatToDo = whatToDo;
    }
}
