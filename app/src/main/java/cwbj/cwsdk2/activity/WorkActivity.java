package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
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
import cwbjsdk.cwsdk.sdk.CWSDK;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.DataBeanCallback;

public class WorkActivity extends Activity implements View.OnClickListener {

    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";

    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;
    TextView tv = null;
    Button btc = null;
    Button btcl = null;
    Handler handler = null;
    ImageView img = null;
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private boolean isContact;

    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;

    private int sendCount = 0;
    String cardNum;
    String cardInfo;
    private SocketClient client;
    Handler wifiHandler = null;
    private Button cancle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work2);
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_clcard);
        tv = (TextView) findViewById(R.id.textViewcn);
        btc = (Button) findViewById(R.id.button);
        btcl = (Button) findViewById(R.id.button2);
        cancle = ((Button) findViewById(R.id.cancle));
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x3) {
//                    prograssBar.setVisibility(View.INVISIBLE);
                    tv.setText("读取卡号为：" + msg.obj.toString());
                } else if (msg.what == 12) {
//                    prograssBar.setVisibility(View.INVISIBLE);
                    tv.setText("取消操作成功");
                }

            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 1:
                        tv.setText("读取卡号为：" + msg.obj.toString());
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                    case 2:
                        Toast.makeText(WorkActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 100:
                        Toast.makeText(WorkActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
//        initCommad();
        if (PubUtils.isWifi) {
            //wifi通信，初始化
            client = new SocketClient();
            //服务端的IP地址和端口号
//                    client.clintValue(getApplicationContext(), "192.168.0.48", 9999);
            client.clintValue(getApplicationContext(), PubUtils.ip, 9999, wifiHandler);
            //开启客户端接收消息线程
            client.openClientThread();
        } else {
            WorkDev = cwsdk.GetBlueToothDevicesByMAC(sMAC);
            if (PubUtils.isBle) {
                //ble通信，初始化
                bluetoothGattUtil = BluetoothGattUtil.getInstance();
                WorkDev.connectGatt(WorkActivity.this, false, bluetoothGattUtil);
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
                                tv.setText("获取到的卡号为：" + data);
                                prograssBar.setVisibility(View.INVISIBLE);

                            }
                        });
                    }

                    @Override
                    public void getDataFail(final int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (code == 100) {
                                    //上电寻卡失败
                                    Toast.makeText(WorkActivity.this, "上电寻卡失败", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(WorkActivity.this, "" + code, Toast.LENGTH_SHORT).show();
                                }
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
                //经典蓝牙初始化
                cwsdk.initialize(WorkActivity.this, true, handler);
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

        //接触ic卡
        btc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubUtils.isContact = true;
//                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    SocketClient.type = 1;
                    client.sendMsg(1);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(1);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(WorkActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.pbocReadInfo(0, timeout);
                    }
                }
            }
        });

        //非接触ic卡
        btcl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubUtils.isContact = false;
//                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    SocketClient.type = 2;
                    //socket通信
                    client.sendMsg(2);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(2);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(WorkActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.pbocReadInfo(1, timeout);
                    }
                }

            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cwsdk.cancle(0);
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub


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
                    Toast.makeText(WorkActivity.this, "读取卡片超时", Toast.LENGTH_SHORT).show();
                    prograssBar.setVisibility(View.GONE);
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

    private void initCommad() {
        PubUtils.COMMAND_IC_CONTACT_1 = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, "poweron", 20), 20);
        PubUtils.COMMAND_IC_CONTACT_2 = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, "00a4040007A0000003330101", 20), 20);
        PubUtils.COMMAND_IC_NOCONTACT_1 = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, "poweron", 20), 20);
        PubUtils.COMMAND_IC_NOCONTACT_2 = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, "00a4040007A0000003330101", 20), 20);
        Log.e("YJL", "COMMAND_IC_CONTACT_1==" + PubUtils.COMMAND_IC_CONTACT_1);
        Log.e("YJL", "COMMAND_IC_CONTACT_2==" + PubUtils.COMMAND_IC_CONTACT_2);
        Log.e("YJL", "COMMAND_IC_NOCONTACT_1==" + PubUtils.COMMAND_IC_NOCONTACT_1);
        Log.e("YJL", "COMMAND_IC_NOCONTACT_2==" + PubUtils.COMMAND_IC_NOCONTACT_2);
        PubUtils.COMMAND_IC_CONTACT[0] = PubUtils.COMMAND_IC_CONTACT_1;
        PubUtils.COMMAND_IC_CONTACT[1] = PubUtils.COMMAND_IC_CONTACT_1;
        PubUtils.COMMAND_IC_NOCONTACT[0] = PubUtils.COMMAND_IC_NOCONTACT_1;
        PubUtils.COMMAND_IC_NOCONTACT[1] = PubUtils.COMMAND_IC_NOCONTACT_2;
    }
}
