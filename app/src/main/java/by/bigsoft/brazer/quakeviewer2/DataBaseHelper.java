package by.bigsoft.brazer.quakeviewer2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper{

    private static DataBaseHelper mDBHelper;
    private static String mName;
    private static int mVesrion;

    public static void init(Context context, String name, int version) {
        mName = name;
        mVesrion = version;
        mDBHelper = new DataBaseHelper(context, name, null, version);
    }

    private DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
