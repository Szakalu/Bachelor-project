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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class BrowseTourActivity extends AppCompatActivity {


    private Spinner spinnerCountry;
    private Spinner spinnerState;
    private ArrayList<Integer> toursList = new ArrayList<>();
    private LinearLayout linearLayoutTours;
    private TextView textViewSites;
    private Button buttonBack;
    private Button buttonNext;
    private ScrollView scrollViewTours;

    private static final String TOUR = "Tour";
    private static final int MAX_TOURS_ON_SITE = 10;

    private int countryId;

    private int howManySitesWithTours = 0;
    private int currentSiteNumber = 0;
    private int lastTourListIndex = 0;
    private int firstTourListIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_tour);
        setTextViewSites();
        setScrollViewTours();
        //setTableLayoutTours();
        setLinearLayoutTours();
        setButtonBack();
        setButtonNext();
        setTextViewTours();
        setSpinnerCountry();
        setSpinnerState();
    }

    private void setScrollViewTours(){
        scrollViewTours = (ScrollView) findViewById(R.id.scrollViewTours);
    }

    private void setLinearLayoutTours(){
        linearLayoutTours = (LinearLayout) findViewById(R.id.linearLayoutTours);
        linearLayoutTours.setBackgroundColor(Color.BLACK);
        linearLayoutTours.setPadding(4,4,4,4);
    }

    private void setButtonBack(){
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setText(getString(R.string.back_button_browse_tour_activity));
        buttonBack.setTextSize(16);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonNext.setEnabled(true);
                currentSiteNumber--;
                if(currentSiteNumber == 1 ){
                    buttonBack.setEnabled(false);
                }
                toursToTablesRowsWhenClickBack();
                textViewSitesSetTextAfterCurrentNumberChange();
                scrollViewTours.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        buttonBack.setEnabled(false);
    }

    private void toursToTablesRowsWhenClickBack(){
        linearLayoutTours.removeAllViews();
        firstTourListIndex = checkHowManyBackTourOnList();
        lastTourListIndex = firstTourListIndex + MAX_TOURS_ON_SITE - 1;

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (int tourListIndex = firstTourListIndex; tourListIndex < lastTourListIndex; ++tourListIndex) {
            setLinearLayoutTourRow(databaseTraveler,tourListIndex);
        }
        databaseTraveler.close();
    }

    private int checkHowManyBackTourOnList(){
        return firstTourListIndex - MAX_TOURS_ON_SITE;
    }

    private void setButtonNext(){
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setText(R.string.next_button_browse_tour_activity);
        buttonNext.setTextSize(16);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBack.setEnabled(true);
                attractionsToTablesRowsWhenClickNext();
                currentSiteNumber++;
                disableButtonNextIfToursSiteEqualsZeroOrOne();
                textViewSitesSetTextAfterCurrentNumberChange();
                scrollViewTours.fullScroll(ScrollView.FOCUS_UP);

            }
        });
    }

    private void attractionsToTablesRowsWhenClickNext(){
        linearLayoutTours.removeAllViews();

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);

        firstTourListIndex = lastTourListIndex +1;
        lastTourListIndex += checkHowManyMoreToursOnList();

        for (int tourListIndex = firstTourListIndex; tourListIndex <= lastTourListIndex; ++tourListIndex) {
            setLinearLayoutTourRow(databaseTraveler,tourListIndex);
        }

        databaseTraveler.close();
    }

    private int checkHowManyMoreToursOnList(){
        int howManyHasBeenShown = currentSiteNumber* MAX_TOURS_ON_SITE;
        if(toursList.size()  - howManyHasBeenShown < MAX_TOURS_ON_SITE){
            return toursList.size() - howManyHasBeenShown;
        }
        return MAX_TOURS_ON_SITE;
    }

    private void setTextViewTours(){
        TextView textViewAttractions = (TextView) findViewById(R.id.textViewTours);
        textViewAttractions.setText(getString(R.string.attractions_text_view_browse_tours_activity));
        textViewAttractions.setTextSize(20);
        textViewAttractions.setTextColor(Color.BLACK);
    }

    private void setSpinnerCountry() {
        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        List<String> list = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowCountry();
        while (cursor.moveToNext()) {
            list.add(translateCountryName(cursor.getString(1)));
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerCountry.setAdapter(adapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countryId = getCountryId(spinnerCountry.getSelectedItem().toString());
                changeStates(spinnerCountry.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getCountryId(String countryName){
        if(countryName.equals(getStringByLocal(BrowseTourActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))){
            countryName = getStringByLocal(BrowseTourActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private String getStateName(Tour tour, DatabaseTraveler databaseTraveler){
        State state = databaseTraveler.getStateWhereStateId(tour.getStateId());
        return state.getStateName();
    }

    private String getCountryName(DatabaseTraveler databaseTraveler){
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(BrowseTourActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(BrowseTourActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }


    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private void changeStates(String country) {
        List<String> listOfStates = new ArrayList<>();
        listOfStates.add(getString(R.string.tours_spinner_All_browse_tours_activity));
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowState();
        while (cursor.moveToNext()) {
            if (cursor.getString(2).equals(country)) {
                listOfStates.add(cursor.getString(1));
                Log.i("State", cursor.getString(1));
            }
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfStates);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerState.setAdapter(adapter);
    }

    private void setSpinnerState() {
        spinnerState = (Spinner) findViewById(R.id.spinnerState);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toursList = setToursList(toursList,spinnerCountry.getSelectedItem().toString(),spinnerState.getSelectedItem().toString());
                setLinearLayoutToursRows(toursList);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setLinearLayoutToursRows(ArrayList<Integer> toursList){
        linearLayoutTours.removeAllViews();
        if(toursList.size()>0 && toursList.size() <= MAX_TOURS_ON_SITE){
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
            for (int tourListIndex = 0; tourListIndex < toursList.size(); ++tourListIndex) {
                setLinearLayoutTourRow(databaseTraveler, tourListIndex);
            }
        }
        else if(toursList.size()>0 && toursList.size() > MAX_TOURS_ON_SITE){
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
            for (int tourListIndex = 0; tourListIndex < MAX_TOURS_ON_SITE; ++tourListIndex) {
                setLinearLayoutTourRow(databaseTraveler, tourListIndex);
            }
            lastTourListIndex = MAX_TOURS_ON_SITE -1;
            firstTourListIndex = lastTourListIndex - 9;
        }
    }

    private void setLinearLayoutTourRow(DatabaseTraveler databaseTraveler, int tourListIndex){
        LinearLayout linearLayoutTour = new LinearLayout(this);

        final Tour tour = databaseTraveler.getOneRowTours(toursList.get(tourListIndex));

        TextView textView = setTextViewTour(tour, databaseTraveler);

        linearLayoutTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent attractionDetailsIntent = new Intent(BrowseTourActivity.this,TourDetailsActivity.class);
                attractionDetailsIntent.putExtra(TOUR,tour.getId());
                startActivity(attractionDetailsIntent);
            }
        });
        linearLayoutTour.addView(textView);

        linearLayoutTours.addView(linearLayoutTour);

        if(checkIfTourIsLastOnPageOrList(tourListIndex)){
            LinearLayout linearLayoutSeparator = new LinearLayout(this);
            linearLayoutSeparator.setBackgroundColor(Color.BLACK);
            linearLayoutSeparator.setPadding(0,0,0,4);
            linearLayoutTours.addView(linearLayoutSeparator);
        }
    }

    private TextView setTextViewTour(Tour tour, DatabaseTraveler databaseTraveler){
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("\n" + getString( R.string.tour_name_table_row_browse_tour_activity) + " " + tour.getTourName() + "\n"
                + getString(R.string.tour_country_table_row_browse_tour_activity) + " " + getCountryName(databaseTraveler) + "\n"
                + getString(R.string.tour_state_table_row_browse_tour_activity) + " " + getStateName(tour, databaseTraveler) + "\n"
                + getString(R.string.tour_description_table_row_browse_tour_activity) + " " + getTwentyFiveSignsFromDescription(tour.getDescription()) + "\n"
                + getString(R.string.tour_attraction_count_table_row_browse_tour_activity) + " " + tour.getAttractionsCount() +"\n"
                + getString(R.string.tour_author_table_row_browse_tour_activity) + " " + tour.getAuthor() + "\n");
        return textView;
    }

    private boolean checkIfTourIsLastOnPageOrList(int index){
        if(index == toursList.size()-1){
            return false;
        }
        else if(index != 0){
            if((index+1)%MAX_TOURS_ON_SITE == 0){
                return false;
            }
        }
        return true;
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

    private ArrayList setToursList(ArrayList<Integer> toursList, String countryName, String stateName){
        toursList.clear();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor;
        if(stateName.equals(getString(R.string.tours_spinner_All_browse_tours_activity))){
            cursor = databaseTraveler.getRowTourWhereCountry(getAllStatesIdsFromCountry(databaseTraveler));
        }
        else {
            cursor = databaseTraveler.getRowToursWhereStateAndCountry(databaseTraveler.getStateIdWhereStateName(spinnerState.getSelectedItem().toString()));

        }
        while (cursor.moveToNext()){
            toursList.add(cursor.getInt(0));
        }
        databaseTraveler.close();
        howManySitesWithTours = calculateHowMany(toursList.size());
        currentSiteNumber = setFirstSiteNumber(howManySitesWithTours);
        textViewSitesSetTextAfterCurrentNumberChange();
        disableButtonNextIfToursSiteEqualsZeroOrOne();
        return toursList;
    }

    private String getAllStatesIdsFromCountry(DatabaseTraveler databaseTraveler){
        StringBuilder stringBuilder = new StringBuilder();
        Cursor cursor = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while (cursor.moveToNext()){
            stringBuilder.append(cursor.getInt(0));
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    private void textViewSitesSetTextAfterCurrentNumberChange(){
        textViewSites.setText(currentSiteNumber + "/" + howManySitesWithTours);
    }

    private String getTwentyFiveSignsFromDescription(String description){
        if(description.length()>25){
            return description.substring(0,25) + ".....";
        }
        return description;
    }

    private int calculateHowMany(int numberOfTours){
        if(numberOfTours > 0){
            if(numberOfTours% MAX_TOURS_ON_SITE ==0){
                return numberOfTours/ MAX_TOURS_ON_SITE;
            }
            return (numberOfTours/ MAX_TOURS_ON_SITE)+1;
        }
        return 0;
    }

    private int setFirstSiteNumber(int howManySitesWithAttractions){
        if(howManySitesWithAttractions > 0){
            return 1;
        }
        return 0;
    }

    private void setTextViewSites(){
        textViewSites = (TextView) findViewById(R.id.textViewSites);
    }

    private void disableButtonNextIfToursSiteEqualsZeroOrOne(){
        if(howManySitesWithTours == currentSiteNumber){
            buttonNext.setEnabled(false);
        }
        else{
            buttonNext.setEnabled(true);
        }
    }
}
