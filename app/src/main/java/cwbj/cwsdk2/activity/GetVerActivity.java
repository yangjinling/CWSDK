package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cwbj.cwsdk2.R;
import cwbj.cwsdk2.util.BluetoothGattUtil;
import cwbj.cwsdk2.util.PubUtils;
import cwbj.cwsdk2.util.SocketClient;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.DataBeanCallback;
import cwbjsdk.cwsdk.sdk.CWSDK;

public class GetVerActivity extends Activity implements View.OnClickListener {


    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;
    TextView tv = null;
    Handler bluetoothHandler = null;
    Handler wifiHandler = null;
    ImageView img = null;
    private Button btn;
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;
    private SocketClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_ver);
        tv = (TextView) findViewById(R.id.textView4);
        btn = ((Button) findViewById(R.id.btn_getVet));
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_settings);
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        WorkDev = cwsdk.GetBlueToothDevicesByMAC(sMAC);
        bluetoothHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x6) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    tv.setText("读取固件版本号为：" + msg.obj.toString());
                }
            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 1:
                        byte[] Ver = BJCWUtil.StrToHex(msg.obj.toString());
                        String version = new String(Ver);
                        tv.setText("读取固件版本号为：" + version);
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                }

            }
        };
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_VERSION);
        if (PubUtils.isWifi) {
            client = new SocketClient();
            //服务端的IP地址和端口号
//                    client.clintValue(getApplicationContext(), "192.168.0.48", 9999);
            client.clintValue(getApplicationContext(), "172.16.39.182", 9999, wifiHandler);
            //开启客户端接收消息线程
            client.openClientThread();
        } else {
            if (PubUtils.isBle) {
                bluetoothGattUtil = BluetoothGattUtil.getInstance();
                WorkDev.connectGatt(GetVerActivity.this, false, bluetoothGattUtil);
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
                                byte[] Ver = BJCWUtil.StrToHex(data);
                                String version = new String(Ver);
                                tv.setText("读取固件版本号为：" + version);
                            }
                        });
                    }

                    @Override
                    public void getDataFail(int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prograssBar.setVisibility(View.INVISIBLE);
                                tv.setText("数据不完整");
                            }
                        });

                    }

                    @Override
                    public void connectfail() {
                        isCanWrite = false;
                    }
                });
            } else {
                cwsdk.initialize(GetVerActivity.this, true, bluetoothHandler);
                cwsdk.workmode = 1;

                //cw_imate.IDataBeanCallback = null;
                cwsdk.IDataBeanCallback = new DataBeanCallback() {
                    @Override
                    public Boolean postData(DataBean dataBean) {
                        // TODO Auto-generated method stub
                        GetCmdId(dataBean);
                        return null;
                    }
                };
            }
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(0);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(0);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(GetVerActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.GetVersion(20);
                    }
                }
            }
        });


    }


    @Override
    public void onClick(View v) {

    }


    private void GetCmdId(DataBean data) {
        int nCmdId = -1;
        nCmdId = data.GetCommId();
        Log.e("BJCW", "操作：" + nCmdId);
        if (nCmdId != DataBean.CMD_WAITEEVENT_OPERATION
                && nCmdId != DataBean.CMD_ICRESETCARD_OPERATION) {

        }
        switch (nCmdId) {
            case DataBean.CMD_OPERATION_CANCEL: {
                Log.e("BJCW", "取消操作");

                break;
            }
            case DataBean.CMD_IDEREADMESSAGE_OPERATION: {
                if (data.GetSucceed() == "true") {

                } else {
                    //updateUIMsg("读取身份证失败，是否重新读取？", 4);
                }
                break;
            }
            case DataBean.CMD_WAITEEVENT_OPERATION: {
                if (data.GetSucceed() == "true") {
                    if (data.GetCardType() == 1) {

                    } else if (data.GetCardType() == 2) {

                    } else if (data.GetCardType() == 4) {

                    }
                    if (data.GetCardType() == 1) {

                    } else {

                    }
                } else {
                    //  updateUIMsg("读取卡失败，是否重新读取？", 5);
                }
                break;
            }
            case DataBean.CMD_PBOCREADINFO_OPERATION:
                if (data.GetSucceed() == "true") {


                } else {
                    //updateUIMsg("读取卡失败，是否重新读取？", 5);
                }
                break;
            case DataBean.CMD_BLUTOOTH_STATE: {
                Log.e("CWLOG", "蓝牙状态：" + data.GetBleState());
                if (data.GetBleState().equals("已连接")) {


                } else {
                    //updateUIMsg("蓝牙未连接，请手动连接设备！", 1);
                }
                break;
            }
            default:
                break;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
