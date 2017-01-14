package com.isbit.movil;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bitcoin.market.IsbitMXNApi;
import org.bitcoin.market.bean.AppAccount;
import org.bitcoin.market.bean.Asset;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FundsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FundsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FundsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    public long last_display_funds_update =0;
   // private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FundsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance(int param1) {
        Fragment fragment = new FundsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
       // args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
          //  mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_funds, container, false);

        displayFunds(rootView);

        displayMarketSummary(); //always call fidplaymarket summary after displayFunds.  we need end point url. we have this url in db only once  diaplyFunds finishes



        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public void displayMarketSummary() {

        if (last_display_funds_update>0){

        new Thread(new Runnable() {
            @Override
            public void run() {
                DS ds = new DS(getActivity());
                ds.open();
                String url_str = ds.get_isbit_url() + "/api/v2/tickers.json"; //TODO correct url depending on environment
                ds.close();
                HttpURLConnection urlConnection = null;
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
                    final String respuesta = readStream(in);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json_ticker = new JSONObject(respuesta).getJSONObject("btcmxn").getJSONObject("ticker");
                                Long at = new JSONObject(respuesta).getJSONObject("btcmxn").getLong("at");
                                Double buy = json_ticker.getDouble("buy");
                                Double sell = json_ticker.getDouble("sell");
                                Double ultimo_precio = json_ticker.getDouble("last");
                                TextView ticker_tv = (TextView) getActivity().findViewById(com.isbit.movil.R.id.ticker_tv);
                                ticker_tv.setText(
                                        " precio venta: " + sell + "\n ulimo precio: " + ultimo_precio + " \n precio compra: " + buy + " \n fecha: " + (new Date(at * 1000)).toString());
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

    }
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


    public void displayFunds(final View rootView){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AppAccount app_account =  new AppAccount();
                DS ds = new DS(getActivity());
                ds.open();
                app_account.setAccessKey(ds.query_access_key());
                app_account.setSecretKey(ds.query_secret_key());
                ds.close();
                //IsbitMXNApi api = new  IsbitMXNApi(DS.query_url_schema(rootView.getContext())+"://"+DS.query_url_host(rootView.getContext()));
                IsbitMXNApi api = new  IsbitMXNApi(getActivity());
                final Asset asset = api.getInfo(app_account);

                Log.i("MainActivity", asset.toString());

                Log.i("MainActivity", "email: "+app_account.getEmail());


                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView section_label = (TextView) rootView.findViewById(R.id.section_label);
                        TextView email = (TextView) rootView.findViewById(R.id.email);
                        TextView available_btc_label = (TextView)  rootView.findViewById(R.id.available_btc_label);
                        TextView frozen_btc_label =    (TextView)  rootView.findViewById(R.id.frozen_btc_label);
                        TextView available_mxn_label = (TextView)  rootView.findViewById(R.id.available_mxn_label);
                        TextView frozen_mxn_label = (TextView)    rootView.findViewById(R.id.frozen_mxn_label);
                        TextView name = (TextView) rootView.findViewById(R.id.name);
                        TextView sn = (TextView) rootView.findViewById(R.id.sn);
                        TextView activated = (TextView) rootView.findViewById(R.id.activated);


                        DS ds = new DS(getActivity());
                        ds.open();
                        section_label.setText(ds.get_isbit_url());

                        available_btc_label.setText(String.format("%.8f",asset.getAvailableBtc()));
                        frozen_btc_label.setText(String.format("%.8f",asset.getFrozenBtc()));
                        available_mxn_label.setText(String.format("%.8f",asset.getAvailableMxn()));
                        frozen_mxn_label.setText(String.format("%.8f",asset.getFrozenMxn()));

                        String email_str = ds.query_database_key("email");
                        String sn_str = ds.query_database_key("sn");

                        sn.setText(sn_str);
                        email.setText(email_str);
                        name.setText(ds.query_database_key("name"));
                        activated.setText("Activo? "+ ds.query_database_key( "activated"));

                        ds.close();
                        SetActionbarInformation a = (SetActionbarInformation) getActivity();
                        a.setActionbarTitle("FONDOS");
                        a.setActionbarSubtitle(email_str+ " (SN:  "+sn_str+")");

                        last_display_funds_update=new Date().getTime();

                    }
                });


            }
        }).start();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
