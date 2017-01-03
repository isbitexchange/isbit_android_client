package com.isbit.movil;

import android.app.Activity;
import android.content.Context;
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


        final Context context = rootView.getContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AppAccount app_account =  new AppAccount();
                app_account.setAccessKey(DB.query_access_key(context));
                app_account.setSecretKey(DB.query_secret_key(context));
                //IsbitMXNApi api = new  IsbitMXNApi(DB.query_url_schema(rootView.getContext())+"://"+DB.query_url_host(rootView.getContext()));
                IsbitMXNApi api = new  IsbitMXNApi(context);
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


                        section_label.setText(DB.get_isbit_url(context));

                        available_btc_label.setText(asset.getAvailableBtc().toString()+" BTC (Disponibles)");
                        frozen_btc_label.setText(asset.getFrozenBtc().toString()+" BTC (Congelados)");
                        available_mxn_label.setText(asset.getAvailableMxn().toString()+" MXN (Disponibles)");
                        frozen_mxn_label.setText(asset.getFrozenMxn()+ " MXN (Congelados)");

                        sn.setText(DB.query_database_key(context,"sn"));
                        email.setText(DB.query_database_key(context,"email"));
                        name.setText(DB.query_database_key(context,"name"));
                        activated.setText("Activo? "+DB.query_database_key(context, "activated"));

                    }
                });


            }
        }).start();



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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
