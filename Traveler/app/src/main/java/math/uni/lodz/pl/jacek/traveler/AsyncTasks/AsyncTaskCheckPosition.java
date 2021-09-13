package math.uni.lodz.pl.jacek.traveler.AsyncTasks;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.AddAttractionActivity;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.LocalizationInfo;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;


public class AsyncTaskCheckPosition extends AsyncTask<Void,Void,Integer> {

    private AddAttractionActivity currentlyActivity;
    public static AsyncResponse delegate = null;

    public AsyncTaskCheckPosition(AddAttractionActivity currentlyActivity) {
        this.currentlyActivity = currentlyActivity;
        currentlyActivity.localizationInfo = new LocalizationInfo();
    }

    @Override
    protected void onPreExecute() {
        currentlyActivity.showDialog(AddAttractionActivity.PLEASE_WAIT_DIALOG_CHECKING_LOCALIZATION);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = -2;
        int counter = 0;
        while(currentlyActivity.localizationInfo.getCountryId() == -1 && counter != 10){
            getLocalization();
            counter++;
        }
        if(counter != 9){
            result = 2;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        currentlyActivity.removeDialog(AddAttractionActivity.PLEASE_WAIT_DIALOG_CHECKING_LOCALIZATION);
        delegate.processFinish(result);
    }

    private void getLocalization(){
        Locale locale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        Geocoder geocoder = new Geocoder(currentlyActivity);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(currentlyActivity.lastKnownLatitude, currentlyActivity.lastKnownLongitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses!=null && addresses.size()>0){
            if(addresses.get(0).getCountryName() != null){
                DatabaseTraveler databaseTraveler = new DatabaseTraveler(currentlyActivity);

                setCountryId(addresses.get(0).getCountryName(), databaseTraveler);
                setStateId(addresses.get(0).getAdminArea(), databaseTraveler);
                setPlaceName(addresses.get(0).getLocality());
                setAddressStreetName(addresses.get(0).getThoroughfare());
                setAddressStreetNumber(addresses.get(0).getSubThoroughfare());
                databaseTraveler.close();
            }
        }
        else{
            Log.i("Localization", "Nope");
        }
        Locale.setDefault(locale);
    }

    private void setCountryId(String countryName, DatabaseTraveler databaseTraveler){
        Cursor cursor = databaseTraveler.getAllCountriesSmallLetters();
        while (cursor.moveToNext()){
            if(countryName.toLowerCase().equals(cursor.getString(1))){
                currentlyActivity.localizationInfo.setCountryId(cursor.getInt(0));
                break;
            }
        }
        cursor.close();
    }

    private void setPlaceName(String placeName){
        if(placeName != null){
            currentlyActivity.localizationInfo.setPlaceName(placeName);
        }
    }

    private void setAddressStreetName(String streetName){
        if(streetName != null){
            currentlyActivity.localizationInfo.setAddress(streetName);
        }
    }

    private void setAddressStreetNumber(String streetNumber){
        if(streetNumber != null){
            currentlyActivity.localizationInfo.setAddress(currentlyActivity.localizationInfo.getAddress() + " " + streetNumber);
        }
    }

    private void setStateId(String localizationState, DatabaseTraveler databaseTraveler){
        if(localizationState != null && currentlyActivity.localizationInfo.getCountryId() != -1){
            Cursor cursor = databaseTraveler.getStatesAscWhereCountryIdEquals(currentlyActivity.localizationInfo.getCountryId());
            while (cursor.moveToNext()){
                if(compareStatesNames(cursor.getString(1).toLowerCase(), localizationState.toLowerCase())){
                    currentlyActivity.localizationInfo.setStateId(cursor.getInt(0));
                    break;
                }
            }
        }
    }

    private boolean compareStatesNames(String stateInDevice, String localizationState){
        int howManySignsCompare = 0;
        for (int i = 0; i < localizationState.length(); ++i){
            if(stateInDevice.charAt(howManySignsCompare) == localizationState.charAt(i)){
                ++howManySignsCompare;
            }
            if(howManySignsCompare == stateInDevice.length()){
                return true;
            }
        }
        return false;
    }
}
