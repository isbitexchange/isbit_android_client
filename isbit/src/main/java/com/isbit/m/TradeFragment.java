package com.isbit.m;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import org.bitcoin.market.IsbitMXNApi;
import org.bitcoin.market.bean.AppAccount;
import org.bitcoin.market.bean.BitOrder;
import org.bitcoin.market.bean.OrderType;
import org.bitcoin.market.bean.Symbol;
import org.bitcoin.market.bean.SymbolPair;
import org.bitcoin.market.utils.TradeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.os.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TradeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradeFragment extends Fragment implements RefreshOrdersInformation {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "TradeFragment";

    private IntentFilter intentFilter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private ChangesReceiver changesReceiver;

    //Handler
    Handler h = new Handler();

    //Seconds pased to reload the trading content
    int delay = 5*1000; //1 second=1000 milisecond
    Runnable runnable;

    public TradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TradeFragment newInstance(String param1, String param2) {
        TradeFragment fragment = new TradeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        intentFilter = new IntentFilter();
        intentFilter.addAction(RealTimeMarketData.ACTION_ORDERBOOK_CHANGED);
         changesReceiver = new ChangesReceiver();
        //getActivity().registerReceiver( changesReceiver,intentFilter);
    }

    @Override
    public void onResume(){
        h.postDelayed(new Runnable() {
            public void run() {
                //Load the order book
                loadOrderBook();

                runnable=this;

                h.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
        getActivity().registerReceiver(changesReceiver,intentFilter);

    }

    @Override
    public void onPause(){
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
        try {
            getActivity().unregisterReceiver(changesReceiver);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStop(){
        super.onStop();
        try {
            getActivity().unregisterReceiver(changesReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_trade, container, false);
        final  SetActionbarInformation activity = (SetActionbarInformation) getActivity();

        initTradeControl(true); //<----- THIS INITS A CONTROL FOR ASK ORDERS (SECOND PARAM TRUE)
        initTradeControl(false);  //<----- THIS INITS A CONTROL FOR  BID ORDERS (SECOND PARAMETER FALSE)


        refresh();

        activity.setActionbarTitle("ISBIT BTC/MXN");
        getActivity().registerReceiver( changesReceiver,intentFilter);


        return  rootView;
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

    private void loadOrderBook(){
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

                JSONObject depth = api.get_depth(new SymbolPair(Symbol.btc, Symbol.mxn), false);
                Log.i("DepositFragment",depth.toString());


                com.alibaba.fastjson.JSONArray asks_json = depth.getJSONArray("asks");
                com.alibaba.fastjson.JSONArray bids_json = depth.getJSONArray("bids");

                final ArrayList< JSONObject> bid_list = new ArrayList< JSONObject>();
                final ArrayList< JSONObject> ask_list = new ArrayList< JSONObject>();


                // for (int i = asks_json.size()-1; i >= 0;   --i) {  // we want to reverse order?
                for (int i = 0; i < asks_json.size();   ++i) {
                    try {
                        if(asks_json.getJSONObject(i).getDouble("amount")>0) {
                            asks_json.getJSONObject(i).put("side","sell");
                            ask_list.add(asks_json.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                for (int i = 0; i < bids_json.size();   ++i) {
                    try {
                        if(bids_json.getJSONObject(i).getDouble("amount")>0) {
                            bids_json.getJSONObject(i).put("side","buy");
                            bid_list.add(bids_json.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }




                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        final ListView bid_orderbook_list = (ListView) getActivity().findViewById(R.id.bid_orderbook_list);

                        final StableArrayAdapter bid_list_adapter = new StableArrayAdapter(getActivity(),
                                android.R.layout.simple_list_item_1, bid_list);
                        bid_orderbook_list.setAdapter(bid_list_adapter);

                        final ListView ask_orderbook_list = (ListView) getActivity().findViewById(R.id.ask_orderbook_list);

                        final StableArrayAdapter ask_list_adapter = new StableArrayAdapter(getActivity(),
                                android.R.layout.simple_list_item_1, ask_list);
                        ask_orderbook_list.setAdapter(ask_list_adapter);
                    }
                });
            }
        }).start();
    }

    private void loadRunningOrders(){
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

                final List<BitOrder> running_orders = api.getRunningOrders(app_account);
                 Collections.reverse(running_orders);

                Log.i("loadRunningOrders",running_orders.toString());

                rootView.post(new Runnable() {
                    @Override
                    public void run() {
                        final ListView list = (ListView) getActivity().findViewById(R.id.running_orders_lv);

                        final RunningOrdersArrayAdapter bid_list_adapter = new RunningOrdersArrayAdapter(getActivity(),
                                android.R.layout.simple_list_item_1, running_orders);
                        list.setAdapter(bid_list_adapter);

                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                               BitOrder itm = (BitOrder) list.getItemAtPosition(position);
                                displayCancelOrderDialog(itm);
                            }
                        });


                    }
                });
            }
        }).start();
    }

    private void initTradeControl( final boolean is_ask){
        // --------------- STARt SELL  or BUY ORDERS CONTROLS

        Button trade_button;

       final EditText price_et;
       final  EditText qty_et;
        final EditText total_et;
        if(is_ask) { //we are setting up the controls for ask orders
            trade_button = (Button) rootView.findViewById(R.id.sell_button);
            price_et = (EditText) rootView.findViewById(R.id.ask_price_et);
            qty_et = (EditText) rootView.findViewById(R.id.ask_qty_et);
            total_et = (EditText) rootView.findViewById(R.id.ask_total_et);
        }else{  //we are setting up the controls for bid orders
            price_et = (EditText) rootView.findViewById(R.id.bid_price_et);
            qty_et = (EditText) rootView.findViewById(R.id.bid_qty_et);
            total_et = (EditText) rootView.findViewById(R.id.bid_total_et);
            trade_button = (Button) rootView.findViewById(R.id.buy_button);

        }

        price_et.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (price_et.isFocused()) {
                            try {

                                final double amount = Double.parseDouble(qty_et.getText().toString());
                                final double price = Double.parseDouble(price_et.getText().toString());
                                double total = price * amount;
                                total_et.setText(String.format(Locale.getDefault(),"%.8f",total));
                            } catch (NumberFormatException e1) {

                            } catch (NullPointerException e2) {

                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );


        qty_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(qty_et.isFocused()) {
                    try {
                        final double amount = Double.parseDouble(qty_et.getText().toString());
                        final double price = Double.parseDouble(price_et.getText().toString());
                        double total = price * amount;
                        total_et.setText(String.format(Locale.getDefault(),"%.8f",total));
                    } catch (NumberFormatException e1) {

                    } catch (NullPointerException e2) {

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        total_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (total_et.isFocused()) {

                    try {
                        final double total = Double.parseDouble(total_et.getText().toString());
                        final double price = Double.parseDouble(price_et.getText().toString());
                        if (price > 0) {
                            double qty = total / price;
                            qty_et.setText(String.format(Locale.getDefault(),"%.8f",qty));
                        }
                    }catch(NumberFormatException e1){

                    }catch(NullPointerException e2){

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        trade_button.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        try {
                            final double amount = Double.parseDouble(qty_et.getText().toString());
                            final double price = Double.parseDouble(price_et.getText().toString());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final AppAccount app_account = new AppAccount();
                                    DS ds = new DS(getActivity());
                                    ds.open();
                                    app_account.setAccessKey(ds.query_access_key());
                                    app_account.setSecretKey(ds.query_secret_key());
                                    ds.close();

                                    IsbitMXNApi api = new IsbitMXNApi(getActivity());
                                    try {
                                        if(is_ask) {
                                            final Long id = api.sell(app_account, amount, price, new SymbolPair(Symbol.btc, Symbol.mxn), OrderType.Limit);
                                            rootView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (id > 0) {
                                                        AlertDialogFragment dlg = new AlertDialogFragment();
                                                        dlg.setMsg("La Orden de Venta fue colocada exitosamente. #Folio " + id);
                                                        dlg.show(getActivity().getSupportFragmentManager(), "ask_order_placed_success_dlg");
                                                    refresh();
                                                    } else {
                                                        //order placement failed
                                                    }
                                                }
                                            });
                                        }else {
                                            final Long id = api.buy(app_account, amount, price, new SymbolPair(Symbol.btc, Symbol.mxn), OrderType.Limit);
                                            rootView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (id > 0) {
                                                        AlertDialogFragment dlg = new AlertDialogFragment();
                                                        dlg.setMsg("La Orden de Compra fue colocada exitosamente. #Folio " + id);
                                                        dlg.show(getActivity().getSupportFragmentManager(), "bid_order_placed_success_dlg");
                                                     refresh();
                                                    } else {
                                                        //order placement failed
                                                    }
                                                }
                                            });
                                        }

                                    }catch (TradeException te){
                                        te.getCode();
                                        AlertDialogFragment dlg = new AlertDialogFragment();
                                        dlg.setMsg("FRACASO en  colocación de Orden. Código "+te.getCode());
                                        dlg.show(getActivity().getSupportFragmentManager(), "order_failure_dlg");

                                        refresh();

                                    }

                                }
                            }).start();


                        }catch (Exception e){

                            Log.e(TAG,e.toString());
                            Toast.makeText(getActivity(),"Parametros de Orden incorrectos",Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );


        //------------END SELL ORDERS CONTROLS
    }


    private void displayCancelOrderDialog(BitOrder itm){

        CancelOrderDialogFragment dlg = new CancelOrderDialogFragment();
        dlg.setOrder(itm);
        dlg.show(getActivity().getSupportFragmentManager(),"cancel_order_dlg");
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void refresh(){
        loadOrderBook();
        loadRunningOrders();
    }

    public class ChangesReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent i){
        refresh();
        }
    }
}
