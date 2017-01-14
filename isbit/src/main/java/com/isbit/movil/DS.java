package com.isbit.movil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by AriRuth on 20/02/2016.
 */
public class DS {
    private final Context context;




    public static final String access_key = "access_key";
    public static final String secret_key = "secret_key";
    public static final String url_host   = "url_host";
    public static final String url_schema = "url_schema";
    private SQLiteDatabase database;
    private  SH dbHelper;

    public DS(Context context){
        this.context = context;
         dbHelper = new SH(context);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }


    public  void  erase(){
        database.execSQL("delete from "+SH.TABLE_KEY_VALUE_PAIRS+";");
    }
    public  boolean save_key_value_pair( String key, String value){
        ContentValues cvs = new ContentValues();
        cvs.put("key",key);
        cvs.put("value",value);
        int update_result_rows_afected = database.update(SH.TABLE_KEY_VALUE_PAIRS, cvs, "key=?", new String[]{key});
        if(update_result_rows_afected>0){
            return true;
        }else {

            ContentValues registro = new ContentValues();
            registro.put("key", key);
            registro.put("value", value);
            long res = database.insert("datos", null, registro);

        }
        return true;
    }

    public  String query_secret_key(){
        return query_database_key(secret_key);

    }

    public  String query_access_key(){
        return query_database_key(access_key);
    }

    public  String query_url_host(){
            return query_database_key(  url_host);
    }
    public   String query_url_schema(){
        return query_database_key(url_schema);
    }

    public   String get_isbit_url(){
        String isbit_url = query_url_schema()+"://"+query_url_host();
        return isbit_url;
    }
    public  String query_database_key(String key){
        String value = null;
        Cursor fila= database.query(true,SH.TABLE_KEY_VALUE_PAIRS,new String[]{"value"},"key=?",new String[]{key},null,null,null,null);
        if(fila.moveToFirst()){
            value = (fila.getString(0));
        }
        fila.close();
       // bd.close();

        return value;
    }


}
