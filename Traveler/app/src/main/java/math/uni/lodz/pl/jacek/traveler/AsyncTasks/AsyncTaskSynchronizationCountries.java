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
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Country;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class AsyncTaskSynchronizationCountries extends AsyncTask<Void,Void,Integer> {


    private ArrayList<Country> arrayListCountriesInDevice = new ArrayList<>();;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;

    public AsyncTaskSynchronizationCountries(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_COUNTRIES);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer result = -1; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Countries ORDER BY id ASC");
            getCountriesFromDevice();
            if(arrayListCountriesInDevice.size()!=0){
                while(resultSet.next()){
                    compareCountriesDeviceAndDatabase(new Country(resultSet.getInt("id"),resultSet.getString("name")));
                }
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addCountry(resultSet.getInt("id"), resultSet.getString("name"));
                }
                databaseTraveler.close();
            }
            resultSet.close();
            statement.close();
            connection.close();
            result = 1;
        } catch (ClassNotFoundException e) {
            result = -1;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -1;
            e.printStackTrace();
        }
        if(result != -1){
            deleteFromDeviceWrongCountries();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_COUNTRIES);
        delegate.processFinish(result);
    }

    private void getCountriesFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowCountry();
        while(cursor.moveToNext()){
            arrayListCountriesInDevice.add(new Country(cursor.getInt(0), cursor.getString(1)));
        }
        databaseTraveler.close();
    }

    private void compareCountriesDeviceAndDatabase(Country countryInDatabase){
        boolean countryIsInDeviceDatabase = false;
        int indexOfCountry;
        for (indexOfCountry = 0; indexOfCountry < arrayListCountriesInDevice.size(); indexOfCountry++) {
            if(checkIfCountriesAreEquals(arrayListCountriesInDevice.get(indexOfCountry), countryInDatabase)){
                countryIsInDeviceDatabase = true;
                break;
            }
        }
        if(countryIsInDeviceDatabase){
            arrayListCountriesInDevice.remove(indexOfCountry);
        }
        else{
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
            databaseTraveler.addCountry(countryInDatabase.getId(), countryInDatabase.getCountryName());
            databaseTraveler.close();
        }

    }

    private boolean checkIfCountriesAreEquals(Country countryInDevice, Country countryInDatabase){
        if(countryInDevice.getId() != countryInDatabase.getId()){
            return false;
        }
        if(!countryInDevice.getCountryName().equals(countryInDatabase.getCountryName())){
            return false;
        }
        return true;
    }


    private void deleteFromDeviceWrongCountries(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Country countryInDevice: arrayListCountriesInDevice) {
            databaseTraveler.deleteCountry(countryInDevice.getId());
        }
        databaseTraveler.close();
    }

}
