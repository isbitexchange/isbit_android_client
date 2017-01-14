package com.isbit.movil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.bitcoin.market.IsbitMXNApi;
import org.bitcoin.market.bean.AppAccount;
import org.bitcoin.market.bean.BitOrder;
import org.bitcoin.market.bean.OrderSide;
import org.bitcoin.market.bean.Symbol;
import org.bitcoin.market.bean.SymbolPair;


public class CancelOrderDialogFragment extends DialogFragment {
    private BitOrder ord;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String msg = "";
        String title = "CANCELAR orden de ";

        if(OrderSide.sell.equals(ord.getOrderSide())){
            title += "VENTA?";

        }else {
            title += "COMPRA?";
        }


        msg+="Precio: "+ord.getOrderMxnPrice()+" MXN";
        msg+="\n";
        msg+="Volumen: "+ord.getOrderAmount()+" BTC";
        msg+="\n";

        builder.setTitle(title);
        builder.setMessage(msg)
                .setPositiveButton("Sí",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                              IsbitMXNApi api = new IsbitMXNApi(getActivity());
                                AppAccount appAccount =  new AppAccount();

                                DS ds = new DS(getActivity());
                                ds.open();
                                appAccount.setAccessKey(ds.query_access_key());
                                appAccount.setSecretKey(ds.query_secret_key());
                                ds.close();

                                SymbolPair symbol_pair = new SymbolPair(Symbol.btc,Symbol.mxn);
                                api.cancel(appAccount, ord.getOrderId(), symbol_pair);

                                Intent broadcast = new Intent();
                                broadcast.setAction(RealTimeMarketData.ACTION_ORDERBOOK_CHANGED);
                                broadcast.putExtra(RealTimeMarketData.EXTRA_PAYLOAD_STRING,"{}");

                                getActivity().sendBroadcast(broadcast);


                            }
                        }).start();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public BitOrder getOrder() {
        return ord;
    }

    public void setOrder(BitOrder ord) {
        this.ord = ord;
    }}
