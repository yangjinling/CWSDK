package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.EventLog;
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
import cwbjsdk.cwsdk.util.SMS4Class;

public class KeyPadActivity extends Activity {


    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;

    TextView tv1 = null;
    TextView tv2 = null;
    TextView tv3 = null;

    Button btc1 = null;
    Button btc2 = null;

    Handler handler = null;

    int[] key = {0x11111111, 0x11111111, 0x11111111, 0x11110136};
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;
    private boolean isMi;
    private SocketClient client;
    Handler wifiHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_pad);

        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_KeyPad);
        tv1 = (TextView) findViewById(R.id.textView12);
        tv2 = (TextView) findViewById(R.id.textView15);
        tv3 = (TextView) findViewById(R.id.textView16);
        //明文
        btc1 = (Button) findViewById(R.id.button8);
        //密文
        btc2 = (Button) findViewById(R.id.button9);

        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
//        initCommand();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 10) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    tv1.setText(msg.obj.toString());
                } else if (msg.what == 0x11) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    tv2.setText(msg.obj.toString().split(";")[0]);
                    tv3.setText(msg.obj.toString().split(";")[1]);
                } else if (msg.what == 0x7) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(KeyPadActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (PubUtils.isMi) {
                            SMS4Class smscala = new SMS4Class();
                            if (null != msg.obj.toString() && msg.obj.toString().length() > 0) {
                                if (null != msg.obj.toString()) {
                                    prograssBar.setVisibility(View.INVISIBLE);
                                    int[] cipher = smscala.SMS4ValueToIntArray(msg.obj.toString());
                                    int[] plain1 = smscala.decrypt(cipher, key);
                                    String strplain = smscala.SMS4ValueToString(plain1);
                                    tv2.setText(msg.obj.toString());
                                    tv3.setText(strplain);
                                }
                            }
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            tv1.setText(msg.obj.toString());
                        }
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                    case 2:
                        Toast.makeText(KeyPadActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
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
                WorkDev.connectGatt(KeyPadActivity.this, false, bluetoothGattUtil);
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
                                if (PubUtils.isMi) {
                                    //如果是获取密文的指令
                                    SMS4Class smscala = new SMS4Class();
                                    if (null != data && data.length() > 0) {
                                        prograssBar.setVisibility(View.INVISIBLE);
                                        int[] cipher = smscala.SMS4ValueToIntArray(data);
                                        int[] plain1 = smscala.decrypt(cipher, key);
                                        String strplain = smscala.SMS4ValueToString(plain1);
                                        tv2.setText(data);
                                        tv3.setText(strplain);
                                    }
                                } else {
                                    //不是密文
                                    prograssBar.setVisibility(View.INVISIBLE);
                                    tv1.setText(data.toString());
                                }
                                Log.e("YJL", "data" + data);
                            }
                        });
                    }

                    @Override
                    public void getDataFail(final int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(KeyPadActivity.this, "" + code, Toast.LENGTH_SHORT).show();
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
                cwsdk.initialize(KeyPadActivity.this, true, handler);
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
        //明文
        btc1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PubUtils.isMi = false;
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(5);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(5);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(KeyPadActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.GetKeyPad(timeout, 0);
                    }
                }
            }
        });

        //密文
        btc2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                PubUtils.isMi = true;
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(6);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(6);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(KeyPadActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.GetKeyPad(timeout, 1);
                    }
                }
            }
        });


    }

    public void initCommand() {
        PubUtils.COMMAND_PIN_PLAINTEXT = PubUtils.sendApdu("FB802380000003000001", 20);
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_PIN_PLAINTEXT);
        PubUtils.COMMAND_PIN_CIPHERTEXT_1 = PubUtils.sendApdu("FB8025800000080000000000000000", 20);
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_PIN_CIPHERTEXT_1);
        PubUtils.COMMAND_PIN_CIPHERTEXT_2 = PubUtils.sendApdu("FB802380000003010002", 20);
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_PIN_CIPHERTEXT_2);
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

    private void GetCmdId(DataBean data) {
        int nCmdId = -1;
        nCmdId = data.GetCommId();
        Log.e("BJCW", "操作：" + nCmdId);
        if (nCmdId != DataBean.CMD_WAITEEVENT_OPERATION
                && nCmdId != DataBean.CMD_ICRESETCARD_OPERATION) {

        }
        switch (nCmdId) {
            case DataBean.CMD_OPERATION_CANCEL: {
                Log.e("CWLOG", "取消操作");

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
