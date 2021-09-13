package math.uni.lodz.pl.jacek.traveler;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class PreviewTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static int TICKLE_TIME = 2000;

    private GoogleMap mapTourAttractionsLocalization;
    private static final String TOUR_ATTRACTIONS = "TourAttractions";
    private Spinner spinnerAttractions;
    private ArrayList<Integer> attractionsIds;
    private ArrayList<Marker> markersList = new ArrayList<>();
    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_trip);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragmentAttractions);
        mapFragment.getMapAsync(PreviewTripActivity.this);
        attractionsIds = getTourIdFromIntent();
        setSpinnerAttractions();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapTourAttractionsLocalization = googleMap;

        mapTourAttractionsLocalization.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mapTourAttractionsLocalization.getUiSettings().setZoomControlsEnabled(false);
        mapTourAttractionsLocalization.getUiSettings().setMapToolbarEnabled(false);
        mapTourAttractionsLocalization.getUiSettings().setAllGesturesEnabled(false);

        setMapMarkersWhenGoogleMapIsReadyAndAnimateTrip(calculateHowManyTimeForCountDownTimer());

        mapTourAttractionsLocalization.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                spinnerAttractions.setSelection(getAttractionPositionOnSpinner(marker.getTitle()));
                //mapTourAttractionsLocalization.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),15));
                return false;
            }
        });
    }

    private int getAttractionPositionOnSpinner(String markerTitle){
        StringBuilder positionOnList = new StringBuilder();
        for(int index = 0; index< markerTitle.length();++index){
            if(markerTitle.charAt(index) == '.'){
                return Integer.parseInt(positionOnList.toString());
            }
            positionOnList.append(markerTitle.charAt(index));
        }
        return Integer.parseInt(markerTitle.charAt(0) + "");
    }

    private int calculateHowManyTimeForCountDownTimer(){
        StringBuffer howManyTime = new StringBuffer();
        howManyTime.append((attractionsIds.size()+1)*2);
        for(int i = 0; i<3;i++){
            howManyTime.append("0");
        }
        Log.i("HowManyTime",howManyTime.toString());
        return Integer.parseInt(howManyTime.toString());
    }

    private void setMapMarkersWhenGoogleMapIsReadyAndAnimateTrip(int howLongTime){
        mapTourAttractionsLocalization.clear();
        new CountDownTimer(howLongTime, TICKLE_TIME) {
            int attractionNumber = 1;
            LatLng previousAttractionLocalization = null;
            int tickleCount = 0;
            Marker marker;
            public void onTick(long millisUntilFinished) {
                marker = drawMarkerOnMapAndReturnItForMarkersList(tickleCount,attractionNumber);

                markersList.add(marker);
                marker.showInfoWindow();
                attractionNumber++;

                paintPathBetweenTwoPoints(previousAttractionLocalization, marker.getPosition());
                previousAttractionLocalization = marker.getPosition();

                mapTourAttractionsLocalization.animateCamera(getCameraUpdatesForAttractions(markersList));
                tickleCount++;
            }

            public void onFinish() {
                marker.hideInfoWindow();
                setMapReadyTrueAndEnableGestures();
                spinnerAttractions.setEnabled(true);
            }
        }.start();
    }

    private void setMapReadyTrueAndEnableGestures(){
        mapReady = true;
        mapTourAttractionsLocalization.getUiSettings().setZoomControlsEnabled(true);
        mapTourAttractionsLocalization.getUiSettings().setAllGesturesEnabled(true);
        mapTourAttractionsLocalization.getUiSettings().setMapToolbarEnabled(false);
    }

    private Marker drawMarkerOnMapAndReturnItForMarkersList(int positionOnList, int attractionNumber){
        Attraction attraction = getAttraction(attractionsIds.get(positionOnList));
        LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());

        Marker marker = mapTourAttractionsLocalization.addMarker(new MarkerOptions()
                .position(attractionLocalization)
                .title(attractionNumber + "." + attraction.getName())
                .icon(BitmapDescriptorFactory.defaultMarker()));

        return marker;
    }


    private void paintPathBetweenTwoPoints(LatLng previousAttractionLocalization, LatLng currentAttractionLocalization ){
        if(previousAttractionLocalization != null){
            mapTourAttractionsLocalization.addPolyline(new PolylineOptions().add(previousAttractionLocalization,currentAttractionLocalization).width(5).color(Color.RED));
        }
    }

    private void setMapMarkersWhenSpinnerAttractionEqualsAllAttractions(){
        mapTourAttractionsLocalization.animateCamera(getCameraUpdatesForAttractions(markersList));
    }

    private void setMapMarkersWhenSpinnerAttractionNotEqualsAllAttractions(String attractionName){

        for (Marker marker: markersList) {
            if(marker.getTitle().equals(attractionName)){
                mapTourAttractionsLocalization.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),14));
            }
        }

    }

    private void setSpinnerAttractions(){
        spinnerAttractions = (Spinner) findViewById(R.id.spinnerAttractions);

        spinnerAttractions.setEnabled(false);
        ArrayList<String> attractionNames = new ArrayList<>();

        int attractionNumber = 1;

        attractionNames.add(getString(R.string.attractions_spinner_All_trip_attractions_activity));

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (Integer attractionId: attractionsIds) {
            attractionNames.add(attractionNumber + "." + databaseTraveler.getOneRowAttractions(attractionId).getName());
            attractionNumber++;
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, attractionNames);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerAttractions.setAdapter(adapter);

        spinnerAttractions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mapReady){
                    if(mapReady && spinnerAttractions.getSelectedItem().toString().equals(getString(R.string.attractions_spinner_All_trip_attractions_activity))){
                        setMapMarkersWhenSpinnerAttractionEqualsAllAttractions();
                    }
                    else{
                        setMapMarkersWhenSpinnerAttractionNotEqualsAllAttractions(spinnerAttractions.getSelectedItem().toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private CameraUpdate getCameraUpdatesForAttractions(ArrayList<Marker> markersList){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markersList) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 200;
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    private ArrayList getTourIdFromIntent(){
        Intent intentExtras = getIntent();
        return intentExtras.getParcelableArrayListExtra(TOUR_ATTRACTIONS);
    }

    private Attraction getAttraction(int attractionId){

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);
        databaseTraveler.close();
        return attraction;
    }

    private String createGoodAttractionNameForDatabase(String numberPlusAttractionName){
        return numberPlusAttractionName.substring(2);
    }
}
