package math.uni.lodz.pl.jacek.traveler;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskAddAttraction;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskCheckPosition;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.LocalizationInfo;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class AddAttractionActivity extends AppCompatActivity implements LocationListener, AsyncResponse {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";

    public static final int PLEASE_WAIT_DIALOG_ADDING_ATTRACTION = 1;
    public static final int PLEASE_WAIT_DIALOG_CHECKING_LOCALIZATION = 2;
    private final static int MY_REQUEST_ID = 1;

    private final static String TRAVELER_TEMP = ".travelertemp";

    private final static int PHOTO_640 = 640;
    private final static int PHOTO_480 = 480;
    private static final String  SAVE_PHOTO_ABSOLUTE_PATH = "save_photo_absolut_path";

    private EditText editTextAttractionName;
    private Spinner spinnerState;
    private AutoCompleteTextView autoCompleteTextViewPlace;
    private Spinner spinnerCountry;
    private Spinner spinnerCategory;
    private EditText editTextAddress;
    private EditText editTextDescription;
    private ImageView imageViewPhoto;

    private Location location;
    private LocationManager locationManager;

    public double lastKnownLatitude = 200;
    public double lastKnownLongitude = 200;

    private String  photoAbsolutePath = "";
    private String photoName = "";
    private Uri photoUri;
    private File forNextPhotosPhoto;
    private int countryId;
    public LocalizationInfo localizationInfo = new LocalizationInfo();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attraction);
        setTextViewAttractionName();
        setEditTextAttractionName();
        setTextViewCountry();
        setButtonTakePhoto();
        setImageViewPhoto();
        setSpinnerCountry();
        setTextViewState();
        setSpinnerState();
        setTextViewPlace();
        setAutoCompleteTextViewPlace();
        setTextViewCategory();
        setSpinnerCategory();
        setDeviceLocalization();
        setTextViewAddress();
        setEditTextAddress();
        setButtonLocalization();
        setButtonMap();
        setTextViewDescription();
        setEditTextDescription();
        setButtonAddAttraction();
    }

    private void setImageViewPhoto(){
        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
    }

    private void setTextViewAttractionName() {
        TextView textViewAttractionName = (TextView) findViewById(R.id.textViewAttractionName);
        textViewAttractionName.setText(getString(R.string.attraction_name_text_view_add_attraction_activity));
        textViewAttractionName.setTextSize(18);
        textViewAttractionName.setTextColor(Color.BLACK);
    }

    private void setButtonLocalization(){
        Button buttonLocalization = (Button) findViewById(R.id.buttonLocalization);
        buttonLocalization.setText(getString(R.string.button_localization_add_attraction_activity));
        buttonLocalization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskCheckPosition.delegate = AddAttractionActivity.this;
                new AsyncTaskCheckPosition(AddAttractionActivity.this).execute();
            }
        });
    }

    private void setButtonMap(){
        Button buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setText(getString(R.string.button_map_add_attraction_activity));
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMapsActivity = new Intent(AddAttractionActivity.this,MapsActivity.class);
                startActivity(intentMapsActivity);
            }
        });
    }


    private void setEditTextAttractionName() {
        editTextAttractionName = (EditText) findViewById(R.id.editTextAttractionName);
        editTextAttractionName.setTextSize(18);
    }

    private void setTextViewCountry() {
        TextView textViewCountry = (TextView) findViewById(R.id.textViewCountry);
        textViewCountry.setText(getString(R.string.attraction_localization_country_text_view_add_attraction_activity));
        textViewCountry.setTextSize(18);
        textViewCountry.setTextColor(Color.BLACK);
    }

    private void setButtonTakePhoto(){
        Button buttonTakePhoto = (Button) findViewById(R.id.buttonTakePhoto);
        buttonTakePhoto.setText(getString(R.string.take_photo_button_add_attraction_activity));

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(AddAttractionActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(AddAttractionActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
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
                changeStates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getCountryId(String countryName) {
        if (countryName.equals(getStringByLocal(AddAttractionActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))) {
            countryName = getStringByLocal(AddAttractionActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private void changeStates() {
        List<String> listOfStates = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while (cursor.moveToNext()) {
            listOfStates.add(cursor.getString(1));
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfStates);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerState.setAdapter(adapter);
    }

    private void setTextViewState() {
        TextView textViewState = (TextView) findViewById(R.id.textViewState);
        textViewState.setText(getString(R.string.attraction_localization_state_text_view_add_attraction_activity));
        textViewState.setTextSize(18);
        textViewState.setTextColor(Color.BLACK);
    }

    private void setSpinnerState() {
        spinnerState = (Spinner) findViewById(R.id.spinnerState);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeAutoCompleteTextViewPlace(spinnerState.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setTextViewPlace() {
        TextView textViewPlace = (TextView) findViewById(R.id.textViewPlace);
        textViewPlace.setText(getString(R.string.attraction_localization_place_text_view_add_attraction_activity));
        textViewPlace.setTextSize(18);
        textViewPlace.setTextColor(Color.BLACK);
    }

    private void setAutoCompleteTextViewPlace() {
        autoCompleteTextViewPlace = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewPlace);
        autoCompleteTextViewPlace.setMinWidth(520);
        autoCompleteTextViewPlace.setMinimumHeight(60);
        autoCompleteTextViewPlace.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_ENTER) {
                    return true;

                }
                return false;
            }
        });
    }

    private void changeAutoCompleteTextViewPlace(String stateName) {
        List<String> listOfPlaces = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowPlaces();
        while (cursor.moveToNext()) {
            if (cursor.getString(2).equals(stateName)) {
                listOfPlaces.add(cursor.getString(1));
            }
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, listOfPlaces);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        autoCompleteTextViewPlace.setThreshold(0);
        autoCompleteTextViewPlace.setAdapter(adapter);
        autoCompleteTextViewPlace.setTextSize(18);
    }

    private void setTextViewCategory() {
        TextView textViewCategory = (TextView) findViewById(R.id.textViewCategory);
        textViewCategory.setText(getString(R.string.attraction_category_text_view_add_attraction_activity));
        textViewCategory.setTextSize(18);
        textViewCategory.setTextColor(Color.BLACK);
    }

    private void setSpinnerCategory() {
        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
        List<String> listCategories = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowCategories();
        while (cursor.moveToNext()) {
            listCategories.add(translateCategory(cursor.getString(1)));
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerCategory.setAdapter(adapter);
    }

    private String translateCategory(String category){
            if(category.equals(getStringByLocal(AddAttractionActivity.this, R.string.categories_museum, "en"))){
                return getStringByLocal(AddAttractionActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
            }
            else if(category.equals(getStringByLocal(AddAttractionActivity.this, R.string.categories_church, "en"))){
                return getStringByLocal(AddAttractionActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
            }
            return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private void setDeviceLocalization() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 1, this);

            try {
                lastKnownLatitude = location.getLatitude();
                lastKnownLongitude = location.getLongitude();
                Log.i("Localization", "Lat: " + lastKnownLatitude + " Lon: " + lastKnownLongitude);
            } catch (NullPointerException gpsnpe) {
                Log.i("Localization", "Null");
            }
        }
    }

    private void setTextViewAddress() {
        TextView textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewAddress.setText(getString(R.string.attraction_address_text_view_add_attraction_activity));
        textViewAddress.setTextSize(18);
        textViewAddress.setTextColor(Color.BLACK);
    }

    private void setEditTextAddress() {
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        editTextAddress.setTextSize(18);
        editTextAddress.setHint(getString(R.string.attraction_address_edit_text_hint_add_attraction_activity));
    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLatitude = location.getLatitude();
        lastKnownLongitude = location.getLongitude();
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

    private void setTextViewDescription() {
        TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        textViewDescription.setText(getString(R.string.attraction_description_text_view_add_attraction_activity));
        textViewDescription.setTextSize(18);
        textViewDescription.setTextColor(Color.BLACK);
    }

    private void setEditTextDescription() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        editTextDescription.setTextSize(18);
        editTextDescription.setWidth(width);
    }

    private void setButtonAddAttraction() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        Button buttonAddAttraction = (Button) findViewById(R.id.buttonAddAttraction);
        buttonAddAttraction.setText(getString(R.string.attraction_add_attraction_button_add_attraction_activity));
        buttonAddAttraction.setWidth(width);

        buttonAddAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIfAttractionIsCorrect()) {
                    Attraction attraction = new Attraction(-1, editTextAttractionName.getText().toString(),
                            getStateId(spinnerState.getSelectedItem().toString()), autoCompleteTextViewPlace.getText().toString(), editTextAddress.getText().toString(),
                            getCategoryId(spinnerCategory.getSelectedItem().toString()), editTextDescription.getText().toString(),
                            lastKnownLatitude, lastKnownLongitude, photoAbsolutePath, getUserName(), 0);

                    AsyncTaskAddAttraction.delegate = AddAttractionActivity.this;
                    new AsyncTaskAddAttraction(AddAttractionActivity.this, AddAttractionActivity.this).execute(attraction);
                }
            }
        });
    }

    private String getUserName(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_USERNAME,null) != null){
            return pref.getString(PREF_USERNAME,null);
        }
        return "NaN";
    }

    private int getCategoryId(String category){
        String categoryName = "";
        if(category.equals(getStringByLocal(AddAttractionActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage()))){
            categoryName = getStringByLocal(AddAttractionActivity.this, R.string.categories_museum, "en");
        }
        else if(category.equals(getStringByLocal(AddAttractionActivity.this, R.string.categories_church, Locale.getDefault().getLanguage()))){
            categoryName = getStringByLocal(AddAttractionActivity.this, R.string.categories_church, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int categoryId = databaseTraveler.getCategoryByName(categoryName);
        databaseTraveler.close();
        return categoryId;
    }

    private int getStateId(String stateName){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int stateId = databaseTraveler.getStateIdWhereStateName(stateName);
        databaseTraveler.close();
        return stateId;
    }

    private boolean checkIfAttractionIsCorrect() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(AddAttractionActivity.this, "GPS is not enable", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkAttractionName()) {
            Toast.makeText(AddAttractionActivity.this, "Wrong attraction name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkCountry()) {
            Toast.makeText(AddAttractionActivity.this, "Country is not selected", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkState()) {
            Toast.makeText(AddAttractionActivity.this, "Statement is not selected", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkPlace()) {
            Toast.makeText(AddAttractionActivity.this, "Wrong place name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkAddress()) {
            Toast.makeText(AddAttractionActivity.this, "Wrong address", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkCategory()) {
            Toast.makeText(AddAttractionActivity.this, "Category is not selected", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkDescription()) {
            Toast.makeText(AddAttractionActivity.this, "Wrong description", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkLocalization()) {
            Toast.makeText(AddAttractionActivity.this, "Wrong localization", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!checkIfPhotoExist()) {
            Toast.makeText(AddAttractionActivity.this, "Please take a photo of Attraction", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkAttractionName() {
        if (editTextAttractionName.getText().toString().length() < 3 || editTextAttractionName.getText().toString().length() > 30) {
            return false;
        }
        if(!splitAttractionNameWordsAndCheckIfAreCorrect(editTextAttractionName.getText().toString())){
            return false;
        }
        return true;
    }

    private boolean splitAttractionNameWordsAndCheckIfAreCorrect(String attractionName){
        String[] splitAttractionName = attractionName.split("\\s+");

        for (String attractionWord: splitAttractionName) {
            if(!checkIfAllCharsAreLettersOrNumber(attractionWord)){
                return false;
            }
        }
        return true;
    }

    private boolean checkIfAllCharsAreLettersOrNumber(String attractionWord){

        char[] charsInNameTab = attractionWord.toCharArray();

        for (char attractionNameChar : charsInNameTab) {
            if(!Character.isLetter(attractionNameChar)){
                if(!Character.isDigit(attractionNameChar)){
                    return false;
                }
            }
        }

        return true;
    }



    private boolean checkCountry() {
        if (spinnerCountry.getSelectedItem().toString().length() == 0) {
            return false;
        }
        return true;
    }

    private boolean checkState() {
        if (spinnerState.getSelectedItem().toString().length() == 0) {
            return false;
        }
        return true;
    }

    private boolean checkPlace() {
        if (autoCompleteTextViewPlace.getText().length() < 3 || autoCompleteTextViewPlace.getText().length() > 40) {
            return false;
        }
        return true;
    }

    private boolean checkAddress() {
        if (editTextAddress.getText().length() < 3 || editTextAddress.getText().length() > 40) {
            return false;
        }
        return true;
    }

    private boolean checkCategory() {
        if (spinnerCategory.getSelectedItem().toString().toString().length() == 0) {
            return false;
        }
        return true;
    }

    private boolean checkDescription() {
        if (editTextDescription.getText().length() < 10 || editTextDescription.getText().length() > 1000) {
            return false;
        }
        return true;
    }

    private boolean checkLocalization() {
        if (lastKnownLatitude == 200 || lastKnownLongitude == 200) {
            return false;
        }
        return true;
    }

    private boolean checkIfPhotoExist(){
        File filePhoto = new File(photoAbsolutePath);
        if(filePhoto.exists()){
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG_ADDING_ATTRACTION:
                ProgressDialog dialogAddingAttraction = new ProgressDialog(this);
                dialogAddingAttraction.setTitle(getString(R.string.dialog_title_attraction_activity));
                dialogAddingAttraction.setMessage(getString(R.string.dialog_message_attraction_activity));
                dialogAddingAttraction.setCancelable(false);
                return dialogAddingAttraction;
            case PLEASE_WAIT_DIALOG_CHECKING_LOCALIZATION:
                ProgressDialog dialogCheckingPosition = new ProgressDialog(this);
                dialogCheckingPosition.setTitle(getString(R.string.dialog_title_checking_position_attraction_activity));
                dialogCheckingPosition.setMessage(getString(R.string.dialog_message_checking_position_attraction_activity));
                dialogCheckingPosition.setCancelable(false);
                return dialogCheckingPosition;
            default:
                break;
        }

        return null;
    }

    @Override
    public void processFinish(Integer output) {
        switch (output) {
            case -1:
                Toast.makeText(this, getString(R.string.toast_something_goes_wrong_all_activity), Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, getString(R.string.toast_attraction_added_attraction_activity), Toast.LENGTH_SHORT).show();
                finish();
                break;
            case 2:
                createAlertDialogCheckIfUserWantToSetLocalization();
                break;
            case -2:
                createAlertDialogCouldentFindLocalization();
                break;
        }

    }

    private void takePhoto(){
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo = null;
        Calendar calendar = Calendar.getInstance();
        if(photoName == "") {
            Log.i("PhotoName", "Empty");
            photoName = "photo" + "_" + calendar.get(Calendar.YEAR) + "_"
                    + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                    + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_"
                    + calendar.get(Calendar.SECOND);
            String fileExtension = ".jpg";

            File catalogToSavePhoto = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_TEMP);
            try {
                photo = File.createTempFile(photoName, fileExtension, catalogToSavePhoto);
            } catch (IOException e) {
                e.printStackTrace();
            }

            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            
            photoUri = Uri.fromFile(photo);
            forNextPhotosPhoto = photo;
        }
        else{
            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }

        startActivityForResult(takePhoto,MY_REQUEST_ID);
        photoAbsolutePath = forNextPhotosPhoto.getAbsolutePath();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==MY_REQUEST_ID && resultCode==RESULT_OK){
            Log.i("Photo","OK");
            try {
                rotateAndScalePhotoAfterTakeIt();
                showPicture();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==MY_REQUEST_ID && resultCode==RESULT_CANCELED) {
            Log.i("Photo","canceled");
        }
    }

    private void rotateAndScalePhotoAfterTakeIt(){
        if(!photoAbsolutePath.equals("")) {
            Matrix deviceMatrix = new Matrix();

            ExifInterface photoInterface = null;
            try {
                photoInterface = new ExifInterface(photoAbsolutePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int photoOrientation = photoInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 5);

            Log.i("PhotoOrientation",photoOrientation + "");

            if (photoOrientation == 3) {
                deviceMatrix.postRotate(180);
            } else if (photoOrientation == 6) {
                deviceMatrix.postRotate(90);
            }
            else if(photoOrientation == 8){
                deviceMatrix.postRotate(270);
            }
            else{
                deviceMatrix.postRotate(0);
            }
            File file = new File(photoAbsolutePath);
            saveRotatedFile(file.getName(),deviceMatrix);
            scalePhoto(file.getName());

            Log.i("PhotoOrient",photoOrientation + "");
        }
    }

    private void saveRotatedFile(String fileName, Matrix deviceMatrix){
        File directoryToSaveFile = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_TEMP);
        Bitmap bitmap = BitmapFactory.decodeFile(photoAbsolutePath);

        Bitmap photoAfterRotation = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), deviceMatrix, false);


        File file = new File(directoryToSaveFile, fileName);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            photoAfterRotation.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            bitmap.recycle();
            photoAfterRotation.recycle();
        } catch (Exception e) {}
    }

    private void scalePhoto(String fileName){
        File directoryToSaveFile= new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_TEMP);
        Bitmap bitmapPhoto = BitmapFactory.decodeFile(photoAbsolutePath);
        if(checkIfPhotoWidthIsBigger(bitmapPhoto)){
            Bitmap scaledPhoto = Bitmap.createScaledBitmap(bitmapPhoto, PHOTO_640, PHOTO_480, false);
            File file = new File(directoryToSaveFile, fileName);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                scaledPhoto.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                bitmapPhoto.recycle();
                scaledPhoto.recycle();
            } catch (Exception e) {}
        }
        else{
            Bitmap scaledPhoto = Bitmap.createScaledBitmap(bitmapPhoto, PHOTO_480, PHOTO_640, false);
            File file = new File(directoryToSaveFile, fileName);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                scaledPhoto.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                bitmapPhoto.recycle();
                scaledPhoto.recycle();
            } catch (Exception e) {
                Log.e("PhotoScaling","Problem");
            }
        }

    }

    private boolean checkIfPhotoWidthIsBigger(Bitmap photo){
        if(photo.getWidth()>photo.getHeight()){
            return true;
        }
        return false;
    }

    private void showPicture(){
        if(!photoAbsolutePath.equals("")){
            Bitmap bitmapPicture = BitmapFactory.decodeFile(photoAbsolutePath);
            Bitmap processedBitmap = Bitmap.createBitmap(bitmapPicture , 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight());
            imageViewPhoto.setImageBitmap(processedBitmap);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_PHOTO_ABSOLUTE_PATH, photoAbsolutePath);
        super.onSaveInstanceState(outState);
    }
    @Override

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        photoAbsolutePath = savedInstanceState.getString(SAVE_PHOTO_ABSOLUTE_PATH);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(AddAttractionActivity.this);
        super.onDestroy();
    }

    private void createAlertDialogCouldentFindLocalization(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddAttractionActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_for_wrong_localization_text_add_attraction_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_ok_button_add_attraction_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createAlertDialogCheckIfUserWantToSetLocalization(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddAttractionActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_for_localization_text_add_attraction_activity)
                + createDialogWhatLocationDataWasFound() + getString(R.string.alert_dialog_message_for_localization_next_text_add_attraction_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_yes_button_add_attraction_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setLocalizationData();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_no_button_add_attraction_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setLocalizationData(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        if(localizationInfo.getCountryId() != -1){
            String countryName = databaseTraveler.getCountryById(localizationInfo.getCountryId()).getCountryName();
            setItemOnSpinnerCountry(translateCountryName(countryName));
        }
        if(localizationInfo.getStateId() != -1){
            String stateName = databaseTraveler.getStateWhereStateId(localizationInfo.getStateId()).getStateName();
            setItemOnSpinnerState(stateName);
        }
        if(localizationInfo.getPlaceName()!= null){
            autoCompleteTextViewPlace.setText(localizationInfo.getPlaceName());
        }
        if(localizationInfo.getAddress() != null){
            editTextAddress.setText(localizationInfo.getAddress());
        }
        databaseTraveler.close();
    }

    private void setItemOnSpinnerCountry(String countryName){
        Adapter adapter = spinnerCountry.getAdapter();
        int spinnerCountryLength = adapter.getCount();
        int i;
        for (i = 0; i < spinnerCountryLength; ++i){
            if(adapter.getItem(i).toString().equals(countryName)){
                break;
            }
        }
        spinnerCountry.setSelection(i);
    }

    private void setItemOnSpinnerState(String stateName){
        Adapter adapter = spinnerState.getAdapter();
        int spinnerStateLength = adapter.getCount();
        int i;
        for(i = 0; i < spinnerStateLength; ++i){
            if(adapter.getItem(i).toString().equals(stateName)){
                break;
            }
        }
        spinnerState.setSelection(i);
    }

    private String createDialogWhatLocationDataWasFound(){
        StringBuilder locationBuilder = new StringBuilder();
        locationBuilder.append(" ");
        locationBuilder.append(getString(R.string.dialog_message_country_attraction_activity));
        locationBuilder.append(", ");
        if(localizationInfo.getStateId() != -1){
            locationBuilder.append(getString(R.string.dialog_message_province_attraction_activity));
            locationBuilder.append(", ");
        }
        if(localizationInfo.getPlaceName() != null){
            locationBuilder.append(getString(R.string.dialog_message_place_attraction_activity));
            locationBuilder.append(", ");
        }
        if(localizationInfo.getAddress() != null){
            locationBuilder.append(getString(R.string.dialog_message_address_attraction_activity));
            locationBuilder.append(", ");
        }
        locationBuilder.replace(locationBuilder.length()-2,locationBuilder.length()-2,".");
        return locationBuilder.toString();
    }
}
