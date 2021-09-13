package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class TripAttractionsActivity extends AppCompatActivity {

    private final static String TOUR = "Tour";
    private final static String TRIP = "Trip";
    private final static String UP = "Up";
    private final static String DOWN = "Down";
    private final static String WHAT_IT_IS = "WhatItIs";
    private static final String TOUR_ATTRACTIONS = "TourAttractions";

    private LinearLayout linearLayoutAttractions;

    private int tourId;
    private ArrayList<Integer> tourAttractionsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_attractions);
        getTourIdAndTourAttractionsList();
        setTextViewTrip();
        setLinearLayoutAttractions();
        setButtonStartTrip();
        setButtonPreviewTrip();
        displayAttractions();
    }

    private void getTourIdAndTourAttractionsList(){
        Intent intent = getIntent();
        tourId = intent.getIntExtra(TRIP,-1);
        tourAttractionsList = getAttractionsFromTour(tourId);
    }

    private ArrayList getAttractionsFromTour(int tourId){
        ArrayList<Integer> toursAttractionsList = new ArrayList();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowTourAttractionsWhereTourId(tourId);
        while (cursor.moveToNext()){
            toursAttractionsList.add(cursor.getInt(2));
        }
        databaseTraveler.close();
        return toursAttractionsList;
    }

    private void setTextViewTrip(){
        TextView textViewTrip = (TextView) findViewById(R.id.textViewTrip);
        textViewTrip.setTextSize(24);
        textViewTrip.setTextColor(Color.BLACK);
        textViewTrip.setText(getTourName());
        //textViewTrip.setText(getString(R.string.text_view_trip_trip_attractions_activity));
    }

    private String getTourName(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        String tourName = databaseTraveler.getOneRowTours(tourId).getTourName();
        databaseTraveler.close();
        return tourName;
    }

    private void setButtonStartTrip(){
        Button buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
        buttonStartTrip.setText(getString(R.string.button_start_trip_trip_attractions_activity));
        buttonStartTrip.setTextSize(16);
        buttonStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTripActivity = new Intent(TripAttractionsActivity.this,TripActivity.class);
                intentTripActivity.putExtra(WHAT_IT_IS,TRIP);
                intentTripActivity.putExtra(TRIP,tourAttractionsList);
                intentTripActivity.putExtra(TOUR,tourId);
                startActivity(intentTripActivity);
            }
        });
    }

    private void setButtonPreviewTrip(){
        Button buttonPreviewTrip = (Button) findViewById(R.id.buttonPreviewTrip);
        buttonPreviewTrip.setText(getString(R.string.button_preview_trip_trip_attractions_activity));
        buttonPreviewTrip.setTextSize(16);
        buttonPreviewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewTripIntent = new Intent(TripAttractionsActivity.this,PreviewTripActivity.class);
                previewTripIntent.putExtra(TOUR_ATTRACTIONS,tourAttractionsList);
                startActivity(previewTripIntent);
            }
        });
    }

    private void setLinearLayoutAttractions(){
        linearLayoutAttractions = (LinearLayout) findViewById(R.id.linearLayoutAttractions);
        linearLayoutAttractions.setOrientation(LinearLayout.VERTICAL);
        linearLayoutAttractions.setPadding(4,4,4,4);
        linearLayoutAttractions.setBackgroundColor(Color.BLACK);
    }

    private void displayAttractions(){
        clearLinearLayoutAttractions();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for(int indexOnList = 0; indexOnList<tourAttractionsList.size();++indexOnList){
            setOrChangeLinearLayoutAttractions(databaseTraveler,tourAttractionsList.get(indexOnList), indexOnList);
        }
        databaseTraveler.close();
    }

    private void setOrChangeLinearLayoutAttractions(DatabaseTraveler databaseTraveler, int attractionId, int attractionIndexOnList){
        LinearLayout linearLayoutWithNumber = new LinearLayout(this);
        TextView textViewWithIndexNumber = new TextView(this);
        textViewWithIndexNumber.setTextSize(18);
        textViewWithIndexNumber.setTextColor(Color.BLACK);
        textViewWithIndexNumber.setText(attractionIndexOnList + 1 + ".");
        textViewWithIndexNumber.setBackgroundColor(Color.WHITE);
        textViewWithIndexNumber.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayoutWithNumber.addView(textViewWithIndexNumber);

        linearLayoutAttractions.addView(linearLayoutWithNumber);


        LinearLayout linearLayoutFirst = new LinearLayout(this);

        final Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);

        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setText(getString(R.string.attraction_name_table_row_browse_attraction_activity) + " " + attraction.getName() + "\n"
                + getString(R.string.attraction_country_table_row_browse_attraction_activity) + " " + getCountryName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_state_table_row_browse_attraction_activity) + " " + getStateName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_description_table_row_browse_attraction_activity) + " " + getTwentyFiveSignsFromDescription(attraction.getDescription()) + "\n"
                + getString(R.string.attraction_author_table_row_browse_attraction_activity) + " " + attraction.getAuthor());

        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayoutFirst.addView(textView);

        linearLayoutFirst.setBackgroundColor(Color.BLACK);

        linearLayoutAttractions.addView(linearLayoutFirst);

        LinearLayout linearLayoutSecond = new LinearLayout(this);
        linearLayoutSecond.setBackgroundColor(Color.WHITE);

        Button buttonUp = setButtonUp(attractionId, attractionIndexOnList);
        Button buttonDown = setButtonDown(attractionId, attractionIndexOnList);

        linearLayoutSecond.addView(buttonUp);
        linearLayoutSecond.addView(buttonDown);

        linearLayoutAttractions.addView(linearLayoutSecond);


        if(tourAttractionsList.indexOf(attractionId) != tourAttractionsList.size()-1){
            LinearLayout linearLayoutSeparator = new LinearLayout(this);
            linearLayoutSeparator.setBackgroundColor(Color.BLACK);
            linearLayoutSeparator.setPadding(0,0,0,4);
            linearLayoutSeparator.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            linearLayoutAttractions.addView(linearLayoutSeparator);
        }

    }

    private String getCategory(Attraction attraction, DatabaseTraveler databaseTraveler){
        String categoryName = databaseTraveler.getCategoryNameById(attraction.getCategoryId());
        return translateCategory(categoryName);
    }

    private String getStateName(Attraction attraction, DatabaseTraveler databaseTraveler){
        State state = databaseTraveler.getStateWhereStateId(attraction.getStateId());
        return state.getStateName();
    }

    private String getCountryName(Attraction attraction, DatabaseTraveler databaseTraveler){
        int countryId = databaseTraveler.getStateWhereStateId(attraction.getStateId()).getCountryId();
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(TripAttractionsActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(TripAttractionsActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(TripAttractionsActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(TripAttractionsActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(TripAttractionsActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(TripAttractionsActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
        }
        return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private String getTwentyFiveSignsFromDescription(String description){
        if(description.length()>25){
            return description.substring(0,25) + ".....";
        }
        return description;
    }

    private Button setButtonUp(final int attractionId, final int attractionIndexOnList){
        Button buttonUp =  new Button(this);
        buttonUp.setText(getString(R.string.button_up_trip_attractions_activity));
        if(attractionIndexOnList == 0){
            buttonUp.setEnabled(false);
        }
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAttractionPosition(attractionId, attractionIndexOnList, UP);
            }
        });
        return buttonUp;
    }

    private Button setButtonDown(final int attractionId, final int attractionIndexOnList){
        Button buttonDown =  new Button(this);
        buttonDown.setText(getString(R.string.button_down_trip_attractions_activity));
        if(attractionIndexOnList == tourAttractionsList.size()-1){
            buttonDown.setEnabled(false);
        }
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAttractionPosition(attractionId, attractionIndexOnList, DOWN);
            }
        });
        return buttonDown;
    }

    private void changeAttractionPosition(int attractionId, int attractionPositionOnList, String upOrDown){
        tourAttractionsList.remove(attractionPositionOnList);

        if(upOrDown.equals(DOWN)){
            tourAttractionsList.add(attractionPositionOnList+1, attractionId);
        }
        else if(upOrDown.equals(UP)){
            tourAttractionsList.add(attractionPositionOnList-1, attractionId);
        }

        displayAttractions();
    }


    private void clearLinearLayoutAttractions(){
        linearLayoutAttractions.removeAllViews();
    }
}
