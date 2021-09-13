package math.uni.lodz.pl.jacek.traveler.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Attraction;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Category;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Country;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Trip;


public class DatabaseTraveler extends SQLiteOpenHelper {

    private static final int DATA_BASE_VERSION = 1;

    private static final String DATA_BASE_NAME = "DatabaseTraveler";

    private static final String TABLE_COUNTRIES = "Countries";
    private static final String COL_COUNTRIES_ID = "id";
    private static final String COL_COUNTRIES_NAME = "name";

    private static final String TABLE_STATES = "States";
    private static final String COL_STATES_ID = "id";
    private static final String COL_STATES_STATE_NAME = "name";
    private static final String COL_STATES_COUNTRY_ID = "country_id";
    private static final String COL_STATES_TO_SYNCHRONIZE = "to_synchronize";

    private static final String TABLE_PLACES = "Places";
    private static final String COL_PLACES_ID = "id";
    private static final String COL_PLACES_PLACE_NAME = "name";
    private static final String COL_PLACES_STATE_ID = "state_id";

    private static final String TABLE_CATEGORIES = "Categories";
    private static final String COL_CATEGORIES_ID = "id";
    private static final String COL_CATEGORIES_NAME = "name";

    private static final String TABLE_ATTRACTIONS = "Attractions";
    private static final String COL_ATTRACTIONS_ID = "id";
    private static final String COL_ATTRACTIONS_ATTRACTION_NAME = "name";
    private static final String COL_ATTRACTIONS_STATE_ID = "state_id";
    private static final String COL_ATTRACTIONS_PLACE_NAME = "place_name";
    private static final String COL_ATTRACTIONS_ADDRESS = "address";
    private static final String COL_ATTRACTIONS_CATEGORY_ID = "category_id";
    private static final String COL_ATTRACTIONS_DESCRIPTION = "description";
    private static final String COL_ATTRACTIONS_LONGITUDE = "longitude";
    private static final String COL_ATTRACTIONS_LATITUDE = "latitude";
    private static final String COL_ATTRACTIONS_PHOTO_PATH = "photo_path";
    private static final String COL_ATTRACTIONS_AUTHOR = "author";

    private static final String TABLE_TOURS = "Tours";
    private static final String COL_TOURS_ID = "id";
    private static final String COL_TOURS_TOUR_NAME = "name";
    private static final String COL_TOURS_DESCRIPTION = "description";
    private static final String COL_TOURS_STATE_ID = "state_id";
    private static final String COL_TOURS_AUTHOR = "author";
    private static final String COL_TOURS_ADDED_CORRECTLY = "added_correctly";
    private static final String COL_TOURS_ATTRACTIONS_COUNT = "attractions_count";


    private static final String TABLE_TOURS_ATTRACTIONS = "Tours_Attractions";
    private static final String COL_TOURS_ATTRACTIONS_ID = "id";
    private static final String COL_TOURS_ATTRACTIONS_TOUR_ID = "tour_id";
    private static final String COL_TOURS_ATTRACTIONS_ATTRACTION_ID = "attraction_id";

    private static final String TABLE_TRIPS = "Trips";
    private static final String COL_TRIPS_ID = "id";
    private static final String COL_TRIPS_USER_MOVES = "user_moves";
    private static final String COL_TRIPS_TOUR_OR_ATTRACTION_ID = "tour_or_attraction_id";
    private static final String COL_TRIPS_TOUR_OR_ATTRACTION = "tour_or_attraction";
    private static final String COL_TRIPS_DATA = "data";
    private static final String COL_TRIPS_STATE = "state";
    private static final String COL_TRIPS_OWNER = "owner";




