package ro.hacktm.cashacab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.Tag;
import android.util.Log;

public class DBHelperManager extends SQLiteOpenHelper {
	 // Logcat tag
    private static final String LOG = DBHelperManager.class.getName();
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "cashacab";
 
    // Table Names
    private static final String TABLE_JOURNEY = "journey";
    
    private static final String KEY_ID = "id";
    private static final String KEY_JOURNEY_ID="jouney_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_PRICE = "price";
    private static final String KEY_IDLE = "idle";
    private static final String KEY_START_MOMENT = "startMoment";
    private static final String KEY_STOP_MOMENT = "stopMoment";

    
 
  
 
    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_JOURNEY = "CREATE TABLE "
            + TABLE_JOURNEY + "("+ KEY_JOURNEY_ID+" INTEGER AUTOINCREMENT PRIMARY KEY, "+ KEY_USER_ID +" INTEGER NOT NULL," +
            		KEY_START_MOMENT+" REAL NOT NULL, "+KEY_STOP_MOMENT+" REAL NOT NULL," +
            	KEY_DISTANCE+" REAL, " + KEY_PRICE + " REAL NOT NULL, " + KEY_IDLE + " REAL)";
 
 
    public DBHelperManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
 
        // creating required tables
        db.execSQL(CREATE_TABLE_JOURNEY);
        
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_JOURNEY);
 
        // create new tables
        onCreate(db);
    }
 
    // ------------------------ "todos" table methods ----------------//
 
     /**
     * Creating a todo
     */
    public long createJourney(Journey journey, long[] tag_ids) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, journey.getUser_id());
        values.put(KEY_START_MOMENT, journey.getStartMoment());
        values.put(KEY_STOP_MOMENT, journey.getStopMoment());
        values.put(KEY_DISTANCE, journey.getDistance());
        values.put(KEY_PRICE, journey.getPrice());
        values.put(KEY_IDLE, journey.getIdle());
        
        long todo_id = db.insert(TABLE_JOURNEY, null, values); 
        return todo_id;
    }
 
    /**
     * get single todo
     */
    public Journey getTodo(long todo_id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        String selectQuery = "SELECT  * FROM " + TABLE_JOURNEY + " WHERE "
                + KEY_ID + " = " + todo_id;
 
        Log.e(LOG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);
 
        if (c != null)
            c.moveToFirst();
 
        Journey journey = new Journey();
        journey.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        journey.setStartMoment(c.getLong(c.getColumnIndex(KEY_START_MOMENT)));
        journey.setStopMoment(c.getLong(c.getColumnIndex(KEY_STOP_MOMENT)));
        journey.setDistance(c.getDouble(c.getColumnIndex(KEY_DISTANCE)));
        journey.setIdle(c.getLong(c.getColumnIndex(KEY_IDLE)));
        journey.setPrice(c.getDouble(c.getColumnIndex(KEY_PRICE)));
       
 
        return journey;
    }
 
    /**
     * getting all todos
     * */
    public List<Journey> getAllToDos() {
        List<Journey> journeys = new ArrayList<Journey>();
        String selectQuery = "SELECT  * FROM " + TABLE_JOURNEY;
 
        Log.e(LOG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                Journey journey = new Journey();
                journey.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                journey.setStartMoment(c.getLong(c.getColumnIndex(KEY_START_MOMENT)));
                journey.setStopMoment(c.getLong(c.getColumnIndex(KEY_STOP_MOMENT)));
                journey.setDistance(c.getDouble(c.getColumnIndex(KEY_DISTANCE)));
                journey.setIdle(c.getLong(c.getColumnIndex(KEY_IDLE)));
                journey.setPrice(c.getDouble(c.getColumnIndex(KEY_PRICE)));
               
                journeys.add(journey);
            } while (c.moveToNext());
        }
 
        return journeys;
    }
 
    /**
     * getting todo count
     */
    public int getJourneyCount() {
        String countQuery = "SELECT  * FROM " + TABLE_JOURNEY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
 
        int count = cursor.getCount();
        cursor.close();
 
        // return count
        return count;
    }
 
 
    /**
     * Deleting a todo
     */
    public void deleteJourney(int journey_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JOURNEY, KEY_ID + " = ?",
                new String[] { String.valueOf(journey_id) });
    }
 
    
    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
 
   

}
