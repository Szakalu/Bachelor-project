package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.MenuActivity;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Category;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

/**
 * Created by Jacek on 27.04.2017.
 */

public class AsyncTaskSynchronizationCategories extends AsyncTask<Void,Void,Integer>{

    private static final int WHAT_TO_DO_ADD_TO_DEVICE = 0;
    private static final int WHAT_TO_DO_LEAVE_ON_DEVICE = 2;

    private ArrayList<Category> arrayListCategoriesInDevice;
    private ArrayList<Category> arrayListCategoriesWhatToDo;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private Context context;

    public AsyncTaskSynchronizationCategories(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
        arrayListCategoriesInDevice = new ArrayList<>();
        arrayListCategoriesWhatToDo = new ArrayList<>();
    }


    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(MenuActivity.PLEASE_WAIT_DIALOG_CATEGORIES);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer result = -4; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        getCategoriesFromDevice();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Categories");
            if(arrayListCategoriesInDevice.size()!=0){
                while(resultSet.next()){
                    Category category = new Category(resultSet.getInt("id"), resultSet.getString("name"),0);
                    category = compareCategoriesDeviceAndDatabase(category);
                    if(category.getWhatToDo() == WHAT_TO_DO_ADD_TO_DEVICE){
                        arrayListCategoriesWhatToDo.add(category);
                    }
                    else if(category.getWhatToDo() == WHAT_TO_DO_LEAVE_ON_DEVICE){
                        //Log.i("StateToDo",place.getWhatToDo()+"");
                        arrayListCategoriesInDevice.remove(category);
                    }
                }
            }
            else{
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
                while(resultSet.next()){
                    databaseTraveler.addCategories(resultSet.getString("name"));
                }
                databaseTraveler.close();
            }
            result = 4;
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            result = -4;
            e.printStackTrace();
        } catch (SQLException e) {
            result = -4;
            e.printStackTrace();
        }
        if(result!=-2){
            deleteFromDeviceWrongCategories();
            addCategoriesToDevice();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(MenuActivity.PLEASE_WAIT_DIALOG_CATEGORIES);
        delegate.processFinish(result);
    }

    private void getCategoriesFromDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        Cursor cursor = databaseTraveler.getRowCategories();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            Category category = new Category(id, name, WHAT_TO_DO_LEAVE_ON_DEVICE);
            arrayListCategoriesInDevice.add(category);
        }
        databaseTraveler.close();
    }

    private Category compareCategoriesDeviceAndDatabase(Category categoryInDatabase){
        for (Category categoryInDevice: arrayListCategoriesInDevice) {
            //Log.i("States Name", stateInDatabase.getStateName() + " / " + stateInDevice.getStateName());
            //Log.i("Country Name", stateInDatabase.getCountryName() + " / " + stateInDevice.getCountryName());

            if(categoryInDevice.getName().equals(categoryInDatabase.getName())){
                return categoryInDevice;
            }
        }
        categoryInDatabase.setWhatToDo(WHAT_TO_DO_ADD_TO_DEVICE);
        return categoryInDatabase;
    }

    private void deleteFromDeviceWrongCategories(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Category category: arrayListCategoriesInDevice) {
            databaseTraveler.deleteCategory(category.getId());
        }
        databaseTraveler.close();
    }

    private void addCategoriesToDevice(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(context);
        for (Category placeInWhatToDo: arrayListCategoriesWhatToDo) {
            databaseTraveler.addCategories(placeInWhatToDo.getName());
        }
        databaseTraveler.close();
    }
}
