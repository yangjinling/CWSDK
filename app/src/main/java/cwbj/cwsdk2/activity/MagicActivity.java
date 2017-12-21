package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cwbj.cwsdk2.R;
import cwbj.cwsdk2.util.BluetoothGattUtil;
import cwbj.cwsdk2.util.PubUtils;
import cwbj.cwsdk2.util.SocketClient;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.DataBeanCallback;
import cwbjsdk.cwsdk.sdk.CWSDK;
import cwbjsdk.cwsdk.util.ParserIdentity;

public class MagicActivity extends Activity implements View.OnClickListener {


    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;

    TextView tv1 = null;
    TextView tv2 = null;
    TextView tv3 = null;

    Button btc = null;
    Handler handler = null;
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;
    private SocketClient client;
    Handler wifiHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic);
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_MagicCard);

        tv1 = (TextView) findViewById(R.id.textView6);
        tv2 = (TextView) findViewById(R.id.textView8);
        tv3 = (TextView) findViewById(R.id.textView10);
        btc = (Button) findViewById(R.id.button7);
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
//        PubUtils.COMMAND_MAGNETIC = PubUtils.sendApdu("FB800014000000", 20);
//        Log.e("YJL", "cmd====" + PubUtils.COMMAND_MAGNETIC);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 9:
                        Show3Tracks(msg.obj.toString());
                        break;
                    case 7:
                        Toast.makeText(MagicActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;

                }


            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 1:
                        String result = dealTrackData(msg.obj.toString());
                        Show3Tracks(result);
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                    case 2:
                        Toast.makeText(MagicActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
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
                WorkDev.connectGatt(MagicActivity.this, false, bluetoothGattUtil);
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
                                Log.e("YJL", "DATA==" + data);
                                String result = dealTrackData(data);
                                Show3Tracks(result);
                            }
                        });
                    }

                    @Override
                    public void getDataFail(final int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MagicActivity.this, "" + code, Toast.LENGTH_SHORT).show();
                                prograssBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    @Override
                    public void connectfail() {
                        isCanWrite = false;
                    }
                });
            } else {
                cwsdk.initialize(MagicActivity.this, true, handler);
                cwsdk.workmode = 1;
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
        btc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(4);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(4);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MagicActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.GetMagicCard(timeout);
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

    void Show3Tracks(String strTrack123) {
        Log.e("BJCW", "磁道数据：" + strTrack123 + "数据分割长度：" + strTrack123.split("#").length);
        if (strTrack123.split("#").length > 0 && !TextUtils.isEmpty(strTrack123.split("#")[0])) {
            tv1.setText(strTrack123.split("#")[0]);
        }
        if (strTrack123.split("#").length > 1 && !TextUtils.isEmpty(strTrack123.split("#")[1])) {
            tv2.setText(strTrack123.split("#")[1]);
        }
        if (strTrack123.split("#").length > 2 && !TextUtils.isEmpty(strTrack123.split("#")[2])) {
            tv3.setText(strTrack123.split("#")[2]);
        }

    }

    String dealTrackData(String data) {
        String StrLenArray = data.substring(0, 5);
        byte[] TrackLenArray = BJCWUtil.StrToHex(data);
        int index = 6;

        String Track1 = data.substring(index, index + TrackLenArray[0] * 2);

        index += TrackLenArray[0] * 2;

        String Track2 = data.substring(index, index + TrackLenArray[1] * 2);

        index += TrackLenArray[1] * 2;

        String Track3 = data.substring(index, index + TrackLenArray[2] * 2);

        byte[] bTrack1Array = BJCWUtil.StrToHex(Track1);
        byte[] bTrack2Array = BJCWUtil.StrToHex(Track2);
        byte[] bTrack3Array = BJCWUtil.StrToHex(Track3);

        String STrack1 = new String(bTrack1Array);
        String STrack2 = new String(bTrack2Array);
        String STrack3 = new String(bTrack3Array);

        return STrack1 + "#" + STrack2 + "#" + STrack3;
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
                Log.e("BJCW", "蓝牙状态：" + data.GetBleState());
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


}
