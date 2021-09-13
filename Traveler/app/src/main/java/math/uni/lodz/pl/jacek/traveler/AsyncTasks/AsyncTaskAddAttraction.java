package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import math.uni.lodz.pl.jacek.traveler.AddAttractionActivity;
import math.uni.lodz.pl.jacek.traveler.DatabaseConnector;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;

public class AsyncTaskAddAttraction extends AsyncTask<Attraction,Void,Integer> {

    private Activity currentlyActivity;
    private Context context;
    public static AsyncResponse delegate = null;



    public AsyncTaskAddAttraction(Activity currentlyActivity, Context context) {
        this.currentlyActivity = currentlyActivity;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Attraction... params) {
        Integer result = -1; //Something wrong with database connection
        DatabaseConnector databaseConnector = new DatabaseConnector();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //Connection connection = DriverManager.getConnection("jdbc:mysql://sql11.freesqldatabase.com:3306/sql11165490?useUnicode=true&characterEncoding=UTF-8", "sql11165490", "w7KqPFzbUq");
            //connection = DriverManager.getConnection("jdbc:mysql://johnny.heliohost.org/travapp_database","travapp_user","qwerty12345678");
            Connection connection = DriverManager.getConnection(databaseConnector.getUrl(), databaseConnector.getUsername(), databaseConnector.getPassword());
            Statement statement = connection.createStatement();
            File filePhoto = new File(params[0].getPhotoPath());
            addPhotoToFtpServer(params[0],filePhoto);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Places WHERE name = " + "'" + params[0].getPlaceName() + "'");
            if(!checkIfPlaceExistInDatabase(resultSet,params[0].getPlaceName(),params[0].getStateId())){
                statement.execute("INSERT INTO Places " +
                        "VALUES ("+ null  + ", " + "'" + params[0].getPlaceName() + "'" + ", "
                        + "'" + params[0].getStateId() + "'" + ")");
            }
            resultSet.close();
            statement.execute("INSERT INTO Attractions " +
                    "VALUES ("+ null  + ", " + "'" + params[0].getName() + "'" + ", "
                    + "'" + params[0].getStateId() + "'" + ", "
                    + "'" + params[0].getPlaceName() + "'" + ", "
                    + "'" + params[0].getAddress() + "'" + ", "
                    + "'" + params[0].getCategoryId() + "'" + ", "
                    + "'" + params[0].getDescription() + "'" + ","
                    + params[0].getLatitude() + ", "
                    + params[0].getLongitude() + ", "
                    + "'" + createCorrectAttractionName(params[0].getName()) +  filePhoto.getName() + "'" + ", "
                    + "'" + params[0].getAuthor() + "'" + ")");
            statement.close();
            connection.close();
            filePhoto.delete();
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
        currentlyActivity.showDialog(AddAttractionActivity.PLEASE_WAIT_DIALOG_ADDING_ATTRACTION);
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(AddAttractionActivity.PLEASE_WAIT_DIALOG_ADDING_ATTRACTION);
        delegate.processFinish(result);
    }

    private boolean checkIfPlaceExistInDatabase(ResultSet resultSet, String placeName, int stateId) throws SQLException {
        while(resultSet.next()){
            if(resultSet.getString("place_name").equals(placeName)){
                if(resultSet.getInt("state_id") == stateId){
                    return true;
                }
            }
        }
        return false;
    }

    private void addPhotoToFtpServer(Attraction attraction, File filePhoto){
        FTPClient ftpClient = new FTPClient();

        Log.i("PhotoName", filePhoto.getName());
        Log.i("PhotoPath",attraction.getPhotoPath());

        try {
            ftpClient.setAutodetectUTF8( true );
            ftpClient.connect("ftp.ezyro.com",21);
            ftpClient.login("ezyro_20177095","qwerty12345678");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.cwd("htdocs");
            FileInputStream srcFileStream = new FileInputStream(attraction.getPhotoPath());
            ftpClient.storeFile(createCorrectAttractionName(attraction.getName()) +  filePhoto.getName(),srcFileStream);
            srcFileStream.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createCorrectAttractionName(String attractionName){
        StringBuffer goodPhotoPathBuffer = new StringBuffer();
        String[] splitedAttractionName = attractionName.split("\\s+");
        if(splitedAttractionName.length>1){
            for (String wordFromAttractionName: splitedAttractionName) {
                goodPhotoPathBuffer.append(wordFromAttractionName);
                goodPhotoPathBuffer.append("_");
            }
            return goodPhotoPathBuffer.toString();
        }
        return splitedAttractionName[0] + "_";
    }
}
