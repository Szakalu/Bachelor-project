package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.MenuActivity;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;

/**
 * Created by Jacek on 27.04.2017.
 */

public class AsyncTaskSynchronizationStates extends AsyncTask<Void,Void,Integer>{

    private static final int WHAT_TO_DO_ADD_TO_DEVICE = 0;
    private static final int WHAT_TO_DO_LEAVE_ON_DEVICE = 2;

    private ArrayList<State> arrayListStatesInDevice;
    private ArrayList<State> arrayListStatesWhatToDo;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;

    public AsyncTaskSynchronizationStates(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
        arrayListStatesInDevice = new ArrayList<>();
        arrayListStatesWhatToDo = new ArrayList<>();
    }


    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_STATES);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer result = -2; //Something wrong with database connection
        getStatesFromDevice();
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM States");
            if(arrayListStatesInDevice.size()!=0){
                while(resultSet.next()){
                    State state = new State(resultSet.getInt("id"), resultSet.getString("name"),resultSet.getInt("country_id"),0,0);
                    //Log.i("State",resultSet.getInt("id") + resultSet.getString("state_name") + resultSet.getString("country_name"));
                    state = compareStatesDeviceAndDatabase(state);
                    if(state.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                        arrayListStatesWhatToDo.add(state);
                    }
                    else if(state.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                        Log.i("StateToDo",state.getWhatToDo()+"");
                        arrayListStatesInDevice.remove(state);
                    }
                }
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addState(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getInt("country_id"),0);
                }
                databaseTraveler.close();
            }
            result = 2;
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            result = -2;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -2;
            e.printStackTrace();
        }
        if(result!=-2){
            deleteFromDeviceWrongState();
            addStatesToDevice();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_STATES);
        delegate.processFinish(result);
    }

    private void getStatesFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowState();
        while(cursor.moveToNext()){
            State state = new State(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListStatesInDevice.add(state);
        }
        databaseTraveler.close();
    }

    private State compareStatesDeviceAndDatabase(State stateInDatabase){
        for (State stateInDevice: arrayListStatesInDevice) {
            if(stateInDatabase.getStateName().equals(stateInDevice.getStateName())){
                return stateInDevice;
            }
        }
        stateInDatabase.setWhatToDo(WHAT_TO_DO_ADD_TO_DEVICE);
        return stateInDatabase;
    }
    private void deleteFromDeviceWrongState(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (State state: arrayListStatesInDevice) {
            databaseTraveler.deleteState(state.getId());
        }
        databaseTraveler.close();
    }

    private void addStatesToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (State stateInWhatToDo: arrayListStatesWhatToDo) {
            databaseTraveler.addState(stateInWhatToDo.getId(), stateInWhatToDo.getStateName(),stateInWhatToDo.getCountryId(),stateInWhatToDo.getToSynchronized());
        }
        databaseTraveler.close();
    }
}
