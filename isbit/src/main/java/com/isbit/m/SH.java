package com.isbit.m;
import  android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import  android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Sebastian on 14/01/2017.
 */

public class SH     extends  SQLiteOpenHelper {
  public static final String TAG = "SH";
    public static final String DATABASE_NAME = "isbit.db";
    public static  final int DATABASE_VERSION = 5;

    public static final String TABLE_KEY_VALUE_PAIRS = "datos";

    public static final String DATABASE_CREATE_TABLE_KEY_VALUE_PAIRS = "create table "+TABLE_KEY_VALUE_PAIRS+"(key text, value text);";

    public SH(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(DATABASE_CREATE_TABLE_KEY_VALUE_PAIRS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(SH.class.getName(),"Upgrading database from version "+oldVersion+" to " +newVersion+" which will destroy all data");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_KEY_VALUE_PAIRS);
        onCreate(db);

    }

}
