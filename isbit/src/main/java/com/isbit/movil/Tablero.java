package com.isbit.movil;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegratorISBIT;
import com.google.zxing.integration.android.IntentResultISBIT;

import org.bitcoin.market.IsbitMXNApi;
import org.bitcoin.market.bean.AppAccount;
import org.bitcoin.market.bean.Asset;
import org.json.JSONException;
import org.json.JSONObject;

public class Tablero extends FragmentActivity {

    public  static  final String TAG = "Tablero";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.isbit.movil.R.layout.activity_tablero);

        final Context context = getApplicationContext();
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url_str = DS.get_isbit_url(context)+"//api/v2/order_book.json?market=btcmxn&asks_limit=10&bids_limit=10";
                HttpURLConnection urlConnection = null;
                URL url = null;

                try {
                    url = new URL(url_str);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    //                urlConnection.setDoOutput(true);
                    //                  urlConnection.setChunkedStreamingMode(0);
//
                    //   OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                    //  writeStream(out);

                    //  out.write("hola".getBytes());

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    final String respuesta= readStream(in);

                    JSONObject orderbook = new JSONObject(respuesta);

                    final JSONArray asks_json  = orderbook.getJSONArray("asks");
                    final JSONArray bids_json =  orderbook.getJSONArray("bids");

                    Tablero.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ListView listview = (ListView) findViewById(android.R.id.list);
                            final ArrayList<JSONObject> list = new ArrayList<JSONObject>();

                            for (int i = asks_json.length()-1; i >= 0;   --i) {
                                try {
                                    if(asks_json.getJSONObject(i).getDouble("remaining_volume")>0) {
                                        list.add(asks_json.getJSONObject(i));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            for (int i = 0; i < bids_json.length();   ++i) {
                                try {
                                    if(asks_json.getJSONObject(i).getDouble("remaining_volume")>0) {
                                        list.add(bids_json.getJSONObject(i));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            final StableArrayAdapter adapter = new StableArrayAdapter(Tablero.this,
                                    android.R.layout.simple_list_item_1, list);
                            listview.setAdapter(adapter);
                            //TextView ticker_tv = (TextView) findViewById(R.id.ticker_tv);
                            //ticker_tv.setText(respuesta);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            }
        }).start();
        */

        View tablero = (View) findViewById(R.id.tablero);

        initIsbit(tablero,Tablero.this);

        Button button = (Button) findViewById(com.isbit.movil.R.id.button_acceder);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    // Intent login_activity_intent = new Intent(Tablero.this, LoginActivity.class);
                    // startActivity(login_activity_intent);
                    //Intent intent = new Intent("com.google.zxing.client.android.SCAN");

                IntentIntegratorISBIT i = new IntentIntegratorISBIT(Tablero.this);

                i.setCaptureActivity(ToolbarCaptureActivity.class);
                i.initiateScan();

               // startActivity(new Intent(Tablero.this,ToolbarCaptureActivity.class));

                    //intent.putExtra("SCAN_MODE", "QR_CODE_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
                    //intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
                    //startActivityForResult(intent, 0);

            }
        });
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResultISBIT result = IntentIntegratorISBIT.parseActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult(requestCode="+requestCode+", resultCode, data="+data+")");
            if (result != null) {
                String contents = result.getContents();
                if (result.getContents()!=null){

                try {
                    JSONObject yeison = new JSONObject(contents);
                    String access_key = yeison.getString("access_key");
                    String secret_key = yeison.getString("secret_key");
                    String url_host = yeison.getString("url_host");
                    String url_schema = yeison.getString("url_schema");

                    DS ds = new DS(Tablero.this);
                    ds.open();
                    ds.save_key_value_pair( DS.access_key, access_key);
                    ds.save_key_value_pair( com.isbit.movil.DS.secret_key, secret_key);
                    ds.save_key_value_pair( com.isbit.movil.DS.url_host, url_host);
                    ds.save_key_value_pair( com.isbit.movil.DS.url_schema, url_schema);
                    ds.close();


                    Intent intent = new Intent(Tablero.this, MainActivity.class);
                    startActivity(intent);

                    //Log.d("api", secret_key + " " + access_key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

                Log.e("shit", "La respuesta fue " + contents);
            }  else {
                // do something else
            }

    }

    public void initIsbit(final View rootView, final Activity activity){
            final ProgressBar pb  = (ProgressBar) rootView.findViewById(R.id.progressBar);
        final Button access_button = (Button) findViewById(com.isbit.movil.R.id.button_acceder);


        DS ds = new DS(activity);
        ds.open();
        final String access_key = ds.query_access_key();
        final String secret_key = ds.query_secret_key();
        ds.close();

        pb.setVisibility(View.VISIBLE);
        access_button.setVisibility(View.GONE);

        if(access_key!=null && !access_key.isEmpty() && secret_key!=null && !secret_key.isEmpty()){
            access_button.setVisibility(View.GONE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final AppAccount app_account = new AppAccount();
                    app_account.setAccessKey(access_key);
                    app_account.setSecretKey(secret_key);
                    IsbitMXNApi api = new IsbitMXNApi(activity);
                    try{
                        final Asset asset = api.getInfo(app_account);
                        Log.i("MainActivity", asset.toString());
                        final Long member_id = asset.getAppAccountId();
                        DS ds = new DS(activity);
                        ds.open();
                        final String email_str = ds.query_database_key( "email");
                        final String sn_str = ds.query_database_key( "sn");
                        ds.close();

                        rootView.post(new Runnable() {
                            @Override
                            public void run() {
                                //boolean active = Boolean.parseBoolean(DS.query_database_key(activity, "activated"));

                                if(email_str.length()>0 && sn_str.length()>0) {
                                    Toast.makeText(activity, "Miembro " + member_id + " Autenticado", Toast.LENGTH_LONG);
                                    Intent intent = new Intent(Tablero.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    AlertDialogFragment adf = new AlertDialogFragment();
                                    adf.setMsg("MIEMBRO NO AUTENTICADO ");
                                    adf.show(getSupportFragmentManager(),"adf_member_not_aut");
                                    pb.setVisibility(View.GONE);
                                    access_button.setVisibility(View.VISIBLE);

                                }

                            }
                        });
                    }catch (final RuntimeException re){
                        rootView.post(new Runnable() {
                            @Override
                            public void run() {
                                //boolean active = Boolean.parseBoolean(DS.query_database_key(activity, "activated"));
                                AlertDialogFragment adf = new AlertDialogFragment();
                                adf.setMsg("FRACASO DE AUTENTICACION "+ re.getMessage());
                                adf.show(getSupportFragmentManager(),"adf_aut_failure");
                                pb.setVisibility(View.GONE);

                            }
                        });
                    }
                }
            }).start();
        }else{
            pb.setVisibility(View.GONE);
            access_button.setVisibility(View.VISIBLE);
        }

        }

}
