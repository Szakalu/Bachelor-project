package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.LoginActivity;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.SignInParameters;

public class AsyncTaskSignIn extends AsyncTask<SignInParameters,Void,Integer> {

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;



    public AsyncTaskSignIn(Activity currentlyActivity) {
        this.currentlyActivity = currentlyActivity;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(LoginActivity.PLEASE_WAIT_DIALOG);
    }

    @Override
    protected Integer doInBackground(SignInParameters... params) {
        Integer result = -1; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Users WHERE username = " + "'" + params[0].getUserName() + "'"
                    + " AND password = " + "'" + params[0].getPassword() + "'");
            result = 2; //Wrong username or password
            if(resultSet.next()){
                result = 1; //Correct username and password
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(LoginActivity.PLEASE_WAIT_DIALOG);
        delegate.processFinish(result);
    }
}
