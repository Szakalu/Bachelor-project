package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskAddTour;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.TourAndTourAttractions;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.serializable.SerializableTour;

public class PickAttractionsForTourActivity extends AppCompatActivity implements AsyncResponse {

    public static final int PLEASE_WAIT_DIALOG = 1;
    private static final String TOUR = "Tour";
    private static final String ATTRACTION = "Attraction";
    private static final String SHOW_ATTRACTIONS = "Show_Attractions";
    private static final String TOUR_ATTRACTIONS = "Tour_Attractions";
    private static final String ATTRACTIONS_LIST = "Attractions_List";
    private final static int MY_REQUEST_ID = 1;
    private static final int MAX_ATTRACTIONS_ON_SITE = 10;

    private TextView textViewAttractionPages;
    private TextView textViewAttractionCount;
    private LinearLayout linearLayoutAttractions;
    private Button buttonAddTour;
    private Button buttonNext;
    private Button buttonBack;


    private ArrayList<Integer> attractionIds = new ArrayList<>();
    private ArrayList<Integer> tourAttractionsIds = new ArrayList<>();

    private int attractionsInTourCount = 0;

    private int currentSiteNumber;
    private int maxSiteNumber;

    private int firstAttractionIndexOnSite;
    private int lastAttractionIndexOnSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_attractions_for_tour);
        getAttractionIdsFromAddTourActivity();
        setAttractionCount();
        setTextViewAttractionCount();
        setLinearLayoutAttractions();
        getAttractionsForTour();
        setButtonAddTour();
        setButtonNext();
        setButtonBack();
        setTextViewAttractionPages();
        setSiteNumbersOnCreate();
        setTextViewAttractionPagesText();
        displayAttractionsOnCreate();
        setButtonShowAttractionsInTour();
    }

    private SerializableTour getSerializableTourFromIntent(){
        Intent intent = getIntent();
        return (SerializableTour) intent.getSerializableExtra(TOUR);
    }

    private void getAttractionIdsFromAddTourActivity(){
        Intent intent = getIntent();
        tourAttractionsIds =  intent.getIntegerArrayListExtra(ATTRACTIONS_LIST);
    }

    private void setAttractionCount(){
        attractionsInTourCount = tourAttractionsIds.size();
    }

    private void setTextViewAttractionPages(){
        textViewAttractionPages = (TextView) findViewById(R.id.textViewAttractionPages);
    }

    private void setTextViewAttractionCount(){
        textViewAttractionCount = (TextView) findViewById(R.id.textViewAttractionCount);
        textViewAttractionCount.setText(getString(R.string.text_view_attraction_count_pick_attraction_for_tour_activity) + " " + attractionsInTourCount);
        textViewAttractionCount.setTextSize(20);
        textViewAttractionCount.setTextColor(Color.BLACK);
    }

    private void setLinearLayoutAttractions(){
        linearLayoutAttractions = (LinearLayout) findViewById(R.id.linearLayoutAttractions);
        linearLayoutAttractions.setOrientation(LinearLayout.VERTICAL);
        linearLayoutAttractions.setPadding(4,4,4,4);
        linearLayoutAttractions.setBackgroundColor(Color.BLACK);
    }

    private void getAttractionsForTour(){
        SerializableTour serializableTour = getSerializableTourFromIntent();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursorAttractions = databaseTraveler.getRowAttractionWhereStateAndCountry(serializableTour.getTourStateId());
        while (cursorAttractions.moveToNext()){
            attractionIds.add(cursorAttractions.getInt(0));
        }
        databaseTraveler.close();
    }

    private void setButtonAddTour(){
        buttonAddTour = (Button) findViewById(R.id.buttonAddTour);
        buttonAddTour.setText(getString(R.string.button_add_tour_pick_attraction_for_tour_activity));

        buttonAddTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tourAttractionsIds.size()>1){
                    TourAndTourAttractions tourAndTourAttractions = new TourAndTourAttractions(getSerializableTourFromIntent(),tourAttractionsIds);
                    AsyncTaskAddTour.delegate = PickAttractionsForTourActivity.this;
                    new AsyncTaskAddTour(PickAttractionsForTourActivity.this).execute(tourAndTourAttractions);
                }
            }
        });
    }

    private void setButtonNext(){
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setText(getString(R.string.button_next_pick_attraction_for_tour_activity));

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonNext();
                currentSiteNumber++;
                checkSitesNumberAndDisableButtonIfNeeded();
                buttonBack.setEnabled(true);
                setTextViewAttractionPagesText();
            }
        });
    }

    private void onClickButtonNext(){
        linearLayoutAttractions.removeAllViews();
        onClickButtonNextSetFirstAndLastIndex();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (int idIndex = firstAttractionIndexOnSite; idIndex<=lastAttractionIndexOnSite;++idIndex) {
            setOrChangeLinearLayoutAttractions(databaseTraveler,attractionIds.get(idIndex));
        }
        databaseTraveler.close();
        Log.i("FirstIndex",firstAttractionIndexOnSite+"");
        Log.i("LastIndex",lastAttractionIndexOnSite+"");
    }

    private void onClickButtonNextSetFirstAndLastIndex(){
        firstAttractionIndexOnSite = onClickButtonNextSetFirstIndex();
        lastAttractionIndexOnSite = onClickButtonNextSetLastIndex();
    }

    private int onClickButtonNextSetFirstIndex(){
        return lastAttractionIndexOnSite+1;
    }

    private int onClickButtonNextSetLastIndex(){
        if(attractionIds.size() - (currentSiteNumber*MAX_ATTRACTIONS_ON_SITE) >= MAX_ATTRACTIONS_ON_SITE){
            return MAX_ATTRACTIONS_ON_SITE;
        }
        return lastAttractionIndexOnSite + attractionIds.size() - (currentSiteNumber*MAX_ATTRACTIONS_ON_SITE);
    }

    private void setButtonBack(){
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setText(getString(R.string.button_back_pick_attraction_for_tour_activity));

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonNext.setEnabled(true);
                onClickButtonBack();
                currentSiteNumber--;
                setTextViewAttractionPagesText();
                checkSitesNumberAndDisableButtonIfNeeded();
            }
        });
    }

    private void onClickButtonBack(){
        linearLayoutAttractions.removeAllViews();
        onClickButtonBackSetFirstAndLastIndex();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (int idIndex = firstAttractionIndexOnSite; idIndex<=lastAttractionIndexOnSite;++idIndex) {
            setOrChangeLinearLayoutAttractions(databaseTraveler,attractionIds.get(idIndex));
        }
        databaseTraveler.close();
        Log.i("FirstIndex",firstAttractionIndexOnSite+"");
        Log.i("LastIndex",lastAttractionIndexOnSite+"");
    }

    private void onClickButtonBackSetFirstAndLastIndex(){
        firstAttractionIndexOnSite = onClickButtonBackSetFirstIndex();
        lastAttractionIndexOnSite = onClickButtonBackSetLastIndex();
    }

    private int onClickButtonBackSetFirstIndex(){
        return firstAttractionIndexOnSite-MAX_ATTRACTIONS_ON_SITE;
    }

    private int onClickButtonBackSetLastIndex(){
        return firstAttractionIndexOnSite+9;
    }

    private void setButtonShowAttractionsInTour(){
        Button buttonShowTourAttractions = (Button) findViewById(R.id.buttonShowTourAttractions);
        buttonShowTourAttractions.setText(getString(R.string.button_show_tour_attractions_pick_attraction_for_tour_activity));

        buttonShowTourAttractions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(attractionsInTourCount>0){
                    Intent showTourAttractionsIntent = new Intent(PickAttractionsForTourActivity.this,ShowTourAttractionsActivity.class);
                    showTourAttractionsIntent.putExtra(SHOW_ATTRACTIONS, tourAttractionsIds);
                    //startActivity(showTourAttractionsIntent);
                    startActivityForResult(showTourAttractionsIntent,MY_REQUEST_ID);
                }
            }
        });
    }

    private void displayAttractionsOnCreate(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        if(attractionIds.size()<=10){
            for (Integer attractionId: attractionIds) {
                setOrChangeLinearLayoutAttractions(databaseTraveler,attractionId);
            }
        }
        else{
            for (int idIndex = 0; idIndex<MAX_ATTRACTIONS_ON_SITE;++idIndex) {
                setOrChangeLinearLayoutAttractions(databaseTraveler,attractionIds.get(idIndex));
            }
            setFirstAndLastAttractionIndex();
        }
        databaseTraveler.close();
        checkSitesNumberAndDisableButtonIfNeeded();
    }

    private void setOrChangeLinearLayoutAttractions(DatabaseTraveler databaseTraveler, int attractionId){
        LinearLayout linearLayoutFirst = new LinearLayout(this);

        final Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);

        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setText(getString(R.string.attraction_name_table_row_browse_attraction_activity) + " " + attraction.getName() + "\n"
                + getString(R.string.attraction_country_table_row_browse_attraction_activity) + " " + getCountryName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_state_table_row_browse_attraction_activity) + " " + getStateName(attraction, databaseTraveler) + "\n"
                + getString(R.string.attraction_description_table_row_browse_attraction_activity) + " " + getTwentyFiveSignsFromDescription(attraction.getDescription()) + "\n"
                + "Autor: " + "Turysta");

        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayoutFirst.addView(textView);

        linearLayoutFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent attractionDetailsIntent = new Intent(PickAttractionsForTourActivity.this,AttractionDetailsActivity.class);
                attractionDetailsIntent.putExtra(ATTRACTION,attraction.getId());
                startActivity(attractionDetailsIntent);
            }
        });

        linearLayoutFirst.setBackgroundColor(Color.BLACK);

        linearLayoutAttractions.addView(linearLayoutFirst);

        LinearLayout linearLayoutSecond = new LinearLayout(this);
        linearLayoutSecond.setBackgroundColor(Color.WHITE);

        Button buttonAddAttraction =  createButtonAddAttraction();
        Button buttonRemoveAttraction = createButtonRemoveAttraction();

        setButtonAddAttraction(buttonAddAttraction, buttonRemoveAttraction, linearLayoutSecond, attraction);
        setButtonRemoveAttraction(buttonRemoveAttraction, buttonAddAttraction, linearLayoutSecond, attraction);
        linearLayoutAttractions.addView(linearLayoutSecond);

        LinearLayout linearLayoutSeparator = new LinearLayout(this);
        linearLayoutSeparator.setBackgroundColor(Color.BLACK);
        linearLayoutSeparator.setPadding(0,0,0,4);
        linearLayoutSeparator.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayoutAttractions.addView(linearLayoutSeparator);
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
        if(countryName.equals(getStringByLocal(PickAttractionsForTourActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(PickAttractionsForTourActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(PickAttractionsForTourActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(PickAttractionsForTourActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(PickAttractionsForTourActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(PickAttractionsForTourActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
        }
        return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private Button createButtonAddAttraction(){
        Button buttonAddAttraction = new Button(this);
        buttonAddAttraction.setText(getString(R.string.button_add_pick_attraction_for_tour_activity));

        return buttonAddAttraction;
    }

    private Button createButtonRemoveAttraction(){
        Button buttonRemoveAttraction = new Button(this);
        buttonRemoveAttraction.setText(getString(R.string.button_remove_pick_attraction_for_tour_activity));

        return buttonRemoveAttraction;
    }

    private void setButtonAddAttraction(final Button buttonAddAttraction, final Button buttonRemoveAttraction, LinearLayout linearLayoutSecond, final Attraction attraction){
        buttonAddAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRemoveAttraction.setEnabled(true);
                buttonAddAttraction.setEnabled(false);
                tourAttractionsIds.add(attraction.getId());
                attractionsInTourCount++;
                changeAttractionsCountInTour();
            }
        });
        if(checkIfAttractionIsInTour(attraction.getId())){
            buttonAddAttraction.setEnabled(false);
        }
        linearLayoutSecond.addView(buttonAddAttraction);
    }

    private void setButtonRemoveAttraction(final Button buttonRemoveAttraction, final Button buttonAddAttraction, LinearLayout linearLayoutSecond, final Attraction attraction){
        buttonRemoveAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAddAttraction.setEnabled(true);
                buttonRemoveAttraction.setEnabled(false);
                removeFromTourAttractionList(attraction.getId());
                attractionsInTourCount--;
                changeAttractionsCountInTour();
            }
        });
        if(!checkIfAttractionIsInTour(attraction.getId())){
            buttonRemoveAttraction.setEnabled(false);
        }

        linearLayoutSecond.addView(buttonRemoveAttraction);
    }

    private boolean checkIfAttractionIsInTour(int attractionId){
        for (int attractionInTourId: tourAttractionsIds) {
            if(attractionInTourId == attractionId){
                return true;
            }
        }
        return false;
    }

    private String getTwentyFiveSignsFromDescription(String description){
        if(description.length()>25){
            return description.substring(0,25) + ".....";
        }
        return description;
    }

    private void changeAttractionsCountInTour(){
        textViewAttractionCount.setText(getString(R.string.text_view_attraction_count_pick_attraction_for_tour_activity) + " " + attractionsInTourCount);
    }

    private void removeFromTourAttractionList(int attractionToRemove){
        for (int i = 0; i < tourAttractionsIds.size(); ++i){
            if(tourAttractionsIds.get(i) == attractionToRemove){
                tourAttractionsIds.remove(i);
                break;
            }
        }
    }

    private int setMaxSiteNumber(){
        if(attractionIds.size() == 0){
            return 0;
        }
        else if(attractionIds.size()<=10){
            return 1;
        }
        return calculateHowManySitesWithAttractions();

    }

    private int calculateHowManySitesWithAttractions(){
        if(attractionIds.size()%MAX_ATTRACTIONS_ON_SITE == 0){
            return attractionIds.size()/MAX_ATTRACTIONS_ON_SITE;
        }
        return (attractionIds.size()/10) + 1;
    }

    private int setFirstSiteNumber(){
        if(maxSiteNumber > 0){
            return 1;
        }
        return 0;
    }

    private void setSiteNumbersOnCreate(){
        maxSiteNumber = setMaxSiteNumber();
        currentSiteNumber = setFirstSiteNumber();
    }

    private void setTextViewAttractionPagesText(){
        textViewAttractionPages.setText(currentSiteNumber + "/" + maxSiteNumber);
    }

    private void checkSitesNumberAndDisableButtonIfNeeded(){
        if(currentSiteNumber == maxSiteNumber && maxSiteNumber > 1){
            buttonNext.setEnabled(false);
        }
        else if(maxSiteNumber > 1 && currentSiteNumber == 1){
            buttonBack.setEnabled(false);
        }
        else if(maxSiteNumber == 0){
            buttonBack.setEnabled(false);
            buttonNext.setEnabled(false);
        }
    }

    private void setFirstAndLastAttractionIndex(){
        firstAttractionIndexOnSite = 0;
        lastAttractionIndexOnSite = 9;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_ID && resultCode == RESULT_OK) {
            ArrayList<Integer> tourAttractionsIdsFromShowTourAttractions = data.getIntegerArrayListExtra(TOUR_ATTRACTIONS);
            if (checkIfAttractionsInTourChanged(tourAttractionsIdsFromShowTourAttractions)) {
                tourAttractionsIds = tourAttractionsIdsFromShowTourAttractions;
                attractionsInTourCount = tourAttractionsIdsFromShowTourAttractions.size();
                setTextViewAttractionPagesText();
                reloadLinearLayoutAttractions();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkIfAttractionsInTourChanged(ArrayList<Integer> tourAttractionsIdsFromShowTourAttractions) {
        if(tourAttractionsIdsFromShowTourAttractions.size() != tourAttractionsIds.size()){
            return true;
        }
        return false;
    }

    private void reloadLinearLayoutAttractions(){
        linearLayoutAttractions.removeAllViews();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (int idIndex = firstAttractionIndexOnSite; idIndex<=lastAttractionIndexOnSite;++idIndex) {
            setOrChangeLinearLayoutAttractions(databaseTraveler,attractionIds.get(idIndex));
        }
        databaseTraveler.close();
    }

    @Override
    public void processFinish(Integer output) {
        switch (output){
            case 1:
                Toast.makeText(this, "Tour added", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
                break;
            case 2:

                break;

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Adding Tour");
                dialog.setMessage("Wait !!!");
                dialog.setCancelable(false);
                return dialog;

            default:
                break;
        }

        return null;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ATTRACTIONS_LIST,tourAttractionsIds);
        setResult(RESULT_CANCELED,intent);
        finish();
    }
}
