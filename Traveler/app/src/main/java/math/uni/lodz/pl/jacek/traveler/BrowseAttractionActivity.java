package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskCheckDeviceConnectedToInternetAndDownloadPhoto;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationCountries;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;

public class BrowseAttractionActivity extends AppCompatActivity implements AsyncResponse{

    private final static String TRAVELER_APP = ".travelerapp";

    public static final int PLEASE_WAIT_DIALOG = 1;

    private Spinner spinnerCountry;
    private Spinner spinnerState;
    private ArrayList<Integer> attractionsList = new ArrayList<>();
    private LinearLayout linearLayoutAttractions;
    private TextView textViewSites;
    private Button buttonBack;
    private Button buttonNext;
    private ScrollView scrollViewAttractions;

    private static final String ATTRACTION = "Attraction";
    private static final int MAX_ATTRACTIONS_ON_SITE = 10;


    private int howManySitesWithAttractions = 0;
    private int currentSiteNumber = 0;
    private int lastAttractionListIndex = 0;
    private int firstAttractionListIndex = 0;
    private int sendedAttractionId = 0;

    private int countryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_attraction);
        setScrollViewAttractions();
        setTextViewSites();
        setLinearLayoutAttractions();
        setButtonBack();
        setButtonNext();
        setTextViewAttractions();
        setSpinnerCountry();
        setSpinnerState();
    }

    private void setLinearLayoutAttractions(){
        linearLayoutAttractions = (LinearLayout) findViewById(R.id.linearLayoutAttractions);
        linearLayoutAttractions.setBackgroundColor(Color.BLACK);
        linearLayoutAttractions.setPadding(4,4,4,4);
    }

    private void setScrollViewAttractions(){
        scrollViewAttractions = (ScrollView) findViewById(R.id.scrollViewAttractions);
    }

    private void setButtonBack(){
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setText(getString(R.string.back_button_browse_attraction_activity));
        buttonBack.setTextSize(16);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonNext.setEnabled(true);
                currentSiteNumber--;
                if(howManySitesWithAttractions == currentSiteNumber || currentSiteNumber == 1 ){
                    buttonBack.setEnabled(false);
                }
                attractionsToTablesRowsWhenClickBack();
                textViewSitesSetTextAfterCurrentNumberChange();
                scrollViewAttractions.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        buttonBack.setEnabled(false);
    }

    private void attractionsToTablesRowsWhenClickBack(){
        linearLayoutAttractions.removeAllViews();
        firstAttractionListIndex = checkHowManyBackAttractionOnList();
        lastAttractionListIndex = firstAttractionListIndex + MAX_ATTRACTIONS_ON_SITE - 1;
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (int index = firstAttractionListIndex; index<=lastAttractionListIndex;++index) {
            setLinearLayoutAttractionsRows(databaseTraveler, index);
        }
        Log.i("FirstIndex",firstAttractionListIndex+"");
        Log.i("LastIndex",lastAttractionListIndex+"");
    }

    private void setLinearLayoutAttractionsRows(DatabaseTraveler databaseTraveler, int index){
        LinearLayout linearLayoutAttraction = new LinearLayout(this);


        final Attraction attraction = databaseTraveler.getOneRowAttractions(attractionsList.get(index));

        TextView textViewAttraction = setTextViewWithAttraction(attraction, databaseTraveler);

        linearLayoutAttraction.addView(textViewAttraction);

        linearLayoutAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLinearLayoutRowClick(attraction);
            }
        });

        linearLayoutAttractions.addView(linearLayoutAttraction);

        if(checkIfAttractionIsLastOnPageOrList(index)) {
            LinearLayout linearLayoutSeparator = new LinearLayout(this);
            linearLayoutAttraction.setBackgroundColor(Color.BLACK);
            linearLayoutAttraction.setPadding(0,0,0,4);
            linearLayoutAttractions.addView(linearLayoutSeparator);
        }
        Log.i("AttractionIndex",index + "");
    }

    private void onLinearLayoutRowClick(Attraction attraction){
        if(checkIfAttractionPhotoFileExist(attraction.getPhotoPath())){
            Intent attractionDetailsIntent = new Intent(BrowseAttractionActivity.this,AttractionDetailsActivity.class);
            attractionDetailsIntent.putExtra(ATTRACTION,attraction.getId());
            startActivity(attractionDetailsIntent);
        }
        else{
            sendedAttractionId = attraction.getId();
            AsyncTaskCheckDeviceConnectedToInternetAndDownloadPhoto.delegate = BrowseAttractionActivity.this;
            new AsyncTaskCheckDeviceConnectedToInternetAndDownloadPhoto(BrowseAttractionActivity.this).execute(sendedAttractionId);
        }
    }

    private boolean checkIfAttractionIsLastOnPageOrList(int index){
        if(index == attractionsList.size()-1){
            return false;
        }
        else if(index != 0){
            if((index+1)%MAX_ATTRACTIONS_ON_SITE == 0){
                return false;
            }
        }
        return true;
    }

    private TextView setTextViewWithAttraction(Attraction attraction, DatabaseTraveler databaseTraveler){
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        textView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("\n" + getString(R.string.attraction_name_table_row_browse_attraction_activity) + " " + attraction.getName() + "\n"
                + getString(R.string.attraction_country_table_row_browse_attraction_activity) + " " + getCountryName(databaseTraveler) + "\n"
                + getString(R.string.attraction_state_table_row_browse_attraction_activity) + " " + getStateName(attraction,databaseTraveler) + "\n"
                + getString(R.string.attraction_description_table_row_browse_attraction_activity) + " " + getTwentyFiveSignsFromDescription(attraction.getDescription()) + "\n"
                + getString(R.string.attraction_author_table_row_browse_attraction_activity) + " " + attraction.getAuthor() + "\n");

        return textView;
    }

    private String getCategory(Attraction attraction, DatabaseTraveler databaseTraveler){
        String categoryName = databaseTraveler.getCategoryNameById(attraction.getCategoryId());
        return translateCategory(categoryName);
    }

    private String getStateName(Attraction attraction, DatabaseTraveler databaseTraveler){
        State state = databaseTraveler.getStateWhereStateId(attraction.getStateId());
        return state.getStateName();
    }

    private String getCountryName(DatabaseTraveler databaseTraveler){
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(BrowseAttractionActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(BrowseAttractionActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String translateCategory(String category){
        if(category.equals(getStringByLocal(BrowseAttractionActivity.this, R.string.categories_museum, "en"))){
            return getStringByLocal(BrowseAttractionActivity.this, R.string.categories_museum, Locale.getDefault().getLanguage());
        }
        else if(category.equals(getStringByLocal(BrowseAttractionActivity.this, R.string.categories_church, "en"))){
            return getStringByLocal(BrowseAttractionActivity.this, R.string.categories_church, Locale.getDefault().getLanguage());
        }
        return "";
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private boolean checkIfAttractionPhotoFileExist(String photoName){
        File photo = new File(Environment.getExternalStorageDirectory() + File.separator + TRAVELER_APP + File.separator + photoName);
        if(photo.isFile() && photo.length() > 0){
            return true;
        }
        return false;
    }


    private int checkHowManyBackAttractionOnList(){
        return firstAttractionListIndex - MAX_ATTRACTIONS_ON_SITE;
    }

    private void setButtonNext(){
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setText(R.string.next_button_browse_attraction_activity);
        buttonNext.setTextSize(16);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBack.setEnabled(true);
                attractionsToTablesRowsWhenClickNext();
                currentSiteNumber++;
                disableButtonNextIfAttractionsSiteEqualsZeroOrOne();
                textViewSitesSetTextAfterCurrentNumberChange();
                scrollViewAttractions.fullScroll(ScrollView.FOCUS_UP);

            }
        });
    }

    private void attractionsToTablesRowsWhenClickNext(){
        linearLayoutAttractions.removeAllViews();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        firstAttractionListIndex = lastAttractionListIndex+1;
        lastAttractionListIndex += checkHowManyMoreAttractionOnList();
        for (int index = firstAttractionListIndex; index<=lastAttractionListIndex;++index) {
            setLinearLayoutAttractionsRows(databaseTraveler, index);
        }
    }

    private int checkHowManyMoreAttractionOnList(){
        int howManyHasBeenShown = currentSiteNumber*MAX_ATTRACTIONS_ON_SITE;
        if(attractionsList.size()  - howManyHasBeenShown < MAX_ATTRACTIONS_ON_SITE){
            return attractionsList.size() - howManyHasBeenShown;
        }
        return MAX_ATTRACTIONS_ON_SITE;
    }

    private void setTextViewAttractions(){
        TextView textViewAttractions = (TextView) findViewById(R.id.textViewTours);
        textViewAttractions.setText(getString(R.string.attractions_text_view_browse_attraction_activity));
        textViewAttractions.setTextSize(20);
        textViewAttractions.setTextColor(Color.BLACK);
    }

    private void setSpinnerCountry() {
        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        List<String> list = new ArrayList<>();
        final DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
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

    private void changeStates() {
        List<String> listOfStates = new ArrayList<>();
        listOfStates.add(getString(R.string.attractions_spinner_All_browse_attraction_activity));
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

    private void setSpinnerState() {
        spinnerState = (Spinner) findViewById(R.id.spinnerState);

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                attractionsList = setAttractionList(attractionsList,spinnerCountry.getSelectedItem().toString(),spinnerState.getSelectedItem().toString());
                Log.i("CountryId",countryId + "");
                attractionsToTablesRows(attractionsList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getCountryId(String countryName){
        if(countryName.equals(getStringByLocal(BrowseAttractionActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))){
            countryName = getStringByLocal(BrowseAttractionActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private void attractionsToTablesRows(ArrayList<Integer> attractionsList){
        linearLayoutAttractions.removeAllViews();
        if(attractionsList.size()>0 && attractionsList.size() <= MAX_ATTRACTIONS_ON_SITE){
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
            for (int index = 0; index < attractionsList.size(); index++) {
                setLinearLayoutAttractionsRows(databaseTraveler,index);
            }
        }
        else if(attractionsList.size()>0 && attractionsList.size() > MAX_ATTRACTIONS_ON_SITE){
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
            for (int attractionListIndex = 0;attractionListIndex<MAX_ATTRACTIONS_ON_SITE;++attractionListIndex) {
                setLinearLayoutAttractionsRows(databaseTraveler, attractionListIndex);
            }
            lastAttractionListIndex = MAX_ATTRACTIONS_ON_SITE-1;
            firstAttractionListIndex = lastAttractionListIndex - 9;
            Log.i("FirstIndex",firstAttractionListIndex+"");
            Log.i("LastIndex",lastAttractionListIndex+"");
        }
    }

    private ArrayList setAttractionList(ArrayList<Integer> attractionsList, String countryName, String stateName){
        attractionsList.clear();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor;
        if(stateName.equals(getString(R.string.attractions_spinner_All_browse_attraction_activity))){
            cursor = databaseTraveler.getRowAttractionWhereCountry(getAllStatesIdsFromCountry(databaseTraveler));
        }
        else {
            cursor = databaseTraveler.getRowAttractionWhereStateAndCountry(databaseTraveler.getStateIdWhereStateName(stateName));

        }
        while (cursor.moveToNext()){
            attractionsList.add(cursor.getInt(0));
        }
        databaseTraveler.close();
        howManySitesWithAttractions = calculateHowMany(attractionsList.size());
        currentSiteNumber = setFirstSiteNumber(howManySitesWithAttractions);
        textViewSitesSetTextAfterCurrentNumberChange();
        disableButtonNextIfAttractionsSiteEqualsZeroOrOne();
        return attractionsList;
    }

    private String getAllStatesIdsFromCountry(DatabaseTraveler databaseTraveler){
        StringBuilder stringBuilder = new StringBuilder();
        Cursor cursor = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while (cursor.moveToNext()){
            stringBuilder.append(cursor.getInt(0));
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    private void textViewSitesSetTextAfterCurrentNumberChange(){
        textViewSites.setText(currentSiteNumber + "/" + howManySitesWithAttractions);
    }

    private String getTwentyFiveSignsFromDescription(String description){
        if(description.length()>25){
            return description.substring(0,25) + ".....";
        }
        return description;
    }

    private int calculateHowMany(int numberOfAttractions){
        if(numberOfAttractions > 0){
            if(numberOfAttractions% MAX_ATTRACTIONS_ON_SITE ==0){
                return numberOfAttractions/ MAX_ATTRACTIONS_ON_SITE;
            }
            return (numberOfAttractions/ MAX_ATTRACTIONS_ON_SITE)+1;
        }
        return 0;
    }

    private int setFirstSiteNumber(int howManySitesWithAttractions){
        if(howManySitesWithAttractions > 0){
            return 1;
        }
        return 0;
    }

    private void setTextViewSites(){
        textViewSites = (TextView) findViewById(R.id.textViewSites);
        textViewSites.setTextColor(Color.BLACK);
    }

    private void disableButtonNextIfAttractionsSiteEqualsZeroOrOne(){
        if(howManySitesWithAttractions == currentSiteNumber){
            buttonNext.setEnabled(false);
        }
        else{
            buttonNext.setEnabled(true);
        }
    }

    @Override
    public void processFinish(Integer output) {
        switch (output) {
            case -1:
                createAlertDialogCheckIfUserWantToHaveAttractionWithoutPhoto();
                break;
            case 1:
                Intent attractionDetailsIntent = new Intent(BrowseAttractionActivity.this,AttractionDetailsActivity.class);
                attractionDetailsIntent.putExtra(ATTRACTION,sendedAttractionId);
                startActivity(attractionDetailsIntent);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Checking");
                dialog.setMessage("Please Wait...");
                dialog.setCancelable(false);
                return dialog;

            default:
                break;
        }

        return null;
    }

    private void createAlertDialogCheckIfUserWantToHaveAttractionWithoutPhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(BrowseAttractionActivity.this);
        builder.setMessage(getString(R.string.alert_dialog_message_text_browse_attraction_activity))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.alert_dialog_positive_button_browse_attraction_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent attractionDetailsIntent = new Intent(BrowseAttractionActivity.this,AttractionDetailsActivity.class);
                        attractionDetailsIntent.putExtra(ATTRACTION,sendedAttractionId);
                        startActivity(attractionDetailsIntent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.alert_dialog_negative_button_browse_attraction_activity), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
