package math.uni.lodz.pl.jacek.traveler;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.math.BigDecimal;
import java.util.ArrayList;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Trip;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

public class ShowMyTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap myTripMap;

    private final static String TRIP = "Trip";
    private static final String ATTRACTION = "Attraction";
    private final static String TOUR = "Tour";


    private Trip trip;
    private ArrayList<Marker> attractionsMarkers = new ArrayList<>();
    private ArrayList<LatLng> userMoves = new ArrayList<>();
    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_my_trip);
        MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.myTripMap);
        mapFragment1.getMapAsync(ShowMyTripActivity.this);
        setTrip();
        setTextViewTripName();
    }

    private void setTrip(){
        Intent intent = getIntent();
        int tripId = intent.getIntExtra(TRIP,-1);
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        trip = databaseTraveler.getTrip(tripId);
        databaseTraveler.close();
    }

    private void setTextViewTripName(){
        TextView textViewTripName = (TextView) findViewById(R.id.textViewTripName);
        textViewTripName.setTextSize(20);
        textViewTripName.setTextColor(Color.BLACK);
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int attractionOrTourId = Integer.parseInt(trip.getAttractionOrTourId());
        if(trip.getAttractionOrTour().equals(ATTRACTION)){
            textViewTripName.setText(databaseTraveler.getOneRowAttractions(attractionOrTourId).getName());
        }
        else if(trip.getAttractionOrTour().equals(TOUR)){
            textViewTripName.setText(databaseTraveler.getOneRowTours(attractionOrTourId).getTourName());
        }
    }

    private void setAttractionsMarkers(){
        ArrayList<Integer> attractionsIds = new ArrayList<>();
        int attractionOrTourId = Integer.parseInt(trip.getAttractionOrTourId());

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);

        if(trip.getAttractionOrTour().equals(ATTRACTION)){
            attractionsIds.add(attractionOrTourId);
        }
        else if(trip.getAttractionOrTour().equals(TOUR)){
            Cursor cursor = databaseTraveler.getRowTourAttractionsWhereTourId(attractionOrTourId);
            while(cursor.moveToNext()){
                attractionsIds.add(cursor.getInt(0));
            }

        }
        getAttractionsAddMarkersToListAndMap(databaseTraveler, attractionsIds);
        databaseTraveler.close();
    }

    private void getAttractionsAddMarkersToListAndMap(DatabaseTraveler databaseTraveler, ArrayList<Integer> attractionsIds){
        for (Integer attractionId: attractionsIds) {
            Attraction attraction = databaseTraveler.getOneRowAttractions(attractionId);
            Marker marker = myTripMap.addMarker(new MarkerOptions()
                    .position(new LatLng(attraction.getLatitude(),attraction.getLongitude()))
                    .title(attraction.getName()));
            attractionsMarkers.add(marker);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myTripMap = googleMap;

        myTripMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        myTripMap.getUiSettings().setAllGesturesEnabled(false);
        myTripMap.getUiSettings().setZoomControlsEnabled(true); // false
        myTripMap.getUiSettings().setMapToolbarEnabled(false);

        setAttractionsMarkers();
        //setUserMoves();
        setUserMovesFake();
        paintUserMoves();
        //setMapMarkersWhenGoogleMapIsReadyAndAnimateTrip(calculateHowManyTimeForCountDownTimer());

        myTripMap.animateCamera(getCameraUpdatesOnAllAttractionsAndAllUserMoves());
    }

    private BitmapDescriptor getUserIconOnMap(){
        Bitmap bitmapUserIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.user_icon);
        Bitmap scaledBitmapUserIcon = Bitmap.createScaledBitmap(bitmapUserIcon,70,100,false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmapUserIcon);
    }

    private void setUserMoves(){
        String userMovesText = trip.getUserMoves();
        StringBuilder stringBuilder = new StringBuilder();
        double lat = 200;
        double lng;
        for(int i = 0; i < userMovesText.length(); ++i){
            if(userMovesText.charAt(i) == ':'){
                lat = Double.parseDouble(stringBuilder.toString());
                stringBuilder.delete(0,stringBuilder.length());
            }
            else if(userMovesText.charAt(i) == ',' || userMovesText.charAt(i) == '!'){
                lng = Double.parseDouble(stringBuilder.toString());
                stringBuilder.delete(0,stringBuilder.length());
                userMoves.add(new LatLng(lat,lng));
                Log.i("Point","Lat: " + lat + " Lng: " + lng);
            }
            else{
                stringBuilder.append(userMovesText.charAt(i));
            }
        }
    }

    private void setUserMovesFake(){
        BigDecimal latBig = new BigDecimal(51.80);
        BigDecimal lngBig = new BigDecimal(19.48);
        double randomNumber;
        for(int i = 0; i < 100; ++i){
            randomNumber = Math.random();
            if(randomNumber < 0.5){
                latBig = latBig.add(new BigDecimal(0.001));
            }
            else{
                lngBig = lngBig.add(new BigDecimal(0.001));
            }
            userMoves.add(new LatLng(latBig.doubleValue(),lngBig.doubleValue()));
        }
    }

    private void paintUserMoves(){
        LatLng userLastPosition = userMoves.get(0);
        for (LatLng latLng: userMoves) {
            if(!latLng.equals(userLastPosition)){
                myTripMap.addPolyline(new PolylineOptions().add(userLastPosition,latLng).width(5).color(Color.RED));
            }
            userLastPosition = latLng;
        }
    }

    private int calculateHowManyTimeForCountDownTimer(){
        StringBuffer howManyTime = new StringBuffer();
        howManyTime.append(userMoves.size());
        for(int i = 0; i<2;i++){
            howManyTime.append("0");
        }
        Log.i("HowManyTime",howManyTime.toString());
        return Integer.parseInt(howManyTime.toString());
    }

    private void setMapMarkersWhenGoogleMapIsReadyAndAnimateTrip(final int howLongTime){
        new CountDownTimer(howLongTime, 100) {
            int tickleCount = 1;
            LatLng previousUserPosition = userMoves.get(0);
            public void onTick(long millisUntilFinished) {
                LatLng currentUserPosition = userMoves.get(tickleCount);
                myTripMap.addPolyline(new PolylineOptions().add(previousUserPosition,currentUserPosition).width(5).color(Color.RED));
                myTripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition,13));
                previousUserPosition = currentUserPosition;
                tickleCount++;
                paintUserMarker(currentUserPosition);
            }

            public void onFinish() {
                int howMore = 0;
                if(tickleCount < userMoves.size()-1){
                    for(int i = tickleCount; i < userMoves.size(); ++i){
                        LatLng currentUserPosition = userMoves.get(i);
                        myTripMap.addPolyline(new PolylineOptions().add(previousUserPosition,currentUserPosition).width(5).color(Color.RED));
                        myTripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition,13));
                        previousUserPosition = currentUserPosition;
                        paintUserMarker(currentUserPosition);
                        howMore++;
                    }
                }
                myTripMap.animateCamera(getCameraUpdatesOnAllUserMoves());
                Log.i("HowMore",howMore + "");
            }
        }.start();
    }

    private void paintUserMarker(LatLng currentUserPosition){
        if(userMarker == null){
            userMarker = myTripMap.addMarker(new MarkerOptions().position(currentUserPosition).title(getString(R.string.marker_i_am_here_text_trip_activity)).icon(getUserIconOnMap()));
        }
        userMarker.setPosition(currentUserPosition);
    }

    private CameraUpdate getCameraUpdatesOnAllAttractionsAndAllUserMoves(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng userMove: userMoves) {
            builder.include(userMove);
        }
        for (Marker attractionMarker: attractionsMarkers) {
            builder.include(attractionMarker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int padding = 100;
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }

    private CameraUpdate getCameraUpdatesOnAllUserMoves(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng userMove: userMoves) {
            builder.include(userMove);
        }
        LatLngBounds bounds = builder.build();

        int padding = 100;
        return CameraUpdateFactory.newLatLngBounds(bounds, padding);
    }
}
