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
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.TourAttraction;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

/**
 * Created by Jacek on 27.04.2017.
 */

public class AsyncTaskSynchronizationTours extends AsyncTask<String,Void,Integer>{

    private static final int WHAT_TO_DO_ADD_TO_DEVICE = 0;
    private static final int WHAT_TO_DO_LEAVE_ON_DEVICE = 2;

    private static final int ADDED_CORRECTLY = 1;
    private static final int NOT_ADDED_CORRECTLY = 0;

    private ArrayList<Tour> arrayListToursInDevice;
    private ArrayList<Tour> arrayListToursWhatToDo;

    private ArrayList<TourAttraction> arrayListToursAttractionsInDevice;
    private ArrayList<TourAttraction> arrayListToursAttractionsWhatToDo;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;

    public AsyncTaskSynchronizationTours(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
        arrayListToursInDevice = new ArrayList<>();
        arrayListToursWhatToDo = new ArrayList<>();
        arrayListToursAttractionsInDevice = new ArrayList<>();
        arrayListToursAttractionsWhatToDo = new ArrayList<>();
    }


    @Override
    protected Integer doInBackground(String... params) {
        Integer result = -6; //Something wrong with database connection
        getToursFromDevice();
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Tours WHERE " + params[0] + " AND added_correctly = 1");
            if(arrayListToursInDevice.size()!=0){
                while(resultSet.next()){
                    Tour tour = new Tour(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getString("description"),
                            resultSet.getInt("state_id"),resultSet.getString("author"),resultSet.getInt("added_correctly"), resultSet.getInt("attractions_count"),WHAT_TO_DO_ADD_TO_DEVICE);

                    tour = compareToursDeviceAndDatabase(tour);
                    if(tour.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                        arrayListToursWhatToDo.add(tour);
                    }
                    else if(tour.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                        //Log.i("StateToDo",place.getWhatToDo()+"");
                        arrayListToursInDevice.remove(tour);
                    }
                }
                deleteFromDeviceWrongTours();
                addToursToDevice();
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addTour(resultSet.getInt("id"),resultSet.getString("name"),resultSet.getString("description"),
                            resultSet.getInt("state_id"),resultSet.getString("author"), NOT_ADDED_CORRECTLY, resultSet.getInt("attractions_count"));
                }
                databaseTraveler.close();
            }
            result = 6;
            getToursFromDevice();
            getToursAttractionsFromDevice();
            String stringForToursAttractions = createStringForToursAttractions(arrayListToursInDevice);
            resultSet = statement.executeQuery("SELECT * FROM Tours_Attractions WHERE " + stringForToursAttractions);
            while (resultSet.next()){
                TourAttraction tourAttraction = new TourAttraction(resultSet.getInt("id"),resultSet.getInt("tour_id"),resultSet.getInt("attraction_id"),WHAT_TO_DO_ADD_TO_DEVICE);

                tourAttraction = compareToursAttractionDeviceAndDatabase(tourAttraction);
                if(tourAttraction.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                    arrayListToursAttractionsWhatToDo.add(tourAttraction);
                }
                else if(tourAttraction.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                    arrayListToursAttractionsInDevice.remove(tourAttraction);
                }
            }
            deleteFromDeviceWrongToursAttractions();
            addToursAttractionsToDevice();
            updateAddedCorrectlyToursInDevice();
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            result = -6;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -6;
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_TOURS);
    }


    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_TOURS);
        delegate.processFinish(result);
    }

    private void updateAddedCorrectlyToursInDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(currentlyActivity);
        for (Tour tour: arrayListToursInDevice) {
            databaseTraveler.updateTour(tour.getId(), ADDED_CORRECTLY);
        }
        databaseTraveler.close();
    }

    private void getToursFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowTours();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String tourName = cursor.getString(1);
            String description = cursor.getString(2);
            int stateId = cursor.getInt(3);
            String author = cursor.getString(4);
            int addedCorrectly = cursor.getInt(5);
            int attractionsCount = cursor.getInt(6);
            Tour tour = new Tour(id, tourName, description, stateId, author, addedCorrectly, attractionsCount, WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListToursInDevice.add(tour);
        }
        databaseTraveler.close();
    }

    private Tour compareToursDeviceAndDatabase(Tour tourInDatabase){
        for (Tour tourInDevice: arrayListToursInDevice) {

            if(checkIfToursAreSame(tourInDevice,tourInDatabase)){
                return tourInDevice;
            }
        }
        return tourInDatabase;
    }

    private boolean checkIfToursAreSame(Tour tourInDevice, Tour tourInDatabase){
        if(tourInDevice.getId() != tourInDatabase.getId()) {
            return false;
        }
        else if (!tourInDevice.getTourName().equals(tourInDatabase.getTourName())){
            return false;
        }
        else if(!tourInDevice.getDescription().equals(tourInDatabase.getDescription())){
            return false;
        }
        else if(tourInDevice.getStateId() != (tourInDatabase.getStateId())){
            return false;
        }
        else if(!tourInDevice.getAuthor().equals(tourInDatabase.getAuthor())){
            return false;
        }
        return true;
    }

    private void deleteFromDeviceWrongTours(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Tour tour: arrayListToursInDevice) {
            databaseTraveler.deleteTour(tour.getId());
        }
        databaseTraveler.close();
    }

    private void addToursToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Tour tourInWhatToDo: arrayListToursWhatToDo) {
            databaseTraveler.addTour(tourInWhatToDo.getId(), tourInWhatToDo.getTourName(), tourInWhatToDo.getDescription(),
                     tourInWhatToDo.getStateId(), tourInWhatToDo.getAuthor(), NOT_ADDED_CORRECTLY, tourInWhatToDo.getAttractionsCount());
        }
        databaseTraveler.close();
    }

    private String createStringForToursAttractions(ArrayList<Tour> toursInDevice){
        if(toursInDevice.size()>0){
            if(toursInDevice.size()==1){
                return createStringForToursAttractionsIfToursListSizeIsOne(toursInDevice);
            }
            else{
                return createStringForToursAttractionsIfToursListSizeIsMoreThenOne(toursInDevice);
            }
        }
        return "";
    }

    private String createStringForToursAttractionsIfToursListSizeIsOne(ArrayList<Tour> toursInDevice){
        return "tour_id" + " = " + toursInDevice.get(0).getId();
    }

    private String createStringForToursAttractionsIfToursListSizeIsMoreThenOne(ArrayList<Tour> toursInDevice){
        StringBuffer whereQuaryBuffer = new StringBuffer();
        whereQuaryBuffer.append("tour_id IN ");
        whereQuaryBuffer.append("(");
        for(int tourIndexPosition=0;tourIndexPosition<toursInDevice.size();tourIndexPosition++){
            whereQuaryBuffer.append(toursInDevice.get(tourIndexPosition).getId());
            if(tourIndexPosition != toursInDevice.size()-1){
                whereQuaryBuffer.append(",");
            }

        }
        whereQuaryBuffer.append(")");
        return whereQuaryBuffer.toString();
    }

    private void getToursAttractionsFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowToursAttractions();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            int tourId = cursor.getInt(1);
            int attractionId = cursor.getInt(2);
            TourAttraction tourAttraction = new TourAttraction(id, tourId, attractionId, WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListToursAttractionsInDevice.add(tourAttraction);
        }
        databaseTraveler.close();
    }

    private TourAttraction compareToursAttractionDeviceAndDatabase(TourAttraction tourAttractionInDatabase){
        for (TourAttraction tourAttractionInDevice: arrayListToursAttractionsInDevice) {
            Log.i("Tours","ID: " + tourAttractionInDatabase.getId() + " " + tourAttractionInDevice.getId());
            Log.i("Tours","Tour ID: " + tourAttractionInDatabase.getTourId() + " " + tourAttractionInDevice.getTourId());
            Log.i("Tours","Attraction ID: " + tourAttractionInDatabase.getAttractionId() + " " + tourAttractionInDevice.getAttractionId());
            if(checkIfToursAttractionsAreSame(tourAttractionInDatabase,tourAttractionInDevice)){
                return tourAttractionInDevice;
            }
        }
        return tourAttractionInDatabase;
    }

    private boolean checkIfToursAttractionsAreSame(TourAttraction tourAttractionInDevice, TourAttraction tourAttractionInDatabase){
        if(tourAttractionInDevice.getId() != tourAttractionInDatabase.getId()) {
            return false;
        }
        else if(tourAttractionInDevice.getTourId() != tourAttractionInDatabase.getTourId()) {
            return false;
        }
        else if(tourAttractionInDevice.getAttractionId() != tourAttractionInDatabase.getAttractionId()) {
            return false;
        }
        return true;
    }

    private void deleteFromDeviceWrongToursAttractions(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (TourAttraction tourAttraction: arrayListToursAttractionsInDevice) {
            databaseTraveler.deleteTourAttraction(tourAttraction.getId());
        }
        databaseTraveler.close();
    }

    private void addToursAttractionsToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (TourAttraction tourAttractionInWhatToDo: arrayListToursAttractionsWhatToDo) {
            databaseTraveler.addTourAttraction(tourAttractionInWhatToDo.getId() , tourAttractionInWhatToDo.getTourId(), tourAttractionInWhatToDo.getAttractionId());
        }
        databaseTraveler.close();
    }
}
