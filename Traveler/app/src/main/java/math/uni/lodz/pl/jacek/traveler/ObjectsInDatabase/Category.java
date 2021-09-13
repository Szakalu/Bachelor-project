package math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase;

/**
 * Created by Jacek on 01.05.2017.
 */

public class Category {

    private int id;
    private String name;
    private int whatToDo;

    public Category(int id, String name, int whatToDo) {
        this.id = id;
        this.name = name;
        this.whatToDo = whatToDo;
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

    public int getWhatToDo() {
        return whatToDo;
    }

    public void setWhatToDo(int whatToDo) {
        this.whatToDo = whatToDo;
    }
}
