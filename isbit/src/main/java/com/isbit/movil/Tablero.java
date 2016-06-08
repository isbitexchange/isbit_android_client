package com.isbit.movil;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Tablero extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.isbit.movil.R.layout.activity_tablero);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url_str = "https://isbit.co//api/v2/order_book.json?market=btcmxn&asks_limit=10&bids_limit=10";
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url_str = "https://isbit.co/api/v2/tickers.json";
                HttpURLConnection urlConnection =null;
                URL url = null;

                try {
                    url = new URL(url_str);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    // urlConnection.setDoOutput(true);
                    // urlConnection.setChunkedStreamingMode(0);
                    // OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    // writeStream(out);
                    // out.write("hola".getBytes());

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    final String respuesta= readStream(in);

                    Tablero.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json_ticker =  new JSONObject(respuesta).getJSONObject("btcmxn").getJSONObject("ticker");
                                Long at =  new JSONObject(respuesta).getJSONObject("btcmxn").getLong("at");
                                Double buy = json_ticker.getDouble("buy");
                                Double sell = json_ticker.getDouble("sell");
                                Double ultimo_precio = json_ticker.getDouble("last");
                                TextView ticker_tv = (TextView) findViewById(com.isbit.movil.R.id.ticker_tv);
                                ticker_tv.setText(
                                    " precio venta: "+sell + "\n ulimo precio: "+ultimo_precio+" \n precio compra: "+buy+" \n fecha: "+(new Date(at*1000)).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

        Button button = (Button) findViewById(com.isbit.movil.R.id.button_acceder);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent login_activity_intent = new Intent(Tablero.this, LoginActivity.class);
                // startActivity(login_activity_intent);
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "PRODUCT_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
                intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
                startActivityForResult(intent, 0);
            }
        });
    }


    private String readStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    private class StableArrayAdapter extends ArrayAdapter<JSONObject> {
        HashMap<JSONObject, Integer> mIdMap = new HashMap<JSONObject, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<JSONObject> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            JSONObject item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout ll = new LinearLayout(Tablero.this);
            TextView precio = new TextView(Tablero.this);
            TextView volumen = new TextView(Tablero.this);

            ll.addView(precio);
            ll.addView(volumen);

            try {
                if(getItem(position).getString("side").equalsIgnoreCase("buy")){
                    precio.setTextColor(Color.GREEN);
                }else{
                    precio.setTextColor(Color.RED);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                precio.setText(getItem(position).getString("price"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {

                volumen.setText("      "+getItem(position).getString("remaining_volume"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ll;
           // return super.getView(position, convertView, parent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT"); //this is the result

                try {
                    JSONObject yeison = new JSONObject(contents);
                    String key = yeison.getString("key");
                    String secret = yeison.getString("secret");

                    Log.e("api", key + " " + secret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("shit", "La respuesta fue " + contents);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            } else {
                // do something else
            }
        }
    }
}
