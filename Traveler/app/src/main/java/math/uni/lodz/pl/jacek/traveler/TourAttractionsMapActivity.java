package math.uni.lodz.pl.jacek.traveler;

import android.content.Intent;
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

import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class TourAttractionsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapTourAttractionsLocalization;
    private static final String TOUR_ATTRACTIONS = "TourAttractions";
    private Spinner spinnerAttractions;
    private ArrayList<Integer> attractionsIds;
    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_attractions_map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragmentAttractions);
        mapFragment.getMapAsync(TourAttractionsMapActivity.this);
        attractionsIds = getTourIdFromIntent();
        setSpinnerAttractions();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapTourAttractionsLocalization = googleMap;

        mapTourAttractionsLocalization.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mapTourAttractionsLocalization.getUiSettings().setZoomGesturesEnabled(false);
        mapTourAttractionsLocalization.getUiSettings().setZoomControlsEnabled(true);
        mapTourAttractionsLocalization.getUiSettings().setMapToolbarEnabled(false);

        setMapMarkersWhenSpinnerAttractionEqualsAll();
    }

    private void setMapMarkersWhenSpinnerAttractionEqualsAll(){
        mapTourAttractionsLocalization.clear();
        ArrayList<Marker> markersList = new ArrayList<>();

        for (Integer attractionId: attractionsIds) {
            Attraction attraction = getAttraction(attractionId);
            LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
            Marker marker = mapTourAttractionsLocalization.addMarker(new MarkerOptions()
                    .position(attractionLocalization)
                    .title(attraction.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            //mapTourAttractionsLocalization.moveCamera(CameraUpdateFactory.newLatLngZoom(attractionLocalization,12));
            markersList.add(marker);
        }

        mapTourAttractionsLocalization.animateCamera(getCameraUpdatesForAttractions(markersList));
    }

    private void setMapMarkersWhenSpinnerAttractionNotEqualsAll(String attractionName){
        mapTourAttractionsLocalization.clear();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);

        Attraction attraction = databaseTraveler.getOneRowAttractions(1);


        for (Integer attractionId: attractionsIds) {
            attraction = databaseTraveler.getOneRowAttractions(attractionId);
            if(attraction.getName().equals(attractionName)){
                break;
            }
        }

        LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
        mapTourAttractionsLocalization.addMarker(new MarkerOptions()
                .position(attractionLocalization)
                .title(attraction.getName())
                .icon(BitmapDescriptorFactory.defaultMarker()));
        mapTourAttractionsLocalization.animateCamera(CameraUpdateFactory.newLatLngZoom(attractionLocalization,14));

    }

    private void setSpinnerAttractions(){
        spinnerAttractions = (Spinner) findViewById(R.id.spinnerAttractions);

        ArrayList<String> attractionNames = new ArrayList<>();

        attractionNames.add(getString(R.string.all_attractions_tour_details_activity));

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (Integer attractionId: attractionsIds) {
            attractionNames.add(databaseTraveler.getOneRowAttractions(attractionId).getName());
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, attractionNames);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerAttractions.setAdapter(adapter);

        spinnerAttractions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mapReady){
                    if(spinnerAttractions.getSelectedItem().toString().equals(getString(R.string.all_attractions_tour_details_activity))){
                        setMapMarkersWhenSpinnerAttractionEqualsAll();
                    }
                    else if(!spinnerAttractions.getSelectedItem().toString().equals("All Attractions")){
                        setMapMarkersWhenSpinnerAttractionNotEqualsAll(spinnerAttractions.getSelectedItem().toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mapReady = true;
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
}
