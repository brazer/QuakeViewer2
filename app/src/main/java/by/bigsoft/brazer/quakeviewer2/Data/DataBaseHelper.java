package by.bigsoft.brazer.quakeviewer2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import by.bigsoft.brazer.quakeviewer2.Constants;
import by.org.cgm.jdbf.JdbfTask;

public class DataBaseHelper extends SQLiteOpenHelper{

    private final String TAG_LOG = "DataBaseHelper";
    private static DataBaseHelper mDBHelper;
    private final static String mName = "QuakeDB.db";
    private final static String tabBLR = "Belarus";
    public final static String tabEarth = "Earth";
    public final static String tabEurope = "Europe";
    private final static int mVersion = 3;

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
                "Date text, " +
                "Time text, " +
                "Latitude real not null, " +
                "Longitude real not null, " +
                "Ts_p real, " +
                "Delta real, " +
                "Kp integer, " +
                "M real not null, " +
                "Location text, " +
                "primary key (N, Date)" +
               ");";
        db.execSQL(sql);
        sql = "CREATE TABLE '"+tabEarth+"' (" +
                "N integer, " +
                "DateTime text, " +
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
        String sql = "drop table if exists " + tabBLR;
        db.execSQL(sql);
        sql = "drop table if exists " + tabEurope;
        db.execSQL(sql);
        sql = "drop table if exists " + tabEarth;
        db.execSQL(sql);
        onCreate(db);
    }

    public static Cursor getQuakes(String tabName) {
        return mDBHelper.getReadableDatabase().query(
                tabName,
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

    public static void addEvent(JdbfTask.QuakeRecordBLR quake) {
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

    public static void addQuake(JdbfTask.QuakeRecordEarth quake, String tabName) {
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
        db.insert(tabName, null, contentValues);
    }

    public static void deleteRecords(Constants.Area area) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (area==Constants.Area.EARTH) db.delete(tabEarth, null, null);
        if (area==Constants.Area.EUROPE) db.delete(tabEurope, null, null);
        if (area==Constants.Area.BELARUS) db.delete(tabBLR, null, null);
    }

}
