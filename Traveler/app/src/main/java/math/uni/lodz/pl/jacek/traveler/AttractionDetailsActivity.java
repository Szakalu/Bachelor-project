package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Attr;

import java.io.File;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class AttractionDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView textViewAttractionName;
    private TextView textViewAttractionDetails;
    private TextView textViewAttractionDescription;

    private final static String WHAT_IT_IS = "WhatItIs";
    private static final String ATTRACTION = "Attraction";
    private final static String TRAVELER_APP = ".travelerapp";
    private final static String BUTTON = "Button";
    private final static String DISABLE = "Disable";

    private GoogleMap mapAttractionLocalization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attraction_details);
        MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.mapAttractionLocalization);
        mapFragment1.getMapAsync(AttractionDetailsActivity.this);
        setImageViewAttractionPhoto();
        setTextViewAttractionName();
        setTextViewAttractionDetails();
        setTextViewAttractionDescription();
        setAttractionInformation(getAttractionIdFromIntent());
        setButtonStartTrip();
    }

    private void setButtonStartTrip(){
        Button buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
        buttonStartTrip.setTextSize(16);
        buttonStartTrip.setText(getString(R.string.button_start_trip_attraction_details_activity));
        buttonStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTripActivity = new Intent(AttractionDetailsActivity.this,TripActivity.class);
                intentTripActivity.putExtra(WHAT_IT_IS,ATTRACTION);
                intentTripActivity.putExtra(ATTRACTION,getAttractionIdFromIntent());
                startActivity(intentTripActivity);
                finish();
            }
        });
        checkIfNeedToDisableButtonStartTrip(buttonStartTrip);
    }

    private void checkIfNeedToDisableButtonStartTrip(Button buttonStartTrip){
        Intent intent = getIntent();
        String infoFromIntent = intent.getStringExtra(BUTTON);

        if(infoFromIntent != null){
            if(infoFromIntent.equals(DISABLE)){
                buttonStartTrip.setEnabled(false);
            }
        }
    }

    private void setImageViewAttractionPhoto(){
        ImageView imageViewAttractionPhoto = (ImageView) findViewById(R.id.imageViewAttractionPhoto);
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(getAttractionIdFromIntent());
        databaseTraveler.close();
        //checkIfAttractionPhotoExist(attraction.getPhotoPath());
        if(checkIfAttractionPhotoExist(attraction.getPhotoPath())){
            try {
                File filePhoto = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + attraction.getPhotoPath());
                Bitmap bitmapPicture = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
                imageViewAttractionPhoto.setImageBitmap(bitmapPicture);
            }catch (Exception e){
                File filePhotoForDelete = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + attraction.getPhotoPath());
                filePhotoForDelete.delete();
            }
        }
    }

    private boolean checkIfAttractionPhotoExist(String attractionPhotoPath){
        File attractionPhoto = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + attractionPhotoPath);
        if(attractionPhoto.exists()){
            if(attractionPhoto.length() > 0){
                return true;
            }
            return false;
        }
        return false;
    }


    private void setTextViewAttractionName(){
        textViewAttractionName = (TextView) findViewById(R.id.textViewAttractionName);
        textViewAttractionName.setTextSize(26);
        textViewAttractionName.setTextColor(Color.BLACK);
    }

    private void setTextViewAttractionDetails(){
        textViewAttractionDetails = (TextView) findViewById(R.id.textViewAttractionDetails);
        textViewAttractionDetails.setTextSize(16);
        textViewAttractionDetails.setTextColor(Color.BLACK);
    }

    private void setTextViewAttractionDescription(){
        textViewAttractionDescription = (TextView) findViewById(R.id.textViewAttractionDescription);
        textViewAttractionDescription.setTextSize(16);
        textViewAttractionDescription.setTextColor(Color.BLACK);
    }

    private int getAttractionIdFromIntent(){
        Intent intentExtras = getIntent();
        return intentExtras.getIntExtra(ATTRACTION,-1);

    }

    private void setAttractionInformation(int attractionId){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);
        textViewAttractionName.setText(attraction.getName());
        textViewAttractionDetails.setText(createAttractionDetailsText(attraction,databaseTraveler));
        textViewAttractionDescription.setText(attraction.getDescription());
        databaseTraveler.close();
    }



    private String createAttractionDetailsText(Attraction attraction, DatabaseTraveler databaseTraveler){
        StringBuffer attractionDetails = new StringBuffer();
        attractionDetails.append(getString(R.string.attraction_country_attraction_details_activity) + " " + getCountryName(attraction, databaseTraveler) + "\n");
        attractionDetails.append(getString(R.string.attraction_state_attraction_details_activity) + " " + getStateName(attraction, databaseTraveler) + "\n");
        attractionDetails.append(getString(R.string.attraction_place_attraction_details_activity) + " " + attraction.getPlaceName() + "\n");
        attractionDetails.append(getString(R.string.attraction_address_attraction_details_activity) + " " + attraction.getAddress() + "\n");
        attractionDetails.append(getString(R.string.attraction_category_attraction_details_activity) + " " + getCategory(attraction, databaseTraveler) + "\n");
        attractionDetails.append(getString(R.string.attraction_author_attraction_details_activity) + " " + attraction.getAuthor() + "\n");
        return attractionDetails.toString();
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
        if(countryName.equals(getStringByLocal(AttractionDetailsActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(AttractionDetailsActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(AttractionDetailsActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(AttractionDetailsActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(AttractionDetailsActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(AttractionDetailsActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
        }
        return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAttractionLocalization = googleMap;

        mapAttractionLocalization.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mapAttractionLocalization.getUiSettings().setScrollGesturesEnabled(false);
        mapAttractionLocalization.getUiSettings().setZoomGesturesEnabled(false);
        mapAttractionLocalization.getUiSettings().setZoomControlsEnabled(true);
        mapAttractionLocalization.getUiSettings().setMapToolbarEnabled(false);

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(getAttractionIdFromIntent());
        databaseTraveler.close();

        LatLng attractionLocalization = new LatLng(attraction.getLatitude(),attraction.getLongitude());
        mapAttractionLocalization.addMarker(new MarkerOptions().position(attractionLocalization).title(attraction.getName()));
        mapAttractionLocalization.moveCamera(CameraUpdateFactory.newLatLngZoom(attractionLocalization,12));
        Log.i("LatLngStart",attractionLocalization.latitude + " " + attractionLocalization.longitude);
    }
}
