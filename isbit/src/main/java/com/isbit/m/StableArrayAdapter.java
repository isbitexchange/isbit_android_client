package com.isbit.m;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sebastian on 01/01/2017.
 */

 class StableArrayAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    HashMap<JSONObject, Integer> mIdMap = new HashMap<JSONObject, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,
                              List<com.alibaba.fastjson.JSONObject> objects) {
        super(context, textViewResourceId, objects);

        this.context = context;


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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout ll = new LinearLayout(context);
        TextView precio = new TextView(context);
        TextView volumen = new TextView(context);

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

            volumen.setText("      "+getItem(position).getString("amount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ll;
        // return super.getView(position, convertView, parent);
    }
}