    public DatabaseTraveler(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_Countries = "CREATE TABLE " + TABLE_COUNTRIES +"("
                + COL_COUNTRIES_ID + " INTEGER PRIMARY KEY,"
                + COL_COUNTRIES_NAME + " TEXT);";

        String CREATE_TABLE_States = "CREATE TABLE " + TABLE_STATES +"("
                + COL_STATES_ID + " INTEGER PRIMARY KEY, "
                + COL_STATES_STATE_NAME + " TEXT, "
                + COL_STATES_COUNTRY_ID + " INT, "
                + COL_STATES_TO_SYNCHRONIZE +  " INT" + ");";

        String CREATE_TABLE_Places = "CREATE TABLE " + TABLE_PLACES +"("
                + COL_PLACES_ID + " INTEGER PRIMARY KEY, "
                + COL_PLACES_PLACE_NAME + " TEXT, "
                + COL_PLACES_STATE_ID + " INT);";

        String CREATE_TABLE_Categories = "CREATE TABLE " + TABLE_CATEGORIES +"("
                + COL_CATEGORIES_ID + " INTEGER PRIMARY KEY, "
                + COL_CATEGORIES_NAME + " TEXT);";

        String CREATE_TABLE_Attractions = "CREATE TABLE " + TABLE_ATTRACTIONS +"("
                + COL_ATTRACTIONS_ID + " INTEGER PRIMARY KEY, "
                + COL_ATTRACTIONS_ATTRACTION_NAME + " TEXT, "
                + COL_ATTRACTIONS_STATE_ID + " INTEGER, "
                + COL_ATTRACTIONS_PLACE_NAME + " TEXT, "
                + COL_ATTRACTIONS_ADDRESS + " TEXT, "
                + COL_ATTRACTIONS_CATEGORY_ID + " INTEGER, "
                + COL_ATTRACTIONS_DESCRIPTION + " TEXT, "
                + COL_ATTRACTIONS_LONGITUDE + " DOUBLE, "
                + COL_ATTRACTIONS_LATITUDE + " DOUBLE, "
                + COL_ATTRACTIONS_PHOTO_PATH + " TEXT, "
                + COL_ATTRACTIONS_AUTHOR + " TEXT" +");";

        String CREATE_TABLE_Tours = "CREATE TABLE " + TABLE_TOURS +"("
                + COL_TOURS_ID + " INTEGER PRIMARY KEY, "
                + COL_TOURS_TOUR_NAME + " TEXT, "
                + COL_TOURS_DESCRIPTION + " TEXT, "
                + COL_TOURS_STATE_ID + " INTEGER, "
                + COL_TOURS_AUTHOR + " TEXT, "
                + COL_TOURS_ADDED_CORRECTLY + " INTEGER, "
                + COL_TOURS_ATTRACTIONS_COUNT + " INTEGER " + ");";

        String CREATE_TABLE_Tours_Attractions = "CREATE TABLE " + TABLE_TOURS_ATTRACTIONS +"("
                + COL_TOURS_ATTRACTIONS_ID + " INTEGER PRIMARY KEY, "
                + COL_TOURS_ATTRACTIONS_TOUR_ID + " INTEGER, "
                + COL_TOURS_ATTRACTIONS_ATTRACTION_ID + " INTEGER);";

        String CREATE_TABLE_Trip = "CREATE TABLE " + TABLE_TRIPS +"("
                + COL_TRIPS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TRIPS_USER_MOVES + " TEXT, "
                + COL_TRIPS_TOUR_OR_ATTRACTION_ID + " TEXT, "
                + COL_TRIPS_TOUR_OR_ATTRACTION + " TEXT, "
                + COL_TRIPS_DATA + " TEXT, "
                + COL_TRIPS_STATE + " TEXT, "
                + COL_TRIPS_OWNER + " TEXT);";
        db.execSQL(CREATE_TABLE_Countries);
        db.execSQL(CREATE_TABLE_States);
        db.execSQL(CREATE_TABLE_Places);
        db.execSQL(CREATE_TABLE_Categories);
        db.execSQL(CREATE_TABLE_Attractions);
        db.execSQL(CREATE_TABLE_Tours);
        db.execSQL(CREATE_TABLE_Tours_Attractions);
        db.execSQL(CREATE_TABLE_Trip);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addCountry(int id, String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COUNTRIES_ID, id);
        values.put(COL_COUNTRIES_NAME,name);
        db.insertOrThrow(TABLE_COUNTRIES,null,values);
    }

    public int getCountryIdByCountryName(String countryName){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT id FROM " + TABLE_COUNTRIES + " WHERE "
                + COL_COUNTRIES_NAME + " = " + "'" + countryName + "'";

        Cursor cursor =  db.rawQuery(selectState,null);

        cursor.moveToFirst();

        return cursor.getInt(0);
    }


    public Cursor getAllCountriesSmallLetters(){
        SQLiteDatabase db = getReadableDatabase();

        String selectCountries = "SELECT id, LOWER(name) FROM " + TABLE_COUNTRIES;

        return  db.rawQuery(selectCountries,null);
    }

