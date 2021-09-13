package math.uni.lodz.pl.jacek.traveler;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Trip;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class MyTripsActivity extends AppCompatActivity {

    private final static int ATTRACTIONS_SYNCHRONIZED = 1;
    private final static int ATTRACTIONS_NOT_SYNCHRONIZED = 0;

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";

    private static final String ATTRACTION = "Attraction";
    private final static String TOUR = "Tour";
    private final static String TRIP = "Trip";

    private LinearLayout linearLayoutMyTrips;
    private ArrayList<Integer> myTripsIndexes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        setMyTripsIndexes();
        setTextViewMyTrips();
        setLinearLayoutMyTrips();
        setAllTripsToLinearLayoutTrips();
    }

    private void showMyTripsIndexes(){
        for (Integer integer: myTripsIndexes) {
            Log.i("MyTripIndex",integer + "");
        }
    }

    private void setMyTripsIndexes(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowsTrips();
        while(cursor.moveToNext()){
            myTripsIndexes.add(cursor.getInt(0));
        }
        databaseTraveler.close();
        showMyTripsIndexes();
    }

    private void setTextViewMyTrips(){
        TextView textViewMyTrips = (TextView) findViewById(R.id.textViewMyTrips);
        textViewMyTrips.setTextSize(20);
        textViewMyTrips.setTextColor(Color.BLACK);
        textViewMyTrips.setText(getString(R.string.text_view_my_trips_my_trips_activity));
    }

    private void setLinearLayoutMyTrips(){
        linearLayoutMyTrips = (LinearLayout) findViewById(R.id.linearLayoutMyTrips);
        linearLayoutMyTrips.setOrientation(LinearLayout.VERTICAL);
        linearLayoutMyTrips.setPadding(4,4,4,4);
        linearLayoutMyTrips.setBackgroundColor(Color.BLACK);
    }

    private void setAllTripsToLinearLayoutTrips(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for(int tripPositionOnList = 0; tripPositionOnList < myTripsIndexes.size(); ++tripPositionOnList){
            final Trip trip = databaseTraveler.getTrip(myTripsIndexes.get(tripPositionOnList));
            if(checkOwnerNick(trip.getOwner()) && checkIfStateIsSynchronized(databaseTraveler, trip.getStateName())){
                setTripToLinearLayoutMyTrips(databaseTraveler, tripPositionOnList, trip);
            }
        }
        databaseTraveler.close();
    }

    private boolean checkOwnerNick(String tripName){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        return tripName.equals(pref.getString(PREF_USERNAME,""));
    }

    private boolean checkIfStateIsSynchronized(DatabaseTraveler databaseTraveler, String stateName){
        if(databaseTraveler.getStateWhereStateName(stateName).getToSynchronized() == ATTRACTIONS_SYNCHRONIZED){
            return true;
        }
        return false;
    }

    private void setTripToLinearLayoutMyTrips(DatabaseTraveler databaseTraveler, int tripPositionOnList, final Trip trip){
        LinearLayout linearLayoutFirst = new LinearLayout(this);

        TextView textViewTripDetails = createTextViewForTourOrAttraction(databaseTraveler, trip);

        textViewTripDetails.setBackgroundColor(Color.WHITE);
        textViewTripDetails.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayoutFirst.addView(textViewTripDetails);
        linearLayoutFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShowMyTrip = new Intent(MyTripsActivity.this,ShowMyTripActivity.class);
                intentShowMyTrip.putExtra(TRIP,trip.getTripId());
                startActivity(intentShowMyTrip);
            }
        });

        linearLayoutFirst.setBackgroundColor(Color.BLACK);

        linearLayoutMyTrips.addView(linearLayoutFirst);

        LinearLayout linearLayoutSecond = new LinearLayout(this);
        linearLayoutSecond.setBackgroundColor(Color.WHITE);
        Button buttonDelete = createButtonDelete();
        linearLayoutSecond.addView(buttonDelete);
        linearLayoutSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShowMyTrip = new Intent(MyTripsActivity.this,ShowMyTripActivity.class);
                intentShowMyTrip.putExtra(TRIP,trip.getTripId());
                startActivity(intentShowMyTrip);
            }
        });

        linearLayoutMyTrips.addView(linearLayoutSecond);

        if(tripPositionOnList != myTripsIndexes.size()-1){
            LinearLayout linearLayoutSeparator = new LinearLayout(this);
            linearLayoutSeparator.setBackgroundColor(Color.BLACK);
            linearLayoutSeparator.setPadding(0,0,0,4);
            linearLayoutSeparator.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            linearLayoutMyTrips.addView(linearLayoutSeparator);
        }

    }

    private TextView createTextViewForTourOrAttraction(DatabaseTraveler databaseTraveler, Trip trip){
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);

        if(trip.getAttractionOrTour().equals(ATTRACTION)){
            textView.setText(createTextForTextViewAttraction(databaseTraveler, trip));
        }
        else if(trip.getAttractionOrTour().equals(TOUR)){
            textView.setText(createTextForTextViewTour(databaseTraveler, trip));
        }
        return textView;
    }

    private String createTextForTextViewAttraction(DatabaseTraveler databaseTraveler, Trip trip){
        Attraction attraction = databaseTraveler.getOneRowAttractions(Integer.parseInt(trip.getAttractionOrTourId()));
        StringBuilder tripBuilder = new StringBuilder();
        //tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_name_my_trips_activity)).append(attraction.getAttractionName()).append(" ").append("\n");
        //tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_country_my_trips_activity)).append(attraction.getCountryName()).append(" ").append("\n");
        //tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_state_my_trips_activity)).append(attraction.getStateName()).append(" ").append("\n");
        tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_data_my_trips_activity)).append(" ").append(trip.getData());
        return tripBuilder.toString();
    }

    private String createTextForTextViewTour(DatabaseTraveler databaseTraveler, Trip trip){
        Tour tour = databaseTraveler.getOneRowTours(Integer.parseInt(trip.getAttractionOrTourId()));
        StringBuilder tripBuilder = new StringBuilder();
        tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_name_my_trips_activity)).append(tour.getTourName()).append(" ").append("\n");
        //tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_country_my_trips_activity)).append(tour.getCountryName()).append(" ").append("\n");
        //tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_state_my_trips_activity)).append(tour.getStateName()).append(" ").append("\n");
        tripBuilder.append(getString(R.string.linear_layout_my_trips_trip_data_my_trips_activity)).append(" ").append(trip.getData());
        return tripBuilder.toString();
    }

    private int countHowManyAttractionsInTour(int tourId){
        int attractionCount = 0;
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowTourAttractionsWhereTourId(tourId);
        while (cursor.moveToNext()){
            attractionCount++;
        }
        return attractionCount;
    }

    private Button createButtonDelete(){
        Button button = new Button(this);
        button.setText(getString(R.string.linear_layout_my_trips_delete_my_trips_activity));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialogCheckIfUserReallyWantToDeleteTrip();
            }
        });
        return button;
    }

    private void createAlertDialogCheckIfUserReallyWantToDeleteTrip(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MyTripsActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_text_delete_my_trips_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_my_trips_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_my_trips_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
