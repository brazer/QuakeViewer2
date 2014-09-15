package by.bigsoft.brazer.quakeviewer2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import by.org.cgm.jdbf.JdbfTask;

public class DataBaseHelper extends SQLiteOpenHelper{

    private final String TAG_LOG = "DataBaseHelper";
    private static DataBaseHelper mDBHelper;
    private static String mName = "QuakeDB.db";
    private static String tabBLR = "Belarus", tabEarth = "Earth", tabEurope = "Europe";
    private static int mVersion = 1;

    public static void newInstance(Context context) {
        mDBHelper = new DataBaseHelper(context, mName, null, mVersion);
    }

    private DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(TAG_LOG, "Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG_LOG, "onCreate");
        String sql = "CREATE TABLE '"+tabBLR+"' (" +
                "N integer, " +
                "Date text primary key, " +
                "Time text, " +
                "Latitude real not null, " +
                "Longitude real not null, " +
                "Ts_p real, " +
                "Delta real, " +
                "Kp integer, " +
                "M real not null, " +
                "Location text" +
               ");";
        db.execSQL(sql);
        sql = "CREATE TABLE '"+tabEarth+"' (" +
                "N integer, " +
                "DateTime text primary key, " +
                "Latitude real not null, " +
                "Longitude real not null, " +
                "Depth integer, " +
                "MPSP real, " +
                "MPLP real, " +
                "MS real, " +
                "Location text, " +
                "LocationRus text" +
                ")";
        db.execSQL(sql);
        sql = "CREATE TABLE '"+tabEurope+"' (" +
                "N integer, " +
                "DateTime text primary key, " +
                "Latitude real not null, " +
                "Longitude real not null, " +
                "Depth integer, " +
                "MPSP real, " +
                "MPLP real, " +
                "MS real, " +
                "Location text, " +
                "LocationRus text" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG_LOG, "onUpgrade");
    }

    public static Cursor getQuakes() {
        return mDBHelper.getReadableDatabase().query(
                tabEarth,
                new String[] {"N", "DateTime", "Latitude", "Longitude", "Depth", "MPSP", "LocationRus", "Location"},
                null, null, null, null, null
        );
    }

    public static Cursor getBelarusEvents() {
        return mDBHelper.getReadableDatabase().query(
                tabBLR,
                new String[] {"N", "Date", "Time", "Latitude", "Longitude", "Kp", "M"},
                null, null, null, null, null
        );
    }

    public static void addQuake(JdbfTask.QuakeRecord quake) {
        if (quake instanceof JdbfTask.QuakeRecordBLR)
            addEvent((JdbfTask.QuakeRecordBLR) quake);
        else
            addQuake((JdbfTask.QuakeRecordEarth) quake);
    }

    private static void addEvent(JdbfTask.QuakeRecordBLR quake) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("N", quake.N);
        contentValues.put("Date", quake.Date);
        contentValues.put("Time", quake.Time);
        contentValues.put("Latitude", quake.Lat);
        contentValues.put("Longitude", quake.Lon);
        contentValues.put("Ts_p", quake.Ts_p);
        contentValues.put("Delta", quake.Delta);
        contentValues.put("Kp", quake.Kp);
        contentValues.put("M", quake.M);
        contentValues.put("Location", quake.Loc);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.insert(tabBLR, null, contentValues);
    }

    private static void addQuake(JdbfTask.QuakeRecordEarth quake) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("N", quake.N);
        contentValues.put("DateTime", quake.DateTime);
        contentValues.put("Latitude", quake.Lat);
        contentValues.put("Longitude", quake.Lon);
        contentValues.put("Depth", quake.Depth);
        contentValues.put("MPSP", quake.MPSP);
        contentValues.put("MPLP", quake.MPLP);
        contentValues.put("MS", quake.MS);
        contentValues.put("Location", quake.Loc);
        contentValues.put("LocationRus", quake.LocRus);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.insert(tabEarth, null, contentValues);
    }

}
