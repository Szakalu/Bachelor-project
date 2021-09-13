package math.uni.lodz.pl.jacek.traveler;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.AttractionVisitedStatusMarker;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class TripActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";

    private final static String WHAT_IT_IS = "WhatItIs";
    private static final String ATTRACTION = "Attraction";
    private final static String TRIP = "Trip";
    private final static String TOUR = "Tour";
    private final static String BUTTON = "Button";
    private final static String DISABLE = "Disable";

    private final static String TRAVELER_APP = ".travelerapp";

    private final static int NOT_VISITED = 0;
    private final static int VISITED = 1;
    private final static int DESTINATION = 2;

    private final static float MAX_METERS_TO_DESTINATION =  30.0f;
    private final static float METERS_WHEN_TRACK_USER_MARKER =  150.0f;

    private GoogleMap tripMap;

    private Location destinationLocation;
    private Location userLocation;
    private LocationManager locationManager;
    private Marker userMarker;
    private Marker destinationMarker;

    private double lastKnownLatitude = 200;
    private double lastKnownLongitude = 200;
    private LatLng latLngLastLocalization;

    private Button buttonNextAttraction;
    private TextView textViewCurrentAttraction;
    private Switch switchTrackMe;

    private ArrayList<AttractionVisitedStatusMarker> attractionVisitedStatusMarkerList = new ArrayList<>();
    private ArrayList<LatLng> userMovesPoints = new ArrayList<>();

    private String lastClickedMarkerTitle = "";
    private int lastClickedMarkerTimeHour;
    private int lastClickedMarkerTimeMinute;
    private int lastClickedMarkerTimeSecond;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.tripMap);
        mapFragment1.getMapAsync(TripActivity.this);
        setAttractionAndVisitedStatusList();
        setButtonNextAttraction();
        setButtonEndTrip();
        setTextViewCurrentAttraction();
        setSwitchTrackMe();
    }

    private void setSwitchTrackMe(){
        switchTrackMe = (Switch) findViewById(R.id.switchTrackMe);
        switchTrackMe.setText(getString(R.string.switch_track_me_text_trip_activity));
        switchTrackMe.setTextSize(17);
        switchTrackMe.setChecked(true);
        switchTrackMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(switchTrackMe.isChecked()){
                    tripMap.getUiSettings().setAllGesturesEnabled(false);
                    tripMap.getUiSettings().setZoomControlsEnabled(false);
                    tripMap.getUiSettings().setMapToolbarEnabled(false);
                    tripMap.animateCamera(getCameraUpdatesWhenTrackingIsOn());
                }
                else{
                    tripMap.getUiSettings().setAllGesturesEnabled(true);
                    tripMap.getUiSettings().setZoomControlsEnabled(true);
                    tripMap.getUiSettings().setMapToolbarEnabled(false);
                }
            }
        });
    }


    private void setButtonNextAttraction(){
        buttonNextAttraction = (Button) findViewById(R.id.buttonNextAttraction);
        buttonNextAttraction.setTextSize(14);
        if(attractionVisitedStatusMarkerList.size() == 1){
            buttonNextAttraction.setText(getString(R.string.button_last_attraction_trip_activity));
        }
        else{
            buttonNextAttraction.setText(getString(R.string.button_next_attraction_trip_activity));
        }
        buttonNextAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkIfThatWasLastAttraction()){
                    createAlertDialogForClickButtonNextAttraction();
                }
                else{
                    createAlertDialogCheckIfUserReallyWantToFinishTrip();

                }

            }
        });
    }

    private boolean checkIfThatWasLastAttraction(){
        int howManyAttractionsLeftToWatch = 0;
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker: attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getVisited() == NOT_VISITED || attractionVisitedStatusMarker.getVisited() == DESTINATION){
                howManyAttractionsLeftToWatch++;
            }
            if(howManyAttractionsLeftToWatch > 1){
                return false;
            }
        }
        return true;
    }

    private void afterTripEnds(){
        switchTrackMe.setChecked(false);
        switchTrackMe.setEnabled(false);
    }

    private void changeDestinationMarkerColorAndSetAttractionAsVisited(){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker : attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getVisited() == DESTINATION){
                attractionVisitedStatusMarker.setVisited(VISITED);
                Marker marker = getAttractionMarker(attractionVisitedStatusMarker.getAttractionId());
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                break;
            }
        }
    }

    private void changeTextViewCurrentAttractionTextForNextDestinationInformations(){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker : attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getVisited() == NOT_VISITED){
                textViewCurrentAttraction.setText(getString(R.string.text_view_current_attraction_text_trip_activity) + ": " + createInformationsAboutAttractionForTextViewCurrentAttraction(attractionVisitedStatusMarker.getAttractionId()));
                break;
            }
        }
    }

    private void setCurrentDestination(){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker : attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getVisited() == NOT_VISITED){
                Log.i("Iamgood", attractionVisitedStatusMarker.getAttractionId() + "");
                attractionVisitedStatusMarker.setVisited(DESTINATION);
                destinationMarker = attractionVisitedStatusMarker.getMarker();
                destinationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                destinationLocation = setDestinationLocation(attractionVisitedStatusMarker.getAttractionId());
                break;
            }
        }
    }

    private Location setDestinationLocation(int destinationAttractionId){
        Location location = new Location("Destination" + destinationAttractionId);
        location.setLatitude(destinationMarker.getPosition().latitude);
        location.setLongitude(destinationMarker.getPosition().longitude);
        return location;
    }

    private boolean checkIfNeedToDisableButtonNextAttraction(){
        int howManyMoreToVisit = 0;
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker : attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getVisited() == NOT_VISITED || attractionVisitedStatusMarker.getVisited() == DESTINATION){
                howManyMoreToVisit++;
            }
            if(howManyMoreToVisit>1){
                return false;
            }
        }
        return true;
    }

    private Marker getAttractionMarker(int attractionId){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker: attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getAttractionId() == attractionId){
                return attractionVisitedStatusMarker.getMarker();
            }
        }
        return attractionVisitedStatusMarkerList.get(0).getMarker();
    }

    private void setButtonEndTrip(){
        Button buttonEndTrip = (Button) findViewById(R.id.buttonEndTrip);
        buttonEndTrip.setTextSize(14);
        buttonEndTrip.setText(getString(R.string.button_end_trip_trip_activity));
        buttonEndTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialogForClickButtonEndTripOrPressBackButton();
            }
        });
    }

    private void doAfterClickPositiveButtonAlertDialogForButtonNextAttraction(){
        changeDestinationMarkerColorAndSetAttractionAsVisited();
        changeTextViewCurrentAttractionTextForNextDestinationInformations();
        if(checkIfNeedToDisableButtonNextAttraction()){
            buttonNextAttraction.setText(getString(R.string.button_last_attraction_trip_activity));
        }
        setCurrentDestination();
        animateCameraAfterClickPositiveButtonAlertDialogForButtonNextAttraction();
    }

    private void animateCameraAfterClickPositiveButtonAlertDialogForButtonNextAttraction(){
        if(getSwitchTrackMeStatus()){
            if(checkIfUserIsCloseHundredFiftyFromDestination()){
                tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(),17));
            }
            else{
                tripMap.animateCamera(getCameraUpdatesWhenTrackingIsOn());
            }
        }
    }

    private void createAlertDialogForClickButtonNextAttraction(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_text_next_attraction_trip_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        doAfterClickPositiveButtonAlertDialogForButtonNextAttraction();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createAlertDialogForClickButtonEndTripOrPressBackButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_text_end_trip_trip_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createAlertDialogForClickButtonLastAttractionOrStepOnLastAttraction();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setTextViewCurrentAttraction(){
        textViewCurrentAttraction = (TextView) findViewById(R.id.textViewCurrentAttraction);
        textViewCurrentAttraction.setTextSize(20);
        textViewCurrentAttraction.setTextColor(Color.BLACK);
        textViewCurrentAttraction.setText(getString(R.string.text_view_current_attraction_text_trip_activity) + ": " + createInformationsAboutAttractionForTextViewCurrentAttraction(attractionVisitedStatusMarkerList.get(0).getAttractionId()));
    }

    private String createInformationsAboutAttractionForTextViewCurrentAttraction(int attractionId){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);
        databaseTraveler.close();
        return attraction.getName() + " " + attraction.getPlaceName() + " " + attraction.getAddress();
    }

    private String getWhatItIsFromIntent(){
        Intent intent = getIntent();
        return intent.getStringExtra(WHAT_IT_IS);
    }

    private void setAttractionAndVisitedStatusList(){
        String whatItIs = getWhatItIsFromIntent();
        Intent intent = getIntent();
        if(whatItIs.equals(ATTRACTION)){
            attractionVisitedStatusMarkerList.add(new AttractionVisitedStatusMarker(intent.getIntExtra(ATTRACTION,-1),NOT_VISITED,null));
        }
        else if(whatItIs.equals(TRIP)){
            ArrayList<Integer> attractionsList = new ArrayList<>(intent.getIntegerArrayListExtra(TRIP));
            for (Integer attractionId: attractionsList) {
                attractionVisitedStatusMarkerList.add(new AttractionVisitedStatusMarker(attractionId,NOT_VISITED,null));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        tripMap = googleMap;

        tripMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastClickedMarkerTitle = "";
            }
        });

        onMarkerClick();

        tripMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        tripMap.getUiSettings().setAllGesturesEnabled(false);
        tripMap.getUiSettings().setZoomControlsEnabled(false);
        tripMap.getUiSettings().setMapToolbarEnabled(false);

        latLngLastLocalization = setDeviceLocalization();

        if(latLngLastLocalization.latitude != 200 && latLngLastLocalization.longitude != 200){
            LatLng iAmHere = latLngLastLocalization;
            userMarker = tripMap.addMarker(new MarkerOptions().position(iAmHere).title(getString(R.string.marker_i_am_here_text_trip_activity)).icon(getUserIconOnMap()));
            addNewLocalizationToUserMovesPointsList(userMarker);
        }

        setMarkersOnMapTheirStatusAndAddThemToMarkersList(tripMap);

        setNextDestinationMarkerLocationAndChangeCurrentAttractionTextForNextDestination();

        if(getSwitchTrackMeStatus()){
            if(checkIfUserIsCloseHundredFiftyFromDestination()){
                tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(),17));
            }
            else{
                tripMap.moveCamera(getCameraUpdatesWhenTrackingIsOn());
            }
        }
    }

    private void onMarkerClick(){
        tripMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!marker.getTitle().equals(getString(R.string.marker_i_am_here_text_trip_activity))){
                    if(lastClickedMarkerTitle.equals(marker.getTitle())){
                        boolean runAttractionDetails = checkIfRunAttractionDetailsActivity();
                        lastClickedMarkerTitle = "";
                        marker.hideInfoWindow();
                        if(runAttractionDetails){
                            runAttractionDetailsActivity(marker);
                        }
                    }
                    else{
                        marker.showInfoWindow();
                        lastClickedMarkerTitle = marker.getTitle();
                        setLastClickedMarkerTime();
                    }
                }else{
                    lastClickedMarkerTitle = "";
                    marker.showInfoWindow();
                }
                return true;
            }
        });
    }

    private void runAttractionDetailsActivity(Marker marker){
        Intent attractionDetailsIntent = new Intent(TripActivity.this,AttractionDetailsActivity.class);
        attractionDetailsIntent.putExtra(ATTRACTION,getAttractionIdFromMarkersList(marker));
        attractionDetailsIntent.putExtra(BUTTON,DISABLE);
        startActivity(attractionDetailsIntent);
    }

    private void setLastClickedMarkerTime(){
        Calendar calendar = Calendar.getInstance();
        lastClickedMarkerTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        lastClickedMarkerTimeMinute = calendar.get(Calendar.MINUTE);
        lastClickedMarkerTimeSecond = calendar.get(Calendar.SECOND);
    }

    private boolean checkIfRunAttractionDetailsActivity(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if(hour == lastClickedMarkerTimeHour && minute == lastClickedMarkerTimeMinute){
            if(second == lastClickedMarkerTimeSecond || second-1 == lastClickedMarkerTimeSecond){
                return true;
            }
        }
        else if(hour == lastClickedMarkerTimeHour && minute+1 == lastClickedMarkerTimeMinute){
            if(second == 0){
                return true;
            }
        }
        return false;
    }


    private int getAttractionIdFromMarkersList(Marker clickedMarker){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker: attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getMarker().equals(clickedMarker)){
                return attractionVisitedStatusMarker.getAttractionId();
            }
        }
        return -1;
    }

    private boolean getSwitchTrackMeStatus(){
        return switchTrackMe.isChecked();
    }

    private void setMarkersOnMapTheirStatusAndAddThemToMarkersList(GoogleMap tripMap){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker : attractionVisitedStatusMarkerList) {
            Attraction attraction = databaseTraveler.getOneRowAttractions(attractionVisitedStatusMarker.getAttractionId());
            if(attractionVisitedStatusMarker.getVisited() == NOT_VISITED){
                Marker marker = tripMap.addMarker(new MarkerOptions().position(getAttractionPosition(attraction))
                        .title(attraction.getName())
                        .snippet(attraction.getPlaceName() + " " + attraction.getAddress())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        //.icon(BitmapDescriptorFactory.fromBitmap(getBitmapForMarker(attraction))));
                attractionVisitedStatusMarker.setMarker(marker);
            }
        }
        databaseTraveler.close();

        setCurrentDestination();
    }

    private LatLng getAttractionPosition(Attraction attraction){
        return new LatLng(attraction.getLatitude(),attraction.getLongitude());
    }

    private String getAttractionPhotoPath(Attraction attraction){
        return attraction.getPhotoPath();
    }

    private Bitmap getBitmapForMarker(Attraction attraction){
        File filePhoto = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + getAttractionPhotoPath(attraction));
        Bitmap bitmapPicture = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
        return Bitmap.createScaledBitmap(bitmapPicture,80,80,false);
    }

    @Override
    public void onLocationChanged(Location locationChange) {
        userLocation = locationChange;
        Marker beforeMoveDestinationMarker = destinationMarker;
        LatLng myCurrentlyLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        userMarker.setPosition(myCurrentlyLocation);

        if(latLngLastLocalization!=null){
            tripMap.addPolyline(new PolylineOptions().add(latLngLastLocalization,myCurrentlyLocation).width(5).color(Color.RED));
        }

        latLngLastLocalization = myCurrentlyLocation;

        addNewLocalizationToUserMovesPointsList(userMarker);

        setNextDestinationMarkerLocationAndChangeCurrentAttractionTextForNextDestination();

        if(getSwitchTrackMeStatus()){
            if(checkIfUserIsCloseHundredFiftyFromDestination()){
                tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngLastLocalization,17));
            }
            else{
                updateCameraAfterMoveToLocationOrSetNextDestination(beforeMoveDestinationMarker);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void updateCameraAfterMoveToLocationOrSetNextDestination(Marker beforeMoveDestinationMarker){
        if(destinationMarker.equals(beforeMoveDestinationMarker)){
            tripMap.moveCamera(getCameraUpdatesWhenTrackingIsOn());
        }
        else{
            tripMap.animateCamera(getCameraUpdatesWhenTrackingIsOn());
        }
    }

    private LatLng setDeviceLocalization(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            userLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 1, this);

            try {
                lastKnownLatitude = userLocation.getLatitude();
                lastKnownLongitude = userLocation.getLongitude();
                Log.i("Localization","Lat: " + lastKnownLatitude + " Lon: " + lastKnownLongitude);
            } catch (NullPointerException gpsnpe) {
                Log.i("Localization","Null");
            }
        }
        return new LatLng(lastKnownLatitude,lastKnownLongitude);
    }

    private BitmapDescriptor getUserIconOnMap(){
        Bitmap bitmapUserIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.user_icon);
        Bitmap scaledBitmapUserIcon = Bitmap.createScaledBitmap(bitmapUserIcon,70,100,false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmapUserIcon);
    }

    private void addNewLocalizationToUserMovesPointsList(Marker marker){
        LatLng userLocalization = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
        userMovesPoints.add(userLocalization);
    }

    private boolean checkIfUserIsCloseToDestination(){
        return userLocation.distanceTo(destinationLocation) <= MAX_METERS_TO_DESTINATION;
    }

    private void setNextDestinationMarkerLocationAndChangeCurrentAttractionTextForNextDestination(){
        if(checkIfThatWasLastAttraction()){
            if(checkIfUserIsCloseToDestination()){
                createAlertDialogForClickButtonLastAttractionOrStepOnLastAttraction();
                setLastAttractionVisited();
                afterTripEnds();
            }
        }
        else{
            if(destinationLocation != null && checkIfUserIsCloseToDestination()){
                changeDestinationMarkerColorAndSetAttractionAsVisited();
                changeTextViewCurrentAttractionTextForNextDestinationInformations();
                if(checkIfNeedToDisableButtonNextAttraction()){
                    buttonNextAttraction.setText(getString(R.string.button_last_attraction_trip_activity));
                }
                setCurrentDestination();
            }
        }
    }

    private CameraUpdate getCameraUpdatesWhenTrackingIsOn(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userMarker.getPosition());
        builder.include(destinationMarker.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 100;
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    private boolean checkIfUserIsCloseHundredFiftyFromDestination(){
        return userLocation.distanceTo(destinationLocation) <= METERS_WHEN_TRACK_USER_MARKER;
    }

    @Override
    public void onBackPressed() {
        createAlertDialogForClickButtonEndTripOrPressBackButton();
    }

    private void createAlertDialogForClickButtonLastAttractionOrStepOnLastAttraction(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_last_attraction_trip_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        saveTripOrAttractionInDatabaseInDatabase();
                        TripActivity.this.finish();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        TripActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setLastAttractionVisited(){
        for (AttractionVisitedStatusMarker attractionVisitedStatusMarker: attractionVisitedStatusMarkerList) {
            if(attractionVisitedStatusMarker.getMarker().equals(destinationMarker)){
                attractionVisitedStatusMarker.setVisited(VISITED);
                attractionVisitedStatusMarker.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
        }
    }

    private void createAlertDialogCheckIfUserReallyWantToFinishTrip(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_check_last_attraction_trip_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        setLastAttractionVisited();
                        afterTripEnds();
                        createAlertDialogForClickButtonLastAttractionOrStepOnLastAttraction();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_trip_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String createUserMovesForDatabase(){
        StringBuilder userMoves = new StringBuilder();
        for (int index = 0; index < userMovesPoints.size(); ++index){
            userMoves.append(userMovesPoints.get(index).latitude).append(":").append(userMovesPoints.get(index).longitude);
            if(index == userMovesPoints.size()-1){
                userMoves.append("!");
            }
            else{
                userMoves.append(",");
            }
        }
        return userMoves.toString();
    }

    private void saveTripOrAttractionInDatabaseInDatabase(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        if(attractionVisitedStatusMarkerList.size() > 1){
            //databaseTraveler.addTrip(createUserMovesForDatabase(), getTourIdFromIntent(), TOUR, createDataForAddingToDatabase(), getTourState(databaseTraveler), getUserName());
        }
        else{
            databaseTraveler.addTrip(createUserMovesForDatabase(), getAttractionIdFromIntent(), ATTRACTION, createDataForAddingToDatabase(), getAttractionState(databaseTraveler), getUserName());
        }
        databaseTraveler.close();
    }

    private String getTourIdFromIntent(){
        Intent intent = getIntent();
        return intent.getIntExtra(TOUR,-1) + "";
    }

    private int getTourState(DatabaseTraveler databaseTraveler){
        return databaseTraveler.getOneRowTours(Integer.parseInt(getTourIdFromIntent())).getStateId();
    }

    private String getAttractionIdFromIntent(){
        Intent intent = getIntent();
        return intent.getIntExtra(ATTRACTION,-1) + "";
    }

    private String getAttractionState(DatabaseTraveler databaseTraveler){
        return getStateName(databaseTraveler.getOneRowAttractions(Integer.parseInt(getAttractionIdFromIntent())), databaseTraveler);
    }


    private String getStateName(Attraction attraction, DatabaseTraveler databaseTraveler){
        State state = databaseTraveler.getStateWhereStateId(attraction.getStateId());
        return state.getStateName();
    }


    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private String createDataForAddingToDatabase(){
        Calendar calendar = Calendar.getInstance();
        return createGoodData(calendar.get(Calendar.DAY_OF_MONTH)) + "/" + createGoodData(calendar.get(Calendar.MONTH)+1) + "/" + createGoodData(calendar.get(Calendar.YEAR));
    }

    private String createGoodData(int dayMonthYear){
        if(dayMonthYear < 10){
            return "0" + dayMonthYear;
        }
        return dayMonthYear + "";
    }

    private String getUserName(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        return pref.getString(PREF_USERNAME,"");
    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(TripActivity.this);
        super.onDestroy();
    }
}
