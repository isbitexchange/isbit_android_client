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


    public static final int DB_VERSION = 2;
    public static final String DB_NAME= "isbit";


    public DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*Creamos la tabla con las columnas*/
        db.execSQL("create table datos(acceso text, clav_unica text);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public static boolean save_keys(Context context, String clave_privada,String clave_acceso) {
        DB admin = new DB(context, DB_NAME, null, DB_VERSION);
        SQLiteDatabase bd = admin.getWritableDatabase();

        ContentValues registro = new ContentValues();
        registro.put("acceso", clave_acceso);
        registro.put("clav_unica", clave_privada);
        bd.insert("datos", null, registro);
        bd.close();

        return true;
    }
    public static String query_secret_key(Context context){

        String clave_secreta = null;
        DB admin=new DB(context,DB_NAME,null,DB_VERSION);
        SQLiteDatabase bd =admin.getReadableDatabase();
        Cursor fila=bd.rawQuery("Select  clav_unica from datos",null);
        if(fila.moveToFirst()){
            clave_secreta = (fila.getString(0));
        }
        else

            bd.close();

        return clave_secreta;
    }

    public static String query_access_key(Context context){

        String access_key = null;
        DB admin=new DB(context,DB_NAME,null,DB_VERSION);
        SQLiteDatabase bd =admin.getReadableDatabase();
        Cursor fila=bd.rawQuery("Select  acceso from datos",null);
        if(fila.moveToFirst()){
            access_key = (fila.getString(0));
        }
        else

            bd.close();

        return access_key;
    }
}
