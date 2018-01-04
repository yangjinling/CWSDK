package cwbj.cwsdk2.activity;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import cwbj.cwsdk2.R;
import cwbj.cwsdk2.util.BluetoothGattUtil;
import cwbj.cwsdk2.util.PubUtils;
import cwbj.cwsdk2.util.SocketClient;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.sdk.CWSDK;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.DataBeanCallback;

public class SignActivity extends AppCompatActivity {
    private static CWSDK cwsdk = CWSDK.getInstance();
    private ImageView iv;
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;
    private SocketClient client;
    Handler bluetoothHandler = null;
    Handler wifiHandler = null;
    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private Button btn_sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        iv = ((ImageView) findViewById(R.id.sign_iv));
        btn_sign = ((Button) findViewById(R.id.btn_sign));
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText("签名");
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        bluetoothHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);

                switch (msg.what) {
                    case 7:
//                        Toast.makeText(SignActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        byte[] bytes = (byte[]) msg.obj;
                        Log.e("YJL", "bytes===" + bytes + "===" + bytes.length);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (null != bmp) {
                            iv.setImageBitmap(bmp);
                        }
                        break;

                    case 8:
                        Log.e("YJL", "失败");
//                        cwsdk.connectionDevice(WorkDev);
////                        eventMask = 0xF;
//                        cwsdk.sign(1);
                        break;
                }
            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 7:
                        Toast.makeText(SignActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                    case 8:
                        byte[] bytes = msg.obj.toString().getBytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        iv.setImageBitmap(bitmap);
                        break;
                }

            }
        };
        if (PubUtils.isWifi) {
            client = new SocketClient();
            //服务端的IP地址和端口号
//                    client.clintValue(getApplicationContext(), "192.168.0.48", 9999);
            client.clintValue(getApplicationContext(), PubUtils.ip, 9999, wifiHandler);
            //开启客户端接收消息线程
            client.openClientThread();
        } else {
            WorkDev = cwsdk.GetBlueToothDevicesByMAC(sMAC);
            if (PubUtils.isBle) {
                bluetoothGattUtil = BluetoothGattUtil.getInstance();
                WorkDev.connectGatt(SignActivity.this, false, bluetoothGattUtil);
                bluetoothGattUtil.registerCallBack(new BluetoothGattUtil.connectCallBack() {
                    @Override
                    public void connectSuccess() {
                        isCanWrite = true;
                    }

                    @Override
                    public void dealData(int type, final String data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prograssBar.setVisibility(View.INVISIBLE);
                                byte[] bytes = data.getBytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                iv.setImageBitmap(bitmap);
                            }
                        });
                    }

                    @Override
                    public void getDataFail(final int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prograssBar.setVisibility(View.INVISIBLE);
//                                tv.setText("" + code);
                            }
                        });

                    }

                    @Override
                    public void connectfail() {
                        isCanWrite = false;
                    }
                });
            } else {
                cwsdk.initialize(SignActivity.this, true, bluetoothHandler);
                cwsdk.workmode = 1;
                cwsdk.IDataBeanCallback = new DataBeanCallback() {
                    @Override
                    public Boolean postData(DataBean dataBean) {
                        // TODO Auto-generated method stub
//                        GetCmdId(dataBean);
                        return null;
                    }
                };
            }
        }
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(10);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(10);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
//                        eventMask = 0xF;
                        cwsdk.sign(0);

                    }
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        cwsdk.finalize();
        cwsdk.StopDiscovery();
        if (null != bluetoothGattUtil) {
            bluetoothGattUtil.unregisterCallBack();
            bluetoothGattUtil.disconnect();
        }
        if (null != client) {
            client.unregisterHandler();
            client.disconnect();
        }
    }
}
