package math.uni.lodz.pl.jacek.traveler.synchronization.objects;

/**
 * Created by Jacek on 14.07.2017.
 */

public class StateSynchronization {

    private int stateId;
    private int whatToDo;

    public StateSynchronization(int stateId, int whatToDo) {
        this.stateId = stateId;
        this.whatToDo = whatToDo;
    }

    public int getStateId() {
        return stateId;
    }

    public int getWhatToDo() {
        return whatToDo;
    }
}
