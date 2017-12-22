package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

import cwbj.cwsdk2.R;
import cwbj.cwsdk2.util.BluetoothGattUtil;
import cwbj.cwsdk2.util.PubUtils;
import cwbj.cwsdk2.util.SocketClient;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.sdk.CWSDK;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.DataBeanCallback;
import cwbjsdk.cwsdk.util.ParserIdentity;

import java.io.FileInputStream;

public class IDMessageActivity extends Activity implements View.OnClickListener {


    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";

    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;
    TextView tv = null;
    Button btc = null;
    Handler handler = null;
    ImageView img = null;

    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    public Context mContext;
    private String bmpPath;
    private String wltPath;
    FileInputStream fis = null;
    private int result;

    private BluetoothGattUtil bluetoothGattUtil;
    private boolean isCanWrite;
    private ParserIdentity identitys;

    private SocketClient client;
    Handler wifiHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idmessage);
        mContext = this;
        bmpPath = mContext.getFileStreamPath("zp.bmp").getAbsolutePath();
        wltPath = mContext.getFileStreamPath("photo.wlt").getAbsolutePath();
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_ID);
        img = (ImageView) findViewById(R.id.imageView);
        tv = (TextView) findViewById(R.id.textView);
        btc = (Button) findViewById(R.id.button3);
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
//        initCommand();
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x5) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    showIDIDinfo();
                }

            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case 1:
                        if (msg.obj.toString().length() > 0) {
                            identitys.parserData(msg.obj.toString(), msg.obj.toString().length());
                            DataBean swDataBean = new DataBean();
                            swDataBean.SetPhoto(identitys.getBmpUser());
                            swDataBean.SetInformation(identitys.strIdDate);
                            showIDIDinfo(swDataBean);
                        }
                        break;
                    case 0:
                        //connect服务器失败
                        client.openClientThread();
                        break;
                    case 2:
                        Toast.makeText(IDMessageActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };
        identitys = new ParserIdentity();
        identitys.mContext = this;
        final DataBean swDataBean = new DataBean();
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
                //ble通信
                bluetoothGattUtil = BluetoothGattUtil.getInstance();
                WorkDev.connectGatt(IDMessageActivity.this, false, bluetoothGattUtil);
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
                                if (data.length() > 0) {
                                    identitys.parserData(data, data.length());
                                    swDataBean.SetPhoto(identitys.getBmpUser());
                                    swDataBean.SetInformation(identitys.strIdDate);
                                    prograssBar.setVisibility(View.INVISIBLE);
                                    showIDIDinfo(swDataBean);
                                }
                            }
                        });
                    }

                    @Override
                    public void getDataFail(final int code) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(IDMessageActivity.this, "" + code, Toast.LENGTH_SHORT).show();
                                prograssBar.setVisibility(View.INVISIBLE);
                            }
                        });
//                        bluetoothGattUtil.writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IDCARD[0]));
                    }

                    @Override
                    public void connectfail() {
                        isCanWrite = false;
                    }
                });
            } else {
                cwsdk.initialize(IDMessageActivity.this, true, handler);
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
                    client.sendMsg(3);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(3);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(IDMessageActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        eventMask = 0xF;
                        cwsdk.idReadMessage(timeout);
                    }
                }

            }
        });


    }

    private void initCommand() {
        PubUtils.COMMAND_IDCARD_1 = PubUtils.sendApdu("FB801100000000", 20);
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_IDCARD_1);
        PubUtils.COMMAND_IDCARD_2 = PubUtils.sendApdu("FB810001000000", 5);
        Log.e("YJL", "cmd====" + PubUtils.COMMAND_IDCARD_2);
        PubUtils.COMMAND_IDCARD[0] = PubUtils.COMMAND_IDCARD_1;
        PubUtils.COMMAND_IDCARD[1] = PubUtils.COMMAND_IDCARD_2;
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
        if (null != identitys) {
            if (null != identitys.getBmpUser() && !identitys.getBmpUser().isRecycled()) {
                identitys.getBmpUser().recycle();
                identitys = null;
            }
        }
        System.gc();
        super.onDestroy();
    }

    void showIDIDinfo() {
        if (null != cwsdk.SWdataBean) {
            img.getLayoutParams().height = 500;
            img.getLayoutParams().width = 300;
            String IDinfo = cwsdk.SWdataBean.GetInformation();

            String IDinfoClass[] = IDinfo.split(";");

            tv.setText(IDinfoClass[0] + "\n" + IDinfoClass[1] + "\n" + IDinfoClass[2] + "\n" + IDinfoClass[3] + "\n" + IDinfoClass[4] + "\n" + IDinfoClass[5] + "\n" + IDinfoClass[6] + "\n" + IDinfoClass[7] + "\n");

            img.setImageBitmap(cwsdk.SWdataBean.GetPhoto());
        }
    }

    void showIDIDinfo(DataBean dataBean) {
        if (null != dataBean) {
            img.getLayoutParams().height = 500;
            img.getLayoutParams().width = 300;
            String IDinfo = dataBean.GetInformation();

            String IDinfoClass[] = IDinfo.split(";");

            tv.setText(IDinfoClass[0] + "\n" + IDinfoClass[1] + "\n" + IDinfoClass[2] + "\n" + IDinfoClass[3] + "\n" + IDinfoClass[4] + "\n" + IDinfoClass[5] + "\n" + IDinfoClass[6] + "\n" + IDinfoClass[7] + "\n");

            img.setImageBitmap(dataBean.GetPhoto());
        }
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
                Log.e("CWLOG", "取消操作");

                break;
            }
            case DataBean.CMD_IDEREADMESSAGE_OPERATION: {
                if (data.GetSucceed() == "true") {

                } else {
                    //updateUIMsg("读取身份证失败，是否重新读取？", 4);
                    Toast.makeText(IDMessageActivity.this, "读取身份证超时", Toast.LENGTH_SHORT).show();
                    prograssBar.setVisibility(View.GONE);
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
