package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.MenuActivity;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

/**
 * Created by Jacek on 27.04.2017.
 */

public class AsyncTaskSynchronizationAttractions extends AsyncTask<String,Void,Integer>{

    private static final int WHAT_TO_DO_ADD_TO_DEVICE = 0;
    private static final int WHAT_TO_DO_LEAVE_ON_DEVICE = 2;

    private final static String TRAVELER_APP = ".travelerapp";

    private ArrayList<Attraction> arrayListAttractionsInDevice;
    private ArrayList<Attraction> arrayListAttractionsWhatToDo;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;
    private FTPClient ftpClient;

    public AsyncTaskSynchronizationAttractions(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
        arrayListAttractionsInDevice = new ArrayList<>();
        arrayListAttractionsWhatToDo = new ArrayList<>();
    }


    @Override
    protected Integer doInBackground(String... params) {
        Integer result = -5; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        getAttractionsFromDevice();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Attractions WHERE " + params[0]);
            if(arrayListAttractionsInDevice.size()!=0){
                while(resultSet.next()){
                    Attraction attraction = new Attraction(resultSet.getInt("id"),resultSet.getString("name"),
                            resultSet.getInt("state_id"), resultSet.getString("place_name"),resultSet.getString("address"),resultSet.getInt("category_id"),
                            resultSet.getString("description"),resultSet.getDouble("latitude"),resultSet.getDouble("longitude"),
                            resultSet.getString("photo_path"),resultSet.getString("author"), WHAT_TO_DO_ADD_TO_DEVICE);

                    attraction = compareAttractionsDeviceAndDatabase(attraction);
                    if(attraction.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                        arrayListAttractionsWhatToDo.add(attraction);
                    }
                    else if(attraction.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                        arrayListAttractionsInDevice.remove(attraction);
                    }
                }
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addAttraction(resultSet.getInt("id"),resultSet.getString("name"),
                            resultSet.getInt("state_id"), resultSet.getString("place_name"),resultSet.getString("address"),resultSet.getInt("category_id"),
                            resultSet.getString("description"),resultSet.getDouble("latitude"),resultSet.getDouble("longitude"),
                            resultSet.getString("photo_path"),resultSet.getString("author"));
                }
                databaseTraveler.close();
            }
            result = 5;
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            result = -5;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -5;
            e.printStackTrace();
        }
        if(result!=-5){
            deleteFromDeviceWrongAttractions();
            addAttractionsToDevice();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_ATTRACTIONS);
    }


    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_ATTRACTIONS);
        delegate.processFinish(result);
    }

    private void getAttractionsFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowAttractions();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String attractionName = cursor.getString(1);
            int stateId = cursor.getInt(2);
            String placeName = cursor.getString(3);
            String address = cursor.getString(4);
            int categoryId = cursor.getInt(5);
            String description = cursor.getString(6);
            double longitude = cursor.getDouble(7);
            double latitude = cursor.getDouble(8);
            String photo_path = cursor.getString(9);
            String author = cursor.getString(10);
            Attraction attraction = new Attraction(id,attractionName,stateId,placeName
                    ,address,categoryId,description,latitude,longitude,photo_path,author,WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListAttractionsInDevice.add(attraction);
        }
        databaseTraveler.close();
    }

    private Attraction compareAttractionsDeviceAndDatabase(Attraction attractionInDatabase){
        for (Attraction attractionInDevice: arrayListAttractionsInDevice) {
            //Log.i("States Name", stateInDatabase.getStateName() + " / " + stateInDevice.getStateName());
            //Log.i("Country Name", stateInDatabase.getCountryName() + " / " + stateInDevice.getCountryName());

            if(checkIfAttractionsAreSame(attractionInDevice,attractionInDatabase)){
                return attractionInDevice;
            }
        }
        //attractionInDatabase.setWhatToDo(WHAT_TO_DO_ADD_TO_DEVICE);
        return attractionInDatabase;
    }

    private boolean checkIfAttractionsAreSame(Attraction attractionInDevice, Attraction attractionInDatabase){
        if(!attractionInDatabase.getName().equals(attractionInDevice.getName())){
            return false;
        }
        else if(attractionInDatabase.getStateId() != attractionInDevice.getStateId()){
            return false;
        }
        else if(!attractionInDatabase.getPlaceName().equals(attractionInDevice.getPlaceName())){
            return false;
        }
        else if(!attractionInDatabase.getAddress().equals(attractionInDevice.getAddress())){
            return false;
        }
        else if(attractionInDatabase.getCategoryId() != attractionInDevice.getCategoryId()){
            return false;
        }
        else if(!attractionInDatabase.getDescription().equals(attractionInDevice.getDescription())){
            return false;
        }
        else if(attractionInDatabase.getLongitude() != attractionInDevice.getLongitude()){
            return false;
        }
        else if(attractionInDatabase.getLatitude() != attractionInDevice.getLatitude()){
            return false;
        }
        else if(!attractionInDatabase.getPhotoPath().equals(attractionInDevice.getPhotoPath())){
            return false;
        }
        return true;
    }

    private void deleteFromDeviceWrongAttractions(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Attraction attraction: arrayListAttractionsInDevice) {
            databaseTraveler.deleteAttraction(attraction.getId());
            deletePhoto(attraction.getPhotoPath());
        }
        databaseTraveler.close();
    }

    private void addAttractionsToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Attraction attractionInWhatToDo: arrayListAttractionsWhatToDo) {
            databaseTraveler.addAttraction(attractionInWhatToDo.getId(),attractionInWhatToDo.getName(), attractionInWhatToDo.getStateId(),
                    attractionInWhatToDo.getPlaceName(),attractionInWhatToDo.getAddress(),attractionInWhatToDo.getCategoryId(),
                    attractionInWhatToDo.getDescription(),attractionInWhatToDo.getLatitude(),attractionInWhatToDo.getLongitude(),
                    attractionInWhatToDo.getPhotoPath(), attractionInWhatToDo.getAuthor());
        }
        databaseTraveler.close();
    }

    private FTPClient connectToFtpServer(){
        ftpClient = new FTPClient();

        try {
            ftpClient.setAutodetectUTF8(true);
            ftpClient.connect("ftp.ezyro.com", 21);
            ftpClient.login("ezyro_20177095", "qwerty12345678");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.cwd("htdocs");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ftpClient;
    }

    private void downloadPhoto(String fileName){

            try {
                File downloadFile1 = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + fileName);
                String remoteFile1 = fileName;
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
                outputStream1.close();
                if (success) {
                    Log.i("File",fileName + " has been downloaded successfully.");
                }
                else{
                    Log.i("File",fileName + " has been not downloaded successfully.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private void disconnectFromFtpServer(){
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfAttractionPhotoFileExist(String photoPath){
        File photo = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + photoPath);
        if(photo.isFile() && photo.length() > 0){
            return true;
        }
        return false;
    }

    private void deletePhoto(String photoPath){
        File photo = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + photoPath);
        if(checkIfAttractionPhotoFileExist(photoPath)){
            photo.delete();
        }
    }
}
