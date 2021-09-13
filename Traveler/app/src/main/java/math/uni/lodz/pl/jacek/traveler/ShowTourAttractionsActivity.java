package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class ShowTourAttractionsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String SHOW_ATTRACTIONS = "Show_Attractions";
    private static final String TOUR_ATTRACTIONS = "Tour_Attractions";

    private TextView textViewAttractionCount;
    private LinearLayout linearLayoutAttractions;
    private ArrayList<Integer> tourAttractionsIds;
    private int attractionsInTourCount;

    private GoogleMap tourAttractionsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tour_attractions);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragmentAttractions);
        mapFragment.getMapAsync(ShowTourAttractionsActivity.this);
        setLinearLayoutAttractions();
        getTourAttractionsIdsAndSetAttractionInTour();
        setAttractionsInTourCount();
        setTextViewAttractionCount();
        //displayTourAttractions();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        tourAttractionsMap = googleMap;

        tourAttractionsMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        tourAttractionsMap.getUiSettings().setZoomGesturesEnabled(false);
        tourAttractionsMap.getUiSettings().setZoomControlsEnabled(false);
        tourAttractionsMap.getUiSettings().setMapToolbarEnabled(false);

        displayTourAttractions();
        //setMapMarkersForTourAttractions();
    }

    private void setMapMarkersForTourAttractions(){
        tourAttractionsMap.clear();
        ArrayList<Marker> markersList = new ArrayList<>();

        for (Integer attractionId: tourAttractionsIds) {
            Attraction attraction = getAttraction(attractionId);
            LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
            Marker marker = tourAttractionsMap.addMarker(new MarkerOptions()
                    .position(attractionLocalization)
                    .title(attraction.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            //mapTourAttractionsLocalization.moveCamera(CameraUpdateFactory.newLatLngZoom(attractionLocalization,12));
            markersList.add(marker);
        }

        tourAttractionsMap.animateCamera(getCameraUpdatesForAttractions(markersList));
    }

    private CameraUpdate getCameraUpdatesForAttractions(ArrayList<Marker> markersList){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markersList) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100;
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    private Attraction getAttraction(int attractionId){

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);
        databaseTraveler.close();
        return attraction;
    }

    private void getTourAttractionsIdsAndSetAttractionInTour(){
        Intent intent = getIntent();
        tourAttractionsIds = intent.getIntegerArrayListExtra(SHOW_ATTRACTIONS);
    }

    private void setAttractionsInTourCount(){
        attractionsInTourCount = tourAttractionsIds.size();
    }

    private void setLinearLayoutAttractions(){
        linearLayoutAttractions = (LinearLayout) findViewById(R.id.linearLayoutAttractions);
        linearLayoutAttractions.setOrientation(LinearLayout.VERTICAL);
        linearLayoutAttractions.setPadding(4,4,4,4);
        linearLayoutAttractions.setBackgroundColor(Color.BLACK);
    }

    private void setTextViewAttractionCount(){
        textViewAttractionCount = (TextView) findViewById(R.id.textViewAttractionCount);
        textViewAttractionCount.setText(getString(R.string.text_view_attraction_count_pick_attraction_for_tour_activity) + " " + attractionsInTourCount);
        textViewAttractionCount.setTextSize(20);
        textViewAttractionCount.setTextColor(Color.BLACK);
    }

    private void displayTourAttractions(){
        tourAttractionsMap.clear();
        ArrayList<Marker> markersList = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (Integer attractionId: tourAttractionsIds) {
            setOrChangeLinearLayoutAttractions(databaseTraveler,attractionId);
            Attraction attraction = getAttraction(attractionId);
            LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
            Marker marker = tourAttractionsMap.addMarker(new MarkerOptions()
                    .position(attractionLocalization)
                    .title(attraction.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            //mapTourAttractionsLocalization.moveCamera(CameraUpdateFactory.newLatLngZoom(attractionLocalization,12));
            markersList.add(marker);
        }
        databaseTraveler.close();
        tourAttractionsMap.animateCamera(getCameraUpdatesForAttractions(markersList));
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
                + getString(R.string.attraction_description_table_row_browse_attraction_activity) + " " + getTwentyFiveSignsFromDescription(attraction.getDescription()));

        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayoutFirst.addView(textView);

        linearLayoutFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        if(countryName.equals(getStringByLocal(ShowTourAttractionsActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(ShowTourAttractionsActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(ShowTourAttractionsActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(ShowTourAttractionsActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(ShowTourAttractionsActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(ShowTourAttractionsActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
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
                changeAttractionsMarkers();
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
                changeAttractionsMarkers();
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
        for (int i = 0; i < tourAttractionsIds.size();++i){
            if(tourAttractionsIds.get(i) == attractionToRemove){
                tourAttractionsIds.remove(i);
                break;
            }
        }
    }

    private void changeAttractionsMarkers(){
        tourAttractionsMap.clear();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (Integer attractionId: tourAttractionsIds) {
            Attraction attraction = getAttraction(attractionId);
            LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
            tourAttractionsMap.addMarker(new MarkerOptions()
                    .position(attractionLocalization)
                    .title(attraction.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker()));
        }
        databaseTraveler.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(TOUR_ATTRACTIONS,tourAttractionsIds);
        setResult(RESULT_OK,intent);
        finish();
    }
}
