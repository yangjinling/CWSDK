package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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

public class FprinterActivity extends Activity {


    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private byte eventMask;
    Handler handler = null;
    Button bt1 = null;
    Button bt2 = null;
    Button bt3 = null;
    AlertDialog.Builder builder = null;
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
        setContentView(R.layout.activity_fprinter);
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        tv_title.setText(R.string.action_Fprinter);
        bt1 = (Button) findViewById(R.id.button4);
        bt2 = (Button) findViewById(R.id.button5);
        bt3 = (Button) findViewById(R.id.button6);
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        builder = new AlertDialog.Builder(this);
//        initCommand();
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
        WorkDev = cwsdk.GetBlueToothDevicesByMAC(sMAC);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x7) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(FprinterActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                }

                if (msg.what == 0x8) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    // tv.setText("读取固件版本号为：" + msg.obj.toString());

                    //AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("指纹数据返回");//设置标题
                    //builder.setIcon(R.drawable);//设置图标
                    builder.setMessage(msg.obj.toString());//
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    AlertDialog dialog = builder.create();//获取dialog
                    dialog.show();//显示对话框

                }


            }
        };
        wifiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                prograssBar.setVisibility(View.INVISIBLE);
                if (msg.what == 7) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(FprinterActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                }

                if (msg.what == 8) {
                    prograssBar.setVisibility(View.INVISIBLE);
                    // tv.setText("读取固件版本号为：" + msg.obj.toString());

                    //AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("指纹数据返回");//设置标题
                    //builder.setIcon(R.drawable);//设置图标
                    builder.setMessage(msg.obj.toString());//
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    AlertDialog dialog = builder.create();//获取dialog
                    dialog.show();//显示对话框

                }
            }
        };
        if (PubUtils.isWifi) {
            //wifi通信，初始化
            client = new SocketClient();
            //服务端的IP地址和端口号
//                    client.clintValue(getApplicationContext(), "192.168.0.48", 9999);
            client.clintValue(getApplicationContext(), "172.16.39.182", 9999, wifiHandler);
            //开启客户端接收消息线程
            client.openClientThread();
        } else {
            if (PubUtils.isBle) {
                bluetoothGattUtil = BluetoothGattUtil.getInstance();
                WorkDev.connectGatt(FprinterActivity.this, false, bluetoothGattUtil);
                bluetoothGattUtil.registerCallBack(new BluetoothGattUtil.connectCallBack() {
                    @Override
                    public void connectSuccess() {
                        isCanWrite = true;
                    }

                    @Override
                    public void dealData(final int type, final String data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prograssBar.setVisibility(View.INVISIBLE);
                                if (type == 0) {
                                    builder.setTitle("指纹数据返回");//设置标题
                                    //builder.setIcon(R.drawable);//设置图标
                                    builder.setMessage(data);//
                                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {

                                        }
                                    });
                                    AlertDialog dialog = builder.create();//获取dialog
                                    dialog.show();//显示对话框
                                } else {
                                    Toast.makeText(FprinterActivity.this, data, Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }

                    @Override
                    public void getDataFail(int code) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                cwsdk.initialize(FprinterActivity.this, true, handler);
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


                eventMask = 0xF;
            }
        }

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(7);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(7);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(FprinterActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        cwsdk.GetFprinterVer(20);
                    }
                }
            }
        });


        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(8);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(8);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(FprinterActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        cwsdk.GetFprinter(20);
                    }
                }
            }
        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                if (PubUtils.isWifi) {
                    //socket通信
                    client.sendMsg(9);
                } else {
                    if (PubUtils.isBle) {
                        if (isCanWrite) {
                            bluetoothGattUtil.writeRXCharacteristic(9);
                        } else {
                            prograssBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(FprinterActivity.this, "蓝牙服务没有开启", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cwsdk.connectionDevice(WorkDev);
                        cwsdk.GetFprinterTest(20);
                    }
                }
            }
        });


    }


    public void initCommand() {
        //指纹模块版本指令
        PubUtils.COMMAND_FINGERVERSION_1 = PubUtils.sendApdu("FB803400000000", 20);
        PubUtils.COMMAND_FINGERVERSION_2 = PubUtils.sendApdu("FB80350500000C000009020004090000000D03", 20);
        PubUtils.COMMAND_FINGERVERSION_3 = PubUtils.sendApdu("00000000000000", 20);
        //指纹模块获取指令
        PubUtils.COMMAND_FINGERMODE_1 = PubUtils.sendApdu("FB803400000000", 20);//
        PubUtils.COMMAND_FINGERMODE_2 = PubUtils.sendApdu("FB80350500000D00000A0200051b000000001e03", 100);
        PubUtils.COMMAND_FINGERMODE_3 = PubUtils.sendApdu("FB80350500000D00000A0200051b000000011f03", 100);
        PubUtils.COMMAND_FINGERMODE_4 = PubUtils.sendApdu("FB80350500000D00000A0200051b000000021c03", 100);
        PubUtils.COMMAND_FINGERMODE_5 = PubUtils.sendApdu("FB80350500000C0000090200041c0300001B03", 100);
        PubUtils.COMMAND_FINGERMODE_6 = PubUtils.sendApdu("00000000000000", 100);
        //指纹特征获取指令
        PubUtils.COMMAND_FINGERFEATURE_1 = PubUtils.sendApdu("FB803400000000", 20);
        PubUtils.COMMAND_FINGERFEATURE_2 = PubUtils.sendApdu("FB80350500000C0000090200040C0100000903", 20);
        PubUtils.COMMAND_FINGERFEATURE_3 = PubUtils.sendApdu("00000000000000", 20);
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
    }
}
