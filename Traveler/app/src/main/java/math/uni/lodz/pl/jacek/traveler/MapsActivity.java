package math.uni.lodz.pl.jacek.traveler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private Location location;
    private LocationManager locationManager;

    private double lastKnownLatitude = 200;
    private double lastKnownLongitude = 200;
    private LatLng latLngLastLocalization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getCountry(){
        Locale.setDefault(new Locale("en"));
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lastKnownLatitude, lastKnownLongitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses!=null && addresses.size()>0){
            Log.i("Country", addresses.get(0).getCountryName());
            Log.i("Country", addresses.get(0).getLocality());
            Log.i("Country", addresses.get(0).getAdminArea());

        }
        else{
            Log.i("Country", "Nope");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        latLngLastLocalization = setDeviceLocalization();

        if(latLngLastLocalization.latitude != 200 && latLngLastLocalization.longitude != 200){
            mMap.clear();
            LatLng iAmHere = latLngLastLocalization;
            mMap.addMarker(new MarkerOptions().position(iAmHere).title("I Am here").icon(getUserIconOnMap()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iAmHere,12));
            Log.i("LatLngStart",latLngLastLocalization.latitude + " " + latLngLastLocalization.longitude);
            //getCountry();
        }
    }



    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        latLngLastLocalization = new LatLng (location.getLatitude(), location.getLongitude());
        LatLng iAmHere = latLngLastLocalization;
        mMap.addMarker(new MarkerOptions().position(iAmHere).title("I Am here").icon(getUserIconOnMap()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iAmHere,12));
        Log.i("LatLng",latLngLastLocalization.latitude + " " + latLngLastLocalization.longitude);
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

    private BitmapDescriptor getUserIconOnMap(){
        Bitmap bitmapUserIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.user_icon);
        Bitmap scaledBitmapUserIcon = Bitmap.createScaledBitmap(bitmapUserIcon,70,100,false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmapUserIcon);
    }

    private LatLng setDeviceLocalization(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 1, this);

            try {
                lastKnownLatitude = location.getLatitude();
                lastKnownLongitude = location.getLongitude();
                Log.i("Localization","Lat: " + lastKnownLatitude + " Lon: " + lastKnownLongitude);
            } catch (NullPointerException gpsnpe) {
                Log.i("Localization","Null");
            }
        }
        return new LatLng(lastKnownLatitude,lastKnownLongitude);
    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(MapsActivity.this);
        super.onDestroy();
    }
}
