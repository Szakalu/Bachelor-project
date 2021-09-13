package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.TourAndTourAttractions;
import math.uni.lodz.pl.jacek.traveler.PickAttractionsForTourActivity;
import math.uni.lodz.pl.jacek.traveler.RegistrationActivity;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.SignUpParameters;
import math.uni.lodz.pl.jacek.traveler.serializable.SerializableTour;

public class AsyncTaskAddTour extends AsyncTask<TourAndTourAttractions,Void,Integer> {

    private final int TOUR_ADDED_NOT_CORRECTLY = 0;
    private final int TOUR_ADDED_CORRECTLY = 1;

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;

    public AsyncTaskAddTour(Activity currentlyActivity) {
        this.currentlyActivity = currentlyActivity;
    }

    @Override
    protected Integer doInBackground(TourAndTourAttractions... params) {
        Integer result = -1; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        int addedTourId;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            SerializableTour serializableTour = params[0].getSerializableTour();
            statement.executeUpdate("INSERT INTO Tours "
                    + "VALUES ("+ null  + ", " + "'" + serializableTour.getTourName() + "'" + ", "
                    + "'" + serializableTour.getTourDescription() + "'" + ", "
                    + "'" + serializableTour.getTourStateId() + "'" + ", "
                    + "'" + serializableTour.getTourAuthor() + "'" + ", " +
                    + TOUR_ADDED_NOT_CORRECTLY + ", " + params[0].getTourAttractionsIds().size() + ")",Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                addedTourId = resultSet.getInt(1);
                resultSet.close();
                ArrayList<Integer> attractionsIds = params[0].getTourAttractionsIds();
                for (Integer attractionId: attractionsIds) {
                    statement.executeUpdate("INSERT INTO Tours_Attractions " +
                            "VALUES ("+ null  + ", " + "'" + addedTourId + "'" + ", "
                            + "'" + attractionId + "'" + ")");
                }
                statement.executeUpdate("UPDATE Tours"
                        + " SET added_correctly = " + TOUR_ADDED_CORRECTLY
                        + " WHERE id = " + addedTourId);
            }
            statement.close();
            connection.close();
            result = 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(PickAttractionsForTourActivity.PLEASE_WAIT_DIALOG);
    }


    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(PickAttractionsForTourActivity.PLEASE_WAIT_DIALOG);
        delegate.processFinish(result);
    }

}
