package com.kmfrog.dabase.example;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.kmfrog.dabase.adapter.BaseListAdapter;
import com.kmfrog.dabase.data.DataCallback;
import com.kmfrog.dabase.data.JsonRequest;
import com.kmfrog.dabase.data.model.UiModel;
import com.kmfrog.dabase.exception.AppException;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    ListView lv;
    BaseListAdapter<Versus> versusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv1);
        Uri uri = Uri.parse("http://192.168.0.11:8080/v1/versus/list?pn=1&ps=20&token=burhqz8iiu7sxk1i4259811xe0fed0ec");
        versusAdapter = new BaseListAdapter<Versus>(App.get(), uri, new VersusParser()) {


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.item_versus, null);
                }
                TextView name = (TextView) convertView.findViewById(R.id.tv_vs_name);
                ImageView frontCover = (ImageView) convertView.findViewById(R.id.iv_vs_frontcover);
                Versus v = getItem(position);
                name.setText(v.createdDate);
                App.get().getBitmapLoader().bind(frontCover, "http://cn.bing.com/sa/simg/CN_Logo_Gray.png"/*v.frontCover*/, R.drawable.ic_launcher);
                return convertView;
            }
        };
        lv.setAdapter(versusAdapter);
        versusAdapter.start();
//        networkRequest();
    }

    private void networkRequest() {
        UiModel<AppVersionInfo> info = new UiModel<AppVersionInfo>(uri,parser);
        info.start();
//        Uri uri = Uri.parse("http://api.zuoyoupk.com/v1/version/info?client=ios");
//        Uri uri = Uri.parse("http://192.168.0.11:8080/v1/version/info?client=ios");
//        JsonRequest<AppVersionInfo> req = new JsonRequest<AppVersionInfo>(uri, new AppVersionInfoParser(), new DataCallback<AppVersionInfo>() {
//            @Override
//            public void onAppError(AppException ex) {
//
//            }
//
//            @Override
//            public void onSuccess(AppVersionInfo result) {
//                tv.setText(result.createdDate);
//            }
//
//            @Override
//            public void onFailure(Throwable e) {
//
//            }
//        });
        Uri uri = Uri.parse("http://192.168.0.11:8080/v1/versus/list?pn=1&token=burhqz8iiu7sxk1i4259811xe0fed0ec");
        JsonRequest<List<Versus>> req = new JsonRequest<List<Versus>>(uri, new VersusParser(), new DataCallback<List<Versus>>() {
            @Override
            public void onSuccess(List<Versus> result) {
                if (result.size() > 0) {
//                    tv.setText(result.get(0).createdDate);
                }
            }

            @Override
            public void onFailure(Throwable e) {

            }

            @Override
            public void onAppError(AppException ex) {

            }
        });
        App.get().getHttpExecutor().put(req);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            networkRequest();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
