package com.kmfrog.dabase.example;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kmfrog.dabase.adapter.BaseListAdapter;
import com.kmfrog.dabase.data.model.ListLoader;

import java.util.zip.Inflater;

public class MainActivity extends Activity {

    ListView lv;

    BaseListAdapter<Versus> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv =(ListView)findViewById(R.id.lv1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new BaseListAdapter<Versus>(App.get().getRequestQueue(), Uri.parse("http://192.168.0.91:8080/v1/events/choice"), true) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null) {
                    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                    view = inflater.inflate(R.layout.item, null);
                }
                TextView tv = (TextView) view.findViewById(R.id.tv1);
                tv.setText(getItem(i).getCreatedBy());
                return view;
            }
        };

        adapter.start();


    }
}
