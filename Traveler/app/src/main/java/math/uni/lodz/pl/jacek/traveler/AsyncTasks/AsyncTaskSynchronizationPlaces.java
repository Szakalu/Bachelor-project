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
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Place;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

/**
 * Created by Jacek on 27.04.2017.
 */

public class AsyncTaskSynchronizationPlaces extends AsyncTask<Void,Void,Integer>{

    private static final int WHAT_TO_DO_ADD_TO_DEVICE = 0;
    private static final int WHAT_TO_DO_LEAVE_ON_DEVICE = 2;

    private ArrayList<Place> arrayListPlacesInDevice;
    private ArrayList<Place> arrayListPlacesWhatToDo;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;

    public AsyncTaskSynchronizationPlaces(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
        arrayListPlacesInDevice = new ArrayList<>();
        arrayListPlacesWhatToDo = new ArrayList<>();
    }


    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_PLACES);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer result = -3; //Something wrong with database connection
        arrayListPlacesInDevice = getPlacesFromDevice(arrayListPlacesInDevice);
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Places");
            if(arrayListPlacesInDevice.size()!=0){
                while(resultSet.next()){
                    Place place = new Place(resultSet.getInt("id"),resultSet.getString("name"), resultSet.getInt("state_id"),WHAT_TO_DO_ADD_TO_DEVICE);
                    place = comparePlacesDeviceAndDatabase(place);
                    if(place.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                        arrayListPlacesWhatToDo.add(place);
                    }
                    else if(place.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                        arrayListPlacesInDevice.remove(place);
                    }
                }
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addPlace(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getInt("state_id"));
                }
                databaseTraveler.close();
            }
            result = 3;
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            result = -3;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -3;
            e.printStackTrace();
        }
        if(result!=-2){
            deleteFromDeviceWrongPlaces();
            addPlacesToDevice();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_PLACES);
        delegate.processFinish(result);
    }

    private ArrayList getPlacesFromDevice(ArrayList arrayListPlacesInDevice){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowPlaces();
        while(cursor.moveToNext()){
            Place place = new Place(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListPlacesInDevice.add(place);
        }
        databaseTraveler.close();
        return arrayListPlacesInDevice;
    }

    private Place comparePlacesDeviceAndDatabase(Place placeInDatabase){
        for (Place placeInDevice: arrayListPlacesInDevice) {
            if(comparePlacesBoolean(placeInDevice,placeInDatabase)){
                return placeInDevice;
            }
        }
        return placeInDatabase;
    }

    private boolean comparePlacesBoolean(Place placeInDevice, Place placeInDatabase){
        if(placeInDevice.getId() != placeInDatabase.getId()){
            return false;
        }
        if(!placeInDevice.getPlaceName().equals(placeInDatabase.getPlaceName())){
            return false;
        }
        if(placeInDevice.getStateId() != placeInDatabase.getStateId()){
            return false;
        }
        return true;
    }

    private void deleteFromDeviceWrongPlaces(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Place place: arrayListPlacesInDevice) {
            databaseTraveler.deletePlace(place.getId());
        }
        databaseTraveler.close();
    }

    private void addPlacesToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Place placeInWhatToDo: arrayListPlacesWhatToDo) {
            databaseTraveler.addPlace(placeInWhatToDo.getId(),placeInWhatToDo.getPlaceName(),placeInWhatToDo.getStateId());
        }
        databaseTraveler.close();
    }
}
