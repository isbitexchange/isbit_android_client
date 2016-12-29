package com.isbit.movil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by AriRuth on 20/02/2016.
 */
public class DB extends SQLiteOpenHelper {
    private final Context context;


    public static final int DB_VERSION = 4;
    public static final String DB_NAME= "isbit";

    public static final String access_key = "access_key";
    public static final String secret_key = "secret_key";
    public static final String url_host   = "url_host";
    public static final String url_schema = "url_schema";

    public DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*Creamos la tabla con las columnas*/
        db.execSQL("create table datos(key text, value text);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public static boolean save_key_value_pair(Context context, String key, String value){
        DB admin = new DB(context, DB_NAME, null, DB_VERSION);
        SQLiteDatabase bd = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("key", key);
        registro.put("value", value);
        bd.insert("datos", null, registro);
        bd.close();
        return true;
    }

    public static String query_secret_key(Context context){
        return query_database_key(context,secret_key);

    }

    public static String query_access_key(Context context){

        return query_database_key(context,access_key);
    }

    public static String query_url_host(Context context){
            return query_database_key( context, url_host);
    }
    public  static String query_url_schema(Context context){
        return query_database_key(context,url_host);
    }

    public static String query_database_key(Context context,String key){

        String value = null;
        DB admin=new DB(context,DB_NAME,null,DB_VERSION);
        SQLiteDatabase bd =admin.getReadableDatabase();
        Cursor fila=bd.rawQuery("Select value from datos where key=?",new String[]{key});
        if(fila.moveToFirst()){
            value = (fila.getString(0));
        }
        else

            bd.close();

        return value;
    }


}
