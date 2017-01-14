package com.isbit.movil;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.DoubleArraySerializer;

import org.bitcoin.market.bean.BitOrder;
import org.bitcoin.market.bean.OrderSide;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sebastian on 01/01/2017.
 */

 class RunningOrdersArrayAdapter extends ArrayAdapter<BitOrder> {
    private Context context;
    HashMap<BitOrder, Integer> mIdMap = new HashMap<BitOrder, Integer>();

    public RunningOrdersArrayAdapter(Context context, int textViewResourceId,
                                     List<BitOrder> objects) {
        super(context, textViewResourceId, objects);

        this.context = context;


        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        BitOrder item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView datetime = new TextView(context);

        TextView precio = new TextView(context);
        precio.setPadding(0,0,8,0);

        LinearLayout hl1 = new LinearLayout(context);
        hl1.setOrientation(LinearLayout.HORIZONTAL);


        TextView volumen = new TextView(context);
       // BitOrder ord = getItem(position);
        ll.addView(datetime);
        ll.addView(hl1);

        hl1.addView(precio);
        hl1.addView(volumen);



        try {
            if((OrderSide.buy).equals(getItem(position).getOrderSide())){
                precio.setTextColor(Color.GREEN);
            }else{
                precio.setTextColor(Color.RED);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            precio.setText(String.format("%.8f",getItem(position).getOrderMxnPrice()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            volumen.setText(String.format("%.8f",getItem(position).getOrderAmount()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {


            android.text.format.DateFormat df = new android.text.format.DateFormat();
            //datetime.setText(df.format("MM/dd hh:mm a", getItem(position).getDatetime()));
            datetime.setText(getItem(position).getCreateTime()+"");
        }catch (Exception e){
            e.printStackTrace();
            Log.e("RunningOrdersArray","problem printing running order date ---"+e.toString());
        }

        return ll;
        // return super.getView(position, convertView, parent);
    }
}
