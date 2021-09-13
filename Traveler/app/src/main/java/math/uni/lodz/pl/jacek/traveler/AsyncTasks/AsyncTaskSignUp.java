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
import math.uni.lodz.pl.jacek.traveler.RegistrationActivity;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.SignUpParameters;

public class AsyncTaskSignUp extends AsyncTask<SignUpParameters,Void,Integer> {

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;

    public AsyncTaskSignUp(Activity currentlyActivity) {
        this.currentlyActivity = currentlyActivity;
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(RegistrationActivity.PLEASE_WAIT_DIALOG);
    }

    @Override
    protected Integer doInBackground(SignUpParameters... params) {
        Integer result = -1; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Users WHERE username = " + "'" + params[0].getUsername() + "'");
            result = 1; //Username don't exist
            if(resultSet.next()){
                if(resultSet.getString("username").equals(params[0].getUsername())){
                    result = 2; //User exist
                }

            }
            resultSet.close();
            if(result!=2){
                resultSet = statement.executeQuery("SELECT * FROM Users WHERE email = " + "'" + params[0].getMail() + "'");
                if(resultSet.next()){
                    Log.i("Mail",params[0].getMail().toString() + " : "  + resultSet.getString("email"));
                    if(resultSet.getString("email").equals(params[0].getMail())){
                        result = 3; //Mail exist
                    }
                }
                resultSet.close();
            }

            if(result == 1){
                statement.execute("INSERT INTO Users " +
                "VALUES ("+ null  + ", " + "'" + params[0].getUsername() + "'" + ", "
                        + "'" + params[0].getPassword() + "'" + ", "
                        + "'" + params[0].getMail() + "'" + ")");
            }
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
        currentlyActivity.removeDialog(RegistrationActivity.PLEASE_WAIT_DIALOG);
        delegate.processFinish(result);
    }

}
