package cwbj.cwsdk2.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import cwbj.cwsdk2.R;
import cwbj.cwsdk2.util.PrefUtils;
import cwbj.cwsdk2.util.PubUtils;

public class MainActivity extends AppCompatActivity {


    EditText et1 = null;
    EditText et2 = null;
    EditText et3 = null;

    TextView tv = null;
    TextView tv1 = null;
    TextView tv2 = null;
    TextView tv3 = null;


    //private static imatecw cw_imate = imatecw.getInstance();

    String SelecetedDevicesStr = null;
    String SelecetedMACStr = null;
    private Button back;
    private Button btn_ble;
    private Button btn_wifi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv = (TextView) findViewById(R.id.sample_text);
        et1 = (EditText) findViewById(R.id.msg1);
        et2 = (EditText) findViewById(R.id.msg2);
        et3 = (EditText) findViewById(R.id.msg3);
        btn_ble = ((Button) findViewById(R.id.btn_ble));
        btn_wifi = ((Button) findViewById(R.id.btn_wifi));
        btn_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubUtils.isWifi = true;
                ChangeURL();
            }
        });
        btn_ble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubUtils.isWifi = false;
                if (!PubUtils.isBle) {
                    PubUtils.isBle = true;
                    btn_ble.setText("ble通信");
                } else {
                    PubUtils.isBle = false;
                    btn_ble.setText("经典蓝牙");
                }
            }
        });
        // tv.setText(stringFromJNI());


    }

    private void ChangeURL() {
        final EditText editText = new EditText(this);
        editText.setText(PrefUtils.getString(MainActivity.this, "URL", "172.20.10.3"));
        new AlertDialog.Builder(this).setTitle("修改URL").setView(editText)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = editText.getText().toString();
                        PrefUtils.setString(MainActivity.this, "URL",url);
                        PubUtils.ip = url;
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
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
            /*获取固件版本*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, GetVerActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, GetVerActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scanning) {
            /*蓝牙扫描设备*/
            Intent intent = new Intent(MainActivity.this, BT_DevicesShow.class);
            startActivityForResult(intent, 20);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clcard) {
            /*接触卡或非接触卡*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, WorkActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, WorkActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        if (id == R.id.action_ID) {
            /*读取身份证*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, IDMessageActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, IDMessageActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (id == R.id.action_Fprinter) {
            /*指纹数据读取*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, FprinterActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, FprinterActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (id == R.id.action_MagicCard) {
            /*读取磁条卡*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, MagicActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, MagicActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (id == R.id.action_KeyPad) {
            /*键盘输入pin*/
            if (PubUtils.isWifi) {
                Intent intent = new Intent(MainActivity.this, KeyPadActivity.class);
                intent.putExtra("MAC", SelecetedMACStr);
                startActivity(intent);
            } else {
                if ("已配对".equals(et3.getText().toString())) {
                    Intent intent = new Intent(MainActivity.this, KeyPadActivity.class);
                    intent.putExtra("MAC", SelecetedMACStr);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(et3.getText().toString())) {
                    Toast.makeText(this, "请去扫描选择蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请去设置中进行蓝牙配对", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == 20) {

            SelecetedDevicesStr = data.getStringExtra("SelectedDevices");

            String[] BT_Devices = SelecetedDevicesStr.split("\\|");

            et3.setText(BT_Devices[0]);

            // String[] BT_DevicesName =  BT_Devices[1].split(":");

            et1.setText(BT_Devices[1]);

            //String[] BT_DevicesMAC =  BT_Devices[2].split(":");

            SelecetedMACStr = BT_Devices[2];  //BT_DevicesMAC[1]+":"+ BT_DevicesMAC[2]+":"+ BT_DevicesMAC[3]+":"+ BT_DevicesMAC[4];
            et2.setText(SelecetedMACStr);

            tv.setText("蓝牙设备已选择!");
        }

    }


}