    public Cursor getRowCountry(){
        String[] columns = {COL_COUNTRIES_ID,COL_COUNTRIES_NAME};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_COUNTRIES,columns,null,null,null,null,null);
    }
    public void deleteCountry(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {id + ""};
        db.delete(TABLE_COUNTRIES,"id=?",args);
    }

    public Country getCountryById(int countryId){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT * FROM " + TABLE_COUNTRIES + " WHERE "
                + COL_COUNTRIES_ID + " = " + "'" + countryId + "'";

        Cursor cursor =  db.rawQuery(selectState,null);

        cursor.moveToFirst();

        return new Country(cursor.getInt(0), cursor.getString(1));
    }

    public void addState(int id, String stateName, int countryId, int toSynchronized){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATES_ID, id);
        values.put(COL_STATES_STATE_NAME,stateName);
        values.put(COL_STATES_COUNTRY_ID,countryId);
        values.put(COL_STATES_TO_SYNCHRONIZE, toSynchronized);
        db.insertOrThrow(TABLE_STATES,null,values);
    }

    public Cursor getRowState(){
        String[] columns = {COL_STATES_ID,COL_STATES_STATE_NAME,COL_STATES_COUNTRY_ID, COL_STATES_TO_SYNCHRONIZE};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_STATES,columns,null,null,null,null,null);
    }

    public int getStateIdWhereStateName(String stateName){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT id FROM " + TABLE_STATES + " WHERE "
                + COL_STATES_STATE_NAME + " = " + "'" + stateName + "'";

        Cursor cursor =  db.rawQuery(selectState,null);

        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public State getStateWhereStateId(int stateId){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT * FROM " + TABLE_STATES + " WHERE "
                + COL_STATES_ID + " = " + "'" + stateId + "'";

        Cursor cursor =  db.rawQuery(selectState,null);

        cursor.moveToFirst();

        return new State(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3),0);
    }

    public State getStateWhereStateName(String stateName){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT * FROM " + TABLE_STATES + " WHERE "
                + COL_STATES_STATE_NAME + " = " + "'" + stateName + "'";

        Cursor cursor =  db.rawQuery(selectState,null);

        cursor.moveToFirst();

        return new State(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), 0);
    }

    public Cursor getStatesAscWhereCountryIdEquals(int countryId){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT * FROM " + TABLE_STATES
                + " WHERE country_id = " + countryId
                + " ORDER BY " + COL_STATES_STATE_NAME + " ASC ";

        return db.rawQuery(selectState,null);
    }

    public void deleteState(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_STATES,"id=?",args);
    }

    public void updateState(int id, int toSynchronized){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATES_TO_SYNCHRONIZE,toSynchronized);
        String[] args = {""+id};
        db.update(TABLE_STATES,values,"id=?",args);
    }

    public void addPlace(int placeId, String placeName, int stateId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PLACES_ID, placeId);
        values.put(COL_PLACES_PLACE_NAME,placeName);
        values.put(COL_PLACES_STATE_ID,stateId);
        db.insertOrThrow(TABLE_PLACES,null,values);
    }

    public Cursor getRowPlaces(){
        String[] columns = {COL_PLACES_ID,COL_PLACES_PLACE_NAME,COL_PLACES_STATE_ID};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_PLACES,columns,null,null,null,null,null);
    }

    public void deletePlace(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_PLACES,"id=?",args);
    }

    public void addCategories(String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORIES_NAME,name);
        db.insertOrThrow(TABLE_CATEGORIES,null,values);
    }

    public Cursor getRowCategories(){
        String[] columns = {COL_CATEGORIES_ID, COL_CATEGORIES_NAME};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_CATEGORIES,columns,null,null,null,null,null);
    }

    public void deleteCategory(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_CATEGORIES,"id=?",args);
    }

    public String getCategoryNameById(int categoryId){
        SQLiteDatabase db = getReadableDatabase();

        String selectCategory = "SELECT name FROM " + TABLE_CATEGORIES + " WHERE "
                + COL_CATEGORIES_ID + " = " + "'" + categoryId + "'";

        Cursor cursor =  db.rawQuery(selectCategory,null);

        cursor.moveToFirst();

        return cursor.getString(0);
    }

    public int getCategoryByName(String categoryName){
        SQLiteDatabase db = getReadableDatabase();

        String selectCategory = "SELECT id FROM " + TABLE_CATEGORIES + " WHERE "
                + COL_CATEGORIES_NAME + " = " + "'" + categoryName + "'";

        Cursor cursor =  db.rawQuery(selectCategory,null);

        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public void addAttraction(int id, String attractionName, int stateId, String placeName,
                              String address, int categoryId, String description,
                              double longitude, double latitude, String photoPath, String author){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ATTRACTIONS_ID,id);
        values.put(COL_ATTRACTIONS_ATTRACTION_NAME,attractionName);
        values.put(COL_ATTRACTIONS_STATE_ID, stateId);
        values.put(COL_ATTRACTIONS_PLACE_NAME,placeName);
        values.put(COL_ATTRACTIONS_ADDRESS,address);
        values.put(COL_ATTRACTIONS_CATEGORY_ID, categoryId);
        values.put(COL_ATTRACTIONS_DESCRIPTION,description);
        values.put(COL_ATTRACTIONS_LONGITUDE,longitude);
        values.put(COL_ATTRACTIONS_LATITUDE,latitude);
        values.put(COL_ATTRACTIONS_PHOTO_PATH,photoPath);
        values.put(COL_ATTRACTIONS_AUTHOR, author);
        db.insertOrThrow(TABLE_ATTRACTIONS,null,values);
    }

    public Cursor getRowAttractions(){
        String[] columns = {COL_ATTRACTIONS_ID,COL_ATTRACTIONS_ATTRACTION_NAME,COL_ATTRACTIONS_STATE_ID,
                COL_ATTRACTIONS_PLACE_NAME,COL_ATTRACTIONS_ADDRESS,COL_ATTRACTIONS_CATEGORY_ID,COL_ATTRACTIONS_DESCRIPTION,
                COL_ATTRACTIONS_LATITUDE,COL_ATTRACTIONS_LONGITUDE,COL_ATTRACTIONS_PHOTO_PATH, COL_ATTRACTIONS_AUTHOR};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ATTRACTIONS,columns,null,null,null,null,null);
    }

    public Cursor getRowAttractionWhereStateAndCountry(int stateId){
        SQLiteDatabase db = getReadableDatabase();

        String selectAttraction = "SELECT * FROM " + TABLE_ATTRACTIONS + " WHERE "
                + COL_ATTRACTIONS_STATE_ID + " = " + stateId;

        return db.rawQuery(selectAttraction,null);
    }

    public Cursor getRowAttractionWhereCountry(String statesIdsLike){
        SQLiteDatabase db = getReadableDatabase();

        String selectAttraction = "SELECT * FROM " + TABLE_ATTRACTIONS + " WHERE "
                + COL_ATTRACTIONS_STATE_ID + " IN (" + statesIdsLike + ")";

        return db.rawQuery(selectAttraction,null);
    }

    public Attraction getOneRowAttractions(int id){
        SQLiteDatabase db = getReadableDatabase();

        String selectAttraction = "SELECT * FROM " + TABLE_ATTRACTIONS + " WHERE "
                + COL_ATTRACTIONS_ID + " = " + id;

        Cursor cursor = db.rawQuery(selectAttraction,null);

        cursor.moveToFirst();

        Attraction attraction = new Attraction(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getString(3),
                cursor.getString(4),cursor.getInt(5),cursor.getString(6),
                cursor.getDouble(7),cursor.getDouble(8),cursor.getString(9),
                cursor.getString(10),0);

        cursor.close();

        return attraction;
    }

    public void deleteAttraction(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_ATTRACTIONS,"id=?",args);
    }

    public void addTour(int id, String tourName, String description, int stateId, String author, int addedCorrectly, int attractionsCount){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TOURS_ID,id);
        values.put(COL_TOURS_TOUR_NAME,tourName);
        values.put(COL_TOURS_DESCRIPTION,description);
        values.put(COL_TOURS_STATE_ID,stateId);
        values.put(COL_TOURS_AUTHOR, author);
        values.put(COL_TOURS_ADDED_CORRECTLY, addedCorrectly);
        values.put(COL_TOURS_ATTRACTIONS_COUNT, attractionsCount);
        db.insertOrThrow(TABLE_TOURS,null,values);
    }

    public Cursor getRowTours(){
        String[] columns = {COL_TOURS_ID,COL_TOURS_TOUR_NAME,COL_TOURS_DESCRIPTION,COL_TOURS_STATE_ID,COL_TOURS_AUTHOR,COL_TOURS_ADDED_CORRECTLY, COL_TOURS_ATTRACTIONS_COUNT};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_TOURS,columns,null,null,null,null,null);
    }

    public Cursor getAllRowsTours(){
        SQLiteDatabase db = getReadableDatabase();

        String selectTour = "SELECT * FROM " + TABLE_TOURS + " WHERE "
                + COL_TOURS_ADDED_CORRECTLY + " = " + 1;

        return db.rawQuery(selectTour,null);
    }

    public void updateTour(int id, int addedCorrectly){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TOURS_ADDED_CORRECTLY,addedCorrectly);
        String[] args = {""+id};
        db.update(TABLE_TOURS,values,"id=?",args);
    }

    public void deleteTour(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_TOURS,"id=?",args);
    }

    public void addTourAttraction(int id, int tourId, int attractionId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TOURS_ATTRACTIONS_ID,id);
        values.put(COL_TOURS_ATTRACTIONS_TOUR_ID,tourId);
        values.put(COL_TOURS_ATTRACTIONS_ATTRACTION_ID,attractionId);
        db.insertOrThrow(TABLE_TOURS_ATTRACTIONS,null,values);
    }

    public Cursor getRowToursAttractions(){
        String[] columns = {COL_TOURS_ATTRACTIONS_ID,COL_TOURS_ATTRACTIONS_TOUR_ID,COL_TOURS_ATTRACTIONS_ATTRACTION_ID};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_TOURS_ATTRACTIONS,columns,null,null,null,null,null);
    }

    public void deleteTourAttraction(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_TOURS_ATTRACTIONS,"id=?",args);
    }

    public Cursor getRowToursWhereStateAndCountry(int stateId){
        SQLiteDatabase db = getReadableDatabase();

        String selectTour = "SELECT * FROM " + TABLE_TOURS + " WHERE "
                + COL_TOURS_STATE_ID + " = " + stateId + " AND " + COL_TOURS_ADDED_CORRECTLY + " = " + 1;

        return db.rawQuery(selectTour,null);
    }

    public Cursor getRowTourWhereCountry(String statesIdsIn){
        SQLiteDatabase db = getReadableDatabase();

        String selectTour = "SELECT * FROM " + TABLE_TOURS + " WHERE "
                + COL_TOURS_STATE_ID + " IN " + "(" + statesIdsIn + ")" + " AND " + COL_TOURS_ADDED_CORRECTLY + " = " + 1;

        return db.rawQuery(selectTour,null);
    }

    public Tour getOneRowTours(int id){
        SQLiteDatabase db = getReadableDatabase();

        String selectTour = "SELECT * FROM " + TABLE_TOURS + " WHERE "
                + COL_TOURS_ID + " = " + id;

        Cursor cursor = db.rawQuery(selectTour,null);

        cursor.moveToFirst();

        Tour tour = new Tour(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getInt(3),cursor.getString(4),cursor.getInt(5),cursor.getInt(6), 0);
        cursor.close();

        return tour;
    }

    public Cursor getRowTourAttractionsWhereTourId(int tour_id){
        SQLiteDatabase db = getReadableDatabase();

        String selectTourAttractions = "SELECT * FROM " + TABLE_TOURS_ATTRACTIONS + " WHERE "
                + COL_TOURS_ATTRACTIONS_TOUR_ID + " = " + tour_id;

        return db.rawQuery(selectTourAttractions,null);
    }

    public void addTrip(String userMoves, String tourOrAttractionId, String tourOrAttraction, String data, String stateName, String owner){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRIPS_USER_MOVES,userMoves);
        values.put(COL_TRIPS_TOUR_OR_ATTRACTION_ID,tourOrAttractionId);
        values.put(COL_TRIPS_TOUR_OR_ATTRACTION,tourOrAttraction);
        values.put(COL_TRIPS_DATA,data);
        values.put(COL_TRIPS_STATE,stateName);
        values.put(COL_TRIPS_OWNER,owner);
        db.insertOrThrow(TABLE_TRIPS,null,values);
    }

    public Cursor getRowsTrips(){
        String[] columns = {COL_TRIPS_ID, COL_TRIPS_USER_MOVES, COL_TRIPS_TOUR_OR_ATTRACTION_ID, COL_TRIPS_TOUR_OR_ATTRACTION, COL_TRIPS_DATA, COL_TRIPS_STATE, COL_TRIPS_OWNER};
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_TRIPS,columns,null,null,null,null,null);
    }

    public Trip getTrip(int tripId){
        SQLiteDatabase db = getReadableDatabase();

        String selectTrip = "SELECT * FROM " + TABLE_TRIPS + " WHERE "
                + COL_TRIPS_ID + " = " + tripId;

        Cursor cursor = db.rawQuery(selectTrip,null);

        cursor.moveToFirst();

        Trip trip = new Trip(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));

        cursor.close();


        return trip;
    }

    public void deleteTrip(int id){
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {""+id};
        db.delete(TABLE_TRIPS,"id=?",args);
    }

    public Cursor getStatesId(){
        SQLiteDatabase db = getReadableDatabase();

        String selectState = "SELECT * FROM " + TABLE_STATES;

        return db.rawQuery(selectState,null);
    }
}
