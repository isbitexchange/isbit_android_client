package com.isbit.m;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;



public class RealTimeMarketData extends Activity {


    public static final String ACTION_ORDERBOOK_CHANGED = "ACTION_ORDERBOOK_CHANGED" ;
    public static final String EXTRA_PAYLOAD_STRING = "EXTRA_PAYLOAD_STRING";

    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.isbit.m.R.layout.activity_main);

        connectWebSocket();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(com.isbit.m.R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.isbit.m.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case com.isbit.m.R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(com.isbit.m.R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private void connectWebSocket() {
        URI uri;
        try {

            DS ds = new DS(RealTimeMarketData.this);
            ds.open();
            String host = ds.query_url_host();
            ds.close();

            uri = new URI("ws://"+host+":8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(com.isbit.m.R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);


                        try {
                            JSONObject msg_json = new JSONObject(message);

                            String challenge = msg_json.getString("challenge");



                            DS ds = new DS(RealTimeMarketData.this);
                            ds.open();
                            String access_key = ds.query_access_key();
                            String private_key = ds.query_secret_key();
                            ds.close();

                            String payload = access_key + challenge;
                            textView.setText(textView.getText() + "\n challenge = " + challenge + "\n");

                            textView.setText(textView.getText() + "\n payload = " + payload + "\n");

                            String signature = hmacsha256_encode(private_key,payload);

                            textView.setText(textView.getText() + "\n signature = " + signature + "\n");


                            JSONObject auth_json  =  new JSONObject();

                            auth_json.put("access_key",access_key);
                            auth_json.put("answer", signature);

                            JSONObject reply_json = new JSONObject();

                            reply_json.put("auth", auth_json);

                            textView.setText(textView.getText() + "\n Mensaje autenticación = " + reply_json.toString() + "\n");

                            mWebSocketClient.send(auth_json.toString());

                            JSONObject orderbook_msg = new JSONObject();

                            orderbook_msg.put("orderbook",new JSONObject().put("action","update"));

                            mWebSocketClient.send(orderbook_msg.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(com.isbit.m.R.id.message);
        mWebSocketClient.send(editText.getText().toString());
        editText.setText("");
    }


    public  String hmacsha256_encode(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return str2Hex(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }



    public String convertStringToHex(String str){

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }

    public String str2Hex(byte[] bytes) {
        StringBuffer hash = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }

    return hash.toString();
    }


}
