package math.uni.lodz.pl.jacek.traveler;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationAttractions;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationCategories;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationPlaces;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationTours;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationCountries;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationStates;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.synchronization.objects.StateSynchronization;

public class MenuActivity extends AppCompatActivity implements AsyncResponse {

    public static final int PLEASE_WAIT_DIALOG_COUNTRIES = 1;
    public static final int PLEASE_WAIT_DIALOG_STATES = 2;
    public static final int PLEASE_WAIT_DIALOG_PLACES = 3;
    public static final int PLEASE_WAIT_DIALOG_CATEGORIES = 4;
    public static final int PLEASE_WAIT_DIALOG_ATTRACTIONS = 5;
    public static final int PLEASE_WAIT_DIALOG_TOURS = 6;

    private final static int MY_REQUEST_ID = 1;

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_SYNCHRONIZATION = "synchronization";
    private static final String SYNCHRONIZATION_TRUE = "true";
    private static final String SYNCHRONIZATION_FALSE = "false";


    private static final int SYNCHRONIZE_TO_FALSE = 0;
    private static final int SYNCHRONIZE_TO_TRUE = 1;
    private static final String SYNCHRONIZATION_STATUS = "synchronization_status";

    private final static String TRAVELER_APP = ".travelerapp";
    private final static String TRAVELER_TEMP = ".travelertemp";

    private final static String DOWNLOAD_OR_REMOVE = "Download_Or_Remove";
    private final static String DOWNLOAD = "Download";
    private final static String REMOVE = "Remove";

    public static final int ATTRACTION_TO_SYNCHRONIZATION = 1;


