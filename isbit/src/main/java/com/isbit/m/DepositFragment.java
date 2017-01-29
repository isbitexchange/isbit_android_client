package com.isbit.m;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.bitcoin.market.IsbitMXNApi;
import org.bitcoin.market.bean.AppAccount;
import org.bitcoin.market.bean.Symbol;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DepositFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DepositFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DepositFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DepositFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DepositFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DepositFragment newInstance(String param1, String param2) {
        DepositFragment fragment = new DepositFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final  View rootView =  inflater.inflate(R.layout.fragment_deposit, container, false);
        final Activity activity = getActivity();

        final ImageView qriv = (ImageView) rootView.findViewById(R.id.qrImageView);
        final TextView addr_tv = (TextView) rootView.findViewById(R.id.addr);


        new Thread(new Runnable() {
            @Override
            public void run() {
                IsbitMXNApi api = new IsbitMXNApi(rootView.getContext());
                AppAccount appAccount = new AppAccount();
                DS ds = new DS(activity);
                ds.open();
                appAccount.setSecretKey(ds.query_secret_key());
                appAccount.setAccessKey(ds.query_access_key());
                ds.close();
                final JSONObject resp = api.getDepositAddress(appAccount,Symbol.btc);
                final String address = resp.getString("address");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        try {
                            BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE,200,200);
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            qriv.setImageBitmap(bitmap);

                            addr_tv.setText(address);

                            qriv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String shareBody = ""+address;
                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                    sharingIntent.setType("text/plain");
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DIRECCION DEPOSITO");
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
                                }
                            });


                        }catch (WriterException e){
                            e.printStackTrace();
                            Log.e("DepositFragment",e.toString());
                        }
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
