package by.bigsoft.brazer.quakeviewer2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

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
                "N integer primary key, " +
                "Date text primary key, " +
                "Time text, " +
                "Latitude real not null, " +
                "Longitude real not null, " +
                "Ts-p real, " +
                "Delta real, " +
                "Kp integer, " +
                "M real not null, " +
                "Location text" +
               ");";
        db.execSQL(sql);
        sql = "CREATE TABLE '"+tabEarth+"' (" +
                "id integer primary key autoincrement," +
                "user text not null," +
                "pass text not null" +
                ")";
        db.execSQL(sql);
        sql = "CREATE TABLE '"+tabEurope+"' (" +
                "id integer primary key autoincrement," +
                "user text not null," +
                "pass text not null" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG_LOG, "onUpgrade");
    }

    public static SQLiteDatabase getWritableDB() {
        return mDBHelper.getWritableDatabase();
    }

    public static Cursor getBelarusEvents() {
        return mDBHelper.getReadableDatabase().query(
                tabBLR,
                new String[] {"N", "Date", "Time", "Latitude", "Longitude", "Kp", "M"},
                null, null, null, null, null
        );
    }

}