    private Button buttonBrowseTheAttractions;
    private Button buttonAddAttraction;
    private Button buttonWhereAmI;
    private Button buttonBrowseTheTours;
    private Button buttonAddTour;
    private Button buttonMyTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        checkIfDirectoryForTempExist();
        checkIfDirectoryForPhotoExist();
        synchronizeDatabase();
        setButtonAddAttraction();
        setButtonWhereAmI();
        setImageButtonOptions();
        setButtonBrowseTheAttractions();
        setButtonBrowseTheTours();
        setButtonAddTour();
        setButtonMyTrips();
        setButtonsWidthAndTextSize();
    }

    private void showDatabaseInfo(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowCountry();
        while (cursor.moveToNext()){
            Log.i("DatabaseInfo",cursor.getInt(0)+"");
            Log.i("DatabaseInfo",cursor.getString(1)+"");
        }
        cursor.close();
        databaseTraveler.close();
    }

    private void checkIfDirectoryForPhotoExist(){
        File directoryForTravelerImages = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP);
        if(directoryForTravelerImages.isDirectory()){
            Log.i("Directory","APP Exist");
        }
        else{
            Log.i("Directory","APP Not exist");
            directoryForTravelerImages.mkdir();
            checkIfDirectoryForPhotoExist();
        }
    }


    private void checkIfDirectoryForTempExist(){
        File directoryForTravelerTemp = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_TEMP);
        if(directoryForTravelerTemp.isDirectory()){
            Log.i("Directory","TEMP Exist");
        }
        else{
            Log.i("Directory","TEMP Not exist");
            directoryForTravelerTemp.mkdir();
            checkIfDirectoryForTempExist();
        }
    }

    private void setButtonsWidthAndTextSize(){
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(buttonWhereAmI);
        buttons.add(buttonBrowseTheAttractions);
        buttons.add(buttonAddAttraction);
        buttons.add(buttonBrowseTheTours);
        buttons.add(buttonAddTour);
        buttons.add(buttonMyTrips);
        for (Button button: buttons) {
            button.setWidth(600);
            button.setTextSize(16);
        }
    }

    private void synchronizeDatabase(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_SYNCHRONIZATION,null) == null || pref.getString(PREF_SYNCHRONIZATION,null) == SYNCHRONIZATION_FALSE){
            AsyncTaskSynchronizationCountries.delegate = MenuActivity.this;
            new AsyncTaskSynchronizationCountries(this,this).execute();
        }
        else{
            createAlertDialogCheckIfUserWantToSynchronizeApplication();
        }
    }

    private void createAlertDialogCheckIfUserWantToSynchronizeApplication(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_for_synchronization_text_menu_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_menu_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AsyncTaskSynchronizationCountries.delegate = MenuActivity.this;
                        new AsyncTaskSynchronizationCountries(MenuActivity.this,MenuActivity.this).execute();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_menu_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createAlertDialogCheckWhatUserWantToDoLogoutOrExit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_when_user_click_back_button))
                .setCancelable(false)
                .setNeutralButton(getString(R.string.alert_dialog_cancel_button_menu_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.alert_dialog_logout_button_menu_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intentLogin = new Intent(MenuActivity.this,LoginActivity.class);
                        startActivity(intentLogin);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_exit_button_menu_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setButtonAddAttraction(){
        buttonAddAttraction = (Button) findViewById(R.id.buttonAddAttraction);
        buttonAddAttraction.setText(getString(R.string.add_attraction_button_menu_activity));

        buttonAddAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddAttraction = new Intent(MenuActivity.this,AddAttractionActivity.class);
                startActivity(intentAddAttraction);
            }
        });
    }

    private void setButtonBrowseTheAttractions(){
        buttonBrowseTheAttractions = (Button) findViewById(R.id.buttonBrowseTheAttractions);
        buttonBrowseTheAttractions.setText(getString(R.string.browse_the_attraction_button_menu_activity));

        buttonBrowseTheAttractions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBrowseAttraction = new Intent(MenuActivity.this,BrowseAttractionActivity.class);
                startActivity(intentBrowseAttraction);
            }
        });
    }

    private void setButtonBrowseTheTours(){
        buttonBrowseTheTours = (Button) findViewById(R.id.buttonBrowesTheTours);
        buttonBrowseTheTours.setText(getString(R.string.browse_the_tours_button_menu_activity));

        buttonBrowseTheTours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBrowseTour = new Intent(MenuActivity.this,BrowseTourActivity.class);
                startActivity(intentBrowseTour);
            }
        });
    }

    private void setButtonMyTrips(){
        buttonMyTrips = (Button) findViewById(R.id.buttonMyTrips);
        buttonMyTrips.setText(getString(R.string.my_trips_button_menu_activity));
        buttonMyTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myTripsIntent = new Intent(MenuActivity.this,MyTripsActivity.class);
                startActivity(myTripsIntent);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG_COUNTRIES:
                ProgressDialog dialogCountry = new ProgressDialog(this);
                dialogCountry.setTitle(getString(R.string.dialog_title_menu_activity));
                dialogCountry.setMessage(getString(R.string.dialog_message_menu_activity));
                dialogCountry.setCancelable(false);
                return dialogCountry;

            case PLEASE_WAIT_DIALOG_STATES:
                ProgressDialog dialogState = new ProgressDialog(this);
                dialogState.setTitle(getString(R.string.dialog_title_state_menu_activity));
                dialogState.setMessage(getString(R.string.dialog_message_state_menu_activity));
                dialogState.setCancelable(false);
                return dialogState;

            case PLEASE_WAIT_DIALOG_PLACES:
                ProgressDialog dialogPlaces = new ProgressDialog(this);
                dialogPlaces.setTitle(getString(R.string.dialog_title_place_menu_activity));
                dialogPlaces.setMessage(getString(R.string.dialog_message_place_menu_activity));
                dialogPlaces.setCancelable(false);
                return dialogPlaces;

            case PLEASE_WAIT_DIALOG_CATEGORIES:
                ProgressDialog dialogCategories = new ProgressDialog(this);
                dialogCategories.setTitle(getString(R.string.dialog_title_category_menu_activity));
                dialogCategories.setMessage(getString(R.string.dialog_message_category_menu_activity));
                dialogCategories.setCancelable(false);
                return dialogCategories;

            case PLEASE_WAIT_DIALOG_ATTRACTIONS:
                ProgressDialog dialogAttractions = new ProgressDialog(this);
                dialogAttractions.setTitle(getString(R.string.dialog_title_attraction_menu_activity));
                dialogAttractions.setMessage(getString(R.string.dialog_message_attraction_menu_activity));
                dialogAttractions.setCancelable(false);
                return dialogAttractions;

            case PLEASE_WAIT_DIALOG_TOURS:
                ProgressDialog dialogTours = new ProgressDialog(this);
                dialogTours.setTitle(getString(R.string.dialog_title_tour_menu_activity));
                dialogTours.setMessage(getString(R.string.dialog_message_tour_menu_activity));
                dialogTours.setCancelable(false);
                return dialogTours;

            default:
                break;
        }

        return null;
    }

    @Override
    public void processFinish(Integer output) {
        switch (output) {
            case -1:
                Toast.makeText(this, "Something goes wrong !!!", Toast.LENGTH_SHORT).show();
                Log.i("Database", "can't connect to database");
                break;
            case 1:
                Toast.makeText(this, "Countries Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("Countries", "synchronized");
                AsyncTaskSynchronizationStates.delegate = MenuActivity.this;
                new AsyncTaskSynchronizationStates(this,this).execute();
                break;
            case 2:
                Toast.makeText(this, "States Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("States", "synchronized");
                AsyncTaskSynchronizationPlaces.delegate = MenuActivity.this;
                new AsyncTaskSynchronizationPlaces(this,this).execute();
                break;

            case 3:
                Toast.makeText(this, "Places Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("Places", "synchronized");
                AsyncTaskSynchronizationCategories.delegate = MenuActivity.this;
                new AsyncTaskSynchronizationCategories(this,this).execute();
                break;

            case 4:
                Toast.makeText(this, "Categories Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("Categories", "synchronized");
                ArrayList<Integer> statesIdsToSynchronized = new ArrayList<>();
                statesIdsToSynchronized = checkIfSomeAttractionsToSynchronized(statesIdsToSynchronized);
                if(statesIdsToSynchronized.size()>0){
                    AsyncTaskSynchronizationAttractions.delegate = MenuActivity.this;
                    new AsyncTaskSynchronizationAttractions(this,this).execute(createStringForDatabase(statesIdsToSynchronized));
                }
                else{
                    setDatabaseSynchronizationTrue();
                }
                break;
            case 5:
                Toast.makeText(this, "Attractions Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("Attractions", "synchronized");
                ArrayList<Integer> statesNamesToSynchronizedForTours = new ArrayList<>();
                statesNamesToSynchronizedForTours = checkIfSomeAttractionsToSynchronized(statesNamesToSynchronizedForTours);
                AsyncTaskSynchronizationTours.delegate = MenuActivity.this;
                new AsyncTaskSynchronizationTours(this,this).execute(createStringForDatabase(statesNamesToSynchronizedForTours));
                break;
            case 6:
                Toast.makeText(this, "Tours Synchronized !!!", Toast.LENGTH_SHORT).show();
                Log.i("Tours", "synchronized");
                setDatabaseSynchronizationTrue();
                break;
        }
    }

    private void setDatabaseSynchronizationTrue(){
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_SYNCHRONIZATION, SYNCHRONIZATION_TRUE)
                .commit();
    }

    private void setDatabaseSynchronizationFalse(){
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_SYNCHRONIZATION, SYNCHRONIZATION_FALSE)
                .commit();
    }

    private ArrayList checkIfSomeAttractionsToSynchronized(ArrayList<Integer> statesIdsToSynchronized){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowState();
        while(cursor.moveToNext()){
            if(cursor.getInt(3) == ATTRACTION_TO_SYNCHRONIZATION){
                statesIdsToSynchronized.add(cursor.getInt(0));
            }
        }
        return statesIdsToSynchronized;
    }

    private String createStringForDatabase(ArrayList<Integer> statesNamesToSynchronized){
        if(statesNamesToSynchronized.size()>0){
            if(statesNamesToSynchronized.size()==1){
                return createStringForDatabaseIfStateListSizeIsOne(statesNamesToSynchronized);
            }
            else{
                return createStringForDatabaseIfStateListSizeIsMoreThenOne(statesNamesToSynchronized);
            }
        }
        return "";
    }

    private String createStringForDatabaseIfStateListSizeIsOne(ArrayList<Integer> statesNamesToSynchronized){
        return "state_id" + " = " + statesNamesToSynchronized.get(0).toString();
    }

    private String createStringForDatabaseIfStateListSizeIsMoreThenOne(ArrayList<Integer> statesNamesToSynchronized){
        StringBuffer whereQuaryBuffer = new StringBuffer();
        whereQuaryBuffer.append("state_id IN ");
        whereQuaryBuffer.append("(");
        for(int stateIndexPosition=0;stateIndexPosition<statesNamesToSynchronized.size();stateIndexPosition++){
            whereQuaryBuffer.append(statesNamesToSynchronized.get(stateIndexPosition));
            if(stateIndexPosition != statesNamesToSynchronized.size()-1){
                whereQuaryBuffer.append(",");
            }

        }
        whereQuaryBuffer.append(")");
        return whereQuaryBuffer.toString();
    }

    private void setButtonWhereAmI(){
        buttonWhereAmI = (Button) findViewById(R.id.buttonWhereAmI);
        buttonWhereAmI.setText(getString(R.string.where_am_i_button_menu_activity));

        buttonWhereAmI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWhereAmI = new Intent(MenuActivity.this,MapsActivity.class);
                startActivity(intentWhereAmI);
            }
        });
    }


    private void setImageButtonOptions(){
        final ImageButton imageButtonOptions = (ImageButton) findViewById(R.id.imageButtonOptions);
        imageButtonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(MenuActivity.this, imageButtonOptions);
                popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().equals(getString(R.string.menu_popup_synchronization))){
                            setDatabaseSynchronizationFalse();
                            synchronizeDatabase();
                        }
                        else if(item.getTitle().equals(getString(R.string.menu_popup_synchronization_options))){
                            Intent intentSynchronization = new Intent(MenuActivity.this,SynchronizationActivity.class);
                            startActivityForResult(intentSynchronization,MY_REQUEST_ID);
                        }
                        else if(item.getTitle().equals(getString(R.string.menu_popup_download_photos))){
                            Intent intentDownload = new Intent(MenuActivity.this,DownloadOrRemovePhotosActivity.class);
                            intentDownload.putExtra(DOWNLOAD_OR_REMOVE,DOWNLOAD);
                            startActivity(intentDownload);
                        }
                        else if(item.getTitle().equals(getString(R.string.menu_popup_clear_memory))){
                            Intent intentRemove = new Intent(MenuActivity.this,DownloadOrRemovePhotosActivity.class);
                            intentRemove.putExtra(DOWNLOAD_OR_REMOVE,REMOVE);
                            startActivity(intentRemove);
                        }
                        else if(item.getTitle().equals(getString(R.string.menu_popup_logout))){
                            Intent intentLogin = new Intent(MenuActivity.this, LoginActivity.class);
                            startActivity(intentLogin);
                            finish();
                        }
                        popup.dismiss();
                        return true;
                    }
                });

                popup.show();
            }
        });
    }

    private void setButtonAddTour(){
        buttonAddTour = (Button) findViewById(R.id.buttonAddTour);
        buttonAddTour.setText(getString(R.string.add_tour_button_menu_activity));

        buttonAddTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addTourIntent = new Intent(MenuActivity.this,AddTourActivity.class);
                startActivity(addTourIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_ID && resultCode == RESULT_OK) {

            if(data.getIntExtra(SYNCHRONIZATION_STATUS,0) == SYNCHRONIZE_TO_FALSE){
                setDatabaseSynchronizationFalse();
                synchronizeDatabase();
            }
            else if(data.getIntExtra(SYNCHRONIZATION_STATUS,0) == SYNCHRONIZE_TO_TRUE){
                setDatabaseSynchronizationTrue();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        createAlertDialogCheckWhatUserWantToDoLogoutOrExit();
    }
}
