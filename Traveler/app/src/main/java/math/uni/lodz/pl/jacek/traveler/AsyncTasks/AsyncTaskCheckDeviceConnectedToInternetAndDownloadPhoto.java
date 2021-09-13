package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.app.Activity;
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
import java.net.InetAddress;

import math.uni.lodz.pl.jacek.traveler.AddAttractionActivity;
import math.uni.lodz.pl.jacek.traveler.BrowseAttractionActivity;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;


public class AsyncTaskCheckDeviceConnectedToInternetAndDownloadPhoto extends AsyncTask<Integer, Void, Integer> {

    private final static String TRAVELER_APP = ".travelerapp";

    private Activity currentlyActivity;
    public static AsyncResponse delegate = null;
    private FTPClient ftpClient;


    public AsyncTaskCheckDeviceConnectedToInternetAndDownloadPhoto(Activity currentlyActivity) {
        this.currentlyActivity = currentlyActivity;
    }


    @Override
    protected Integer doInBackground(Integer... params) {
        int result = -1;

        if(checkIfInternetIsAvailable()){
            connectToFtpServer();
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(currentlyActivity);
            downloadPhoto(databaseTraveler.getOneRowAttractions(params[0]).getPhotoPath());
            disconnectFromFtpServer();
            result = 1;
        }

        return result;
    }

    protected void onPreExecute() {
        currentlyActivity.showDialog(BrowseAttractionActivity.PLEASE_WAIT_DIALOG);
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(BrowseAttractionActivity.PLEASE_WAIT_DIALOG);
        delegate.processFinish(result);
    }

    public boolean checkIfInternetIsAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            Log.i("google",ipAddr.toString());
            if(ipAddr.equals("")){
                return false;
            }
            return true;


        } catch (Exception e) {
            return false;
        }
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
}
