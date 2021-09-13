package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class TourDetailsActivity extends AppCompatActivity {

    private TextView textViewTourName;
    private TextView textViewTourDetails;
    private TextView textViewTourDescription;
    private LinearLayout linearLayoutAttractions;

    private static final String TOUR = "Tour";
    private static final String ATTRACTION = "Attraction";
    private static final String TOUR_ATTRACTIONS = "TourAttractions";
    private final static String TRIP = "Trip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_details);
        setTextViewTourName();
        setTextViewTourDetails();
        setTextViewTourDescription();
        setTourInformation(getTourIdFromIntent());
        setLinearLayoutAttractions();
        setButtonMap();
        setButtonStartTrip();
    }

    private void setButtonStartTrip(){
        Button buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
        buttonStartTrip.setTextSize(16);
        buttonStartTrip.setText(getString(R.string.button_start_trip_tour_details_activity));

        buttonStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTripAttractions = new Intent(TourDetailsActivity.this,TripAttractionsActivity.class);
                intentTripAttractions.putExtra(TRIP,getTourIdFromIntent());
                startActivity(intentTripAttractions);
                finish();
            }
        });
    }

    private void setTextViewTourName(){
        textViewTourName = (TextView) findViewById(R.id.textViewTourName);
        textViewTourName.setTextSize(26);
        textViewTourName.setTextColor(Color.BLACK);
    }

    private void setTextViewTourDetails(){
        textViewTourDetails = (TextView) findViewById(R.id.textViewTourDetails);
        textViewTourDetails.setTextSize(16);
        textViewTourDetails.setTextColor(Color.BLACK);
    }

    private void setTextViewTourDescription(){
        textViewTourDescription = (TextView) findViewById(R.id.textViewTourDescription);
        textViewTourDescription.setTextSize(16);
        textViewTourDescription.setTextColor(Color.BLACK);
    }

    private int getTourIdFromIntent(){
        Intent intentExtras = getIntent();
        return intentExtras.getIntExtra(TOUR,-1);
    }

    private void setTourInformation(int tourId){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Tour tour = databaseTraveler.getOneRowTours(tourId);
        textViewTourName.setText(tour.getTourName());
        textViewTourDetails.setText(createTourDetailsText(tour, databaseTraveler));
        textViewTourDescription.setText(tour.getDescription());
        databaseTraveler.close();
    }



    private String createTourDetailsText(Tour tour, DatabaseTraveler databaseTraveler){
        StringBuffer attractionDetails = new StringBuffer();
        attractionDetails.append(getString(R.string.tour_country_tour_details_activity) + " " + getTourCountryName(tour, databaseTraveler) + "\n");
        attractionDetails.append(getString(R.string.tour_state_tour_details_activity) + " " + getStateTourName(tour, databaseTraveler) + "\n");
        attractionDetails.append(getString(R.string.tour_attractions_count_tour_details_activity) + " " + countHowManyAttractionsInTour(tour.getId()) + "\n");
        return attractionDetails.toString();
    }

    private String getTourCountryName(Tour tour, DatabaseTraveler databaseTraveler){
        int countryId = databaseTraveler.getStateWhereStateId(tour.getStateId()).getCountryId();
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String getStateTourName(Tour tour, DatabaseTraveler databaseTraveler){
        State state = databaseTraveler.getStateWhereStateId(tour.getStateId());
        return state.getStateName();
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

    private void setLinearLayoutAttractions(){
        linearLayoutAttractions = (LinearLayout) findViewById(R.id.linearLayoutAttractions);
        final ArrayList<Integer> tourAttractionsList = getAttractionsFromTour(getTourIdFromIntent());
        linearLayoutAttractions.setBackgroundColor(Color.BLACK);
        linearLayoutAttractions.setPadding(4,4,4,4);

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for(int index = 0; index < tourAttractionsList.size(); ++index) {
            LinearLayout linearLayoutAttraction = new LinearLayout(this);

            final Attraction attraction = databaseTraveler.getOneRowAttractions(tourAttractionsList.get(index));

            TextView textView = setTextViewAttraction(attraction, databaseTraveler);

            linearLayoutAttraction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent attractionDetailsIntent = new Intent(TourDetailsActivity.this,AttractionDetailsActivity.class);
                    attractionDetailsIntent.putExtra(ATTRACTION,attraction.getId());
                    startActivity(attractionDetailsIntent);
                }
            });
            linearLayoutAttraction.addView(textView);
            linearLayoutAttractions.addView(linearLayoutAttraction);

            if(index != tourAttractionsList.size()-1){
                LinearLayout linearLayoutSeparator = new LinearLayout(this);
                linearLayoutSeparator.setBackgroundColor(Color.BLACK);
                linearLayoutSeparator.setPadding(0,0,0,4);
                linearLayoutAttractions.addView(linearLayoutSeparator);
            }
        }
        databaseTraveler.close();
    }

    private TextView setTextViewAttraction(Attraction attraction, DatabaseTraveler databaseTraveler){
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("\n" + getString(R.string.attraction_name_table_row_browse_attraction_activity) + " " + attraction.getName() + "\n"
                + getString(R.string.attraction_country_table_row_browse_attraction_activity) + " " + getCountryName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_state_table_row_browse_attraction_activity) + " " + getStateName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_description_table_row_browse_attraction_activity) + " " + getTwentyFiveSignsFromDescription(attraction.getDescription()) + "\n"
                + getString(R.string.attraction_author_table_row_browse_attraction_activity) + " " + attraction.getAuthor() + "\n");
        return textView;
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
        if(countryName.equals(getStringByLocal(TourDetailsActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(TourDetailsActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(TourDetailsActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(TourDetailsActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(TourDetailsActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(TourDetailsActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
        }
        return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
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

    private String getTwentyFiveSignsFromDescription(String description){
        if(description.length()>25){
            return description.substring(0,25) + ".....";
        }
        return description;
    }

    private void setButtonMap(){
        Button buttonMap = (Button) findViewById(R.id.buttonShowMap);

        buttonMap.setTextSize(16);
        buttonMap.setTextColor(Color.BLACK);
        buttonMap.setText(getString(R.string.button_map_tour_details_activity));

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTourAttractionsMap = new Intent(TourDetailsActivity.this,TourAttractionsMapActivity.class);
                intentTourAttractionsMap.putExtra(TOUR_ATTRACTIONS,getAttractionsFromTour(getTourIdFromIntent()));
                startActivity(intentTourAttractionsMap);
            }
        });
    }
}
