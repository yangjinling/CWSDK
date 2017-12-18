package cwbj.cwsdk2.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import cwbj.cwsdk2.adapter.MyAdapter;
import cwbj.cwsdk2.R;
import cwbjsdk.cwsdk.sdk.CWSDK;


public class BT_DevicesShow extends Activity {

    ListView lvBTDevices = null;
    //    ArrayAdapter<String> adtDevices;
    MyAdapter adtDevices;
    List<String> lstDevices = new ArrayList<String>();

    Handler handler = null;
    String ReStr = null;
    ListView lv = null;
    private static CWSDK cwsdk = CWSDK.getInstance();
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt__devices_show);
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        prograssBar.setVisibility(View.VISIBLE);
        tv_title.setText(R.string.action_scanning);
        lvBTDevices = (ListView) findViewById(R.id.listViewa);
      /*  adtDevices = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lstDevices);*/
        adtDevices = new MyAdapter(this, lstDevices);
        lvBTDevices.setAdapter(adtDevices);

        cwsdk.workmode = 0;

        lvBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                // TODO Auto-generated method stub

                Intent intent = new Intent();
                ReStr = (String) lvBTDevices.getItemAtPosition((int) arg3);
                cwsdk.finalize();
                intent.putExtra("SelectedDevices", ReStr);
                setResult(20, intent);
                finish();

            }

        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x1) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    Log.e("CWLOG", "接受了---" + msg.obj.toString());
                    Log.e("CWLOG", "listsie===" + lstDevices.size());
                    if (lstDevices.contains(msg.obj.toString())) {

                    } else {
                        lstDevices.add(msg.obj.toString());
                        adtDevices.notifyDataSetChanged();
                    }
                }
            }

        };

        cwsdk.initialize(BT_DevicesShow.this, true, handler);

    }


    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("CWLOG", "BT_DevicesShow onPause called.");
    }

    /*       校验蓝牙权限      */
    private void checkBluetoothPermission(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            } else {
                //具有权限
                cwsdk.initialize(BT_DevicesShow.this, true, handler);
            }
        } else {
            //系统不高于6.0直接执行
            cwsdk.initialize(BT_DevicesShow.this, true, handler);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                cwsdk.initialize(BT_DevicesShow.this, true, handler);
            } else {
                // 权限拒绝                // 下面的方法最好写一个跳转，可以直接跳转到权限设置页面，方便用户
//                          denyPermission();
            }
        }
    }

    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        cwsdk.finalize();
        cwsdk.StopDiscovery();
        Log.e("CWLOG", "BT_DevicesShow onStop called.");

    }

    //退出当前Activity时被调用,调用之后Activity就结束了
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("CWLOG", "BT_DevicesShow onDestory called.");
    }

}
